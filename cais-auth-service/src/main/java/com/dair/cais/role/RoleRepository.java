package com.dair.cais.role;

import static com.dair.cais.config.CaisAuthConstants.MONGO_COLLECTION_ROLES;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
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
public class RoleRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public RoleEntity createUpsertRole(RoleEntity alertEntity) {
        RoleEntity alEntity = mongoTemplate.save(alertEntity, MONGO_COLLECTION_ROLES);
        return alEntity;
    }

    public RoleEntity patchRole(RoleEntity alertEntity) {

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

        RoleEntity modifiedRoleEntity = mongoTemplate.findAndModify(query, update,
                options, RoleEntity.class, MONGO_COLLECTION_ROLES);

        if (modifiedRoleEntity == null) {
            throw new CaisBaseException(String.format("Role not found with id: %s", alertEntity.getId()));
        }
        return modifiedRoleEntity;
    }

    public RoleEntity getRoleById(String roleId) {
        ObjectId id = new ObjectId(roleId);
        RoleEntity roleEntity = mongoTemplate
                .findById(id, RoleEntity.class, MONGO_COLLECTION_ROLES);
        return roleEntity;
    }

    public RoleEntity deleteRoleById(String roleId) {
        Query query = new Query(Criteria.where("_id").is(roleId));

        RoleEntity roleEntity = mongoTemplate.findAndRemove(query, RoleEntity.class, MONGO_COLLECTION_ROLES);
        return roleEntity;
    }

    public List<RoleEntity> getAllRoles(String name, Date createdDateFrom, Date createdDateTo, int offset,
            int limit) {
        final Query query = new Query();
        final List<Criteria> criteria = new ArrayList<>();

        if (createdDateFrom != null)
            criteria.add(Criteria.where("createdDate").gte(createdDateFrom));
        if (createdDateTo != null)
            criteria.add(Criteria.where("createdDate").lte(createdDateTo));

        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));

        List<RoleEntity> alertEntities = new ArrayList<>();
        List<RoleEntity> documents = mongoTemplate.find(query, RoleEntity.class, MONGO_COLLECTION_ROLES);
        alertEntities.addAll(documents);
        return alertEntities;
    }

}