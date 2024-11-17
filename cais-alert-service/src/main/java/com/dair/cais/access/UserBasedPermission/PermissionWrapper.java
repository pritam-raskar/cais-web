package com.dair.cais.access.UserBasedPermission;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Data
public class PermissionWrapper {
    // Keep existing fields for backward compatibility
    private Map<String, AlertTypeOrgPermissions> alertType;
    private Map<String, List<ActionCondition>> modules;
    private Map<String, List<ActionCondition>> reports;

    // Add new generic map for future entity types
    private Map<String, Object> additionalPermissions;

    public void setPermissionsByType(String entityType, Object permissions) {
        switch(entityType.toLowerCase()) {
            case "alert-types":
                if (permissions instanceof Map) {
                    this.alertType = (Map<String, AlertTypeOrgPermissions>) permissions;
                } else if (permissions instanceof AlertTypeOrgPermissions) {
                    if (this.alertType == null) {
                        this.alertType = new HashMap<>();
                    }
                    this.alertType.put(entityType, (AlertTypeOrgPermissions) permissions);
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




//package com.dair.cais.access.UserBasedPermission;
//
//import lombok.Data;
//
//import java.util.List;
//import java.util.Map;
//
//@Data
//class PermissionWrapper {
//    private Map<String, AlertTypeOrgPermissions> alertType;
//    private Map<String, List<ActionCondition>> modules;
//    private Map<String, List<ActionCondition>> reports;
//}