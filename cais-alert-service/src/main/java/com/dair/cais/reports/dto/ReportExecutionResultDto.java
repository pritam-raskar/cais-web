package com.dair.cais.reports.dto;

import lombok.Data;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for report execution results
 */
@Data
public class ReportExecutionResultDto {
    private Integer reportId;
    private String reportName;
    private List<ReportColumnDto> columns;
    private List<Map<String, Object>> data;
    private Integer totalRows;
    private Boolean isCached;
    private ZonedDateTime executionTime;
}