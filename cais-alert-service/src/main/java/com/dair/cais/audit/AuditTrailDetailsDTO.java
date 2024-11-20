package com.dair.cais.audit;

import lombok.Data;

@Data
public class AuditTrailDetailsDTO {
    private AuditTrailEntity auditTrail;
    private String actionName;
    private String userName;

    public AuditTrailDetailsDTO(AuditTrailEntity auditTrail, String actionName, String userName) {
        this.auditTrail = auditTrail;
        this.actionName = actionName;
        this.userName = userName;
    }
}