package com.dair.cais.alert;

import com.dair.cais.audit.AuditLogRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/alerts")
@Tag(name = "alerts-with-audit")
public class AlertController {

    @Autowired
    private AlertService alertServiceWithAudit;

    @GetMapping("")
    @Operation(summary = "Get All alerts which are not deleted and are active")
    public ResponseEntity<List<Alert>> getAllActiveAlerts(@RequestBody AuditLogRequest auditLogRequest) {
        List<Alert> activeAlerts = alertServiceWithAudit.getAllActiveAlerts(auditLogRequest);
        return ResponseEntity.ok().body(activeAlerts);
    }

    //TODO
    // we will also need methods to get deleted alerts
    // we will also need methods to get all inactive alerts
    // we will aslo need to activate an alert
    // we will also need a method to inactivate and alert and also in bulk
    @PostMapping("/insert")
    @Operation(summary = "Create an alert with alert data payload and alert type, with audit")
    public ResponseEntity<AlertEntity> insertAlert(
            @RequestParam String alertType,
            @RequestBody AlertEntity alertEntity,
            @RequestBody AuditLogRequest auditLogRequest) {
        AlertEntity createdAlert = alertServiceWithAudit.insertAlert(alertType, alertEntity, auditLogRequest);
        return ResponseEntity.ok().body(createdAlert);
    }

    @GetMapping("/alertId/{alertId}")
    @Operation(summary = "Get an alertId using the alert field alertId, with audit")
    public ResponseEntity<Alert> getAlertById(@PathVariable final String alertId,
                                              @RequestBody AuditLogRequest auditLogRequest) {
        Alert alertById = alertServiceWithAudit.getAlertOnId(alertId, auditLogRequest);
        return ResponseEntity.ok().body(alertById);
    }


    @GetMapping("/findAlertsByOrgFamily")
    @Operation(summary = "Get alerts from orgFamily, with audit")
    public ResponseEntity<List<AlertEntity>> findAlertsByOrgFamily(@RequestParam("org") String substring,
                                                                   @RequestBody AuditLogRequest auditLogRequest) {
        List<AlertEntity> alerts = alertServiceWithAudit.findAlertsByOrgFamily(substring,auditLogRequest);
        return ResponseEntity.ok().body(alerts);
    }

    @GetMapping("/alertbyloggedinUser/{userId}")
    @Operation(summary = "Get alerts from orgFamily, with audit")
    public ResponseEntity<List<AlertEntity>> findAlertsByOrgFamilyBYUserOrgUnits(@PathVariable String userId,
                                                                                 @RequestBody AuditLogRequest auditLogRequest) {
        List<AlertEntity> alerts = alertServiceWithAudit.findAlertsByOrgFamilyBYUserOrgKeys(userId,auditLogRequest);
        return ResponseEntity.ok().body(alerts);
    }

    @GetMapping("/findAlertsByOrg")
    @Operation(summary = "Get alerts from orgId, with audit")
    public ResponseEntity<List<AlertEntity>> findAlertsByOrg(@RequestParam("org") String substring,
                                                             @RequestBody AuditLogRequest auditLogRequest) {
        List<AlertEntity> alerts = alertServiceWithAudit.findAlertsByOrg(substring,auditLogRequest);
        return ResponseEntity.ok().body(alerts);
    }

