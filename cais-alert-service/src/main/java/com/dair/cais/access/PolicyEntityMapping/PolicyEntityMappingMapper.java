package com.dair.cais.access.PolicyEntityMapping;

import org.springframework.stereotype.Component;

@Component
public class PolicyEntityMappingMapper {

    public PolicyEntityMapping toDto(PolicyEntityMappingEntity entity) {
        if (entity == null) {
            return null;
        }

        PolicyEntityMapping dto = new PolicyEntityMapping();
        dto.setMappingId(entity.getMappingId());

        if (entity.getPolicy() != null) {
            dto.setPolicyId(entity.getPolicy().getPolicyId());
            dto.setPolicyName(entity.getPolicy().getName());
        }

        dto.setEntityType(entity.getEntityType());
        dto.setEntityId(entity.getEntityId());

        if (entity.getAction() != null) {
            dto.setActionId(entity.getAction().getActionId());
            dto.setActionName(entity.getAction().getActionName());
            dto.setActionCategory(entity.getAction().getActionCategory());
            dto.setActionType(entity.getAction().getActionType());
            dto.setActionDescription(entity.getAction().getActionDescription());
        }

        dto.setCondition(entity.getCondition());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());

        return dto;
    }

    public PolicyEntityMappingEntity toEntity(PolicyEntityMapping dto) {
        if (dto == null) {
            return null;
        }

        PolicyEntityMappingEntity entity = new PolicyEntityMappingEntity();
        entity.setMappingId(dto.getMappingId());
        entity.setEntityType(dto.getEntityType());
        entity.setEntityId(dto.getEntityId());
        entity.setCondition(dto.getCondition());

        return entity;
    }
}