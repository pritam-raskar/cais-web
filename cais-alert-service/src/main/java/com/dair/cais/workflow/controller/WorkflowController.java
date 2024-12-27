package com.dair.cais.workflow.controller;

import com.dair.cais.workflow.exception.ErrorResponse;
import com.dair.cais.workflow.model.Workflow;
import com.dair.cais.workflow.model.WorkflowDetailDTO;
import com.dair.cais.workflow.service.WorkflowService;
import com.dair.cais.workflow.exception.WorkflowAlreadyExistsException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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
}