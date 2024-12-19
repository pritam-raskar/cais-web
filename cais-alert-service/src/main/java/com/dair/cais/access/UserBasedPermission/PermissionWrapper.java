package com.dair.cais.access.UserBasedPermission;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Data
public class PermissionWrapper {
    private Map<String, AlertTypeOrgPermissionsNew> alertType;
    private Map<String, List<ActionCondition>> modules;
    private Map<String, List<ActionCondition>> reports;
    private Map<String, Object> additionalPermissions;

    @SuppressWarnings("unchecked")
    public void setPermissionsByType(String entityType, Object permissions) {
        switch(entityType.toLowerCase()) {
            case "alert-types":
                if (permissions instanceof Map) {
                    this.alertType = (Map<String, AlertTypeOrgPermissionsNew>) permissions;
                } else if (permissions instanceof AlertTypeOrgPermissionsNew) {
                    if (this.alertType == null) {
                        this.alertType = new HashMap<>();
                    }
                    this.alertType.put(entityType, (AlertTypeOrgPermissionsNew) permissions);
                }
                break;
            case "modules":
                this.modules = (Map<String, List<ActionCondition>>) permissions;
                break;
            case "reports":
                this.reports = (Map<String, List<ActionCondition>>) permissions;
                break;
            default:
                if (this.additionalPermissions == null) {
                    this.additionalPermissions = new HashMap<>();
                }
                this.additionalPermissions.put(entityType, permissions);
        }
    }
}