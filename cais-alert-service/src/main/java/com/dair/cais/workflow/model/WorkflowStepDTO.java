package com.dair.cais.workflow.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class WorkflowStepDTO {
    @NotNull
    private Long stepId;

    @NotNull
    private WorkflowStepPositionDTO position;

    @NotNull
    private WorkflowStepDataDTO data;

    @NotNull
    private WorkflowStepPropertiesDTO props;

    private List<Long> checklist;
}
