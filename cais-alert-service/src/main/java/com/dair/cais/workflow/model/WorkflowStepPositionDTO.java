package com.dair.cais.workflow.model;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class WorkflowStepPositionDTO {
    @NotNull
    private Integer x;

    @NotNull
    private Integer y;
}
