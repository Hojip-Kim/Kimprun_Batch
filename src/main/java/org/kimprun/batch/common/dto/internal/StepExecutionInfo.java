package org.kimprun.batch.common.dto.internal;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class StepExecutionInfo {
    private String status;
    private long readCount;
    private long writeCount;
    private long commitCount;
    private long rollbackCount;
    private long filterCount;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String exitCode;

    public StepExecutionInfo(String status, long readCount, long writeCount, long commitCount, long rollbackCount, long filterCount, LocalDateTime startTime, LocalDateTime endTime, String exitCode) {
        this.status = status;
        this.readCount = readCount;
        this.writeCount = writeCount;
        this.commitCount = commitCount;
        this.rollbackCount = rollbackCount;
        this.filterCount = filterCount;
        this.startTime = startTime;
        this.endTime = endTime;
        this.exitCode = exitCode;
    }
}