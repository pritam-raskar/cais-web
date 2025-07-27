package com.dair.cais.cases.audit;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Data Transfer Object for Case Audit Trail.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuditTrail {
    private Long auditId;
    private Long caseId;
    private String action;
    private String userId;
    private String userName;
    private LocalDateTime timestamp;
    private String details;
    private String oldValue;
    private String newValue;
}