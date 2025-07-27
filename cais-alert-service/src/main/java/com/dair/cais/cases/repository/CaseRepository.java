package com.dair.cais.cases.repository;

import com.dair.cais.cases.entity.CaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Case entities.
 */
@Repository
public interface CaseRepository extends JpaRepository<CaseEntity, Long> {

    /**
     * Find a case by its case number.
     *
     * @param caseNumber the case number
     * @return the case entity if found
     */
    Optional<CaseEntity> findByCaseNumber(String caseNumber);

    /**
     * Find all cases owned by a specific user.
     *
     * @param ownerId the owner ID
     * @return a list of case entities
     */
    List<CaseEntity> findByOwnerId(String ownerId);

    /**
     * Find all cases for a specific organizational unit.
     *
     * @param orgUnitId the organization unit ID
     * @return a list of case entities
     */
    List<CaseEntity> findByOrgUnitId(String orgUnitId);

    /**
     * Find all cases with a specific status.
     *
     * @param status the case status
     * @return a list of case entities
     */
    List<CaseEntity> findByStatus(String status);

    /**
     * Find all cases of a specific type.
     *
     * @param caseType the case type
     * @return a list of case entities
     */
    List<CaseEntity> findByCaseType(String caseType);

    /**
     * Find all cases created within a date range.
     *
     * @param start the start date and time
     * @param end the end date and time
     * @return a list of case entities
     */
    List<CaseEntity> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}