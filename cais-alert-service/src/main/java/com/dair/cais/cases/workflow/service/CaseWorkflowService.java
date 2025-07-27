package com.dair.cais.cases.workflow.service;

import com.dair.cais.cases.Case;
import com.dair.cais.cases.entity.CaseEntity;
import com.dair.cais.cases.mapper.CaseMapper;
import com.dair.cais.cases.repository.CaseRepository;
import com.dair.cais.steps.Step;
import com.dair.cais.steps.StepRepository;
import com.dair.cais.workflow.dto.StepTransitionDTO;
import com.dair.cais.workflow.entity.WorkflowEntity;
import com.dair.cais.workflow.entity.WorkflowStepEntity;
import com.dair.cais.workflow.entity.WorkflowTransitionEntity;
import com.dair.cais.workflow.exception.WorkflowValidationException;
import com.dair.cais.workflow.repository.WorkflowRepository;
import com.dair.cais.workflow.repository.WorkflowStepRepository;
import com.dair.cais.workflow.repository.WorkflowTransitionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service class for Case Workflow operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseWorkflowService {

    private final CaseRepository caseRepository;
    private final CaseMapper caseMapper;
    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final StepRepository stepRepository;

    /**
     * Gets available steps for a case based on its current step.
     *
     * @param caseId the case ID
     * @return list of available steps
     * @throws EntityNotFoundException if the case or workflow is not found
     */
    @Transactional(readOnly = true)
    public List<Step> getAvailableSteps(Long caseId) {
        log.debug("Getting available steps for case ID: {}", caseId);

        CaseEntity caseEntity = getCaseEntityById(caseId);
        validateWorkflowAssigned(caseEntity);

        Long currentStepId = caseEntity.getCurrentStepId();
        if (currentStepId == null) {
            log.error("Case ID: {} does not have a current step", caseId);
            throw new IllegalStateException("Case does not have a current step");
        }

        // Get transitions from the current step
        List<WorkflowTransitionEntity> transitions = workflowTransitionRepository
                .findByWorkflowWorkflowIdAndSourceStepWorkflowStepId(
                        caseEntity.getWorkflowId(),
                        currentStepId);

        // Extract target steps from transitions
        List<Step> availableSteps = new ArrayList<>();
        for (WorkflowTransitionEntity transition : transitions) {
            WorkflowStepEntity targetStepEntity = transition.getTargetStep();
            Step step = stepRepository.findById(targetStepEntity.getStep().getStepId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Step not found with ID: " + targetStepEntity.getStep().getStepId()));
            availableSteps.add(step);
        }

        log.info("Found {} available steps for case ID: {}", availableSteps.size(), caseId);
        return availableSteps;
    }

    /**
     * Transitions a case to a new step.
     *
     * @param caseId  the case ID
     * @param stepId  the target step ID
     * @param reason  the reason for the transition
     * @param userId  the ID of the user making the transition
     * @return the updated case
     * @throws EntityNotFoundException if the case, workflow, or step is not found
     * @throws IllegalStateException if the transition is not valid
     */
    @Transactional
    public Case transitionCase(Long caseId, Long stepId, String reason, String userId) {
        log.debug("Transitioning case ID: {} to step ID: {} with reason: {}", caseId, stepId, reason);

        CaseEntity caseEntity = getCaseEntityById(caseId);
        validateWorkflowAssigned(caseEntity);

        Long currentStepId = caseEntity.getCurrentStepId();
        if (currentStepId == null) {
            // If no current step, assign the first step
            log.info("Case ID: {} has no current step, assigning initial step", caseId);
            return assignInitialStep(caseEntity, stepId, reason, userId);
        }

        // Validate transition is allowed
        validateTransition(caseEntity.getWorkflowId(), currentStepId, stepId);

        // Get target step details
        Step targetStep = stepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("Step not found with ID: " + stepId));

        // Update case with new step
        caseEntity.setCurrentStepId(stepId);
        caseEntity.setCurrentStepName(targetStep.getStepName());
        caseEntity.setUpdatedAt(LocalDateTime.now());

        // If step has a status mapping, update case status
        if (targetStep.getStepStatusId() != null) {
            // TODO: Implement status update logic based on step status
            // This would involve getting the status name from the step status repository
            // caseEntity.setStatus(statusName);
        }

        // Save updated case
        CaseEntity savedEntity = caseRepository.save(caseEntity);

        // TODO: Log transition to audit trail

        log.info("Transitioned case ID: {} from step ID: {} to step ID: {}",
                caseId, currentStepId, stepId);

        return caseMapper.toModel(savedEntity);
    }

    /**
     * Gets possible transitions for a case based on its current step.
     *
     * @param caseId the case ID
     * @return list of possible workflow transitions
     * @throws EntityNotFoundException if the case or workflow is not found
     */
    @Transactional(readOnly = true)
    public List<WorkflowTransitionEntity> getPossibleTransitions(Long caseId) {
        log.debug("Getting possible transitions for case ID: {}", caseId);

        CaseEntity caseEntity = getCaseEntityById(caseId);
        validateWorkflowAssigned(caseEntity);

        Long currentStepId = caseEntity.getCurrentStepId();
        if (currentStepId == null) {
            log.error("Case ID: {} does not have a current step", caseId);
            throw new IllegalStateException("Case does not have a current step");
        }

        List<WorkflowTransitionEntity> transitions = workflowTransitionRepository
                .findByWorkflowWorkflowIdAndSourceStepWorkflowStepId(
                        caseEntity.getWorkflowId(),
                        currentStepId);

        log.info("Found {} possible transitions for case ID: {}", transitions.size(), caseId);
        return transitions;
    }

    /**
     * Assigns a workflow to a case.
     *
     * @param caseId     the case ID
     * @param workflowId the workflow ID
     * @param userId     the ID of the user assigning the workflow
     * @return the updated case
     * @throws EntityNotFoundException if the case or workflow is not found
     */
    @Transactional
    public Case assignWorkflow(Long caseId, Long workflowId, String userId) {
        log.debug("Assigning workflow ID: {} to case ID: {}", workflowId, caseId);

        CaseEntity caseEntity = getCaseEntityById(caseId);

        // Check if workflow exists
        WorkflowEntity workflow = workflowRepository.findById(workflowId)
                .orElseThrow(() -> new EntityNotFoundException("Workflow not found with ID: " + workflowId));

        // If case already has a workflow, validate it's not in progress
        if (caseEntity.getWorkflowId() != null && caseEntity.getCurrentStepId() != null) {
            log.error("Case ID: {} already has workflow ID: {} assigned and is in progress",
                    caseId, caseEntity.getWorkflowId());
            throw new IllegalStateException("Cannot change workflow for a case that is already in progress");
        }

        // Assign workflow to case
        caseEntity.setWorkflowId(workflowId);
        caseEntity.setUpdatedAt(LocalDateTime.now());

        // Find default initial step if exists
        List<WorkflowStepEntity> workflowSteps = workflowStepRepository
                .findByWorkflowWorkflowId(workflowId);

        WorkflowStepEntity defaultStep = workflowSteps.stream()
                .filter(step -> Boolean.TRUE.equals(step.getIsDefault()))
                .findFirst()
                .orElse(null);

        // If default step exists, assign it
        if (defaultStep != null) {
            Step step = stepRepository.findById(defaultStep.getStep().getStepId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Step not found with ID: " + defaultStep.getStep().getStepId()));

            caseEntity.setCurrentStepId(step.getStepId());
            caseEntity.setCurrentStepName(step.getStepName());

            // TODO: Log initial step assignment to audit trail
        }

        CaseEntity savedEntity = caseRepository.save(caseEntity);

        log.info("Assigned workflow ID: {} to case ID: {}", workflowId, caseId);
        return caseMapper.toModel(savedEntity);
    }

    /**
     * Gets step transitions for a case.
     *
     * @param caseId the case ID
     * @return the step transition DTO
     * @throws EntityNotFoundException if the case or workflow is not found
     */
    @Transactional(readOnly = true)
    public StepTransitionDTO getCaseStepTransitions(Long caseId) {
        log.debug("Getting step transitions for case ID: {}", caseId);

        CaseEntity caseEntity = getCaseEntityById(caseId);
        validateWorkflowAssigned(caseEntity);

        Long currentStepId = caseEntity.getCurrentStepId();
        if (currentStepId == null) {
            log.error("Case ID: {} does not have a current step", caseId);
            throw new IllegalStateException("Case does not have a current step");
        }

        // Get next transitions (where current step is source)
        List<WorkflowTransitionEntity> nextTransitions = workflowTransitionRepository
                .findByWorkflowWorkflowIdAndSourceStepWorkflowStepId(
                        caseEntity.getWorkflowId(),
                        currentStepId);

        // Get back transitions (where current step is target)
        List<WorkflowTransitionEntity> backTransitions = workflowTransitionRepository
                .findByWorkflowWorkflowIdAndTargetStepWorkflowStepId(
                        caseEntity.getWorkflowId(),
                        currentStepId);

        // Create StepTransitionDTO with next and back step info
        StepTransitionDTO transitionDTO = new StepTransitionDTO();

        // Map next steps to DTOs
        transitionDTO.setNextSteps(nextTransitions.stream()
                .map(transition -> {
                    com.dair.cais.workflow.dto.StepInfo stepInfo = new com.dair.cais.workflow.dto.StepInfo();
                    stepInfo.setStepId(transition.getTargetStep().getStep().getStepId());
                    stepInfo.setLabel(transition.getTargetStep().getLabel());
                    return stepInfo;
                })
                .collect(Collectors.toList()));

        // Map back steps to DTOs
        transitionDTO.setBackSteps(backTransitions.stream()
                .map(transition -> {
                    com.dair.cais.workflow.dto.StepInfo stepInfo = new com.dair.cais.workflow.dto.StepInfo();
                    stepInfo.setStepId(transition.getSourceStep().getStep().getStepId());
                    stepInfo.setLabel(transition.getSourceStep().getLabel());
                    return stepInfo;
                })
                .collect(Collectors.toList()));

        log.info("Found {} next steps and {} back steps for case ID: {}",
                transitionDTO.getNextSteps().size(),
                transitionDTO.getBackSteps().size(),
                caseId);

        return transitionDTO;
    }

    /**
     * Gets a case entity by ID.
     *
     * @param caseId the case ID
     * @return the case entity
     * @throws EntityNotFoundException if the case is not found
     */
    private CaseEntity getCaseEntityById(Long caseId) {
        return caseRepository.findById(caseId)
                .orElseThrow(() -> {
                    log.error("Case not found with ID: {}", caseId);
                    return new EntityNotFoundException("Case not found with ID: " + caseId);
                });
    }

    /**
     * Validates that a case has a workflow assigned.
     *
     * @param caseEntity the case entity
     * @throws IllegalStateException if no workflow is assigned
     */
    private void validateWorkflowAssigned(CaseEntity caseEntity) {
        if (caseEntity.getWorkflowId() == null) {
            log.error("Case ID: {} does not have a workflow assigned", caseEntity.getCaseId());
            throw new IllegalStateException("No workflow assigned to this case");
        }
    }

    /**
     * Validates that a transition between steps is allowed.
     *
     * @param workflowId    the workflow ID
     * @param sourceStepId  the source step ID
     * @param targetStepId  the target step ID
     * @throws IllegalStateException if the transition is not valid
     */
    private void validateTransition(Long workflowId, Long sourceStepId, Long targetStepId) {
        boolean validTransition = workflowTransitionRepository
                .findByWorkflowWorkflowIdAndSourceStepWorkflowStepId(workflowId, sourceStepId)
                .stream()
                .anyMatch(transition ->
                        transition.getTargetStep().getStep().getStepId().equals(targetStepId));

        if (!validTransition) {
            log.error("Invalid transition from step ID: {} to step ID: {} in workflow ID: {}",
                    sourceStepId, targetStepId, workflowId);
            throw new WorkflowValidationException(
                    "Invalid transition between steps in workflow: " + workflowId);
        }
    }

    /**
     * Assigns an initial step to a case.
     *
     * @param caseEntity the case entity
     * @param stepId     the step ID
     * @param reason     the reason for assignment
     * @param userId     the ID of the user making the assignment
     * @return the updated case
     * @throws EntityNotFoundException if the step is not found
     */
    private Case assignInitialStep(CaseEntity caseEntity, Long stepId, String reason, String userId) {
        // Get target step details
        Step targetStep = stepRepository.findById(stepId)
                .orElseThrow(() -> new EntityNotFoundException("Step not found with ID: " + stepId));

        // Update case with new step
        caseEntity.setCurrentStepId(stepId);
        caseEntity.setCurrentStepName(targetStep.getStepName());
        caseEntity.setUpdatedAt(LocalDateTime.now());

        // If step has a status mapping, update case status
        if (targetStep.getStepStatusId() != null) {
            // TODO: Implement status update logic based on step status
            // This would involve getting the status name from the step status repository
            // caseEntity.setStatus(statusName);
        }

        // Save updated case
        CaseEntity savedEntity = caseRepository.save(caseEntity);

        // TODO: Log initial step assignment to audit trail

        log.info("Assigned initial step ID: {} to case ID: {}",
                stepId, caseEntity.getCaseId());

        return caseMapper.toModel(savedEntity);
    }
}