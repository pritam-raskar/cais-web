package com.dair.cais.alert;

import com.dair.cais.alert.exception.AlertOperationException;
import com.dair.cais.alert.exception.AlertValidationException;
import com.dair.cais.alert.filter.AlertFilterRequest;
import com.dair.cais.alert.filter.FilterCriteria;
import com.dair.cais.audit.AuditLogRequest;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alerts")
@Tag(name = "Alert Management", description = "APIs for managing alerts")
@RequiredArgsConstructor
@Slf4j
public class AlertController {
    private final AlertService alertService;

    @GetMapping("/active")
    @Operation(summary = "Get all active alerts")
    public ResponseEntity<List<Alert>> getAllActiveAlerts() {
        log.debug("Request received to get all active alerts");
        return ResponseEntity.ok(alertService.getAllActiveAlerts());
    }

    @GetMapping("/audit/active")
    @Operation(summary = "Get all active alerts with audit")
    public ResponseEntity<List<Alert>> getAllActiveAlertsWithAudit(
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received to get all active alerts with audit");
        return ResponseEntity.ok(alertService.getAllActiveAlertsWithAudit(auditLogRequest));
    }

    @PostMapping("/alertbyloggedinUser/{userId}")
    @Operation(summary = "Get filtered alerts for logged in user",
            description = "Retrieves alerts from orgFamily for logged in user with additional filtering capabilities")
    public ResponseEntity<List<AlertEntity>> findAlertsByOrgFamilyBYUserOrgUnits(
            @PathVariable String userId,
            @RequestBody AlertFilterRequest request) {

        log.debug("Received request to find alerts for user: {} with filters: {} and audit request: {}",
                userId, request.getFilterCriteria(), request.getAuditLogRequest());

        try {
            List<AlertEntity> alerts = alertService.findAlertsByOrgFamilyByUserOrgKeysWithAudit(
                    userId,
                    request.getFilterCriteria(),
                    request.getAuditLogRequest());

            log.debug("Found {} alerts matching criteria for user: {}", alerts.size(), userId);
            return ResponseEntity.ok().body(alerts);

        } catch (Exception e) {
            log.error("Error processing alert request for user: {}", userId, e);
            throw new AlertOperationException("Failed to retrieve alerts", e);
        }
    }



    @GetMapping("/alertId/{alertId}")
    @Operation(summary = "Get an alert by its alert ID")
    public ResponseEntity<Alert> getAlertById(@PathVariable final String alertId) {
        log.debug("Request received to get alert by ID: {}", alertId);
        return ResponseEntity.ok(alertService.getAlertOnId(alertId));
    }

    @GetMapping("/audit/alertId/{alertId}")
    @Operation(summary = "Get an alert by its alert ID with audit")
    public ResponseEntity<Alert> getAlertByIdWithAudit(
            @PathVariable final String alertId,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received to get alert by ID: {} with audit", alertId);
        return ResponseEntity.ok(alertService.getAlertOnId(alertId, auditLogRequest));
    }

    @GetMapping("/findAlertsByOrgFamily")
    @Operation(summary = "Get alerts by organization family")
    public ResponseEntity<List<AlertEntity>> findAlertsByOrgFamily(
            @RequestParam("org") String searchString) {
        log.debug("Request received to find alerts by org family: {}", searchString);
        return ResponseEntity.ok(alertService.findAlertsByOrgFamily(searchString));
    }

    @GetMapping("/audit/findAlertsByOrgFamily")
    @Operation(summary = "Get alerts by organization family with audit")
    public ResponseEntity<List<AlertEntity>> findAlertsByOrgFamilyWithAudit(
            @RequestParam("org") String searchString,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received to find alerts by org family: {} with audit", searchString);
        return ResponseEntity.ok(alertService.findAlertsByOrgFamilyWithAudit(searchString, auditLogRequest));
    }

    @GetMapping("/findAlertsByOrg")
    @Operation(summary = "Get alerts by organization")
    public ResponseEntity<List<AlertEntity>> findAlertsByOrg(
            @RequestParam("org") String searchString) {
        log.debug("Request received to find alerts by org: {}", searchString);
        return ResponseEntity.ok(alertService.findAlertsByOrg(searchString));
    }

    @GetMapping("/audit/findAlertsByOrg")
    @Operation(summary = "Get alerts by organization with audit")
    public ResponseEntity<List<AlertEntity>> findAlertsByOrgWithAudit(
            @RequestParam("org") String searchString,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received to find alerts by org: {} with audit", searchString);
        return ResponseEntity.ok(alertService.findAlertsByOrgWithAudit(searchString, auditLogRequest));
    }

