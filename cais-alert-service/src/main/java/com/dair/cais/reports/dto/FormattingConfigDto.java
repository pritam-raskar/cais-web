package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * DTO for formatting configuration (JSONB column)
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FormattingConfigDto {
    private VisualFormatDto visualFormat;
    private DataFormatDto dataFormat;
    private List<ConditionalFormatDto> conditionalFormats;
}