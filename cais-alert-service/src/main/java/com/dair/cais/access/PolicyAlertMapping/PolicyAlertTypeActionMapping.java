package com.dair.cais.access.PolicyAlertMapping;


import lombok.Data;

@Data

public class PolicyAlertTypeActionMapping {
    private Integer pataId;
    private Integer policyId;
    private String alertTypeId;
    private Integer actionId;
    private String actionName;
    private String actionCategory;
    private String actionType;
    private String actionDescription;
    private String condition;
}