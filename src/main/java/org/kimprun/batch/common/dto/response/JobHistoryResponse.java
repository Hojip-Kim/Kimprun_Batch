package org.kimprun.batch.common.dto.response;

import org.kimprun.batch.common.dto.internal.JobExecutionInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobHistoryResponse {
    private int totalCount;
    private List<JobExecutionInfo> executions;
}