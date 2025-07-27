package com.dair.cais.cases.audit.controller;

import com.dair.cais.audit.AuditTrail;
import com.dair.cais.cases.audit.service.CaseAuditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * REST controller for case history operations.
 */
@Slf4j
@RestController
@RequestMapping("/case-history")
@Tag(name = "Case History", description = "APIs for retrieving case history from audit trail")
@RequiredArgsConstructor
public class CaseHistoryController {

    private final CaseAuditService caseAuditService;

    /**
     * Get case history from audit trail.
     *
     * @param caseId the case ID
     * @return list of audit trail entries
     */
    @GetMapping("/cases/{caseId}")
    @Operation(summary = "Get case history")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Case not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<AuditTrail>> getCaseHistory(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId) {
        log.info("REST request to get history for case ID: {}", caseId);

        try {
            List<AuditTrail> history = caseAuditService.getCaseHistory(caseId);
            return ResponseEntity.ok(history);
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving case history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get case history from audit trail within a date range.
     *
     * @param caseId    the case ID
     * @param startDate the start date
     * @param endDate   the end date
     * @return list of audit trail entries
     */
    @GetMapping("/cases/{caseId}/date-range")
    @Operation(summary = "Get case history by date range")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "History retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Case not found"),
            @ApiResponse(responseCode = "400", description = "Invalid date range"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<AuditTrail>> getCaseHistoryByDateRange(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId,
            @Parameter(description = "Start date (ISO format)", example = "2023-01-01T00:00:00")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @Parameter(description = "End date (ISO format)", example = "2023-12-31T23:59:59")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        log.info("REST request to get history for case ID: {} between {} and {}", caseId, startDate, endDate);

        try {
            List<AuditTrail> history = caseAuditService.getCaseHistory(caseId, startDate, endDate);
            return ResponseEntity.ok(history);
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid date range: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving case history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}