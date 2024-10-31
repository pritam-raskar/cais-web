package com.dair.cais.access.userorgrolemapping;

import lombok.Data;
import java.util.List;

@Data
public class UserOrgRoleMappingRequest {
    private String userId;
    private List<OrgRoleMapping> mappings;

    @Data
    public static class OrgRoleMapping {
        private Integer orgId;
        private Integer roleId;
    }
}