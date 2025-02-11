package com.dair.cais.workflow.controller;

import com.dair.cais.workflow.model.WorkflowConfigurationDTO;
import com.dair.cais.workflow.exception.ErrorResponse;
import com.dair.cais.workflow.service.WorkflowConfigurationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/v1/workflows")
@Tag(name = "Workflow Configuration", description = "APIs for managing workflow configurations")
@RequiredArgsConstructor
@Validated
public class WorkflowConfigurationController {

    private final WorkflowConfigurationService workflowConfigurationService;

    @GetMapping("/{workflowId}/configuration")
    @Operation(summary = "Get workflow configuration",
            description = "Retrieves the complete configuration for a workflow including steps, transitions, and their properties")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Workflow not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<WorkflowConfigurationDTO> getWorkflowConfiguration(
            @Parameter(description = "ID of the workflow", required = true)
            @PathVariable Long workflowId) {
        log.info("REST request to get workflow configuration for workflow ID: {}", workflowId);
        WorkflowConfigurationDTO config = workflowConfigurationService.getWorkflowConfiguration(workflowId);
        return ResponseEntity.ok(config);
    }

    @PutMapping(value = "/{workflowId}/configuration",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Update workflow configuration",
            description = "Updates the complete configuration for a workflow including steps, transitions, and their properties")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Configuration updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid configuration",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Workflow not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> updateWorkflowConfiguration(
            @Parameter(description = "ID of the workflow", required = true)
            @PathVariable Long workflowId,
            @Parameter(description = "New workflow configuration", required = true)
            @Valid @RequestBody WorkflowConfigurationDTO configuration) {
        log.info("REST request to update workflow configuration for workflow ID: {}", workflowId);

        if (!workflowId.equals(configuration.getWorkflowId())) {
            log.error("Workflow ID mismatch. Path: {}, Body: {}", workflowId, configuration.getWorkflowId());
            return ResponseEntity.badRequest().build();
        }

        workflowConfigurationService.saveWorkflowConfiguration(configuration);
        return ResponseEntity.ok().build();
    }
}