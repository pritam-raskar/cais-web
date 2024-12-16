package com.dair.cais.reports.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class ReportCreateDto {
    private List<ReportParameterDto> parameters;
    @NotBlank(message = "Report identifier is required")
    @Pattern(regexp = "^[a-zA-Z0-9_-]+$", message = "Report identifier can only contain letters, numbers, underscores and hyphens")
    @Size(max = 100)
    private String reportIdentifier;

    @NotBlank(message = "Report name is required")
    @Size(max = 255)
    private String reportName;

    @Size(max = 1000)
    private String reportDescription;

    @NotNull(message = "Connection ID is required")
    private Long connectionId;

    @NotBlank(message = "Table/View name is required")
    @Size(max = 255)
    private String tableViewName;

    @Pattern(regexp = "^(TABLE|CHART)$", message = "Report type must be either TABLE or CHART")
    private String reportType = "TABLE";

    private Integer cacheDuration = 0;
    private Integer maxRows = 1000;
    private List<ReportColumnCreateDto> columns;

    public List<ReportParameterDto> getParameters() {
        return parameters;
    }

    private Boolean isTab;
}