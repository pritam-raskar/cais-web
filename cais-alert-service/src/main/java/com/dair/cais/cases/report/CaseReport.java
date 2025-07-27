package com.dair.cais.cases.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for Case Reports.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CaseReport {
    private Long caseId;
    private String caseNumber;
    private String title;
    private String status;
    private String priority;
    private String caseType;
    private String orgUnitId;
    private String orgFamily;
    private String ownerId;
    private String ownerName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime closedAt;
    private Duration resolutionTime;
    private String resolution;
    private String currentStepName;
    private Integer daysOpen;
    private Boolean isWithinSla;
}