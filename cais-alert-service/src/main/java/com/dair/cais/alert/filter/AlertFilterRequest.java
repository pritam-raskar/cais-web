package com.dair.cais.alert.filter;

import com.dair.cais.audit.AuditLogRequest;
import com.dair.cais.alert.filter.FilterCriteria;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AlertFilterRequest {
    private FilterCriteria filterCriteria;
    private AuditLogRequest auditLogRequest;
}