package com.dair.cais.alert;

import com.dair.cais.alert.dto.BulkStepChangeRequest;
import com.dair.cais.alert.dto.BulkStepChangeResponse;
import com.dair.cais.alert.dto.StepTransitionDTO;
import com.dair.cais.alert.exception.AlertOperationException;
import com.dair.cais.alert.exception.AlertValidationException;
import com.dair.cais.alert.filter.AlertFilterRequest;
import com.dair.cais.alert.filter.FilterCriteria;
import com.dair.cais.audit.AuditLogRequest;
import com.dair.cais.workflow.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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
    
    /**
     * Extracts user ID from authentication context
     */
    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
                return ((UserDetails) authentication.getPrincipal()).getUsername();
            }
            if (authentication != null && authentication.getName() != null) {
                return authentication.getName();
            }
            throw new SecurityException("No authenticated user found");
        } catch (SecurityException e) {
            throw e;
        } catch (Exception e) {
            log.error("Authentication context error", e);
            throw new SecurityException("Authentication failed");
        }
    }

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
            @RequestParam Long stepId,
            @RequestParam(required = false) String userId) {
        log.debug("Request received to change step for alert: {} by user: {}", alertId, userId);
        return ResponseEntity.ok(alertService.changeStep(alertId, stepId, userId));
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

    @PostMapping("/bulk/step-change")
    @Operation(summary = "Change steps for multiple alerts",
            description = "Bulk operation to change steps for multiple alerts with individual validation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bulk operation completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BulkStepChangeResponse> bulkStepChange(
            @Valid @RequestBody BulkStepChangeRequest request) {
        log.debug("Request received for bulk step change: {} alerts to step {}", 
                request.getAlertIds().size(), request.getStepId());
        
        BulkStepChangeResponse response = alertService.changeStepBulk(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/audit/bulk/step-change")
    @Operation(summary = "Change steps for multiple alerts with audit",
            description = "Bulk operation to change steps for multiple alerts with audit logging")
    public ResponseEntity<BulkStepChangeResponse> bulkStepChangeWithAudit(
            @Valid @RequestBody BulkStepChangeRequest request,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received for bulk step change with audit: {} alerts to step {}", 
                request.getAlertIds().size(), request.getStepId());
        
        BulkStepChangeResponse response = alertService.changeStepBulkWithAudit(request, auditLogRequest);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/rollback/{alertId}")
    @Operation(summary = "Rollback alert to previous step")
    public ResponseEntity<Alert> rollbackStep(
            @PathVariable String alertId,
            @RequestParam("reason") String rollbackReason) {
        log.debug("Request received to rollback step for alert: {}", alertId);
        return ResponseEntity.ok(alertService.rollbackStep(alertId, rollbackReason));
    }

    @PatchMapping(value = "/audit/rollback/{alertId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Rollback alert to previous step with audit")
    public ResponseEntity<Alert> rollbackStepWithAudit(
            @PathVariable String alertId,
            @RequestParam("reason") String rollbackReason,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.debug("Request received to rollback step for alert: {} with audit", alertId);
        return ResponseEntity.ok(alertService.rollbackStepWithAudit(alertId, rollbackReason, auditLogRequest));
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

    @GetMapping("/{alertId}/step-transitions")
    @Operation(summary = "Get next and previous possible steps for an alert",
            description = "Returns the possible next steps and previous steps based on the current step in the workflow")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved step transitions"),
            @ApiResponse(responseCode = "404", description = "Alert or workflow not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<StepTransitionDTO> getAlertStepTransitions(
            @Parameter(description = "ID of the alert", required = true)
            @PathVariable String alertId) {
        log.info("REST request to get step transitions for alert ID: {}", alertId);

        try {
            StepTransitionDTO transitions = alertService.getAlertStepTransitions(alertId);
            return ResponseEntity.ok(transitions);
        } catch (EntityNotFoundException e) {
            log.warn("Alert or workflow not found for alert ID: {}", alertId);
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving step transitions for alert ID: {}", alertId, e);
            throw new RuntimeException("Error retrieving step transitions", e);
        }
    }

    /*@GetMapping("/audit/{alertId}/step-transitions")
    @Operation(summary = "Get next and previous possible steps for an alert with audit",
            description = "Returns the possible next steps and previous steps based on the current step in the workflow with audit logging")
    public ResponseEntity<StepTransitionDTO> getAlertStepTransitionsWithAudit(
            @Parameter(description = "ID of the alert", required = true)
            @PathVariable String alertId,
            @RequestBody AuditLogRequest auditLogRequest) {
        log.info("REST request to get step transitions for alert ID: {} with audit", alertId);

        StepTransitionDTO transitions = alertService.getAlertStepTransitions(alertId);

        // Log the audit
        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setAffectedItemId(alertId);
        auditLogRequest.setDescription("Retrieved step transitions");
        auditTrailService.logAction(
                auditLogRequest.getUserId(),
                auditLogRequest.getUserRole(),
                auditLogRequest.getActionId(),
                auditLogRequest.getDescription(),
                auditLogRequest.getCategory(),
                auditLogRequest.getAffectedItemType(),
                auditLogRequest.getAffectedItemId(),
                null,
                String.format("Found %d next steps and %d back steps",
                        transitions.getNextSteps().size(),
                        transitions.getBackSteps().size())
        );

        return ResponseEntity.ok(transitions);
    }*/
    
    @PostMapping("/bulk/change-step")
    @Operation(summary = "Change step for multiple alerts",
            description = "Performs bulk step change operation for multiple alerts with validation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bulk step change completed"),
            @ApiResponse(responseCode = "400", description = "Invalid request data"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<BulkStepChangeResponse> changeStepBulk(
            @Parameter(description = "Bulk step change request", required = true)
            @Valid @RequestBody BulkStepChangeRequest request) {
        String currentUserId = getCurrentUserId();
        log.info("REST request for bulk step change by user {}: {} alerts to step {}", 
                currentUserId, request.getAlertIds().size(), request.getStepId());
        
        try {
            BulkStepChangeResponse response = alertService.changeStepBulk(request);
            log.info("Bulk step change completed by user {}: {} successful, {} failed", 
                    currentUserId, response.getSuccessCount(), response.getFailureCount());
            return ResponseEntity.ok(response);
        } catch (AlertValidationException e) {
            log.error("Bulk step change validation failed for user {}: {}", currentUserId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Error during bulk step change operation for user {}", currentUserId, e);
            throw new AlertOperationException("Bulk step change operation failed: " + e.getMessage(), e);
        }
    }
    
    @PostMapping("/audit/bulk/change-step")
    @Operation(summary = "Change step for multiple alerts with audit",
            description = "Performs bulk step change operation with comprehensive audit logging")
    public ResponseEntity<BulkStepChangeResponse> changeStepBulkWithAudit(
            @Valid @RequestBody BulkStepChangeRequest request,
            @RequestBody AuditLogRequest auditLogRequest) {
        String currentUserId = getCurrentUserId();
        // Set user context in audit request
        auditLogRequest.setUserId(Long.valueOf(currentUserId));
        
        log.info("REST request for bulk step change with audit by user {}: {} alerts to step {}", 
                currentUserId, request.getAlertIds().size(), request.getStepId());
        
        try {
            BulkStepChangeResponse response = alertService.changeStepBulkWithAudit(request, auditLogRequest);
            log.info("Bulk step change with audit completed by user {}: {} successful, {} failed", 
                    currentUserId, response.getSuccessCount(), response.getFailureCount());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error during bulk step change with audit for user {}", currentUserId, e);
            throw new AlertOperationException("Bulk step change with audit failed: " + e.getMessage(), e);
        }
    }
    
    @ExceptionHandler(AlertOperationException.class)
    public ResponseEntity<ErrorResponse> handleAlertOperationException(AlertOperationException ex) {
        log.error("Alert operation exception: {}", ex.getMessage(), ex);
        ErrorResponse error = new ErrorResponse(
                "ALERT_OPERATION_ERROR",
                ex.getMessage(),
                System.currentTimeMillis()
        );
        return ResponseEntity.badRequest().body(error);
    }
}
