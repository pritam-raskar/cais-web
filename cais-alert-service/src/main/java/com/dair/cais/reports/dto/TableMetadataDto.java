package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * DTO for table metadata
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TableMetadataDto {
    private String tableSchema;
    private String tableName;
    private String tableType;
    private String description;
    private Long approximateRowCount;
    private Integer columnCount;
    private Boolean isView;
    private Boolean hasData;

    // Additional calculated properties
    public String getFullName() {
        return tableSchema + "." + tableName;
    }
}