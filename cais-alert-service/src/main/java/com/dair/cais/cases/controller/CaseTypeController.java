package com.dair.cais.cases.controller;

import com.dair.cais.cases.CaseType;
import com.dair.cais.cases.service.CaseTypeService;
import com.dair.cais.workflow.exception.ErrorResponse;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST controller for Case Type management operations.
 */
@Slf4j
@RestController
@RequestMapping("/case-types")
@Tag(name = "Case Type Management", description = "APIs for managing case types")
@RequiredArgsConstructor
public class CaseTypeController {

    private final CaseTypeService caseTypeService;

    /**
     * Creates a new case type.
     *
     * @param caseType the case type data
     * @return the created case type
     */
    @PostMapping
    @Operation(summary = "Create a new case type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Case type created successfully",
                    content = @Content(schema = @Schema(implementation = CaseType.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> createCaseType(@Valid @RequestBody CaseType caseType) {
        log.info("REST request to create a new case type: {}", caseType);
        try {
            CaseType createdCaseType = caseTypeService.createCaseType(caseType);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdCaseType);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid case type data: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (DataIntegrityViolationException e) {
            log.warn("Database constraint violation when creating case type", e);

            // Check if it's a foreign key violation for workflow
            if (e.getMessage() != null && e.getMessage().contains("fk_case_type_workflow")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "The specified workflow does not exist");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Other data integrity issues
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Data integrity violation occurred. Please check your input.");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error creating case type", e);

            // Generic error response for unexpected errors
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "An unexpected error occurred while creating the case type");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Retrieves a case type by its ID.
     *
     * @param typeId the case type ID
     * @return the case type
     */
    @GetMapping("/{typeId}")
    @Operation(summary = "Get a case type by ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Case type found",
                    content = @Content(schema = @Schema(implementation = CaseType.class))),
            @ApiResponse(responseCode = "404", description = "Case type not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CaseType> getCaseType(
            @Parameter(description = "ID of the case type to retrieve", required = true)
            @PathVariable Long typeId) {
        log.info("REST request to get case type with ID: {}", typeId);
        try {
            CaseType caseType = caseTypeService.getCaseType(typeId);
            return ResponseEntity.ok(caseType);
        } catch (EntityNotFoundException e) {
            log.warn("Case type not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("Error retrieving case type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Retrieves all case types.
     *
     * @return the list of all case types
     */
    @GetMapping
    @Operation(summary = "Get all case types")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Case types retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CaseType>> getAllCaseTypes() {
        log.info("REST request to get all case types");
        try {
            List<CaseType> caseTypes = caseTypeService.getAllCaseTypes();
            return ResponseEntity.ok(caseTypes);
        } catch (Exception e) {
            log.error("Error retrieving case types", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Updates an existing case type.
     *
     * @param typeId the case type ID
     * @param caseType the case type data
     * @return the updated case type
     */
    @PutMapping("/{typeId}")
    @Operation(summary = "Update an existing case type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Case type updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input"),
            @ApiResponse(responseCode = "404", description = "Case type not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> updateCaseType(
            @Parameter(description = "ID of the case type to update", required = true)
            @PathVariable Long typeId,
            @Valid @RequestBody CaseType caseType) {
        log.info("REST request to update case type with ID: {}", typeId);
        try {
            CaseType updatedCaseType = caseTypeService.updateCaseType(typeId, caseType);
            return ResponseEntity.ok(updatedCaseType);
        } catch (EntityNotFoundException e) {
            log.warn("Case type not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            log.warn("Invalid case type data: {}", e.getMessage());
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (DataIntegrityViolationException e) {
            log.warn("Database constraint violation when updating case type", e);

            // Check if it's a foreign key violation for workflow
            if (e.getMessage() != null && e.getMessage().contains("fk_case_type_workflow")) {
                Map<String, String> errorResponse = new HashMap<>();
                errorResponse.put("error", "The specified workflow does not exist");
                return ResponseEntity.badRequest().body(errorResponse);
            }

            // Other data integrity issues
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", "Data integrity violation occurred. Please check your input.");
            return ResponseEntity.badRequest().body(errorResponse);
        } catch (Exception e) {
            log.error("Error updating case type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Deletes a case type.
     *
     * @param typeId the case type ID
     * @return void
     */
    @DeleteMapping("/{typeId}")
    @Operation(summary = "Delete a case type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Case type deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Case type not found"),
            @ApiResponse(responseCode = "409", description = "Case type is in use"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Void> deleteCaseType(
            @Parameter(description = "ID of the case type to delete", required = true)
            @PathVariable Long typeId) {
        log.info("REST request to delete case type with ID: {}", typeId);
        try {
            caseTypeService.deleteCaseType(typeId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.warn("Case type not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            // This would be thrown if the case type is in use
            log.warn("Cannot delete case type: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (Exception e) {
            log.error("Error deleting case type", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}