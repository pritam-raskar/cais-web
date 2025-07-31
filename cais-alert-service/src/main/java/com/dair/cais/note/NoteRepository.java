package com.dair.cais.note;

import com.dair.exception.CaisBaseException;
import lombok.extern.slf4j.Slf4j;
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

import static com.dair.cais.common.config.CaisAlertConstants.MONGO_COLLECTION_ALERT_NOTES;

@Repository
@Slf4j
public class NoteRepository {

    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private NoteMapperExtended noteMapperExtended;


    public NoteEntity createUpsertNote(NoteEntity alertEntity) {
        NoteEntity alEntity = mongoTemplate.save(alertEntity, MONGO_COLLECTION_ALERT_NOTES);
        return alEntity;
    }

    public NoteEntityExtended addNote(String note, String alertId, String createdBy, String entity, String entityValue) {
        NoteEntityExtended finalentity = noteMapperExtended.toEntity(note, alertId, createdBy, entity, entityValue);
        NoteEntityExtended alEntity = mongoTemplate.save(finalentity, MONGO_COLLECTION_ALERT_NOTES);
        return alEntity;
    }

    public List<NoteExtended> findByAlertId(String alertId) {
        Query query = new Query();
        query.addCriteria(Criteria.where("alertId").is(alertId));
        List<NoteEntityExtended> noteEntities = mongoTemplate.find(query, NoteEntityExtended.class, MONGO_COLLECTION_ALERT_NOTES);
        return noteMapperExtended.toModel(noteEntities);
    }

    public NoteEntity patchNote(NoteEntity alertEntity) {

        Update update = new Update();

        for (Field field : alertEntity.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            Optional<Object> fieldValue;
            try {
                fieldValue = Optional.ofNullable(field.get(alertEntity));
                if (fieldValue.isPresent()
                        && field.getName() != "serialVersionUID"
                        && field.getName() != "id") {
                    log.debug("Updating field {}: {}", field.getName(), fieldValue.get());
                    update.set(field.getName(), fieldValue.get());
                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                log.error("Error accessing field {}: {}", field.getName(), e.getMessage());
            }
        }

        Query query = new Query(Criteria.where("id").is(alertEntity.getId()));
        FindAndModifyOptions options = new FindAndModifyOptions().returnNew(true).upsert(true);

        NoteEntity modifiedNoteEntity = mongoTemplate.findAndModify(query, update,
                options, NoteEntity.class, MONGO_COLLECTION_ALERT_NOTES);

        if (modifiedNoteEntity == null) {
            throw new CaisBaseException(String.format("Note not found with id: %s", alertEntity.getId()));
        }
        return modifiedNoteEntity;
    }

    public NoteEntity getNoteById(String noteId) {
        ObjectId id = new ObjectId(noteId);
        NoteEntity noteEntity = mongoTemplate
                .findById(id, NoteEntity.class, MONGO_COLLECTION_ALERT_NOTES);
        return noteEntity;
    }

    public List<NoteEntity> getAllNotes(String name, Date createdDateFrom, Date createdDateTo, int offset,
            int limit) {
        final Query query = new Query();
        final List<Criteria> criteria = new ArrayList<>();

        if (createdDateFrom != null)
            criteria.add(Criteria.where("createdDate").gte(createdDateFrom));
        if (createdDateTo != null)
            criteria.add(Criteria.where("createdDate").lte(createdDateTo));

        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));

        List<NoteEntity> alertEntities = new ArrayList<>();
        List<NoteEntity> documents = mongoTemplate.find(query, NoteEntity.class, MONGO_COLLECTION_ALERT_NOTES);
        alertEntities.addAll(documents);
        return alertEntities;
    }

}