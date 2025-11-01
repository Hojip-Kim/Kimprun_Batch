package org.kimprun.batch.cmc.dto.internal.coin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@Getter
public class CmcUsdQuoteDto {

    @JsonProperty("price")
    private double price;

    @JsonProperty("volume_24h")
    private double volume24h;

    @JsonProperty("volume_change_24h")
    private double volumeChange24h;

    @JsonProperty("percent_change_1h")
    private double percentChange1h;

    @JsonProperty("percent_change_24h")
    private double percentChange24h;

    @JsonProperty("percent_change_7d")
    private double percentChange7d;

    @JsonProperty("percent_change_30d")
    private double percentChange30d;

    @JsonProperty("percent_change_60d")
    private double percentChange60d;

    @JsonProperty("percent_change_90d")
    private double percentChange90d;

    @JsonProperty("market_cap")
    private double marketCap;

    @JsonProperty("market_cap_dominance")
    private double marketCapDominance;

    @JsonProperty("fully_diluted_market_cap")
    private double fullyDilutedMarketCap;

    @JsonProperty("tvl")
    private Double tvl; // Using Double object to allow null values

    @JsonProperty("last_updated")
    private String lastUpdated; // ISO 8601 format: "2025-07-03T12:39:00.000Z"

    public CmcUsdQuoteDto(double price, double volume24h, double volumeChange24h, double percentChange1h, double percentChange24h, double percentChange7d, double percentChange30d, double percentChange60d, double percentChange90d, double marketCap, double marketCapDominance, double fullyDilutedMarketCap, Double tvl, String lastUpdated) {
        this.price = price;
        this.volume24h = volume24h;
        this.volumeChange24h = volumeChange24h;
        this.percentChange1h = percentChange1h;
        this.percentChange24h = percentChange24h;
        this.percentChange7d = percentChange7d;
        this.percentChange30d = percentChange30d;
        this.percentChange60d = percentChange60d;
        this.percentChange90d = percentChange90d;
        this.marketCap = marketCap;
        this.marketCapDominance = marketCapDominance;
        this.fullyDilutedMarketCap = fullyDilutedMarketCap;
        this.tvl = tvl;
        this.lastUpdated = lastUpdated;
    }
}
