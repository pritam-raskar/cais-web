package com.dair.cais.workflow.model;

import lombok.Data;

@Data
public class WorkflowTransitionPropertiesDTO {
    private Boolean allowAutomaticTransition = false;
    private Boolean requiredNote = false;
}
