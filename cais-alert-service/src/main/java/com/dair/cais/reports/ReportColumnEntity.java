package com.dair.cais.reports;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.ZonedDateTime;
import java.util.Map;

@Entity
@Table(name = "cm_report_columns", schema = "info_alert")
@Data
public class ReportColumnEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "report_columns_seq")
    @SequenceGenerator(name = "report_columns_seq",
            sequenceName = "info_alert.cm_report_columns_seq",
            allocationSize = 1)
    @Column(name = "crc_id")
    private Integer crcId;

    @Column(name = "report_id", nullable = false)
    private Integer reportId;

    @Column(name = "source_column", length = 255, nullable = false)
    private String sourceColumn;

    @Column(name = "data_type", length = 50, nullable = false)
    private String dataType;

    @Column(name = "display_name", length = 255, nullable = false)
    private String displayName;

    @Column(name = "formatting_json", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> formattingJson;  // Changed from Object to Map<String, Object>

    @Column(name = "is_sortable")
    private Boolean isSortable = true;

    @Column(name = "is_filterable")
    private Boolean isFilterable = true;

    @Column(name = "is_exportable")
    private Boolean isExportable = true;

    @Column(name = "is_visible")
    private Boolean isVisible = true;

    @Column(name = "sort_priority")
    private Integer sortPriority = 0;

    @Column(name = "sort_direction", length = 4)
    private String sortDirection;

    @Column(name = "column_width", length = 20)
    private String columnWidth;

    @Column(name = "alignment", length = 10)
    private String alignment = "left";

    @Column(name = "created_at", nullable = false)
    private ZonedDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = ZonedDateTime.now();
        updatedAt = ZonedDateTime.now();
        if (isSortable == null) isSortable = true;
        if (isFilterable == null) isFilterable = true;
        if (isExportable == null) isExportable = true;
        if (isVisible == null) isVisible = true;
        if (sortPriority == null) sortPriority = 0;
        if (alignment == null) alignment = "left";
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = ZonedDateTime.now();
    }
}