package com.dair.cais.access.RolePolicyMapping;

import lombok.Data;

@Data
public class RolesPolicyMapping {
    private Integer rpmId;
    private Integer policyId;
    private Integer roleId;
    private String policyName; // Optional: for convenience
    private String roleName;   // Optional: for convenience
}