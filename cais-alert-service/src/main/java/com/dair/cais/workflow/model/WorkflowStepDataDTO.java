package com.dair.cais.workflow.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class WorkflowStepDataDTO {
    @NotNull
    private String label;
}
