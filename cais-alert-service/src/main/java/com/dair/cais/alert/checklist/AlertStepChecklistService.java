package com.dair.cais.alert.checklist;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class AlertStepChecklistService {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public AlertStepChecklistService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public AlertStepChecklist saveAlertStepChecklist(AlertStepChecklist alertStepChecklist) {
        alertStepChecklist.setCreatedDate(LocalDateTime.now());
        return mongoTemplate.save(alertStepChecklist, "AlertStepChecklist");
    }
}