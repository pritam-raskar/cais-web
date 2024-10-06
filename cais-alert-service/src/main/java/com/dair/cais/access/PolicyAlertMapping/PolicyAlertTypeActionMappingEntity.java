package com.dair.cais.access.PolicyAlertMapping;

import com.dair.cais.access.Actions.ActionEntity;
import com.dair.cais.access.alertType.alertTypeEntity;
import com.dair.cais.access.policy.PolicyEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cm_policy_alert_type_action_mapping", schema = "info_alert")
public class PolicyAlertTypeActionMappingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pata_seq")
    @SequenceGenerator(name = "pata_seq", sequenceName = "info_alert.cm_policy_alert_type_action_mapping_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "pata_id")
    private Integer pataId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private PolicyEntity policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alert_type_id", referencedColumnName = "alert_type_id", nullable = false)
    private alertTypeEntity alertType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    private ActionEntity action;

    @Column(name = "condition")
    private String condition;
}