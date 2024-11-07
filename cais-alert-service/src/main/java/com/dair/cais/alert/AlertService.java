package com.dair.cais.alert;

import com.dair.cais.access.UserBasedPermission.UserPermissionService;
import com.dair.cais.audit.AuditLogRequest;
import com.dair.cais.audit.AuditTrailService;
import com.dair.cais.common.config.CaisAlertConstants;
import com.dair.cais.steps.Step;
import com.dair.cais.steps.StepRepository;
import com.dair.cais.steps.StepStatus;
import com.dair.cais.steps.StepStatusRepository;
import com.dair.exception.CaisBaseException;
import com.dair.exception.CaisIllegalArgumentException;
import com.dair.exception.CaisNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AlertService {
   private final AlertMapper alertMapper;
   private final StepRepository stepsRepository;
   private final StepStatusRepository stepStatusRepository;
   private final MongoTemplate mongoTemplate;
   private final AlertRepository alertRepository;
   private final AuditTrailService auditTrailService;
   private final UserPermissionService userPermissionService;

   // Base Operations
   public List<Alert> getAllActiveAlerts() {
      log.debug("Fetching all active and non-deleted alerts");
      List<AlertEntity> activeAlertEntities = alertRepository.findAllActiveAndNonDeletedAlerts();
      return activeAlertEntities.stream()
              .map(alertMapper::toModel)
              .collect(Collectors.toList());
   }

   public List<Alert> getAllActiveAlertsWithAudit(AuditLogRequest auditLogRequest) {
      List<Alert> activeAlerts = getAllActiveAlerts();

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setDescription("Fetched all active and non-deleted alerts");
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              null,
              null,
              "Fetched " + activeAlerts.size() + " alerts");

      return activeAlerts;
   }

   // Alert Retrieval Operations
   public Alert getAlertOnId(final String alertId) {
      log.debug("Fetching alert with ID: {}", alertId);
      AlertEntity alertById = alertRepository.getAlertOnId(alertId);
      if (alertById == null) {
         throw new CaisNotFoundException("Alert not found with ID: " + alertId);
      }
      return alertMapper.toModel(alertById);
   }

   public Alert getAlertOnId(final String alertId, AuditLogRequest auditLogRequest) {
      Alert alert = getAlertOnId(alertId);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setAffectedItemId(alertId);
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              auditLogRequest.getAffectedItemId(),
              null,
              alert.toString());

      return alert;
   }

   public List<AlertEntity> findAlertsByOrgFamily(String searchstring) {
      log.debug("Finding alerts by org family: {}", searchstring);
      return alertRepository.findAlertsByOrgFamily(searchstring);
   }

   public List<AlertEntity> findAlertsByOrgFamilyWithAudit(String searchstring, AuditLogRequest auditLogRequest) {
      List<AlertEntity> alerts = findAlertsByOrgFamily(searchstring);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setDescription("Searched alerts by org family: " + searchstring);
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              null,
              null,
              "Found " + alerts.size() + " alerts");

      return alerts;
   }

   public List<AlertEntity> findAlertsByOrg(String searchstring) {
      log.debug("Finding alerts by org: {}", searchstring);
      return alertRepository.findAlertsByOrg(searchstring);
   }

   public List<AlertEntity> findAlertsByOrgWithAudit(String searchstring, AuditLogRequest auditLogRequest) {
      List<AlertEntity> alerts = findAlertsByOrg(searchstring);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setDescription("Searched alerts by org: " + searchstring);
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              null,
              null,
              "Found " + alerts.size() + " alerts");

      return alerts;
   }

   public List<AlertEntity> findAlertsByOrgFamilyByUserOrgKeys(String userId) {
      log.debug("Finding alerts for user: {}", userId);
      List<String> userOrgKeys = userPermissionService.getDistinctOrgKeysForUser(userId);
      return alertRepository.findAlertsByOrgFamilyBYUserOrgKeys(userOrgKeys);
   }

   public List<AlertEntity> findAlertsByOrgFamilyByUserOrgKeysWithAudit(String userId, AuditLogRequest auditLogRequest) {
      List<AlertEntity> alerts = findAlertsByOrgFamilyByUserOrgKeys(userId);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setDescription("Fetched alerts for user's org units");
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              null,
              null,
              "Found " + alerts.size() + " alerts for user " + userId);

      return alerts;
   }

   // Alert Update Operations
   public Alert updateTotalScore(String alertId, int totalScore) {
      log.debug("Updating total score for alert: {} to {}", alertId, totalScore);
      return alertRepository.updateTotalScore(alertId, totalScore);
   }

   public Alert updateTotalScoreWithAudit(String alertId, int totalScore, AuditLogRequest auditLogRequest) {
      Alert oldAlert = getAlertOnId(alertId);
      Alert updatedAlert = updateTotalScore(alertId, totalScore);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setAffectedItemId(alertId);
      auditLogRequest.setOldValue(oldAlert.getTotalScore().toString());
      auditLogRequest.setNewValue(String.valueOf(totalScore));
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              auditLogRequest.getAffectedItemId(),
              auditLogRequest.getOldValue(),
              auditLogRequest.getNewValue());

      return updatedAlert;
   }

   public Alert updateOwnerId(String alertId, String ownerId) {
      log.debug("Updating owner for alert: {} to {}", alertId, ownerId);
      return alertRepository.updateOwnerId(alertId, ownerId);
   }

   public Alert updateOwnerIdWithAudit(String alertId, String ownerId, AuditLogRequest auditLogRequest) {
      Alert oldAlert = getAlertOnId(alertId);
      Alert updatedAlert = updateOwnerId(alertId, ownerId);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setAffectedItemId(alertId);
      auditLogRequest.setOldValue(oldAlert.getOwnerId());
      auditLogRequest.setNewValue(ownerId);
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              auditLogRequest.getAffectedItemId(),
              auditLogRequest.getOldValue(),
              auditLogRequest.getNewValue());

      return updatedAlert;
   }

   public Alert updateOrgUnitId(String alertId, String orgUnitId) {
      log.debug("Updating org unit for alert: {} to {}", alertId, orgUnitId);
      return alertRepository.updateOrgUnitId(alertId, orgUnitId);
   }

   public Alert updateOrgUnitIdWithAudit(String alertId, String orgUnitId, AuditLogRequest auditLogRequest) {
      Alert oldAlert = getAlertOnId(alertId);
      Alert updatedAlert = updateOrgUnitId(alertId, orgUnitId);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setAffectedItemId(alertId);
      auditLogRequest.setOldValue(oldAlert.getOrgUnitId());
      auditLogRequest.setNewValue(orgUnitId);
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              auditLogRequest.getAffectedItemId(),
              auditLogRequest.getOldValue(),
              auditLogRequest.getNewValue());

      return updatedAlert;
   }

   public Alert updateStatus(String alertId, String statusId) {
      log.debug("Updating status for alert: {} to {}", alertId, statusId);
      return alertRepository.updateStatus(alertId, statusId);
   }

   public Alert updateStatusWithAudit(String alertId, String statusId, AuditLogRequest auditLogRequest) {
      Alert oldAlert = getAlertOnId(alertId);
      Alert updatedAlert = updateStatus(alertId, statusId);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setAffectedItemId(alertId);
      auditLogRequest.setOldValue(oldAlert.getStatus());
      auditLogRequest.setNewValue(statusId);
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              auditLogRequest.getAffectedItemId(),
              auditLogRequest.getOldValue(),
              auditLogRequest.getNewValue());

      return updatedAlert;
   }
   // Step Management Operations
   public Alert changeStep(String alertId, Long stepId) {
      log.debug("Changing step for alert: {} to step: {}", alertId, stepId);
      Step step = stepsRepository.findByStepId(stepId);
      if (step == null) {
         throw new CaisNotFoundException("Step not found with id: " + stepId);
      }

      StepStatus stepStatus = stepStatusRepository.findByStepStatusId(step.getStepStatusId());
      if (stepStatus == null) {
         throw new CaisNotFoundException("Step status not found for step id: " + stepId);
      }

      AlertEntity updatedEntity = alertRepository.changeStep(alertId, stepId, step.getStepName(), stepStatus.getStepName());
      if (updatedEntity == null) {
         throw new CaisNotFoundException("Alert not found with id: " + alertId);
      }
      return alertMapper.toModel(updatedEntity);
   }

   public Alert changeStepWithAudit(String alertId, Long stepId, AuditLogRequest auditLogRequest) {
      Alert oldAlert = getAlertOnId(alertId);
      Alert updatedAlert = changeStep(alertId, stepId);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setAffectedItemId(alertId);
      auditLogRequest.setOldValue(oldAlert.getAlertStepName());
      auditLogRequest.setNewValue(updatedAlert.getAlertStepName());
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              auditLogRequest.getAffectedItemId(),
              auditLogRequest.getOldValue(),
              auditLogRequest.getNewValue());

      return updatedAlert;
   }

   // Alert Search Operations
   public List<AlertEntity> findAlertsByCriteria(
           String alertId, String createDate, String lastUpdateDate, String totalScore,
           String createdBy, String businessDate, String focalEntity, String focus, String alertTypeId,
           String alertRegion, String alertGroupId, Boolean isConsolidated, Boolean isActive,
           Boolean hasMultipleScenario, Boolean isDeleted, String orgUnitId, String orgFamily,
           String previousOrgUnitId, Boolean isOrgUnitUpdated, Boolean isRelatedAlert, String ownerId,
           String ownerName, String status, String alertStepId, String alertStepName, Boolean isCaseCreated) {

      log.debug("Finding alerts by criteria");
      return alertRepository.findAlertsByCriteria(
              alertId, createDate, lastUpdateDate, totalScore, createdBy, businessDate,
              focalEntity, focus, alertTypeId, alertRegion, alertGroupId, isConsolidated,
              isActive, hasMultipleScenario, isDeleted, orgUnitId, orgFamily, previousOrgUnitId,
              isOrgUnitUpdated, isRelatedAlert, ownerId, ownerName, status, alertStepId,
              alertStepName, isCaseCreated);
   }

   public List<AlertEntity> findAlertsByCriteriaWithAudit(
           String alertId, String createDate, String lastUpdateDate, String totalScore,
           String createdBy, String businessDate, String focalEntity, String focus, String alertTypeId,
           String alertRegion, String alertGroupId, Boolean isConsolidated, Boolean isActive,
           Boolean hasMultipleScenario, Boolean isDeleted, String orgUnitId, String orgFamily,
           String previousOrgUnitId, Boolean isOrgUnitUpdated, Boolean isRelatedAlert, String ownerId,
           String ownerName, String status, String alertStepId, String alertStepName, Boolean isCaseCreated,
           AuditLogRequest auditLogRequest) {

      List<AlertEntity> alerts = findAlertsByCriteria(
              alertId, createDate, lastUpdateDate, totalScore, createdBy, businessDate,
              focalEntity, focus, alertTypeId, alertRegion, alertGroupId, isConsolidated,
              isActive, hasMultipleScenario, isDeleted, orgUnitId, orgFamily, previousOrgUnitId,
              isOrgUnitUpdated, isRelatedAlert, ownerId, ownerName, status, alertStepId,
              alertStepName, isCaseCreated);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setDescription("Searched alerts by criteria");
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              null,
              null,
              "Found " + alerts.size() + " alerts");

      return alerts;
   }

   // Alert Management Operations
   public Map<String, Object> getAllAlerts(String name, String state, List<String> accountNumbers,
                                           List<String> owners, List<String> assignees, Date createdDateFrom,
                                           Date createdDateTo, @Valid int limit, @Valid int offset) {
      log.debug("Getting all alerts with filters");
      validateRequestParams(name, state, accountNumbers, owners, assignees, createdDateFrom,
              createdDateTo, offset, limit);

      try {
         List<AlertEntity> allAlertEntities = alertRepository.getAllAlerts(name, state, accountNumbers,
                 owners, assignees, createdDateFrom, createdDateTo, offset, limit);

         List<Alert> allAlerts = allAlertEntities.stream()
                 .map(alertMapper::toModel)
                 .collect(Collectors.toList());

         Map<String, Object> response = new HashMap<>();
         response.put("alerts", allAlerts);
         response.put("count", allAlerts.size());
         return response;
      } catch (Exception e) {
         log.error("Error retrieving alerts", e);
         throw new CaisBaseException("Error retrieving alerts");
      }
   }

   public Map<String, Object> getAllAlertsWithAudit(String name, String state, List<String> accountNumbers,
                                                    List<String> owners, List<String> assignees, Date createdDateFrom,
                                                    Date createdDateTo, @Valid int limit, @Valid int offset,
                                                    AuditLogRequest auditLogRequest) {
      Map<String, Object> response = getAllAlerts(name, state, accountNumbers, owners, assignees,
              createdDateFrom, createdDateTo, limit, offset);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setDescription("Retrieved filtered alerts");
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              null,
              null,
              "Retrieved " + response.get("count") + " alerts");

      return response;
   }

   public Alert patchAlert(String alertId, String alertType, Alert alert) {
      log.debug("Patching alert: {} of type: {}", alertId, alertType);
      AlertEntity upsertedAlert = alertRepository.patchAlert(alertMapper.toEntity(alertId, alert), alertType);
      return alertMapper.toModel(upsertedAlert);
   }

   public Alert patchAlertWithAudit(String alertId, String alertType, Alert alert,
                                    AuditLogRequest auditLogRequest) {
      Alert oldAlert = getAlertById(alertId, alertType);
      Alert updatedAlert = patchAlert(alertId, alertType, alert);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setAffectedItemId(alertId);
      auditLogRequest.setOldValue(oldAlert.toString());
      auditLogRequest.setNewValue(updatedAlert.toString());
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              auditLogRequest.getAffectedItemId(),
              auditLogRequest.getOldValue(),
              auditLogRequest.getNewValue());

      return updatedAlert;
   }

   public void deleteAlertById(String alertId, String alertType) {
      log.debug("Deleting alert: {} of type: {}", alertId, alertType);
      AlertEntity alertById = alertRepository.deleteAlertById(alertId, alertType);
      if (alertById == null) {
         throw new CaisNotFoundException("Alert not found with id: " + alertId);
      }
   }

   public void deleteAlertByIdWithAudit(String alertId, String alertType, AuditLogRequest auditLogRequest) {
      Alert alertToDelete = getAlertById(alertId, alertType);
      deleteAlertById(alertId, alertType);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setAffectedItemId(alertId);
      auditLogRequest.setOldValue(alertToDelete.toString());
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              auditLogRequest.getAffectedItemId(),
              auditLogRequest.getOldValue(),
              auditLogRequest.getNewValue());
   }
   // Bulk Operations
   public List<Alert> createAlerts(List<Alert> alerts) {
      log.debug("Creating {} bulk alerts", alerts.size());
      return alerts.stream()
              .map(this::createAlert)
              .collect(Collectors.toList());
   }

   public List<Alert> createAlertsWithAudit(List<Alert> alerts, AuditLogRequest auditLogRequest) {
      List<Alert> createdAlerts = createAlerts(alerts);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setDescription("Created bulk alerts");
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              null,
              null,
              "Created " + createdAlerts.size() + " alerts");

      return createdAlerts;
   }

   public Alert createAlert(Alert alert) {
      log.debug("Creating new alert");
      List<String> validationErrors = validateAlert(alert);
      if (!validationErrors.isEmpty()) {
         throw new AlertValidationException("Alert validation failed", validationErrors);
      }

      AlertEntity alertEntity = alertMapper.toEntity(alert);
      AlertEntity upsertedAlert = alertRepository.createUpsertAlert(alertEntity);
      return alertMapper.toModel(upsertedAlert);
   }

   public Alert createAlertWithAudit(Alert alert, AuditLogRequest auditLogRequest) {
      Alert createdAlert = createAlert(alert);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setAffectedItemId(createdAlert.getAlertId());
      auditLogRequest.setNewValue(createdAlert.toString());
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              auditLogRequest.getAffectedItemId(),
              null,
              auditLogRequest.getNewValue());

      return createdAlert;
   }

   public Alert getAlertById(final String alertId, String alertType) {
      log.debug("Getting alert by ID: {} and type: {}", alertId, alertType);
      AlertEntity alertById = alertRepository.getAlertById(alertId, alertType);
      if (alertById == null) {
         throw new CaisNotFoundException("Alert not found with id: " + alertId);
      }
      return alertMapper.toModel(alertById);
   }

   public Alert getAlertByIdWithAudit(final String alertId, String alertType,
                                      AuditLogRequest auditLogRequest) {
      Alert alert = getAlertById(alertId, alertType);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setAffectedItemId(alertId);
      auditTrailService.logAction(auditLogRequest.getUserId(),
              auditLogRequest.getUserRole(),
              auditLogRequest.getActionId(),
              auditLogRequest.getDescription(),
              auditLogRequest.getCategory(),
              auditLogRequest.getAffectedItemType(),
              auditLogRequest.getAffectedItemId(),
              null,
              alert.toString());

      return alert;
   }

   // Validation Methods
   private List<String> validateAlert(Alert alert) {
      List<String> errors = new ArrayList<>();

      if (!StringUtils.hasText(alert.getAlertId())) {
         errors.add("Alert ID is required");
      }
      if (!StringUtils.hasText(alert.getCreateDate())) {
         errors.add("Create date is required");
      }
      if (!StringUtils.hasText(alert.getCreatedBy())) {
         errors.add("Created by is required");
      }
      if (!StringUtils.hasText(alert.getBusinessDate())) {
         errors.add("Business date is required");
      }
      if (!StringUtils.hasText(alert.getFocalEntity())) {
         errors.add("Focal entity is required");
      }
      if (!StringUtils.hasText(alert.getFocus())) {
         errors.add("Focus is required");
      }
      if (!StringUtils.hasText(alert.getAlertTypeId())) {
         errors.add("Alert type ID is required");
      } else {
         Query query = new Query(Criteria.where("alertTypeId").is(alert.getAlertTypeId()));
         boolean alertTypeExists = mongoTemplate.exists(query, CaisAlertConstants.CAIS_ALERT_TYPE_COLLECTION_NAME);
         if (!alertTypeExists) {
            errors.add("Invalid Alert type ID: " + alert.getAlertTypeId());
         }
      }

      return errors;
   }

   private void validateRequestParams(String name, String state, List<String> accountNumbers,
                                      List<String> owners, List<String> assignees, Date createdDateFrom,
                                      Date createdDateTo, int offset, int limit) {
      StringBuilder errorMessage = new StringBuilder();

      if (name != null && !name.isEmpty() && name.length() > 20) {
         errorMessage.append("name cannot be longer than 20 characters;");
      }

      if (accountNumbers != null && accountNumbers.size() > 5) {
         errorMessage.append("accountNumber list cannot be greater than 5;");
      }

      if (owners != null && owners.size() > 5) {
         errorMessage.append("owner list cannot be greater than 5;");
      }

      if (assignees != null && assignees.size() > 5) {
         errorMessage.append("assignee list cannot be greater than 5;");
      }

      if (createdDateFrom != null && createdDateTo != null && createdDateFrom.after(createdDateTo)) {
         errorMessage.append("from date cannot be after to date;");
      }

      if (limit < 0) {
         errorMessage.append("limit cannot be negative;");
      }
      if (offset < 0) {
         errorMessage.append("offset cannot be negative;");
      }

      if (errorMessage.length() > 0) {
         throw new CaisIllegalArgumentException(errorMessage.toString());
      }
   }
}






