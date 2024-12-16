package com.dair.cais.reports;

import jakarta.persistence.*;
import jakarta.validation.constraints.AssertTrue;
import lombok.Data;
import lombok.ToString;
import org.hibernate.annotations.Comment;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "cm_report", schema = "info_alert")
@Data
public class ReportsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cm_report_seq")
    @SequenceGenerator(
            name = "cm_report_seq",
            sequenceName = "info_alert.cm_report_seq",
            allocationSize = 1
    )
    @Column(name = "report_id")
    @Comment("Primary key for the report")
    private Integer reportId;

    @Column(name = "report_identifier", length = 100, nullable = false, unique = true)
    @Comment("Unique identifier for the report")
    private String reportIdentifier;

    @Column(name = "report_name", length = 255, nullable = false)
    private String reportName;

    @Column(name = "report_description")
    private String reportDescription;

    @Column(name = "connection_id", nullable = false)
    @Comment("Foreign key to cm_connection table")
    private Long connectionId;

    @Column(name = "is_published")
    private Boolean isPublished = false;

    @Column(name = "is_tab")
    @Comment("Flag indicating if the report is displayed as a tab")
    private Boolean isTab = false;

    @Column(name = "table_view_name", length = 255, nullable = false)
    private String tableViewName;

    @Column(name = "status", length = 20, nullable = false)
    @Comment("Current status of the report: DRAFT, PUBLISHED, or ARCHIVED")
    private String status = "DRAFT";

    @Column(name = "report_type", length = 20, nullable = false)
    private String reportType = "TABLE";

    @Column(name = "cache_duration")
    @Comment("Cache duration in minutes, 0 means no cache")
    private Integer cacheDuration = 0;

    @Column(name = "max_rows")
    private Integer maxRows = 1000;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @OneToMany(mappedBy = "reportId", fetch = FetchType.LAZY)
    @OrderBy("sortPriority ASC")
    @ToString.Exclude
    private List<ReportColumnEntity> columns = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();

        // Set default values
        if (status == null) status = "DRAFT";
        if (reportType == null) reportType = "TABLE";
        if (cacheDuration == null) cacheDuration = 0;
        if (maxRows == null) maxRows = 1000;
        if (isPublished == null) isPublished = false;
        if (isTab == null) isTab = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }

    /**
     * Validates the status value against allowed values
     */
    @AssertTrue(message = "Invalid status value")
    @Transient
    private boolean isValidStatus() {
        return status == null ||
                status.equals("DRAFT") ||
                status.equals("PUBLISHED") ||
                status.equals("ARCHIVED");
    }

    /**
     * Validates the report type against allowed values
     */
    @AssertTrue(message = "Invalid report type value")
    @Transient
    private boolean isValidReportType() {
        return reportType == null ||
                reportType.equals("TABLE") ||
                reportType.equals("CHART");
    }

    // New parameters field with the necessary mapping
    @OneToMany(mappedBy = "report", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @ToString.Exclude
    private List<ReportParameterEntity> parameters = new ArrayList<>();
}