package com.dair.cais.reports.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ReportColumnCreateDto implements BaseColumnDto {
    @NotBlank(message = "Source column name is required")
    private String sourceColumn;

    @NotBlank(message = "Data type is required")
    private String dataType;

    private String displayName;
    private Boolean isSortable;
    private Boolean isFilterable;
    private Boolean isExportable;
    private Boolean isVisible;
    private Integer sortPriority;
    private String sortDirection;
    private String columnWidth;
    private String alignment;
    private FormattingConfigDto formattingJson;
}