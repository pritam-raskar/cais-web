package com.dair.cais.access.Role;

import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public Role toModel(RoleEntity entity) {
        if (entity == null) {
            return null;
        }

        Role model = new Role();
        model.setRoleId(entity.getRoleId());
        model.setRoleName(entity.getRoleName());
        model.setDescription(entity.getDescription());
        model.setRoleIdentifier(entity.getRoleIdentifier());

        return model;
    }

    public RoleEntity toEntity(Role model) {
        if (model == null) {
            return null;
        }

        RoleEntity entity = new RoleEntity();
        entity.setRoleId(model.getRoleId());
        entity.setRoleName(model.getRoleName());
        entity.setDescription(model.getDescription());
        entity.setRoleIdentifier(model.getRoleIdentifier());

        return entity;
    }
}