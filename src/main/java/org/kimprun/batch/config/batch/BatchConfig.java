package org.kimprun.batch.config.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.context.annotation.Configuration;

/**
 * Spring Batch 설정 클래스
 *
 * Spring Boot Auto Configuration 활용:
 * - JobRepository: application.yml의 spring.batch.jdbc 설정 적용
 * - JobLauncher: 기본적으로 동기 실행
 * - JobExplorer: Job 실행 정보 조회
 * - TransactionManager: TransactionConfig에서 별도 설정
 */
@Slf4j
@Configuration
@EnableBatchProcessing
public class BatchConfig {

}
