package com.dair.cais.access.PolicyReportMapping;

import com.dair.cais.access.reports.ReportEntity;
import com.dair.cais.access.policy.PolicyEntity;
import com.dair.cais.access.Actions.ActionEntity;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cm_policy_report_action_mapping", schema = "info_alert")
public class PolicyReportActionMappingEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pra_seq")
    @SequenceGenerator(name = "pra_seq", sequenceName = "info_alert.cm_policy_report_action_mapping_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "pra_id")
    private Integer praId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "policy_id", nullable = false)
    private PolicyEntity policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private ReportEntity report;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", nullable = false)
    private ActionEntity action;

    @Column(name = "condition")
    private String condition;
}
