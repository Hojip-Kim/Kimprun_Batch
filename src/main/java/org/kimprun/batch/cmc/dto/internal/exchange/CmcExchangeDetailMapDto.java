package org.kimprun.batch.cmc.dto.internal.exchange;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@NoArgsConstructor
@Getter
public class CmcExchangeDetailMapDto extends HashMap<String, CmcExchangeDetailDto> {
    @JsonAnySetter
    public void setCoin(String key, CmcExchangeDetailDto value) {
        this.put(key, value);
    }

}
