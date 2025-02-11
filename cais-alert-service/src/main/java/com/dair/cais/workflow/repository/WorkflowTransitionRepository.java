package com.dair.cais.workflow.repository;

import com.dair.cais.workflow.entity.WorkflowTransitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransitionEntity, Long> {
    List<WorkflowTransitionEntity> findByWorkflowWorkflowId(Long workflowId);

    Optional<WorkflowTransitionEntity> findByWorkflowWorkflowIdAndSourceStepWorkflowStepIdAndTargetStepWorkflowStepId(
            Long workflowId, Long sourceStepId, Long targetStepId);
}
