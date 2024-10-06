package com.dair.cais.access.PolicyModuleMapping;

import com.dair.cais.access.modules.ModuleEntity;
import com.dair.cais.access.policy.PolicyEntity;
import com.dair.cais.access.Actions.ActionEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cm_policy_module_action_mapping", schema = "info_alert")
public class PolicyModuleMappingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pma_seq")
    @SequenceGenerator(name = "pma_seq", sequenceName = "info_alert.cm_alert_type_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "pma_id")
    private Integer pmaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private PolicyEntity policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "module_id", nullable = false)
    private ModuleEntity module;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    private ActionEntity action;

    @Column(name = "condition")
    private String condition;
}