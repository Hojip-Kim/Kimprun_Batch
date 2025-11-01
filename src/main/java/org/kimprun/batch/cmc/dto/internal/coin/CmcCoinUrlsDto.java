package org.kimprun.batch.cmc.dto.internal.coin;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Getter
public class CmcCoinUrlsDto {
    private List<String> website;
    private List<String> twitter;
    private List<String> message_board;
    private List<String> chat;
    private List<String> facebook;
    private List<String> explorer;
    private List<String> reddit;
    private List<String> technical_doc;
    private List<String> source_code;
    private List<String> announcement;
}