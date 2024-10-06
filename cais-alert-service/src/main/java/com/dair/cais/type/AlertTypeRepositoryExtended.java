package com.dair.cais.type;

import com.dair.cais.common.config.CaisAlertConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class AlertTypeRepositoryExtended {

    @Autowired
    private MongoTemplate mongoTemplate;

    // Fetch alert type fields from MongoDB
    public AlertTypeEntityExtended getAlertTypeFields(String alertTypeId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("alertTypeId").is(alertTypeId));
        return mongoTemplate.findOne(query, AlertTypeEntityExtended.class, "alertTypes");
    }

    public List<AlertTypeEntityExtended> fetchAllAlertTypes() {
        final Query query = new Query();
        final List<Criteria> criteria = new ArrayList<>();
        List<AlertTypeEntityExtended> typeEntities = new ArrayList<>();
        List<AlertTypeEntityExtended> documents = mongoTemplate.find(query, AlertTypeEntityExtended.class,
                CaisAlertConstants.CAIS_ALERT_TYPE_COLLECTION_NAME);
        typeEntities.addAll(documents);
        return typeEntities;
    }
}
