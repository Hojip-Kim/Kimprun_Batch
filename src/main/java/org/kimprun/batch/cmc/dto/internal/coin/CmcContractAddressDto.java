package org.kimprun.batch.cmc.dto.internal.coin;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class CmcContractAddressDto {

    @JsonProperty("contract_address")
    private String contractAddress;

    private CmcDataPlatformDto platform;
}
