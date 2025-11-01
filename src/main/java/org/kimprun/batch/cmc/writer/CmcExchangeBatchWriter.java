package org.kimprun.batch.cmc.writer;

import org.kimprun.batch.cmc.dao.CmcBatchDao;
import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeDetailDto;
import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
public class CmcExchangeBatchWriter {

    private final CmcBatchDao cmcBatchDao;

    public CmcExchangeBatchWriter(CmcBatchDao cmcBatchDao) {
        this.cmcBatchDao = cmcBatchDao;
    }

    /**
     * 거래소 맵 데이터 Writer
     */
    @Transactional
    public ItemWriter<CmcExchangeDto> getExchangeMapWriter() {
        return new ItemWriter<CmcExchangeDto>() {
            @Override
            public void write(Chunk<? extends CmcExchangeDto> chunk) throws Exception {
                List<? extends CmcExchangeDto> items = chunk.getItems();
                if (!items.isEmpty()) {
                    log.info("거래소 맵 데이터 Writer 시작: {} 건", items.size());
                    // 안전한 타입 변환
                    @SuppressWarnings("unchecked")
                    List<CmcExchangeDto> exchangeMapItems = (List<CmcExchangeDto>) items;
                    cmcBatchDao.upsertCmcExchangeMap(exchangeMapItems);
                    log.info("거래소 맵 데이터 Writer 완료: {} 건", items.size());
                }
            }
        };
    }

    /**
     * 거래소 상세 정보 Writer
     */
    @Transactional
    public ItemWriter<List<CmcExchangeDetailDto>> getExchangeInfoWriter() {
        return new ItemWriter<List<CmcExchangeDetailDto>>() {
            @Override
            public void write(Chunk<? extends List<CmcExchangeDetailDto>> chunk) throws Exception {
                for (List<CmcExchangeDetailDto> batchItems : chunk.getItems()) {
                    if (batchItems != null && !batchItems.isEmpty()) {
                        log.info("거래소 상세 정보 배치 Writer 시작: {} 건", batchItems.size());
                        cmcBatchDao.upsertCmcExchangeInfo(batchItems);
                        log.info("거래소 상세 정보 배치 Writer 완료: {} 건", batchItems.size());
                    }
                }
            }
        };
    }
} 