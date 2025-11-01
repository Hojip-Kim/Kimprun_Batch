package org.kimprun.batch.cmc.dao.mybatis;

import org.kimprun.batch.cmc.dto.internal.coin.CmcApiDataDto;
import org.kimprun.batch.cmc.dto.internal.coin.CmcCoinInfoDataDto;
import org.kimprun.batch.cmc.dto.internal.coin.CmcCoinMapDataDto;
import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeDto;
import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeDetailDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface CmcBatchMapper {
    
    // 코인 관련 배치 메서드들
    void upsertCmcCoinMap(@Param("coinMapList") List<CmcCoinMapDataDto> coinMapList);
    void upsertCmcCoinMapRank(@Param("rankedCoins") List<CmcCoinMapDataDto> rankedCoins); // 코인 맵에서 랭킹 데이터 처리
    void updateCmcCoinMapRankFk(@Param("rankedCoins") List<CmcCoinMapDataDto> rankedCoins); // 코인 맵에서 랭킹 FK 업데이트
    void upsertCmcCoinRank(@Param("latestInfoList") List<CmcApiDataDto> latestInfoList);
    void updateCmcCoinRankFk(@Param("latestInfoList") List<CmcApiDataDto> latestInfoList);
    void insertCmcCoinInfo(@Param("coinInfo") CmcCoinInfoDataDto coinInfo);
    void updateCmcCoinWithCoinInfo(@Param("coinInfoId") Long coinInfoId, @Param("coinInfo") CmcCoinInfoDataDto coinInfo);
    List<Long> getCmcCoinIds(@Param("limit") int limit);
    
    // 거래소 관련 배치 메서드들
    void upsertCmcExchangeMap(@Param("exchangeMapList") List<CmcExchangeDto> exchangeMapList);
    Long getExchangeInfoIdByExchangeId(@Param("exchangeId") Long exchangeId);
    void insertCmcExchangeInfo(@Param("exchangeId") Long exchangeId, @Param("fiats") String fiats);
    void updateCmcExchangeInfo(@Param("exchangeInfoId") Long exchangeInfoId, @Param("fiats") String fiats);
    Long getExchangeMetaIdByExchangeId(@Param("exchangeId") Long exchangeId);
    void insertCmcExchangeMeta(@Param("exchangeDetail") CmcExchangeDetailDto exchangeDetail);
    void updateCmcExchangeMeta(@Param("exchangeMetaId") Long exchangeMetaId, @Param("exchangeDetail") CmcExchangeDetailDto exchangeDetail);
    Long getExchangeUrlIdByExchangeId(@Param("exchangeId") Long exchangeId);
    void insertCmcExchangeUrl(@Param("exchangeId") Long exchangeId, @Param("website") String website);
    void updateCmcExchangeUrl(@Param("exchangeUrlId") Long exchangeUrlId, @Param("website") String website);
    void updateCmcExchangeWithDetails(@Param("exchangeInfoId") Long exchangeInfoId, 
                                     @Param("exchangeMetaId") Long exchangeMetaId, 
                                     @Param("exchangeUrlId") Long exchangeUrlId,
                                     @Param("exchangeDetail") CmcExchangeDetailDto exchangeDetail);
    List<Integer> getCmcExchangeIds(@Param("limit") int limit);
    
    // 유틸리티 메서드들
    boolean existsCmcCoin(@Param("cmcCoinId") Long cmcCoinId);
    boolean existsCmcExchange(@Param("cmcExchangeId") Integer cmcExchangeId);
    
    // CMC Coin과 기존 Coin 테이블 매핑
    List<Map<String, Object>> getMappableCoinIds();
    int linkCmcCoinWithExistingCoin(@Param("coinId") Long coinId, @Param("symbol") String symbol);
    
    // 모든 CMC Coin ID 조회
    List<Long> getAllCmcCoinIds();
    
    // CmcCoinMeta 관련 - 메타 데이터 삽입과 동시에 코인 정보에 연결
    void insertCmcCoinMeta(@Param("coin") CmcApiDataDto coin);
    void updateCmcCoinInfoWithMeta(@Param("cmcCoinId") Long cmcCoinId);
    
    // CmcMainnet 관련
    void deleteCmcMainnet(@Param("coinId") Long coinId);
    void insertCmcMainnet(@Param("explorerUrl") String explorerUrl, @Param("coinId") Long coinId);
    
    // CmcPlatform 관련
    void deleteCmcPlatform(@Param("coinId") Long coinId);
    void insertCmcPlatform(@Param("coinInfo") CmcCoinInfoDataDto coinInfo);
    
    // 배치 실행 조건 검사
    boolean shouldRunCoinMapSync();
    boolean shouldRunCoinInfoSync(); 
    boolean shouldRunExchangeSync();
    boolean shouldRunCoinRankSync();
    boolean shouldRunCoinMetaSync();
    long getCmcCoinCount();
    long getCmcExchangeCount();
    
    // 유틸리티 메서드
    Long getLastInsertedId(@Param("tableName") String tableName);
}