package com.dair.cais.workflow.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO for workflow operations.
 */
@Data
public class WorkflowDetailDTO {
    private Long workflowId;
    private String workflowName;
    private String description;
    private String createdBy;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedDate;
    private String updatedBy;
}