    @GetMapping("/find")
    @Operation(summary = "Mandatory/base level field filters to fetch the list of alerts, with audit")
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
            @RequestParam(required = false) Boolean isCaseCreated,
            @RequestBody AuditLogRequest auditLogRequest
    ) {
        List<AlertEntity> alerts = alertServiceWithAudit.findAlertsByCriteria(
                alertId, createDate, lastUpdateDate, totalScore, createdBy, businessDate,
                focalEntity, focus, alertTypeId, alertRegion, alertGroupId, isConsolidated,
                isActive, hasMultipleScenario, isDeleted, orgUnitId, orgFamily, previousOrgUnitId,
                isOrgUnitUpdated, isRelatedAlert, ownerId, ownerName, status, alertStepId, alertStepName, isCaseCreated,
                auditLogRequest
        );
        return ResponseEntity.ok().body(alerts);
    }

    @PatchMapping("/changescore/{alertId}")
    public ResponseEntity<Alert> updateTotalScore(@PathVariable String alertId,
                                                  @RequestParam int totalScore,
                                                  @RequestBody AuditLogRequest auditLogRequest) {
        Alert updatedAlert = alertServiceWithAudit.updateTotalScore(alertId, totalScore, auditLogRequest);
        return ResponseEntity.ok().body(updatedAlert);
    }

    @PatchMapping("/changeowner/{alertId}")
    public ResponseEntity<Alert> updateOwnerId(@PathVariable String alertId,
                                               @RequestParam String userId,
                                               @RequestBody AuditLogRequest auditLogRequest) {
        Alert updatedAlert = alertServiceWithAudit.updateOwnerId(alertId, userId, auditLogRequest);
        return ResponseEntity.ok().body(updatedAlert);
    }

    @PatchMapping("/changeorg/{alertId}")
    public ResponseEntity<Alert> updateOrgUnitId(@PathVariable String alertId,
                                                 @RequestParam String orgUnitId,
                                                 @RequestBody AuditLogRequest auditLogRequest) {
        Alert updatedAlert = alertServiceWithAudit.updateOrgUnitId(alertId, orgUnitId, auditLogRequest);
        return ResponseEntity.ok().body(updatedAlert);
    }

    @PatchMapping("/changestatus/{alertId}")
    public ResponseEntity<Alert> updateStatus(@PathVariable String alertId,
                                              @RequestParam String statusId,
                                              @RequestBody AuditLogRequest auditLogRequest) {
        Alert updatedAlert = alertServiceWithAudit.updateStatus(alertId, statusId, auditLogRequest);
        return ResponseEntity.ok().body(updatedAlert);
    }

    @PatchMapping("/changestep/{alertId}")
    public ResponseEntity<Alert> changeStep(@PathVariable String alertId,
                                            @RequestParam Long stepId,
                                            @RequestBody AuditLogRequest auditLogRequest) {
        Alert updatedAlert = alertServiceWithAudit.changeStep(alertId, stepId, auditLogRequest);
        return ResponseEntity.ok(updatedAlert);
    }

    @GetMapping("/search")
    @Operation(summary = "Get all alerts with search options and audit")
    public ResponseEntity<Map<String, Object>> getAllAlerts(
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
        Map<String, Object> allAlerts = alertServiceWithAudit.getAllAlerts(name, state, accountNumberList, owners, assignees,
                createdDateFrom, createdDateTo, limit, offset, auditLogRequest);
        return ResponseEntity.ok().body(allAlerts);
    }

    @PatchMapping("/{alertId}")
    @Operation(summary = "Update an alert with audit")
    public ResponseEntity<Alert> patchAlert(@PathVariable final String alertId,
                                            @RequestParam(required = true) String alertType,
                                            @RequestBody Alert alert,
                                            @RequestBody AuditLogRequest auditLogRequest) {
        Alert updatedAlert = alertServiceWithAudit.patchAlert(alertId, alertType, alert, auditLogRequest);
        return ResponseEntity.ok().body(updatedAlert);
    }

    @DeleteMapping("/{alertId}")
    @Operation(summary = "Delete an Alert by its id with audit")
    public ResponseEntity<Void> deleteAlertById(@PathVariable final String alertId,
                                                @RequestParam(required = true) String alertType,
                                                @RequestBody AuditLogRequest auditLogRequest) {
        alertServiceWithAudit.deleteAlertById(alertId, alertType, auditLogRequest);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/create")
    @Operation(summary = "Create an alert with audit")
    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert,
                                             @RequestBody AuditLogRequest auditLogRequest) {
        Alert createdAlert = alertServiceWithAudit.createAlert(alert, auditLogRequest);
        return ResponseEntity.ok().body(createdAlert);
    }

    @ExceptionHandler(AlertValidationException.class)
    public ResponseEntity<Map<String, List<String>>> handleAlertValidationException(AlertValidationException ex) {
        Map<String, List<String>> errorResponse = new HashMap<>();
        errorResponse.put("errors", ex.getErrors());
        return ResponseEntity.badRequest().body(errorResponse);
    }


}


