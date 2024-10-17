package com.dair.cais.alert;

import com.dair.cais.common.config.CaisAlertConstants;
import com.dair.exception.CaisBaseException;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperation;
import org.springframework.data.mongodb.core.aggregation.AggregationOperationContext;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
public class AlertRepository {

    @Autowired
    private MongoTemplate mongoTemplate;


    public List<AlertEntity> findAllActiveAndNonDeletedAlerts() {
        Query query = new Query();
        query.addCriteria(Criteria.where("isDeleted").is(false));
        query.addCriteria(Criteria.where("isActive").is(true));

        return mongoTemplate.find(query, AlertEntity.class, CaisAlertConstants.ALERTS);
    }

    public List<AlertEntity> findAlertsByOrgFamily(String substring) {
        // Define the query criteria
        Query query = new Query();
        query.addCriteria(Criteria.where("orgFamily").regex(substring, "i"));
        query.addCriteria(Criteria.where("isDeleted").is(false));
        query.addCriteria(Criteria.where("isActive").is(true));

        // Execute the query and return the results
        return mongoTemplate.find(query, AlertEntity.class, CaisAlertConstants.ALERTS);
    }


    public List<AlertEntity> findAlertsByOrgFamilyBYUserOrgKeys(List<String> orgKeys) {
        AggregationOperation matchActiveNonDeleted = Aggregation.match(
                Criteria.where("isDeleted").is(false).and("isActive").is(true)
        );

        AggregationOperation splitOrgFamily = new AggregationOperation() {
            @Override
            public Document toDocument(AggregationOperationContext context) {
                return new Document("$addFields",
                        new Document("orgFamilyTokens",
                                new Document("$split", Arrays.asList("$orgFamily", ":"))
                        )
                );
            }
        };

        AggregationOperation matchOrgKeys = Aggregation.match(
                new Criteria().orOperator(
                        Criteria.where("orgFamilyTokens").in(orgKeys),
                        Criteria.where("orgFamily").in(orgKeys)
                )
        );

        Aggregation aggregation = Aggregation.newAggregation(
                matchActiveNonDeleted,
                splitOrgFamily,
                matchOrgKeys
        );

        AggregationResults<AlertEntity> results = mongoTemplate.aggregate(
                aggregation,
                CaisAlertConstants.ALERTS,
                AlertEntity.class
        );

        return results.getMappedResults();
    }





    public List<AlertEntity> findAlertsByOrg(String substring) {
        // Define the query criteria
        Query query_org = new Query();
        query_org.addCriteria(Criteria.where("orgUnitId").is(substring));

        // Execute the query and return the results
        return mongoTemplate.find(query_org, AlertEntity.class, CaisAlertConstants.ALERTS);
    }


    public AlertEntity insertAlert(AlertEntity alertEntity) {
        return mongoTemplate.save(alertEntity, CaisAlertConstants.ALERTS);
    }



