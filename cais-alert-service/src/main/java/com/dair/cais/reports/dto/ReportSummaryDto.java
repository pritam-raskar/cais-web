package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportSummaryDto {
    private Integer reportId;
    private String reportIdentifier;
    private String reportName;
    private String reportDescription;
    private String reportType;
    private String status;
    private Boolean isPublished;
    private String createdBy;
    private java.time.ZonedDateTime createdAt;
    private String updatedBy;
    private java.time.ZonedDateTime updatedAt;
}