    @GetMapping("/find")
    @Operation(summary = "Find alerts by multiple criteria")
    public ResponseEntity<List<AlertEntity>> findAlertsByCriteria(
            @RequestParam(required = false) String alertId,
            @RequestParam(required = false) String createDate,
            @RequestParam(required = false) String lastUpdateDate,
            @RequestParam(required = false) String totalScore,
            @RequestParam(required = false) String createdBy,
            @RequestParam(required = false) String businessDate,
            @RequestParam(required = false) String focalEntity,
            @RequestParam(required = false) String focus,
            @RequestParam(required = false) String alertTypeId,
            @RequestParam(required = false) String alertRegion,
            @RequestParam(required = false) String alertGroupId,
            @RequestParam(required = false) Boolean isConsolidated,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(required = false) Boolean hasMultipleScenario,
            @RequestParam(required = false) Boolean isDeleted,
            @RequestParam(required = false) String orgUnitId,
            @RequestParam(required = false) String orgFamily,
            @RequestParam(required = false) String previousOrgUnitId,
            @RequestParam(required = false) Boolean isOrgUnitUpdated,
            @RequestParam(required = false) Boolean isRelatedAlert,
            @RequestParam(required = false) String ownerId,
            @RequestParam(required = false) String ownerName,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String alertStepId,
            @RequestParam(required = false) String alertStepName,
            @RequestParam(required = false) Boolean isCaseCreated) {
        log.debug("Request received to find alerts by criteria");
        return ResponseEntity.ok(alertService.findAlertsByCriteria(
                alertId, createDate, lastUpdateDate, totalScore, createdBy, businessDate,
                focalEntity, focus, alertTypeId, alertRegion, alertGroupId, isConsolidated,
                isActive, hasMultipleScenario, isDeleted, orgUnitId, orgFamily, previousOrgUnitId,
                isOrgUnitUpdated, isRelatedAlert, ownerId, ownerName, status, alertStepId,
                alertStepName, isCaseCreated));
    }

//    @GetMapping("/audit/find")
//    @Operation(summary = "Find alerts by multiple criteria with audit")
//    public ResponseEntity<List<AlertEntity>> findAlertsByCriteriaWithAudit(
//            // Same parameters as above
//            @RequestBody AuditLogRequest auditLogRequest) {
//        log.debug("Request received to find alerts by criteria with audit");
//        return ResponseEntity.ok(alertService.findAlertsByCriteriaWithAudit(
//                alertId, createDate, lastUpdateDate, totalScore, createdBy, businessDate,
//                focalEntity, focus, alertTypeId, alertRegion, alertGroupId, isConsolidated,
//                isActive, hasMultipleScenario, isDeleted, orgUnitId, orgFamily, previousOrgUnitId,
//                isOrgUnitUpdated, isRelatedAlert, ownerId, ownerName, status, alertStepId,
//                alertStepName, isCaseCreated, auditLogRequest));
//    }

    @PatchMapping("/changescore/{alertId}")
    @Operation(summary = "Update alert total score")
    public ResponseEntity<Alert> updateTotalScore(
            @PathVariable String alertId,
            @RequestParam int totalScore) {
        log.debug("Request received to update total score for alert: {}", alertId);
        return ResponseEntity.ok(alertService.updateTotalScore(alertId, totalScore));
    }

    @PatchMapping("/audit/changescore/{alertId}")
    @Operation(summary = "Update alert total score with audit")
    public ResponseEntity<Alert> updateTotalScoreWithAudit(
            @PathVariable String alertId,
            @RequestParam int totalScore,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received to update total score for alert: {} with audit", alertId);
        return ResponseEntity.ok(alertService.updateTotalScoreWithAudit(alertId, totalScore, auditLogRequest));
    }

    @PatchMapping("/changeowner/{alertId}")
    @Operation(summary = "Update alert owner")
    public ResponseEntity<Alert> updateOwnerId(
            @PathVariable String alertId,
            @RequestParam String ownerId) {
        log.debug("Request received to update owner for alert: {}", alertId);
        return ResponseEntity.ok(alertService.updateOwnerId(alertId, ownerId));
    }

    @PatchMapping("/audit/changeowner/{alertId}")
    @Operation(summary = "Update alert owner with audit")
    public ResponseEntity<Alert> updateOwnerIdWithAudit(
            @PathVariable String alertId,
            @RequestParam String ownerId,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received to update owner for alert: {} with audit", alertId);
        return ResponseEntity.ok(alertService.updateOwnerIdWithAudit(alertId, ownerId, auditLogRequest));
    }

