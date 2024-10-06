package com.dair.cais.access.PolicyModuleMapping;

import org.springframework.stereotype.Component;

@Component
public class PolicyModuleMappingMapper {

    public PolicyModuleMapping toDto(PolicyModuleMappingEntity entity) {
        if (entity == null) {
            return null;
        }

        PolicyModuleMapping dto = new PolicyModuleMapping();
        dto.setPmaId(entity.getPmaId());

        if (entity.getPolicy() != null) {
            dto.setPolicyId(entity.getPolicy().getPolicyId());
            dto.setPolicyName(entity.getPolicy().getName());
        }

        if (entity.getModule() != null) {
            dto.setModuleId(entity.getModule().getModuleId());
            dto.setModuleName(entity.getModule().getModuleName());
        }

        if (entity.getAction() != null) {
            dto.setActionId(entity.getAction().getActionId());
            dto.setActionName(entity.getAction().getActionName());
        }

        dto.setCondition(entity.getCondition());

        return dto;
    }

    public PolicyModuleMappingEntity toEntity(PolicyModuleMapping dto) {
        if (dto == null) {
            return null;
        }

        PolicyModuleMappingEntity entity = new PolicyModuleMappingEntity();
        entity.setPmaId(dto.getPmaId());
        entity.setCondition(dto.getCondition());

        // Note: Policy, Module, and Action entities should be set in the service layer
        // We only set the IDs here to avoid creating incomplete entity objects

        return entity;
    }
}