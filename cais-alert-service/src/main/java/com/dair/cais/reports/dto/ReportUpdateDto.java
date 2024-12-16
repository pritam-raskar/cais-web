package com.dair.cais.reports.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import java.util.List;

@Data
public class ReportUpdateDto {
    @Size(max = 255)
    private String reportName;

    @Size(max = 1000)
    private String reportDescription;

    @Size(max = 255)
    private String tableViewName;

    @Pattern(regexp = "^(TABLE|CHART)$")
    private String reportType;

    private Integer cacheDuration;
    private Integer maxRows;
    private List<ReportColumnUpdateDto> columns;
    private List<ReportParameterDto> parameters;  // Add this field

    private Boolean isTab;
}