package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * Full Column DTO including all fields
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ReportColumnDto extends BaseAuditDto {
    private Integer crcId;
    private Integer reportId;
    private String sourceColumn;
    private String displayName;
    private String dataType;
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