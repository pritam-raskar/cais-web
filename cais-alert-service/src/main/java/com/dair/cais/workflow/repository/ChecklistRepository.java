package com.dair.cais.workflow.repository;

import com.dair.cais.workflow.entity.ChecklistEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChecklistRepository extends JpaRepository<ChecklistEntity, Long> {
    boolean existsByListName(String listName);
}