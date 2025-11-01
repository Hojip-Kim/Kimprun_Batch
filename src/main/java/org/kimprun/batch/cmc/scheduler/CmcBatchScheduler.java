package org.kimprun.batch.cmc.scheduler;

import org.kimprun.batch.cmc.dao.CmcBatchDao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class CmcBatchScheduler {

    private final JobLauncher jobLauncher;
    private final Job cmcDataSyncJob;
    private final CmcBatchDao cmcBatchDao;

    /**
     * 매일 새벽 2시에 CoinMarketCap 데이터 동기화 실행
     * CoinMarketCap 데이터가 보통 UTC 기준으로 갱신되므로 한국시간 새벽 2시에 실행
     */
    @Scheduled(cron = "0 0 2 * * ?", zone = "Asia/Seoul")
    public void runCmcDataSyncJob() {
        try {
            log.info("CMC 데이터 동기화 시작");
            log.info("실행 시간: {}", LocalDateTime.now());

            // 동기화 필요 여부 사전 확인
            boolean coinMapSync = cmcBatchDao.shouldRunCoinMapSync();
            boolean coinInfoSync = cmcBatchDao.shouldRunCoinInfoSync();
            boolean exchangeSync = cmcBatchDao.shouldRunExchangeSync();
            boolean coinRankSync = cmcBatchDao.shouldRunCoinRankSync();
            boolean coinMetaSync = cmcBatchDao.shouldRunCoinMetaSync();

            log.info("동기화 필요 여부 - 코인 맵: {}, 코인 상세: {}, 거래소: {}, 코인 랭킹: {}, 코인 메타: {}",
                    coinMapSync, coinInfoSync, exchangeSync, coinRankSync, coinMetaSync);

            if (!coinMapSync && !coinInfoSync && !exchangeSync && !coinRankSync && !coinMetaSync) {
                log.info("모든 데이터가 최신 상태입니다. 배치 작업을 건너뜁니다.");
                return;
            }

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLocalDateTime("executeTime", LocalDateTime.now())
                    .toJobParameters();

            jobLauncher.run(cmcDataSyncJob, jobParameters);

            log.info("CMC 데이터 동기화 완료");

        } catch (Exception e) {
            log.error("CoinMarketCap 데이터 동기화 중 오류 발생", e);
        }
    }

    /**
     * 수동 실행용 메서드 (Controller에서 호출)
     */
    public void runManualCmcDataSync() {
        try {
            log.info("수동 CMC 데이터 동기화 시작");

            JobParameters jobParameters = new JobParametersBuilder()
                    .addLocalDateTime("executeTime", LocalDateTime.now())
                    .addString("mode", "manual")
                    .toJobParameters();

            jobLauncher.run(cmcDataSyncJob, jobParameters);

            log.info("수동 CMC 데이터 동기화 완료");

        } catch (Exception e) {
            log.error("수동 CMC 데이터 동기화 중 오류 발생", e);
            throw new RuntimeException("배치 실행 실패: " + e.getMessage(), e);
        }
    }

    /**
     * CMC API 사용 현황 조회
     */
    public String getCmcApiUsageStatus() {
        try {
            long coinCount = cmcBatchDao.getCmcCoinCount();
            long exchangeCount = cmcBatchDao.getCmcExchangeCount();

            return String.format("총 코인: %d개, 총 거래소: %d개", coinCount, exchangeCount);

        } catch (Exception e) {
            log.error("CMC API 사용 현황 조회 실패", e);
            return "조회 실패: " + e.getMessage();
        }
    }
}
