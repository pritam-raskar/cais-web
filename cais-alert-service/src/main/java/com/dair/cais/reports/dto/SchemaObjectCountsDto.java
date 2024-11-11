package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * DTO for schema object counts
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchemaObjectCountsDto {
    private Integer tables;
    private Integer views;
    private Integer functions;
    private Integer sequences;
    private Long totalRowCount;
    private Long totalDiskSpace;
}
