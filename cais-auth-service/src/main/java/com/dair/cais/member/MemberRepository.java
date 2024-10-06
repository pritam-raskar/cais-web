package com.dair.cais.member;

import static com.dair.cais.config.CaisAuthConstants.MONGO_COLLECTION_USERS;

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
public class MemberRepository {

    @Autowired
    private MongoTemplate mongoTemplate;

    public MemberEntity createUpsertMember(MemberEntity alertEntity) {
        MemberEntity alEntity = mongoTemplate.save(alertEntity, MONGO_COLLECTION_USERS);
        return alEntity;
    }

    public MemberEntity patchMember(MemberEntity alertEntity) {

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

        MemberEntity modifiedMemberEntity = mongoTemplate.findAndModify(query, update,
                options, MemberEntity.class, MONGO_COLLECTION_USERS);

        if (modifiedMemberEntity == null) {
            throw new CaisBaseException(String.format("Member not found with id: %s", alertEntity.getId()));
        }
        return modifiedMemberEntity;
    }

    public MemberEntity getMemberById(String memberId) {
        ObjectId id = new ObjectId(memberId);
        MemberEntity memberEntity = mongoTemplate
                .findById(id, MemberEntity.class, MONGO_COLLECTION_USERS);
        return memberEntity;
    }

    public MemberEntity deleteMemberById(String memberId) {
        Query query = new Query(Criteria.where("_id").is(memberId));

        MemberEntity memberEntity = mongoTemplate.findAndRemove(query, MemberEntity.class, MONGO_COLLECTION_USERS);
        return memberEntity;
    }

    public List<MemberEntity> getAllMembers(String name, int offset,
            int limit) {
        final Query query = new Query();
        final List<Criteria> criteria = new ArrayList<>();

        if (!criteria.isEmpty())
            query.addCriteria(new Criteria().andOperator(criteria.toArray(new Criteria[criteria.size()])));

        List<MemberEntity> alertEntities = new ArrayList<>();
        List<MemberEntity> documents = mongoTemplate.find(query, MemberEntity.class, MONGO_COLLECTION_USERS);
        alertEntities.addAll(documents);
        return alertEntities;
    }

}