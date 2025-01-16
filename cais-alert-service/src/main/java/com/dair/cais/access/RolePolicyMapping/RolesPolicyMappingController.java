package com.dair.cais.access.RolePolicyMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/roles-policy-mappings")
public class RolesPolicyMappingController {
    private static final Logger logger = LoggerFactory.getLogger(RolesPolicyMappingController.class);

    @Autowired
    private RolesPolicyMappingService service;

    @GetMapping
    public ResponseEntity<List<RolesPolicyMapping>> getAllMappings() {
        logger.info("Received request to get all roles-policy mappings");
        List<RolesPolicyMapping> mappings = service.getAllMappings();
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/{rpmId}")
    public ResponseEntity<RolesPolicyMapping> getMappingById(@PathVariable Integer rpmId) {
        logger.info("Received request to get roles-policy mapping with ID: {}", rpmId);
        return service.getMappingById(rpmId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<RolesPolicyMapping>> getMappingsByPolicyId(@PathVariable Integer policyId) {
        logger.info("Received request to get roles-policy mappings for policy ID: {}", policyId);
        List<RolesPolicyMapping> mappings = service.getMappingsByPolicyId(policyId);
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<RolesPolicyMapping>> getMappingsByRoleId(@PathVariable Integer roleId) {
        logger.info("Received request to get roles-policy mappings for role ID: {}", roleId);
        List<RolesPolicyMapping> mappings = service.getMappingsByRoleId(roleId);
        return ResponseEntity.ok(mappings);
    }

    @PostMapping("/create")
    @Operation(summary = "Create or update multiple role-policy mappings")
    public ResponseEntity<List<RolesPolicyMapping>> bulkCreateOrUpdate(
            @RequestBody List<RolesPolicyMapping> requests) {
        log.info("Received request to bulk create/update {} role-policy mappings", requests.size());
        List<RolesPolicyMapping> mappings = service.bulkCreateOrUpdate(requests);
        return ResponseEntity.ok(mappings);
    }

    @DeleteMapping("/role/{roleId}/policies")
    @Operation(summary = "Delete policy mappings for a role")
    @ApiResponse(responseCode = "204", description = "Mappings successfully deleted")
    @ApiResponse(responseCode = "404", description = "Role not found")
    public ResponseEntity<Void> deletePolicyMappingsForRole(
            @Parameter(description = "Role ID", required = true)
            @PathVariable Integer roleId,
            @Parameter(description = "List of Policy IDs to delete", required = true)
            @RequestBody List<Integer> policyIds) {
        log.info("Received request to delete policy mappings for role: {} with policies: {}",
                roleId, policyIds);
        service.deletePolicyMappingsForRole(roleId, policyIds);
        return ResponseEntity.noContent().build();
    }



    @PostMapping
    public ResponseEntity<RolesPolicyMapping> createMapping(@RequestParam Integer policyId, @RequestParam Integer roleId) {
        logger.info("Received request to create new roles-policy mapping for policy ID: {} and role ID: {}", policyId, roleId);
        RolesPolicyMapping createdMapping = service.createMapping(policyId, roleId);
        return ResponseEntity.ok(createdMapping);
    }

    @PutMapping("/{rpmId}")
    public ResponseEntity<RolesPolicyMapping> updateMapping(@PathVariable Integer rpmId, @RequestParam Integer policyId, @RequestParam Integer roleId) {
        logger.info("Received request to update roles-policy mapping with ID: {}", rpmId);
        return service.updateMapping(rpmId, policyId, roleId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{rpmId}")
    public ResponseEntity<Void> deleteMapping(@PathVariable Integer rpmId) {
        logger.info("Received request to delete roles-policy mapping with ID: {}", rpmId);
        service.deleteMapping(rpmId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/role/{roleId}")
    @Operation(summary = "Update policy mappings for a role",
            description = "Updates policy mappings for a role. Adds new mappings if they don't exist and removes existing mappings if request is empty.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Mappings successfully updated"),
                    @ApiResponse(responseCode = "404", description = "Role not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    public ResponseEntity<List<RolesPolicyMapping>> updateRolePolicyMappings(
            @Parameter(description = "ID of the role to update", required = true)
            @PathVariable @NotNull Integer roleId,

            @Parameter(description = "List of policy mappings to update. If empty or null, all existing mappings will be removed.")
            @RequestBody(required = false) List<@Valid RolePolicyRequest> requests) {

        log.debug("Starting policy mapping update for role ID: {}", roleId);

        try {
            // Handle null request body by converting to empty list
            List<RolePolicyRequest> requestList = requests != null ? requests : Collections.emptyList();

            // Validate requests if not empty
            if (!requestList.isEmpty()) {
                validateRolePolicyRequests(roleId, requestList);
            }

            log.info("Processing policy mapping update for role ID: {}, number of policies: {}",
                    roleId, requestList.size());

            List<RolesPolicyMapping> updatedMappings = service.updateRolePolicyMappings(roleId, requestList);

            log.info("Successfully updated policy mappings for role ID: {}. New mapping count: {}",
                    roleId, updatedMappings.size());

            return ResponseEntity.ok(updatedMappings);

        } catch (EntityNotFoundException e) {
            log.error("Role not found with ID: {}", roleId);
            throw e;
        } catch (Exception e) {
            log.error("Error updating policy mappings for role ID: {}", roleId, e);
            throw new RolePolicyMappingException("Failed to update policy mappings", e);
        }
    }

    /**
     * Validates the role policy requests against the provided role ID.
     *
     * @param roleId The role ID to validate against
     * @param requests The list of role policy requests to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateRolePolicyRequests(Integer roleId, List<RolePolicyRequest> requests) {
        if (requests.stream().anyMatch(req -> !req.roleId().equals(roleId))) {
            String message = String.format("Request contains roleId that doesn't match path parameter: %d", roleId);
            log.error(message);
            throw new IllegalArgumentException(message);
        }
    }

    @Schema(description = "Request object for updating role-policy mappings")
    public record RolePolicyRequest(
            @Schema(description = "ID of the policy to map to the role", required = true)
            @NotNull Integer policyId,

            @Schema(description = "ID of the role", required = true)
            @NotNull Integer roleId
    ) {}
}