package com.dair.cais.access.PolicyModuleMapping;

import lombok.Data;

@Data
public class PolicyModuleMapping {
    private Integer pmaId;
    private Integer policyId;
    private String policyName;
    private Integer moduleId;
    private String moduleName;
    private Integer actionId;
    private String actionName;
    private String condition;
}