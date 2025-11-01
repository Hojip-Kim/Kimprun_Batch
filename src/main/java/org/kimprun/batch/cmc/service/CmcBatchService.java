package org.kimprun.batch.cmc.service;

import org.kimprun.batch.cmc.dto.response.CmcApiStatusResponse;
import org.kimprun.batch.cmc.dto.response.CmcBatchSyncResponse;
import org.kimprun.batch.common.dto.response.BatchHealthResponse;
import org.kimprun.batch.common.dto.response.JobExecutionStatusResponse;
import org.kimprun.batch.common.dto.response.JobHistoryResponse;
import org.kimprun.batch.common.dto.response.RunningJobsResponse;

public interface CmcBatchService {

    /**
     * CoinMarketCap 데이터 전체 동기화 실행
     * @param mode 실행 모드 (manual, scheduled 등)
     * @return 동기화 실행 결과
     */
    CmcBatchSyncResponse runCmcDataSync(String mode);

    /**
     * CMC API 사용률 및 배치 상태 조회
     * @return API 상태 정보
     */
    CmcApiStatusResponse getCmcApiStatus();

    /**
     * 최근 실행된 Job 이력 조회
     * @param limit 조회할 최대 개수
     * @return Job 실행 이력
     */
    JobHistoryResponse getJobHistory(int limit);

    /**
     * 특정 Job 실행 상세 정보 조회
     * @param executionId Job 실행 ID
     * @return Job 실행 상세 정보
     */
    JobExecutionStatusResponse getJobExecutionStatus(Long executionId);

    /**
     * 현재 실행 중인 Job 조회
     * @return 실행 중인 Job 목록
     */
    RunningJobsResponse getRunningJobs();

    /**
     * 배치 작업 헬스 체크
     * @return 배치 시스템 상태
     */
    BatchHealthResponse healthCheck();
}
