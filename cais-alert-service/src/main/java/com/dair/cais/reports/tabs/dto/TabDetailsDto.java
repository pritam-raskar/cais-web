package com.dair.cais.reports.tabs.dto;

import com.dair.cais.reports.dto.ReportColumnDto;
import com.dair.cais.reports.dto.ReportParameterDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for detailed tab response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TabDetailsDto {
    private Integer reportId;
    private String reportIdentifier;
    private String tabName;
    private String tabDescription;
    private String reportType;
    private String tableViewName;
    private Boolean isPublished;
    private List<ReportColumnDto> columns;
    private List<ReportParameterDto> parameters;
}
