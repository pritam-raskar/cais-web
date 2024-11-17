package com.dair.cais.access.Role;

import com.dair.cais.access.user.UserEntity;
import com.dair.cais.exception.RoleDeleteException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs for managing roles and their associations")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "Create a new role",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Role created successfully"),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            })
    public ResponseEntity<Role> createRole(@Valid @RequestBody Role role) {
        log.debug("Creating new role: {}", role.getRoleName());
        Role createdRole = roleService.createRole(role);
        return new ResponseEntity<>(createdRole, HttpStatus.CREATED);
    }

    @PutMapping("/{roleId}")
    @Operation(summary = "Update an existing role",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role updated successfully"),
                    @ApiResponse(responseCode = "404", description = "Role not found"),
                    @ApiResponse(responseCode = "400", description = "Invalid input")
            })
    public ResponseEntity<Role> updateRole(
            @Parameter(description = "ID of the role to update") @PathVariable Integer roleId,
            @Valid @RequestBody Role role) {
        log.debug("Updating role with ID {}: {}", roleId, role.getRoleName());
        Role updatedRole = roleService.updateRole(roleId, role);
        return ResponseEntity.ok(updatedRole);
    }

    @GetMapping("/name/{roleName}")
    @Operation(summary = "Get role by name",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role found"),
                    @ApiResponse(responseCode = "404", description = "Role not found")
            })
    public ResponseEntity<Role> getRoleByName(
            @Parameter(description = "Name of the role to retrieve") @PathVariable String roleName) {
        log.debug("Fetching role by name: {}", roleName);
        Role role = roleService.getRoleByName(roleName);
        return ResponseEntity.ok(role);
    }

    @GetMapping("/id/{roleId}")
    @Operation(summary = "Get role by Id",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Role found"),
                    @ApiResponse(responseCode = "404", description = "Role not found")
            })
    public ResponseEntity<Role> getRoleById(
            @Parameter(description = "Name of the role to retrieve") @PathVariable Integer roleId) {
        log.debug("Fetching role by Id: {}", roleId);
        Role role = roleService.getRoleById(roleId);
        return ResponseEntity.ok(role);
    }

    @GetMapping
    @Operation(summary = "Get all roles")
    public ResponseEntity<List<Role>> getAllRoles() {
        log.debug("Fetching all roles");
        List<Role> roles = roleService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/details")
    @Operation(summary = "Get all roles with user count")
    public ResponseEntity<List<Role>> getAllRolesWithUserCount() {
        log.debug("Fetching all roles with user count");
        List<Role> roles = roleService.getAllRolesWithUserCount();
        return ResponseEntity.ok(roles);
    }

    @GetMapping("/{roleName}/users")
    @Operation(summary = "Get users by role name",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Users found"),
                    @ApiResponse(responseCode = "404", description = "Role not found")
            })
    public ResponseEntity<List<UserEntity>> getUsersByRoleName(
            @Parameter(description = "Name of the role") @PathVariable String roleName) {
        log.debug("Fetching users for role: {}", roleName);
        List<UserEntity> users = roleService.getUsersByRoleName(roleName);
        return ResponseEntity.ok(users);
    }

    @DeleteMapping("/{roleId}")
    @Operation(summary = "Delete a role",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
                    @ApiResponse(responseCode = "404", description = "Role not found"),
                    @ApiResponse(responseCode = "409", description = "Role cannot be deleted due to existing associations",
                            content = @Content(schema = @Schema(implementation = RoleDeletionError.class)))
            })
    public ResponseEntity<?> deleteRole(
            @Parameter(description = "ID of the role to delete") @PathVariable Integer roleId) {
        log.debug("Attempting to delete role: {}", roleId);
        try {
            roleService.deleteRole(roleId);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException e) {
            log.warn("Role not found for deletion: {}", roleId);
            return ResponseEntity.notFound().build();
        } catch (RoleDeleteException e) {
            log.warn("Cannot delete role {} due to user associations: {}", roleId, e.getUserCount());
            RoleDeletionError error = new RoleDeletionError(e.getMessage(), e.getUserCount());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    // Error response class for better API documentation
    @Schema(description = "Error response when role deletion fails due to existing associations")
    public record RoleDeletionError(
            @Schema(description = "Error message") String error,
            @Schema(description = "Number of users associated with the role") Long userCount
    ) {}

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
}