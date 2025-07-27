package com.dair.cais.workflow.dto;

import lombok.Data;
import java.util.List;

/**
 * DTO for workflow step transitions.
 * Contains information about next possible steps and back steps.
 */
@Data
public class StepTransitionDTO {
    private List<StepInfo> nextSteps;
    private List<StepInfo> backSteps;
}