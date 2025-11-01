package org.kimprun.batch.cmc.reader;

import org.kimprun.batch.cmc.component.CoinMarketCapComponent;
import org.kimprun.batch.cmc.dao.CmcBatchDao;
import org.kimprun.batch.cmc.dto.internal.coin.CmcApiDataDto;
import org.kimprun.batch.cmc.dto.internal.coin.CmcCoinInfoDataDto;
import org.kimprun.batch.cmc.dto.internal.coin.CmcCoinInfoDataMapDto;
import org.kimprun.batch.cmc.dto.internal.coin.CmcCoinMapDataDto;
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
public class CmcCoinBatchReader {

    private final CoinMarketCapComponent coinMarketCapComponent;
    private final CmcBatchDao cmcBatchDao;

    private static final int BATCH_SIZE = 5000;
    private static final int MAX_CMC_INFO_BATCH_SIZE = 100;

    public CmcCoinBatchReader(CoinMarketCapComponent coinMarketCapComponent, CmcBatchDao cmcBatchDao) {
        this.coinMarketCapComponent = coinMarketCapComponent;
        this.cmcBatchDao = cmcBatchDao;
    }

    @StepScope
    public ItemReader<CmcCoinMapDataDto> getCoinMapReader() {
        return new ItemReader<CmcCoinMapDataDto>() {
            private ItemReader<CmcCoinMapDataDto> delegate;
            private boolean initialized = false;
            
            @Override
            public CmcCoinMapDataDto read() throws Exception {
                if (!initialized) {
                    delegate = createCoinMapReader();
                    initialized = true;
                }
                return delegate.read();
            }
        };
    }
    
    private ItemReader<CmcCoinMapDataDto> createCoinMapReader() {
        // 동기화 필요 여부 체크
        if (!cmcBatchDao.shouldRunCoinMapSync()) {
            log.info("코인 맵 데이터가 최신 상태입니다. API 호출을 건너뜁니다.");
            return new ListItemReader<>(new ArrayList<>());
        }
        
        List<CmcCoinMapDataDto> allCoinMapData = new ArrayList<>();
        
        // 최대 10,000개 코인 데이터를 가져옴 (2번 호출)
        for (int i = 0; i < 2; i++) {
            int start = (i * BATCH_SIZE) + 1;
            List<CmcCoinMapDataDto> batchData = coinMarketCapComponent.getCoinMapFromCMC(start, BATCH_SIZE);
            allCoinMapData.addAll(batchData);
            log.info("코인 맵 데이터 {} 번째 배치 수집 완료: {} 건", i + 1, batchData.size());
        }
        
        log.info("총 코인 맵 데이터 수집 완료: {} 건", allCoinMapData.size());
        return new ListItemReader<>(allCoinMapData);
    }

    @StepScope
    public ItemReader<CmcApiDataDto> getLatestCoinInfoReader() {
        return new ItemReader<CmcApiDataDto>() {
            private ItemReader<CmcApiDataDto> delegate;
            private boolean initialized = false;
            
            @Override
            public CmcApiDataDto read() throws Exception {
                if (!initialized) {
                    delegate = createLatestCoinInfoReader();
                    initialized = true;
                }
                return delegate.read();
            }
        };
    }
    
    private ItemReader<CmcApiDataDto> createLatestCoinInfoReader() {
        // 코인 맵이 없으면 최신 정보도 가져올 필요 없음
        if (cmcBatchDao.getCmcCoinCount() == 0) {
            log.info("CMC 코인 데이터가 없습니다. 최신 정보 수집을 건너뜁니다.");
            return new ListItemReader<>(new ArrayList<>());
        }
        
        List<CmcApiDataDto> allLatestData = new ArrayList<>();
        
        // 최대 10,000개 코인의 최신 데이터를 가져옴 (2번 호출)
        for (int i = 0; i < 2; i++) {
            int start = (i * BATCH_SIZE) + 1;
            List<CmcApiDataDto> batchData = coinMarketCapComponent.getLatestCoinInfoFromCMC(start, BATCH_SIZE);
            allLatestData.addAll(batchData);
            log.info("코인 최신 정보 {} 번째 배치 수집 완료: {} 건", i + 1, batchData.size());
        }
        
        log.info("총 코인 최신 정보 수집 완료: {} 건", allLatestData.size());
        return new ListItemReader<>(allLatestData);
    }

    @StepScope
    public ItemReader<List<CmcCoinInfoDataDto>> getCmcCoinInfoReader() {
        return new ItemReader<List<CmcCoinInfoDataDto>>() {
            private ItemReader<List<CmcCoinInfoDataDto>> delegate;
            private boolean initialized = false;
            
            @Override
            public List<CmcCoinInfoDataDto> read() throws Exception {
                if (!initialized) {
                    delegate = createCmcCoinInfoReader();
                    initialized = true;
                }
                return delegate.read();
            }
        };
    }
    
    private ItemReader<List<CmcCoinInfoDataDto>> createCmcCoinInfoReader() {
        // 상세 정보 동기화 필요 여부 체크
        if (!cmcBatchDao.shouldRunCoinInfoSync()) {
            log.info("코인 상세 정보 동기화가 필요하지 않습니다. API 호출을 건너뜁니다.");
            return new ListItemReader<>(new ArrayList<>());
        }
        
        // 데이터베이스에서 기존 코인 ID들을 가져옴
        List<Long> cmcCoinIds = cmcBatchDao.getCmcCoinIds(1000); // 최대 1000개
        
        if (cmcCoinIds.isEmpty()) {
            log.info("조회할 CMC 코인 ID가 없습니다. 상세 정보 수집을 건너뜁니다.");
            return new ListItemReader<>(new ArrayList<>());
        }
        
        List<List<CmcCoinInfoDataDto>> allCoinInfoBatches = new ArrayList<>();
        
        // 100개씩 나누어서 처리 (CMC API 제한)
        for (int i = 0; i < cmcCoinIds.size(); i += MAX_CMC_INFO_BATCH_SIZE) {
            int endIndex = Math.min(i + MAX_CMC_INFO_BATCH_SIZE, cmcCoinIds.size());
            List<Integer> batchIds = cmcCoinIds.subList(i, endIndex).stream()
                .map(Long::intValue)
                .toList();
            
            CmcCoinInfoDataMapDto coinInfoMapDto = coinMarketCapComponent.getCmcCoinInfos(batchIds);
            
            List<CmcCoinInfoDataDto> batchCoinInfoList = new ArrayList<>();
            if (coinInfoMapDto != null && !coinInfoMapDto.isEmpty()) {
                // HashMap을 직접 순회
                for (Map.Entry<String, CmcCoinInfoDataDto> entry : coinInfoMapDto.entrySet()) {
                    batchCoinInfoList.add(entry.getValue());
                }
            }
            
            if (!batchCoinInfoList.isEmpty()) {
                allCoinInfoBatches.add(batchCoinInfoList);
                log.info("코인 상세 정보 {} 번째 배치 수집 완료: {} 건", (i / MAX_CMC_INFO_BATCH_SIZE) + 1, batchCoinInfoList.size());
            }
        }
        
        log.info("총 코인 상세 정보 배치 수집 완료: {} 배치", allCoinInfoBatches.size());
        return new ListItemReader<>(allCoinInfoBatches);
    }
    
    // CoinMarketCapComponent에 직접 액세스할 수 있도록 getter 메서드 제공
    public CoinMarketCapComponent getCmcCoinInfoComponent() {
        return coinMarketCapComponent;
    }
}
