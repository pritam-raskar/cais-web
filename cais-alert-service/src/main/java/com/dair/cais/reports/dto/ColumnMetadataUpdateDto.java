package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * DTO for column metadata updates
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ColumnMetadataUpdateDto {
    private String columnName;
    private String description;
    private String displayName;
    private ColumnFormattingDto formatting;
}
