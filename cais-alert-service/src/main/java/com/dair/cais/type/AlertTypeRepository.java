package com.dair.cais.type;

import com.dair.cais.common.config.CaisAlertConstants;
import com.dair.exception.CaisBaseException;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public class AlertTypeRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    //fetch alerttype fields from Mongo
    public AlertTypeEntity getAlertTypeFields(String alertType) {
        Query query = new Query();
        query.addCriteria(Criteria.where("alertType").is(alertType));
        AlertTypeEntity typeEntity = mongoTemplate.findOne(query, AlertTypeEntity.class, CaisAlertConstants.CAIS_ALERT_TYPE_COLLECTION_NAME);
        return typeEntity;
    }


    public AlertTypeEntity createUpsertAlertType(AlertTypeEntity typeEntity) {
        AlertTypeEntity alEntity = mongoTemplate.save(typeEntity, CaisAlertConstants.ALERT_TYPE_COLLECTION_NAME);
        return alEntity;
    }

    public AlertTypeEntity patchAlertType(AlertTypeEntity typeEntity, String typeType) {

        Update update = new Update();

        for (Field field : typeEntity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Optional<Object> fieldValue;
            try {
                fieldValue = Optional.ofNullable(field.get(typeEntity));
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

        Query query = new Query(Criteria.where("id").is(typeEntity.getId()));
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);

        AlertTypeEntity modifiedAlertTypeEntity = mongoTemplate.findAndModify(query, update,
                options, AlertTypeEntity.class, CaisAlertConstants.ALERT_TYPE_COLLECTION_NAME);

        if (modifiedAlertTypeEntity == null) {
            throw new CaisBaseException(String.format("AlertType not found with id: %s", typeEntity.getId()));
        }
        return modifiedAlertTypeEntity;
    }

    public AlertTypeEntity getAlertTypeById(String typeId) {
        ObjectId id = new ObjectId(typeId);
        AlertTypeEntity typeEntity = mongoTemplate
                .findById(id, AlertTypeEntity.class, CaisAlertConstants.CAIS_ALERT_TYPE_COLLECTION_NAME);
        return typeEntity;
    }


     public List<AlertTypeEntity> getAllAlertTypes(String name, String state, List<String> accountNumbers,
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

        List<AlertTypeEntity> typeEntities = new ArrayList<>();
        List<AlertTypeEntity> documents = mongoTemplate.find(query, AlertTypeEntity.class,
                CaisAlertConstants.ALERT_TYPE_COLLECTION_NAME);
        typeEntities.addAll(documents);
        return typeEntities;
    }

}