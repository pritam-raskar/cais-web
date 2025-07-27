package com.dair.cais.cases.repository;

import com.dair.cais.cases.entity.CaseTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Case Type entities.
 */
@Repository
public interface CaseTypeRepository extends JpaRepository<CaseTypeEntity, Long> {

    /**
     * Find a case type by its name.
     *
     * @param name the case type name
     * @return the case type entity if found
     */
    Optional<CaseTypeEntity> findByName(String name);

    /**
     * Find all active case types.
     *
     * @return a list of active case type entities
     */
    List<CaseTypeEntity> findByIsActiveTrue();
}