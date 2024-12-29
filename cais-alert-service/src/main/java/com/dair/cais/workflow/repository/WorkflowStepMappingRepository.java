package com.dair.cais.workflow.repository;

import com.dair.cais.workflow.entity.WorkflowStepMappingEntity;
import com.dair.cais.workflow.model.WorkflowDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowStepMappingRepository extends JpaRepository<WorkflowStepMappingEntity, Integer> {
    @Query("SELECT new com.dair.cais.workflow.model.WorkflowDetail(" +
            "w.workflowId, w.workflowName) " +
            "FROM WorkflowEntity w " +
            "JOIN WorkflowStepMappingEntity wsm ON w.workflowId = wsm.workflow.workflowId " +
            "WHERE wsm.step.stepId = :stepId")
    List<WorkflowDetail> findWorkflowDetailsByStepId(@Param("stepId") Long stepId);
}
