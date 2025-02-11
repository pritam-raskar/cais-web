package com.dair.cais.workflow.model;

import lombok.Data;

@Data
public class WorkflowStepDeadlineChangeDTO {
    private Boolean active = false;
    private Long stepId;
}
