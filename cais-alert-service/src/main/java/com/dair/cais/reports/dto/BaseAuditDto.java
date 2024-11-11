package com.dair.cais.reports.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.ZonedDateTime;

/**
 * Base DTO for common audit fields
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseAuditDto {
    private String createdBy;
    private ZonedDateTime createdAt;
    private String updatedBy;
    private ZonedDateTime updatedAt;
}