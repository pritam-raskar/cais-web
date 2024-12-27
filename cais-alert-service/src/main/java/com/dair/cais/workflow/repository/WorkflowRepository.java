package com.dair.cais.workflow.repository;

import com.dair.cais.workflow.entity.WorkflowEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository for workflow operations.
 */
@Repository
public interface WorkflowRepository extends JpaRepository<WorkflowEntity, Long> {
    boolean existsByWorkflowName(String workflowName);
}