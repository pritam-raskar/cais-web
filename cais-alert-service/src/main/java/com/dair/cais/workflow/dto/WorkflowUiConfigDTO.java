package com.dair.cais.workflow.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

/**
 * DTO for workflow UI configuration updates.
 */
@Data
public class WorkflowUiConfigDTO {
    @NotNull(message = "UI configuration cannot be null")
    private String uiConfig;
}