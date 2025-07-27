package com.dair.cais.cases.service;

import com.dair.cais.cases.Case;
import com.dair.cais.cases.entity.CaseEntity;
import com.dair.cais.cases.entity.CaseTypeEntity;
import com.dair.cais.cases.mapper.CaseMapper;
import com.dair.cais.cases.repository.CaseRepository;
import com.dair.cais.cases.repository.CaseTypeRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Service class for Case management operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseService {

    private final CaseRepository caseRepository;
    private final CaseMapper caseMapper;
    private final CaseTypeRepository caseTypeRepository;

    /**
     * Creates a new case.
     *
     * @param caseData the case data to create
     * @return the created case
     */
    @Transactional
    public Case createCase(Case caseData) {
        log.debug("Creating new case: {}", caseData);

        validateCaseData(caseData);

        // Generate a unique case number if not provided
        if (caseData.getCaseNumber() == null || caseData.getCaseNumber().isEmpty()) {
            caseData.setCaseNumber(generateCaseNumber());
        }

        // Set default values for new case
        if (caseData.getStatus() == null) {
            caseData.setStatus("New");
        }
        if (caseData.getIsActive() == null) {
            caseData.setIsActive(true);
        }

        // If case type is provided but workflow ID is not, fetch the workflow ID from the case type
        if (caseData.getCaseType() != null && caseData.getWorkflowId() == null) {
            try {
                CaseTypeEntity caseTypeEntity = caseTypeRepository.findByName(caseData.getCaseType())
                        .orElseThrow(() -> new EntityNotFoundException("Case type not found with name: " + caseData.getCaseType()));

                // Set the workflow ID from the case type
                if (caseTypeEntity.getWorkflowId() != null) {
                    log.info("Automatically assigning workflow ID: {} from case type: {}",
                            caseTypeEntity.getWorkflowId(), caseData.getCaseType());
                    caseData.setWorkflowId(caseTypeEntity.getWorkflowId());
                } else {
                    log.warn("Case type '{}' does not have an associated workflow", caseData.getCaseType());
                }
            } catch (EntityNotFoundException e) {
                log.warn("Could not fetch workflow ID from case type: {}", e.getMessage());
                // Continue without setting workflow ID - don't fail the case creation
            } catch (Exception e) {
                log.error("Error while fetching workflow ID from case type", e);
                // Continue without setting workflow ID - don't fail the case creation
            }
        }

        LocalDateTime now = LocalDateTime.now();
        caseData.setCreatedAt(now);
        caseData.setUpdatedAt(now);

        CaseEntity caseEntity = caseMapper.toEntity(caseData);
        CaseEntity savedEntity = caseRepository.save(caseEntity);

        log.info("Created new case with ID: {}", savedEntity.getCaseId());
        return caseMapper.toModel(savedEntity);
    }

    /**
     * Retrieves a case by its ID.
     *
     * @param caseId the case ID
     * @return the case
     * @throws EntityNotFoundException if the case is not found
     */
    @Transactional(readOnly = true)
    public Case getCase(Long caseId) {
        log.debug("Fetching case with ID: {}", caseId);

        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> {
                    log.error("Case not found with ID: {}", caseId);
                    return new EntityNotFoundException("Case not found with ID: " + caseId);
                });

        return caseMapper.toModel(caseEntity);
    }

    /**
     * Retrieves a case by its case number.
     *
     * @param caseNumber the case number
     * @return the case
     * @throws EntityNotFoundException if the case is not found
     */
    @Transactional(readOnly = true)
    public Case getCaseByCaseNumber(String caseNumber) {
        log.debug("Fetching case with case number: {}", caseNumber);

        CaseEntity caseEntity = caseRepository.findByCaseNumber(caseNumber)
                .orElseThrow(() -> {
                    log.error("Case not found with case number: {}", caseNumber);
                    return new EntityNotFoundException("Case not found with case number: " + caseNumber);
                });

        return caseMapper.toModel(caseEntity);
    }

    /**
     * Retrieves all cases.
     *
     * @return the list of all cases
     */
    @Transactional(readOnly = true)
    public List<Case> getAllCases() {
        log.debug("Fetching all cases");

        List<CaseEntity> caseEntities = caseRepository.findAll();
        return caseMapper.toModelList(caseEntities);
    }

    /**
     * Finds cases based on search criteria.
     *
     * @param criteria the search criteria
     * @return the list of matching cases
     */
    @Transactional(readOnly = true)
    public List<Case> findCases(Map<String, Object> criteria) {
        log.debug("Finding cases with criteria: {}", criteria);

        // Build dynamic query based on criteria
        // This is a simplified example - in a real implementation,
        // you might use a more sophisticated approach like Specification API
        CaseEntity probe = new CaseEntity();

        if (criteria.containsKey("status")) {
            probe.setStatus((String) criteria.get("status"));
        }
        if (criteria.containsKey("caseType")) {
            probe.setCaseType((String) criteria.get("caseType"));
        }
        if (criteria.containsKey("ownerId")) {
            probe.setOwnerId((String) criteria.get("ownerId"));
        }
        if (criteria.containsKey("orgUnitId")) {
            probe.setOrgUnitId((String) criteria.get("orgUnitId"));
        }
        if (criteria.containsKey("isActive")) {
            probe.setIsActive((Boolean) criteria.get("isActive"));
        }

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING)
                .withIgnoreCase();

        Example<CaseEntity> example = Example.of(probe, matcher);
        List<CaseEntity> caseEntities = caseRepository.findAll(example);

        return caseMapper.toModelList(caseEntities);
    }

    /**
     * Updates an existing case.
     *
     * @param caseId the case ID
     * @param caseData the updated case data
     * @return the updated case
     * @throws EntityNotFoundException if the case is not found
     */
    @Transactional
    public Case updateCase(Long caseId, Case caseData) {
        log.debug("Updating case with ID: {}", caseId);

        CaseEntity existingEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> {
                    log.error("Case not found with ID: {}", caseId);
                    return new EntityNotFoundException("Case not found with ID: " + caseId);
                });

        // Update fields from caseData, but preserve certain fields
        CaseEntity updateEntity = caseMapper.toEntity(caseData);
        updateEntity.setCaseId(caseId);
        updateEntity.setCaseNumber(existingEntity.getCaseNumber()); // Preserve case number
        updateEntity.setCreatedAt(existingEntity.getCreatedAt()); // Preserve creation date
        updateEntity.setCreatedBy(existingEntity.getCreatedBy()); // Preserve creator
        updateEntity.setUpdatedAt(LocalDateTime.now()); // Set update timestamp

        CaseEntity savedEntity = caseRepository.save(updateEntity);

        log.info("Updated case with ID: {}", caseId);
        return caseMapper.toModel(savedEntity);
    }

    /**
     * Deletes a case.
     *
     * @param caseId the case ID
     * @throws EntityNotFoundException if the case is not found
     */
    @Transactional
    public void deleteCase(Long caseId) {
        log.debug("Deleting case with ID: {}", caseId);

        // Check if case exists
        if (!caseRepository.existsById(caseId)) {
            log.error("Case not found with ID: {}", caseId);
            throw new EntityNotFoundException("Case not found with ID: " + caseId);
        }

        caseRepository.deleteById(caseId);
        log.info("Deleted case with ID: {}", caseId);
    }

    /**
     * Changes the status of a case.
     *
     * @param caseId the case ID
     * @param newStatus the new status
     * @param reason the reason for status change
     * @return the updated case
     * @throws EntityNotFoundException if the case is not found
     */
    @Transactional
    public Case changeStatus(Long caseId, String newStatus, String reason) {
        log.debug("Changing status of case ID: {} to {} with reason: {}", caseId, newStatus, reason);

        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> {
                    log.error("Case not found with ID: {}", caseId);
                    return new EntityNotFoundException("Case not found with ID: " + caseId);
                });

        String oldStatus = caseEntity.getStatus();
        caseEntity.setStatus(newStatus);
        caseEntity.setUpdatedAt(LocalDateTime.now());

        // If status is changed to "Closed", set closedAt timestamp
        if ("Closed".equalsIgnoreCase(newStatus) && !"Closed".equalsIgnoreCase(oldStatus)) {
            caseEntity.setClosedAt(LocalDateTime.now());
        }

        CaseEntity savedEntity = caseRepository.save(caseEntity);

        // TODO: Log the status change to audit history with the reason

        log.info("Changed status of case ID: {} from {} to {}", caseId, oldStatus, newStatus);
        return caseMapper.toModel(savedEntity);
    }

    /**
     * Assigns a case to a user.
     *
     * @param caseId the case ID
     * @param userId the user ID
     * @return the updated case
     * @throws EntityNotFoundException if the case is not found
     */
    @Transactional
    public Case assignCase(Long caseId, String userId) {
        log.debug("Assigning case ID: {} to user ID: {}", caseId, userId);

        CaseEntity caseEntity = caseRepository.findById(caseId)
                .orElseThrow(() -> {
                    log.error("Case not found with ID: {}", caseId);
                    return new EntityNotFoundException("Case not found with ID: " + caseId);
                });

        // TODO: Get user details from user service to set owner name
        // For now, just setting the ID
        caseEntity.setOwnerId(userId);
        caseEntity.setUpdatedAt(LocalDateTime.now());

        CaseEntity savedEntity = caseRepository.save(caseEntity);

        log.info("Assigned case ID: {} to user ID: {}", caseId, userId);
        return caseMapper.toModel(savedEntity);
    }

    /**
     * Validates case data.
     *
     * @param caseData the case data to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateCaseData(Case caseData) {
        if (caseData == null) {
            throw new IllegalArgumentException("Case data cannot be null");
        }

        if (caseData.getTitle() == null || caseData.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Case title is required");
        }

        // Add more validation as needed
    }

    /**
     * Generates a unique case number.
     *
     * @return a unique case number
     */
    private String generateCaseNumber() {
        // Generate a case number format: CASE-yyyyMMdd-XXXX
        String datePart = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String randomPart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return "CASE-" + datePart + "-" + randomPart;
    }
}