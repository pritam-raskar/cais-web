package com.dair.cais.workflow.model;

import lombok.Data;

@Data
public class WorkflowStepDeadlineEmailDTO {
    private Boolean active = false;
    private Integer count;
    private String measure;
}
