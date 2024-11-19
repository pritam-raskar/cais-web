package com.dair.cais.note;

import com.dair.cais.audit.AuditLogRequest;

import java.util.List;

public  class AddNoteToMultipleAlertsRequest {
    private String note;
    private List<String> alertIds;
    private String createdBy;
    private String entity;
    private String entityValue;
    private AuditLogRequest auditLogRequest;

    // Getters and setters
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public List<String> getAlertIds() { return alertIds; }
    public void setAlertIds(List<String> alertIds) { this.alertIds = alertIds; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }

    public String getEntityValue() { return entityValue; }
    public void setEntityValue(String entityValue) { this.entityValue = entityValue; }

    public AuditLogRequest getAuditLogRequest() { return auditLogRequest; }
    public void setAuditLogRequest(AuditLogRequest auditLogRequest) { this.auditLogRequest = auditLogRequest; }
}