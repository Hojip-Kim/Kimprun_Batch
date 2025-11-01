package org.kimprun.batch.cmc.processor;

import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeDetailDto;
import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CmcExchangeBatchProcessor {

    /**
     * 거래소 맵 데이터 처리
     * 필수 필드 검증 및 데이터 정제
     */
    public ItemProcessor<CmcExchangeDto, CmcExchangeDto> getExchangeMapProcessor() {
        return new ItemProcessor<CmcExchangeDto, CmcExchangeDto>() {
            @Override
            public CmcExchangeDto process(CmcExchangeDto item) throws Exception {
                // 필수 필드 검증 - id는 long primitive이므로 > 0 체크
                if (item.getId() <= 0 || item.getName() == null || item.getSlug() == null) {
                    log.warn("거래소 맵 데이터 필수 필드 누락으로 스킵: ID={}, Name={}, Slug={}", 
                            item.getId(), item.getName(), item.getSlug());
                    return null;
                }
                
                // 이름이나 slug가 빈 문자열인 경우도 체크
                if (item.getName().trim().isEmpty() || item.getSlug().trim().isEmpty()) {
                    log.warn("거래소 맵 데이터 필수 필드가 비어있음으로 스킵: ID={}, Name={}, Slug={}", 
                            item.getId(), item.getName(), item.getSlug());
                    return null;
                }
                
                return item;
            }
        };
    }

    /**
     * 거래소 상세 정보 배치 처리
     * 리스트 내 각 아이템 검증
     */
    public ItemProcessor<List<CmcExchangeDetailDto>, List<CmcExchangeDetailDto>> getExchangeInfoProcessor() {
        return new ItemProcessor<List<CmcExchangeDetailDto>, List<CmcExchangeDetailDto>>() {
            @Override
            public List<CmcExchangeDetailDto> process(List<CmcExchangeDetailDto> items) throws Exception {
                if (items == null || items.isEmpty()) {
                    log.debug("거래소 상세 정보 배치가 비어있음");
                    return null;
                }
                
                // 유효한 아이템만 필터링 - id는 Long wrapper이므로 null 체크 가능
                List<CmcExchangeDetailDto> validItems = items.stream()
                    .filter(item -> {
                        if (item.getId() == null || item.getId() <= 0) {
                            log.warn("거래소 상세 정보 ID 누락 또는 유효하지 않음으로 스킵: ID={}", item.getId());
                            return false;
                        }
                        return true;
                    })
                    .toList();
                
                if (validItems.isEmpty()) {
                    log.debug("거래소 상세 정보 배치에서 유효한 아이템 없음");
                    return null;
                }
                
                log.debug("거래소 상세 정보 배치 처리 완료: {}/{} 건", validItems.size(), items.size());
                return validItems;
            }
        };
    }
} 