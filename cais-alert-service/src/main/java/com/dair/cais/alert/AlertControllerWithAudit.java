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
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alerts")
@Tag(name = "alerts-with-audit")
public class AlertControllerWithAudit {

    @Autowired
    private AlertServiceWithAudit alertServiceWithAudit;

    @PostMapping("/insert")
    @Operation(summary = "Create an alert with alert data payload and alert type, with audit")
    public ResponseEntity<AlertEntity> insertAlert(
            @RequestParam String alertType,
            @RequestBody AlertEntity alertEntity,
            @RequestHeader AuditLogRequest auditLogRequest) {
        AlertEntity createdAlert = alertServiceWithAudit.insertAlert(alertType, alertEntity, auditLogRequest);
        return ResponseEntity.ok().body(createdAlert);
    }

    @GetMapping("/alertId/{alertId}")
    @Operation(summary = "Get an alertId using the alert field alertId, with audit")
    public ResponseEntity<Alert> getAlertById(@PathVariable final String alertId,
                                              @RequestHeader AuditLogRequest auditLogRequest) {
        Alert alertById = alertServiceWithAudit.getAlertOnId(alertId, auditLogRequest);
        return ResponseEntity.ok().body(alertById);
    }

    @PatchMapping("/changescore/{alertId}")
    public ResponseEntity<Alert> updateTotalScore(@PathVariable String alertId, 
                                                  @RequestParam int totalScore,
                                                  @RequestHeader AuditLogRequest auditLogRequest) {
        Alert updatedAlert = alertServiceWithAudit.updateTotalScore(alertId, totalScore, auditLogRequest);
        return ResponseEntity.ok().body(updatedAlert);
    }

    @PatchMapping("/changeowner/{alertId}")
    public ResponseEntity<Alert> updateOwnerId(@PathVariable String alertId, 
                                               @RequestParam String ownerId,
                                               @RequestHeader AuditLogRequest auditLogRequest) {
        Alert updatedAlert = alertServiceWithAudit.updateOwnerId(alertId, ownerId, auditLogRequest);
        return ResponseEntity.ok().body(updatedAlert);
    }

    @PatchMapping("/changeorg/{alertId}")
    public ResponseEntity<Alert> updateOrgUnitId(@PathVariable String alertId, 
                                                 @RequestParam String orgUnitId,
                                                 @RequestHeader AuditLogRequest auditLogRequest) {
        Alert updatedAlert = alertServiceWithAudit.updateOrgUnitId(alertId, orgUnitId, auditLogRequest);
        return ResponseEntity.ok().body(updatedAlert);
    }

    @PatchMapping("/changestatus/{alertId}")
    public ResponseEntity<Alert> updateStatus(@PathVariable String alertId, 
                                              @RequestParam String statusId,
                                              @RequestHeader AuditLogRequest auditLogRequest) {
        Alert updatedAlert = alertServiceWithAudit.updateStatus(alertId, statusId, auditLogRequest);
        return ResponseEntity.ok().body(updatedAlert);
    }

    @PatchMapping("/changestep/{alertId}")
    public ResponseEntity<Alert> changeStep(@PathVariable String alertId, 
                                            @RequestParam Long stepId,
                                            @RequestHeader AuditLogRequest auditLogRequest) {
        Alert updatedAlert = alertServiceWithAudit.changeStep(alertId, stepId, auditLogRequest);
        return ResponseEntity.ok(updatedAlert);
    }

    @GetMapping("")
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
            @RequestHeader AuditLogRequest auditLogRequest) {
        Map<String, Object> allAlerts = alertServiceWithAudit.getAllAlerts(name, state, accountNumberList, owners, assignees,
                createdDateFrom, createdDateTo, limit, offset, auditLogRequest);
        return ResponseEntity.ok().body(allAlerts);
    }

    @PostMapping("/create")
    @Operation(summary = "Create an alert with audit")
    public ResponseEntity<Alert> createAlert(@RequestBody Alert alert,
                                             @RequestHeader AuditLogRequest auditLogRequest) {
        Alert createdAlert = alertServiceWithAudit.createAlert(alert, auditLogRequest);
        return ResponseEntity.ok().body(createdAlert);
    }

    @DeleteMapping("/{alertId}")
    @Operation(summary = "Delete an Alert by its id with audit")
    public ResponseEntity<Void> deleteAlertById(@PathVariable final String alertId,
                                                @RequestParam(required = true) String alertType,
                                                @RequestHeader AuditLogRequest auditLogRequest) {
        alertServiceWithAudit.deleteAlertById(alertId, alertType, auditLogRequest);
        return ResponseEntity.ok().build();
    }
}
