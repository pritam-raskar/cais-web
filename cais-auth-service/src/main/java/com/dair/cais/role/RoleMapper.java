package com.dair.cais.role;

import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public Role toModel(RoleEntity entity) {
        Role role = new Role();
        role.setId(String.valueOf(entity.getId()));
        role.setName(entity.getName());
        role.setDescription(entity.getDescription());
        role.setIdentifier(entity.getIdentifier());
        role.setUserCount(entity.getUserCount());

        role.setCreatedDate(entity.getCreatedDate());
        role.setUpdatedDate(entity.getUpdatedDate());

        return role;
    }

    public RoleEntity toEntity(Role role) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(role.getId());
        mapRoleToEntity(role, roleEntity);

        return roleEntity;
    }

    public RoleEntity toEntity(String roleId, Role role) {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(roleId);
        mapRoleToEntity(role, roleEntity);

        return roleEntity;

    }

    private void mapRoleToEntity(Role role, RoleEntity entity) {
        entity.setName(role.getName());
        entity.setDescription(role.getDescription());
        entity.setUserCount(role.getUserCount());
        entity.setIdentifier(role.getIdentifier());

        entity.setCreatedDate(role.getCreatedDate());
        entity.setUpdatedDate(role.getUpdatedDate());
    }

}