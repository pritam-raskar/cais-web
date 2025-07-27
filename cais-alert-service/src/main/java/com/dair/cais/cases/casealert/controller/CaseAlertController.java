package com.dair.cais.cases.casealert.controller;

import com.dair.cais.alert.Alert;
import com.dair.cais.cases.Case;
import com.dair.cais.cases.casealert.service.CaseAlertService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Case-Alert association operations.
 */
@Slf4j
@RestController
@RequestMapping("/case-alerts")
@Tag(name = "Case-Alert Management", description = "APIs for managing case-alert associations")
@RequiredArgsConstructor
public class CaseAlertController {

    private final CaseAlertService caseAlertService;

    /**
     * Adds an alert to a case.
     *
     * @param caseId  the case ID
     * @param alertId the alert ID
     * @param reason  the reason for adding the alert
     * @return void response
     */
    @PostMapping("/cases/{caseId}/alerts")
    @Operation(summary = "Add an alert to a case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Alert added to case successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or alert already associated with case"),
            @ApiResponse(responseCode = "404", description = "Case or alert not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> addAlertToCase(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId,
            @Parameter(description = "ID of the alert to add", required = true)
            @RequestParam String alertId,
            @Parameter(description = "Reason for adding the alert to the case", required = true)
            @RequestBody String reason) {
        log.info("REST request to add alert ID: {} to case ID: {} with reason: {}", alertId, caseId, reason);

        try {
            // TODO: Get user ID from security context
            String userId = "system"; // Placeholder

            caseAlertService.addAlertToCase(caseId, alertId, reason, userId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.error("Case or alert not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error adding alert to case", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Removes an alert from a case.
     *
     * @param caseId  the case ID
     * @param alertId the alert ID
     * @return void response
     */
    @DeleteMapping("/cases/{caseId}/alerts/{alertId}")
    @Operation(summary = "Remove an alert from a case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Alert removed from case successfully"),
            @ApiResponse(responseCode = "404", description = "Association not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> removeAlertFromCase(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId,
            @Parameter(description = "ID of the alert to remove", required = true)
            @PathVariable String alertId) {
        log.info("REST request to remove alert ID: {} from case ID: {}", alertId, caseId);

        try {
            caseAlertService.removeAlertFromCase(caseId, alertId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.error("Association not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error removing alert from case", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets all alerts associated with a case.
     *
     * @param caseId the case ID
     * @return the list of alerts
     */
    @GetMapping("/cases/{caseId}/alerts")
    @Operation(summary = "Get all alerts associated with a case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Alerts retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Case not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Alert>> getAlertsForCase(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId) {
        log.info("REST request to get alerts for case ID: {}", caseId);

        try {
            List<Alert> alerts = caseAlertService.getAlertsForCase(caseId);
            return ResponseEntity.ok(alerts);
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving alerts for case", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets all cases associated with an alert.
     *
     * @param alertId the alert ID
     * @return the list of cases
     */
    @GetMapping("/alerts/{alertId}/cases")
    @Operation(summary = "Get all cases associated with an alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cases retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Alert not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Case>> getCasesForAlert(
            @Parameter(description = "ID of the alert", required = true)
            @PathVariable String alertId) {
        log.info("REST request to get cases for alert ID: {}", alertId);

        try {
            List<Case> cases = caseAlertService.getCasesForAlert(alertId);
            return ResponseEntity.ok(cases);
        } catch (EntityNotFoundException e) {
            log.error("Alert not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving cases for alert", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Creates a new case from an alert.
     *
     * @param alertId  the alert ID
     * @param caseData the case data
     * @return the created case
     */
    @PostMapping("/alerts/{alertId}/create-case")
    @Operation(summary = "Create a new case from an alert")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Case created successfully",
                    content = @Content(schema = @Schema(implementation = Case.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Alert not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Case> createCaseFromAlert(
            @Parameter(description = "ID of the alert", required = true)
            @PathVariable String alertId,
            @Valid @RequestBody Case caseData) {
        log.info("REST request to create case from alert ID: {}", alertId);

        try {
            // TODO: Get user ID from security context
            String userId = "system"; // Placeholder

            Case createdCase = caseAlertService.createCaseFromAlert(alertId, caseData, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCase);
        } catch (EntityNotFoundException e) {
            log.error("Alert not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid case data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating case from alert", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}