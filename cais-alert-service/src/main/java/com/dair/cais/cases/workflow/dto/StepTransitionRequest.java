package com.dair.cais.cases.workflow.dto;

import lombok.Data;

/**
 * DTO for case step transition requests.
 */
@Data
public class StepTransitionRequest {
    private String reason;
    private String comment;
}