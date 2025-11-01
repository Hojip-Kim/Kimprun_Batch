package org.kimprun.batch.cmc.component;

import org.kimprun.batch.cmc.dto.internal.coin.*;
import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeDto;
import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeApiStatusDto;
import org.kimprun.batch.cmc.dto.internal.exchange.CmcExchangeDetailMapDto;
import org.kimprun.batch.common.ratelimit.DistributedRateLimiter;
import org.kimprun.batch.common.ratelimit.RateLimitResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@Slf4j
@Qualifier("cmc")
public class CoinMarketCapComponent {

    private final RestClient coinMarketCapClient;
    private final DistributedRateLimiter distributedRateLimiter;
    @Value("${cmc.api.key}")
    private String cmcApiKey;

    @Value("${cmc.api.coinmap_url}")
    private String cmcCoinMapUrl;
    @Value("${cmc.api.latest_url}")
    private String cmcLatestUrl;
    @Value("${cmc.api.coin_info_url}")
    private String cmcCoinInfoUrl;
    @Value("${cmc.api.exchange_map_url}")
    private String cmcExchangeMapUrl;
    @Value("${cmc.api.exchange_info_url}")
    private String cmcExchangeInfoUrl;

    public CoinMarketCapComponent(RestClient coinMarketCapClient, DistributedRateLimiter rateLimiter) {
        this.coinMarketCapClient = coinMarketCapClient;
        this.distributedRateLimiter = rateLimiter;
    }

