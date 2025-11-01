package org.kimprun.batch.cmc.dto.internal.coin;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@NoArgsConstructor
@Getter
public class CmcCoinInfoDataMapDto extends HashMap<String, CmcCoinInfoDataDto> {
    @JsonAnySetter
    public void setCoin(String key, CmcCoinInfoDataDto value) {
        this.put(key, value);
    }
}
