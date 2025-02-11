package com.dair.cais.workflow.model;

import lombok.Data;

@Data
public class WorkflowStepPropertiesDTO {
    private Boolean isDefault = false;
    private WorkflowStepDeadlineDTO deadline;
}
