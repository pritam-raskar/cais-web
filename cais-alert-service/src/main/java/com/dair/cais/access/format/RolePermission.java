package com.dair.cais.access.format;

import lombok.Data;
import java.util.Map;
import java.util.List;

@Data
public class RolePermission {
    private String role;
    private Map<String, AlertTypePermissions> alertType;
    private Map<String, List<ActionCondition>> modules;
    private Map<String, List<ActionCondition>> reports;
}

