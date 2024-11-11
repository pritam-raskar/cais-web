package com.dair.cais.reports;


import com.dair.cais.reports.enums.enums;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@Table(name = "cm_report_parameter", schema = "info_alert")
@EqualsAndHashCode(callSuper = true)
public class ReportParameterEntity extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parameter_id")
    private Long parameterId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private ReportsEntity report;

    @Column(name = "parameter_name", nullable = false)
    private String parameterName;

    @Column(name = "parameter_label")
    private String parameterLabel;

    @Column(name = "parameter_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private enums.ParameterType parameterType;

    @Column(name = "is_required")
    private Boolean isRequired = false;

    @Column(name = "default_value")
    private String defaultValue;

    @Column(name = "validation_rules", columnDefinition = "jsonb")
    private String validationRules;
}
