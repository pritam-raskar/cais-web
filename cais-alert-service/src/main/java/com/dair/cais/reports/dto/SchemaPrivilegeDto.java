package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * DTO for schema privileges
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SchemaPrivilegeDto {
    private String schemaName;
    private String grantee;
    private String privilegeType;
    private Boolean isGrantable;
}
