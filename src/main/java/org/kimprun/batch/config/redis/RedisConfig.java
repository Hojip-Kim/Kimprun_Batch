package org.kimprun.batch.config.redis;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 및 Redisson 설정
 * Batch 서버용 - Rate Limiter 전용
 */
@Configuration
public class RedisConfig {

    @Value("${spring.application.name}")
    private String applicationName;

    @Value("${server.port}")
    private String serverPort;

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    /**
     * 기존 Spring Data Redis Template (캐시 용도)
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // key, value 문자열로 serialization.
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        return template;
    }

    /**
     * Redisson Client 설정
     * 분산락과 Rate Limiter를 위한 클라이언트
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();

        // 단일 Redis 서버 설정
        config.useSingleServer()
            .setAddress("redis://" + redisHost + ":" + redisPort)
            .setConnectionPoolSize(64)  // 연결 풀 크기
            .setConnectionMinimumIdleSize(24)  // 최소 유휴 연결
            .setConnectTimeout(10000)  // 연결 타임아웃 10초
            .setTimeout(3000)  // 명령 타임아웃 3초
            .setRetryAttempts(3)  // 재시도 횟수
            .setRetryInterval(1500)  // 재시도 간격 1.5초
            .setKeepAlive(true)  // Keep-alive 활성화
            .setPingConnectionInterval(30000);  // Ping 간격 30초

        // Lock 관련 설정
        config.setLockWatchdogTimeout(30000L);  // Lock 워치독 타임아웃 - 30초
        config.setNettyThreads(32);  // Netty 스레드 수

        return Redisson.create(config);
    }

    /**
     * 서버 인스턴스 고유 ID 생성
     * Redisson 락에서 락 소유자 식별용
     */
    @Bean
    public String serverInstanceId() {
        String hostName = System.getenv("HOSTNAME"); // 쿠버네티스 파드 이름
        if (hostName == null) {
            hostName = System.getProperty("user.name", "unknown");
        }
        return applicationName + ":" + hostName + ":" + serverPort;
    }
}
