package org.kimprun.batch.cmc.controller;

import org.kimprun.batch.cmc.dto.response.CmcApiStatusResponse;
import org.kimprun.batch.cmc.dto.response.CmcBatchSyncResponse;
import org.kimprun.batch.common.dto.response.BatchHealthResponse;
import org.kimprun.batch.common.dto.response.JobExecutionStatusResponse;
import org.kimprun.batch.common.dto.response.JobHistoryResponse;
import org.kimprun.batch.common.dto.response.RunningJobsResponse;
import org.kimprun.batch.exception.dto.ApiResponse;
import org.kimprun.batch.cmc.service.CmcBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/batch/cmc")
@RequiredArgsConstructor
public class CmcBatchController {

    private final CmcBatchService cmcBatchService;

    /**
     * CoinMarketCap 데이터 전체 동기화 실행
     */
    @PostMapping("/sync")
    public ApiResponse<CmcBatchSyncResponse> runCmcDataSync(
            @RequestParam(defaultValue = "manual") String mode) {

        try {
            CmcBatchSyncResponse response = cmcBatchService.runCmcDataSync(mode);
            return ApiResponse.success(response);

        } catch (RuntimeException e) {
            log.error("CMC 배치 실행 중 오류 발생", e);
            return ApiResponse.error(500, "BATCH_EXECUTION_ERROR", e.getMessage());

        } catch (Exception e) {
            log.error("예상치 못한 오류 발생", e);
            return ApiResponse.error(500, "UNEXPECTED_ERROR", "배치 작업 실행 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * CMC API 사용률 및 배치 상태 조회
     */
    @GetMapping("/api-status")
    public ApiResponse<CmcApiStatusResponse> getCmcApiStatus() {
        try {
            CmcApiStatusResponse response = cmcBatchService.getCmcApiStatus();
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("CMC API 상태 조회 중 오류 발생", e);
            return ApiResponse.error(500, "INTERNAL_ERROR", "CMC API 상태 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 최근 실행된 Job 이력 조회 (최대 10개)
     */
    @GetMapping("/history")
    public ApiResponse<JobHistoryResponse> getJobHistory(
            @RequestParam(defaultValue = "10") int limit) {

        try {
            JobHistoryResponse response = cmcBatchService.getJobHistory(limit);
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("Job 이력 조회 중 오류 발생", e);
            return ApiResponse.error(500, "INTERNAL_ERROR", "Job 이력 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 특정 Job 실행 상세 정보 조회
     */
    @GetMapping("/execution/{executionId}")
    public ApiResponse<JobExecutionStatusResponse> getJobExecutionStatus(
            @PathVariable Long executionId) {

        try {
            JobExecutionStatusResponse response = cmcBatchService.getJobExecutionStatus(executionId);
            return ApiResponse.success(response);

        } catch (IllegalArgumentException e) {
            log.error("Job 실행 정보를 찾을 수 없음: {}", executionId, e);
            return ApiResponse.error(404, "NOT_FOUND", e.getMessage());

        } catch (Exception e) {
            log.error("Job 실행 상세 정보 조회 중 오류 발생", e);
            return ApiResponse.error(500, "INTERNAL_ERROR", "Job 실행 상세 정보 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 현재 실행 중인 Job 조회
     */
    @GetMapping("/running")
    public ApiResponse<RunningJobsResponse> getRunningJobs() {
        try {
            RunningJobsResponse response = cmcBatchService.getRunningJobs();
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("실행 중인 Job 조회 중 오류 발생", e);
            return ApiResponse.error(500, "INTERNAL_ERROR", "실행 중인 Job 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 배치 작업 헬스 체크
     */
    @GetMapping("/health")
    public ApiResponse<BatchHealthResponse> healthCheck() {
        try {
            BatchHealthResponse response = cmcBatchService.healthCheck();
            return ApiResponse.success(response);

        } catch (Exception e) {
            log.error("배치 시스템 헬스 체크 중 오류 발생", e);
            return ApiResponse.error(500, "INTERNAL_ERROR", "배치 시스템에 문제가 있습니다: " + e.getMessage());
        }
    }
}
