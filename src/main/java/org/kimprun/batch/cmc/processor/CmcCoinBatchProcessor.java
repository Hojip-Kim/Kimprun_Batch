
package org.kimprun.batch.cmc.processor;

import org.kimprun.batch.cmc.dto.internal.coin.CmcApiDataDto;
import org.kimprun.batch.cmc.dto.internal.coin.CmcCoinInfoDataDto;
import org.kimprun.batch.cmc.dto.internal.coin.CmcCoinMapDataDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class CmcCoinBatchProcessor {

    /**
     * 코인 맵 데이터 처리
     * 필수 필드 검증 및 데이터 정제
     */
    public ItemProcessor<CmcCoinMapDataDto, CmcCoinMapDataDto> getCoinMapProcessor() {
        return new ItemProcessor<CmcCoinMapDataDto, CmcCoinMapDataDto>() {
            @Override
            public CmcCoinMapDataDto process(CmcCoinMapDataDto item) throws Exception {
                // 필수 필드 검증 - Long wrapper 타입이므로 null 및 유효값 체크
                if (item.getId() == null || item.getId() <= 0 || 
                    item.getName() == null || item.getSymbol() == null) {
                    log.warn("코인 맵 데이터 필수 필드 누락으로 스킵: ID={}, Name={}, Symbol={}", 
                            item.getId(), item.getName(), item.getSymbol());
                    return null;
                }
                
                // 이름이나 심볼이 빈 문자열인 경우도 체크
                if (item.getName().trim().isEmpty() || item.getSymbol().trim().isEmpty()) {
                    log.warn("코인 맵 데이터 필수 필드가 비어있음으로 스킵: ID={}, Name={}, Symbol={}", 
                            item.getId(), item.getName(), item.getSymbol());
                    return null;
                }
                
                return item;
            }
        };
    }

    /**
     * 코인 최신 정보 데이터 처리
     * 랭킹 정보 검증 및 데이터 정제
     */
    public ItemProcessor<CmcApiDataDto, CmcApiDataDto> getLatestCoinInfoProcessor() {
        return new ItemProcessor<CmcApiDataDto, CmcApiDataDto>() {
            @Override
            public CmcApiDataDto process(CmcApiDataDto item) throws Exception {
                // 필수 필드 검증
                if (item.getId() == null || item.getId() <= 0 || item.getCmcRank() == null) {
                    log.warn("코인 최신 정보 필수 필드 누락으로 스킵: ID={}, Rank={}", 
                            item.getId(), item.getCmcRank());
                    return null;
                }
                
                // 랭킹이 유효한 범위인지 검증
                if (item.getCmcRank() <= 0 || item.getCmcRank() > 1000000) {
                    log.warn("코인 ID {} 랭킹이 유효하지 않음: {}", item.getId(), item.getCmcRank());
                    return null;
                }
                
                return item;
            }
        };
    }

    /**
     * 코인 상세 정보 배치 처리
     * 리스트 내 각 아이템 검증
     */
    public ItemProcessor<List<CmcCoinInfoDataDto>, List<CmcCoinInfoDataDto>> getCoinInfoProcessor() {
        return new ItemProcessor<List<CmcCoinInfoDataDto>, List<CmcCoinInfoDataDto>>() {
            @Override
            public List<CmcCoinInfoDataDto> process(List<CmcCoinInfoDataDto> items) throws Exception {
                if (items == null || items.isEmpty()) {
                    log.debug("코인 상세 정보 배치가 비어있음");
                    return null;
                }
                
                // 유효한 아이템만 필터링
                List<CmcCoinInfoDataDto> validItems = items.stream()
                    .filter(item -> {
                        if (item.getId() == null || item.getId() <= 0) {
                            log.warn("코인 상세 정보 ID 누락 또는 유효하지 않음으로 스킵: ID={}", item.getId());
                            return false;
                        }
                        return true;
                    })
                    .toList();
                
                if (validItems.isEmpty()) {
                    log.debug("코인 상세 정보 배치에서 유효한 아이템 없음");
                    return null;
                }
                
                log.debug("코인 상세 정보 배치 처리 완료: {}/{} 건", validItems.size(), items.size());
                return validItems;
            }
        };
    }
} 