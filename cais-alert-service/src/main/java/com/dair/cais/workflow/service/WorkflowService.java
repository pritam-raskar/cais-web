package com.dair.cais.workflow.service;

import com.dair.cais.workflow.entity.WorkflowEntity;
import com.dair.cais.workflow.exception.WorkflowAlreadyExistsException;
import com.dair.cais.workflow.mapper.WorkflowMapper;
import com.dair.cais.workflow.model.Workflow;
import com.dair.cais.workflow.model.WorkflowDetailDTO;
import com.dair.cais.workflow.repository.WorkflowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;

import java.time.LocalDateTime;
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
    private final JdbcTemplate jdbcTemplate;

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

        // Set creation timestamp if not provided
        if (workflow.getCreatedDate() == null) {
            workflow.setCreatedDate(LocalDateTime.now());
        }
        workflow.setUpdatedDate(LocalDateTime.now());

        WorkflowEntity entity = workflowMapper.toEntity(workflow);
        entity = workflowRepository.save(entity);

        log.info("Successfully created workflow with id: {}", entity.getWorkflowId());
        return workflowMapper.toModel(entity);
    }

    /**
     * Retrieves workflow details from cm_workflow table.
     *
     * @param workflowId the workflow ID to fetch details for
     * @return List of workflow details
     * @throws EntityNotFoundException if workflow doesn't exist
     */
    @Transactional(readOnly = true)
    public List<WorkflowDetailDTO> getWorkflowDetails(Long workflowId) {
        log.debug("Fetching workflow details for workflow id: {}", workflowId);

        // First verify the workflow exists
        if (!workflowRepository.existsById(workflowId)) {
            log.error("Workflow not found with id: {}", workflowId);
            throw new EntityNotFoundException("Workflow not found with id: " + workflowId);
        }

        String sql = """
            SELECT workflow_id,
                   workflow_name,
                   description,
                   created_by,
                   created_date,
                   updated_date,
                   updated_by
            FROM info_alert.cm_workflow
            WHERE workflow_id = ?
        """;

        try {
            List<WorkflowDetailDTO> details = jdbcTemplate.query(
                    sql,
                    (rs, rowNum) -> {
                        WorkflowDetailDTO dto = new WorkflowDetailDTO();
                        dto.setWorkflowId(rs.getLong("workflow_id"));
                        dto.setWorkflowName(rs.getString("workflow_name"));
                        dto.setDescription(rs.getString("description"));
                        dto.setCreatedBy(rs.getString("created_by"));
                        dto.setCreatedDate(rs.getTimestamp("created_date") != null ?
                                rs.getTimestamp("created_date").toLocalDateTime() : null);
                        dto.setUpdatedDate(rs.getTimestamp("updated_date") != null ?
                                rs.getTimestamp("updated_date").toLocalDateTime() : null);
                        dto.setUpdatedBy(rs.getString("updated_by"));
                        return dto;
                    },
                    workflowId
            );

            log.debug("Found {} workflow details for workflow id: {}", details.size(), workflowId);
            return details;

        } catch (DataAccessException e) {
            log.error("Error fetching workflow details for workflow id: {}", workflowId, e);
            throw new RuntimeException("Failed to fetch workflow details: " + e.getMessage(), e);
        }
    }

    /**
     * Updates an existing workflow.
     *
     * @param workflowId ID of the workflow to update
     * @param workflow Updated workflow data
     * @return Updated workflow
     * @throws EntityNotFoundException if workflow not found
     */
    @Transactional
    public Workflow updateWorkflow(Long workflowId, Workflow workflow) {
        log.debug("Updating workflow with id: {} with data: {}", workflowId, workflow);

        WorkflowEntity existingEntity = workflowRepository.findById(workflowId)
                .orElseThrow(() -> {
                    log.error("Workflow not found with id: {}", workflowId);
                    return new EntityNotFoundException("Workflow not found with id: " + workflowId);
                });

        // Check if new name conflicts with existing workflow (excluding current workflow)
        if (!existingEntity.getWorkflowName().equals(workflow.getWorkflowName()) &&
                workflowRepository.existsByWorkflowName(workflow.getWorkflowName())) {
            log.warn("Cannot update workflow. Name '{}' already exists", workflow.getWorkflowName());
            throw new WorkflowAlreadyExistsException(
                    String.format("Workflow with name '%s' already exists", workflow.getWorkflowName())
            );
        }

        WorkflowEntity updatedEntity = workflowMapper.toEntity(workflow);
        updatedEntity.setWorkflowId(workflowId); // Ensure ID is preserved
        updatedEntity = workflowRepository.save(updatedEntity);

        log.info("Successfully updated workflow with id: {}", workflowId);
        return workflowMapper.toModel(updatedEntity);
    }

    /**
     * Deletes a workflow by ID.
     *
     * @param workflowId ID of the workflow to delete
     * @throws EntityNotFoundException if workflow not found
     */
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

    /**
     * Validates workflow data before creation or update.
     *
     * @param workflow Workflow to validate
     * @throws IllegalArgumentException if validation fails
     */
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

        log.debug("Workflow validation successful");
    }

    /**
     * Checks if a workflow exists by ID.
     *
     * @param workflowId ID to check
     * @return true if workflow exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean workflowExists(Long workflowId) {
        boolean exists = workflowRepository.existsById(workflowId);
        log.debug("Checking if workflow exists with id {}: {}", workflowId, exists);
        return exists;
    }

    /**
     * Retrieves workflow count.
     *
     * @return Total number of workflows
     */
    @Transactional(readOnly = true)
    public long getWorkflowCount() {
        long count = workflowRepository.count();
        log.debug("Retrieved total workflow count: {}", count);
        return count;
    }
}