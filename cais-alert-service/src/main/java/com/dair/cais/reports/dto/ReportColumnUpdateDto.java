package com.dair.cais.reports.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * DTO for column updates
 */
@Data
public class ReportColumnUpdateDto {
    private Integer crcId;

    @Size(max = 255)
    private String displayName;

    private Boolean isSortable;
    private Boolean isFilterable;
    private Boolean isExportable;
    private Boolean isVisible;
    private Integer sortPriority;

    @Pattern(regexp = "^(ASC|DESC)$")
    private String sortDirection;

    @Size(max = 20)
    private String columnWidth;

    @Pattern(regexp = "^(left|center|right|justify)$")
    private String alignment;

    private FormattingConfigDto formattingJson;
}