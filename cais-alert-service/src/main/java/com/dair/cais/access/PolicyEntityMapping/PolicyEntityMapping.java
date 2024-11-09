package com.dair.cais.access.PolicyEntityMapping;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PolicyEntityMapping {
    private Integer mappingId;
    private Integer policyId;
    private String policyName;    // For convenience in responses
    private String entityType;
    private String entityId;
    private String entityName;
    private Integer actionId;
    private String actionName;    // For convenience in responses
    private String actionCategory;
    private String actionType;
    private String actionDescription;
    private String condition;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}