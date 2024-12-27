package com.dair.cais.steps.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
public class StepDTO {
    private Long stepId;

    @NotNull(message = "Step name cannot be null")
    @Size(min = 1, max = 255, message = "Step name must be between 1 and 255 characters")
    private String stepName;

    @Size(max = 1000, message = "Description cannot exceed 1000 characters")
    private String description;

    private String createdBy;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
    private String updatedBy;
    private Integer stepStatusId;
}