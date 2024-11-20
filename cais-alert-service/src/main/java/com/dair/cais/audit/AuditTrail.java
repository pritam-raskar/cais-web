package com.dair.cais.audit;

import lombok.Data;
import java.time.ZonedDateTime;

@Data
public class AuditTrail {
    private Long auditId;
    private Integer actionId;
    private String actionName;
    private Long userId;
    private String userRole;
    private String userName;
    private ZonedDateTime actionTimestamp;
    private String description;
    private String category;
    private String affectedItemType;
    private String affectedItemId;
    private String oldValue;
    private String newValue;
}