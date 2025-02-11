package com.dair.cais.workflow.repository;

import com.dair.cais.workflow.entity.WorkflowStepEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkflowStepRepository extends JpaRepository<WorkflowStepEntity, Long> {
    List<WorkflowStepEntity> findByWorkflowWorkflowId(Long workflowId);
}

