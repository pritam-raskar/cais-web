package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * DTO for column formatting configuration
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ColumnFormattingDto {
    private String alignment;
    private String format;
    private String displayFormat;
    private Boolean useThousandsSeparator;
    private Integer decimalPlaces;
    private String prefix;
    private String suffix;
}