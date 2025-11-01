package org.kimprun.batch.cmc.dto.internal.coin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CmcApiDataDto {
    @JsonProperty("id")
    private Long id;
    @JsonProperty("name")
    private String name;
    @JsonProperty("symbol")
    private String symbol;
    @JsonProperty("slug")
    private String slug;
    @JsonProperty("num_market_pairs")
    private Long numMarketPairs;
    @JsonProperty("date_added")
    private String dateAdded;
    @JsonProperty("tags")
    private String[] tags;
    @JsonProperty("max_supply")
    private String maxSupply;
    @JsonProperty("circulating_supply")
    private String circulatingSupply;
    @JsonProperty("total_supply")
    private String totalSupply;
    @JsonProperty("infinite_supply")
    private Boolean infiniteSupply;
    @JsonProperty("platform")
    private CmcDataPlatformDto platform;
    @JsonProperty("cmc_rank")
    private Long cmcRank;
    @JsonProperty("self_reported_circulating_supply")
    private String selfReportedCirculatingSupply;
    @JsonProperty("self_reported_market_cap")
    private String selfReportedMarketCap;
    @JsonProperty("tvl_ratio")
    private Double tvlRatio;
    @JsonProperty("last_updated")
    private String lastUpdated;
    @JsonProperty("quote")
    private CmcApiQuoteDto quote;

    public CmcApiDataDto(Long id, String name, String symbol, String slug, Long numMarketPairs, String dateAdded, String[] tags, String maxSupply, String circulatingSupply, CmcDataPlatformDto platform, String totalSupply, Boolean infiniteSupply, Long cmcRank, String selfReportedCirculatingSupply, String selfReportedMarketCap, Double tvlRatio, String lastUpdated, CmcApiQuoteDto quote) {
        this.id = id;
        this.name = name;
        this.symbol = symbol;
        this.slug = slug;
        this.numMarketPairs = numMarketPairs;
        this.dateAdded = dateAdded;
        this.tags = tags;
        this.maxSupply = maxSupply;
        this.circulatingSupply = circulatingSupply;
        this.totalSupply = totalSupply;
        this.infiniteSupply = infiniteSupply;
        this.cmcRank = cmcRank;
        this.platform = platform;
        this.selfReportedCirculatingSupply = selfReportedCirculatingSupply;
        this.selfReportedMarketCap = selfReportedMarketCap;
        this.tvlRatio = tvlRatio;
        this.lastUpdated = lastUpdated;
        this.quote = quote;
    }
}
