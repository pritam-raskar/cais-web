package com.dair.cais.alert;

import com.dair.cais.audit.AuditLogRequest;
import com.dair.cais.audit.AuditTrailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class AlertServiceWithAudit extends AlertService {

    @Autowired
    private AuditTrailService auditTrailService;

    public AlertEntity insertAlert(String alertType, AlertEntity alertEntity, AuditLogRequest auditLogRequest) {
        AlertEntity createdAlert = super.insertAlert(alertType, alertEntity);
        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setAffectedItemId(createdAlert.getAlertId());
        auditLogRequest.setNewValue(createdAlert.toString());
        auditTrailService.logAction(auditLogRequest.getUserId() , auditLogRequest.getUserRole(), auditLogRequest.getActionId(), auditLogRequest.getDescription(), auditLogRequest.getCategory(),
                auditLogRequest.getAffectedItemType(),auditLogRequest.getAffectedItemId(),auditLogRequest.getOldValue(),auditLogRequest.getNewValue());
        return createdAlert;
    }



    public Alert getAlertOnId(final String alertId, AuditLogRequest auditLogRequest) {
        Alert alert = super.getAlertOnId(alertId);
        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setAffectedItemId(alertId);
        auditTrailService.logAction(auditLogRequest.getUserId() , auditLogRequest.getUserRole(), auditLogRequest.getActionId(), auditLogRequest.getDescription(), auditLogRequest.getCategory(),
                auditLogRequest.getAffectedItemType(),auditLogRequest.getAffectedItemId(),auditLogRequest.getOldValue(),auditLogRequest.getNewValue());;
        return alert;
    }

    public Alert updateTotalScore(String alertId, int totalScore, AuditLogRequest auditLogRequest) {
        Alert oldAlert = super.getAlertOnId(alertId);
        Alert updatedAlert = super.updateTotalScore(alertId, totalScore);
        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setAffectedItemId(alertId);
        auditLogRequest.setOldValue(oldAlert.getTotalScore().toString());
        auditLogRequest.setNewValue(String.valueOf(totalScore));
        auditTrailService.logAction(auditLogRequest.getUserId() , auditLogRequest.getUserRole(), auditLogRequest.getActionId(), auditLogRequest.getDescription(), auditLogRequest.getCategory(),
                auditLogRequest.getAffectedItemType(),auditLogRequest.getAffectedItemId(),auditLogRequest.getOldValue(),auditLogRequest.getNewValue());;
        return updatedAlert;
    }

    public Alert updateOwnerId(String alertId, String ownerId, AuditLogRequest auditLogRequest) {
        Alert oldAlert = super.getAlertOnId(alertId);
        Alert updatedAlert = super.updateOwnerId(alertId, ownerId);
        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setAffectedItemId(alertId);
        auditLogRequest.setOldValue(oldAlert.getOwnerId());
        auditLogRequest.setNewValue(ownerId);
        auditTrailService.logAction(auditLogRequest.getUserId() , auditLogRequest.getUserRole(), auditLogRequest.getActionId(), auditLogRequest.getDescription(), auditLogRequest.getCategory(),
                auditLogRequest.getAffectedItemType(),auditLogRequest.getAffectedItemId(),auditLogRequest.getOldValue(),auditLogRequest.getNewValue());;
        return updatedAlert;
    }

    public Alert updateOrgUnitId(String alertId, String orgUnitId, AuditLogRequest auditLogRequest) {
        Alert oldAlert = super.getAlertOnId(alertId);
        Alert updatedAlert = super.updateOrgUnitId(alertId, orgUnitId);
        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setAffectedItemId(alertId);
        auditLogRequest.setOldValue(oldAlert.getOrgUnitId());
        auditLogRequest.setNewValue(orgUnitId);
        auditTrailService.logAction(auditLogRequest.getUserId() , auditLogRequest.getUserRole(), auditLogRequest.getActionId(), auditLogRequest.getDescription(), auditLogRequest.getCategory(),
                auditLogRequest.getAffectedItemType(),auditLogRequest.getAffectedItemId(),auditLogRequest.getOldValue(),auditLogRequest.getNewValue());;
        return updatedAlert;
    }

    public Alert updateStatus(String alertId, String statusId, AuditLogRequest auditLogRequest) {
        Alert oldAlert = super.getAlertOnId(alertId);
        Alert updatedAlert = super.updateStatus(alertId, statusId);
        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setAffectedItemId(alertId);
        auditLogRequest.setOldValue(oldAlert.getStatus());
        auditLogRequest.setNewValue(statusId);
        auditTrailService.logAction(auditLogRequest.getUserId() , auditLogRequest.getUserRole(), auditLogRequest.getActionId(), auditLogRequest.getDescription(), auditLogRequest.getCategory(),
                auditLogRequest.getAffectedItemType(),auditLogRequest.getAffectedItemId(),auditLogRequest.getOldValue(),auditLogRequest.getNewValue());;
        return updatedAlert;
    }

    public Alert changeStep(String alertId, Long stepId, AuditLogRequest auditLogRequest) {
        Alert oldAlert = super.getAlertOnId(alertId);
        Alert updatedAlert = super.changeStep(alertId, stepId);
        auditLogRequest.setAffectedItemId(alertId);
        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setOldValue(oldAlert.getAlertStepId());
        auditLogRequest.setNewValue(updatedAlert.getAlertStepId());
        auditTrailService.logAction(auditLogRequest.getUserId() , auditLogRequest.getUserRole(), auditLogRequest.getActionId(), auditLogRequest.getDescription(), auditLogRequest.getCategory(),
                auditLogRequest.getAffectedItemType(),auditLogRequest.getAffectedItemId(),auditLogRequest.getOldValue(),auditLogRequest.getNewValue());;
        return updatedAlert;
    }

    public Map<String, Object> getAllAlerts(String name, String state, List<String> accountNumbers, List<String> owners,
                                            List<String> assignees, Date createdDateFrom, Date createdDateTo, int limit, int offset, 
                                            AuditLogRequest auditLogRequest) {
        Map<String, Object> alerts = super.getAllAlerts(name, state, accountNumbers, owners, assignees,
                createdDateFrom, createdDateTo, limit, offset);
        auditLogRequest.setAffectedItemType("Alert");
        auditTrailService.logAction(auditLogRequest.getUserId() , auditLogRequest.getUserRole(), auditLogRequest.getActionId(), auditLogRequest.getDescription(), auditLogRequest.getCategory(),
                auditLogRequest.getAffectedItemType(),auditLogRequest.getAffectedItemId(),auditLogRequest.getOldValue(),auditLogRequest.getNewValue());;
        return alerts;
    }

    public Alert createAlert(Alert alert, AuditLogRequest auditLogRequest) {
        Alert createdAlert = super.createAlert(alert);
        auditLogRequest.setAffectedItemId(createdAlert.getAlertId());
        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setNewValue(createdAlert.toString());
        auditTrailService.logAction(auditLogRequest.getUserId() , auditLogRequest.getUserRole(), auditLogRequest.getActionId(), auditLogRequest.getDescription(), auditLogRequest.getCategory(),
                auditLogRequest.getAffectedItemType(),auditLogRequest.getAffectedItemId(),auditLogRequest.getOldValue(),auditLogRequest.getNewValue());;
        return createdAlert;
    }

    public void deleteAlertById(String alertId, String alertType, AuditLogRequest auditLogRequest) {
        Alert alertToDelete = super.getAlertById(alertId, alertType);
        super.deleteAlertById(alertId, alertType);
        auditLogRequest.setAffectedItemType("Alert");
        auditLogRequest.setAffectedItemId(alertId);
        auditLogRequest.setOldValue(alertToDelete.toString());
        auditTrailService.logAction(auditLogRequest.getUserId() , auditLogRequest.getUserRole(), auditLogRequest.getActionId(), auditLogRequest.getDescription(), auditLogRequest.getCategory(),
                auditLogRequest.getAffectedItemType(),auditLogRequest.getAffectedItemId(),auditLogRequest.getOldValue(),auditLogRequest.getNewValue());;
    }
}
