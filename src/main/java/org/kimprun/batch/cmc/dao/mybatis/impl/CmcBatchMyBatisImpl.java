package org.kimprun.batch.cmc.dao.mybatis.impl;

import org.kimprun.batch.cmc.dao.CmcBatchDao;
import org.kimprun.batch.cmc.dao.mybatis.CmcBatchMapper;
import org.kimprun.batch.cmc.dto.internal.coin.CmcApiDataDto;
import org.kimprun.batch.cmc.dto.internal.coin.CmcCoinInfoDataDto;
import org.kimprun.batch.cmc.dto.internal.coin.CmcCoinMapDataDto;
import org.kimprun.batch.cmc.dto.internal.coin.CmcDataPlatformDto;
import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeDto;
import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeDetailDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Repository("cmcBatchDao")
@Slf4j
public class CmcBatchMyBatisImpl implements CmcBatchDao {

    private final CmcBatchMapper cmcBatchMapper;

    public CmcBatchMyBatisImpl(CmcBatchMapper cmcBatchMapper) {
        this.cmcBatchMapper = cmcBatchMapper;
    }

    @Override
    @Transactional
    public void upsertCmcCoinMap(List<CmcCoinMapDataDto> coinMapList) {
        try {
            // 기본값 처리는 MyBatis XML에서 COALESCE로 처리
            cmcBatchMapper.upsertCmcCoinMap(coinMapList);
            log.info("코인 맵 데이터 {} 건 업서트 완료", coinMapList.size());
        } catch (Exception e) {
            log.error("코인 맵 데이터 업서트 중 오류 발생", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void upsertCmcCoinMapRank(List<CmcCoinMapDataDto> rankedCoins) {
        try {
            // 유효한 랭킹 데이터만 필터링
            List<CmcCoinMapDataDto> validRankedCoins = rankedCoins.stream()
                .filter(coin -> coin.getId() != null && coin.getRank() != null && coin.getRank() > 0)
                .toList();

            if (validRankedCoins.isEmpty()) {
                log.warn("업데이트할 유효한 랭킹 데이터가 없습니다.");
                return;
            }

            // 랭킹 데이터 UPSERT
            cmcBatchMapper.upsertCmcCoinMapRank(validRankedCoins);
            log.info("코인 맵 랭킹 데이터 {} 건 UPSERT 완료", validRankedCoins.size());
            
            // 코인 테이블에 랭킹 FK 업데이트
            cmcBatchMapper.updateCmcCoinMapRankFk(validRankedCoins);
            log.info("코인 테이블 랭킹 FK {} 건 업데이트 완료", validRankedCoins.size());
            
        } catch (Exception e) {
            log.error("코인 맵 랭킹 데이터 업데이트 중 오류 발생", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void updateCmcCoinLatestInfo(List<CmcApiDataDto> latestInfoList) {
        try {
            // 유효한 데이터만 필터링 (cmc_coin 테이블에 존재하는 코인만)
            List<CmcApiDataDto> validItems = latestInfoList.stream()
                .filter(coin -> coin.getId() != null && coin.getCmcRank() != null && coin.getCmcRank() > 0)
                .filter(coin -> cmcBatchMapper.existsCmcCoin(coin.getId()))
                .toList();

            if (validItems.isEmpty()) {
                log.warn("업데이트할 유효한 코인 랭킹 데이터가 없습니다.");
                return;
            }

            // 랭킹 데이터 UPSERT
            cmcBatchMapper.upsertCmcCoinRank(validItems);
            log.info("코인 랭킹 데이터 {} 건 UPSERT 완료", validItems.size());
            
            // 코인 테이블에 랭킹 FK 업데이트
            cmcBatchMapper.updateCmcCoinRankFk(validItems);
            log.info("코인 테이블 랭킹 FK {} 건 업데이트 완료", validItems.size());
            
        } catch (Exception e) {
            log.error("코인 랭킹 데이터 업데이트 중 오류 발생", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void upsertCmcCoinInfo(List<CmcCoinInfoDataDto> coinInfoList) {
        int processedCount = 0;
        
        for (CmcCoinInfoDataDto info : coinInfoList) {
            if (info.getId() != null) {
                try {
                    // 기본값 처리는 MyBatis XML에서 COALESCE로 처리
                    
                    // CmcCoinInfo 테이블에 상세 정보 저장
                    cmcBatchMapper.insertCmcCoinInfo(info);
                    
                    // CmcCoin 테이블에 외래키와 logo 정보 업데이트 (coinInfoId는 XML에서 처리)
                    cmcBatchMapper.updateCmcCoinWithCoinInfo(null, info);
                    processedCount++;
                        
                } catch (Exception e) {
                    log.error("코인 상세 정보 처리 중 오류 발생 - coin_id: {}", info.getId(), e);
                }
            }
        }

        log.info("코인 상세 정보 {} 건 업서트 완료", processedCount);
    }

    @Override
    public List<Long> getCmcCoinIds(int limit) {
        return cmcBatchMapper.getCmcCoinIds(limit);
    }

    @Override
    @Transactional
    public void upsertCmcExchangeMap(List<CmcExchangeDto> exchangeMapList) {
        try {
            cmcBatchMapper.upsertCmcExchangeMap(exchangeMapList);
            log.info("거래소 맵 데이터 {} 건 업서트 완료", exchangeMapList.size());
        } catch (Exception e) {
            log.error("거래소 맵 데이터 업서트 중 오류 발생", e);
            throw e;
        }
    }

    @Override
    @Transactional
    public void upsertCmcExchangeInfo(List<CmcExchangeDetailDto> exchangeInfoList) {
        int processedCount = 0;
        
        for (CmcExchangeDetailDto info : exchangeInfoList) {
            if (info.getId() != null) {
                try {
                    // ExchangeInfo 처리 (cmc_exchange_id 기반으로 기존 정보 확인 후 INSERT/UPDATE)
                    String fiatsData = (info.getFiats() != null) ? info.getFiats().toString() : "";
                    Long exchangeInfoId = cmcBatchMapper.getExchangeInfoIdByExchangeId(info.getId());
                    
                    if (exchangeInfoId == null) {
                        // 기존 정보가 없으면 INSERT 후 cmc_exchange 테이블에 FK 연결
                        cmcBatchMapper.insertCmcExchangeInfo(info.getId(), fiatsData);
                        exchangeInfoId = getLastInsertedId("cmc_exchange_info");
                    } else {
                        // 기존 정보가 있으면 UPDATE
                        cmcBatchMapper.updateCmcExchangeInfo(exchangeInfoId, fiatsData);
                    }
                    
                    // ExchangeMeta 처리 (cmc_exchange_id 기반으로 기존 정보 확인 후 INSERT/UPDATE)
                    Long exchangeMetaId = cmcBatchMapper.getExchangeMetaIdByExchangeId(info.getId());
                    
                    if (exchangeMetaId == null) {
                        // 기존 정보가 없으면 INSERT 후 cmc_exchange 테이블에 FK 연결
                        cmcBatchMapper.insertCmcExchangeMeta(info);
                        exchangeMetaId = getLastInsertedId("cmc_exchange_meta");
                    } else {
                        // 기존 정보가 있으면 UPDATE
                        cmcBatchMapper.updateCmcExchangeMeta(exchangeMetaId, info);
                    }
                    
                    // ExchangeUrl 처리 (웹사이트 정보가 있을 경우, cmc_exchange_id 기반으로 기존 정보 확인 후 INSERT/UPDATE)
                    Long exchangeUrlId = cmcBatchMapper.getExchangeUrlIdByExchangeId(info.getId());
                    
                    if (info.getUrls() != null && info.getUrls().getWebsite() != null && !info.getUrls().getWebsite().isEmpty()) {
                        String website = info.getUrls().getWebsite().get(0);
                        if (website != null && !website.trim().isEmpty()) {
                            if (exchangeUrlId == null) {
                                // 기존 정보가 없으면 INSERT 후 cmc_exchange 테이블에 FK 연결
                                cmcBatchMapper.insertCmcExchangeUrl(info.getId(), website);
                                exchangeUrlId = getLastInsertedId("cmc_exchange_url");
                            } else {
                                // 기존 정보가 있으면 UPDATE
                                cmcBatchMapper.updateCmcExchangeUrl(exchangeUrlId, website);
                            }
                        }
                    }
                    
                    // CmcExchange 테이블에 FK들 업데이트 (새로 생성된 경우에만)
                    cmcBatchMapper.updateCmcExchangeWithDetails(exchangeInfoId, exchangeMetaId, exchangeUrlId, info);
                    processedCount++;
                    
                    log.debug("거래소 상세 정보 처리 완료 - exchange_id: {}, info_id: {}, meta_id: {}, url_id: {}", 
                        info.getId(), exchangeInfoId, exchangeMetaId, exchangeUrlId);
                        
                } catch (Exception e) {
                    log.error("거래소 상세 정보 처리 중 오류 발생 - exchange_id: {}", info.getId(), e);
                }
            }
        }

        log.info("거래소 상세 정보 {} 건 업서트 완료", processedCount);
    }

    @Override
    public List<Integer> getCmcExchangeIds(int limit) {
        return cmcBatchMapper.getCmcExchangeIds(limit);
    }

    @Override
    public boolean existsCmcCoin(Long cmcCoinId) {
        return cmcBatchMapper.existsCmcCoin(cmcCoinId);
    }

    @Override
    public boolean existsCmcExchange(Integer cmcExchangeId) {
        return cmcBatchMapper.existsCmcExchange(cmcExchangeId);
    }

    @Override
    @Transactional
    public void linkCmcCoinWithExistingCoin() {
        try {
            List<Map<String, Object>> mappableCoinIds = cmcBatchMapper.getMappableCoinIds();
            
            if (mappableCoinIds.isEmpty()) {
                log.debug("매핑할 CMC Coin과 기존 Coin이 없습니다.");
                return;
            }
            
            int linkedCount = 0;
            
            for (Map<String, Object> coinInfo : mappableCoinIds) {
                Long coinId = ((Number) coinInfo.get("coin_id")).longValue();
                String symbol = (String) coinInfo.get("symbol");
                
                int updated = cmcBatchMapper.linkCmcCoinWithExistingCoin(coinId, symbol);
                if (updated > 0) {
                    linkedCount++;
                    log.debug("CMC Coin 매핑: symbol={}, coin_id={}", symbol, coinId);
                }
            }
            
            if (linkedCount > 0) {
                log.info("CMC Coin과 기존 Coin 매핑 완료: {} 건", linkedCount);
            } else {
                log.debug("매핑할 CMC Coin과 기존 Coin이 없습니다.");
            }
            
        } catch (Exception e) {
            log.error("CMC Coin 매핑 중 오류 발생", e);
            throw e;
        }
    }

    @Override
    public List<Long> getAllCmcCoinIds() {
        return cmcBatchMapper.getAllCmcCoinIds();
    }

    @Override
    @Transactional
    public void upsertCmcCoinInfoBulk(List<CmcCoinInfoDataDto> coinInfoList) {
        // 기존 upsertCmcCoinInfo 메서드와 동일한 로직 사용
        upsertCmcCoinInfo(coinInfoList);
    }

    @Override
    @Transactional
    public void upsertCmcCoinMeta(List<CmcApiDataDto> latestInfoList) {
        // 유효한 데이터만 필터링 (USD quote 데이터가 있는 것만)
        List<CmcApiDataDto> validItems = latestInfoList.stream()
            .filter(coin -> coin.getId() != null && coin.getQuote() != null && coin.getQuote().getUSD() != null)
            .toList();

        if (validItems.isEmpty()) {
            log.warn("업데이트할 유효한 코인 메타 데이터가 없습니다.");
            return;
        }

        int processedCount = 0;
        int errorCount = 0;
        int skippedCount = 0;
        
        for (CmcApiDataDto coin : validItems) {
            try {
                // CMC 코인이 존재하는지 확인
                if (!cmcBatchMapper.existsCmcCoin(coin.getId())) {
                    log.debug("CMC 코인이 존재하지 않아 메타 데이터 처리 건너뜀 - coin_id: {}", coin.getId());
                    skippedCount++;
                    continue;
                }
                
                // 코인 메타 데이터 UPSERT
                cmcBatchMapper.insertCmcCoinMeta(coin);
                
                // 코인 정보에 메타 ID 연결 (SQL에서 안전하게 처리)
                cmcBatchMapper.updateCmcCoinInfoWithMeta(coin.getId());
                
                processedCount++;
                log.debug("코인 메타 데이터 처리 완료 - coin_id: {}", coin.getId());
                    
            } catch (Exception e) {
                log.error("코인 메타 데이터 처리 중 오류 발생 - coin_id: {}", coin.getId(), e);
                errorCount++;
            }
        }

        log.info("코인 메타 데이터 처리 완료 - 성공: {} 건, 오류: {} 건, 건너뜀: {} 건", processedCount, errorCount, skippedCount);
    }

    @Override
    @Transactional
    public void upsertCmcMainnet(List<CmcCoinInfoDataDto> coinInfoList) {
        int processedCount = 0;
        
        for (CmcCoinInfoDataDto info : coinInfoList) {
            if (info.getId() != null && info.getUrls() != null && info.getUrls().getExplorer() != null && !info.getUrls().getExplorer().isEmpty()) {
                try {
                    // 1. 해당 cmc_coin_id의 기존 mainnet 데이터 모두 삭제
                    cmcBatchMapper.deleteCmcMainnet(info.getId());
                    
                    // 2. 모든 explorer URL을 개별적으로 삽입
                    for (String explorerUrl : info.getUrls().getExplorer()) {
                        if (explorerUrl != null && !explorerUrl.trim().isEmpty()) {
                            cmcBatchMapper.insertCmcMainnet(explorerUrl.trim(), info.getId());
                            processedCount++;
                        }
                    }
                } catch (Exception e) {
                    log.error("CmcMainnet 처리 중 오류 발생 - coin_id: {}", info.getId(), e);
                }
            }
        }

        log.info("CmcMainnet 데이터 {} 건 업서트 완료", processedCount);
    }

    @Override
    @Transactional
    public void upsertCmcPlatform(List<CmcCoinInfoDataDto> coinInfoList) {
        int insertedCount = 0;
        
        for (CmcCoinInfoDataDto info : coinInfoList) {
            if (info.getId() != null && info.getPlatform() != null) {
                try {
                    CmcDataPlatformDto platform = info.getPlatform();
                    if (platform.getName() != null && !platform.getName().trim().isEmpty()) {
                        
                        // 1. 해당 cmc_coin_id의 기존 platform 데이터 모두 삭제
                        cmcBatchMapper.deleteCmcPlatform(info.getId());
                        
                        // 2. 새로운 platform 데이터 삽입
                        cmcBatchMapper.insertCmcPlatform(info);
                        insertedCount++;
                    }
                } catch (Exception e) {
                    log.error("CmcPlatform 처리 중 오류 발생 - coin_id: {}", info.getId(), e);
                }
            }
        }

        log.info("CmcPlatform 데이터 {} 건 업서트 완료", insertedCount);
    }

    @Override
    public boolean shouldRunCoinMapSync() {
        boolean shouldRun = cmcBatchMapper.shouldRunCoinMapSync();
        log.info("코인 맵 동기화 필요 여부: {}", shouldRun);
        return shouldRun;
    }

    @Override
    public boolean shouldRunCoinInfoSync() {
        boolean shouldRun = cmcBatchMapper.shouldRunCoinInfoSync();
        log.info("코인 상세 정보 동기화 필요 여부: {}", shouldRun);
        return shouldRun;
    }

    @Override
    public boolean shouldRunExchangeSync() {
        boolean shouldRun = cmcBatchMapper.shouldRunExchangeSync();
        log.info("거래소 동기화 필요 여부: {}", shouldRun);
        return shouldRun;
    }

    @Override
    public boolean shouldRunCoinRankSync() {
        // cmc_rank 테이블이 비어있거나, cmc_coin에 rank가 연결되지 않은 코인이 있으면 동기화 필요
        boolean shouldRun = cmcBatchMapper.shouldRunCoinRankSync();
        log.info("코인 랭킹 동기화 필요 여부: {}", shouldRun);
        return shouldRun;
    }

    @Override
    public boolean shouldRunCoinMetaSync() {
        // cmc_coin_meta 테이블이 비어있거나, cmc_coin_info에 meta가 연결되지 않은 정보가 있으면 동기화 필요
        boolean shouldRun = cmcBatchMapper.shouldRunCoinMetaSync();
        log.info("코인 메타 동기화 필요 여부: {}", shouldRun);
        return shouldRun;
    }

    @Override
    public long getCmcCoinCount() {
        long count = cmcBatchMapper.getCmcCoinCount();
        log.debug("CMC 코인 총 개수: {}", count);
        return count;
    }

    @Override
    public long getCmcExchangeCount() {
        long count = cmcBatchMapper.getCmcExchangeCount();
        log.debug("CMC 거래소 총 개수: {}", count);
        return count;
    }

    private Long getLastInsertedId(String tableName) {
        try {
            return cmcBatchMapper.getLastInsertedId(tableName);
        } catch (Exception e) {
            log.error("마지막 삽입된 ID 조회 중 오류 - table: {}", tableName, e);
            return null;
        }
    }
}