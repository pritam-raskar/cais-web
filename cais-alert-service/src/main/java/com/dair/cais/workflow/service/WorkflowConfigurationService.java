package com.dair.cais.workflow.service;

import com.dair.cais.steps.StepRepository;
import com.dair.cais.workflow.model.*;
import com.dair.cais.workflow.entity.*;
import com.dair.cais.workflow.exception.WorkflowValidationException;
import com.dair.cais.workflow.repository.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowConfigurationService {
    private final WorkflowRepository workflowRepository;
    private final WorkflowStepRepository workflowStepRepository;
    private final WorkflowStepDeadlineRepository workflowStepDeadlineRepository;
    private final WorkflowTransitionRepository workflowTransitionRepository;
    private final WorkflowTransitionReasonMappingRepository transitionReasonMappingRepository;
    private final TransitionReasonRepository transitionReasonRepository;
    private final StepRepository stepRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveWorkflowConfiguration(WorkflowConfigurationDTO configDTO) {
        log.info("Saving workflow configuration for workflow ID: {}", configDTO.getWorkflowId());

        try {
            validateWorkflowConfiguration(configDTO);

            WorkflowEntity workflow = workflowRepository.findById(configDTO.getWorkflowId())
                    .orElseThrow(() -> new WorkflowValidationException("Workflow not found"));

            // Save steps
            Map<Long, WorkflowStepEntity> savedSteps = saveWorkflowSteps(workflow, configDTO.getSteps());

            // Save transitions
            saveWorkflowTransitions(workflow, configDTO.getTransitions(), savedSteps);

            log.info("Successfully saved workflow configuration for workflow ID: {}", configDTO.getWorkflowId());
        } catch (Exception e) {
            log.error("Failed to save workflow configuration for workflow ID: {}", configDTO.getWorkflowId(), e);
            throw new WorkflowValidationException("Failed to save workflow configuration", e);
        }
    }

    @Transactional(readOnly = true)
    public WorkflowConfigurationDTO getWorkflowConfiguration(Long workflowId) {
        log.info("Retrieving workflow configuration for workflow ID: {}", workflowId);

        try {
            WorkflowEntity workflow = workflowRepository.findById(workflowId)
                    .orElseThrow(() -> new WorkflowValidationException("Workflow not found"));

            // Get steps
            List<WorkflowStepEntity> steps = workflowStepRepository.findByWorkflowWorkflowId(workflowId);
            List<WorkflowStepDTO> stepDTOs = steps.stream()
                    .map(this::convertToStepDTO)
                    .collect(Collectors.toList());

            // Get transitions
            List<WorkflowTransitionEntity> transitions = workflowTransitionRepository.findByWorkflowWorkflowId(workflowId);
            List<WorkflowTransitionDTO> transitionDTOs = transitions.stream()
                    .map(this::convertToTransitionDTO)
                    .collect(Collectors.toList());

            WorkflowConfigurationDTO configDTO = new WorkflowConfigurationDTO();
            configDTO.setWorkflowId(workflowId);
            configDTO.setSteps(stepDTOs);
            configDTO.setTransitions(transitionDTOs);

            return configDTO;
        } catch (Exception e) {
            log.error("Failed to retrieve workflow configuration for workflow ID: {}", workflowId, e);
            throw new WorkflowValidationException("Failed to retrieve workflow configuration", e);
        }
    }

    private void validateWorkflowConfiguration(WorkflowConfigurationDTO configDTO) {
        log.debug("Validating workflow configuration");

        // Validate steps
        Set<Long> stepIds = new HashSet<>();
        for (WorkflowStepDTO step : configDTO.getSteps()) {
            if (!stepRepository.existsById(step.getStepId())) {
                throw new WorkflowValidationException("Invalid step ID: " + step.getStepId());
            }
            if (!stepIds.add(step.getStepId())) {
                throw new WorkflowValidationException("Duplicate step ID: " + step.getStepId());
            }
        }

        // Validate transitions
        for (WorkflowTransitionDTO transition : configDTO.getTransitions()) {
            if (!stepIds.contains(transition.getSource())) {
                throw new WorkflowValidationException("Invalid source step ID in transition: " + transition.getSource());
            }
            if (!stepIds.contains(transition.getTarget())) {
                throw new WorkflowValidationException("Invalid target step ID in transition: " + transition.getTarget());
            }

            // Validate transition reasons if present
            if (transition.getReasons() != null) {
                for (Long reasonId : transition.getReasons()) {
                    if (!transitionReasonRepository.existsById(reasonId)) {
                        throw new WorkflowValidationException("Invalid transition reason ID: " + reasonId);
                    }
                }
            }
        }
    }

    private Map<Long, WorkflowStepEntity> saveWorkflowSteps(WorkflowEntity workflow, List<WorkflowStepDTO> steps) {
        log.debug("Saving {} workflow steps", steps.size());

        // Delete existing steps and their related entities
        List<WorkflowStepEntity> existingSteps = workflowStepRepository.findByWorkflowWorkflowId(workflow.getWorkflowId());
        workflowStepRepository.deleteAll(existingSteps);

        // Save new steps
        Map<Long, WorkflowStepEntity> savedSteps = new HashMap<>();
        for (WorkflowStepDTO stepDTO : steps) {
            WorkflowStepEntity step = createWorkflowStep(workflow, stepDTO);
            step = workflowStepRepository.save(step);

            // Save deadline if present
            if (stepDTO.getProps() != null && stepDTO.getProps().getDeadline() != null) {
                saveWorkflowStepDeadline(step, stepDTO.getProps().getDeadline());
            }

            savedSteps.put(stepDTO.getStepId(), step);
        }

        return savedSteps;
    }



    private WorkflowStepEntity createWorkflowStep(WorkflowEntity workflow, WorkflowStepDTO stepDTO) {
        WorkflowStepEntity step = new WorkflowStepEntity();
        step.setWorkflow(workflow);
        step.setStep(stepRepository.getReferenceById(stepDTO.getStepId()));
        step.setPositionX(stepDTO.getPosition().getX());
        step.setPositionY(stepDTO.getPosition().getY());
        step.setLabel(stepDTO.getData().getLabel());
        step.setIsDefault(stepDTO.getProps().getIsDefault());
        step.setCreatedBy("SYSTEM"); // TODO: Get from security context
        return step;
    }

    private void saveWorkflowStepDeadline(WorkflowStepEntity step, WorkflowStepDeadlineDTO deadlineDTO) {
        log.debug("Processing deadline configuration for step ID: {}", step.getWorkflowStepId());

        // If deadlineDTO is null or not active, delete any existing deadline and return
        if (deadlineDTO == null || !deadlineDTO.getActive()) {
            Optional<WorkflowStepDeadlineEntity> existingDeadline =
                    workflowStepDeadlineRepository.findByWorkflowStepWorkflowStepId(step.getWorkflowStepId());

            if (existingDeadline.isPresent()) {
                log.debug("Deleting existing deadline for step ID: {} as deadline is not active",
                        step.getWorkflowStepId());
                workflowStepDeadlineRepository.delete(existingDeadline.get());
            }
            return;
        }

        try {
            WorkflowStepDeadlineEntity deadline = new WorkflowStepDeadlineEntity();
            deadline.setWorkflowStep(step);
            deadline.setIsActive(true);

            // Validate and set count value and measure
            if (deadlineDTO.getCount() != null && deadlineDTO.getMeasure() != null) {
                validateDeadlineMeasure(deadlineDTO.getMeasure());
                deadline.setCountValue(deadlineDTO.getCount());
                deadline.setMeasureUnit(deadlineDTO.getMeasure().toUpperCase());
            }

            // Handle email reminder settings
            if (deadlineDTO.getActions() != null &&
                    deadlineDTO.getActions().getSendEmailBeforeDeadline() != null) {

                WorkflowStepDeadlineEmailDTO emailDTO =
                        deadlineDTO.getActions().getSendEmailBeforeDeadline();

                deadline.setEmailReminderActive(emailDTO.getActive());
                if (emailDTO.getActive()) {
                    validateEmailReminderSettings(emailDTO);
                    deadline.setEmailReminderCount(emailDTO.getCount());
                    deadline.setEmailReminderMeasure(emailDTO.getMeasure().toUpperCase());
                }
            }

            // Handle step change settings
            if (deadlineDTO.getActions() != null &&
                    deadlineDTO.getActions().getChangeStepTo() != null) {

                WorkflowStepDeadlineChangeDTO changeDTO =
                        deadlineDTO.getActions().getChangeStepTo();

                deadline.setStepChangeActive(changeDTO.getActive());
                if (changeDTO.getActive() && changeDTO.getStepId() != null) {
                    validateTargetStep(changeDTO.getStepId());
                    deadline.setTargetStepId(changeDTO.getStepId());
                }
            }

            WorkflowStepDeadlineEntity savedDeadline =
                    workflowStepDeadlineRepository.save(deadline);
            log.debug("Saved deadline configuration with ID: {}", savedDeadline.getDeadlineId());

        } catch (Exception e) {
            log.error("Failed to save deadline configuration for step ID: {}",
                    step.getWorkflowStepId(), e);
            throw new WorkflowValidationException(
                    "Failed to save deadline configuration: " + e.getMessage(), e);
        }
    }

    private void validateDeadlineMeasure(String measure) {
        try {
            MeasureUnit.valueOf(measure.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new WorkflowValidationException(
                    "Invalid measure unit: " + measure + ". Allowed values are: " +
                            Arrays.toString(MeasureUnit.values()));
        }
    }

    private void validateEmailReminderSettings(WorkflowStepDeadlineEmailDTO emailDTO) {
        if (emailDTO.getCount() == null || emailDTO.getMeasure() == null) {
            throw new WorkflowValidationException(
                    "Email reminder count and measure must be provided when email reminder is active");
        }
        validateDeadlineMeasure(emailDTO.getMeasure());
    }

    private void validateTargetStep(Long stepId) {
        if (!stepRepository.existsById(stepId)) {
            throw new WorkflowValidationException(
                    "Target step with ID " + stepId + " does not exist");
        }
    }

    private void saveWorkflowTransitions(
            WorkflowEntity workflow,
            List<WorkflowTransitionDTO> transitions,
            Map<Long, WorkflowStepEntity> savedSteps) {

        log.debug("Saving {} workflow transitions", transitions.size());

        try {
            // Delete existing transitions and their reason mappings
            List<WorkflowTransitionEntity> existingTransitions =
                    workflowTransitionRepository.findByWorkflowWorkflowId(workflow.getWorkflowId());

            if (!existingTransitions.isEmpty()) {
                log.debug("Deleting {} existing transitions for workflow ID: {}",
                        existingTransitions.size(), workflow.getWorkflowId());
                workflowTransitionRepository.deleteAll(existingTransitions);
            }

            // Save new transitions
            LocalDateTime now = LocalDateTime.now();
            List<WorkflowTransitionEntity> newTransitions = new ArrayList<>();

            for (WorkflowTransitionDTO transitionDTO : transitions) {
                WorkflowStepEntity sourceStep = savedSteps.get(transitionDTO.getSource());
                WorkflowStepEntity targetStep = savedSteps.get(transitionDTO.getTarget());

                validateTransitionSteps(transitionDTO, sourceStep, targetStep);

                WorkflowTransitionEntity transition = new WorkflowTransitionEntity();
                transition.setWorkflow(workflow);
                transition.setSourceStep(sourceStep);
                transition.setTargetStep(targetStep);
                transition.setCreatedDate(now);
                transition.setUpdatedDate(now);

                // Handle transition properties
                if (transitionDTO.getProps() != null) {
                    transition.setAllowAutomaticTransition(
                            Optional.ofNullable(transitionDTO.getProps().getAllowAutomaticTransition())
                                    .orElse(false));
                    transition.setRequiredNote(
                            Optional.ofNullable(transitionDTO.getProps().getRequiredNote())
                                    .orElse(false));
                } else {
                    transition.setAllowAutomaticTransition(false);
                    transition.setRequiredNote(false);
                }

                WorkflowTransitionEntity savedTransition = workflowTransitionRepository.save(transition);
                log.debug("Saved transition with ID: {} from step {} to step {}",
                        savedTransition.getTransitionId(),
                        sourceStep.getWorkflowStepId(),
                        targetStep.getWorkflowStepId());

                // Save transition reasons if present
                if (transitionDTO.getReasons() != null && !transitionDTO.getReasons().isEmpty()) {
                    saveTransitionReasonMappings(savedTransition, transitionDTO.getReasons());
                }

                newTransitions.add(savedTransition);
            }

            log.info("Successfully saved {} transitions for workflow ID: {}",
                    newTransitions.size(), workflow.getWorkflowId());

        } catch (Exception e) {
            log.error("Failed to save workflow transitions for workflow ID: {}",
                    workflow.getWorkflowId(), e);
            throw new WorkflowValidationException("Failed to save workflow transitions", e);
        }
    }

    private void validateTransitionSteps(
            WorkflowTransitionDTO transitionDTO,
            WorkflowStepEntity sourceStep,
            WorkflowStepEntity targetStep) {

        if (sourceStep == null) {
            throw new WorkflowValidationException(
                    String.format("Invalid source step ID in transition: %d", transitionDTO.getSource()));
        }
        if (targetStep == null) {
            throw new WorkflowValidationException(
                    String.format("Invalid target step ID in transition: %d", transitionDTO.getTarget()));
        }
    }

    private void saveTransitionReasonMappings(
            WorkflowTransitionEntity transition,
            List<Long> reasonIds) {

        log.debug("Saving {} reason mappings for transition ID: {}",
                reasonIds.size(), transition.getTransitionId());

        try {
            List<WorkflowTransitionReasonMappingEntity> mappings = new ArrayList<>();

            for (Long reasonId : reasonIds) {
                // Validate reason existence
                if (!transitionReasonRepository.existsById(reasonId)) {
                    throw new WorkflowValidationException(
                            String.format("Invalid transition reason ID: %d", reasonId));
                }

                WorkflowTransitionReasonMappingEntity mapping =
                        new WorkflowTransitionReasonMappingEntity();
                mapping.setTransition(transition);
                mapping.setReason(transitionReasonRepository.getReferenceById(reasonId));
                mappings.add(mapping);
            }

            transitionReasonMappingRepository.saveAll(mappings);
            log.debug("Successfully saved {} reason mappings for transition ID: {}",
                    mappings.size(), transition.getTransitionId());

        } catch (Exception e) {
            log.error("Failed to save transition reason mappings for transition ID: {}",
                    transition.getTransitionId(), e);
            throw new WorkflowValidationException(
                    "Failed to save transition reason mappings", e);
        }
    }

    private WorkflowTransitionEntity createWorkflowTransition(
            WorkflowEntity workflow,
            WorkflowTransitionDTO transitionDTO,
            Map<Long, WorkflowStepEntity> savedSteps) {
        WorkflowTransitionEntity transition = new WorkflowTransitionEntity();
        transition.setWorkflow(workflow);
        transition.setSourceStep(savedSteps.get(transitionDTO.getSource()));
        transition.setTargetStep(savedSteps.get(transitionDTO.getTarget()));
        transition.setAllowAutomaticTransition(transitionDTO.getProps().getAllowAutomaticTransition());
        transition.setRequiredNote(transitionDTO.getProps().getRequiredNote());
        return transition;
    }

    private WorkflowStepDTO convertToStepDTO(WorkflowStepEntity entity) {
        WorkflowStepDTO dto = new WorkflowStepDTO();
        dto.setStepId(entity.getStep().getStepId());

        WorkflowStepPositionDTO position = new WorkflowStepPositionDTO();
        position.setX(entity.getPositionX());
        position.setY(entity.getPositionY());
        dto.setPosition(position);

        WorkflowStepDataDTO data = new WorkflowStepDataDTO();
        data.setLabel(entity.getLabel());
        dto.setData(data);

        WorkflowStepPropertiesDTO props = new WorkflowStepPropertiesDTO();
        props.setIsDefault(entity.getIsDefault());

        if (entity.getDeadline() != null) {
            props.setDeadline(convertToDeadlineDTO(entity.getDeadline()));
        }

        dto.setProps(props);

        return dto;
    }

    private WorkflowStepDeadlineDTO convertToDeadlineDTO(WorkflowStepDeadlineEntity entity) {
        WorkflowStepDeadlineDTO dto = new WorkflowStepDeadlineDTO();
        dto.setActive(entity.getIsActive());
        dto.setCount(entity.getCountValue());
        dto.setMeasure(entity.getMeasureUnit() != null ? entity.getMeasureUnit().toLowerCase() : null);

        WorkflowStepDeadlineActionsDTO actions = new WorkflowStepDeadlineActionsDTO();

        if (entity.getEmailReminderActive() != null) {
            WorkflowStepDeadlineEmailDTO email = new WorkflowStepDeadlineEmailDTO();
            email.setActive(entity.getEmailReminderActive());
            email.setCount(entity.getEmailReminderCount());
            email.setMeasure(entity.getEmailReminderMeasure() != null ?
                    entity.getEmailReminderMeasure().toLowerCase() : null);
            actions.setSendEmailBeforeDeadline(email);
        }

        if (entity.getStepChangeActive() != null) {
            WorkflowStepDeadlineChangeDTO change = new WorkflowStepDeadlineChangeDTO();
            change.setActive(entity.getStepChangeActive());
            change.setStepId(entity.getTargetStepId());
            actions.setChangeStepTo(change);
        }

        dto.setActions(actions);
        return dto;
    }

    private WorkflowTransitionDTO convertToTransitionDTO(WorkflowTransitionEntity entity) {
        WorkflowTransitionDTO dto = new WorkflowTransitionDTO();
        dto.setSource(entity.getSourceStep().getStep().getStepId());
        dto.setTarget(entity.getTargetStep().getStep().getStepId());

        WorkflowTransitionPropertiesDTO props = new WorkflowTransitionPropertiesDTO();
        props.setAllowAutomaticTransition(entity.getAllowAutomaticTransition());
        props.setRequiredNote(entity.getRequiredNote());
        dto.setProps(props);

        if (entity.getReasonMappings() != null) {
            dto.setReasons(entity.getReasonMappings().stream()
                    .map(mapping -> mapping.getReason().getId())
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    @Transactional
    public void parseAndSaveWorkflowConfiguration(Long workflowId, String uiConfig) {
        try {
            log.info("Parsing UI config for workflow ID: {}", workflowId);

            JsonNode jsonNode = objectMapper.readTree(uiConfig);
            WorkflowConfigurationDTO configDTO = new WorkflowConfigurationDTO();
            configDTO.setWorkflowId(workflowId);

            // Parse steps
            JsonNode stepsNode = jsonNode.get("steps");
            if (stepsNode != null && stepsNode.isArray()) {
                configDTO.setSteps(parseSteps(stepsNode));
            }

            // Parse transitions
            JsonNode transitionsNode = jsonNode.get("transitions");
            if (transitionsNode != null && transitionsNode.isArray()) {
                configDTO.setTransitions(parseTransitions(transitionsNode));
            }

            // Save the configuration
            saveWorkflowConfiguration(configDTO);

            log.info("Successfully parsed and saved workflow configuration for workflow ID: {}", workflowId);
        } catch (Exception e) {
            log.error("Error parsing workflow configuration for workflow ID: {}", workflowId, e);
            throw new WorkflowValidationException("Failed to parse workflow configuration", e);
        }
    }

    private List<WorkflowStepDTO> parseSteps(JsonNode stepsNode) {
        List<WorkflowStepDTO> steps = new ArrayList<>();
        stepsNode.forEach(stepNode -> {
            WorkflowStepDTO stepDTO = new WorkflowStepDTO();
            stepDTO.setStepId(stepNode.get("stepId").asLong());

            // Parse position
            JsonNode positionNode = stepNode.get("position");
            WorkflowStepPositionDTO position = new WorkflowStepPositionDTO();
            position.setX(positionNode.get("x").asInt());
            position.setY(positionNode.get("y").asInt());
            stepDTO.setPosition(position);

            // Parse data
            JsonNode dataNode = stepNode.get("data");
            WorkflowStepDataDTO data = new WorkflowStepDataDTO();
            data.setLabel(dataNode.get("label").asText());
            stepDTO.setData(data);

            // Parse properties
            JsonNode propsNode = stepNode.get("props");
            if (propsNode != null) {
                stepDTO.setProps(parseStepProperties(propsNode));
            }

            // Parse checklist
            JsonNode checklistNode = stepNode.get("checklist");
            if (checklistNode != null && checklistNode.isArray()) {
                List<Long> checklist = new ArrayList<>();
                checklistNode.forEach(item -> checklist.add(item.asLong()));
                stepDTO.setChecklist(checklist);
            }

            steps.add(stepDTO);
        });
        return steps;
    }

    private WorkflowStepPropertiesDTO parseStepProperties(JsonNode propsNode) {
        WorkflowStepPropertiesDTO props = new WorkflowStepPropertiesDTO();
        props.setIsDefault(propsNode.get("default").asBoolean());

        JsonNode deadlineNode = propsNode.get("deadline");
        if (deadlineNode != null) {
            WorkflowStepDeadlineDTO deadline = new WorkflowStepDeadlineDTO();
            deadline.setActive(deadlineNode.get("active").asBoolean());
            deadline.setCount(deadlineNode.has("count") ? deadlineNode.get("count").asInt() : null);
            deadline.setMeasure(deadlineNode.has("measure") ? deadlineNode.get("measure").asText() : null);

            JsonNode actionsNode = deadlineNode.get("actions");
            if (actionsNode != null) {
                WorkflowStepDeadlineActionsDTO actions = new WorkflowStepDeadlineActionsDTO();

                JsonNode emailNode = actionsNode.get("send_email_before_deadline");
                if (emailNode != null) {
                    WorkflowStepDeadlineEmailDTO email = new WorkflowStepDeadlineEmailDTO();
                    email.setActive(emailNode.get("active").asBoolean());
                    email.setCount(emailNode.has("count") ? emailNode.get("count").asInt() : null);
                    email.setMeasure(emailNode.has("measure") ? emailNode.get("measure").asText() : null);
                    actions.setSendEmailBeforeDeadline(email);
                }

                JsonNode changeStepNode = actionsNode.get("change_step_to");
                if (changeStepNode != null) {
                    WorkflowStepDeadlineChangeDTO changeStep = new WorkflowStepDeadlineChangeDTO();
                    changeStep.setActive(changeStepNode.get("active").asBoolean());
                    changeStep.setStepId(changeStepNode.has("step_id") ?
                            changeStepNode.get("step_id").asLong() : null);
                    actions.setChangeStepTo(changeStep);
                }

                deadline.setActions(actions);
            }
            props.setDeadline(deadline);
        }
        return props;
    }

    private List<WorkflowTransitionDTO> parseTransitions(JsonNode transitionsNode) {
        List<WorkflowTransitionDTO> transitions = new ArrayList<>();
        transitionsNode.forEach(transitionNode -> {
            WorkflowTransitionDTO transition = new WorkflowTransitionDTO();
            transition.setSource(transitionNode.get("source").asLong());
            transition.setTarget(transitionNode.get("target").asLong());

            // Parse properties
            JsonNode propsNode = transitionNode.get("props");
            if (propsNode != null) {
                WorkflowTransitionPropertiesDTO props = new WorkflowTransitionPropertiesDTO();
                props.setAllowAutomaticTransition(
                        propsNode.has("allow_automatic_transition") ?
                                propsNode.get("allow_automatic_transition").asBoolean() : false
                );
                props.setRequiredNote(
                        propsNode.has("required_note") ?
                                propsNode.get("required_note").asBoolean() : false
                );
                transition.setProps(props);
            }

            // Parse reasons
            JsonNode reasonsNode = transitionNode.get("reasons");
            if (reasonsNode != null && reasonsNode.isArray()) {
                List<Long> reasons = new ArrayList<>();
                reasonsNode.forEach(reason -> reasons.add(reason.asLong()));
                transition.setReasons(reasons);
            }

            transitions.add(transition);
        });
        return transitions;
    }
}