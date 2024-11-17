package com.dair.cais.alert.rdbms;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "cm_alerts" , schema = "info_alert")
@EntityListeners(AuditingEntityListener.class)
public class RdbmsAlertEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "alert_id", nullable = false, unique = true)
    private String alertId;

    @Column(name = "create_date", nullable = false)
    private LocalDateTime createDate;

    @Column(name = "last_update_date", nullable = false)
    private LocalDateTime lastUpdateDate;

    @Column(name = "total_score", nullable = false)
    private Double totalScore;

    @Column(name = "created_by")
    private String createdBy;

    @Column(name = "business_date", nullable = false)
    private LocalDateTime businessDate;

    @Column(name = "focal_entity", nullable = false)
    private String focalEntity;

    @Column(name = "focus", nullable = false)
    private String focus;

    @Column(name = "alert_type_id", nullable = false)
    private String alertTypeId;

    @Column(name = "alert_region", nullable = false)
    private String alertRegion;

    @Column(name = "alert_group_id", nullable = false)
    private String alertGroupId;

    @Column(name = "is_consolidated", nullable = false)
    private Boolean isConsolidated;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;

    @Column(name = "has_multiple_scenario", nullable = false)
    private Boolean hasMultipleScenario;

    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted;

    @Column(name = "org_id", nullable = false)
    private String orgUnitId;

    @Column(name = "org_family", nullable = false)
    private String orgFamily;

    @Column(name = "previous_org_unit_id", nullable = false)
    private String previousOrgUnitId;

    @Column(name = "is_org_unit_updated", nullable = false)
    private Boolean isOrgUnitUpdated;

    @Column(name = "is_related_alert", nullable = false)
    private Boolean isRelatedAlert;

    @Column(name = "owner_id", nullable = false)
    private String ownerId;

    @Column(name = "owner_name", nullable = false)
    private String ownerName;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "alert_step_id", nullable = false)
    private String alertStepId;

    @Column(name = "alert_step_name", nullable = false)
    private String alertStepName;

    @Column(name = "is_case_created", nullable = false)
    private Boolean isCaseCreated;

    @Column(name = "details", nullable = false, columnDefinition = "TEXT")
    private String details;

    @Column(name = "reason_details", columnDefinition = "jsonb")
    private String reasonDetails;

    // Additional fields
    @Column(name = "pivot")
    private String pivot;

    @Column(name = "account_id")
    private String accountId;

    @Column(name = "transaction_id")
    private String transactionId;

    @Column(name = "customer_id")
    private String customerId;

    @Column(name = "household_id")
    private String householdId;

    @Column(name = "priority")
    private String priority;

    @Column(name = "tag")
    private String tag;

    @Column(name = "branch_id")
    private String branchId;

    @Column(name = "representative_id")
    private String representativeId;

    @Column(name = "building_block")
    private String buildingBlock;

    @Column(name = "watch_list_id")
    private String watchListId;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}