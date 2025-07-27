package com.dair.cais.cases.audit.service;

import com.dair.cais.audit.AuditLogRequest;
import com.dair.cais.audit.AuditTrail;
import com.dair.cais.audit.AuditTrailDetailsDTO;
import com.dair.cais.audit.AuditTrailService;
import com.dair.cais.cases.repository.CaseRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for case audit integration.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseAuditService {

    private final AuditTrailService auditTrailService;
    private final CaseRepository caseRepository;

    /**
     * Get case history from audit trail.
     *
     * @param caseId the case ID
     * @return list of audit trail entries
     * @throws EntityNotFoundException if the case is not found
     */
    @Transactional(readOnly = true)
    public List<AuditTrail> getCaseHistory(Long caseId) {
        log.debug("Getting history for case ID: {}", caseId);

        // Validate case exists
        if (!caseRepository.existsById(caseId)) {
            log.error("Case not found with ID: {}", caseId);
            throw new EntityNotFoundException("Case not found with ID: " + caseId);
        }

        // Use existing AuditTrailService to get case history
        // Assuming the audit system uses "Case" as the affectedItemType and caseId as the affectedItemId
        List<AuditTrail> auditTrails = auditTrailService.getAuditTrailByItem("Case", caseId.toString());

        log.info("Retrieved {} audit trail entries for case ID: {}", auditTrails.size(), caseId);
        return auditTrails;
    }

    /**
     * Get case history from audit trail within a date range.
     *
     * @param caseId    the case ID
     * @param startDate the start date
     * @param endDate   the end date
     * @return list of audit trail entries
     * @throws EntityNotFoundException if the case is not found
     * @throws IllegalArgumentException if date range is invalid
     */
    @Transactional(readOnly = true)
    public List<AuditTrail> getCaseHistory(Long caseId, LocalDateTime startDate, LocalDateTime endDate) {
        log.debug("Getting history for case ID: {} between {} and {}", caseId, startDate, endDate);

        // Validate case exists
        if (!caseRepository.existsById(caseId)) {
            log.error("Case not found with ID: {}", caseId);
            throw new EntityNotFoundException("Case not found with ID: " + caseId);
        }

        // Validate date range
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            log.error("Invalid date range: end date {} is before start date {}", endDate, startDate);
            throw new IllegalArgumentException("End date cannot be before start date");
        }

        // Since the existing audit service doesn't have a direct method for filtering by date range and item,
        // we'll get all audit entries for the case and filter them in-memory
        List<AuditTrail> allAuditTrails = auditTrailService.getAuditTrailByItem("Case", caseId.toString());

        // Filter by date range
        if (startDate != null && endDate != null) {
            ZonedDateTime startZoned = ZonedDateTime.of(startDate, ZoneId.systemDefault());
            ZonedDateTime endZoned = ZonedDateTime.of(endDate, ZoneId.systemDefault());

            return allAuditTrails.stream()
                    .filter(trail -> !trail.getActionTimestamp().isBefore(startZoned) &&
                            !trail.getActionTimestamp().isAfter(endZoned))
                    .collect(Collectors.toList());
        } else if (startDate != null) {
            ZonedDateTime startZoned = ZonedDateTime.of(startDate, ZoneId.systemDefault());

            return allAuditTrails.stream()
                    .filter(trail -> !trail.getActionTimestamp().isBefore(startZoned))
                    .collect(Collectors.toList());
        } else if (endDate != null) {
            ZonedDateTime endZoned = ZonedDateTime.of(endDate, ZoneId.systemDefault());

            return allAuditTrails.stream()
                    .filter(trail -> !trail.getActionTimestamp().isAfter(endZoned))
                    .collect(Collectors.toList());
        }

        return allAuditTrails;
    }

    /**
     * Log a case event to the audit trail.
     *
     * @param caseId   the case ID
     * @param action   the action performed
     * @param userId   the ID of the user performing the action
     * @param details  the details of the action
     * @param oldValue the old value (optional)
     * @param newValue the new value (optional)
     * @throws EntityNotFoundException if the case is not found
     */
    @Transactional
    public void logCaseEvent(Long caseId, String action, String userId, String details,
                             String oldValue, String newValue) {
        log.debug("Logging event for case ID: {}, action: {}, user: {}", caseId, action, userId);

        // Validate case exists
        if (!caseRepository.existsById(caseId)) {
            log.error("Case not found with ID: {}", caseId);
            throw new EntityNotFoundException("Case not found with ID: " + caseId);
        }

        // Create audit log request for existing AuditTrailService
        AuditLogRequest auditLogRequest = new AuditLogRequest();
        auditLogRequest.setUserId(Long.valueOf(userId));
        auditLogRequest.setUserRole("User"); // Default role, this should be determined by user service
        auditLogRequest.setActionId(getActionIdForCaseEvent(action)); // Map to appropriate action ID
        auditLogRequest.setDescription(details);
        auditLogRequest.setCategory("Case");
        auditLogRequest.setAffectedItemType("Case");
        auditLogRequest.setAffectedItemId(caseId.toString());
        auditLogRequest.setOldValue(oldValue);
        auditLogRequest.setNewValue(newValue);

        // Log the event using existing service
        auditTrailService.logAction(
                auditLogRequest.getUserId(),
                auditLogRequest.getUserRole(),
                auditLogRequest.getActionId(),
                auditLogRequest.getDescription(),
                auditLogRequest.getCategory(),
                auditLogRequest.getAffectedItemType(),
                auditLogRequest.getAffectedItemId(),
                auditLogRequest.getOldValue(),
                auditLogRequest.getNewValue()
        );

        log.info("Logged case event for case ID: {}, action: {}", caseId, action);
    }

    /**
     * Simplified version of logCaseEvent without old and new values.
     *
     * @param caseId  the case ID
     * @param action  the action performed
     * @param userId  the ID of the user performing the action
     * @param details the details of the action
     */
    @Transactional
    public void logCaseEvent(Long caseId, String action, String userId, String details) {
        logCaseEvent(caseId, action, userId, details, null, null);
    }

    /**
     * Map case event action to existing action ID.
     * This would need to be updated based on your action IDs in the system.
     *
     * @param action the case action
     * @return the action ID
     */
    private Integer getActionIdForCaseEvent(String action) {
        // Map common case actions to existing action IDs
        // These would need to be updated based on your actual action IDs
        switch (action.toLowerCase()) {
            case "create":
                return 1; // Create action ID
            case "update":
                return 2; // Update action ID
            case "delete":
                return 3; // Delete action ID
            case "status_change":
                return 4; // Status change action ID
            case "assign":
                return 5; // Assignment action ID
            case "comment":
                return 6; // Comment action ID
            case "attachment":
                return 7; // Attachment action ID
            default:
                return 8; // Default/other action ID
        }
    }
}