    @PatchMapping("/changeorg/{alertId}")
    @Operation(summary = "Update alert organization")
    public ResponseEntity<Alert> updateOrgUnitId(
            @PathVariable String alertId,
            @RequestParam String orgUnitId) {
        log.debug("Request received to update org unit for alert: {}", alertId);
        return ResponseEntity.ok(alertService.updateOrgUnitId(alertId, orgUnitId));
    }

    @PatchMapping("/audit/changeorg/{alertId}")
    @Operation(summary = "Update alert organization with audit")
    public ResponseEntity<Alert> updateOrgUnitIdWithAudit(
            @PathVariable String alertId,
            @RequestParam String orgUnitId,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received to update org unit for alert: {} with audit", alertId);
        return ResponseEntity.ok(alertService.updateOrgUnitIdWithAudit(alertId, orgUnitId, auditLogRequest));
    }



    @PatchMapping("/changestep/{alertId}")
    @Operation(summary = "Change alert step")
    public ResponseEntity<Alert> changeStep(
            @PathVariable String alertId,
            @RequestParam Long stepId) {
        log.debug("Request received to change step for alert: {}", alertId);
        return ResponseEntity.ok(alertService.changeStep(alertId, stepId));
    }

    @PatchMapping(value = "/audit/changestep/{alertId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Change alert step with audit")
    public ResponseEntity<Alert> changeStepWithAudit(
            @PathVariable String alertId,
            @RequestParam Long stepId,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received to change step for alert: {} with audit", alertId);
        return ResponseEntity.ok(alertService.changeStepWithAudit(alertId, stepId, auditLogRequest));
    }

