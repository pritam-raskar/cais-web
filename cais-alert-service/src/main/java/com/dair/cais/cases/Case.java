package com.dair.cais.cases;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Case management.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Case {
    private Long caseId;
    private String caseNumber;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String caseType;
    private String orgUnitId;
    private String orgFamily;
    private String ownerId;
    private String ownerName;
    private Long workflowId;
    private Long currentStepId;
    private String currentStepName;
    private LocalDateTime dueDate;
    private String resolution;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    private Boolean isActive;
}