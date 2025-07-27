package com.dair.cais.cases.controller;

import com.dair.cais.cases.Case;
import com.dair.cais.cases.service.CaseService;
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
import java.util.Map;

/**
 * REST controller for Case management operations.
 */
@Slf4j
@RestController
@RequestMapping("/cases")
@Tag(name = "Case Management", description = "APIs for managing cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    /**
     * Creates a new case.
     *
     * @param caseData the case data
     * @return the created case
     */
    @PostMapping
    @Operation(summary = "Create a new case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Case created successfully",
                    content = @Content(schema = @Schema(implementation = Case.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Case> createCase(@Valid @RequestBody Case caseData) {
        log.info("REST request to create a new case");
        try {
            Case createdCase = caseService.createCase(caseData);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCase);
        } catch (IllegalArgumentException e) {
            log.error("Invalid case data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error creating case", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a case by its ID.
     *
     * @param caseId the case ID
     * @return the case
     */
    @GetMapping("/{caseId}")
    @Operation(summary = "Get a case by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Case found",
                    content = @Content(schema = @Schema(implementation = Case.class))),
            @ApiResponse(responseCode = "404", description = "Case not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Case> getCase(
            @Parameter(description = "ID of the case to retrieve", required = true)
            @PathVariable Long caseId) {
        log.info("REST request to get case with ID: {}", caseId);
        try {
            Case caseData = caseService.getCase(caseId);
            return ResponseEntity.ok(caseData);
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving case", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves a case by its case number.
     *
     * @param caseNumber the case number
     * @return the case
     */
    @GetMapping("/by-number")
    @Operation(summary = "Get a case by case number")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Case found",
                    content = @Content(schema = @Schema(implementation = Case.class))),
            @ApiResponse(responseCode = "404", description = "Case not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Case> getCaseByCaseNumber(
            @Parameter(description = "Case number to retrieve", required = true)
            @RequestParam String caseNumber) {
        log.info("REST request to get case with case number: {}", caseNumber);
        try {
            Case caseData = caseService.getCaseByCaseNumber(caseNumber);
            return ResponseEntity.ok(caseData);
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving case", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all cases.
     *
     * @return the list of all cases
     */
    @GetMapping
    @Operation(summary = "Get all cases")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cases retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Case>> getAllCases() {
        log.info("REST request to get all cases");
        try {
            List<Case> cases = caseService.getAllCases();
            return ResponseEntity.ok(cases);
        } catch (Exception e) {
            log.error("Error retrieving cases", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Finds cases based on search criteria.
     *
     * @param criteria the search criteria
     * @return the list of matching cases
     */
    @PostMapping("/search")
    @Operation(summary = "Find cases by criteria")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cases retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Case>> findCases(@RequestBody Map<String, Object> criteria) {
        log.info("REST request to find cases by criteria: {}", criteria);
        try {
            List<Case> cases = caseService.findCases(criteria);
            return ResponseEntity.ok(cases);
        } catch (IllegalArgumentException e) {
            log.error("Invalid search criteria: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error searching cases", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates an existing case.
     *
     * @param caseId the case ID
     * @param caseData the case data
     * @return the updated case
     */
    @PutMapping("/{caseId}")
    @Operation(summary = "Update an existing case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Case updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Case not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Case> updateCase(
            @Parameter(description = "ID of the case to update", required = true)
            @PathVariable Long caseId,
            @Valid @RequestBody Case caseData) {
        log.info("REST request to update case with ID: {}", caseId);
        try {
            Case updatedCase = caseService.updateCase(caseId, caseData);
            return ResponseEntity.ok(updatedCase);
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid case data: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error updating case", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a case.
     *
     * @param caseId the case ID
     * @return void
     */
    @DeleteMapping("/{caseId}")
    @Operation(summary = "Delete a case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Case deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Case not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteCase(
            @Parameter(description = "ID of the case to delete", required = true)
            @PathVariable Long caseId) {
        log.info("REST request to delete case with ID: {}", caseId);
        try {
            caseService.deleteCase(caseId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error deleting case", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Changes the status of a case.
     *
     * @param caseId the case ID
     * @param newStatus the new status
     * @param reason the reason for status change
     * @return the updated case
     */
    @PatchMapping("/{caseId}/status")
    @Operation(summary = "Change the status of a case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Case status changed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Case not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Case> changeStatus(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId,
            @Parameter(description = "New status value", required = true)
            @RequestParam String newStatus,
            @Parameter(description = "Reason for status change", required = true)
            @RequestParam String reason) {
        log.info("REST request to change status of case ID: {} to {} with reason: {}", caseId, newStatus, reason);
        try {
            Case updatedCase = caseService.changeStatus(caseId, newStatus, reason);
            return ResponseEntity.ok(updatedCase);
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid status change request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error changing case status", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Assigns a case to a user.
     *
     * @param caseId the case ID
     * @param userId the user ID
     * @return the updated case
     */
    @PatchMapping("/{caseId}/assign")
    @Operation(summary = "Assign a case to a user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Case assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Case not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Case> assignCase(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId,
            @Parameter(description = "ID of the user to assign the case to", required = true)
            @RequestParam String userId) {
        log.info("REST request to assign case ID: {} to user ID: {}", caseId, userId);
        try {
            Case updatedCase = caseService.assignCase(caseId, userId);
            return ResponseEntity.ok(updatedCase);
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.error("Invalid assignment request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error assigning case", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}