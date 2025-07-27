package com.dair.cais.cases.casealert.repository;

import com.dair.cais.cases.casealert.entity.CaseAlertEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Case-Alert associations.
 */
@Repository
public interface CaseAlertRepository extends JpaRepository<CaseAlertEntity, Long> {

    /**
     * Find all alert associations for a specific case.
     *
     * @param caseId the case ID
     * @return list of case-alert associations
     */
    List<CaseAlertEntity> findByCaseId(Long caseId);

    /**
     * Find all case associations for a specific alert.
     *
     * @param alertId the alert ID
     * @return list of case-alert associations
     */
    List<CaseAlertEntity> findByAlertId(String alertId);

    /**
     * Delete the association between a case and an alert.
     *
     * @param caseId the case ID
     * @param alertId the alert ID
     */
    void deleteByCaseIdAndAlertId(Long caseId, String alertId);
}