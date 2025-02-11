package com.dair.cais.workflow.model;

import lombok.Data;

@Data
public class WorkflowStepDeadlineActionsDTO {
    private WorkflowStepDeadlineEmailDTO sendEmailBeforeDeadline;
    private WorkflowStepDeadlineChangeDTO changeStepTo;
}
