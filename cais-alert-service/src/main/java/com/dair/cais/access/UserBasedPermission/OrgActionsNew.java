package com.dair.cais.access.UserBasedPermission;

import lombok.Data;

import java.util.Map;

@Data
public class OrgActionsNew {
    private Map<String, ActionFormat> actions;
}
