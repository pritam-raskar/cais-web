package com.dair.cais.workflow.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class WorkflowConfigurationDTO {
    @NotNull
    @JsonProperty("workflow_id")
    private Long workflowId;

    @NotNull
    private List<WorkflowStepDTO> steps;

    @NotNull
    private List<WorkflowTransitionDTO> transitions;
}

