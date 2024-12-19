package com.dair.cais.reports.tabs.dto;

import com.dair.cais.reports.dto.ReportColumnCreateDto;
import com.dair.cais.reports.dto.ReportParameterDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

/**
 * DTO for tab creation request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TabCreateRequestDto {
    @NotBlank(message = "Tab identifier is required")
    @Size(max = 100, message = "Tab identifier cannot exceed 100 characters")
    private String reportIdentifier;

    @NotBlank(message = "Tab name is required")
    @Size(max = 255, message = "Tab name cannot exceed 255 characters")
    private String reportName;

    private String reportDescription;

    @NotNull(message = "Connection ID is required")
    private Long connectionId;

    @NotBlank(message = "Table/View name is required")
    private String tableViewName;

    private String reportType;
    private Integer cacheDuration;
    private Integer maxRows;
    private List<ReportColumnCreateDto> columns;
    private List<ReportParameterDto> parameters;
}

