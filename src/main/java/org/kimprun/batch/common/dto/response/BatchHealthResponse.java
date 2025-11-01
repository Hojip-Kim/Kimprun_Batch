package org.kimprun.batch.common.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchHealthResponse {
    private String message;
    private boolean jobRepositoryConnected;
    private boolean targetJobExists;
    private List<String> availableJobs;
    private LocalDateTime timestamp;
}