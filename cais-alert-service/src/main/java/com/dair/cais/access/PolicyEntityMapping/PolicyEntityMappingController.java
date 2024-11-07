package com.dair.cais.access.PolicyEntityMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/policy-mappings")
@RequiredArgsConstructor
@Tag(name = "Policy Entity Mapping", description = "APIs for managing policy entity mappings")
public class PolicyEntityMappingController {

    private final PolicyEntityMappingService mappingService;

    @GetMapping
    @Operation(summary = "Get all mappings", description = "Retrieves all policy entity mappings")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved mappings")
    public ResponseEntity<List<PolicyEntityMapping>> getAllMappings() {
        log.info("REST request to get all policy entity mappings");
        List<PolicyEntityMapping> mappings = mappingService.getAllMappings();
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get mapping by ID", description = "Retrieves a specific mapping by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved mapping")
    @ApiResponse(responseCode = "404", description = "Mapping not found")
    public ResponseEntity<PolicyEntityMapping> getMappingById(@PathVariable Integer id) {
        log.info("REST request to get mapping : {}", id);
        PolicyEntityMapping mapping = mappingService.getMappingById(id);
        return ResponseEntity.ok(mapping);
    }

    @GetMapping("/policy/{policyId}")
    @Operation(summary = "Get mappings by policy ID", description = "Retrieves all mappings for a specific policy")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved mappings")
    public ResponseEntity<List<PolicyEntityMapping>> getMappingsByPolicyId(@PathVariable Integer policyId) {
        log.info("REST request to get mappings for policy : {}", policyId);
        List<PolicyEntityMapping> mappings = mappingService.getMappingsByPolicyId(policyId);
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/entity-type/{entityType}")
    @Operation(summary = "Get mappings by entity type", description = "Retrieves all mappings for a specific entity type")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved mappings")
    public ResponseEntity<List<PolicyEntityMapping>> getMappingsByEntityType(@PathVariable String entityType) {
        log.info("REST request to get mappings for entity type : {}", entityType);
        List<PolicyEntityMapping> mappings = mappingService.getMappingsByEntityType(entityType);
        return ResponseEntity.ok(mappings);
    }

    @PostMapping("/batch")
    @Operation(summary = "Create multiple mappings", description = "Creates multiple policy entity mappings")
    @ApiResponse(responseCode = "200", description = "Successfully created mappings")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<List<PolicyEntityMapping>> createMappings(
            @Valid @RequestBody List<PolicyEntityMapping> mappings) {
        log.info("REST request to create {} mappings", mappings.size());
        List<PolicyEntityMapping> result = mappingService.createMappings(mappings);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update mapping", description = "Updates an existing policy entity mapping")
    @ApiResponse(responseCode = "200", description = "Successfully updated mapping")
    @ApiResponse(responseCode = "404", description = "Mapping not found")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<PolicyEntityMapping> updateMapping(
            @PathVariable Integer id,
            @Valid @RequestBody PolicyEntityMapping mapping) {
        log.info("REST request to update mapping : {}", id);
        PolicyEntityMapping result = mappingService.updateMapping(id, mapping);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete mapping", description = "Deletes a specific policy entity mapping")
    @ApiResponse(responseCode = "204", description = "Successfully deleted mapping")
    @ApiResponse(responseCode = "404", description = "Mapping not found")
    public ResponseEntity<Void> deleteMapping(@PathVariable Integer id) {
        log.info("REST request to delete mapping : {}", id);
        mappingService.deleteMapping(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/policy/{policyId}")
    @Operation(summary = "Delete mappings by policy", description = "Deletes all mappings for a specific policy")
    @ApiResponse(responseCode = "204", description = "Successfully deleted mappings")
    public ResponseEntity<Void> deleteMappingsByPolicy(@PathVariable Integer policyId) {
        log.info("REST request to delete mappings for policy : {}", policyId);
        mappingService.deleteMappingsByPolicyId(policyId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/policy/{policyId}/entity-type/{entityType}")
    @Operation(summary = "Create mappings for policy and entity type",
            description = "Creates multiple mappings for a specific policy and entity type")
    @ApiResponse(responseCode = "200", description = "Successfully created mappings")
    @ApiResponse(responseCode = "400", description = "Invalid input data")
    public ResponseEntity<List<PolicyEntityMapping>> createMappingsForPolicyAndEntityType(
            @PathVariable Integer policyId,
            @PathVariable String entityType,
            @Valid @RequestBody List<PolicyEntityMapping> mappings) {
        log.info("REST request to create mappings for policy {} and entity type {}", policyId, entityType);

        // Validate that all mappings match the path parameters
        if (mappings.stream().anyMatch(m -> !m.getPolicyId().equals(policyId) ||
                !m.getEntityType().equals(entityType))) {
            log.error("Invalid request: mappings don't match policy ID or entity type");
            return ResponseEntity.badRequest().build();
        }

        List<PolicyEntityMapping> result = mappingService.createMappings(mappings);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/policy/{policyId}/entity-type/{entityType}")
    @Operation(summary = "Delete mappings by policy and entity type",
            description = "Deletes all mappings for a specific policy and entity type combination")
    @ApiResponse(responseCode = "204", description = "Successfully deleted mappings")
    public ResponseEntity<Void> deleteMappingsByPolicyAndEntityType(
            @PathVariable Integer policyId,
            @PathVariable String entityType) {
        log.info("REST request to delete mappings for policy {} and entity type {}", policyId, entityType);
        mappingService.deleteMappingsByPolicyIdAndEntityType(policyId, entityType);
        return ResponseEntity.noContent().build();
    }
}