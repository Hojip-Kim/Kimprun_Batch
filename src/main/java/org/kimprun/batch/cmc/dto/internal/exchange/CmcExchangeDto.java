package org.kimprun.batch.cmc.dto.internal.exchange;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CmcExchangeDto {
    private long id;
    private String name;
    private String slug;
    @JsonProperty("is_active")
    private boolean isActive;
    @JsonProperty("is_listed")
    private boolean isListed;
    @JsonProperty("is_redistributable")
    private boolean isRedistributable;
    @JsonProperty("first_historical_data")
    private String firstHistoricalData;
    @JsonProperty("last_historical_data")
    private String lastHistoricalData;

    public CmcExchangeDto(long id, String name, String slug, boolean isActive, boolean isListed, boolean isRedistributable, String firstHistoricalData, String lastHistoricalData) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.isActive = isActive;
        this.isListed = isListed;
        this.isRedistributable = isRedistributable;
        this.firstHistoricalData = firstHistoricalData;
        this.lastHistoricalData = lastHistoricalData;
    }
}
