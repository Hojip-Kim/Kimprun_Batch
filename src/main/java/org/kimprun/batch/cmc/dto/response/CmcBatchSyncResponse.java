package org.kimprun.batch.cmc.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CmcBatchSyncResponse {
    private String message;
    private String mode;
    private LocalDateTime timestamp;
    private boolean asyncExecution;
}