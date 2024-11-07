package com.dair.cais.access.RolePolicyMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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
}