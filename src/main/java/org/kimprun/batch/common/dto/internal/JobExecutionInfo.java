package org.kimprun.batch.common.dto.internal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class JobExecutionInfo {
    private Long jobExecutionId;
    private Long jobInstanceId;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String exitCode;
    private Map<String, Object> parameters;
    private List<String> runningSteps;

    public JobExecutionInfo(Long jobExecutionId, Long jobInstanceId, String status, LocalDateTime startTime, LocalDateTime endTime, String exitCode, Map<String, Object> parameters, List<String> runningSteps) {
        this.jobExecutionId = jobExecutionId;
        this.jobInstanceId = jobInstanceId;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
        this.exitCode = exitCode;
        this.parameters = parameters;
        this.runningSteps = runningSteps;
    }
}