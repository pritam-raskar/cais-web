package com.dair.cais.audit;

public class AuditLogRequest {
    private Long userId;
    private String userRole;
    private Integer actionId;
    private String description;
    private String category;
    private String affectedItemType;
    private String affectedItemId;
    private String oldValue;
    private String newValue;

    public AuditLogRequest(AuditLogRequest auditLogRequest) {
    }

    // Getters and setters
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public Integer getActionId() { return actionId; }
    public void setActionId(Integer actionId) { this.actionId = actionId; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getAffectedItemType() { return affectedItemType; }
    public void setAffectedItemType(String affectedItemType) { this.affectedItemType = affectedItemType; }

    public String getAffectedItemId() { return affectedItemId; }
    public void setAffectedItemId(String affectedItemId) { this.affectedItemId = affectedItemId; }

    public String getOldValue() { return oldValue; }
    public void setOldValue(String oldValue) { this.oldValue = oldValue; }

    public String getNewValue() { return newValue; }
    public void setNewValue(String newValue) { this.newValue = newValue; }
}