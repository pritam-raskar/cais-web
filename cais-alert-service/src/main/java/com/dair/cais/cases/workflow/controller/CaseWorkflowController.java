package com.dair.cais.cases.workflow.controller;

import com.dair.cais.cases.Case;
import com.dair.cais.cases.workflow.dto.StepTransitionRequest;
import com.dair.cais.cases.workflow.service.CaseWorkflowService;
import com.dair.cais.steps.Step;
import com.dair.cais.workflow.dto.StepTransitionDTO;
import com.dair.cais.workflow.entity.WorkflowTransitionEntity;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for Case Workflow operations.
 */
@Slf4j
@RestController
@RequestMapping("/case-workflows")
@Tag(name = "Case Workflow Management", description = "APIs for managing case workflows")
@RequiredArgsConstructor
public class CaseWorkflowController {

    private final CaseWorkflowService caseWorkflowService;

    /**
     * Gets all available steps for a case based on case type workflow.
     * Follows the pattern: case → caseType → workflow → steps
     *
     * @param caseId the case ID
     * @return the list of all workflow steps
     */
    @GetMapping("/cases/{caseId}/available-steps")
    @Operation(summary = "Get all available steps for a case via case type workflow",
            description = "Returns all steps in the workflow associated with the case's case type")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Steps retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Case or case type not found"),
            @ApiResponse(responseCode = "400", description = "Case has no case type assigned or case type has no workflow"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<Step>> getAvailableSteps(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId) {
        log.info("REST request to get available steps for case ID: {}", caseId);

        try {
            List<Step> steps = caseWorkflowService.getAvailableStepsViaCaseType(caseId);
            return ResponseEntity.ok(steps);
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Invalid workflow state: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving available steps", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Transitions a case to a new step.
     *
     * @param caseId   the case ID
     * @param stepId   the step ID
     * @param request  the transition request containing reason and comments
     * @return the updated case
     */
    @PostMapping("/cases/{caseId}/transition")
    @Operation(summary = "Transition a case to a new step")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Case transitioned successfully",
                    content = @Content(schema = @Schema(implementation = Case.class))),
            @ApiResponse(responseCode = "404", description = "Case or step not found"),
            @ApiResponse(responseCode = "400", description = "Invalid transition or case has no workflow assigned"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Case> transitionCase(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId,
            @Parameter(description = "ID of the target step", required = true)
            @RequestParam Long stepId,
            @RequestBody StepTransitionRequest request) {
        log.info("REST request to transition case ID: {} to step ID: {} with reason: {}",
                caseId, stepId, request.getReason());

        try {
            // TODO: Get user ID from security context
            String userId = "system"; // Placeholder

            Case updatedCase = caseWorkflowService.transitionCase(caseId, stepId, request.getReason(), userId);
            return ResponseEntity.ok(updatedCase);
        } catch (EntityNotFoundException e) {
            log.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error transitioning case", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets possible transitions for a case.
     *
     * @param caseId the case ID
     * @return the list of possible transitions
     */
    @GetMapping("/cases/{caseId}/possible-transitions")
    @Operation(summary = "Get possible transitions for a case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Transitions retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Case not found"),
            @ApiResponse(responseCode = "400", description = "Case has no workflow assigned or no current step"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<WorkflowTransitionEntity>> getPossibleTransitions(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId) {
        log.info("REST request to get possible transitions for case ID: {}", caseId);

        try {
            List<WorkflowTransitionEntity> transitions = caseWorkflowService.getPossibleTransitions(caseId);
            return ResponseEntity.ok(transitions);
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Invalid workflow state: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving possible transitions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Assigns a workflow to a case.
     *
     * @param caseId     the case ID
     * @param workflowId the workflow ID
     * @return the updated case
     */
    @PostMapping("/cases/{caseId}/assign-workflow")
    @Operation(summary = "Assign a workflow to a case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Workflow assigned successfully",
                    content = @Content(schema = @Schema(implementation = Case.class))),
            @ApiResponse(responseCode = "404", description = "Case or workflow not found"),
            @ApiResponse(responseCode = "400", description = "Case already has a workflow in progress"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Case> assignWorkflow(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId,
            @Parameter(description = "ID of the workflow", required = true)
            @RequestParam Long workflowId) {
        log.info("REST request to assign workflow ID: {} to case ID: {}", workflowId, caseId);

        try {
            // TODO: Get user ID from security context
            String userId = "system"; // Placeholder

            Case updatedCase = caseWorkflowService.assignWorkflow(caseId, workflowId, userId);
            return ResponseEntity.ok(updatedCase);
        } catch (EntityNotFoundException e) {
            log.error("Entity not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Invalid workflow state: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error assigning workflow", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Gets step transitions for a case.
     *
     * @param caseId the case ID
     * @return the step transitions
     */
    @GetMapping("/cases/{caseId}/step-transitions")
    @Operation(summary = "Get step transitions for a case")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Step transitions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = StepTransitionDTO.class))),
            @ApiResponse(responseCode = "404", description = "Case not found"),
            @ApiResponse(responseCode = "400", description = "Case has no workflow assigned or no current step"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<StepTransitionDTO> getCaseStepTransitions(
            @Parameter(description = "ID of the case", required = true)
            @PathVariable Long caseId) {
        log.info("REST request to get step transitions for case ID: {}", caseId);

        try {
            StepTransitionDTO transitions = caseWorkflowService.getCaseStepTransitions(caseId);
            return ResponseEntity.ok(transitions);
        } catch (EntityNotFoundException e) {
            log.error("Case not found: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException e) {
            log.error("Invalid workflow state: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving step transitions", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}