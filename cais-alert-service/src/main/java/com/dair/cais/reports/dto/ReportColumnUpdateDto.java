package com.dair.cais.reports.dto;

import lombok.Data;

@Data
public class ReportColumnUpdateDto {
    private Integer crcId;

    // Required fields for new columns
    private String sourceColumn;
    private String dataType;

    // Updatable fields
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