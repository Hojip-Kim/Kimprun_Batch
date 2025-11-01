package org.kimprun.batch.config.transaction;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

/**
 * 트랜잭션 매니저 설정
 *
 * Batch 서버는 MyBatis + Spring Batch 사용
 */
@Configuration
@EnableTransactionManagement
public class TransactionConfig {

    /**
     * 트랜잭션 매니저
     * MyBatis와 Spring Batch 모두 이 트랜잭션 매니저 사용
     */
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
}
