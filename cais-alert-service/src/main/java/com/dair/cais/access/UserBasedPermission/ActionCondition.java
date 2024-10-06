package com.dair.cais.access.UserBasedPermission;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActionCondition {
    private String action;
    private String condition;
}
