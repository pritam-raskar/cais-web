package com.dair.cais.access.Role;

import lombok.Data;

@Data
public class Role {
    private Integer roleId;
    private String roleName;
    private String description;
    private String roleIdentifier;
    private Long userCount;
}