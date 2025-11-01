package org.kimprun.batch.cmc.dto.internal.coin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CmcCoinMapDataDto {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("rank")
    private Long rank;
    @JsonProperty("name")
    private String name;
    @JsonProperty("symbol")
    private String symbol;
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("is_active")
    private Boolean isActive;
    @JsonProperty("status")
    private Boolean status;
    @JsonProperty("first_historical_data")
    private String first_historical_data;
    @JsonProperty("last_historical_data")
    private String last_historical_data;
    @JsonProperty("platform")
    private CmcDataPlatformDto platform;

    public CmcCoinMapDataDto(Long id, Long rank, String name, String symbol, String slug, Boolean isActive, Boolean status, String first_historical_data, String last_historical_data, CmcDataPlatformDto platform) {
        this.id = id;
        this.rank = rank;
        this.name = name;
        this.symbol = symbol;
        this.slug = slug;
        this.isActive = isActive;
        this.status = status;
        this.first_historical_data = first_historical_data;
        this.last_historical_data = last_historical_data;
        this.platform = platform;
    }
    
    // MyBatis를 위한 computed property
    public Boolean getIsMainnet() {
        return this.platform == null;
    }
    
    // 타임스탬프를 위한 헬퍼 메서드들
    public String getFirstHistoricalData() {
        return this.first_historical_data;
    }
    
    public String getLastHistoricalData() {
        return this.last_historical_data;
    }
}
