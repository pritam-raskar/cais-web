package com.dair.cais.cases.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity class for Case table in database.
 */
@Data
@Entity
@Table(name = "cm_case", schema = "info_alert")
@EntityListeners(AuditingEntityListener.class)
public class CaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "case_seq")
    @SequenceGenerator(name = "case_seq", sequenceName = "info_alert.cm_case_case_id_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "case_id")
    private Long caseId;

    @Column(name = "case_number", unique = true, nullable = false)
    private String caseNumber;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "priority")
    private String priority;

    @Column(name = "case_type")
    private String caseType;

    @Column(name = "org_unit_id")
    private String orgUnitId;

    @Column(name = "org_family")
    private String orgFamily;

    @Column(name = "owner_id")
    private String ownerId;

    @Column(name = "owner_name")
    private String ownerName;

    @Column(name = "workflow_id")
    private Long workflowId;

    @Column(name = "current_step_id")
    private Long currentStepId;

    @Column(name = "current_step_name")
    private String currentStepName;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "resolution", columnDefinition = "TEXT")
    private String resolution;

    @Column(name = "created_by")
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}