package org.kimprun.batch.cmc.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kimprun.batch.slack.listener.SlackJobExecutionListener;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CmcCoinBatchJob {

    private final JobRepository jobRepository;
    private final SlackJobExecutionListener slackJobExecutionListener;

    private final Step coinMapSyncStep;
    private final Step coinLatestInfoSyncStep;
    private final Step coinDetailInfoSyncStep;
    private final Step exchangeMapSyncStep;
    private final Step exchangeDetailInfoSyncStep;
    private final Step coinInfoBulkStep;
    private final Step coinMetaStep;
    private final Step coinMappingStep;

    @Bean("cmcDataSyncJob")
    public Job cmcDataSyncJob() {
        return new JobBuilder("cmcDataSyncJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(new JobExecutionListener() {
                    @Override
                    public void beforeJob(JobExecution jobExecution) {
                        log.info("=== CoinMarketCap 데이터 동기화 Job 시작 ===");
                        log.info("Job 시작 시간: {}", LocalDateTime.now());
                        log.info("Job ID: {}", jobExecution.getJobId());
                    }

                    @Override
                    public void afterJob(JobExecution jobExecution) {
                        log.info("=== CoinMarketCap 데이터 동기화 Job 완료 ===");
                        log.info("Job 종료 시간: {}", LocalDateTime.now());
                        log.info("Job 상태: {}", jobExecution.getStatus());
                        log.info("Job 실행 시간: {}ms", jobExecution.getEndTime().getNano() - jobExecution.getStartTime().getNano());
                    }
                })
                .listener(slackJobExecutionListener)  // Slack 알림 Listener 추가
                // Step 순차 실행: 코인 맵 -> 거래소 맵 -> 거래소 상세 정보 -> CmcCoinInfo 일괄 -> CmcCoinMeta -> 코인 최신 정보 -> 코인 상세 정보 -> 코인 매핑
                .start(coinMapSyncStep)
                .next(exchangeMapSyncStep)
                .next(exchangeDetailInfoSyncStep)
                .next(coinInfoBulkStep)
                .next(coinMetaStep)
                .next(coinLatestInfoSyncStep)
                .next(coinDetailInfoSyncStep)
                .next(coinMappingStep)
                .build();
    }
}