    @GetMapping("")
    @Operation(summary = "Get all alerts with pagination and filtering")
    public ResponseEntity<Map<String, Object>> getAllAlerts(
            @RequestParam(required = false) String name,
            @RequestParam(required = false, name = "state") String state,
            @RequestParam(required = false, name = "accountNumber") List<String> accountNumberList,
            @RequestParam(required = false, name = "owner") List<String> owners,
            @RequestParam(required = false, name = "assignee") List<String> assignees,
            @RequestParam(required = false, name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateFrom,
            @RequestParam(required = false, name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateTo,
            @Valid @RequestParam(defaultValue = "10") int limit,
            @Valid @RequestParam(defaultValue = "0") int offset) {
        log.debug("Request received to get all alerts with pagination and filtering");
        return ResponseEntity.ok(alertService.getAllAlerts(name, state, accountNumberList,
                owners, assignees, createdDateFrom, createdDateTo, limit, offset));
    }

    @GetMapping("/audit")
    @Operation(summary = "Get all alerts with pagination, filtering, and audit")
    public ResponseEntity<Map<String, Object>> getAllAlertsWithAudit(
            @RequestParam(required = false) String name,
            @RequestParam(required = false, name = "state") String state,
            @RequestParam(required = false, name = "accountNumber") List<String> accountNumberList,
            @RequestParam(required = false, name = "owner") List<String> owners,
            @RequestParam(required = false, name = "assignee") List<String> assignees,
            @RequestParam(required = false, name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateFrom,
            @RequestParam(required = false, name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateTo,
            @Valid @RequestParam(defaultValue = "10") int limit,
            @Valid @RequestParam(defaultValue = "0") int offset,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received to get all alerts with pagination, filtering, and audit");
        return ResponseEntity.ok(alertService.getAllAlertsWithAudit(name, state, accountNumberList,
                owners, assignees, createdDateFrom, createdDateTo, limit, offset, auditLogRequest));
    }

    @PatchMapping("/{alertId}")
    @Operation(summary = "Update an alert")
    public ResponseEntity<Alert> patchAlert(
            @PathVariable final String alertId,
            @RequestParam(required = true) String alertType,
            @RequestBody Alert alert) {
        log.debug("Request received to patch alert: {} of type: {}", alertId, alertType);
        return ResponseEntity.ok(alertService.patchAlert(alertId, alertType, alert));
    }

    @PatchMapping("/audit/{alertId}")
    @Operation(summary = "Update an alert with audit")
    public ResponseEntity<Alert> patchAlertWithAudit(
            @PathVariable final String alertId,
            @RequestParam(required = true) String alertType,
            @RequestBody Alert alert,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received to patch alert: {} of type: {} with audit", alertId, alertType);
        return ResponseEntity.ok(alertService.patchAlertWithAudit(alertId, alertType, alert, auditLogRequest));
    }

    @DeleteMapping("/{alertId}")
    @Operation(summary = "Delete an Alert by its id")
    public ResponseEntity<Void> deleteAlertById(
            @PathVariable final String alertId,
            @RequestParam(required = true) String alertType) {
        log.debug("Request received to delete alert: {} of type: {}", alertId, alertType);
        alertService.deleteAlertById(alertId, alertType);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/audit/{alertId}")
    @Operation(summary = "Delete an Alert by its id with audit")
    public ResponseEntity<Void> deleteAlertByIdWithAudit(
            @PathVariable final String alertId,
            @RequestParam(required = true) String alertType,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received to delete alert: {} of type: {} with audit", alertId, alertType);
        alertService.deleteAlertByIdWithAudit(alertId, alertType, auditLogRequest);
        return ResponseEntity.noContent().build();
    }

    @Hidden
    @PostMapping("/bulk")
    @Operation(summary = "Create bulk alerts")
    public ResponseEntity<List<Alert>> createAlerts(@RequestBody List<Alert> alerts) {
        log.debug("Request received to create {} bulk alerts", alerts.size());
        return ResponseEntity.ok(alertService.createAlerts(alerts));
    }

    @Hidden
    @PostMapping("/audit/bulk")
    @Operation(summary = "Create bulk alerts with audit")
    public ResponseEntity<List<Alert>> createAlertsWithAudit(
            @RequestBody List<Alert> alerts,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received to create {} bulk alerts with audit", alerts.size());
        return ResponseEntity.ok(alertService.createAlertsWithAudit(alerts, auditLogRequest));
    }

    @PostMapping("/create")
    @Operation(summary = "Create an alert")
    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert) {
        log.debug("Request received to create new alert");
        return ResponseEntity.ok(alertService.createAlert(alert));
    }

    @PostMapping("/audit/create")
    @Operation(summary = "Create an alert with audit")
    public ResponseEntity<Alert> createAlertWithAudit(
            @RequestBody Alert alert,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received to create new alert with audit");
        return ResponseEntity.ok(alertService.createAlertWithAudit(alert, auditLogRequest));
    }

    @GetMapping("/{alertId}")
    @Operation(summary = "Get an Alert by its mongoDB id")
    public ResponseEntity<Alert> getAlertById(
            @PathVariable final String alertId,
            @RequestParam(required = false) String alertType) {
        log.debug("Request received to get alert by ID: {} and type: {}", alertId, alertType);
        return ResponseEntity.ok(alertService.getAlertById(alertId, alertType));
    }

    @GetMapping("/audit/{alertId}")
    @Operation(summary = "Get an Alert by its mongoDB id with audit")
    public ResponseEntity<Alert> getAlertByIdWithAudit(
            @PathVariable final String alertId,
            @RequestParam(required = false) String alertType,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received to get alert by ID: {} and type: {} with audit", alertId, alertType);
        return ResponseEntity.ok(alertService.getAlertByIdWithAudit(alertId, alertType, auditLogRequest));
    }

    @ExceptionHandler(AlertValidationException.class)
    public ResponseEntity<Map<String, List<String>>> handleAlertValidationException(
            AlertValidationException ex) {
        log.error("Alert validation exception occurred: {}", ex.getMessage());
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", ex.getErrors());
        return ResponseEntity.badRequest().body(errorResponse);
    }
}





//package com.dair.cais.alert;
//
//import com.dair.cais.audit.AuditLogRequest;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.format.annotation.DateTimeFormat;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import javax.validation.Valid;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@Slf4j
//@RequestMapping("/alerts")
//@Tag(name = "alerts-with-audit")
//public class AlertController {
//
//    @Autowired
//    private AlertService alertServiceWithAudit;
//
//    @GetMapping("")
//    @Operation(summary = "Get All alerts which are not deleted and are active")
//    public ResponseEntity<List<Alert>> getAllActiveAlerts(@RequestBody AuditLogRequest auditLogRequest) {
//        List<Alert> activeAlerts = alertServiceWithAudit.getAllActiveAlerts(auditLogRequest);
//        return ResponseEntity.ok().body(activeAlerts);
//    }
//
//    //TODO
//    // we will also need methods to get deleted alerts
//    // we will also need methods to get all inactive alerts
//    // we will aslo need to activate an alert
//    // we will also need a method to inactivate and alert and also in bulk
//    @PostMapping("/insert")
//    @Operation(summary = "Create an alert with alert data payload and alert type, with audit")
//    public ResponseEntity<AlertEntity> insertAlert(
//            @RequestParam String alertType,
//            @RequestBody AlertEntity alertEntity,
//            @RequestBody AuditLogRequest auditLogRequest) {
//        AlertEntity createdAlert = alertServiceWithAudit.insertAlert(alertType, alertEntity, auditLogRequest);
//        return ResponseEntity.ok().body(createdAlert);
//    }
//
//    @GetMapping("/alertId/{alertId}")
//    @Operation(summary = "Get an alertId using the alert field alertId, with audit")
//    public ResponseEntity<Alert> getAlertById(@PathVariable final String alertId,
//                                              @RequestBody AuditLogRequest auditLogRequest) {
//        Alert alertById = alertServiceWithAudit.getAlertOnId(alertId, auditLogRequest);
//        return ResponseEntity.ok().body(alertById);
//    }
//
//
//    @GetMapping("/findAlertsByOrgFamily")
//    @Operation(summary = "Get alerts from orgFamily, with audit")
//    public ResponseEntity<List<AlertEntity>> findAlertsByOrgFamily(@RequestParam("org") String substring,
//                                                                   @RequestBody AuditLogRequest auditLogRequest) {
//        List<AlertEntity> alerts = alertServiceWithAudit.findAlertsByOrgFamily(substring,auditLogRequest);
//        return ResponseEntity.ok().body(alerts);
//    }
//
//    @GetMapping("/alertbyloggedinUser/{userId}")  // Changed to POST since we're using request body
//    @Operation(summary = "Get alerts from orgFamily for logged in user")
//    public ResponseEntity<List<AlertEntity>> findAlertsByOrgFamilyBYUserOrgUnits(
//            @PathVariable String userId,
//            @RequestBody AuditLogRequest auditLogRequest) {
//
//        log.debug("Received request to find alerts for user: {} with audit request: {}", userId, auditLogRequest);
//        List<AlertEntity> alerts = alertServiceWithAudit.findAlertsByOrgFamilyBYUserOrgKeys(userId, auditLogRequest);
//        return ResponseEntity.ok().body(alerts);
//    }
//
//    @GetMapping("/findAlertsByOrg")
//    @Operation(summary = "Get alerts from orgId, with audit")
//    public ResponseEntity<List<AlertEntity>> findAlertsByOrg(@RequestParam("org") String substring,
//                                                             @RequestBody AuditLogRequest auditLogRequest) {
//        List<AlertEntity> alerts = alertServiceWithAudit.findAlertsByOrg(substring,auditLogRequest);
//        return ResponseEntity.ok().body(alerts);
//    }
//
//    @GetMapping("/find")
//    @Operation(summary = "Mandatory/base level field filters to fetch the list of alerts, with audit")
//    public ResponseEntity<List<AlertEntity>> findAlertsByCriteria(
//            @RequestParam(required = false) String alertId,
//            @RequestParam(required = false) String createDate,
//            @RequestParam(required = false) String lastUpdateDate,
//            @RequestParam(required = false) String totalScore,
//            @RequestParam(required = false) String createdBy,
//            @RequestParam(required = false) String businessDate,
//            @RequestParam(required = false) String focalEntity,
//            @RequestParam(required = false) String focus,
//            @RequestParam(required = false) String alertTypeId,
//            @RequestParam(required = false) String alertRegion,
//            @RequestParam(required = false) String alertGroupId,
//            @RequestParam(required = false) Boolean isConsolidated,
//            @RequestParam(required = false) Boolean isActive,
//            @RequestParam(required = false) Boolean hasMultipleScenario,
//            @RequestParam(required = false) Boolean isDeleted,
//            @RequestParam(required = false) String orgUnitId,
//            @RequestParam(required = false) String orgFamily,
//            @RequestParam(required = false) String previousOrgUnitId,
//            @RequestParam(required = false) Boolean isOrgUnitUpdated,
//            @RequestParam(required = false) Boolean isRelatedAlert,
//            @RequestParam(required = false) String ownerId,
//            @RequestParam(required = false) String ownerName,
//            @RequestParam(required = false) String status,
//            @RequestParam(required = false) String alertStepId,
//            @RequestParam(required = false) String alertStepName,
//            @RequestParam(required = false) Boolean isCaseCreated,
//            @RequestBody AuditLogRequest auditLogRequest
//    ) {
//        List<AlertEntity> alerts = alertServiceWithAudit.findAlertsByCriteria(
//                alertId, createDate, lastUpdateDate, totalScore, createdBy, businessDate,
//                focalEntity, focus, alertTypeId, alertRegion, alertGroupId, isConsolidated,
//                isActive, hasMultipleScenario, isDeleted, orgUnitId, orgFamily, previousOrgUnitId,
//                isOrgUnitUpdated, isRelatedAlert, ownerId, ownerName, status, alertStepId, alertStepName, isCaseCreated,
//                auditLogRequest
//        );
//        return ResponseEntity.ok().body(alerts);
//    }
//
//    @PatchMapping("/changescore/{alertId}")
//    public ResponseEntity<Alert> updateTotalScore(@PathVariable String alertId,
//                                                  @RequestParam int totalScore,
//                                                  @RequestBody AuditLogRequest auditLogRequest) {
//        Alert updatedAlert = alertServiceWithAudit.updateTotalScore(alertId, totalScore, auditLogRequest);
//        return ResponseEntity.ok().body(updatedAlert);
//    }
//
//    @PatchMapping("/changeowner/{alertId}")
//    public ResponseEntity<Alert> updateOwnerId(@PathVariable String alertId,
//                                               @RequestParam String userId,
//                                               @RequestBody AuditLogRequest auditLogRequest) {
//        Alert updatedAlert = alertServiceWithAudit.updateOwnerId(alertId, userId, auditLogRequest);
//        return ResponseEntity.ok().body(updatedAlert);
//    }
//
//    @PatchMapping("/changeorg/{alertId}")
//    public ResponseEntity<Alert> updateOrgUnitId(@PathVariable String alertId,
//                                                 @RequestParam String orgUnitId,
//                                                 @RequestBody AuditLogRequest auditLogRequest) {
//        Alert updatedAlert = alertServiceWithAudit.updateOrgUnitId(alertId, orgUnitId, auditLogRequest);
//        return ResponseEntity.ok().body(updatedAlert);
//    }
//
//    @PatchMapping("/changestatus/{alertId}")
//    public ResponseEntity<Alert> updateStatus(@PathVariable String alertId,
//                                              @RequestParam String statusId,
//                                              @RequestBody AuditLogRequest auditLogRequest) {
//        Alert updatedAlert = alertServiceWithAudit.updateStatus(alertId, statusId, auditLogRequest);
//        return ResponseEntity.ok().body(updatedAlert);
//    }
//
//    @PatchMapping("/changestep/{alertId}")
//    public ResponseEntity<Alert> changeStep(@PathVariable String alertId,
//                                            @RequestParam Long stepId,
//                                            @RequestBody AuditLogRequest auditLogRequest) {
//        Alert updatedAlert = alertServiceWithAudit.changeStep(alertId, stepId, auditLogRequest);
//        return ResponseEntity.ok(updatedAlert);
//    }
//
//    @GetMapping("/search")
//    @Operation(summary = "Get all alerts with search options and audit")
//    public ResponseEntity<Map<String, Object>> getAllAlerts(
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false, name = "state") String state,
//            @RequestParam(required = false, name = "accountNumber") List<String> accountNumberList,
//            @RequestParam(required = false, name = "owner") List<String> owners,
//            @RequestParam(required = false, name = "assignee") List<String> assignees,
//            @RequestParam(required = false, name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateFrom,
//            @RequestParam(required = false, name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateTo,
//            @Valid @RequestParam(defaultValue = "10") int limit,
//            @Valid @RequestParam(defaultValue = "0") int offset,
//            @RequestBody AuditLogRequest auditLogRequest) {
//        Map<String, Object> allAlerts = alertServiceWithAudit.getAllAlerts(name, state, accountNumberList, owners, assignees,
//                createdDateFrom, createdDateTo, limit, offset, auditLogRequest);
//        return ResponseEntity.ok().body(allAlerts);
//    }
//
//    @PatchMapping("/{alertId}")
//    @Operation(summary = "Update an alert with audit")
//    public ResponseEntity<Alert> patchAlert(@PathVariable final String alertId,
//                                            @RequestParam(required = true) String alertType,
//                                            @RequestBody Alert alert,
//                                            @RequestBody AuditLogRequest auditLogRequest) {
//        Alert updatedAlert = alertServiceWithAudit.patchAlert(alertId, alertType, alert, auditLogRequest);
//        return ResponseEntity.ok().body(updatedAlert);
//    }
//
//    @DeleteMapping("/{alertId}")
//    @Operation(summary = "Delete an Alert by its id with audit")
//    public ResponseEntity<Void> deleteAlertById(@PathVariable final String alertId,
//                                                @RequestParam(required = true) String alertType,
//                                                @RequestBody AuditLogRequest auditLogRequest) {
//        alertServiceWithAudit.deleteAlertById(alertId, alertType, auditLogRequest);
//        return ResponseEntity.ok().build();
//    }
//
//
//    @PostMapping("/create")
//    @Operation(summary = "Create an alert with audit")
//    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert,
//                                             @RequestBody AuditLogRequest auditLogRequest) {
//        Alert createdAlert = alertServiceWithAudit.createAlert(alert, auditLogRequest);
//        return ResponseEntity.ok().body(createdAlert);
//    }
//
//    @ExceptionHandler(AlertValidationException.class)
//    public ResponseEntity<Map<String, List<String>>> handleAlertValidationException(AlertValidationException ex) {
//        Map<String, List<String>> errorResponse = new HashMap<>();
//        errorResponse.put("errors", ex.getErrors());
//        return ResponseEntity.badRequest().body(errorResponse);
//    }
//
//
//}





//package com.dair.cais.alert;
//
//        import io.swagger.v3.oas.annotations.Hidden;
//        import io.swagger.v3.oas.annotations.Operation;
//        import io.swagger.v3.oas.annotations.tags.Tag;
//        import org.springframework.beans.factory.annotation.Autowired;
//        import org.springframework.format.annotation.DateTimeFormat;
//        import org.springframework.http.ResponseEntity;
//        import org.springframework.web.bind.annotation.*;
//
//        import javax.validation.Valid;
//        import java.util.Date;
//        import java.util.HashMap;
//        import java.util.List;
//        import java.util.Map;
//
//@RestController
//@RequestMapping("/alerts")
//@Tag(name = "alerts")
//
//public class AlertController {
//    @Autowired
//    private AlertService alertService;
//
//    @PostMapping("/insert")
//    @Operation(summary = "Create an alert with alert data payload and alert type")
//    public ResponseEntity<AlertEntity> insertAlert(
//            @RequestParam String alertType,
//            @RequestBody AlertEntity alertEntity) {
//        AlertEntity createdAlert = alertService.insertAlert(alertType, alertEntity);
//        return ResponseEntity.ok().body(createdAlert);
//    }
//
//    @GetMapping("/alertId/{alertId}")
//    @Operation(summary = "Get an alertId using the alert field alertId")
//    public ResponseEntity<Alert> getAlertById(@PathVariable final String alertId) {
//        Alert alertById = alertService.getAlertOnId(alertId);
//        return ResponseEntity.ok().body(alertById);
//    }
//
//
//    @GetMapping("/findAlertsByOrgFamily")
//    @Operation(summary = "Get alerts from orgFamily")
//    public List<AlertEntity> findAlertsByOrgFamily(@RequestParam("org") String substring) {
//        return alertService.findAlertsByOrgFamily(substring);
//    }
//
//    @GetMapping("/findAlertsByOrg")
//    @Operation(summary = "Get alerts from orgId")
//    public List<AlertEntity> findAlertsByOrg(@RequestParam("org") String substring) {
//        return alertService.findAlertsByOrg(substring);
//    }
//
//    @GetMapping("/find")
//    @Operation(summary = "Mandatory/base level field filters to fetch the list of alerts")
//    public List<AlertEntity> findAlertsByCriteria(
//            @RequestParam(required = false) String alertId,
//            @RequestParam(required = false) String createDate,
//            @RequestParam(required = false) String lastUpdateDate,
//            @RequestParam(required = false) String totalScore,
//            @RequestParam(required = false) String createdBy,
//            @RequestParam(required = false) String businessDate,
//            @RequestParam(required = false) String focalEntity,
//            @RequestParam(required = false) String focus,
//            @RequestParam(required = false) String alertTypeId,
//            @RequestParam(required = false) String alertRegion,
//            @RequestParam(required = false) String alertGroupId,
//            @RequestParam(required = false) Boolean isConsolidated,
//            @RequestParam(required = false) Boolean isActive,
//            @RequestParam(required = false) Boolean hasMultipleScenario,
//            @RequestParam(required = false) Boolean isDeleted,
//            @RequestParam(required = false) String orgUnitId,
//            @RequestParam(required = false) String orgFamily,
//            @RequestParam(required = false) String previousOrgUnitId,
//            @RequestParam(required = false) Boolean isOrgUnitUpdated,
//            @RequestParam(required = false) Boolean isRelatedAlert,
//            @RequestParam(required = false) String ownerId,
//            @RequestParam(required = false) String ownerName,
//            @RequestParam(required = false) String status,
//            @RequestParam(required = false) String alertStepId,
//            @RequestParam(required = false) String alertStepName,
//            @RequestParam(required = false) Boolean isCaseCreated
//    ) {
//        return alertService.findAlertsByCriteria(
//                alertId, createDate, lastUpdateDate, totalScore, createdBy, businessDate,
//                focalEntity, focus, alertTypeId, alertRegion, alertGroupId, isConsolidated,
//                isActive, hasMultipleScenario, isDeleted, orgUnitId, orgFamily, previousOrgUnitId,
//                isOrgUnitUpdated, isRelatedAlert, ownerId, ownerName, status, alertStepId, alertStepName, isCaseCreated
//        );
//    }
//
//    @PatchMapping("/changescore")
//    public ResponseEntity<Alert> updateTotalScore(@PathVariable String alertId, @RequestParam int totalScore) {
//        Alert updatedAlert = alertService.updateTotalScore(alertId, totalScore);
//        return ResponseEntity.ok().body(updatedAlert);
//    }
//
//
//    @PatchMapping("/changeowner")
//    public ResponseEntity<Alert> updateOwnerId(@PathVariable String alertId, @RequestParam String ownerId) {
//        Alert updatedAlert = alertService.updateOwnerId(alertId, ownerId);
//        return ResponseEntity.ok().body(updatedAlert);
//    }
//
//    @PatchMapping("/changeorg")
//    public ResponseEntity<Alert> updateOrgUnitId(@PathVariable String alertId, @RequestParam String orgUnitId) {
//        Alert updatedAlert = alertService.updateOrgUnitId(alertId, orgUnitId);
//        return ResponseEntity.ok().body(updatedAlert);
//    }
//
//    @PatchMapping("/changestatus")
//    public ResponseEntity<Alert> updateStatus(@PathVariable String alertId, @RequestParam String statusId) {
//        Alert updatedAlert = alertService.updateStatus(alertId, statusId);
//        return ResponseEntity.ok().body(updatedAlert);
//    }
//
//    @PatchMapping("/changestep/{alertId}")
//    public ResponseEntity<Alert> changeStep(@PathVariable String alertId, @RequestParam Long stepId) {
//        Alert updatedAlert = alertService.changeStep(alertId, stepId);
//        return ResponseEntity.ok(updatedAlert);
//    }
//
//
//    @GetMapping("")
//    @Operation(summary = "Get all alerts; Use query params for search options like offset ,limit ,fuzzy search")
//    public ResponseEntity<Map<String, Object>> getAllAlerts(
//            @RequestParam(required = false) String name,
//            @RequestParam(required = false, name = "state") String state,
//            @RequestParam(required = false, name = "accountNumber") List<String> accountNumberList,
//            @RequestParam(required = false, name = "owner") List<String> owners,
//            @RequestParam(required = false, name = "assignee") List<String> assignees,
//            @RequestParam(required = false, name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateFrom,
//            @RequestParam(required = false, name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateTo,
//            @Valid @RequestParam(defaultValue = "10") int limit,
//            @Valid @RequestParam(defaultValue = "0") int offset) {
//
//        Map<String, Object> allAlerts = alertService.getAllAlerts(name, state, accountNumberList, owners, assignees,
//                createdDateFrom, createdDateTo, limit, offset);
//
//        return ResponseEntity.ok().body(allAlerts);
//    }
//
//    @PatchMapping("{alertId}")
//    @Operation(summary = "Update an alert")
//    public ResponseEntity<Alert> patchAlert(@PathVariable final String alertId,
//                                            @RequestParam(required = true) String alertType, @RequestBody Alert alert) {
//        Alert updatedAlert = alertService.patchAlert(alertId, alertType, alert);
//        return ResponseEntity.ok().body(updatedAlert);
//    }
//
//    @DeleteMapping("{alertId}")
//    @Operation(summary = "Delete an Alert by its id")
//    public ResponseEntity<Alert> deleteAlertById(@PathVariable final String alertId,
//                                                 @RequestParam(required = true) String alertType) {
//        alertService.deleteAlertById(alertId, alertType);
//        return ResponseEntity.ok().build();
//    }
//
//    @Hidden
//    @PostMapping("/bulk")
//    @Operation(summary = "Create bulk alerts")
//    public ResponseEntity<List<Alert>> createAlerts(@RequestBody List<Alert> alerts) {
//        List<Alert> createdAlerts = alertService.createAlerts(alerts);
//        return ResponseEntity.ok().body(createdAlerts);
//    }
//
//
//
////   @PostMapping("/create")
////   @Operation(summary = "Create an alert")
////   public ResponseEntity<Alert> createAlert(@RequestBody Alert alert) {
////      Alert createdAlert = alertService.createAlert(alert);
////      return ResponseEntity.ok().body(createdAlert);
////   }
//
//    @PostMapping("/create")
//    @Operation(summary = "Create an alert")
//    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert) {
//        Alert createdAlert = alertService.createAlert(alert);
//        return ResponseEntity.ok().body(createdAlert);
//    }
//
//    @ExceptionHandler(AlertValidationException.class)
//    public ResponseEntity<Map<String, List<String>>> handleAlertValidationException(AlertValidationException ex) {
//        Map<String, List<String>> errorResponse = new HashMap<>();
//        errorResponse.put("errors", ex.getErrors());
//        return ResponseEntity.badRequest().body(errorResponse);
//    }
//
//
//    @GetMapping("{alertId}")
//    @Operation(summary = "Get an Alert by its mongoDB id")
//    public ResponseEntity<Alert> getAlertById(@PathVariable final String alertId,
//                                              @RequestParam(required = false) String alertType) {
//        Alert alertById = alertService.getAlertById(alertId, alertType);
//        return ResponseEntity.ok().body(alertById);
//    }
//
//
//
//}