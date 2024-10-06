package com.dair.cais.permission;

import static com.dair.cais.config.CaisAuthConstants.MONGO_COLLECTION_PERMISSIONS;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.dair.exception.CaisBaseException;

@Repository
public class PermissionRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public PermissionEntity createUpsertPermission(PermissionEntity alertEntity) {
        PermissionEntity alEntity = mongoTemplate.save(alertEntity, MONGO_COLLECTION_PERMISSIONS);
        return alEntity;
    }

    public PermissionEntity patchPermission(PermissionEntity alertEntity) {

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

        PermissionEntity modifiedPermissionEntity = mongoTemplate.findAndModify(query, update,
                options, PermissionEntity.class, MONGO_COLLECTION_PERMISSIONS);

        if (modifiedPermissionEntity == null) {
            throw new CaisBaseException(String.format("Permission not found with id: %s", alertEntity.getId()));
        }
        return modifiedPermissionEntity;
    }

    public PermissionEntity getPermissionById(String permissionId) {
        ObjectId id = new ObjectId(permissionId);
        PermissionEntity permissionEntity = mongoTemplate
                .findById(id, PermissionEntity.class, MONGO_COLLECTION_PERMISSIONS);
        return permissionEntity;
    }

    public PermissionEntity deletePermissionById(String permissionId) {
        Query query = new Query(Criteria.where("_id").is(permissionId));

        PermissionEntity permissionEntity = mongoTemplate.findAndRemove(query, PermissionEntity.class,
                MONGO_COLLECTION_PERMISSIONS);
        return permissionEntity;
    }

    public List<PermissionEntity> getAllPermissions(String alertType, String role, String businessUnit, int offset,
            int limit) {
        final Query query = new Query();
        final List<Criteria> criteria = new ArrayList<>();

        if (alertType != null && !alertType.isBlank()) {
            String alertTypeRegex = "^" + alertType + "$";
            criteria.add(Criteria.where("alertTypes").regex(alertTypeRegex, "i"));
        }
        if (role != null && !role.isBlank()) {
            String roleRegex = "^" + role + "$";
            criteria.add(Criteria.where("roles").regex(roleRegex, "i"));
        }
        if (businessUnit != null && !businessUnit.isBlank()) {
            String businessUnitRegex = "^" + businessUnit + "$";
            criteria.add(Criteria.where("businessUnits").regex(businessUnitRegex, "i"));
        }

        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));

        List<PermissionEntity> alertEntities = new ArrayList<>();
        List<PermissionEntity> documents = mongoTemplate.find(query, PermissionEntity.class,
                MONGO_COLLECTION_PERMISSIONS);
        alertEntities.addAll(documents);
        return alertEntities;
    }

}