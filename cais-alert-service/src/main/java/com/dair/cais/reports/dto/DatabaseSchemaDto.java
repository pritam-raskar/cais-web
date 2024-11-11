package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import java.util.List;

/**
 * DTO for database schema information
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatabaseSchemaDto {
    private String schemaName;
    private String description;
    private Integer tableCount;
    private Integer viewCount;
    private List<TableMetadataDto> tables;
    private List<TableMetadataDto> views;
}

