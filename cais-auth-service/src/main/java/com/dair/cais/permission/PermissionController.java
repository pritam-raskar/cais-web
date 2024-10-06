package com.dair.cais.permission;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @author Dair
 * @since 2022
 */

@RestController
@RequestMapping("/permissions")
@Tag(name = "permissions")

public class PermissionController {

   @Autowired
   private PermissionService permissionService;

   @PostMapping
   @Operation(summary = "Create a permission")
   public ResponseEntity<Permission> createPermission(@RequestBody Permission permission) {
      Permission createdPermission = permissionService.createPermission(permission);
      return ResponseEntity.ok().body(createdPermission);
   }

   @PatchMapping("{permissionId}")
   @Operation(summary = "Update a permission")
   public ResponseEntity<Permission> patchPermission(@PathVariable final String permissionId,
         @RequestBody Permission permission) {
      Permission updatedPermission = permissionService.patchPermission(permissionId, permission);
      return ResponseEntity.ok().body(updatedPermission);
   }

   @PostMapping("/bulk")
   @Operation(summary = "Create bulk permissions")
   public ResponseEntity<List<Permission>> createPermissions(@RequestBody List<Permission> permissions) {
      List<Permission> createdPermissions = permissionService.createPermissions(permissions);
      return ResponseEntity.ok().body(createdPermissions);
   }

   @GetMapping("{permissionId}")
   @Operation(summary = "Get a permission by its id")
   public ResponseEntity<Permission> getPermissionById(@PathVariable final String permissionId) {
      Permission permissionById = permissionService.getPermissionById(permissionId);
      return ResponseEntity.ok().body(permissionById);
   }

   @DeleteMapping("{permissionId}")
   @Operation(summary = "Delete a permission by its id")
   public ResponseEntity<Permission> deletePermissionById(@PathVariable final String permissionId) {
      Permission permissionById = permissionService.deletePermissionById(permissionId);
      return ResponseEntity.ok().body(permissionById);
   }

   @GetMapping("")
   @Operation(summary = "Get all permissions; Use query params for search options like offset ,limit ,fuzzy search")
   public ResponseEntity<Map<String, Object>> getAllPermissions(
         @RequestParam(name = "alertType", required = false) String alertType,
         @RequestParam(name = "role", required = false) String role,
         @RequestParam(name = "businessUnit", required = false) String businessUnit,
         @Valid @RequestParam(defaultValue = "10") int limit,
         @Valid @RequestParam(defaultValue = "0") int offset) {
      return ResponseEntity.ok()
            .body(permissionService.getAllPermissions(alertType, role, businessUnit, limit, offset));
   }
}