package com.dair.cais.access.UserBasedPermission;

import lombok.Data;

import java.util.Map;

@Data
public class AlertTypeOrgPermissions {
    private Map<String, OrgActions> orgId;
}
