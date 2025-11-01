package org.kimprun.batch.cmc.dto.internal.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@NoArgsConstructor
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CmcExchangeDetailDto {
    private Long id;
    private String name;
    private String slug;
    private String description;
    private String notice;
    private String logo;
    private Object countries;
    private Object fiats;
    private CmcExchangeUrlsDto urls;
    private Object tags;
    private String type;
    private Long porStatus;
    private Long porAuditStatus;
    private Long walletSourceStatus;
    private String porSwitch;
    private Long alertType;
    private String alertLink;
    @JsonProperty("date_launched")
    private String dateLaunched;
    @JsonProperty("is_hidden")
    private Boolean isHidden;
    @JsonProperty("is_redistributable")
    private Boolean isRedistributable;
    @JsonProperty("market_fee")
    private BigDecimal marketFee;
    @JsonProperty("taker_fee")
    private BigDecimal takerFee;
    @JsonProperty("spot_volume_usd")
    private BigDecimal spotVolumeUsd;
    @JsonProperty("spot_volume_last_updated")
    private String spotVolumeLastUpdated;
    @JsonProperty("weekly_visited")
    private Long weeklyVisited;
}
