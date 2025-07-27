package com.dair.cais.cases.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Entity class for Case Type table in database.
 */
@Data
@Entity
@Table(name = "cm_case_type", schema = "info_alert")
@EntityListeners(AuditingEntityListener.class)
public class CaseTypeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "case_type_seq")
    @SequenceGenerator(name = "case_type_seq", sequenceName = "info_alert.cm_case_type_type_id_seq", allocationSize = 1, schema = "info_alert")
    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "workflow_id")
    private Long workflowId;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

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
/* todo: Features
    1 - Custom fields as json, same like alert type
    2 - add field - supportedAlertTypes - if null all or specific alert type array [trade, account]
    3 - bu family - give  default bu identifier depend upon case configuration, for example UI will have field
          just like policy validation context based and then as per that rule assign the BU.
    4 - scenario - if 1 alert is from US and another from UK and there is a case, then user will be able to access
        if he has access to the case as per case bu family, & need to disable the alert BU check if alert has BU and user has access to case.
    5 - Case to case mapping
    6 - case LOD (Line of Defence) routing logic

 */