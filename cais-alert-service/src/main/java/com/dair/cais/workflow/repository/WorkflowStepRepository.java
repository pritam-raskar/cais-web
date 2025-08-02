package com.dair.cais.workflow.repository;

import com.dair.cais.workflow.entity.WorkflowStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStepEntity, Long> {
    List<WorkflowStepEntity> findByWorkflowWorkflowId(Long workflowId);
    Optional<WorkflowStepEntity> findByWorkflowWorkflowIdAndStepStepId(Long workflowId, Long stepId);
    Optional<WorkflowStepEntity> findByWorkflowStepId(Long workflowStepId);
}

