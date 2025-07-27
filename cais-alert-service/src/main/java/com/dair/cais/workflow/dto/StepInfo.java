package com.dair.cais.workflow.dto;

import lombok.Data;

/**
 * DTO for step information used in transitions.
 */
@Data
public class StepInfo {
    private String label;
    private Long stepId;
}