package org.kimprun.batch.cmc.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.kimprun.batch.cmc.dto.response.CmcApiStatusResponse;
import org.kimprun.batch.cmc.dto.response.CmcBatchSyncResponse;
import org.kimprun.batch.common.dto.internal.JobExecutionInfo;
import org.kimprun.batch.common.dto.internal.StepExecutionInfo;
import org.kimprun.batch.common.dto.response.BatchHealthResponse;
import org.kimprun.batch.common.dto.response.JobExecutionStatusResponse;
import org.kimprun.batch.common.dto.response.JobHistoryResponse;
import org.kimprun.batch.common.dto.response.RunningJobsResponse;
import org.kimprun.batch.cmc.scheduler.CmcBatchScheduler;
import org.kimprun.batch.cmc.service.CmcBatchService;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class CmcBatchServiceImpl implements CmcBatchService {

    private final JobExplorer jobExplorer;
    private final CmcBatchScheduler cmcBatchScheduler;

    @Override
    public CmcBatchSyncResponse runCmcDataSync(String mode) {
        log.info("=== CMC 데이터 동기화 실행 요청 ===");
        log.info("실행 모드: {}", mode);
        log.info("요청 시간: {}", LocalDateTime.now());

        // 비동기로 배치 실행
        CompletableFuture.runAsync(() -> {
            try {
                cmcBatchScheduler.runManualCmcDataSync();
            } catch (Exception e) {
                log.error("CMC 데이터 동기화 비동기 실행 중 오류 발생", e);
            }
        });

        return CmcBatchSyncResponse.builder()
            .message("CMC 데이터 동기화가 비동기로 시작되었습니다")
            .mode(mode)
            .timestamp(LocalDateTime.now())
            .asyncExecution(true)
            .build();
    }

    @Override
    public CmcApiStatusResponse getCmcApiStatus() {
        String usageStatus = cmcBatchScheduler.getCmcApiUsageStatus();

        return CmcApiStatusResponse.builder()
            .status(usageStatus)
            .timestamp(LocalDateTime.now())
            .build();
    }

    @Override
    public JobHistoryResponse getJobHistory(int limit) {
        List<JobInstance> instances = jobExplorer.findJobInstancesByJobName("cmcDataSyncJob", 0, limit);

        List<JobExecutionInfo> history = instances.stream()
            .flatMap(instance -> jobExplorer.getJobExecutions(instance).stream())
            .sorted((e1, e2) -> e2.getStartTime().compareTo(e1.getStartTime()))
            .limit(limit)
            .map(jobExecution -> {
                Map<String, Object> parameters = new java.util.HashMap<>();
                jobExecution.getJobParameters().getParameters().forEach((key, value) -> {
                    parameters.put(key, value.getValue());
                });

                return new JobExecutionInfo(
                    jobExecution.getId(),
                    jobExecution.getJobInstance().getId(),
                    jobExecution.getStatus().name(),
                    jobExecution.getStartTime(),
                    jobExecution.getEndTime(),
                    jobExecution.getExitStatus().getExitCode(),
                    parameters,
                    null
                );
            })
            .toList();

        return JobHistoryResponse.builder()
            .totalCount(history.size())
            .executions(history)
            .build();
    }

    @Override
    public JobExecutionStatusResponse getJobExecutionStatus(Long executionId) {
        JobExecution jobExecution = jobExplorer.getJobExecution(executionId);

        if (jobExecution == null) {
            throw new IllegalArgumentException("해당 Job 실행 정보를 찾을 수 없습니다: " + executionId);
        }

        Map<String, StepExecutionInfo> steps = new java.util.HashMap<>();
        for (StepExecution step : jobExecution.getStepExecutions()) {
            StepExecutionInfo stepInfo = new StepExecutionInfo(
                step.getStatus().name(),
                step.getReadCount(),
                step.getWriteCount(),
                step.getCommitCount(),
                step.getRollbackCount(),
                step.getFilterCount(),
                step.getStartTime(),
                step.getEndTime(),
                step.getExitStatus().getExitCode()
            );
            steps.put(step.getStepName(), stepInfo);
        }

        return JobExecutionStatusResponse.builder()
            .stepExecutions(steps)
            .build();
    }

    @Override
    public RunningJobsResponse getRunningJobs() {
        List<String> jobNames = jobExplorer.getJobNames();
        List<JobExecutionInfo> runningJobs = jobNames.stream()
            .filter(jobName -> jobName.equals("cmcDataSyncJob"))
            .flatMap(jobName -> jobExplorer.findRunningJobExecutions(jobName).stream())
            .map(jobExecution -> {
                List<String> runningSteps = jobExecution.getStepExecutions().stream()
                    .filter(step -> step.getStatus().isRunning())
                    .map(StepExecution::getStepName)
                    .toList();

                return new JobExecutionInfo(
                    jobExecution.getId(),
                    jobExecution.getJobInstance().getId(),
                    jobExecution.getStatus().name(),
                    jobExecution.getStartTime(),
                    null,
                    null,
                    null,
                    runningSteps
                );
            })
            .toList();

        return RunningJobsResponse.builder()
            .runningJobsCount(runningJobs.size())
            .runningJobs(runningJobs)
            .build();
    }

    @Override
    public BatchHealthResponse healthCheck() {
        List<String> jobNames = jobExplorer.getJobNames();
        boolean hasTargetJob = jobNames.contains("cmcDataSyncJob");

        return BatchHealthResponse.builder()
            .message("배치 시스템이 정상 작동 중입니다")
            .jobRepositoryConnected(true)
            .targetJobExists(hasTargetJob)
            .availableJobs(jobNames)
            .timestamp(LocalDateTime.now())
            .build();
    }
}
