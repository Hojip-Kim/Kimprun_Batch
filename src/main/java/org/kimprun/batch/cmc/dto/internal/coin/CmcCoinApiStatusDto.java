package org.kimprun.batch.cmc.dto.internal.coin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CmcCoinApiStatusDto {
    @JsonProperty("timestamp")
    private String timestamp;
    @JsonProperty("error_code")
    private Long errorCode;
    @JsonProperty("error_message")
    private String errorMessage;
    @JsonProperty("elapsed")
    private Long elapsed;
    @JsonProperty("credit_count")
    private Long creditCount;
    @JsonProperty("total_count")
    private Long totalCount;

    public CmcCoinApiStatusDto(String timestamp, Long errorCode, String errorMessage, Long elapsed, Long creditCount, Long totalCount) {
        this.timestamp = timestamp;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.elapsed = elapsed;
        this.creditCount = creditCount;
        this.totalCount = totalCount;
    }

}
