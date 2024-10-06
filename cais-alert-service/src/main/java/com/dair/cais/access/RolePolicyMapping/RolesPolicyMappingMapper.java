package com.dair.cais.access.RolePolicyMapping;

import org.springframework.stereotype.Component;

@Component
public class RolesPolicyMappingMapper {

    public RolesPolicyMapping toModel(RolesPolicyMappingEntity entity) {
        if (entity == null) {
            return null;
        }

        RolesPolicyMapping model = new RolesPolicyMapping();
        model.setRpmId(entity.getRpmId());
        model.setPolicyId(entity.getPolicy().getPolicyId());
        model.setRoleId(entity.getRole().getRoleId());
        model.setPolicyName(entity.getPolicy().getName());
        model.setRoleName(entity.getRole().getRoleName());

        return model;
    }

    public RolesPolicyMappingEntity toEntity(RolesPolicyMapping model) {
        if (model == null) {
            return null;
        }

        RolesPolicyMappingEntity entity = new RolesPolicyMappingEntity();
        entity.setRpmId(model.getRpmId());
        // Note: Policy and Role entities should be set in the service layer
        return entity;
    }
}