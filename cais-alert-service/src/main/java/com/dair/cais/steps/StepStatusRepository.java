package com.dair.cais.steps;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StepStatusRepository extends JpaRepository<StepStatus, Integer> {
    StepStatus findByStepStatusId(Integer stepStatusId);
}