package com.dair.cais.attachment;

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

import static com.dair.cais.common.config.CaisAlertConstants.MONGO_COLLECTION_ALERT_ATTACHMENTS;

@Repository
public class AttachmentRepository {

    private final MongoTemplate mongoTemplate;

    @Autowired
    public AttachmentRepository(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public ExtendedAttachment save(ExtendedAttachment attachment) {
        return mongoTemplate.save(attachment, "attachments"); // Assuming 'attachments' is your collection name
    }

    public List<AttachmentEntityExtended> getAttachmentsByAlertId(String alertId) {
        Query query = new Query(Criteria.where("alertId").is(alertId));
        query.fields().exclude("fileData");
        return mongoTemplate.find(query, AttachmentEntityExtended.class, "attachments");
    }


        public AttachmentEntity createUpsertAttachment(AttachmentEntity attachmentEntity) {
        AttachmentEntity alEntity = mongoTemplate.save(attachmentEntity, MONGO_COLLECTION_ALERT_ATTACHMENTS);
        return alEntity;
    }

    public AttachmentEntity patchAttachment(AttachmentEntity attachmentEntity) {

        Update update = new Update();

        for (Field field : attachmentEntity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Optional<Object> fieldValue;
            try {
                fieldValue = Optional.ofNullable(field.get(attachmentEntity));
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

        Query query = new Query(Criteria.where("id").is(attachmentEntity.getId()));
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);

        AttachmentEntity modifiedAttachmentEntity = mongoTemplate.findAndModify(query, update,
                options, AttachmentEntity.class, MONGO_COLLECTION_ALERT_ATTACHMENTS);

        if (modifiedAttachmentEntity == null) {
            throw new CaisBaseException(String.format("Attachment not found with id: %s", attachmentEntity.getId()));
        }
        return modifiedAttachmentEntity;
    }

    public AttachmentEntity getAttachmentById(String attachmentId) {
        ObjectId id = new ObjectId(attachmentId);
        AttachmentEntity attachmentEntity = mongoTemplate
                .findById(id, AttachmentEntity.class, MONGO_COLLECTION_ALERT_ATTACHMENTS);
        return attachmentEntity;
    }

    public List<AttachmentEntity> getAllAttachments(String alertId, String name, Date createdDateFrom,
            Date createdDateTo, int offset,
            int limit) {
        final Query query = new Query();
        final List<Criteria> criteria = new ArrayList<>();

        if (alertId != null)
            criteria.add(Criteria.where("alertId").is(alertId));
        if (createdDateFrom != null)
            criteria.add(Criteria.where("createdDate").gte(createdDateFrom));
        if (createdDateTo != null)
            criteria.add(Criteria.where("createdDate").lte(createdDateTo));

        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));

        List<AttachmentEntity> alertEntities = new ArrayList<>();
        List<AttachmentEntity> documents = mongoTemplate.find(query, AttachmentEntity.class,
                MONGO_COLLECTION_ALERT_ATTACHMENTS);
        alertEntities.addAll(documents);
        return alertEntities;
    }

}