package com.dair.cais.workflow.repository;

import com.dair.cais.workflow.entity.WorkflowTransitionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WorkflowTransitionRepository extends JpaRepository<WorkflowTransitionEntity, Long> {
    List<WorkflowTransitionEntity> findByWorkflowWorkflowId(Long workflowId);

    Optional<WorkflowTransitionEntity> findByWorkflowWorkflowIdAndSourceStepWorkflowStepIdAndTargetStepWorkflowStepId(
            Long workflowId, Long sourceStepId, Long targetStepId);

    @Query("SELECT wt FROM WorkflowTransitionEntity wt " +
            "WHERE wt.workflow.workflowId = :workflowId " +
            "AND wt.sourceStep.workflowStepId = :stepId")
    List<WorkflowTransitionEntity> findByWorkflowWorkflowIdAndSourceStepWorkflowStepId(
            @Param("workflowId") Long workflowId,
            @Param("stepId") Long stepId);

    @Query("SELECT wt FROM WorkflowTransitionEntity wt " +
            "WHERE wt.workflow.workflowId = :workflowId " +
            "AND wt.targetStep.workflowStepId = :stepId")
    List<WorkflowTransitionEntity> findByWorkflowWorkflowIdAndTargetStepWorkflowStepId(
            @Param("workflowId") Long workflowId,
            @Param("stepId") Long stepId);
}
