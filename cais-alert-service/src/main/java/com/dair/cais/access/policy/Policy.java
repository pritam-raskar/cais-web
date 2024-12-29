package com.dair.cais.access.policy;

import lombok.Data;

import java.util.List;

@Data
public class Policy {
    private Integer policyId;
    private String name;
    private String description;
    private String type;
    private Boolean isActive;
    private List<AssociatedRole> associatedRoles;
}