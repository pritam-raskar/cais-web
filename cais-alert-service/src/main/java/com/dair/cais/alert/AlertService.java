package com.dair.cais.alert;

import com.dair.cais.access.UserBasedPermission.UserPermissionService;
import com.dair.cais.access.user.UserEntity;
import com.dair.cais.access.user.UserRepository;
import com.dair.cais.alert.dto.StepInfo;
import com.dair.cais.alert.dto.StepTransitionDTO;
import com.dair.cais.alert.exception.AlertCreationException;
import com.dair.cais.alert.exception.AlertOperationException;
import com.dair.cais.alert.exception.AlertValidationException;
import com.dair.cais.alert.filter.FilterCriteria;
import com.dair.cais.alert.filter.MongoQueryBuilder;
import com.dair.cais.alert.rdbms.RdbmsAlertEntity;
import com.dair.cais.alert.rdbms.RdbmsAlertMapper;
import com.dair.cais.alert.rdbms.RdbmsAlertRepository;
import com.dair.cais.audit.AuditLogRequest;
import com.dair.cais.audit.AuditTrailService;
import com.dair.cais.common.config.CaisAlertConstants;
import com.dair.cais.organization.OrganizationFamilyEntity;
import com.dair.cais.organization.OrganizationFamilyRepository;
import com.dair.cais.steps.Step;
import com.dair.cais.steps.StepRepository;
import com.dair.cais.steps.StepStatus;
import com.dair.cais.steps.StepStatusRepository;
import com.dair.cais.type.AlertTypeExtended;
import com.dair.cais.type.AlertTypeServiceExtended;
import com.dair.cais.workflow.entity.WorkflowStepEntity;
import com.dair.cais.workflow.entity.WorkflowTransitionEntity;
import com.dair.cais.workflow.repository.WorkflowTransitionRepository;
import com.dair.exception.CaisBaseException;
import com.dair.exception.CaisIllegalArgumentException;
import com.dair.exception.CaisNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;
import org.springframework.data.mongodb.core.aggregation.Aggregation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AlertService {

   private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

   private final AlertMapper alertMapper;
   private final RdbmsAlertMapper rdbmsAlertMapper;
   private final StepRepository stepsRepository;
   private final StepStatusRepository stepStatusRepository;
   private final MongoTemplate mongoTemplate;
   private final AlertRepository alertRepository;
   private final RdbmsAlertRepository rdbmsAlertRepository;
   private final AuditTrailService auditTrailService;
   private final UserPermissionService userPermissionService;
   private final ObjectMapper objectMapper;
   private final UserRepository userRepository;
   private final OrganizationFamilyRepository orgFamilyRepository;
   private final MongoQueryBuilder mongoQueryBuilder;
   private final WorkflowTransitionRepository workflowTransitionRepository;

   @Autowired
   private AlertTypeServiceExtended alertTypeServiceExtended;



   // Base Operations
   @Transactional
   public Alert createAlert(Alert alert) {
      log.debug("Starting synchronized alert creation for alertId: {}", alert.getAlertId());

      List<String> validationErrors = validateAlert(alert);
      if (!validationErrors.isEmpty()) {
         log.error("Validation failed for alert: {}", alert.getAlertId());
         throw new AlertValidationException("Alert validation failed", validationErrors);
      }

      AlertEntity savedMongoEntity = null;

      try {
         // Set timestamps
         LocalDateTime now = LocalDateTime.now();
         String formattedDateTime = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

         // Create a copy of the alert to avoid modifying the input
         Alert processedAlert = new Alert();
         BeanUtils.copyProperties(alert, processedAlert);

         // Set only the system-managed dates
         processedAlert.setCreateDate(formattedDateTime);
         processedAlert.setLastUpdateDate(formattedDateTime);
         processedAlert.setCreatedAt(now);
         processedAlert.setUpdatedAt(now);

         // Create in MongoDB first
         AlertEntity mongoEntity = alertMapper.toEntity(processedAlert);
         savedMongoEntity = alertRepository.createUpsertAlert(mongoEntity);

         if (savedMongoEntity == null) {
            throw new AlertCreationException("Failed to save alert in MongoDB", null);
         }

         // Create in RDBMS
         RdbmsAlertEntity rdbmsEntity = rdbmsAlertMapper.toRdbmsEntity(processedAlert);
         try {
            rdbmsAlertRepository.save(rdbmsEntity);
         } catch (Exception e) {
            // If RDBMS fails, manually rollback MongoDB
            if (savedMongoEntity != null) {
               try {
                  alertRepository.deleteById(savedMongoEntity.getAlertId());
                  log.debug("Successfully rolled back from Mongo for alertId: {}", alert.getAlertId());
               } catch (Exception deleteEx) {
                  log.error("Failed to rollback MongoDB save for alertId: {}", alert.getAlertId(), deleteEx);
               }
            }
            throw new AlertCreationException("Failed to save alert in RDBMS", e);
         }

         log.debug("Successfully created alert in both databases for alertId: {}", alert.getAlertId());
         return alertMapper.toModel(savedMongoEntity);

      } catch (Exception e) {
         // Ensure MongoDB is rolled back if anything fails
         if (savedMongoEntity != null) {
            try {
               alertRepository.deleteById(savedMongoEntity.getAlertId());
            } catch (Exception deleteEx) {
               log.error("Failed to rollback MongoDB save for alertId: {}", alert.getAlertId(), deleteEx);
            }
         }
         log.error("Failed to create alert: {}. Error: {}", alert.getAlertId(), e.getMessage(), e);
         throw new AlertCreationException("Failed to create alert in databases", e);
      }
   }

   @SuppressWarnings("unchecked")
   private void formatDatesInMap(Map<String, Object> map) {
      for (Map.Entry<String, Object> entry : map.entrySet()) {
         Object value = entry.getValue();
         if (value instanceof String && isDateString((String) value)) {
            // Leave the date string as is if it's already in the correct format
            continue;
         } else if (value instanceof Map) {
            formatDatesInMap((Map<String, Object>) value);
         } else if (value instanceof List) {
            formatDatesInList((List<Object>) value);
         }
      }
   }

   @SuppressWarnings("unchecked")
   private void formatDatesInList(List<Object> list) {
      for (int i = 0; i < list.size(); i++) {
         Object item = list.get(i);
         if (item instanceof String && isDateString((String) item)) {
            // Leave the date string as is if it's already in the correct format
            continue;
         } else if (item instanceof Map) {
            formatDatesInMap((Map<String, Object>) item);
         } else if (item instanceof List) {
            formatDatesInList((List<Object>) item);
         }
      }
   }

   private boolean isDateString(String value) {
      try {
         // Check if the string matches the expected format "yyyy-MM-dd'T'HH:mm:ss"
         return value.matches("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}:\\d{2}");
      } catch (Exception e) {
         return false;
      }
   }
















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

   @Transactional
   public List<AlertEntity> findAlertsByOrgFamilyByUserOrgKeysWithAudit(
           String userId,
           FilterCriteria filterCriteria,
           AuditLogRequest auditLogRequest) {

      log.debug("Finding filtered alerts for user: {} with criteria: {}", userId, filterCriteria);

      try {
         List<String> userOrgKeys = userPermissionService.getDistinctOrgKeysForUser(userId);
         if (userOrgKeys.isEmpty()) {
            log.warn("No organization keys found for user: {}", userId);
            return Collections.emptyList();
         }

         // Create base criteria for org family matching
         Criteria orgCriteria = new Criteria().orOperator(
                 Criteria.where("orgFamilyTokens").in(userOrgKeys),
                 Criteria.where("orgFamily").in(userOrgKeys)
         );

         // Add active and non-deleted criteria
         Criteria baseCriteria = new Criteria().andOperator(
                 Criteria.where("isDeleted").is(false),
                 Criteria.where("isActive").is(true),
                 orgCriteria
         );

         // Combine with user filters if provided
         Criteria finalCriteria = filterCriteria != null ?
                 new Criteria().andOperator(baseCriteria, mongoQueryBuilder.buildCriteria(filterCriteria)) :
                 baseCriteria;

         // Build and execute the aggregation
         Aggregation aggregation = Aggregation.newAggregation(
                 new AggregationOperation() {
                    @Override
                    public Document toDocument(AggregationOperationContext context) {
                       return new Document("$addFields",
                               new Document("orgFamilyTokens",
                                       new Document("$split", Arrays.asList("$orgFamily", ":"))
                               )
                       );
                    }
                 },
                 Aggregation.match(finalCriteria)
         );

         AggregationResults<AlertEntity> results = mongoTemplate.aggregate(
                 aggregation,
                 CaisAlertConstants.ALERTS,
                 AlertEntity.class
         );

         List<AlertEntity> alerts = results.getMappedResults();

         // Create audit log in the same transaction
         auditTrailService.logAction(
                 auditLogRequest.getUserId(),
                 auditLogRequest.getUserRole(),
                 auditLogRequest.getActionId(),
                 "Retrieved filtered alerts for user",
                 auditLogRequest.getCategory(),
                 "Alert",
                 null,
                 null,
                 String.format("Found %d alerts for user %s", alerts.size(), userId)
         );

         log.debug("Successfully retrieved {} alerts for user: {}", alerts.size(), userId);
         return alerts;

      } catch (Exception e) {
         log.error("Error retrieving filtered alerts for user: {}", userId, e);
         throw new AlertOperationException("Failed to retrieve filtered alerts", e);
      }
   }

   @Transactional
   public Alert updateTotalScore(String alertId, int totalScore) {
      log.debug("Starting synchronized score update for alertId: {}", alertId);
      try {
         // First check if record exists in both DBs
         AlertEntity mongoAlert = alertRepository.findByAlertId(alertId);
         RdbmsAlertEntity rdbmsAlert = rdbmsAlertRepository.findByAlertId(alertId)
                 .orElseThrow(() -> new CaisNotFoundException("Alert not found in RDBMS with id: " + alertId));

         if (mongoAlert == null) {
            throw new CaisNotFoundException("Alert not found in MongoDB with id: " + alertId);
         }

         // Update MongoDB
         Query query = new Query(Criteria.where("alertId").is(alertId));
         Update update = new Update();
         update.set("totalScore", totalScore);
         update.set("lastUpdateDate", LocalDateTime.now().toString());
//         update.set("reasonDetails.reasons.scoreTotal", totalScore);

         AlertEntity updatedMongoAlert = mongoTemplate.findAndModify(
                 query,
                 update,
                 FindAndModifyOptions.options().returnNew(true),
                 AlertEntity.class,
                 CaisAlertConstants.ALERTS
         );

         if (updatedMongoAlert == null) {
            throw new CaisNotFoundException("Failed to update MongoDB alert with id: " + alertId);
         }

         // Update PostgreSQL
         rdbmsAlert.setTotalScore((double) totalScore);
         rdbmsAlert.setLastUpdateDate(LocalDateTime.now());

         // Convert MongoDB's Map to JSON string for PostgreSQL
         String reasonDetailsJson = objectMapper.writeValueAsString(updatedMongoAlert.getReasonDetails());
//         rdbmsAlert.setReasonDetails(reasonDetailsJson);

         RdbmsAlertEntity savedRdbmsAlert = rdbmsAlertRepository.save(rdbmsAlert);
         log.info("Successfully updated scores in both databases for alertId: {}", alertId);

         return alertMapper.toModel(updatedMongoAlert);
      } catch (Exception e) {
         log.error("Failed to update scores for alertId: {}. Rolling back both databases.", alertId, e);
         TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
         throw new RuntimeException("Failed to update scores in databases", e);
      }
   }

   @Transactional
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

   @Transactional
   public Alert unassignUserFromAlert(String alertId) {
      log.debug("Starting synchronized ownerId update for alertId: {}", alertId);

      try {
         // Validate if the alert exists in MongoDB
         AlertEntity mongoAlert = alertRepository.findByAlertId(alertId);
         if (mongoAlert == null) {
            throw new CaisNotFoundException("Alert not found in MongoDB with id: " + alertId);
         }

         // Validate if the alert exists in PostgreSQL
         RdbmsAlertEntity rdbmsAlert = rdbmsAlertRepository.findByAlertId(alertId)
                 .orElseThrow(() -> new CaisNotFoundException("Alert not found in RDBMS with id: " + alertId));



         // Update MongoDB
         Query query = new Query(Criteria.where("alertId").is(alertId));
         Update update = new Update();
         update.set("ownerId", "");
         update.set("ownerName", "");
         update.set("lastUpdateDate", LocalDateTime.now().toString());

         AlertEntity updatedMongoAlert = mongoTemplate.findAndModify(
                 query,
                 update,
                 FindAndModifyOptions.options().returnNew(true),
                 AlertEntity.class,
                 CaisAlertConstants.ALERTS
         );

         if (updatedMongoAlert == null) {
            throw new CaisNotFoundException("Failed to update MongoDB alert with id: " + alertId);
         }

         // Update PostgreSQL
         rdbmsAlert.setOwnerId("");
         rdbmsAlert.setOwnerName("");
         rdbmsAlert.setLastUpdateDate(LocalDateTime.now());
         RdbmsAlertEntity savedRdbmsAlert = rdbmsAlertRepository.save(rdbmsAlert);

         log.info("Successfully unassigned owner in both databases for alertId: {}", alertId);
         return alertMapper.toModel(updatedMongoAlert);
      } catch (Exception e) {
         log.error("Failed to unassign owner for alertId: {}. Rolling back both databases.", alertId, e);
         TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
         throw new RuntimeException("Failed to unassign owner in databases", e);
      }
   }

   @Transactional
   public Alert unassignUserFromAlertWithAudit(String alertId,  AuditLogRequest auditLogRequest) {
      Alert oldAlert = getAlertOnId(alertId);
      Alert updatedAlert = unassignUserFromAlert(alertId);

      auditLogRequest.setAffectedItemType("Alert");
      auditLogRequest.setAffectedItemId(alertId);
      auditLogRequest.setOldValue(oldAlert.getOwnerId());
      auditLogRequest.setNewValue("");
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


   @Transactional
   public Alert updateOwnerId(String alertId, String ownerId) {
      log.debug("Starting synchronized ownerId update for alertId: {}", alertId);

      try {
         // Validate if the alert exists in MongoDB
         AlertEntity mongoAlert = alertRepository.findByAlertId(alertId);
         if (mongoAlert == null) {
            throw new CaisNotFoundException("Alert not found in MongoDB with id: " + alertId);
         }

         // Validate if the alert exists in PostgreSQL
         RdbmsAlertEntity rdbmsAlert = rdbmsAlertRepository.findByAlertId(alertId)
                 .orElseThrow(() -> new CaisNotFoundException("Alert not found in RDBMS with id: " + alertId));

         // Fetch the user by ID from the repository
         UserEntity user = userRepository.findById(ownerId)
                 .orElseThrow(() -> new CaisNotFoundException("User not found with id: " + ownerId));

         // Extract the username
         String userName = user.getUserLoginName();

         // Update MongoDB
         Query query = new Query(Criteria.where("alertId").is(alertId));
         Update update = new Update();
         update.set("ownerId", ownerId);
         update.set("ownerName", userName);
         update.set("lastUpdateDate", LocalDateTime.now().toString());

         AlertEntity updatedMongoAlert = mongoTemplate.findAndModify(
                 query,
                 update,
                 FindAndModifyOptions.options().returnNew(true),
                 AlertEntity.class,
                 CaisAlertConstants.ALERTS
         );

         if (updatedMongoAlert == null) {
            throw new CaisNotFoundException("Failed to update MongoDB alert with id: " + alertId);
         }

         // Update PostgreSQL
         rdbmsAlert.setOwnerId(ownerId);
         rdbmsAlert.setOwnerName(userName);
         rdbmsAlert.setLastUpdateDate(LocalDateTime.now());
         RdbmsAlertEntity savedRdbmsAlert = rdbmsAlertRepository.save(rdbmsAlert);

         log.info("Successfully updated owner in both databases for alertId: {}", alertId);
         return alertMapper.toModel(updatedMongoAlert);
      } catch (Exception e) {
         log.error("Failed to update owner for alertId: {}. Rolling back both databases.", alertId, e);
         TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
         throw new RuntimeException("Failed to update owner in databases", e);
      }
   }




   @Transactional
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

   @Transactional
   public Alert updateOrgUnitId(String alertId, String orgUnitId)  {
      log.debug("Starting synchronized orgUnitId update for alertId: {}", alertId);
      try {
         // First check if record exists in both DBs
         AlertEntity mongoAlert = alertRepository.findByAlertId(alertId);
         RdbmsAlertEntity rdbmsAlert = rdbmsAlertRepository.findByAlertId(alertId)
                 .orElseThrow(() -> new CaisNotFoundException("Alert not found in RDBMS with id: " + alertId));

         if (mongoAlert == null) {
            throw new CaisNotFoundException("Alert not found in MongoDB with id: " + alertId);
         }


         Optional<OrganizationFamilyEntity> organizationTree = orgFamilyRepository.findByOrgKey(orgUnitId);
         if (organizationTree.isEmpty()) {
            throw new CaisNotFoundException("OrgFamily not found with id: " + orgUnitId);
         }

         OrganizationFamilyEntity orgEntity = organizationTree.orElseThrow(() ->
                 new CaisNotFoundException("OrgFamily not found with id: " + orgUnitId)
         );

         String orgHierarchyorg = orgEntity.getOrgFamily();


         Query query = null;
         query = new Query(Criteria.where("alertId").is(alertId));
         String previousOrgUnitId = alertRepository.findByAlertId(alertId).getOrgUnitId();
         // Update MongoDB

         Update update = new Update();
         update.set("orgUnitId", orgUnitId);
         update.set("previousOrgUnitId", previousOrgUnitId);
         update.set("orgFamily", orgHierarchyorg);
         update.set("isOrgUnitUpdated" , true);
         update.set("lastUpdateDate", LocalDateTime.now().toString());


         AlertEntity updatedMongoAlert = mongoTemplate.findAndModify(
                 query,
                 update,
                 FindAndModifyOptions.options().returnNew(true),
                 AlertEntity.class,
                 CaisAlertConstants.ALERTS
         );

         if (updatedMongoAlert == null) {
            throw new CaisNotFoundException("Failed to update MongoDB alert with id: " + alertId);
         }



         // Update PostgreSQL
         rdbmsAlert.setOrgUnitId(orgUnitId);
         rdbmsAlert.setPreviousOrgUnitId(previousOrgUnitId);
         rdbmsAlert.setOrgFamily(orgHierarchyorg);
         rdbmsAlert.setIsOrgUnitUpdated(true);
         rdbmsAlert.setLastUpdateDate(LocalDateTime.now());


         RdbmsAlertEntity savedRdbmsAlert = rdbmsAlertRepository.save(rdbmsAlert);
         log.info("Successfully updated orgId in both databases for alertId: {}", alertId);

         return alertMapper.toModel(updatedMongoAlert);
      } catch (Exception e) {
         log.error("Failed to update orgId for alertId: {}. Rolling back both databases.", alertId, e);
         TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
         throw new RuntimeException("Failed to update orgId in databases", e);
      }
   }

   @Transactional
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


   @Transactional
   public  Alert changeStep(String alertId, Long stepId)  {
      log.debug("Starting synchronized  change step update for alertId: {}", alertId);
      try {

         // First check if record exists in both DBs

         AlertEntity mongoAlert = alertRepository.findByAlertId(alertId);
         RdbmsAlertEntity rdbmsAlert = rdbmsAlertRepository.findByAlertId(alertId)
                 .orElseThrow(() -> new CaisNotFoundException("Alert not found in RDBMS with id: " + alertId));

         if (mongoAlert == null) {
            throw new CaisNotFoundException("Alert not found in MongoDB with id: " + alertId);
         }

         Step step = stepsRepository.findByStepId(stepId);
         if (step == null) {
            throw new CaisNotFoundException("Step not found with id: " + stepId);
         }

         StepStatus stepStatus = stepStatusRepository.findByStepStatusId(step.getStepStatusId());
         if (stepStatus == null) {
            throw new CaisNotFoundException("Step status not found for step id: " + stepId);
         }

         String StepName = step.getStepName();
         String Status = stepStatus.getStepName();



         Query query = null;
         query = new Query(Criteria.where("alertId").is(alertId));
         // Update MongoDB

         Update update = new Update();
         update.set("alertStepName", StepName);
         update.set("alertStepId", stepId);
         update.set("status", Status);
         update.set("lastStepUpdatedDate" , LocalDateTime.now().toString());
         update.set("lastUpdateDate", LocalDateTime.now().toString());


         AlertEntity updatedMongoAlert = mongoTemplate.findAndModify(
                 query,
                 update,
                 FindAndModifyOptions.options().returnNew(true),
                 AlertEntity.class,
                 CaisAlertConstants.ALERTS
         );

         if (updatedMongoAlert == null) {
            throw new CaisNotFoundException("Failed to update MongoDB alert with id: " + alertId);
         }

         // Update PostgreSQL
         rdbmsAlert.setStatus(Status);
         rdbmsAlert.setAlertStepId(String.valueOf(stepId));
         rdbmsAlert.setAlertStepName(StepName);
         rdbmsAlert.setLastUpdateDate(LocalDateTime.now());
         rdbmsAlert.setLastStepUpdatedDate(LocalDateTime.now());
//         rdbmsAlert.setDueDate(LocalDateTime.now()+10);


         RdbmsAlertEntity savedRdbmsAlert = rdbmsAlertRepository.save(rdbmsAlert);
         log.info("Successfully updated step in both databases for alertId: {}", alertId);

         return alertMapper.toModel(updatedMongoAlert);
      } catch (Exception e) {
         log.error("Failed to update step for alertId: {}. Rolling back both databases.", alertId, e);
         TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
         throw new RuntimeException("Failed to update osteprgId in databases", e);
      }
   }
   @Transactional
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
   @Transactional
   public Alert patchAlert(String alertId, String alertType, Alert alert) {
      log.debug("Starting synchronized patch for alertId: {}", alertId);
      try {
         // Update MongoDB
         AlertEntity updatedMongoEntity = alertRepository.patchAlert(alertMapper.toEntity(alertId, alert), alertType);

         // Update RDBMS
         rdbmsAlertRepository.findByAlertId(alertId)
                 .map(entity -> {
                    // Update all non-null fields from alert to entity
                    if (alert.getCreateDate() != null) entity.setCreateDate(LocalDateTime.parse(alert.getCreateDate()));
                    if (alert.getLastUpdateDate() != null)
                       entity.setLastUpdateDate(LocalDateTime.parse(alert.getLastUpdateDate()));
                    if (alert.getTotalScore() != null) entity.setTotalScore(alert.getTotalScore().doubleValue());
                    if (alert.getBusinessDate() != null)
                       entity.setBusinessDate(LocalDateTime.parse(alert.getBusinessDate()));
                    if (alert.getFocalEntity() != null) entity.setFocalEntity(alert.getFocalEntity());
                    if (alert.getFocus() != null) entity.setFocus(alert.getFocus());
                    if (alert.getAlertTypeId() != null) entity.setAlertTypeId(alert.getAlertTypeId());
                    if (alert.getAlertRegion() != null) entity.setAlertRegion(alert.getAlertRegion());
                    if (alert.getAlertGroupId() != null) entity.setAlertGroupId(alert.getAlertGroupId());
                    if (alert.getIsConsolidated() != null) entity.setIsConsolidated(alert.getIsConsolidated());
                    if (alert.getIsActive() != null) entity.setIsActive(alert.getIsActive());
                    if (alert.getHasMultipleScenario() != null)
                       entity.setHasMultipleScenario(alert.getHasMultipleScenario());
                    if (alert.getIsDeleted() != null) entity.setIsDeleted(alert.getIsDeleted());
                    if (alert.getOrgUnitId() != null) entity.setOrgUnitId(alert.getOrgUnitId());
                    if (alert.getOrgUnitKey() != null) entity.setOrgKey(alert.getOrgUnitKey());
                    if (alert.getOrgFamily() != null) entity.setOrgFamily(alert.getOrgFamily());
                    if (alert.getPreviousOrgUnitId() != null) entity.setPreviousOrgUnitId(alert.getPreviousOrgUnitId());
                    if (alert.getIsOrgUnitUpdated() != null) entity.setIsOrgUnitUpdated(alert.getIsOrgUnitUpdated());
                    if (alert.getIsRelatedAlert() != null) entity.setIsRelatedAlert(alert.getIsRelatedAlert());
                    if (alert.getOwnerId() != null) entity.setOwnerId(alert.getOwnerId());
                    if (alert.getOwnerName() != null) entity.setOwnerName(alert.getOwnerName());
                    if (alert.getStatus() != null) entity.setStatus(alert.getStatus());
                    if (alert.getAlertStepId() != null) entity.setAlertStepId(alert.getAlertStepId());
                    if (alert.getAlertStepName() != null) entity.setAlertStepName(alert.getAlertStepName());
                    if (alert.getIsCaseCreated() != null) entity.setIsCaseCreated(alert.getIsCaseCreated());
                    if (alert.getDetails() != null) entity.setDetails(alert.getDetails());

                    return rdbmsAlertRepository.save(entity);
                 })
                 .orElseThrow(() -> new CaisNotFoundException("Alert not found in RDBMS with id: " + alertId));

         log.debug("Successfully patched alert in both databases for alertId: {}", alertId);
         return alertMapper.toModel(updatedMongoEntity);
      } catch (Exception e) {
         log.error("Failed to patch alert for alertId: {}", alertId, e);
         throw new RuntimeException("Failed to patch alert in databases", e);
      }
   }

   @Transactional
   public Alert patchAlertWithAudit(String alertId, String alertType, Alert alert, AuditLogRequest auditLogRequest) {
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

   @Transactional
   public void deleteAlertById(String alertId, String alertType) {
      log.debug("Starting synchronized deletion for alertId: {}", alertId);
      try {
         // Delete from MongoDB
         AlertEntity deletedMongoAlert = alertRepository.deleteAlertById(alertId, alertType);
         if (deletedMongoAlert == null) {
            throw new CaisNotFoundException("Alert not found in MongoDB with id: " + alertId);
         }

         // Delete from RDBMS
         rdbmsAlertRepository.deleteByAlertId(alertId);

         log.debug("Successfully deleted alert from both databases for alertId: {}", alertId);
      } catch (Exception e) {
         log.error("Failed to delete alert for alertId: {}", alertId, e);
         throw new RuntimeException("Failed to delete alert from databases", e);
      }
   }

   @Transactional
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


   @Transactional
   public List<Alert> createAlerts(List<Alert> alerts) {
      log.debug("Starting synchronized creation of {} alerts", alerts.size());
      try {
         List<Alert> createdAlerts = new ArrayList<>();
         for (Alert alert : alerts) {
            LocalDateTime now = LocalDateTime.now();
            alert.setCreatedAt(now);
            alert.setUpdatedAt(now);
            createdAlerts.add(createAlert(alert));
         }
         log.debug("Successfully created {} alerts in both databases", alerts.size());
         return createdAlerts;
      } catch (Exception e) {
         log.error("Failed to create alerts in batch", e);
         throw new RuntimeException("Failed to create alerts in databases", e);
      }
   }

   @Transactional
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

//   @Transactional
//   public Alert createAlert(Alert alert) {
//      log.debug("Starting synchronized alert creation for alertId: {}", alert.getAlertId());
//      List<String> validationErrors = validateAlert(alert);
//      if (!validationErrors.isEmpty()) {
//         log.error("Validation failed for alert: {}", alert.getAlertId());
//         throw new AlertValidationException("Alert validation failed", validationErrors);
//      }
//
//      try {
//         // Create in MongoDB
//         LocalDateTime now = LocalDateTime.now();
//         alert.setCreateDate(String.valueOf(now));
//         alert.setLastUpdateDate(String.valueOf(now));
//         alert.setCreatedAt(now);
//         alert.setUpdatedAt(now);
//         AlertEntity mongoEntity = alertMapper.toEntity(alert);
//         AlertEntity savedMongoEntity = alertRepository.createUpsertAlert(mongoEntity);
//
//         // Create in RDBMS
//         RdbmsAlertEntity rdbmsEntity = rdbmsAlertMapper.toRdbmsEntity(alert);
//         rdbmsAlertRepository.save(rdbmsEntity);
//
//         log.debug("Successfully created alert in both databases for alertId: {}", alert.getAlertId());
//         return alertMapper.toModel(savedMongoEntity);
//      } catch (Exception e) {
//         log.error("Failed to create alert: {}", alert.getAlertId(), e);
//         throw new RuntimeException("Failed to create alert in databases", e);
//      }
//   }

   @Transactional
   public Alert createAlertWithAudit(Alert alert, AuditLogRequest auditLogRequest) {
      LocalDateTime now = LocalDateTime.now();
      alert.setCreatedAt(now);
      alert.setUpdatedAt(now);
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
      if (!StringUtils.hasText(alert.getOrgUnitId())) {
         errors.add("Org Unit is required");
      }
      if (!StringUtils.hasText(alert.getOrgFamily())) {
         errors.add("Org Family is required");
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

   @Transactional(readOnly = true)
   public StepTransitionDTO getAlertStepTransitions(String alertId) {
      log.debug("Getting step transitions for alert ID: {}", alertId);

      try {
         // Get alert details
         Alert alert = getAlertOnId(alertId);
         if (alert == null) {
            throw new EntityNotFoundException("Alert not found with ID: " + alertId);
         }

         if (alert.getAlertTypeId() == null) {
            throw new IllegalStateException("Alert type ID is not set for alert: " + alertId);
         }

         // Get alert type configuration from MongoDB
         AlertTypeExtended alertType = alertTypeServiceExtended.getAlertTypeFields(alert.getAlertTypeId());
         if (alertType == null) {
            throw new EntityNotFoundException("Alert type not found with ID: " + alert.getAlertTypeId());
         }

         // Parse field schema to get workflow ID
         Long workflowId = extractWorkflowIdFromAlertType(alertType);
         if (workflowId == null) {
            throw new IllegalStateException("No workflow ID found in alert type configuration");
         }

         if (alert.getAlertStepId() == null) {
            throw new IllegalStateException("Alert does not have a current step assigned");
         }

         Long currentStepId = Long.parseLong(alert.getAlertStepId());

         // TODO: WF transition mapping in database is enterd as 17 & 18 instead of 70 & 6 (WF ID), may be because it is inserting step id's id
         // fix the ui update first, that will fix the issue.

         // Get next steps from workflow_transition table where current step is source
         List<WorkflowTransitionEntity> nextTransitions = workflowTransitionRepository
                 .findByWorkflowWorkflowIdAndSourceStepWorkflowStepId(workflowId, currentStepId);

         // Get previous steps from workflow_transition table where current step is target
         List<WorkflowTransitionEntity> previousTransitions = workflowTransitionRepository
                 .findByWorkflowWorkflowIdAndTargetStepWorkflowStepId(workflowId, currentStepId);

         StepTransitionDTO transitionDTO = new StepTransitionDTO();

         // Map next steps
         List<StepInfo> nextSteps = nextTransitions.stream()
                 .map(transition -> {
                    StepInfo stepInfo = new StepInfo();
                    WorkflowStepEntity targetStep = transition.getTargetStep();
                    stepInfo.setStepId(targetStep.getStep().getStepId());
                    stepInfo.setLabel(targetStep.getLabel());
                    return stepInfo;
                 })
                 .collect(Collectors.toList());

         // Map previous steps
         List<StepInfo> backSteps = previousTransitions.stream()
                 .map(transition -> {
                    StepInfo stepInfo = new StepInfo();
                    WorkflowStepEntity sourceStep = transition.getSourceStep();
                    stepInfo.setStepId(sourceStep.getStep().getStepId());
                    stepInfo.setLabel(sourceStep.getLabel());
                    return stepInfo;
                 })
                 .collect(Collectors.toList());

         transitionDTO.setNextSteps(nextSteps);
         transitionDTO.setBackSteps(backSteps);

         log.debug("Found {} next steps and {} back steps for alert ID: {}",
                 nextSteps.size(), backSteps.size(), alertId);

         return transitionDTO;

      } catch (EntityNotFoundException e) {
         log.error("Entity not found: {}", e.getMessage());
         throw e;
      } catch (NumberFormatException e) {
         log.error("Invalid step ID format for alert ID: {}", alertId, e);
         throw new IllegalArgumentException("Invalid step ID format: " + e.getMessage());
      } catch (Exception e) {
         log.error("Error retrieving step transitions for alert ID: {}", alertId, e);
         throw new IllegalStateException("Error retrieving step transitions: " + e.getMessage());
      }
   }

   /**
    * Extracts workflow ID from alert type configuration
    * @param alertType The alert type containing workflow configuration
    * @return The workflow ID
    * @throws IllegalStateException if workflow ID cannot be extracted
    */
   private Long extractWorkflowIdFromAlertType(AlertTypeExtended alertType) {
      // Validate input parameter
      if (alertType == null) {
         throw new IllegalArgumentException("AlertTypeExtended cannot be null");
      }

      // Check workflow ID
      Integer wfId = alertType.getWorkflowId();
      if (wfId == null) {
         throw new IllegalStateException("Workflow ID not found in alert type configuration");
      }

      // Additional validation to ensure workflow ID is positive
      if (wfId <= 0) {
         throw new IllegalArgumentException("Workflow ID must be a positive integer");
      }

      // Convert and return the workflow ID
      return Long.valueOf(wfId);
   }
}





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
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.StringUtils;
//
//import javax.validation.Valid;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//@Transactional
//public class AlertService {
//   private final AlertMapper alertMapper;
//   private final StepRepository stepsRepository;
//   private final StepStatusRepository stepStatusRepository;
//   private final MongoTemplate mongoTemplate;
//   private final AlertRepository alertRepository;
//   private final AuditTrailService auditTrailService;
//   private final UserPermissionService userPermissionService;
//
//   // Base Operations
//   public List<Alert> getAllActiveAlerts() {
//      log.debug("Fetching all active and non-deleted alerts");
//      List<AlertEntity> activeAlertEntities = alertRepository.findAllActiveAndNonDeletedAlerts();
//      return activeAlertEntities.stream()
//              .map(alertMapper::toModel)
//              .collect(Collectors.toList());
//   }
//
//   public List<Alert> getAllActiveAlertsWithAudit(AuditLogRequest auditLogRequest) {
//      List<Alert> activeAlerts = getAllActiveAlerts();
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setDescription("Fetched all active and non-deleted alerts");
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              null,
//              null,
//              "Fetched " + activeAlerts.size() + " alerts");
//
//      return activeAlerts;
//   }
//
//   // Alert Retrieval Operations
//   public Alert getAlertOnId(final String alertId) {
//      log.debug("Fetching alert with ID: {}", alertId);
//      AlertEntity alertById = alertRepository.getAlertOnId(alertId);
//      if (alertById == null) {
//         throw new CaisNotFoundException("Alert not found with ID: " + alertId);
//      }
//      return alertMapper.toModel(alertById);
//   }
//
//   public Alert getAlertOnId(final String alertId, AuditLogRequest auditLogRequest) {
//      Alert alert = getAlertOnId(alertId);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(),
//              null,
//              alert.toString());
//
//      return alert;
//   }
//
//   public List<AlertEntity> findAlertsByOrgFamily(String searchstring) {
//      log.debug("Finding alerts by org family: {}", searchstring);
//      return alertRepository.findAlertsByOrgFamily(searchstring);
//   }
//
//   public List<AlertEntity> findAlertsByOrgFamilyWithAudit(String searchstring, AuditLogRequest auditLogRequest) {
//      List<AlertEntity> alerts = findAlertsByOrgFamily(searchstring);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setDescription("Searched alerts by org family: " + searchstring);
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              null,
//              null,
//              "Found " + alerts.size() + " alerts");
//
//      return alerts;
//   }
//
//   public List<AlertEntity> findAlertsByOrg(String searchstring) {
//      log.debug("Finding alerts by org: {}", searchstring);
//      return alertRepository.findAlertsByOrg(searchstring);
//   }
//
//   public List<AlertEntity> findAlertsByOrgWithAudit(String searchstring, AuditLogRequest auditLogRequest) {
//      List<AlertEntity> alerts = findAlertsByOrg(searchstring);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setDescription("Searched alerts by org: " + searchstring);
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              null,
//              null,
//              "Found " + alerts.size() + " alerts");
//
//      return alerts;
//   }
//
//   public List<AlertEntity> findAlertsByOrgFamilyByUserOrgKeys(String userId) {
//      log.debug("Finding alerts for user: {}", userId);
//      List<String> userOrgKeys = userPermissionService.getDistinctOrgKeysForUser(userId);
//      return alertRepository.findAlertsByOrgFamilyBYUserOrgKeys(userOrgKeys);
//   }
//
//   public List<AlertEntity> findAlertsByOrgFamilyByUserOrgKeysWithAudit(String userId, AuditLogRequest auditLogRequest) {
//      List<AlertEntity> alerts = findAlertsByOrgFamilyByUserOrgKeys(userId);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setDescription("Fetched alerts for user's org units");
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              null,
//              null,
//              "Found " + alerts.size() + " alerts for user " + userId);
//
//      return alerts;
//   }
//
//   // Alert Update Operations
//   public Alert updateTotalScore(String alertId, int totalScore) {
//      log.debug("Updating total score for alert: {} to {}", alertId, totalScore);
//      return alertRepository.updateTotalScore(alertId, totalScore);
//   }
//
//   public Alert updateTotalScoreWithAudit(String alertId, int totalScore, AuditLogRequest auditLogRequest) {
//      Alert oldAlert = getAlertOnId(alertId);
//      Alert updatedAlert = updateTotalScore(alertId, totalScore);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditLogRequest.setOldValue(oldAlert.getTotalScore().toString());
//      auditLogRequest.setNewValue(String.valueOf(totalScore));
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(),
//              auditLogRequest.getOldValue(),
//              auditLogRequest.getNewValue());
//
//      return updatedAlert;
//   }
//
//   public Alert updateOwnerId(String alertId, String ownerId) {
//      log.debug("Updating owner for alert: {} to {}", alertId, ownerId);
//      return alertRepository.updateOwnerId(alertId, ownerId);
//   }
//
//   public Alert updateOwnerIdWithAudit(String alertId, String ownerId, AuditLogRequest auditLogRequest) {
//      Alert oldAlert = getAlertOnId(alertId);
//      Alert updatedAlert = updateOwnerId(alertId, ownerId);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditLogRequest.setOldValue(oldAlert.getOwnerId());
//      auditLogRequest.setNewValue(ownerId);
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(),
//              auditLogRequest.getOldValue(),
//              auditLogRequest.getNewValue());
//
//      return updatedAlert;
//   }
//
//   public Alert updateOrgUnitId(String alertId, String orgUnitId) {
//      log.debug("Updating org unit for alert: {} to {}", alertId, orgUnitId);
//      return alertRepository.updateOrgUnitId(alertId, orgUnitId);
//   }
//
//   public Alert updateOrgUnitIdWithAudit(String alertId, String orgUnitId, AuditLogRequest auditLogRequest) {
//      Alert oldAlert = getAlertOnId(alertId);
//      Alert updatedAlert = updateOrgUnitId(alertId, orgUnitId);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditLogRequest.setOldValue(oldAlert.getOrgUnitId());
//      auditLogRequest.setNewValue(orgUnitId);
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(),
//              auditLogRequest.getOldValue(),
//              auditLogRequest.getNewValue());
//
//      return updatedAlert;
//   }
//
//   public Alert updateStatus(String alertId, String statusId) {
//      log.debug("Updating status for alert: {} to {}", alertId, statusId);
//      return alertRepository.updateStatus(alertId, statusId);
//   }
//
//   public Alert updateStatusWithAudit(String alertId, String statusId, AuditLogRequest auditLogRequest) {
//      Alert oldAlert = getAlertOnId(alertId);
//      Alert updatedAlert = updateStatus(alertId, statusId);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditLogRequest.setOldValue(oldAlert.getStatus());
//      auditLogRequest.setNewValue(statusId);
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(),
//              auditLogRequest.getOldValue(),
//              auditLogRequest.getNewValue());
//
//      return updatedAlert;
//   }
//   // Step Management Operations
//   public Alert changeStep(String alertId, Long stepId) {
//      log.debug("Changing step for alert: {} to step: {}", alertId, stepId);
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
//      AlertEntity updatedEntity = alertRepository.changeStep(alertId, stepId, step.getStepName(), stepStatus.getStepName());
//      if (updatedEntity == null) {
//         throw new CaisNotFoundException("Alert not found with id: " + alertId);
//      }
//      return alertMapper.toModel(updatedEntity);
//   }
//
//   public Alert changeStepWithAudit(String alertId, Long stepId, AuditLogRequest auditLogRequest) {
//      Alert oldAlert = getAlertOnId(alertId);
//      Alert updatedAlert = changeStep(alertId, stepId);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditLogRequest.setOldValue(oldAlert.getAlertStepName());
//      auditLogRequest.setNewValue(updatedAlert.getAlertStepName());
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(),
//              auditLogRequest.getOldValue(),
//              auditLogRequest.getNewValue());
//
//      return updatedAlert;
//   }
//
//   // Alert Search Operations
//   public List<AlertEntity> findAlertsByCriteria(
//           String alertId, String createDate, String lastUpdateDate, String totalScore,
//           String createdBy, String businessDate, String focalEntity, String focus, String alertTypeId,
//           String alertRegion, String alertGroupId, Boolean isConsolidated, Boolean isActive,
//           Boolean hasMultipleScenario, Boolean isDeleted, String orgUnitId, String orgFamily,
//           String previousOrgUnitId, Boolean isOrgUnitUpdated, Boolean isRelatedAlert, String ownerId,
//           String ownerName, String status, String alertStepId, String alertStepName, Boolean isCaseCreated) {
//
//      log.debug("Finding alerts by criteria");
//      return alertRepository.findAlertsByCriteria(
//              alertId, createDate, lastUpdateDate, totalScore, createdBy, businessDate,
//              focalEntity, focus, alertTypeId, alertRegion, alertGroupId, isConsolidated,
//              isActive, hasMultipleScenario, isDeleted, orgUnitId, orgFamily, previousOrgUnitId,
//              isOrgUnitUpdated, isRelatedAlert, ownerId, ownerName, status, alertStepId,
//              alertStepName, isCaseCreated);
//   }
//
//   public List<AlertEntity> findAlertsByCriteriaWithAudit(
//           String alertId, String createDate, String lastUpdateDate, String totalScore,
//           String createdBy, String businessDate, String focalEntity, String focus, String alertTypeId,
//           String alertRegion, String alertGroupId, Boolean isConsolidated, Boolean isActive,
//           Boolean hasMultipleScenario, Boolean isDeleted, String orgUnitId, String orgFamily,
//           String previousOrgUnitId, Boolean isOrgUnitUpdated, Boolean isRelatedAlert, String ownerId,
//           String ownerName, String status, String alertStepId, String alertStepName, Boolean isCaseCreated,
//           AuditLogRequest auditLogRequest) {
//
//      List<AlertEntity> alerts = findAlertsByCriteria(
//              alertId, createDate, lastUpdateDate, totalScore, createdBy, businessDate,
//              focalEntity, focus, alertTypeId, alertRegion, alertGroupId, isConsolidated,
//              isActive, hasMultipleScenario, isDeleted, orgUnitId, orgFamily, previousOrgUnitId,
//              isOrgUnitUpdated, isRelatedAlert, ownerId, ownerName, status, alertStepId,
//              alertStepName, isCaseCreated);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setDescription("Searched alerts by criteria");
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              null,
//              null,
//              "Found " + alerts.size() + " alerts");
//
//      return alerts;
//   }
//
//   // Alert Management Operations
//   public Map<String, Object> getAllAlerts(String name, String state, List<String> accountNumbers,
//                                           List<String> owners, List<String> assignees, Date createdDateFrom,
//                                           Date createdDateTo, @Valid int limit, @Valid int offset) {
//      log.debug("Getting all alerts with filters");
//      validateRequestParams(name, state, accountNumbers, owners, assignees, createdDateFrom,
//              createdDateTo, offset, limit);
//
//      try {
//         List<AlertEntity> allAlertEntities = alertRepository.getAllAlerts(name, state, accountNumbers,
//                 owners, assignees, createdDateFrom, createdDateTo, offset, limit);
//
//         List<Alert> allAlerts = allAlertEntities.stream()
//                 .map(alertMapper::toModel)
//                 .collect(Collectors.toList());
//
//         Map<String, Object> response = new HashMap<>();
//         response.put("alerts", allAlerts);
//         response.put("count", allAlerts.size());
//         return response;
//      } catch (Exception e) {
//         log.error("Error retrieving alerts", e);
//         throw new CaisBaseException("Error retrieving alerts");
//      }
//   }
//
//   public Map<String, Object> getAllAlertsWithAudit(String name, String state, List<String> accountNumbers,
//                                                    List<String> owners, List<String> assignees, Date createdDateFrom,
//                                                    Date createdDateTo, @Valid int limit, @Valid int offset,
//                                                    AuditLogRequest auditLogRequest) {
//      Map<String, Object> response = getAllAlerts(name, state, accountNumbers, owners, assignees,
//              createdDateFrom, createdDateTo, limit, offset);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setDescription("Retrieved filtered alerts");
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              null,
//              null,
//              "Retrieved " + response.get("count") + " alerts");
//
//      return response;
//   }
//
//   public Alert patchAlert(String alertId, String alertType, Alert alert) {
//      log.debug("Patching alert: {} of type: {}", alertId, alertType);
//      AlertEntity upsertedAlert = alertRepository.patchAlert(alertMapper.toEntity(alertId, alert), alertType);
//      return alertMapper.toModel(upsertedAlert);
//   }
//
//   public Alert patchAlertWithAudit(String alertId, String alertType, Alert alert,
//                                    AuditLogRequest auditLogRequest) {
//      Alert oldAlert = getAlertById(alertId, alertType);
//      Alert updatedAlert = patchAlert(alertId, alertType, alert);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditLogRequest.setOldValue(oldAlert.toString());
//      auditLogRequest.setNewValue(updatedAlert.toString());
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(),
//              auditLogRequest.getOldValue(),
//              auditLogRequest.getNewValue());
//
//      return updatedAlert;
//   }
//
//   public void deleteAlertById(String alertId, String alertType) {
//      log.debug("Deleting alert: {} of type: {}", alertId, alertType);
//      AlertEntity alertById = alertRepository.deleteAlertById(alertId, alertType);
//      if (alertById == null) {
//         throw new CaisNotFoundException("Alert not found with id: " + alertId);
//      }
//   }
//
//   public void deleteAlertByIdWithAudit(String alertId, String alertType, AuditLogRequest auditLogRequest) {
//      Alert alertToDelete = getAlertById(alertId, alertType);
//      deleteAlertById(alertId, alertType);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditLogRequest.setOldValue(alertToDelete.toString());
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(),
//              auditLogRequest.getOldValue(),
//              auditLogRequest.getNewValue());
//   }
//   // Bulk Operations
//   public List<Alert> createAlerts(List<Alert> alerts) {
//      log.debug("Creating {} bulk alerts", alerts.size());
//      return alerts.stream()
//              .map(this::createAlert)
//              .collect(Collectors.toList());
//   }
//
//   public List<Alert> createAlertsWithAudit(List<Alert> alerts, AuditLogRequest auditLogRequest) {
//      List<Alert> createdAlerts = createAlerts(alerts);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setDescription("Created bulk alerts");
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              null,
//              null,
//              "Created " + createdAlerts.size() + " alerts");
//
//      return createdAlerts;
//   }
//
//   public Alert createAlert(Alert alert) {
//      log.debug("Creating new alert");
//      List<String> validationErrors = validateAlert(alert);
//      if (!validationErrors.isEmpty()) {
//         throw new AlertValidationException("Alert validation failed", validationErrors);
//      }
//
//      AlertEntity alertEntity = alertMapper.toEntity(alert);
//      AlertEntity upsertedAlert = alertRepository.createUpsertAlert(alertEntity);
//      return alertMapper.toModel(upsertedAlert);
//   }
//
//   public Alert createAlertWithAudit(Alert alert, AuditLogRequest auditLogRequest) {
//      Alert createdAlert = createAlert(alert);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(createdAlert.getAlertId());
//      auditLogRequest.setNewValue(createdAlert.toString());
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(),
//              null,
//              auditLogRequest.getNewValue());
//
//      return createdAlert;
//   }
//
//   public Alert getAlertById(final String alertId, String alertType) {
//      log.debug("Getting alert by ID: {} and type: {}", alertId, alertType);
//      AlertEntity alertById = alertRepository.getAlertById(alertId, alertType);
//      if (alertById == null) {
//         throw new CaisNotFoundException("Alert not found with id: " + alertId);
//      }
//      return alertMapper.toModel(alertById);
//   }
//
//   public Alert getAlertByIdWithAudit(final String alertId, String alertType,
//                                      AuditLogRequest auditLogRequest) {
//      Alert alert = getAlertById(alertId, alertType);
//
//      auditLogRequest.setAffectedItemType("Alert");
//      auditLogRequest.setAffectedItemId(alertId);
//      auditTrailService.logAction(auditLogRequest.getUserId(),
//              auditLogRequest.getUserRole(),
//              auditLogRequest.getActionId(),
//              auditLogRequest.getDescription(),
//              auditLogRequest.getCategory(),
//              auditLogRequest.getAffectedItemType(),
//              auditLogRequest.getAffectedItemId(),
//              null,
//              alert.toString());
//
//      return alert;
//   }
//
//   // Validation Methods
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
//   private void validateRequestParams(String name, String state, List<String> accountNumbers,
//                                      List<String> owners, List<String> assignees, Date createdDateFrom,
//                                      Date createdDateTo, int offset, int limit) {
//      StringBuilder errorMessage = new StringBuilder();
//
//      if (name != null && !name.isEmpty() && name.length() > 20) {
//         errorMessage.append("name cannot be longer than 20 characters;");
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
//      if (createdDateFrom != null && createdDateTo != null && createdDateFrom.after(createdDateTo)) {
//         errorMessage.append("from date cannot be after to date;");
//      }
//
//      if (limit < 0) {
//         errorMessage.append("limit cannot be negative;");
//      }
//      if (offset < 0) {
//         errorMessage.append("offset cannot be negative;");
//      }
//
//      if (errorMessage.length() > 0) {
//         throw new CaisIllegalArgumentException(errorMessage.toString());
//      }
//   }
//}
//
