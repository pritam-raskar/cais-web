package com.dair.cais.role;

import java.util.Date;
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
public class RoleService {

   @Autowired
   private RoleMapper roleMapper;
   @Autowired
   private RoleRepository roleRepository;

   public Role createRole(Role role) {
      RoleEntity upsertedRole = roleRepository.createUpsertRole(roleMapper.toEntity(role));
      return roleMapper.toModel(upsertedRole);
   }

   public Role patchRole(String alertId, Role role) {
      RoleEntity upsertedRole = roleRepository.patchRole(roleMapper.toEntity(alertId, role));
      return roleMapper.toModel(upsertedRole);
   }

   public Role getRoleById(final String roleId) {
      RoleEntity roleById = roleRepository.getRoleById(roleId);
      if (roleById == null) {
         throw new CaisNotFoundException();
      }
      return roleMapper.toModel(roleById);
   }

   public Role deleteRoleById(String roleId) {
      RoleEntity roleById = roleRepository.deleteRoleById(roleId);
      if (roleById == null) {
         throw new CaisNotFoundException();
      }
      return roleMapper.toModel(roleById);
   }

   public Map<String, Object> getAllRoles(String name, Date createdDateFrom, Date createdDateTo, @Valid int limit,
         @Valid int offset) {
      validateRequestParams(name, createdDateFrom, createdDateTo, offset,
            limit);

      try {

         List<RoleEntity> allRoleEntities = roleRepository.getAllRoles(name,
               createdDateFrom, createdDateTo, offset, limit);

         List<Role> allRoles = allRoleEntities.stream().map(a -> roleMapper.toModel(a))
               .collect(Collectors.toList());

         Map<String, Object> response = new HashMap<>();
         response.put("roles", allRoles);
         response.put("count", allRoles.size());
         return response;
      } catch (Exception e) {
         throw new CaisBaseException("Error retrieving roles");
      }
   }

   public List<Role> createRoles(List<Role> roles) {
      List<Role> createdRoles = roles.stream().map(a -> createRole(a)).collect(Collectors.toList());
      return createdRoles;
   }

   private void validateRequestParams(String name, Date createdDateFrom, Date createdDateTo, int offset, int limit) {
      StringBuilder errorMessage = new StringBuilder();

      if (name != null && !name.isEmpty()) {
         if (name.length() > 20) {
            errorMessage.append("name cannot be longer than 20 characters;");
         }
      }

      if (createdDateFrom != null && createdDateTo != null) {
         if (createdDateFrom.after(createdDateTo)) {
            errorMessage.append("from date cannot be after to date;");
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

}
