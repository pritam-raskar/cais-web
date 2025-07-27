package com.dair.cais.cases.casealert.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity class for Case-Alert association table in database.
 */
@Data
@Entity
@Table(name = "cm_case_alert", schema = "info_alert",
        uniqueConstraints = @UniqueConstraint(columnNames = {"case_id", "alert_id"}))
@EntityListeners(AuditingEntityListener.class)
public class CaseAlertEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "case_alert_seq")
    @SequenceGenerator(name = "case_alert_seq", sequenceName = "info_alert.cm_case_alert_id_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "id")
    private Long id;

    @Column(name = "case_id", nullable = false)
    private Long caseId;

    @Column(name = "alert_id", nullable = false)
    private String alertId;

    @Column(name = "added_by", nullable = false)
    private String addedBy;

    @CreatedDate
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @PrePersist
    protected void onCreate() {
        if (addedAt == null) {
            addedAt = LocalDateTime.now();
        }
    }
}