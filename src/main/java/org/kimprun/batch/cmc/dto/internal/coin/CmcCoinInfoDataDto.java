package org.kimprun.batch.cmc.dto.internal.coin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class CmcCoinInfoDataDto {
    private Long id;
    private String name;
    private String symbol;
    private String category;
    private String description;
    private String slug;
    private String logo;
    private String subreddit;
    private String notice;

    private List<String> tags;

    @JsonProperty("tag-names")
    private List<String> tagNames;

    @JsonProperty("tag-groups")
    private List<String> tagGroups;

    private CmcCoinUrlsDto urls;
    private CmcDataPlatformDto platform;

    @JsonProperty("date_added")
    private String dateAdded;

    @JsonProperty("twitter_username")
    private String twitterUsername;

    @JsonProperty("is_hidden")
    private Integer isHidden;

    @JsonProperty("date_launched")
    private String dateLaunched;

    @JsonProperty("contract_address")
    private List<CmcContractAddressDto> contractAddresses;

    @JsonProperty("self_reported_circulating_supply")
    private String selfReportedCirculatingSupply;

    @JsonProperty("self_reported_tags")
    private List<String> selfReportedTags;

    @JsonProperty("self_reported_market_cap")
    private String selfReportedMarketCap;

    @JsonProperty("infinite_supply")
    private Boolean infiniteSupply;
}
