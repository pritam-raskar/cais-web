package com.dair.cais.access.modules;

import lombok.Data;

@Data
public class Module {
    private Integer moduleId;
    private String moduleName;
    private Long level;
    private Long parentModuleId;
}