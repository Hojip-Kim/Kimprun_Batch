package org.kimprun.batch.cmc.reader;

import org.kimprun.batch.cmc.component.CoinMarketCapComponent;
import org.kimprun.batch.cmc.dao.CmcBatchDao;
import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeDetailDto;
import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeDetailMapDto;
import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CmcExchangeBatchReader {

    private final CoinMarketCapComponent coinMarketCapComponent;
    private final CmcBatchDao cmcBatchDao;

    private static final int BATCH_SIZE = 5000;
    private static final int MAX_EXCHANGE_INFO_BATCH_SIZE = 100;

    public CmcExchangeBatchReader(CoinMarketCapComponent coinMarketCapComponent, CmcBatchDao cmcBatchDao) {
        this.coinMarketCapComponent = coinMarketCapComponent;
        this.cmcBatchDao = cmcBatchDao;
    }

    @StepScope
    public ItemReader<CmcExchangeDto> getExchangeMapReader() {
        log.info("거래소 맵 데이터 Reader 시작");
        
        // 거래소 동기화 필요 여부 체크
        if (!cmcBatchDao.shouldRunExchangeSync()) {
            log.info("거래소 맵 데이터가 최신 상태입니다. API 호출을 건너뜁니다.");
            return new ListItemReader<>(new ArrayList<>());
        }
        
        List<CmcExchangeDto> allExchangeMapData = new ArrayList<>();
        
        // 최대 5,000개 거래소 데이터를 가져옴
        List<CmcExchangeDto> exchangeData = coinMarketCapComponent.getExchangeMap(1, BATCH_SIZE);
        allExchangeMapData.addAll(exchangeData);
        log.info("거래소 맵 데이터 수집 완료: {} 건", exchangeData.size());
        
        log.info("총 거래소 맵 데이터 수집 완료: {} 건", allExchangeMapData.size());
        return new ListItemReader<>(allExchangeMapData);
    }

    @StepScope
    public ItemReader<List<CmcExchangeDetailDto>> getExchangeInfoReader() {
        log.info("거래소 상세 정보 Reader 시작");
        
        // 데이터베이스에서 기존 거래소 ID들을 가져옴
        List<Integer> exchangeIds = cmcBatchDao.getCmcExchangeIds(1000); // 최대 1000개
        
        if (exchangeIds.isEmpty()) {
            log.info("조회할 CMC 거래소 ID가 없습니다. 상세 정보 수집을 건너뜁니다.");
            return new ListItemReader<>(new ArrayList<>());
        }
        
        List<List<CmcExchangeDetailDto>> allExchangeInfoBatches = new ArrayList<>();
        
        // 100개씩 나누어서 처리 (CMC API 제한)
        for (int i = 0; i < exchangeIds.size(); i += MAX_EXCHANGE_INFO_BATCH_SIZE) {
            int endIndex = Math.min(i + MAX_EXCHANGE_INFO_BATCH_SIZE, exchangeIds.size());
            List<Integer> batchIds = exchangeIds.subList(i, endIndex);
            
            CmcExchangeDetailMapDto exchangeInfoMapDto = coinMarketCapComponent.getExchangeInfo(batchIds);
            
            List<CmcExchangeDetailDto> batchExchangeInfoList = new ArrayList<>();
            if (exchangeInfoMapDto != null && !exchangeInfoMapDto.isEmpty()) {
                // HashMap을 직접 순회
                for (Map.Entry<String, CmcExchangeDetailDto> entry : exchangeInfoMapDto.entrySet()) {
                    batchExchangeInfoList.add(entry.getValue());
                }
            }
            
            if (!batchExchangeInfoList.isEmpty()) {
                allExchangeInfoBatches.add(batchExchangeInfoList);
                log.info("거래소 상세 정보 {} 번째 배치 수집 완료: {} 건", (i / MAX_EXCHANGE_INFO_BATCH_SIZE) + 1, batchExchangeInfoList.size());
            }
        }
        
        log.info("총 거래소 상세 정보 배치 수집 완료: {} 배치", allExchangeInfoBatches.size());
        return new ListItemReader<>(allExchangeInfoBatches);
    }
    
    // CoinMarketCapComponent에 직접 액세스할 수 있도록 getter 메서드 제공
    public CoinMarketCapComponent getCmcExchangeInfoComponent() {
        return coinMarketCapComponent;
    }
} 