package com.dair.cais.cases.casealert.service;

import com.dair.cais.alert.Alert;
import com.dair.cais.alert.AlertMapper;
import com.dair.cais.alert.AlertService;
import com.dair.cais.cases.Case;
import com.dair.cais.cases.casealert.entity.CaseAlertEntity;
import com.dair.cais.cases.casealert.mapper.CaseAlertMapper;
import com.dair.cais.cases.casealert.repository.CaseAlertRepository;
import com.dair.cais.cases.entity.CaseEntity;
import com.dair.cais.cases.mapper.CaseMapper;
import com.dair.cais.cases.repository.CaseRepository;
import com.dair.cais.cases.service.CaseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Service class for Case-Alert association operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseAlertService {

    private final CaseAlertRepository caseAlertRepository;
    private final CaseAlertMapper caseAlertMapper;
    private final CaseRepository caseRepository;
    private final CaseMapper caseMapper;
    private final AlertService alertService;
    private final AlertMapper alertMapper;
    private final CaseService caseService;

    /**
     * Adds an alert to a case.
     *
     * @param caseId  the case ID
     * @param alertId the alert ID
     * @param reason  the reason for adding the alert
     * @param userId  the ID of the user adding the alert
     * @throws EntityNotFoundException if the case or alert is not found
     * @throws IllegalArgumentException if the alert is already associated with the case
     */
    @Transactional
    public void addAlertToCase(Long caseId, String alertId, String reason, String userId) {
        log.debug("Adding alert ID: {} to case ID: {} with reason: {}", alertId, caseId, reason);

        // Validate case exists
        if (!caseRepository.existsById(caseId)) {
            log.error("Case not found with ID: {}", caseId);
            throw new EntityNotFoundException("Case not found with ID: " + caseId);
        }

        // Validate alert exists
        try {
            alertService.getAlertOnId(alertId);
        } catch (Exception e) {
            log.error("Alert not found with ID: {}", alertId);
            throw new EntityNotFoundException("Alert not found with ID: " + alertId);
        }

        // Check if association already exists
        if (!caseAlertRepository.findByCaseId(caseId).stream()
                .anyMatch(association -> association.getAlertId().equals(alertId))) {

            CaseAlertEntity association = new CaseAlertEntity();
            association.setCaseId(caseId);
            association.setAlertId(alertId);
            association.setAddedBy(userId);
            association.setAddedAt(LocalDateTime.now());
            association.setReason(reason);

            caseAlertRepository.save(association);
            log.info("Added alert ID: {} to case ID: {}", alertId, caseId);
        } else {
            log.warn("Alert ID: {} is already associated with case ID: {}", alertId, caseId);
            throw new IllegalArgumentException("Alert is already associated with this case");
        }
    }

    /**
     * Removes an alert from a case.
     *
     * @param caseId  the case ID
     * @param alertId the alert ID
     * @throws EntityNotFoundException if the association is not found
     */
    @Transactional
    public void removeAlertFromCase(Long caseId, String alertId) {
        log.debug("Removing alert ID: {} from case ID: {}", alertId, caseId);

        // Check if association exists
        boolean exists = caseAlertRepository.findByCaseId(caseId).stream()
                .anyMatch(association -> association.getAlertId().equals(alertId));

        if (!exists) {
            log.error("No association found between case ID: {} and alert ID: {}", caseId, alertId);
            throw new EntityNotFoundException("No association found between case and alert");
        }

        caseAlertRepository.deleteByCaseIdAndAlertId(caseId, alertId);
        log.info("Removed alert ID: {} from case ID: {}", alertId, caseId);
    }

    /**
     * Gets all alerts associated with a case.
     *
     * @param caseId the case ID
     * @return list of alerts associated with the case
     * @throws EntityNotFoundException if the case is not found
     */
    @Transactional(readOnly = true)
    public List<Alert> getAlertsForCase(Long caseId) {
        log.debug("Getting alerts for case ID: {}", caseId);

        // Validate case exists
        if (!caseRepository.existsById(caseId)) {
            log.error("Case not found with ID: {}", caseId);
            throw new EntityNotFoundException("Case not found with ID: " + caseId);
        }

        List<CaseAlertEntity> associations = caseAlertRepository.findByCaseId(caseId);
        List<Alert> alerts = new ArrayList<>();

        for (CaseAlertEntity association : associations) {
            try {
                Alert alert = alertService.getAlertOnId(association.getAlertId());
                alerts.add(alert);
            } catch (Exception e) {
                log.warn("Could not retrieve alert with ID: {}", association.getAlertId(), e);
                // Continue processing other alerts
            }
        }

        return alerts;
    }

    /**
     * Gets all cases associated with an alert.
     *
     * @param alertId the alert ID
     * @return list of cases associated with the alert
     * @throws EntityNotFoundException if the alert is not found
     */
    @Transactional(readOnly = true)
    public List<Case> getCasesForAlert(String alertId) {
        log.debug("Getting cases for alert ID: {}", alertId);

        // Validate alert exists
        try {
            alertService.getAlertOnId(alertId);
        } catch (Exception e) {
            log.error("Alert not found with ID: {}", alertId);
            throw new EntityNotFoundException("Alert not found with ID: " + alertId);
        }

        List<CaseAlertEntity> associations = caseAlertRepository.findByAlertId(alertId);
        List<Case> cases = new ArrayList<>();

        for (CaseAlertEntity association : associations) {
            try {
                CaseEntity caseEntity = caseRepository.findById(association.getCaseId())
                        .orElseThrow(() -> new EntityNotFoundException("Case not found with ID: " + association.getCaseId()));
                cases.add(caseMapper.toModel(caseEntity));
            } catch (Exception e) {
                log.warn("Could not retrieve case with ID: {}", association.getCaseId(), e);
                // Continue processing other cases
            }
        }

        return cases;
    }

    /**
     * Creates a new case from an alert.
     *
     * @param alertId   the alert ID
     * @param caseData  basic case data
     * @param userId    the ID of the user creating the case
     * @return the created case
     * @throws EntityNotFoundException if the alert is not found
     */
    @Transactional
    public Case createCaseFromAlert(String alertId, Case caseData, String userId) {
        log.debug("Creating case from alert ID: {}", alertId);

        // Get alert details
        Alert alert;
        try {
            alert = alertService.getAlertOnId(alertId);
        } catch (Exception e) {
            log.error("Alert not found with ID: {}", alertId);
            throw new EntityNotFoundException("Alert not found with ID: " + alertId);
        }

        // Populate case data from alert if not provided
        if (caseData.getTitle() == null || caseData.getTitle().isEmpty()) {
            caseData.setTitle("Case from Alert: " + alertId);
        }

        if (caseData.getDescription() == null || caseData.getDescription().isEmpty()) {
            caseData.setDescription("Case created from Alert ID: " + alertId);
        }

        // Copy relevant details from alert to case
        caseData.setOrgUnitId(alert.getOrgUnitId());
        caseData.setOrgFamily(alert.getOrgFamily());
        caseData.setCreatedBy(userId);

        // Create the case
        Case createdCase = caseService.createCase(caseData);

        // Associate the alert with the case
        CaseAlertEntity association = new CaseAlertEntity();
        association.setCaseId(createdCase.getCaseId());
        association.setAlertId(alertId);
        association.setAddedBy(userId);
        association.setAddedAt(LocalDateTime.now());
        association.setReason("Case created from this alert");

        caseAlertRepository.save(association);

        log.info("Created case ID: {} from alert ID: {}", createdCase.getCaseId(), alertId);
        return createdCase;
    }
}