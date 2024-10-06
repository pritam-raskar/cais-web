package com.dair.cais.access.modules;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cm_modules", schema = "info_alert")
public class ModuleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "module_seq")
    @SequenceGenerator(name = "module_seq", sequenceName = "info_alert.cm_common_sequence", allocationSize = 1, schema = "info_alert")
    @Column(name = "module_id")
    private Integer moduleId;

    @Column(name = "module_name")
    private String moduleName;

    @Column(name = "level")
    private Integer level;

    @Column(name = "parent_module_id")
    private Integer parentModuleId;
}