package com.dair.cais.access.RoleBasedPermission;
import lombok.Data;
import java.util.List;
import java.util.Set;

@Data
public class RolePolicyDocument {
    private String role;
    private List<Permission> permissions;
    private Metadata metadata;

    @Data
    public static class Permission {
        private Integer policyId;
        private List<String> actions;
        private List<String> alertTypes;
        private List<String> modules;
        private List<String> reports;
        private String condition;
    }

    @Data
    public static class Metadata {
        private int totalPolicies;
        private Set<String> uniqueActions;
        private Set<String> uniqueAlertTypes;
        private Set<String> uniqueModules;
        private Set<String> uniqueReports;
    }
}