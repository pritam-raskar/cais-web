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
    private String lastUpdateDate; //This has to be changed to Date
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
    private Map<String, Object> reasonDetails;
    private Map<String, Object> customFields;
    private Map<String, Object> workflowInfo;
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
    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}




//package com.dair.cais.alert;
//
//import lombok.Data;
//import org.springframework.data.mongodb.core.mapping.FieldType;
//import org.springframework.data.mongodb.core.mapping.MongoId;
//
//import java.io.Serializable;
//import java.util.Date;
//import java.util.Map;
//
//@Data
//public class AlertEntity implements Serializable {
//
//    private static final long serialVersionUID = 1L;
//
//    @MongoId(FieldType.OBJECT_ID)
//    private String id;
//    private String alertId;
//    private Date createDate;
//    private Date lastUpdateDate;
//    private Number totalScore;
//    private String createdBy;
//    private Date businessDate;
//    private String focalEntity;
//    private String focus;
//    private String alertTypeId;
//    private String alertRegion;
//    private String alertGroupId;
//    private Boolean isConsolidated;
//    private Boolean isActive;
//    private Boolean hasMultipleScenario;
//    private Boolean isDeleted;
//    private String orgUnitId;
//    private String orgFamily;
//    private String previousOrgUnitId;
//    private Boolean isOrgUnitUpdated;
//    private Boolean isRelatedAlert;
//    private String ownerId;
//    private String ownerName;
//    private String status;
//    private String alertStepId;
//    private String alertStepName;
//    private Boolean isCaseCreated;
//    private String details;
//    private Map<String, Object> reasonDetails;
//    private Map<String, Object> customFields;
//    private Map<String, Object> workflowInfo;
//
//
//}