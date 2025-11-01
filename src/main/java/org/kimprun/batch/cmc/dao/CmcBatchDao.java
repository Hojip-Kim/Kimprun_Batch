package org.kimprun.batch.cmc.dao;

import org.kimprun.batch.cmc.dto.internal.coin.CmcApiDataDto;
import org.kimprun.batch.cmc.dto.internal.coin.CmcCoinInfoDataDto;
import org.kimprun.batch.cmc.dto.internal.coin.CmcCoinMapDataDto;
import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeDto;
import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeDetailDto;

import java.util.List;

public interface CmcBatchDao {
    
    // 코인 관련 배치 메서드들
    void upsertCmcCoinMap(List<CmcCoinMapDataDto> coinMapList);
    void upsertCmcCoinMapRank(List<CmcCoinMapDataDto> rankedCoins); // 코인 맵에서 랭킹 데이터 처리
    void updateCmcCoinLatestInfo(List<CmcApiDataDto> latestInfoList);
    void upsertCmcCoinInfo(List<CmcCoinInfoDataDto> coinInfoList);
    List<Long> getCmcCoinIds(int limit);
    
    // 거래소 관련 배치 메서드들
    void upsertCmcExchangeMap(List<CmcExchangeDto> exchangeMapList);
    void upsertCmcExchangeInfo(List<CmcExchangeDetailDto> exchangeInfoList);
    List<Integer> getCmcExchangeIds(int limit);
    
    // 유틸리티 메서드들
    boolean existsCmcCoin(Long cmcCoinId);
    boolean existsCmcExchange(Integer cmcExchangeId);
    
    // CMC Coin과 기존 Coin 테이블 매핑
    void linkCmcCoinWithExistingCoin();
    
    // 모든 CMC Coin ID 조회 (CmcCoinInfo 배치 처리용)
    List<Long> getAllCmcCoinIds();
    
    // CmcCoinInfo 일괄 처리 (getCmcCoinInfos API 활용)
    void upsertCmcCoinInfoBulk(List<CmcCoinInfoDataDto> coinInfoList);
    
    // CmcCoinMeta 데이터 처리 (getLatestCoinInfoFromCMC API 활용)
    void upsertCmcCoinMeta(List<CmcApiDataDto> latestInfoList);
    
    // CmcMainnet 데이터 처리 (getCmcCoinInfos에서 explorer URLs 활용)
    void upsertCmcMainnet(List<CmcCoinInfoDataDto> coinInfoList);
    
    // CmcPlatform 데이터 처리 (getCmcCoinInfos에서 platform 정보 활용)
    void upsertCmcPlatform(List<CmcCoinInfoDataDto> coinInfoList);
    
    // 배치 실행 조건 검사 메서드들
    boolean shouldRunCoinMapSync();
    boolean shouldRunCoinInfoSync();
    boolean shouldRunExchangeSync();
    boolean shouldRunCoinRankSync();  // cmc_rank 테이블이 비어있거나 오래된 경우
    boolean shouldRunCoinMetaSync();  // cmc_coin_meta 테이블이 비어있거나 오래된 경우
    long getCmcCoinCount();
    long getCmcExchangeCount();
}