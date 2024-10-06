package com.dair.cais.access.policy;

import org.springframework.stereotype.Component;

@Component
public class PolicyMapper {

    public Policy toModel(PolicyEntity entity) {
        if (entity == null) {
            return null;
        }

        Policy policy = new Policy();
        policy.setPolicyId(entity.getPolicyId());
        policy.setName(entity.getName());
        policy.setDescription(entity.getDescription());
        policy.setType(entity.getType());
        policy.setIsActive(entity.getIsActive());

        return policy;
    }

    public PolicyEntity toEntity(Policy model) {
        if (model == null) {
            return null;
        }

        PolicyEntity entity = new PolicyEntity();
        entity.setPolicyId(model.getPolicyId());
        entity.setName(model.getName());
        entity.setDescription(model.getDescription());
        entity.setType(model.getType());
        entity.setIsActive(model.getIsActive());

        return entity;
    }
}
