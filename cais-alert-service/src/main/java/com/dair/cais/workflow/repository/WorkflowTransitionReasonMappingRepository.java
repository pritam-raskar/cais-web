package com.dair.cais.workflow.repository;

import com.dair.cais.workflow.entity.WorkflowTransitionReasonMappingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkflowTransitionReasonMappingRepository extends JpaRepository<WorkflowTransitionReasonMappingEntity, Long> {
    List<WorkflowTransitionReasonMappingEntity> findByTransitionTransitionId(Long transitionId);

    void deleteByTransitionTransitionId(Long transitionId);
}
