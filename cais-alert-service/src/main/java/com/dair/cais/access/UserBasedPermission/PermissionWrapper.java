package com.dair.cais.access.UserBasedPermission;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
class PermissionWrapper {
    private Map<String, AlertTypeOrgPermissions> alertType;
    private Map<String, List<ActionCondition>> modules;
    private Map<String, List<ActionCondition>> reports;
}