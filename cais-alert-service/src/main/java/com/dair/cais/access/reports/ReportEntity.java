package com.dair.cais.access.reports;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "cm_reports", schema = "info_alert")
public class ReportEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_seq")
    @SequenceGenerator(name = "report_seq", sequenceName = "info_alert.cm_reports_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "report_id")
    private Integer reportId;

    @Column(name = "report_name")
    private String reportName;

    @Column(name = "report_description")
    private String reportDescription;

    @Column(name = "connector_id")
    private Integer connectorId;

    @Column(name = "table_or_view_name")
    private String tableOrViewName;
}
