package com.dair.cais.alert;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class AlertMapper {
    private final ObjectMapper objectMapper;

    public Alert toModel(AlertEntity entity) {
        Alert alert = new Alert();
        alert.setId(String.valueOf(entity.getId()));
        alert.setAlertId(entity.getAlertId());
        alert.setCreateDate(entity.getCreateDate());
        alert.setLastUpdateDate(entity.getLastUpdateDate());
        alert.setTotalScore(entity.getTotalScore());
        alert.setCreatedBy(entity.getCreatedBy());
        alert.setBusinessDate(entity.getBusinessDate());
        alert.setFocalEntity(entity.getFocalEntity());
        alert.setFocus(entity.getFocus());
        alert.setAlertTypeId(entity.getAlertTypeId());
        alert.setAlertRegion(entity.getAlertRegion());
        alert.setAlertGroupId(entity.getAlertGroupId());
        alert.setIsConsolidated(entity.getIsConsolidated());
        alert.setIsActive(entity.getIsActive());
        alert.setHasMultipleScenario(entity.getHasMultipleScenario());
        alert.setIsDeleted(entity.getIsDeleted());
        alert.setOrgUnitId(entity.getOrgUnitId());
        alert.setOrgUnitKey(entity.getOrgUnitKey());
        alert.setOrgFamily(entity.getOrgFamily());
        alert.setPreviousOrgUnitId(entity.getPreviousOrgUnitId());
        alert.setIsOrgUnitUpdated(entity.getIsOrgUnitUpdated());
        alert.setIsRelatedAlert(entity.getIsRelatedAlert());
        alert.setOwnerId(entity.getOwnerId());
        alert.setOwnerName(entity.getOwnerName());
        alert.setStatus(entity.getStatus());
        alert.setAlertStepId(entity.getAlertStepId());
        alert.setAlertStepName(entity.getAlertStepName());
        alert.setIsCaseCreated(entity.getIsCaseCreated());
        alert.setDetails(entity.getDetails());
        alert.setReasonDetails(entity.getReasonDetails());
        alert.setCustomFields(entity.getCustomFields());
        alert.setWorkflowInfo(entity.getWorkflowInfo());

        // Additional fields
        alert.setPivot(entity.getPivot());
        alert.setAccountId(entity.getAccountId());
        alert.setTransactionId(entity.getTransactionId());
        alert.setCustomerId(entity.getCustomerId());
        alert.setHouseHoldId(entity.getHouseHoldId());
        alert.setPriority(entity.getPriority());
        alert.setTag(entity.getTag());
        alert.setBranchId(entity.getBranchId());
        alert.setRepresentativeId(entity.getRepresentativeId());
        alert.setBuildingBlock(entity.getBuildingBlock());
        alert.setWatchListId(entity.getWatchListId());

        // New fields
        alert.setRepresentativeName(entity.getRepresentativeName());
        alert.setLastStepUpdatedDate(entity.getLastStepUpdatedDate());
        alert.setDueIn(entity.getDueIn());
        alert.setDeadLine(entity.getDeadLine());
        alert.setOrgKey(entity.getOrgKey());
        alert.setBranchName(entity.getBranchName());
        alert.setCustomerName(entity.getCustomerName());
        alert.setAccountName(entity.getAccountName());
        alert.setScenarioModelIds(entity.getScenarioModelIds());
        alert.setScenarioModelNames(entity.getScenarioModelNames());
        alert.setAlertTypeName(entity.getAlertTypeName());

        // Audit timestamps
        alert.setCreatedAt(entity.getCreatedAt());
        alert.setUpdatedAt(entity.getUpdatedAt());

        return alert;
    }

    public AlertEntity toEntity(Alert alert) {
        AlertEntity alertEntity = new AlertEntity();
        alertEntity.setId(alert.getId());
        mapAlertToEntity(alert, alertEntity);
        return alertEntity;
    }

    public AlertEntity toEntity(String alertId, Alert alert) {
        AlertEntity alertEntity = new AlertEntity();
        alertEntity.setId(alertId);
        mapAlertToEntity(alert, alertEntity);
        return alertEntity;
    }

    private void mapAlertToEntity(Alert alert, AlertEntity alertEntity) {
        alertEntity.setAlertId(alert.getAlertId());
        alertEntity.setCreateDate(alert.getCreateDate());
        alertEntity.setLastUpdateDate(alert.getLastUpdateDate());
        alertEntity.setTotalScore(alert.getTotalScore());
        alertEntity.setCreatedBy(alert.getCreatedBy());
        alertEntity.setBusinessDate(alert.getBusinessDate());
        alertEntity.setFocalEntity(alert.getFocalEntity());
        alertEntity.setFocus(alert.getFocus());
        alertEntity.setAlertTypeId(alert.getAlertTypeId());
        alertEntity.setAlertRegion(alert.getAlertRegion());
        alertEntity.setAlertGroupId(alert.getAlertGroupId());
        alertEntity.setIsConsolidated(alert.getIsConsolidated());
        alertEntity.setIsActive(alert.getIsActive());
        alertEntity.setHasMultipleScenario(alert.getHasMultipleScenario());
        alertEntity.setIsDeleted(alert.getIsDeleted());
        alertEntity.setOrgUnitId(alert.getOrgUnitId());
        alertEntity.setOrgUnitKey(alert.getOrgUnitKey());
        alertEntity.setOrgFamily(alert.getOrgFamily());
        alertEntity.setPreviousOrgUnitId(alert.getPreviousOrgUnitId());
        alertEntity.setIsOrgUnitUpdated(alert.getIsOrgUnitUpdated());
        alertEntity.setIsRelatedAlert(alert.getIsRelatedAlert());
        alertEntity.setOwnerId(alert.getOwnerId());
        alertEntity.setOwnerName(alert.getOwnerName());
        alertEntity.setStatus(alert.getStatus());
        alertEntity.setAlertStepId(alert.getAlertStepId());
        alertEntity.setAlertStepName(alert.getAlertStepName());
        alertEntity.setIsCaseCreated(alert.getIsCaseCreated());
        alertEntity.setDetails(alert.getDetails());
        alertEntity.setReasonDetails(alert.getReasonDetails());
        alertEntity.setCustomFields(alert.getCustomFields());
        alertEntity.setWorkflowInfo(alert.getWorkflowInfo());

        // Additional fields
        alertEntity.setPivot(alert.getPivot());
        alertEntity.setAccountId(alert.getAccountId());
        alertEntity.setTransactionId(alert.getTransactionId());
        alertEntity.setCustomerId(alert.getCustomerId());
        alertEntity.setHouseHoldId(alert.getHouseHoldId());
        alertEntity.setPriority(alert.getPriority());
        alertEntity.setTag(alert.getTag());
        alertEntity.setBranchId(alert.getBranchId());
        alertEntity.setRepresentativeId(alert.getRepresentativeId());
        alertEntity.setBuildingBlock(alert.getBuildingBlock());
        alertEntity.setWatchListId(alert.getWatchListId());

        // New fields
        alertEntity.setRepresentativeName(alert.getRepresentativeName());
        alertEntity.setLastStepUpdatedDate(alert.getLastStepUpdatedDate());
        alertEntity.setDueIn(alert.getDueIn());
        alertEntity.setDeadLine(alert.getDeadLine());
        alertEntity.setOrgKey(alert.getOrgKey());
        alertEntity.setBranchName(alert.getBranchName());
        alertEntity.setCustomerName(alert.getCustomerName());
        alertEntity.setAccountName(alert.getAccountName());
        alertEntity.setScenarioModelIds(alert.getScenarioModelIds());
        alertEntity.setScenarioModelNames(alert.getScenarioModelNames());
        alertEntity.setAlertTypeName(alert.getAlertTypeName());

        // Audit timestamps
        if (alert.getCreatedAt() != null) {
            alertEntity.setCreatedAt(alert.getCreatedAt());
        }
        if (alert.getUpdatedAt() != null) {
            alertEntity.setUpdatedAt(alert.getUpdatedAt());
        }
    }
}



