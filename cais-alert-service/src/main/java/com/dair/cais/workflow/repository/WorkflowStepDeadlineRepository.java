package com.dair.cais.workflow.repository;

import com.dair.cais.workflow.entity.WorkflowStepDeadlineEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WorkflowStepDeadlineRepository extends JpaRepository<WorkflowStepDeadlineEntity, Long> {
    Optional<WorkflowStepDeadlineEntity> findByWorkflowStepWorkflowStepId(Long workflowStepId);
}
