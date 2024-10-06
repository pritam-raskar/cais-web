package com.dair.cais.role;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/roles")
@Tag(name = "roles")

public class RoleController {

   @Autowired
   private RoleService roleService;

   @PostMapping
   @Operation(summary = "Create a role")
   public ResponseEntity<Role> createRole(@RequestBody Role role) {
      Role createdRole = roleService.createRole(role);
      return ResponseEntity.ok().body(createdRole);
   }

   @PatchMapping("{roleId}")
   @Operation(summary = "Update a role")
   public ResponseEntity<Role> patchRole(@PathVariable final String roleId, @RequestBody Role role) {
      Role updatedRole = roleService.patchRole(roleId, role);
      return ResponseEntity.ok().body(updatedRole);
   }

   @PostMapping("/bulk")
   @Operation(summary = "Create bulk roles")
   public ResponseEntity<List<Role>> createRoles(@RequestBody List<Role> roles) {
      List<Role> createdRoles = roleService.createRoles(roles);
      return ResponseEntity.ok().body(createdRoles);
   }

   @GetMapping("{roleId}")
   @Operation(summary = "Get a role by its id")
   public ResponseEntity<Role> getRoleById(@PathVariable final String roleId) {
      Role roleById = roleService.getRoleById(roleId);
      return ResponseEntity.ok().body(roleById);
   }

   @DeleteMapping("{roleId}")
   @Operation(summary = "Delete a role by its id")
   public ResponseEntity<Role> deleteRoleById(@PathVariable final String roleId) {
      Role roleById = roleService.deleteRoleById(roleId);
      return ResponseEntity.ok().body(roleById);
   }

   @GetMapping("")
   @Operation(summary = "Get all roles; Use query params for search options like offset ,limit ,fuzzy search")
   public ResponseEntity<Map<String, Object>> getAllRoles(
         @RequestParam(required = false) String name,
         @RequestParam(required = false, name = "from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateFrom,
         @RequestParam(required = false, name = "to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) Date createdDateTo,
         @Valid @RequestParam(defaultValue = "10") int limit,
         @Valid @RequestParam(defaultValue = "0") int offset) {
      return ResponseEntity.ok()
            .body(roleService.getAllRoles(name,
                  createdDateFrom, createdDateTo, limit, offset));
   }
}