package com.dair.cais.access.UserBasedPermission;

import lombok.Data;

import java.util.Map;

@Data
public class AlertTypeOrgPermissionsNew {
    private Map<String, OrgActionsNew> orgId;
}
