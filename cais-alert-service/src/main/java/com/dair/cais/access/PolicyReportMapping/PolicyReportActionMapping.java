package com.dair.cais.access.PolicyReportMapping;

import lombok.Data;

@Data
public class PolicyReportActionMapping {
    private Integer praId;
    private Integer policyId;
    private String policyName;
    private Integer reportId;
    private String reportName;
    private Integer actionId;
    private String actionName;
    private String condition;
}
