package org.kimprun.batch.common.ratelimit.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kimprun.batch.common.ratelimit.DistributedRateLimiter;
import org.kimprun.batch.common.ratelimit.RateLimitResult;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Redisson 기반 분산 Rate Limiter 구현체
 *
 * 기존의 복잡한 Lua 스크립트 대신 Redisson의 RRateLimiter를 사용하여
 * 간단하고 안정적인 Rate Limiting 기능을 제공
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DistributedRateLimiterImpl implements DistributedRateLimiter {

    private final RedissonClient redissonClient;
    private final String serverInstanceId;

    // 로컬 멀티스레드 간 Redis 접근 직렬화용 락
    private final Object rateLimitLock = new Object();

    /**
     * CoinMarketCap API Rate Limiter (동시성 보장)
     *
     * @return {허용여부, 남은요청수}
     */
    @Override
    public RateLimitResult tryAcquireCmcApiLimit() {
        return tryAcquireWithRetry("cmc-api", 30, 60, 3); // 1분당 30회, 최대 3회 재시도
    }

    /**
     * CoinMarketCap API Rate Limiter (blocking)
     * permit이 사용 가능해질 때까지 최대 timeout만큼 대기
     *
     * @param timeoutSeconds 최대 대기 시간 (초)
     * @return {허용여부, 남은요청수}
     */
    @Override
    public RateLimitResult tryAcquireCmcApiLimitBlocking(long timeoutSeconds) {
        String rateLimiterKey = "kimprun:ratelimit:cmc-api";
        int limit = 30;
        int windowSeconds = 60;

        // 로컬 멀티스레드 간 Redis 접근 직렬화 (네트워크 레이턴시 race condition 방지)
        synchronized (rateLimitLock) {
            try {
                RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterKey);

                // Rate Limiter 설정 (초기화는 한 번만 수행됨)
                rateLimiter.trySetRate(RateType.OVERALL, limit, windowSeconds, RateIntervalUnit.SECONDS);

                // permit이 사용 가능해질 때까지 blocking (최대 timeoutSeconds 대기)
                boolean acquired = rateLimiter.tryAcquire(1, timeoutSeconds, TimeUnit.SECONDS);

                if (acquired) {
                    long remainingPermits = rateLimiter.availablePermits();
                    log.info("CMC API Rate Limit 통과 (blocking) - 서버: {}, 남은 permits: {}/{}",
                        serverInstanceId, remainingPermits, limit);
                    return new RateLimitResult(true, remainingPermits, limit, windowSeconds);
                } else {
                    log.error("CMC API Rate Limit 타임아웃 - 서버: {}, 대기시간: {}초 초과",
                        serverInstanceId, timeoutSeconds);
                    return new RateLimitResult(false, 0, limit, windowSeconds);
                }

            } catch (Exception e) {
                log.error("CMC API Rate Limit (blocking) 확인 중 오류 발생 - 서버: {}",
                    serverInstanceId, e);
                return new RateLimitResult(false, 0, limit, windowSeconds);
            }
        }
    }

    /**
     * 재시도 로직이 포함된 Rate Limiter (동시성 문제 해결)
     *
     * @param resource 리소스 식별자
     * @param limit 제한 수
     * @param windowSeconds 시간 윈도우 (초)
     * @param maxRetries 최대 재시도 횟수
     * @return 제한 결과
     */
    @Override
    public RateLimitResult tryAcquireWithRetry(String resource, int limit, int windowSeconds, int maxRetries) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                RateLimitResult result = tryAcquire(resource, limit, windowSeconds);

                // 성공적으로 결과를 얻었으면 반환
                if (result != null) {
                    if (attempt > 1) {
                        log.info("Rate Limit 재시도 성공 - 시도: {}/{}, 리소스: {}, 결과: {}",
                            attempt, maxRetries, resource, result.isAllowed());
                    }
                    return result;
                }

            } catch (Exception e) {
                lastException = e;
                log.warn("Rate Limit 시도 실패 - 시도: {}/{}, 리소스: {}, 오류: {}",
                    attempt, maxRetries, resource, e.getMessage());

                if (attempt < maxRetries) {
                    try {
                        // 지수적 백오프: 50ms, 100ms, 200ms
                        Thread.sleep(50L * (1L << (attempt - 1)));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }

        log.error("Rate Limit 모든 재시도 실패 - 리소스: {}, 최종 차단", resource, lastException);
        return new RateLimitResult(false, 0, limit, windowSeconds);
    }

    /**
     * CoinMarketCap API Rate Limit 체크 (카운터 증가 없음)
     *
     * @return {허용여부, 남은요청수}
     */
    @Override
    public RateLimitResult checkCmcApiLimit() {
        return checkRateLimit("cmc-api", 30, 60);
    }

    /**
     * 일반적인 Rate Limiting (Redisson 기반)
     *
     * @param resource 리소스 식별자
     * @param limit 제한 수
     * @param windowSeconds 시간 윈도우 (초)
     * @return 제한 결과
     */
    @Override
    public RateLimitResult tryAcquire(String resource, int limit, int windowSeconds) {
        String rateLimiterKey = "kimprun:ratelimit:" + resource;

        // 로컬 멀티스레드 간 Redis 접근 직렬화 (네트워크 레이턴시 race condition 방지)
        synchronized (rateLimitLock) {
            try {
                RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterKey);

                // Rate Limiter 설정 (초기화는 한 번만 수행됨)
                rateLimiter.trySetRate(RateType.OVERALL, limit, windowSeconds, RateIntervalUnit.SECONDS);

                // 1개의 permit 획득 시도 (즉시 반환)
                boolean acquired = rateLimiter.tryAcquire(1, 0, TimeUnit.SECONDS);

                if (acquired) {
                    // 남은 permits 수 계산 (근사치)
                    long remainingPermits = rateLimiter.availablePermits();

                    log.debug("Rate Limit 통과 - 서버: {}, 리소스: {}, 남은 permits: {}/{}",
                        serverInstanceId, resource, remainingPermits, limit);

                    return new RateLimitResult(true, remainingPermits, limit, windowSeconds);
                } else {
                    log.warn("Rate Limit 초과 - 서버: {}, 리소스: {}, 제한: {} requests/{}초",
                        serverInstanceId, resource, limit, windowSeconds);

                    return new RateLimitResult(false, 0, limit, windowSeconds);
                }

            } catch (Exception e) {
                log.error("Rate Limit 확인 중 오류 발생 - 서버: {}, 리소스: {}",
                    serverInstanceId, resource, e);

                // 오류 시 안전하게 차단
                return new RateLimitResult(false, 0, limit, windowSeconds);
            }
        }
    }

    /**
     * Rate Limit 상태 확인 (permit 소모 없이 체크만)
     *
     * @param resource 리소스 식별자
     * @param limit 제한 수
     * @param windowSeconds 시간 윈도우 (초)
     * @return 현재 상태
     */
    private RateLimitResult checkRateLimit(String resource, int limit, int windowSeconds) {
        String rateLimiterKey = "kimprun:ratelimit:" + resource;

        try {
            RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterKey);

            // Rate Limiter 설정 확인/초기화
            rateLimiter.trySetRate(RateType.OVERALL, limit, windowSeconds, RateIntervalUnit.SECONDS);

            // 현재 사용 가능한 permits 수
            long availablePermits = rateLimiter.availablePermits();
            boolean allowed = availablePermits > 0;

            log.debug("Rate Limit 상태 확인 - 서버: {}, 리소스: {}, 사용가능: {}/{}, 허용: {}",
                serverInstanceId, resource, availablePermits, limit, allowed);

            return new RateLimitResult(allowed, availablePermits, limit, windowSeconds);

        } catch (Exception e) {
            log.error("Rate Limit 상태 확인 중 오류 발생 - 리소스: {}", resource, e);
            return new RateLimitResult(false, 0, limit, windowSeconds);
        }
    }

    /**
     * 현재 사용률 조회 (Redisson 기반)
     *
     * @param resource 리소스 식별자
     * @param windowSeconds 시간 윈도우 (사용하지 않음 - Redisson이 내부 관리)
     * @return 현재 사용 가능한 permits 수
     */
    @Override
    public long getCurrentUsage(String resource, int windowSeconds) {
        String rateLimiterKey = "kimprun:ratelimit:" + resource;

        try {
            RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterKey);

            // 사용 가능한 permits 수 반환
            return rateLimiter.availablePermits();

        } catch (Exception e) {
            log.error("Rate Limit 사용률 조회 중 오류 발생 - 리소스: {}", resource, e);
            return 0L;
        }
    }

    /**
     * Rate Limit 초기화 (테스트/긴급상황용)
     *
     * @param resource 리소스 식별자
     */
    @Override
    public void resetRateLimit(String resource) {
        String rateLimiterKey = "kimprun:ratelimit:" + resource;

        try {
            RRateLimiter rateLimiter = redissonClient.getRateLimiter(rateLimiterKey);

            // Rate Limiter 삭제
            rateLimiter.delete();

            log.info("Rate Limit 초기화 완료 - 서버: {}, 리소스: {}", serverInstanceId, resource);

        } catch (Exception e) {
            log.error("Rate Limit 초기화 중 오류 발생 - 리소스: {}", resource, e);
        }
    }
}
