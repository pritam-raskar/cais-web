package com.dair.cais.access.policy;

import lombok.Data;

@Data
public class Policy {
    private Integer policyId;
    private String name;
    private String description;
    private String type;
    private Boolean isActive;
}