    public List<AlertEntity> findAlertsByCriteria(
            String alertId, String createDate, String lastUpdateDate, String totalScore,
            String createdBy, String businessDate, String focalEntity, String focus, String alertTypeId,
            String alertRegion, String alertGroupId, Boolean isConsolidated, Boolean isActive,
            Boolean hasMultipleScenario, Boolean isDeleted, String orgUnitId, String orgFamily,
            String previousOrgUnitId, Boolean isOrgUnitUpdated, Boolean isRelatedAlert, String ownerId,
            String ownerName, String status, String alertStepId, String alertStepName, Boolean isCaseCreated
    ) {
        Query query = new Query();

        if (alertId != null) query.addCriteria(Criteria.where("alertId").regex(alertId, "i"));
        if (createDate != null) query.addCriteria(Criteria.where("createDate").regex(createDate, "i"));
        if (lastUpdateDate != null) query.addCriteria(Criteria.where("lastUpdateDate").regex(lastUpdateDate, "i"));

        // Parse the totalScore parameter
        if (totalScore != null) {
            Matcher matcher = Pattern.compile("([<>=!]*)(\\d+)").matcher(totalScore);
            if (matcher.matches()) {
                String operator = matcher.group(1);
                Integer score = Integer.parseInt(matcher.group(2));

                switch (operator) {
                    case "<=":
                        query.addCriteria(Criteria.where("totalScore").lte(score));
                        break;
                    case ">=":
                        query.addCriteria(Criteria.where("totalScore").gte(score));
                        break;
                    case "!=":
                        query.addCriteria(Criteria.where("totalScore").ne(score));
                        break;
                    case "<":
                        query.addCriteria(Criteria.where("totalScore").lt(score));
                        break;
                    case ">":
                        query.addCriteria(Criteria.where("totalScore").gt(score));
                        break;
                    default:
                        query.addCriteria(Criteria.where("totalScore").is(score));
                        break;
                }
            }
        }

        if (createdBy != null) query.addCriteria(Criteria.where("createdBy").regex(createdBy, "i"));
        if (businessDate != null) query.addCriteria(Criteria.where("businessDate").regex(businessDate, "i"));
        if (focalEntity != null) query.addCriteria(Criteria.where("focalEntity").regex(focalEntity, "i"));
        if (focus != null) query.addCriteria(Criteria.where("focus").regex(focus, "i"));
        if (alertTypeId != null) query.addCriteria(Criteria.where("alertTypeId").regex(alertTypeId, "i"));
        if (alertRegion != null) query.addCriteria(Criteria.where("alertRegion").regex(alertRegion, "i"));
        if (alertGroupId != null) query.addCriteria(Criteria.where("alertGroupId").regex(alertGroupId, "i"));
        if (isConsolidated != null) query.addCriteria(Criteria.where("isConsolidated").is(isConsolidated));
        if (isActive != null) query.addCriteria(Criteria.where("isActive").is(isActive));
        if (hasMultipleScenario != null) query.addCriteria(Criteria.where("hasMultipleScenario").is(hasMultipleScenario));
        if (isDeleted != null) query.addCriteria(Criteria.where("isDeleted").is(isDeleted));
        if (orgUnitId != null) query.addCriteria(Criteria.where("orgUnitId").regex(orgUnitId, "i"));
        if (orgFamily != null) query.addCriteria(Criteria.where("orgFamily").regex(orgFamily, "i"));
        if (previousOrgUnitId != null) query.addCriteria(Criteria.where("previousOrgUnitId").regex(previousOrgUnitId, "i"));
        if (isOrgUnitUpdated != null) query.addCriteria(Criteria.where("isOrgUnitUpdated").is(isOrgUnitUpdated));
        if (isRelatedAlert != null) query.addCriteria(Criteria.where("isRelatedAlert").is(isRelatedAlert));
        if (ownerId != null) query.addCriteria(Criteria.where("ownerId").regex(ownerId, "i"));
        if (ownerName != null) query.addCriteria(Criteria.where("ownerName").regex(ownerName, "i"));
        if (status != null) query.addCriteria(Criteria.where("status").regex(status, "i"));
        if (alertStepId != null) query.addCriteria(Criteria.where("alertStepId").regex(alertStepId, "i"));
        if (alertStepName != null) query.addCriteria(Criteria.where("alertStepName").regex(alertStepName, "i"));
        if (isCaseCreated != null) query.addCriteria(Criteria.where("isCaseCreated").is(isCaseCreated));

        return mongoTemplate.find(query, AlertEntity.class, "alerts");
    }


    public Alert updateTotalScore(String alertId, int totalScore) {
        Query query = new Query();
        query.addCriteria(Criteria.where("alertId").is(alertId));
        Update update = new Update();
        update.set("totalScore", totalScore);

        Alert updatedAlert = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Alert.class
        );

        if (updatedAlert == null) {
            throw new CaisBaseException(String.format("Alert not found with id: %s", alertId));
        }

