package com.dair.cais.reports.dto;

public interface BaseColumnDto {
    String getDisplayName();
    Boolean getIsSortable();
    Boolean getIsFilterable();
    Boolean getIsExportable();
    Boolean getIsVisible();
    Integer getSortPriority();
    String getSortDirection();
    String getColumnWidth();
    String getAlignment();
    FormattingConfigDto getFormattingJson();
}

