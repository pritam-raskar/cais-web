package com.dair.cais.alert.rdbms;

import com.dair.cais.alert.Alert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
@Slf4j
public class RdbmsAlertMapper {

    private LocalDateTime parseLocalDateTime(String timestampString) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return LocalDateTime.parse(timestampString, formatter);

    }
    public RdbmsAlertEntity toRdbmsEntity(Alert alert) {
        RdbmsAlertEntity entity = new RdbmsAlertEntity();
        entity.setAlertId(alert.getAlertId());
        LocalDateTime createDate = LocalDateTime.parse(alert.getCreateDate());
        LocalDateTime lastUpdateDate = LocalDateTime.parse(alert.getLastUpdateDate());

        entity.setCreateDate(createDate);
        entity.setLastUpdateDate(lastUpdateDate);
//        entity.setCreateDate(LocalDateTime.parse(alert.getCreateDate()));
//        entity.setLastUpdateDate(LocalDateTime.parse(alert.getLastUpdateDate()));
        entity.setTotalScore(alert.getTotalScore().doubleValue());
        LocalDateTime dateTime = parseLocalDateTime(alert.getBusinessDate());
        entity.setBusinessDate(dateTime);
//        entity.setBusinessDate(LocalDateTime.parse(alert.getBusinessDate()));
        entity.setFocalEntity(alert.getFocalEntity());
        entity.setFocus(alert.getFocus());
        entity.setAlertTypeId(alert.getAlertTypeId());
        entity.setAlertRegion(alert.getAlertRegion());
        entity.setAlertGroupId(alert.getAlertGroupId());
        entity.setIsConsolidated(alert.getIsConsolidated());
        entity.setIsActive(alert.getIsActive());
        entity.setHasMultipleScenario(alert.getHasMultipleScenario());
        entity.setIsDeleted(alert.getIsDeleted());
        entity.setOrgUnitId(alert.getOrgUnitId());
        entity.setOrgFamily(alert.getOrgFamily());
        entity.setPreviousOrgUnitId(alert.getPreviousOrgUnitId());
        entity.setIsOrgUnitUpdated(alert.getIsOrgUnitUpdated());
        entity.setIsRelatedAlert(alert.getIsRelatedAlert());
        entity.setOwnerId(alert.getOwnerId());
        entity.setOwnerName(alert.getOwnerName());
        entity.setStatus(alert.getStatus());
        entity.setAlertStepId(alert.getAlertStepId());
        entity.setAlertStepName(alert.getAlertStepName());
        entity.setIsCaseCreated(alert.getIsCaseCreated());
        entity.setDetails(alert.getDetails());
        entity.setPivot(alert.getPivot());
        entity.setAccountId(alert.getAccountId());
        entity.setTransactionId(alert.getTransactionId());
        entity.setCustomerId(alert.getCustomerId());
        entity.setHouseholdId(alert.getHouseHoldId());
        entity.setPriority(alert.getPriority());
        entity.setTag(alert.getTag());
        entity.setBranchId(alert.getBranchId());
        entity.setRepresentativeId(alert.getRepresentativeId());
        entity.setBuildingBlock(alert.getBuildingBlock());
        entity.setWatchListId(alert.getWatchListId());
        entity.setCreatedBy(alert.getCreatedBy());
        entity.setReasonDetails(alert.getReasonDetails().toString());

        return entity;
    }
}