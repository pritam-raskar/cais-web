package com.dair.cais.note;

import com.dair.cais.audit.AuditLogRequest;

public class AddNoteWithAuditRequest {
    private String note;
    private String createdBy;
    private String entity;
    private String entityValue;
    private AuditLogRequest auditLogRequest;

    // Getters and setters
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getEntity() { return entity; }
    public void setEntity(String entity) { this.entity = entity; }

    public String getEntityValue() { return entityValue; }
    public void setEntityValue(String entityValue) { this.entityValue = entityValue; }

    public AuditLogRequest getAuditLogRequest() { return auditLogRequest; }
    public void setAuditLogRequest(AuditLogRequest auditLogRequest) { this.auditLogRequest = auditLogRequest; }
}