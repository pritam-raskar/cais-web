package com.dair.cais.alert.rdbms;

import com.dair.cais.alert.Alert;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Component
@Slf4j
public class RdbmsAlertMapper {

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null) {
            return null;
        }

        try {
            // First try parsing with T format (2024-10-31T23:54:41)
            return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        } catch (DateTimeParseException e) {
            try {
                // If that fails, try the space format (yyyy-MM-dd HH:mm:ss)
                return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } catch (DateTimeParseException e2) {
                log.error("Failed to parse date: {} with both formats", dateStr);
                throw new RuntimeException("Unable to parse date: " + dateStr, e2);
            }
        }
    }

    public RdbmsAlertEntity toRdbmsEntity(Alert alert) {
        RdbmsAlertEntity entity = new RdbmsAlertEntity();

        try {
            entity.setAlertId(alert.getAlertId());

            // Handle dates with the new parser
            entity.setCreateDate(parseDateTime(alert.getCreateDate()));
            entity.setLastUpdateDate(parseDateTime(alert.getLastUpdateDate()));
            entity.setBusinessDate(parseDateTime(alert.getBusinessDate()));

            // New date fields
            if (alert.getLastStepUpdatedDate() != null) {
                entity.setLastStepUpdatedDate(parseDateTime(alert.getLastStepUpdatedDate()));
            }
            if (alert.getDeadLine() != null) {
                entity.setDeadLine(parseDateTime(alert.getDeadLine()));
            }

            // Base fields
            entity.setTotalScore(alert.getTotalScore().doubleValue());
            entity.setCreatedBy(alert.getCreatedBy());
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
            entity.setReasonDetails(alert.getReasonDetails().toString());

            // Additional fields
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

            // New fields
            entity.setRepresentativeName(alert.getRepresentativeName());
            entity.setDueIn(alert.getDueIn());
            entity.setOrgKey(alert.getOrgKey());
            entity.setBranchName(alert.getBranchName());
            entity.setCustomerName(alert.getCustomerName());
            entity.setAccountName(alert.getAccountName());

            // Audit fields
            if (alert.getCreatedAt() != null) {
                entity.setCreatedAt(alert.getCreatedAt());
            }
            if (alert.getUpdatedAt() != null) {
                entity.setUpdatedAt(alert.getUpdatedAt());
            }

        } catch (Exception e) {
            log.error("Error mapping Alert to RdbmsAlertEntity: {}", e.getMessage());
            throw new RuntimeException("Failed to map Alert to RdbmsAlertEntity", e);
        }

        return entity;
    }
}



//package com.dair.cais.alert.rdbms;
//
//import com.dair.cais.alert.Alert;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.stereotype.Component;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.time.format.DateTimeParseException;
//
//@Component
//@Slf4j
//public class RdbmsAlertMapper {
//
//    private LocalDateTime parseDateTime(String dateStr) {
//        if (dateStr == null) {
//            return null;
//        }
//
//        try {
//            // First try parsing with T format (2024-10-31T23:54:41)
//            return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
//        } catch (DateTimeParseException e) {
//            try {
//                // If that fails, try the space format (yyyy-MM-dd HH:mm:ss)
//                return LocalDateTime.parse(dateStr, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
//            } catch (DateTimeParseException e2) {
//                log.error("Failed to parse date: {} with both formats", dateStr);
//                throw new RuntimeException("Unable to parse date: " + dateStr, e2);
//            }
//        }
//    }
//
//    public RdbmsAlertEntity toRdbmsEntity(Alert alert) {
//        RdbmsAlertEntity entity = new RdbmsAlertEntity();
//
//        try {
//            entity.setAlertId(alert.getAlertId());
//
//            // Handle dates with the new parser
//            entity.setCreateDate(parseDateTime(alert.getCreateDate()));
//            entity.setLastUpdateDate(parseDateTime(alert.getLastUpdateDate()));
//            entity.setBusinessDate(parseDateTime(alert.getBusinessDate()));
//
//            // Rest of the mappings remain unchanged
//            entity.setTotalScore(alert.getTotalScore().doubleValue());
//            entity.setFocalEntity(alert.getFocalEntity());
//            entity.setFocus(alert.getFocus());
//            entity.setAlertTypeId(alert.getAlertTypeId());
//            entity.setAlertRegion(alert.getAlertRegion());
//            entity.setAlertGroupId(alert.getAlertGroupId());
//            entity.setIsConsolidated(alert.getIsConsolidated());
//            entity.setIsActive(alert.getIsActive());
//            entity.setHasMultipleScenario(alert.getHasMultipleScenario());
//            entity.setIsDeleted(alert.getIsDeleted());
//            entity.setOrgUnitId(alert.getOrgUnitId());
//            entity.setOrgFamily(alert.getOrgFamily());
//            entity.setPreviousOrgUnitId(alert.getPreviousOrgUnitId());
//            entity.setIsOrgUnitUpdated(alert.getIsOrgUnitUpdated());
//            entity.setIsRelatedAlert(alert.getIsRelatedAlert());
//            entity.setOwnerId(alert.getOwnerId());
//            entity.setOwnerName(alert.getOwnerName());
//            entity.setStatus(alert.getStatus());
//            entity.setAlertStepId(alert.getAlertStepId());
//            entity.setAlertStepName(alert.getAlertStepName());
//            entity.setIsCaseCreated(alert.getIsCaseCreated());
//            entity.setDetails(alert.getDetails());
//            entity.setPivot(alert.getPivot());
//            entity.setAccountId(alert.getAccountId());
//            entity.setTransactionId(alert.getTransactionId());
//            entity.setCustomerId(alert.getCustomerId());
//            entity.setHouseholdId(alert.getHouseHoldId());
//            entity.setPriority(alert.getPriority());
//            entity.setTag(alert.getTag());
//            entity.setBranchId(alert.getBranchId());
//            entity.setRepresentativeId(alert.getRepresentativeId());
//            entity.setBuildingBlock(alert.getBuildingBlock());
//            entity.setWatchListId(alert.getWatchListId());
//            entity.setCreatedBy(alert.getCreatedBy());
//            entity.setReasonDetails(alert.getReasonDetails().toString());
//
//            // Set audit fields if they exist
//            if (alert.getCreatedAt() != null) {
//                entity.setCreatedAt(alert.getCreatedAt());
//            }
//            if (alert.getUpdatedAt() != null) {
//                entity.setUpdatedAt(alert.getUpdatedAt());
//            }
//
//        } catch (Exception e) {
//            log.error("Error mapping Alert to RdbmsAlertEntity: {}", e.getMessage());
//            throw new RuntimeException("Failed to map Alert to RdbmsAlertEntity", e);
//        }
//
//        return entity;
//    }
//}
//
//
