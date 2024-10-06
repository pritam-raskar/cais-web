package com.dair.cais.access.UserBasedPermission;

import lombok.Data;

import java.util.List;

@Data
public class OrgActions {
    private List<ActionCondition> actions;
}
