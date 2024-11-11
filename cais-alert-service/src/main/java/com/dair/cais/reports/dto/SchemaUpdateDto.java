package com.dair.cais.reports.dto;

import lombok.Data;

import java.util.List;

/**
 * DTO for schema update request
 */
@Data
public class SchemaUpdateDto {
    private String schemaName;
    private String newDescription;
    private List<SchemaPrivilegeDto> privileges;
}
