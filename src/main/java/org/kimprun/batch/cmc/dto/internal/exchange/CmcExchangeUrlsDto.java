package org.kimprun.batch.cmc.dto.internal.exchange;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class CmcExchangeUrlsDto {
    private List<String> website;
    private List<String> chat;
    private List<String> twitter;
    private List<String> register;
    private List<String> fee;
    private List<String> blog;
    private List<String> actual;
}
