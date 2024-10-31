package com.dair.cais.access.userorgrolemapping;

import lombok.Data;

@Data
public class UserOrgRoleMappingDto {
    private String userId;
    private Long mappingId;
    private Integer orgId;
    private Integer roleId;
}