//
//
//package com.dair.cais.alert;
//
//import com.dair.cais.access.UserBasedPermission.UserPermissionService;
//import com.dair.cais.audit.AuditLogRequest;
//import com.dair.cais.audit.AuditTrailService;
//import com.dair.cais.common.config.CaisAlertConstants;
//import com.dair.cais.steps.Step;
//import com.dair.cais.steps.StepRepository;
//import com.dair.cais.steps.StepStatus;
//import com.dair.cais.steps.StepStatusRepository;
//import com.dair.exception.CaisBaseException;
//import com.dair.exception.CaisIllegalArgumentException;
//import com.dair.exception.CaisNotFoundException;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.stereotype.Service;
//import org.springframework.util.StringUtils;
//
//import javax.validation.Valid;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//public class AlertService {
//
//   @Autowired
//   private AlertMapper alertMapper;
//
//   @Autowired
//   private StepRepository stepsRepository;
//
//   @Autowired
//   private StepStatusRepository stepStatusRepository;
//
//   @Autowired
//   private MongoTemplate mongoTemplate;
//
//   @Autowired
//   private AlertRepository alertRepository;
//
//   @Autowired
//   private AuditTrailService auditTrailService;
//
//   @Autowired
//   private UserPermissionService userPermissionService;
//
//
//   public List<Alert> getAllActiveAlerts(AuditLogRequest auditLogRequest) {
//      log.info("Fetching all active and non-deleted alerts");
//      List<AlertEntity> activeAlertEntities = alertRepository.findAllActiveAndNonDeletedAlerts();
//      List<Alert> activeAlerts = activeAlertEntities.stream()
//              .map(alertMapper::toModel)
//              .collect(Collectors.toList());
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setDescription("Fetched all active and non-deleted alerts");
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              null, // No specific affected item ID for this operation
//              null, // No old value
//              "Fetched " + activeAlerts.size() + " alerts");
//
//      return activeAlerts;
//   }
//
//
//   private Alert getAlertOnId(final String alertId) {
//      AlertEntity alertById = alertRepository.getAlertOnId(alertId);
//      if (alertById == null) {
//         throw new CaisNotFoundException();
//      }
//      return alertMapper.toModel(alertById);
//
//
//   }
//
//   public Alert getAlertOnId(final String alertId, AuditLogRequest auditLogRequest) {
//      AlertEntity alertById = alertRepository.getAlertOnId(alertId);
//      if (alertById == null) {
//         throw new CaisNotFoundException();
//      }
//      Alert alert = alertMapper.toModel(alertById);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());
//
//      return alert;
//   }
//
//   public List<AlertEntity> findAlertsByOrgFamily(String searchstring, AuditLogRequest auditLogRequest) {
//      List<AlertEntity> alerts = alertRepository.findAlertsByOrgFamily(searchstring);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());
//
//      return alerts;
//   }
//
//   public List<AlertEntity> findAlertsByOrgFamilyBYUserOrgKeys(String userId, AuditLogRequest auditLogRequest) {
//      log.info("Fetching alerts for user: {}", userId);
//
//      List<String> userOrgKeys = userPermissionService.getDistinctOrgKeysForUser(userId);
//      List<AlertEntity> alertEntities = alertRepository.findAlertsByOrgFamilyBYUserOrgKeys(userOrgKeys);
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setDescription("Fetched alerts for user's org units");
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              null, // No specific affected item ID for this operation
//              null, // No old value
//              "Fetched " + alertEntities.size() + " alerts for user " + userId);
//
//      return alertEntities;
//   }
//
//   public List<AlertEntity> findAlertsByOrg(String searchstring, AuditLogRequest auditLogRequest) {
//      List<AlertEntity> alerts = alertRepository.findAlertsByOrg(searchstring);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());
//
//      return alerts;
//   }
//
//   public List<AlertEntity> findAlertsByCriteria(
//           String alertId, String createDate, String lastUpdateDate, String totalScore,
//           String createdBy, String businessDate, String focalEntity, String focus, String alertTypeId,
//           String alertRegion, String alertGroupId, Boolean isConsolidated, Boolean isActive,
//           Boolean hasMultipleScenario, Boolean isDeleted, String orgUnitId, String orgFamily,
//           String previousOrgUnitId, Boolean isOrgUnitUpdated, Boolean isRelatedAlert, String ownerId,
//           String ownerName, String status, String alertStepId, String alertStepName, Boolean isCaseCreated,
//           AuditLogRequest auditLogRequest
//   ) {
//      List<AlertEntity> alerts = alertRepository.findAlertsByCriteria(
//              alertId, createDate, lastUpdateDate, totalScore, createdBy, businessDate, focalEntity, focus,
//              alertTypeId, alertRegion, alertGroupId, isConsolidated, isActive, hasMultipleScenario, isDeleted,
//              orgUnitId, orgFamily, previousOrgUnitId, isOrgUnitUpdated, isRelatedAlert, ownerId, ownerName,
//              status, alertStepId, alertStepName, isCaseCreated
//      );
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());
//
//      return alerts;
//   }
//
//   public Alert updateTotalScore(String alertId, int totalScore, AuditLogRequest auditLogRequest) {
//      Alert oldAlert = getAlertOnId(alertId, auditLogRequest);
//      Alert updatedAlert = alertRepository.updateTotalScore(alertId, totalScore);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditLogRequest.setOldValue(oldAlert.getTotalScore().toString());
//      auditLogRequest.setNewValue(String.valueOf(totalScore));
//      auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());
//
//      return updatedAlert;
//   }
//
//   public Alert updateOwnerId(String alertId, String ownerId, AuditLogRequest auditLogRequest) {
//      Alert oldAlert = getAlertOnId(alertId, auditLogRequest);
//      Alert updatedAlert = alertRepository.updateOwnerId(alertId, ownerId);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditLogRequest.setOldValue(oldAlert.getOwnerId());
//      auditLogRequest.setNewValue(ownerId);
//      auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());
//
//      return updatedAlert;
//   }
//
//   public Alert updateOrgUnitId(String alertId, String orgUnitId, AuditLogRequest auditLogRequest) {
//      Alert oldAlert = getAlertOnId(alertId, auditLogRequest);
//      Alert updatedAlert = alertRepository.updateOrgUnitId(alertId, orgUnitId);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditLogRequest.setOldValue(oldAlert.getOrgUnitId());
//      auditLogRequest.setNewValue(orgUnitId);
//      auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());
//
//      return updatedAlert;
//   }
//
//   public Alert updateStatus(String alertId, String statusId, AuditLogRequest auditLogRequest) {
//      Alert oldAlert = getAlertOnId(alertId, auditLogRequest);
//      Alert updatedAlert = alertRepository.updateStatus(alertId, statusId);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditLogRequest.setOldValue(oldAlert.getStatus());
//      auditLogRequest.setNewValue(statusId);
//      auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());
//
//      return updatedAlert;
//   }
//
//   public Alert changeStep(String alertId, Long stepId, AuditLogRequest auditLogRequest) {
//      Step step = stepsRepository.findByStepId(stepId);
//      if (step == null) {
//         throw new CaisNotFoundException("Step not found with id: " + stepId);
//      }
//
//      StepStatus stepStatus = stepStatusRepository.findByStepStatusId(step.getStepStatusId());
//      if (stepStatus == null) {
//         throw new CaisNotFoundException("Step status not found for step id: " + stepId);
//      }
//
//      Alert oldAlert = getAlertOnId(alertId);
//      AlertEntity updatedEntity = alertRepository.changeStep(alertId, stepId, step.getStepName(), stepStatus.getStepName());
//      if (updatedEntity == null) {
//         throw new CaisNotFoundException("Alert not found with id: " + alertId);
//      }
//      Alert updatedAlert = alertMapper.toModel(updatedEntity);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditLogRequest.setOldValue(oldAlert.getAlertStepName());
//      auditLogRequest.setNewValue(updatedAlert.getAlertStepName());
//      auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());
//
//      return updatedAlert;
//   }
//
//   public Map<String, Object> getAllAlerts(String name, String state, List<String> accountNumbers, List<String> owners,
//                                           List<String> assignees, Date createdDateFrom, Date createdDateTo, @Valid int limit, @Valid int offset,
//                                           AuditLogRequest auditLogRequest) {
//      validateRequestParams(name, state, accountNumbers, owners, assignees, createdDateFrom, createdDateTo, offset, limit);
//
//      try {
//         List<AlertEntity> allAlertEntities = alertRepository.getAllAlerts(name, state, accountNumbers, owners,
//                 assignees, createdDateFrom, createdDateTo, offset, limit);
//
//         List<Alert> allAlerts = allAlertEntities.stream().map(a -> alertMapper.toModel(a))
//                 .collect(Collectors.toList());
//
//         Map<String, Object> response = new HashMap<>();
//         response.put("alerts", allAlerts);
//         response.put("count", allAlerts.size());
//
//         auditLogRequest.setAffectedItemType("Alert");
//         auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
//                 auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
//                 auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());
//
//         return response;
//      } catch (Exception e) {
//         throw new CaisBaseException("Error retrieving alerts");
//      }
//   }
//
//   public Alert patchAlert(String alertId, String alertType, Alert alert, AuditLogRequest auditLogRequest) {
//      AlertEntity upsertedAlert = alertRepository.patchAlert(alertMapper.toEntity(alertId, alert), alertType);
//      Alert updatedAlert = alertMapper.toModel(upsertedAlert);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());
//
//      return updatedAlert;
//   }
//
//   public void deleteAlertById(String alertId, String alertType, AuditLogRequest auditLogRequest) {
//      Alert alertToDelete = getAlertById(alertId, alertType, auditLogRequest);
//      AlertEntity alertById = alertRepository.deleteAlertById(alertId, alertType);
//      if (alertById == null) {
//         throw new CaisNotFoundException();
//      }
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditLogRequest.setOldValue(alertToDelete.toString());
//      auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());
//   }
//
//   public List<Alert> createAlerts(List<Alert> alerts, AuditLogRequest auditLogRequest) {
//      List<Alert> createdAlerts = alerts.stream().map(a -> createAlert(a, auditLogRequest)).collect(Collectors.toList());
//      return createdAlerts;
//   }
//
//   public Alert createAlert(Alert alert, AuditLogRequest auditLogRequest) {
//      List<String> validationErrors = validateAlert(alert);
//      if (!validationErrors.isEmpty()) {
//         throw new AlertValidationException("Alert validation failed", validationErrors);
//      }
//
//      AlertEntity alertEntity = alertMapper.toEntity(alert);
//      AlertEntity upsertedAlert = alertRepository.createUpsertAlert(alertEntity);
//      Alert createdAlert = alertMapper.toModel(upsertedAlert);
//
//      auditLogRequest.setAffectedItemId(createdAlert.getAlertId());
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setNewValue(createdAlert.toString());
//      auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());
//
//      return createdAlert;
//   }
//
//   private List<String> validateAlert(Alert alert) {
//      List<String> errors = new ArrayList<>();
//
//      if (!StringUtils.hasText(alert.getAlertId())) {
//         errors.add("Alert ID is required");
//      }
//      if (!StringUtils.hasText(alert.getCreateDate())) {
//         errors.add("Create date is required");
//      }
//      if (!StringUtils.hasText(alert.getCreatedBy())) {
//         errors.add("Created by is required");
//      }
//      if (!StringUtils.hasText(alert.getBusinessDate())) {
//         errors.add("Business date is required");
//      }
//      if (!StringUtils.hasText(alert.getFocalEntity())) {
//         errors.add("Focal entity is required");
//      }
//      if (!StringUtils.hasText(alert.getFocus())) {
//         errors.add("Focus is required");
//      }
//      if (!StringUtils.hasText(alert.getAlertTypeId())) {
//         errors.add("Alert type ID is required");
//      } else {
//         Query query = new Query(Criteria.where("alertTypeId").is(alert.getAlertTypeId()));
//         boolean alertTypeExists = mongoTemplate.exists(query, CaisAlertConstants.CAIS_ALERT_TYPE_COLLECTION_NAME);
//         if (!alertTypeExists) {
//            errors.add("Invalid Alert type ID: " + alert.getAlertTypeId());
//         }
//      }
//
//      return errors;
//   }
//
//   public AlertEntity insertAlert(String alertType, AlertEntity alertEntity, AuditLogRequest auditLogRequest) {
//      validateAlert(alertType, alertEntity);
//      alertEntity.setAlertTypeId(alertType);
//      AlertEntity createdAlert = alertRepository.insertAlert(alertEntity);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(createdAlert.getAlertId());
//      auditLogRequest.setNewValue(createdAlert.toString());
//      auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());
//
//      return createdAlert;
//   }
//
//   private void validateAlert(String alertType, AlertEntity alertEntity) {
//      if (StringUtils.isEmpty(alertType)) {
//         throw new CaisBaseException("AlertType is mandatory");
//      }
//
//      if (alertEntity == null) {
//         throw new CaisBaseException("AlertEntity cannot be null");
//      }
//
//      if (!alertType.equals(alertEntity.getAlertTypeId())) {
//         throw new IllegalArgumentException(String.format("Alert type mismatch: expected %s but got %s", alertType, alertEntity.getAlertTypeId()));
//      }
//
//      if (StringUtils.isEmpty(alertEntity.getAlertId())) {
//         throw new CaisBaseException("AlertId is mandatory");
//      }
//      if (StringUtils.isEmpty(alertEntity.getCreateDate())) {
//         throw new CaisBaseException("CreateDate is mandatory");
//      }
//      if (alertEntity.getTotalScore() == null) {
//         throw new CaisBaseException("TotalScore is mandatory");
//      }
//      if (StringUtils.isEmpty(alertEntity.getCreatedBy())) {
//         throw new CaisBaseException("CreatedBy is mandatory");
//      }
//      if (StringUtils.isEmpty(alertEntity.getBusinessDate())) {
//         throw new CaisBaseException("BusinessDate is mandatory");
//      }
//      if (StringUtils.isEmpty(alertEntity.getFocalEntity())) {
//         throw new CaisBaseException("FocalEntity is mandatory");
//      }
//      if (StringUtils.isEmpty(alertEntity.getFocus())) {
//         throw new CaisBaseException("Focus is mandatory");
//      }
//      if (StringUtils.isEmpty(alertEntity.getDetails())) {
//         throw new CaisBaseException("Details is mandatory");
//      }
//      if (alertEntity.getReasonDetails() == null) {
//         throw new CaisBaseException("ReasonDetails and Reasons are mandatory");
//      }
//   }
//
//   private void validateRequestParams(String name, String state, List<String> accountNumbers,
//                                      List<String> owners, List<String> assignees, Date createdDateFrom, Date createdDateTo, int offset, int limit) {
//      StringBuilder errorMessage = new StringBuilder();
//
//      if (name != null && !name.isEmpty()) {
//         if (name.length() > 20) {
//            errorMessage.append("name cannot be longer than 20 characters;");
//         }
//      }
//
//      if (accountNumbers != null && accountNumbers.size() > 5) {
//         errorMessage.append("accountNumber list cannot be greater than 5;");
//      }
//
//      if (owners != null && owners.size() > 5) {
//         errorMessage.append("owner list cannot be greater than 5;");
//      }
//
//      if (assignees != null && assignees.size() > 5) {
//         errorMessage.append("assignee list cannot be greater than 5;");
//      }
//
//      if (createdDateFrom != null && createdDateTo != null) {
//         if (createdDateFrom.after(createdDateTo)) {
//            errorMessage.append("from date cannot be after to date;");
//         }
//      }
//
//      if (limit < 0) {
//         errorMessage.append("limit cannot be negative;");
//      }
//      if (offset < 0) {
//         errorMessage.append("offset cannot be negative;");
//      }
//      if (errorMessage.isEmpty()) {
//         return;
//      }
//
//      throw new CaisIllegalArgumentException(errorMessage.toString());
//   }
//
//   public Alert getAlertById(final String alertId, String alertType, AuditLogRequest auditLogRequest) {
//      AlertEntity alertById = alertRepository.getAlertById(alertId, alertType);
//      if (alertById == null) {
//         throw new CaisNotFoundException();
//      }
//      Alert alert = alertMapper.toModel(alertById);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditTrailService.logAction(auditLogRequest.getUserId(), auditLogRequest.getUserRole(), auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(), auditLogRequest.getCategory(), auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(), auditLogRequest.getOldValue(), auditLogRequest.getNewValue());
//
//      return alert;
//   }
//}
//
