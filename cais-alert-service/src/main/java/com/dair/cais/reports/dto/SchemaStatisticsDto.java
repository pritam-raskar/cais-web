package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.ZonedDateTime;

/**
 * DTO for schema statistics
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchemaStatisticsDto {
    private String schemaName;
    private Long diskSize;
    private Long rowCount;
    private Integer objectCount;
    private ZonedDateTime lastAnalyzed;
}
