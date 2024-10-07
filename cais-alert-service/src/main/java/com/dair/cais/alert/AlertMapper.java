package com.dair.cais.alert;

import org.springframework.stereotype.Component;

@Component
public class AlertMapper {


    public Alert toModel(AlertEntity entity) {
        Alert alert = new Alert();
        alert.setId(String.valueOf(entity.getId()));;
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


//        alertEntity.setName(alert.getName());
//        alertEntity.setType(alert.getType());
//        alertEntity.setAccountNumber(alert.getAccountNumber());
//        alertEntity.setBusinessUnit(alert.getBusinessUnit());
//        alertEntity.setBusinessUnitFamily(alert.getBusinessUnitFamily());
//        alertEntity.setBusinessUnitFamilyPrevious(alert.getBusinessUnitFamilyPrevious());
//        alertEntity.setDetails(alert.getDetails());
//        alertEntity.setScore(alert.getScore());
//        alertEntity.setState(alert.getState());
//        alertEntity.setFocusDetails(alert.getFocusDetails());
//        alertEntity.setCustomFields(alert.getCustomFields());
//        alertEntity.setWorkflowInfo(alert.getWorkflowInfo());
//
//        alertEntity.setCreatedDate(alert.getCreatedDate());
    }

}