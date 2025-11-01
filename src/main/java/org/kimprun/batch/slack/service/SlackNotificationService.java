package org.kimprun.batch.slack.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kimprun.batch.slack.dto.SlackAttachment;
import org.kimprun.batch.slack.dto.SlackField;
import org.kimprun.batch.slack.dto.SlackMessage;
import org.kimprun.batch.slack.port.SlackNotificationPort;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Slack 알림 전송을 위한 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SlackNotificationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final SlackNotificationPort slackNotificationPort;

    /**
     * 배치 작업 완료 알림을 전송합니다.
     *
     * @param jobExecution 완료된 작업 실행 정보
     */
    public void notifyJobCompletion(JobExecution jobExecution) {
        try {
            SlackMessage message = buildJobCompletionMessage(jobExecution);
            slackNotificationPort.sendMessage(message);
        } catch (Exception e) {
            log.error("배치 작업 완료 알림 전송 중 오류 발생", e);
        }
    }

    /**
     * 배치 작업 완료 메시지를 생성합니다.
     *
     * @param jobExecution 작업 실행 정보
     * @return Slack 메시지
     */
    private SlackMessage buildJobCompletionMessage(JobExecution jobExecution) {
        String jobName = jobExecution.getJobInstance().getJobName();
        String status = jobExecution.getStatus().toString();
        boolean isSuccess = jobExecution.getStatus() == BatchStatus.COMPLETED;

        // 실행 시간 계산
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();
        Duration duration = Duration.between(startTime, endTime);

        // 메시지 텍스트
        String messageText = String.format("*[Batch Job %s]*\n`%s`",
                isSuccess ? "완료 ✅" : "실패 ❌",
                jobName);

        // 필드 생성
        List<SlackField> fields = new ArrayList<>();
        fields.add(SlackField.of("상태", status));
        fields.add(SlackField.of("시작 시간", startTime.format(DATE_TIME_FORMATTER)));
        fields.add(SlackField.of("종료 시간", endTime.format(DATE_TIME_FORMATTER)));
        fields.add(SlackField.of("실행 시간", formatDuration(duration)));

        // Step 정보 추가
        Collection<StepExecution> stepExecutions = jobExecution.getStepExecutions();
        if (!stepExecutions.isEmpty()) {
            fields.add(SlackField.longField("Step 정보", buildStepInfo(stepExecutions)));
        }

        // Exit 상태 추가
        if (jobExecution.getExitStatus().getExitDescription() != null
                && !jobExecution.getExitStatus().getExitDescription().isEmpty()) {
            fields.add(SlackField.longField("Exit Description",
                    jobExecution.getExitStatus().getExitDescription()));
        }

        // Attachment 생성
        SlackAttachment attachment = SlackAttachment.builder()
                .color(isSuccess ? "good" : "danger")
                .title("배치 작업 실행 결과")
                .fields(fields)
                .footer("Kimprun Batch System")
                .timestamp(endTime.atZone(ZoneId.systemDefault()).toEpochSecond())
                .build();

        return SlackMessage.builder()
                .text(messageText)
                .attachments(List.of(attachment))
                .build();
    }

    /**
     * Step 실행 정보를 문자열로 변환합니다.
     *
     * @param stepExecutions Step 실행 정보 목록
     * @return Step 정보 문자열
     */
    private String buildStepInfo(Collection<StepExecution> stepExecutions) {
        StringBuilder sb = new StringBuilder();
        for (StepExecution stepExecution : stepExecutions) {
            sb.append(String.format("• %s: %s (Read: %d, Write: %d)\n",
                    stepExecution.getStepName(),
                    stepExecution.getStatus(),
                    stepExecution.getReadCount(),
                    stepExecution.getWriteCount()));
        }
        return sb.toString();
    }

    /**
     * Duration을 읽기 쉬운 형식으로 변환합니다.
     *
     * @param duration Duration 객체
     * @return 포맷된 문자열
     */
    private String formatDuration(Duration duration) {
        long seconds = duration.getSeconds();
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        long millis = duration.toMillis() % 1000;

        if (hours > 0) {
            return String.format("%d시간 %d분 %d초", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%d분 %d초", minutes, secs);
        } else if (secs > 0) {
            return String.format("%d.%03d초", secs, millis);
        } else {
            return String.format("%dms", millis);
        }
    }
}
