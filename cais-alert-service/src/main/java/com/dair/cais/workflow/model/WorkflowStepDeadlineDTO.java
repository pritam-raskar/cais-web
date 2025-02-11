package com.dair.cais.workflow.model;

import lombok.Data;

@Data
public class WorkflowStepDeadlineDTO {
    private Boolean active = false;
    private Integer count;
    private String measure;
    private WorkflowStepDeadlineActionsDTO actions;
}
