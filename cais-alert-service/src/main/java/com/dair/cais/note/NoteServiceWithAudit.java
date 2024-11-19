package com.dair.cais.note;

import com.dair.cais.audit.AuditLogRequest;
import com.dair.cais.audit.AuditTrailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class NoteServiceWithAudit {

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private NoteMapperExtended noteMapperExtended;

    @Autowired
    private AuditTrailService auditTrailService;

    public NoteExtended addNoteWithAudit(String note, String alertId, String createdBy,
                                         String entity, String entityValue, AuditLogRequest auditLogRequest) {
        // Validate inputs
        if (alertId == null || alertId.trim().isEmpty()) {
            throw new IllegalArgumentException("AlertId cannot be null or empty");
        }
        if (note == null || note.trim().isEmpty()) {
            throw new IllegalArgumentException("Note cannot be null or empty");
        }
        validateAuditLogRequest(auditLogRequest);

        // Add the note
        NoteEntityExtended noteEntity = noteRepository.addNote(note, alertId, createdBy, entity, entityValue);

        // Log the audit
        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setAffectedItemId(alertId);
        //auditLogRequest.setNewValue(note); // Store the note content as the new value
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

        return noteMapperExtended.toModel(noteEntity);
    }

    public Map<String, NoteExtended> addNoteToMultipleAlertsWithAudit(
            String note, List<String> alertIds, String createdBy,
            String entity, String entityValue, AuditLogRequest auditLogRequest) {
        // Validate inputs
        if (alertIds == null || alertIds.isEmpty()) {
            throw new IllegalArgumentException("AlertIds list cannot be null or empty");
        }
        if (note == null || note.trim().isEmpty()) {
            throw new IllegalArgumentException("Note cannot be null or empty");
        }
        validateAuditLogRequest(auditLogRequest);

        Map<String, NoteExtended> result = new HashMap<>();

        for (String alertId : alertIds) {
            try {
                // Create note with alertId as entityValue and "Alert" as entity
                NoteExtended noteExtended = addNoteWithAudit(
                        note,
                        alertId,
                        createdBy,
                        "Alert",  // Always set entity as "Alert"
                        alertId,  // Use the current alertId as entityValue
                        new AuditLogRequest(auditLogRequest)  // Create new instance to avoid modifying original
                );

                // Log each successful creation
                System.out.println("Created note for alertId: " + alertId);
                result.put(alertId, noteExtended);
            } catch (Exception e) {
                // Log any errors but continue processing other alerts
                System.err.println("Failed to add note to alert " + alertId + ": " + e.getMessage());
                result.put(alertId, null);
            }
        }

        return result;
    }

    public List<NoteExtended> getNotesWithAudit(String alertId, AuditLogRequest auditLogRequest) {
        // Validate inputs
        if (alertId == null || alertId.trim().isEmpty()) {
            throw new IllegalArgumentException("AlertId cannot be null or empty");
        }
        validateAuditLogRequest(auditLogRequest);

        // Log the audit for viewing notes
        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setAffectedItemId(alertId);
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

        return noteRepository.findByAlertId(alertId);
    }

    private void validateAuditLogRequest(AuditLogRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("AuditLogRequest cannot be null");
        }
        if (request.getUserId() == null) {
            throw new IllegalArgumentException("userId cannot be null in AuditLogRequest");
        }
        if (request.getUserRole() == null || request.getUserRole().trim().isEmpty()) {
            throw new IllegalArgumentException("userRole cannot be null or empty in AuditLogRequest");
        }
        if (request.getActionId() == null) {
            throw new IllegalArgumentException("actionId cannot be null in AuditLogRequest");
        }
    }
}