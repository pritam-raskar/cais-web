package com.dair.cais.branch;

import static com.dair.cais.config.CaisAuthConstants.MONGO_COLLECTION_BRANCHES;

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
public class BranchRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public BranchEntity createUpsertBranch(BranchEntity alertEntity) {
        BranchEntity alEntity = mongoTemplate.save(alertEntity, MONGO_COLLECTION_BRANCHES);
        return alEntity;
    }

    public BranchEntity patchBranch(BranchEntity alertEntity) {

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

        BranchEntity modifiedBranchEntity = mongoTemplate.findAndModify(query, update,
                options, BranchEntity.class, MONGO_COLLECTION_BRANCHES);

        if (modifiedBranchEntity == null) {
            throw new CaisBaseException(String.format("Branch not found with id: %s", alertEntity.getId()));
        }
        return modifiedBranchEntity;
    }

    public BranchEntity getBranchById(String branchId) {
        ObjectId id = new ObjectId(branchId);
        BranchEntity branchEntity = mongoTemplate
                .findById(id, BranchEntity.class, MONGO_COLLECTION_BRANCHES);
        return branchEntity;
    }

    public BranchEntity deleteBranchById(String branchId) {
        Query query = new Query(Criteria.where("_id").is(branchId));

        BranchEntity branchEntity = mongoTemplate.findAndRemove(query, BranchEntity.class, MONGO_COLLECTION_BRANCHES);
        return branchEntity;
    }

    public List<BranchEntity> getAllBranchs(String name, Date createdDateFrom, Date createdDateTo, int offset,
            int limit) {
        final Query query = new Query();
        final List<Criteria> criteria = new ArrayList<>();

        if (createdDateFrom != null)
            criteria.add(Criteria.where("createdDate").gte(createdDateFrom));
        if (createdDateTo != null)
            criteria.add(Criteria.where("createdDate").lte(createdDateTo));

        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));

        List<BranchEntity> alertEntities = new ArrayList<>();
        List<BranchEntity> documents = mongoTemplate.find(query, BranchEntity.class, MONGO_COLLECTION_BRANCHES);
        alertEntities.addAll(documents);
        return alertEntities;
    }

}