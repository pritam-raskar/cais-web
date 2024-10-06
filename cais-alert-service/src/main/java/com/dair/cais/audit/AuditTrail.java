package com.dair.cais.audit;

import lombok.Data;

import java.time.ZonedDateTime;

@Data
public class AuditTrail {
    private Long auditId;
    private Integer actionId;
    private Long userId;
    private String userRole;
    private ZonedDateTime actionTimestamp;
    private String description;
    private String category;
    private String affectedItemType;
    private String affectedItemId;
    private String oldValue;
    private String newValue;

    // Getters and setters
}