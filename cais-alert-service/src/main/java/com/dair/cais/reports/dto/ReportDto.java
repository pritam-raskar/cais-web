package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportDto extends BaseAuditDto {
    private Integer reportId;
    private String reportIdentifier;
    private String reportName;
    private String reportDescription;
    private Long connectionId;
    private String tableViewName;
    private String reportType;
    private String status;
    private Integer cacheDuration;
    private Integer maxRows;
    private Boolean isPublished;
    private List<ReportColumnDto> columns;
    private List<ReportParameterDto> parameters;  // Add this field
}