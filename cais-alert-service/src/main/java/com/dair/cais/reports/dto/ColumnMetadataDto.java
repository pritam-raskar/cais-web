package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ColumnMetadataDto {
    private String columnName;
    private String dataType;
    private String description;
    private String defaultValue;
    private Boolean nullable;
    private Integer maxLength;
    private Integer precision;
    private Integer scale;
    private Integer ordinalPosition;
    private Boolean isPrimaryKey;  // Changed from primaryKey
    private Boolean isForeignKey;  // Changed from foreignKey
    private String foreignTableName;
    private String foreignColumnName;
    private ColumnFormattingDto suggestedFormatting;
}