        return updatedAlert;
    }

    public Alert updateOwnerId(String alertId, String ownerId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("alertId").is(alertId));
        Update update = new Update();
        update.set("ownerId", ownerId);

        Alert updatedAlert = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Alert.class
        );

        if (updatedAlert == null) {
            throw new CaisBaseException(String.format("Alert not found with id: %s", alertId));
        }

        return updatedAlert;
    }

    public Alert updateOrgUnitId(String alertId, String orgUnitId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("alertId").is(alertId));
        Update update = new Update();
        update.set("previousOrgUnitId", mongoTemplate.findOne(query, Alert.class).getOrgUnitId());
        update.set("orgUnitId", orgUnitId);

        Alert updatedAlert = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Alert.class
        );

        if (updatedAlert == null) {
            throw new CaisBaseException(String.format("Alert not found with id: %s", alertId));
        }

        return updatedAlert;
    }


    public Alert updateStatus(String alertId, String statusId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("alertId").is(alertId));
        Update update = new Update();
        update.set("status", statusId);

        Alert updatedAlert = mongoTemplate.findAndModify(
                query,
                update,
                FindAndModifyOptions.options().returnNew(true),
                Alert.class
        );

        if (updatedAlert == null) {
            throw new CaisBaseException(String.format("Alert not found with id: %s", alertId));
        }

        return updatedAlert;
    }

    public AlertEntity changeStep(String alertId, Long stepId, String stepName, String statusName) {
        Query query = new Query(Criteria.where("alertId").is(alertId));
        Update update = new Update()
                .set("alertStepId", stepId)
                .set("alertStepName", stepName)
                .set("status", statusName)
                .set("lastUpdateDate", LocalDateTime.now());

        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true);

        return mongoTemplate.findAndModify(query, update, options, AlertEntity.class, CaisAlertConstants.ALERTS);
    }

    public AlertEntity createUpsertAlert(AlertEntity alertEntity) {
        String collectionName = extractCollectionName(alertEntity);
        AlertEntity alEntity = mongoTemplate.save(alertEntity, collectionName);
        return alEntity;
    }

    public AlertEntity getAlertById(String alertId, String alertType) {
        // Determine the collection name based on alertType or default to ALERTS
        String collectionName;
        if (alertType == null) {
            collectionName = CaisAlertConstants.ALERTS;
        } else {
            collectionName = extractCollectionName(alertType);
        }

        // Convert alertId to ObjectId if your MongoDB _id is of type ObjectId
        // If _id is a String, you can remove this conversion
        ObjectId id;
        try {
            id = new ObjectId(alertId);
        } catch (IllegalArgumentException e) {
            // Handle the case where alertId is not a valid ObjectId
            return null; // or throw an exception depending on your use case
        }

        // Fetch the alert entity from the database
        AlertEntity alertEntity = mongoTemplate.findById(id, AlertEntity.class, collectionName);

        return alertEntity;
    }

    public AlertEntity  getAlertOnId(String alertId) {
        // Define the collection name, assuming it's fixed
        String collectionName = "alerts";

        // Create a query object that matches the alertId field
        Query query = new Query();
//        query.addCriteria(Criteria.where("alertId").is(alertId));
        query.addCriteria(Criteria.where("alertId").is(alertId));
        query.addCriteria(Criteria.where("isDeleted").is(false));
        query.addCriteria(Criteria.where("isActive").is(true));


        // Use findOne to execute the query
        AlertEntity alertEntity = mongoTemplate.findOne(query, AlertEntity.class, collectionName);

        return alertEntity;
    }

    public AlertEntity patchAlert(AlertEntity alertEntity, String alertType) {
        String collectionName = extractCollectionName(alertType);
        Update update = new Update();

        for (Field field : alertEntity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Optional<Object> fieldValue;
            try {
                fieldValue = Optional.ofNullable(field.get(alertEntity));
                if (fieldValue.isPresent()
                        && field.getName() != "serialVersionUID"
                        && field.getName() != "id") {
                    System.out.println(field.getName() + ": " + fieldValue.get());
                    update.set(field.getName(), fieldValue.get());
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        Query query = new Query(Criteria.where("id").is(alertEntity.getId()));
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);

        AlertEntity modifiedAlertEntity = mongoTemplate.findAndModify(query, update,
                options, AlertEntity.class, collectionName);

        if (modifiedAlertEntity == null) {
            throw new CaisBaseException(String.format("Alert not found with id: %s", alertEntity.getId()));
        }
        return modifiedAlertEntity;
    }

    public AlertEntity deleteAlertById(String alertId, String alertType) {
        String collectionName = extractCollectionName(alertType);
        Query query = new Query(Criteria.where("id").is(alertId));
        AlertEntity alertEntity = mongoTemplate
                .findAndRemove(query, AlertEntity.class, collectionName);
        return alertEntity;
    }

    public List<AlertEntity> getAllAlerts(String name, String state, List<String> accountNumbers,
            List<String> owners, List<String> assignees, Date createdDateFrom, Date createdDateTo, int offset,
            int limit) {
        final Query query = new Query();
        final List<Criteria> criteria = new ArrayList<>();

        if (state != null && !state.isEmpty())
            criteria.add(Criteria.where("state").regex("^" + state + "$", "i"));
        if (accountNumbers != null && !accountNumbers.isEmpty())
            criteria.add(Criteria.where("accountNumber").in(accountNumbers));
        if (owners != null && !owners.isEmpty())
            criteria.add(Criteria.where("owner").in(owners));
        if (assignees != null && !assignees.isEmpty())
            criteria.add(Criteria.where("assignee").in(assignees));
        if (createdDateFrom != null)
            criteria.add(Criteria.where("createdDate").gte(createdDateFrom));
        if (createdDateTo != null)
            criteria.add(Criteria.where("createdDate").lte(createdDateTo));

        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));

//        List<AlertEntity> alertEntities = new ArrayList<>();
//        for (String collectionName : CaisAlertConstants.allAlertTypes) {
//            List<AlertEntity> documents = mongoTemplate.find(query, AlertEntity.class, collectionName);
//            alertEntities.addAll(documents);
//        }


        List<AlertEntity> alertEntity = mongoTemplate.find(query, AlertEntity.class, CaisAlertConstants.ALERTS);
        return alertEntity;
    }

    private String extractCollectionName(AlertEntity alertEntity) {
        String collectionName = CaisAlertConstants.ALERTS;
//        if (CaisAlertConstants.validAlertTypes.contains(alertEntity.getType().toLowerCase())) {
//          if (CaisAlertConstants.validAlertTypes.contains(alertEntity.getAlertTypeId().toLowerCase())) {
////            collectionName = alertEntity.getType();
//            collectionName = alertEntity.getAlertTypeId();
//        }
        return collectionName.toLowerCase();

    }



    private String extractCollectionName(String alertType) {
        String collectionName = CaisAlertConstants.ALERTS;
        if (CaisAlertConstants.validAlertTypes.contains(alertType.toLowerCase())) {
            collectionName = alertType;
        }
        return collectionName.toLowerCase();
    }

//    private String extractCollectionName(String alertType) {
//        String collectionName = CaisAlertConstants.OTHER;
//        if (CaisAlertConstants.validAlertTypes.contains(alertType.toLowerCase())) {
//            collectionName = alertType;
//        }
//        return collectionName.toLowerCase();
//    }

}