package com.dair.cais.permission;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dair.exception.CaisBaseException;
import com.dair.exception.CaisIllegalArgumentException;
import com.dair.exception.CaisNotFoundException;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PermissionService {

   @Autowired
   private PermissionMapper permissionMapper;
   @Autowired
   private PermissionRepository permissionRepository;

   public Permission createPermission(Permission permission) {
      validatePermissions(permission);
      PermissionEntity upsertedPermission = permissionRepository
            .createUpsertPermission(permissionMapper.toEntity(permission));
      return permissionMapper.toModel(upsertedPermission);
   }

   public Permission patchPermission(String permissionId, Permission permission) {
      validatePermissions(permission);
      PermissionEntity upsertedPermission = permissionRepository
            .patchPermission(permissionMapper.toEntity(permissionId, permission));
      return permissionMapper.toModel(upsertedPermission);
   }

   public Permission getPermissionById(final String permissionId) {
      PermissionEntity permissionById = permissionRepository.getPermissionById(permissionId);
      if (permissionById == null) {
         throw new CaisNotFoundException();
      }
      return permissionMapper.toModel(permissionById);
   }

   public Permission deletePermissionById(String permissionId) {
      PermissionEntity permissionById = permissionRepository.deletePermissionById(permissionId);
      if (permissionById == null) {
         throw new CaisNotFoundException();
      }
      return permissionMapper.toModel(permissionById);
   }

   public Map<String, Object> getAllPermissions(String alertType, String role, String businessUnit, @Valid int limit,
         @Valid int offset) {
      validateRequestParams(alertType, role, businessUnit, offset, limit);

      try {

         List<PermissionEntity> allPermissionEntities = permissionRepository.getAllPermissions(alertType, role,
               businessUnit, offset,
               limit);

         List<Permission> allPermissions = allPermissionEntities.stream().map(a -> permissionMapper.toModel(a))
               .collect(Collectors.toList());

         Map<String, Object> response = new HashMap<>();
         response.put("permissions", allPermissions);
         response.put("count", allPermissions.size());
         return response;
      } catch (Exception e) {
         throw new CaisBaseException("Error retrieving permissions");
      }
   }

   public List<Permission> createPermissions(List<Permission> permissions) {
      List<Permission> createdPermissions = permissions.stream().map(a -> createPermission(a))
            .collect(Collectors.toList());
      return createdPermissions;
   }

   private void validateRequestParams(String alertType, String role, String businessUnit, int offset, int limit) {
      StringBuilder errorMessage = new StringBuilder();

      if (alertType != null && !alertType.isEmpty()) {
         if (alertType.length() > 20) {
            errorMessage.append("alertType cannot be longer than 20 characters;");
         }
      }

      if (role != null && !role.isEmpty()) {
         if (role.length() > 20) {
            errorMessage.append("role cannot be longer than 20 characters;");
         }
      }

      if (businessUnit != null && !businessUnit.isEmpty()) {
         if (businessUnit.length() > 20) {
            errorMessage.append("businessUnit cannot be longer than 20 characters;");
         }
      }

      if (limit < 0) {
         errorMessage.append("limit cannot be negative;");
      }
      if (offset < 0) {
         errorMessage.append("offset cannot be negative;");
      }
      if (errorMessage.isEmpty()) {
         return;
      }

      throw new CaisIllegalArgumentException(errorMessage.toString());
   }

   private void validatePermissions(Permission permission) {

   }

}
