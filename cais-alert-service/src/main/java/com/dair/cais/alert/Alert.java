package com.dair.cais.alert;


import com.dair.model.CaisBaseModel;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)

public class Alert extends CaisBaseModel {
    private String id;
    private String alertId;
    private String createDate;   //This has to be changed to Date
    private String lastUpdateDate; //This has to be changed to Date
    private Number totalScore;
    private String createdBy;
    private String businessDate; //This has to be changed to Date
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
    private Map<String, Object> reasonDetails;
    private Map<String, Object> customFields;
    private Map<String, Object> workflowInfo;

    // Additional fields
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
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Add getters and setters for these fields
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

}