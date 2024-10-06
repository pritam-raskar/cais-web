package com.dair.cais.access.format;

import lombok.Data;

import java.util.List;

@Data
public class AlertTypePermissions {
    private List<ActionCondition> actions;
}