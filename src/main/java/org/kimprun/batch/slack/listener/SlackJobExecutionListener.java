package org.kimprun.batch.slack.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kimprun.batch.slack.service.SlackNotificationService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

/**
 * 배치 작업 실행 시 Slack 알림을 전송하는 Listener
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SlackJobExecutionListener implements JobExecutionListener {

    private final SlackNotificationService slackNotificationService;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("배치 작업 시작: {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("배치 작업 완료: {} (상태: {})",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus());

        // Slack 알림 전송
        slackNotificationService.notifyJobCompletion(jobExecution);
    }
}
