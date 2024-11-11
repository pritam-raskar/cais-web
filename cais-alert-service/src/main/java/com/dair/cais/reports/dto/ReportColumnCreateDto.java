package com.dair.cais.reports.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportColumnCreateDto {
    @NotBlank(message = "Source column name is required")
    @Size(max = 255)
    private String sourceColumn;

    @NotBlank(message = "Display name is required")
    @Size(max = 255)
    private String displayName;

    @NotBlank(message = "Data type is required")
    @Size(max = 50)
    private String dataType;

    private Boolean isSortable = true;
    private Boolean isFilterable = true;
    private Boolean isExportable = true;
    private Boolean isVisible = true;
    private Integer sortPriority = 0;

    @Pattern(regexp = "^(ASC|DESC)$", message = "Sort direction must be either ASC or DESC")
    private String sortDirection;

    @Size(max = 20)
    private String columnWidth;

    @Pattern(regexp = "^(left|center|right|justify)$", message = "Invalid alignment value")
    private String alignment = "left";

    private FormattingConfigDto formattingJson;
}
