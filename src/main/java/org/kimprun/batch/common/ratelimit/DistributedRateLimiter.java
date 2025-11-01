package org.kimprun.batch.common.ratelimit;

/**
 * Redis 기반 Rate Limiter 인터페이스
 *
 * 여러 서버 인스턴스에서 공통으로 API 호출 제한을 관리
 */
public interface DistributedRateLimiter {

    /**
     * CoinMarketCap API Rate Limiter (동시성 보장)
     *
     * @return {허용여부, 남은요청수}
     */
    RateLimitResult tryAcquireCmcApiLimit();

    /**
     * CoinMarketCap API Rate Limiter (blocking)
     * permit이 사용 가능해질 때까지 최대 timeout만큼 대기
     *
     * @param timeoutSeconds 최대 대기 시간 (초)
     * @return {허용여부, 남은요청수}
     */
    RateLimitResult tryAcquireCmcApiLimitBlocking(long timeoutSeconds);

    /**
     * 재시도 로직이 포함된 Rate Limiter (동시성 문제 해결)
     *
     * @param resource 리소스 식별자
     * @param limit 제한 수
     * @param windowSeconds 시간(초)
     * @param maxRetries 최대 재시도 횟수
     * @return 제한 결과
     */
    RateLimitResult tryAcquireWithRetry(String resource, int limit, int windowSeconds, int maxRetries);

    /**
     * CoinMarketCap API Rate Limit 체크 (카운터 증가 없음)
     * @return {허용여부, 남은요청수}
     */
    RateLimitResult checkCmcApiLimit();

    /**
     * 일반적인 Rate Limiting
     *
     * @param resource 리소스 식별자
     * @param limit 제한 수
     * @param windowSeconds 시간(초)
     * @return 제한 결과
     */
    RateLimitResult tryAcquire(String resource, int limit, int windowSeconds);

    /**
     * 현재 사용률 조회
     *
     * @param resource 리소스 식별자
     * @param windowSeconds 시간 윈도우
     * @return 현재 사용 중인 요청 수
     */
    long getCurrentUsage(String resource, int windowSeconds);

    /**
     * Rate Limit 초기화 (긴급상황용)
     *
     * @param resource 리소스 식별자
     */
    void resetRateLimit(String resource);
}
