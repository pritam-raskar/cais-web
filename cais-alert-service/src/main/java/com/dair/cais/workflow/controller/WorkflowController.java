package com.dair.cais.workflow.controller;

import com.dair.cais.workflow.exception.ErrorResponse;
import com.dair.cais.workflow.exception.WorkflowUpdateException;
import com.dair.cais.workflow.model.Workflow;
import com.dair.cais.workflow.model.WorkflowDetailDTO;
import com.dair.cais.workflow.service.WorkflowService;
import com.dair.cais.workflow.exception.WorkflowAlreadyExistsException;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

/**
 * REST controller for workflow operations.
 */
@Slf4j
@RestController
@RequestMapping("/workflows")
@Tag(name = "Workflow Management", description = "APIs for managing workflows")
@RequiredArgsConstructor
public class WorkflowController {
    private final WorkflowService workflowService;

    @GetMapping
    @Operation(summary = "Get all workflows")
    public ResponseEntity<List<Workflow>> getAllWorkflows() {
        log.info("Fetching all workflows");
        List<Workflow> workflows = workflowService.getAllWorkflows();
        return ResponseEntity.ok(workflows);
    }

    @GetMapping("/{workflowId}")
    @Operation(summary = "Get workflow by ID")
    public ResponseEntity<Workflow> getWorkflowById(@PathVariable Long workflowId) {
        log.info("Fetching workflow with id: {}", workflowId);
        Workflow workflow = workflowService.getWorkflowById(workflowId);
        return workflow != null ? ResponseEntity.ok(workflow) : ResponseEntity.notFound().build();
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Create new workflow")
    public ResponseEntity<?> createWorkflow(@Valid @RequestBody Workflow workflow) {
        log.info("Creating new workflow: {}", workflow);
        try {
            Workflow createdWorkflow = workflowService.createWorkflow(workflow);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(createdWorkflow);
        } catch (WorkflowAlreadyExistsException e) {
            log.warn("Workflow creation failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorResponse("CONFLICT", e.getMessage()));
        }
    }

    @GetMapping("/{workflowId}/details")
    @Operation(summary = "Get workflow details including steps and transitions")
    public ResponseEntity<List<WorkflowDetailDTO>> getWorkflowDetails(@PathVariable Long workflowId) {
        log.info("Fetching workflow details for workflow id: {}", workflowId);
        List<WorkflowDetailDTO> details = workflowService.getWorkflowDetails(workflowId);
        return ResponseEntity.ok(details);
    }

    @PutMapping("/{workflowId}")
    @Operation(summary = "Update an existing workflow")
    public ResponseEntity<Workflow> updateWorkflow(
            @PathVariable Long workflowId,
            @Valid @RequestBody Workflow workflow) {
        log.info("Updating workflow with id: {}", workflowId);
        Workflow updatedWorkflow = workflowService.updateWorkflow(workflowId, workflow);
        return ResponseEntity.ok(updatedWorkflow);
    }

    @DeleteMapping("/{workflowId}")
    @Operation(summary = "Delete a workflow")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable Long workflowId) {
        log.info("Deleting workflow with id: {}", workflowId);
        workflowService.deleteWorkflow(workflowId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    @Operation(summary = "Get total number of workflows")
    public ResponseEntity<Long> getWorkflowCount() {
        log.info("Fetching total workflow count");
        long count = workflowService.getWorkflowCount();
        return ResponseEntity.ok(count);
    }

    @GetMapping("/exists/{workflowId}")
    @Operation(summary = "Check if a workflow exists")
    public ResponseEntity<Boolean> workflowExists(@PathVariable Long workflowId) {
        log.info("Checking if workflow exists with id: {}", workflowId);
        boolean exists = workflowService.workflowExists(workflowId);
        return ResponseEntity.ok(exists);
    }

    @GetMapping("/{workflowId}/ui-config")
    @Operation(summary = "Get workflow UI configuration")
    public ResponseEntity<String> getWorkflowUiConfig(@PathVariable Long workflowId) {
        log.info("Fetching UI configuration for workflow with id: {}", workflowId);
        String uiConfig = workflowService.getWorkflowUiConfig(workflowId);
        return ResponseEntity
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(uiConfig);
    }

    @PutMapping(value = "/{workflowId}/ui-config",
            produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update workflow UI configuration",
            description = "Updates the UI configuration for a specific workflow. Replaces the entire configuration.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "UI configuration updated successfully",
                    content = @Content(schema = @Schema(implementation = Workflow.class))),
            @ApiResponse(responseCode = "404",
                    description = "Workflow not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500",
                    description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Workflow> updateWorkflowUiConfig(
            @Parameter(description = "ID of the workflow to update", required = true)
            @PathVariable Long workflowId,

            @Parameter(description = "New UI configuration", required = true)
            @Valid @RequestBody com.dair.cais.workflow.dto.WorkflowUiConfigDTO uiConfigDTO) {

        log.info("Received request to update UI configuration for workflow ID: {}", workflowId);

        try {
            // Validate input
            if (uiConfigDTO == null || uiConfigDTO.getUiConfig() == null) {
                log.error("Invalid input: UI configuration cannot be null for workflow ID: {}", workflowId);
                return ResponseEntity.badRequest().build();
            }

            // Update workflow UI configuration
            Workflow updatedWorkflow = workflowService.updateWorkflowUiConfig(
                    workflowId,
                    uiConfigDTO.getUiConfig()
            );

            log.info("Successfully updated UI configuration for workflow ID: {}", workflowId);
            return ResponseEntity.ok(updatedWorkflow);

        } catch (EntityNotFoundException e) {
            log.error("Workflow not found with ID: {}", workflowId);
            return ResponseEntity.notFound().build();

        } catch (WorkflowUpdateException e) {
            log.error("Failed to update UI configuration for workflow ID: {}", workflowId, e);
            return ResponseEntity.internalServerError().build();

        } catch (Exception e) {
            log.error("Unexpected error while updating UI configuration for workflow ID: {}", workflowId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}