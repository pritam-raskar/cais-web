package com.dair.cais.workflow.service;

import com.dair.cais.workflow.entity.WorkflowEntity;
import com.dair.cais.workflow.exception.WorkflowAlreadyExistsException;
import com.dair.cais.workflow.exception.WorkflowUpdateException;
import com.dair.cais.workflow.mapper.WorkflowMapper;
import com.dair.cais.workflow.model.Workflow;
import com.dair.cais.workflow.model.WorkflowDetailDTO;
import com.dair.cais.workflow.repository.WorkflowRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service for handling workflow operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final WorkflowMapper workflowMapper;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public List<Workflow> getAllWorkflows() {
        log.debug("Fetching all workflows");
        List<WorkflowEntity> entities = workflowRepository.findAll();
        return workflowMapper.toModelList(entities);
    }

    @Transactional(readOnly = true)
    public Workflow getWorkflowById(Long workflowId) {
        log.debug("Fetching workflow with id: {}", workflowId);
        Optional<WorkflowEntity> entity = workflowRepository.findById(workflowId);
        return entity.map(workflowMapper::toModel).orElse(null);
    }

    @Transactional
    public Workflow createWorkflow(Workflow workflow) {
        log.debug("Creating new workflow: {}", workflow);

        validateWorkflow(workflow);

        if (workflow.getCreatedDate() == null) {
            workflow.setCreatedDate(LocalDateTime.now());
        }
        workflow.setUpdatedDate(LocalDateTime.now());

        WorkflowEntity entity = workflowMapper.toEntity(workflow);
        entity = workflowRepository.save(entity);

        log.info("Successfully created workflow with id: {}", entity.getWorkflowId());
        return workflowMapper.toModel(entity);
    }

    @Transactional(readOnly = true)
    public List<WorkflowDetailDTO> getWorkflowDetails(Long workflowId) {
        log.debug("Fetching workflow details for workflow id: {}", workflowId);

        return workflowRepository.findById(workflowId)
                .map(entity -> {
                    WorkflowDetailDTO dto = new WorkflowDetailDTO();
                    dto.setWorkflowId(entity.getWorkflowId());
                    dto.setWorkflowName(entity.getWorkflowName());
                    dto.setDescription(entity.getDescription());
                    dto.setCreatedBy(entity.getCreatedBy());
                    dto.setCreatedDate(entity.getCreatedDate());
                    dto.setUpdatedDate(entity.getUpdatedDate());
                    dto.setUpdatedBy(entity.getUpdatedBy());
                    return Collections.singletonList(dto);
                })
                .orElseThrow(() -> {
                    log.error("Workflow not found with id: {}", workflowId);
                    return new EntityNotFoundException("Workflow not found with id: " + workflowId);
                });
    }

    @Transactional
    public Workflow updateWorkflow(Long workflowId, Workflow workflow) {
        log.debug("Updating workflow with id: {} with data: {}", workflowId, workflow);

        WorkflowEntity existingEntity = workflowRepository.findById(workflowId)
                .orElseThrow(() -> {
                    log.error("Workflow not found with id: {}", workflowId);
                    return new EntityNotFoundException("Workflow not found with id: " + workflowId);
                });

        if (!existingEntity.getWorkflowName().equals(workflow.getWorkflowName()) &&
                workflowRepository.existsByWorkflowName(workflow.getWorkflowName())) {
            log.warn("Cannot update workflow. Name '{}' already exists", workflow.getWorkflowName());
            throw new WorkflowAlreadyExistsException(
                    String.format("Workflow with name '%s' already exists", workflow.getWorkflowName())
            );
        }

        WorkflowEntity updatedEntity = workflowMapper.toEntity(workflow);
        updatedEntity.setWorkflowId(workflowId);
        updatedEntity = workflowRepository.save(updatedEntity);

        log.info("Successfully updated workflow with id: {}", workflowId);
        return workflowMapper.toModel(updatedEntity);
    }

    @Transactional
    public void deleteWorkflow(Long workflowId) {
        log.debug("Deleting workflow with id: {}", workflowId);

        if (!workflowRepository.existsById(workflowId)) {
            log.error("Cannot delete workflow. Workflow not found with id: {}", workflowId);
            throw new EntityNotFoundException("Workflow not found with id: " + workflowId);
        }

        workflowRepository.deleteById(workflowId);
        log.info("Successfully deleted workflow with id: {}", workflowId);
    }

    @Transactional(readOnly = true)
    public boolean workflowExists(Long workflowId) {
        boolean exists = workflowRepository.existsById(workflowId);
        log.debug("Checking if workflow exists with id {}: {}", workflowId, exists);
        return exists;
    }

    @Transactional(readOnly = true)
    public long getWorkflowCount() {
        long count = workflowRepository.count();
        log.debug("Retrieved total workflow count: {}", count);
        return count;
    }

    @Transactional(readOnly = true)
    public String getWorkflowUiConfig(Long workflowId) {
        log.debug("Fetching UI configuration for workflow with id: {}", workflowId);

        return workflowRepository.findById(workflowId)
                .map(entity -> {
                    String uiConfig = entity.getUiConfig();
                    return uiConfig != null ? uiConfig : "{}";
                })
                .orElseThrow(() -> {
                    log.error("Workflow not found with id: {}", workflowId);
                    return new EntityNotFoundException("Workflow not found with id: " + workflowId);
                });
    }

    /**
     * Updates the UI configuration for a workflow.
     *
     * @param workflowId ID of the workflow to update
     * @param uiConfig New UI configuration as text
     * @return Updated workflow
     * @throws EntityNotFoundException if workflow not found
     * @throws WorkflowUpdateException if update fails
     */
    @Transactional
    public Workflow updateWorkflowUiConfig(Long workflowId, String uiConfig) {
        log.debug("Updating UI configuration for workflow with id: {}", workflowId);

        try {
            WorkflowEntity existingEntity = workflowRepository.findById(workflowId)
                    .orElseThrow(() -> new EntityNotFoundException("Workflow not found with id: " + workflowId));

            existingEntity.setUiConfig(uiConfig);
            existingEntity.setUpdatedDate(LocalDateTime.now());

            WorkflowEntity updatedEntity = workflowRepository.save(existingEntity);
            log.info("Successfully updated UI configuration for workflow with id: {}", workflowId);

            return workflowMapper.toModel(updatedEntity);

        } catch (EntityNotFoundException e) {
            log.error("Workflow not found with id: {}", workflowId);
            throw e;
        } catch (Exception e) {
            log.error("Failed to update UI configuration for workflow with id: {}", workflowId, e);
            throw new WorkflowUpdateException("Failed to update workflow UI configuration", e);
        }
    }

    private void validateWorkflow(Workflow workflow) {
        log.debug("Validating workflow data: {}", workflow);

        if (workflow == null) {
            throw new IllegalArgumentException("Workflow cannot be null");
        }

        if (workflow.getWorkflowName() == null || workflow.getWorkflowName().trim().isEmpty()) {
            throw new IllegalArgumentException("Workflow name cannot be null or empty");
        }

        if (workflow.getCreatedBy() == null || workflow.getCreatedBy().trim().isEmpty()) {
            throw new IllegalArgumentException("Created by cannot be null or empty");
        }

        if (workflow.getUiConfig() != null) {
            try {
                JsonNode jsonNode = objectMapper.readTree(workflow.getUiConfig());
                validateUiConfigStructure(jsonNode);
            } catch (Exception e) {
                log.error("Invalid UI configuration format: {}", e.getMessage());
                throw new IllegalArgumentException("Invalid UI configuration format: " + e.getMessage());
            }
        }

        log.debug("Workflow validation successful");
    }

    private void validateUiConfigStructure(JsonNode jsonNode) {
        if (!jsonNode.has("steps") || !jsonNode.has("transitions")) {
            throw new IllegalArgumentException("UI configuration must contain 'steps' and 'transitions' arrays");
        }

        if (!jsonNode.get("steps").isArray()) {
            throw new IllegalArgumentException("Steps must be an array");
        }

        if (!jsonNode.get("transitions").isArray()) {
            throw new IllegalArgumentException("Transitions must be an array");
        }

        jsonNode.get("steps").forEach(step -> {
            if (!step.has("stepId") || !step.has("position") || !step.has("data")) {
                throw new IllegalArgumentException("Each step must contain stepId, position, and data fields");
            }
        });

        jsonNode.get("transitions").forEach(transition -> {
            if (!transition.has("id") || !transition.has("source") || !transition.has("target")) {
                throw new IllegalArgumentException("Each transition must contain id, source, and target fields");
            }
        });
    }
}