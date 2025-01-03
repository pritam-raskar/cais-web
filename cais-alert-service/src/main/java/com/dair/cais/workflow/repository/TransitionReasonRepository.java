package com.dair.cais.workflow.repository;

import com.dair.cais.workflow.entity.TransitionReasonEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransitionReasonRepository extends JpaRepository<TransitionReasonEntity, Long> {
    boolean existsByReasonDetails(String reasonDetails);
}