package com.dair.cais.access.RolePolicyMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            description = "Updates policy mappings for a role. Adds new mappings if they don't exist and removes mappings that aren't in the request.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Mappings successfully updated"),
                    @ApiResponse(responseCode = "400", description = "Invalid request"),
                    @ApiResponse(responseCode = "404", description = "Role or Policy not found"),
                    @ApiResponse(responseCode = "500", description = "Internal server error")
            })
    public ResponseEntity<List<RolesPolicyMapping>> updateRolePolicyMappings(
            @Parameter(description = "ID of the role to update", required = true)
            @PathVariable @NotNull Integer roleId,

            @Parameter(description = "List of policy mappings to update", required = true)
            @RequestBody @NotEmpty List<@Valid RolePolicyRequest> requests) {

        // Validate that all roleIds in the request match the path parameter
        if (requests.stream().anyMatch(req -> !req.roleId().equals(roleId))) {
            log.error("Request contains roleId that doesn't match path parameter: {}", roleId);
            throw new IllegalArgumentException("All roleId values must match the path parameter");
        }

        log.info("Received request to update policy mappings for role ID: {}, number of policies: {}",
                roleId, requests.size());

        List<RolesPolicyMapping> updatedMappings = service.updateRolePolicyMappings(roleId, requests);

        log.info("Successfully updated policy mappings for role ID: {}, updated mappings count: {}",
                roleId, updatedMappings.size());

        return ResponseEntity.ok(updatedMappings);
    }

    @Schema(description = "Request object for updating role-policy mappings")
    public record RolePolicyRequest(
            @Schema(description = "ID of the policy to map to the role", required = true)
            @NotNull Integer policyId,

            @Schema(description = "ID of the role", required = true)
            @NotNull Integer roleId
    ) {}
}