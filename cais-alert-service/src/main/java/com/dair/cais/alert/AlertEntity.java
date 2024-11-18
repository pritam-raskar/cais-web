package com.dair.cais.alert;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Map;

@NoArgsConstructor
@Data
public class AlertEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    @MongoId(FieldType.OBJECT_ID)
    private String id;
    private String alertId;
    private String createDate;
    private String lastUpdateDate;
    private Number totalScore;
    private String createdBy;
    private String businessDate;
    private String focalEntity;
    private String focus;
    private String alertTypeId;
    private String alertRegion;
    private String alertGroupId;
    private Boolean isConsolidated;
    private Boolean isActive;
    private Boolean hasMultipleScenario;
    private Boolean isDeleted;
    private String orgUnitId;
    private String orgFamily;
    private String previousOrgUnitId;
    private Boolean isOrgUnitUpdated;
    private Boolean isRelatedAlert;
    private String ownerId;
    private String ownerName;
    private String status;
    private String alertStepId;
    private String alertStepName;
    private Boolean isCaseCreated;
    private String details;


    // Existing additional fields
    private String pivot;
    private String accountId;
    private String transactionId;
    private String customerId;
    private String houseHoldId;
    private String priority;
    private String tag;
    private String branchId;
    private String representativeId;
    private String buildingBlock;
    private String watchListId;

    // New fields
    private String representativeName;
    private String lastStepUpdatedDate;  // Keep as String for MongoDB
    private String dueIn;
    private String deadLine;             // Keep as String for MongoDB
    private String orgKey;
    private String branchName;
    private String customerName;
    private String accountName;

    private Map<String, Object> reasonDetails;
    private Map<String, Object> customFields;
    private Map<String, Object> workflowInfo;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}