//package com.dair.cais.alert;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//
//@Component
//@Slf4j
//@RequiredArgsConstructor
//public class AlertMapper {
//    private final ObjectMapper objectMapper;
//
//    public Alert toModel(AlertEntity entity) {
//        Alert alert = new Alert();
//        alert.setId(String.valueOf(entity.getId()));
//        alert.setAlertId(entity.getAlertId());
//        alert.setCreateDate(entity.getCreateDate());
//        alert.setLastUpdateDate(entity.getLastUpdateDate());
//        alert.setTotalScore(entity.getTotalScore());
//        alert.setCreatedBy(entity.getCreatedBy());
//        alert.setBusinessDate(entity.getBusinessDate());
//        alert.setFocalEntity(entity.getFocalEntity());
//        alert.setFocus(entity.getFocus());
//        alert.setAlertTypeId(entity.getAlertTypeId());
//        alert.setAlertRegion(entity.getAlertRegion());
//        alert.setAlertGroupId(entity.getAlertGroupId());
//        alert.setIsConsolidated(entity.getIsConsolidated());
//        alert.setIsActive(entity.getIsActive());
//        alert.setHasMultipleScenario(entity.getHasMultipleScenario());
//        alert.setIsDeleted(entity.getIsDeleted());
//        alert.setOrgUnitId(entity.getOrgUnitId());
//        alert.setOrgFamily(entity.getOrgFamily());
//        alert.setPreviousOrgUnitId(entity.getPreviousOrgUnitId());
//        alert.setIsOrgUnitUpdated(entity.getIsOrgUnitUpdated());
//        alert.setIsRelatedAlert(entity.getIsRelatedAlert());
//        alert.setOwnerId(entity.getOwnerId());
//        alert.setOwnerName(entity.getOwnerName());
//        alert.setStatus(entity.getStatus());
//        alert.setAlertStepId(entity.getAlertStepId());
//        alert.setAlertStepName(entity.getAlertStepName());
//        alert.setIsCaseCreated(entity.getIsCaseCreated());
//        alert.setDetails(entity.getDetails());
//        alert.setReasonDetails(entity.getReasonDetails());
//        alert.setCustomFields(entity.getCustomFields());
//        alert.setWorkflowInfo(entity.getWorkflowInfo());
//
//        // Additional fields
//        alert.setPivot(entity.getPivot());
//        alert.setAccountId(entity.getAccountId());
//        alert.setTransactionId(entity.getTransactionId());
//        alert.setCustomerId(entity.getCustomerId());
//        alert.setHouseHoldId(entity.getHouseHoldId());
//        alert.setPriority(entity.getPriority());
//        alert.setTag(entity.getTag());
//        alert.setBranchId(entity.getBranchId());
//        alert.setRepresentativeId(entity.getRepresentativeId());
//        alert.setBuildingBlock(entity.getBuildingBlock());
//        alert.setWatchListId(entity.getWatchListId());
//
//        // Audit timestamps
//        alert.setCreatedAt(entity.getCreatedAt());
//        alert.setUpdatedAt(entity.getUpdatedAt());
//
//        return alert;
//    }
//
//    public AlertEntity toEntity(Alert alert) {
//        AlertEntity alertEntity = new AlertEntity();
//        alertEntity.setId(alert.getId());
//        mapAlertToEntity(alert, alertEntity);
//        return alertEntity;
//    }
//
//    public AlertEntity toEntity(String alertId, Alert alert) {
//        AlertEntity alertEntity = new AlertEntity();
//        alertEntity.setId(alertId);
//        mapAlertToEntity(alert, alertEntity);
//        return alertEntity;
//    }
//
//    private void mapAlertToEntity(Alert alert, AlertEntity alertEntity) {
//        alertEntity.setAlertId(alert.getAlertId());
//        alertEntity.setCreateDate(alert.getCreateDate());
//        alertEntity.setLastUpdateDate(alert.getLastUpdateDate());
//        alertEntity.setTotalScore(alert.getTotalScore());
//        alertEntity.setCreatedBy(alert.getCreatedBy());
//        alertEntity.setBusinessDate(alert.getBusinessDate());
//        alertEntity.setFocalEntity(alert.getFocalEntity());
//        alertEntity.setFocus(alert.getFocus());
//        alertEntity.setAlertTypeId(alert.getAlertTypeId());
//        alertEntity.setAlertRegion(alert.getAlertRegion());
//        alertEntity.setAlertGroupId(alert.getAlertGroupId());
//        alertEntity.setIsConsolidated(alert.getIsConsolidated());
//        alertEntity.setIsActive(alert.getIsActive());
//        alertEntity.setHasMultipleScenario(alert.getHasMultipleScenario());
//        alertEntity.setIsDeleted(alert.getIsDeleted());
//        alertEntity.setOrgUnitId(alert.getOrgUnitId());
//        alertEntity.setOrgFamily(alert.getOrgFamily());
//        alertEntity.setPreviousOrgUnitId(alert.getPreviousOrgUnitId());
//        alertEntity.setIsOrgUnitUpdated(alert.getIsOrgUnitUpdated());
//        alertEntity.setIsRelatedAlert(alert.getIsRelatedAlert());
//        alertEntity.setOwnerId(alert.getOwnerId());
//        alertEntity.setOwnerName(alert.getOwnerName());
//        alertEntity.setStatus(alert.getStatus());
//        alertEntity.setAlertStepId(alert.getAlertStepId());
//        alertEntity.setAlertStepName(alert.getAlertStepName());
//        alertEntity.setIsCaseCreated(alert.getIsCaseCreated());
//        alertEntity.setDetails(alert.getDetails());
//        alertEntity.setReasonDetails(alert.getReasonDetails());
//        alertEntity.setCustomFields(alert.getCustomFields());
//        alertEntity.setWorkflowInfo(alert.getWorkflowInfo());
//
//        // Additional fields
//        alertEntity.setPivot(alert.getPivot());
//        alertEntity.setAccountId(alert.getAccountId());
//        alertEntity.setTransactionId(alert.getTransactionId());
//        alertEntity.setCustomerId(alert.getCustomerId());
//        alertEntity.setHouseHoldId(alert.getHouseHoldId());
//        alertEntity.setPriority(alert.getPriority());
//        alertEntity.setTag(alert.getTag());
//        alertEntity.setBranchId(alert.getBranchId());
//        alertEntity.setRepresentativeId(alert.getRepresentativeId());
//        alertEntity.setBuildingBlock(alert.getBuildingBlock());
//        alertEntity.setWatchListId(alert.getWatchListId());
//
//        // Audit timestamps
//        if (alert.getCreatedAt() != null) {
//            alertEntity.setCreatedAt(alert.getCreatedAt());
//        }
//        if (alert.getUpdatedAt() != null) {
//            alertEntity.setUpdatedAt(alert.getUpdatedAt());
//        }
//    }
//}