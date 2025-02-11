package com.dair.cais.workflow.model;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class WorkflowTransitionDTO {
    @NotNull
    private Long source;

    @NotNull
    private Long target;

    @NotNull
    private WorkflowTransitionPropertiesDTO props;

    private List<Long> reasons;
}