    private HttpHeaders getCMCHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-CMC_PRO_API_KEY", cmcApiKey);
        return headers;
    }

    // 대부분 start가 파라미터로 들어가있는것은 2까지만 순회 돌면 됨.

    // 5000개 호출시 per credit : 1
    // 1, 5000
    public List<CmcCoinMapDataDto> getCoinMapFromCMC(int start, int limit) {
        log.info("CoinMarketCap Coin Map 데이터 가져오기 시작");

        // Rate Limit 대기 후 재시도 로직
        waitForRateLimitAvailability("getCoinMapFromCMC");

        String url = String.format(cmcCoinMapUrl, start, limit);

        try {
            CmcApiResponseDto<CmcExchangeApiStatusDto, List<CmcCoinMapDataDto>> cmcResponse = coinMarketCapClient.get()
                    .uri(url)
                    .headers(headers -> headers.addAll(getCMCHeaders()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<CmcApiResponseDto<CmcExchangeApiStatusDto, List<CmcCoinMapDataDto>>>() {});

            return cmcResponse.getData();
        } catch (Exception e) {
            log.error("CMC Coin Map 조회 실패: {} - start: {}, limit: {}", e.getMessage(), start, limit);
            throw e;
        }
    }

    // 5000개 호출시 per credit : 25
    // 1, 5000
    // 코인 인포중, 숫자와 관련된 데이터들을 받아옴
    public List<CmcApiDataDto> getLatestCoinInfoFromCMC(int start, int limit){
        log.info("CoinMarketCap 최신 데이터 가져오기 시작");

        // Rate Limit 대기 후 재시도 로직
        waitForRateLimitAvailability("getLatestCoinInfoFromCMC");

        String url = String.format(cmcLatestUrl, start, limit);

        try {
            CmcApiResponseDto<CmcCoinApiStatusDto, List<CmcApiDataDto>> cmcResponse = coinMarketCapClient.get()
                    .uri(url)
                    .headers(headers -> headers.addAll(getCMCHeaders()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<CmcApiResponseDto<CmcCoinApiStatusDto, List<CmcApiDataDto>>>() {});

            return cmcResponse.getData();
        } catch (Exception e) {
            log.error("CMC Latest 조회 실패: {} - start: {}, limit: {}", e.getMessage(), start, limit);
            throw e;
        }
    }

    // 최대 100개까지만 가능 - per credit : 1
    // 코인의 id를 List의 형태로 넣어주고, coinMarketCap의 api를통해 정보를 가져옵니다.
    public CmcCoinInfoDataMapDto getCmcCoinInfos(List<Integer> cmcCoinIds){
        // Rate Limit 대기 후 재시도 로직
        waitForRateLimitAvailability("getCmcCoinInfos");

        String sequenceMainnetCmcIds = cmcCoinIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
        String url = String.format(cmcCoinInfoUrl, sequenceMainnetCmcIds);

        try {
            CmcApiResponseDto<CmcExchangeApiStatusDto, CmcCoinInfoDataMapDto> cmcResponse = coinMarketCapClient.get()
                    .uri(url)
                    .headers(headers -> headers.addAll(getCMCHeaders()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<CmcApiResponseDto<CmcExchangeApiStatusDto, CmcCoinInfoDataMapDto>>() {});

            return cmcResponse.getData();
        } catch (Exception e) {
            log.error("CMC 코인 정보 조회 실패: {} - IDs: {}", e.getMessage(), sequenceMainnetCmcIds);
            // 빈 응답 반환하여 애플리케이션 시작 중단 방지
            return new CmcCoinInfoDataMapDto();
        }
    }

    // 5000개 호출시 per credit : 1
    // 최대 limit 5000까지만 가능
    public List<CmcExchangeDto> getExchangeMap(int start, int limit){
        log.info("CoinMarketCap Exchange Map 데이터 가져오기 시작");

        // Rate Limit 대기 후 재시도 로직
        waitForRateLimitAvailability("getExchangeMap");

        String url = String.format(cmcExchangeMapUrl, start, limit);

        try {
            CmcApiResponseDto<CmcExchangeApiStatusDto, List<CmcExchangeDto>> cmcResponse = coinMarketCapClient.get()
                    .uri(url)
                    .headers(headers -> headers.addAll(getCMCHeaders()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<CmcApiResponseDto<CmcExchangeApiStatusDto, List<CmcExchangeDto>>>() {});

            return cmcResponse.getData();
        } catch (Exception e) {
            log.error("CMC Exchange Map 조회 실패: {} - start: {}, limit: {}", e.getMessage(), start, limit);
            throw e;
        }
    }

    // 최대 exchangeId 100개까지 가능 - per credit : 1
    public CmcExchangeDetailMapDto getExchangeInfo(List<Integer> exchangeIds){
        // Rate Limit 대기 후 재시도 로직
        waitForRateLimitAvailability("getExchangeInfo");

        String sequenceExchangeIds = exchangeIds.stream().map(String::valueOf).reduce((a, b) -> a + "," + b).orElse("");
        String url = String.format(cmcExchangeInfoUrl, sequenceExchangeIds);

        try {
            CmcApiResponseDto<CmcExchangeApiStatusDto, CmcExchangeDetailMapDto> cmcResponse = coinMarketCapClient.get()
                    .uri(url)
                    .headers(headers -> headers.addAll(getCMCHeaders()))
                    .retrieve()
                    .body(new ParameterizedTypeReference<CmcApiResponseDto<CmcExchangeApiStatusDto, CmcExchangeDetailMapDto>>() {});

            return cmcResponse.getData();
        } catch (Exception e) {
            log.error("CMC Exchange Info 조회 실패: {} - IDs: {}", e.getMessage(), sequenceExchangeIds);
            throw e;
        }
    }

    /**
     * Rate Limit 가용성 대기 메서드
     * Redisson blocking tryAcquire 사용 - permit이 사용 가능해질 때까지 자동 대기
     *
     * @param methodName 호출하는 메서드명
     */
    private void waitForRateLimitAvailability(String methodName) {
        // Redisson의 blocking tryAcquire 사용 - 최대 60초 대기
        RateLimitResult rateLimitResult = distributedRateLimiter.tryAcquireCmcApiLimitBlocking(60);

        if (!rateLimitResult.isAllowed()) {
            log.error("CMC API Rate Limit 타임아웃 - 메서드: {}, 60초 대기 후에도 permit 획득 실패", methodName);
            throw new RuntimeException("CMC API Rate Limit timeout after 65 seconds");
        }

        log.debug("CMC API Rate Limit 통과 - 메서드: {}, 남은 permits: {}",
            methodName, rateLimitResult.getRemainingRequests());
    }
}
