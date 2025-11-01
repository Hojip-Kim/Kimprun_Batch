package org.kimprun.batch.common.dto.response;

import org.kimprun.batch.common.dto.internal.StepExecutionInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobExecutionStatusResponse {
    private Map<String, StepExecutionInfo> stepExecutions;
}