package com.dair.cais.permission;

import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public Permission toModel(PermissionEntity entity) {
        Permission permission = new Permission();
        permission.setId(String.valueOf(entity.getId()));
        permission.setName(entity.getName());
        permission.setDescription(entity.getDescription());
        permission.setAlertTypes(entity.getAlertTypes());
        permission.setRoles(entity.getRoles());
        permission.setBusinessUnits(entity.getBusinessUnits());
        permission.setAssignedOnly(entity.isAssignedOnly());

        permission.setCreatedDate(entity.getCreatedDate());
        permission.setUpdatedDate(entity.getUpdatedDate());

        return permission;
    }

    public PermissionEntity toEntity(Permission permission) {
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setId(permission.getId());
        mapPermissionToEntity(permission, permissionEntity);

        return permissionEntity;
    }

    public PermissionEntity toEntity(String permissionId, Permission permission) {
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setId(permissionId);
        mapPermissionToEntity(permission, permissionEntity);

        return permissionEntity;

    }

    private void mapPermissionToEntity(Permission permission, PermissionEntity entity) {
        entity.setName(permission.getName());
        entity.setDescription(permission.getDescription());
        entity.setAlertTypes(permission.getAlertTypes());
        entity.setRoles(permission.getRoles());
        entity.setBusinessUnits(permission.getBusinessUnits());
        entity.setAssignedOnly(permission.isAssignedOnly());

        entity.setCreatedDate(permission.getCreatedDate());
        entity.setUpdatedDate(permission.getUpdatedDate());
    }

}