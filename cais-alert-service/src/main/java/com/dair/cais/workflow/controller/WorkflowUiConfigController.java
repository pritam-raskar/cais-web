package com.dair.cais.workflow.controller;

import com.dair.cais.workflow.dto.WorkflowUiConfigDTO;
import com.dair.cais.workflow.model.Workflow;
import com.dair.cais.workflow.service.WorkflowConfigurationService;
import com.dair.cais.workflow.service.WorkflowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/workflows")
@RequiredArgsConstructor
public class WorkflowUiConfigController {

    private final WorkflowService workflowService;
    private final WorkflowConfigurationService workflowConfigurationService;
    private final ObjectMapper objectMapper;

    @PutMapping("/{workflowId}/ui-config")
    @Operation(summary = "Update workflow UI configuration and save relational data")
    public ResponseEntity<Workflow> updateWorkflowUiConfig(
            @PathVariable Long workflowId,
            @RequestBody String uiConfig) {

        log.info("Updating workflow UI configuration for workflow ID: {}", workflowId);

        // First save the UI config JSON
        Workflow workflow = workflowService.updateWorkflowUiConfig(workflowId, uiConfig);

        // Then parse and save the configuration in relational tables
        workflowConfigurationService.parseAndSaveWorkflowConfiguration(workflowId, uiConfig);

        return ResponseEntity.ok(workflow);
    }
}