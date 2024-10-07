package com.dair.cais.access.PolicyAlertMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/policy-mappings")
@Tag(name = "Policy Alert Type Action Mapping", description = "Manage policy alert type action mappings")
@RequiredArgsConstructor
@Validated
public class PolicyAlertTypeActionMappingController {
    private static final Logger logger = LoggerFactory.getLogger(PolicyAlertTypeActionMappingController.class);

    @Autowired
    private PolicyAlertTypeActionMappingService mappingService;

    @GetMapping("/policy/{policyId}")
    @Operation(summary = "Get mappings by policy ID", description = "Retrieve all mappings for a specific policy")
    @ApiResponse(responseCode = "200", description = "Mappings retrieved successfully")
    @ApiResponse(responseCode = "404", description = "Policy not found")
    public ResponseEntity<List<PolicyAlertTypeActionMapping>> getMappingsByPolicyId(@PathVariable Integer policyId) {
        log.info("Fetching mappings for policy ID: {}", policyId);
        List<PolicyAlertTypeActionMapping> mappings = mappingService.getMappingsByPolicyId(policyId);
        return ResponseEntity.ok(mappings);
    }

    @PostMapping
    @Operation(summary = "Create new mappings", description = "Create multiple new policy alert type action mappings")
    @ApiResponse(responseCode = "201", description = "Mappings created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<List<PolicyAlertTypeActionMapping>> createMappings(@Valid @RequestBody List<PolicyAlertTypeActionMapping> mappings) {
        log.info("Creating {} new mappings", mappings.size());
        List<PolicyAlertTypeActionMapping> createdMappings = mappingService.createMappings(mappings);
        return new ResponseEntity<>(createdMappings, HttpStatus.CREATED);
    }

    @PutMapping
    @Operation(summary = "Update mappings", description = "Update existing policy alert type action mappings")
    @ApiResponse(responseCode = "200", description = "Mappings updated successfully")
    @ApiResponse(responseCode = "404", description = "One or more mappings not found")
    public ResponseEntity<List<PolicyAlertTypeActionMapping>> updateMappings(@Valid @RequestBody List<PolicyAlertTypeActionMapping> mappings) {
        log.info("Updating {} mappings", mappings.size());
        List<PolicyAlertTypeActionMapping> updatedMappings = mappingService.updateMappings(mappings);
        return ResponseEntity.ok(updatedMappings);
    }

    @DeleteMapping
    @Operation(summary = "Delete mappings by policy IDs", description = "Delete existing policy alert type action mappings for given policy IDs")
    @ApiResponse(responseCode = "204", description = "Mappings deleted successfully")
    @ApiResponse(responseCode = "404", description = "No mappings found for the given policy IDs")
    public ResponseEntity<Void> deleteMappings(@RequestBody List<Integer> policyIds) {
        log.info("Deleting mappings for {} policies", policyIds.size());
        mappingService.deleteMappings(policyIds);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/policy/{policyId}")
    @Operation(summary = "Delete mappings by policy ID", description = "Delete existing policy alert type action mappings for a given policy ID")
    @ApiResponse(responseCode = "204", description = "Mappings deleted successfully")
    @ApiResponse(responseCode = "404", description = "No mappings found for the given policy ID")
    public ResponseEntity<Void> deleteMappingsByPolicyId(@PathVariable Integer policyId) {
        log.info("Deleting mappings for policy ID: {}", policyId);
        mappingService.deleteMappingsByPolicyId(policyId);
        return ResponseEntity.noContent().build();
    }

//    @GetMapping("/policy/{policyId}")
//    public ResponseEntity<List<PolicyAlertTypeActionMapping>> getMappingsByPolicyId(@PathVariable Integer policyId) {
//        logger.info("Received request to get mappings for policy ID: {}", policyId);
//        List<PolicyAlertTypeActionMapping> mappings = mappingService.getMappingsByPolicyId(policyId);
//        logger.info("Returning {} mappings for policy ID: {}", mappings.size(), policyId);
//        return ResponseEntity.ok(mappings);
//    }
//
//    @PostMapping
//    public ResponseEntity<PolicyAlertTypeActionMapping> createMapping(@RequestBody PolicyAlertTypeActionMapping mapping) {
//        logger.info("Received request to create new mapping for policy ID: {}", mapping.getPolicyId());
//        PolicyAlertTypeActionMapping createdMapping = mappingService.createMapping(mapping);
//        logger.info("Created new mapping with ID: {} for policy ID: {}", createdMapping.getPataId(), createdMapping.getPolicyId());
//        return ResponseEntity.ok(createdMapping);
//    }
//
//    @PutMapping("/{pataId}")
//    public ResponseEntity<PolicyAlertTypeActionMapping> updateMapping(@PathVariable Integer pataId, @RequestBody PolicyAlertTypeActionMapping mapping) {
//        logger.info("Received request to update mapping with ID: {}", pataId);
//        PolicyAlertTypeActionMapping updatedMapping = mappingService.updateMapping(pataId, mapping);
//        if (updatedMapping != null) {
//            logger.info("Updated mapping with ID: {}", pataId);
//            return ResponseEntity.ok(updatedMapping);
//        } else {
//            logger.warn("Mapping not found for ID: {}", pataId);
//            return ResponseEntity.notFound().build();
//        }
//    }
//
//    // @PutMapping("/policyid/{policyId}")
//    // public ResponseEntity<PolicyAlertTypeActionMapping> updateMappingPolicyId(@PathVariable Integer policyId, @RequestBody PolicyAlertTypeActionMapping mapping) {
//    //     logger.info("Received request to update mapping with policyId: {}", policyId);
//    //     PolicyAlertTypeActionMapping updatedMapping = mappingService.updateMappingPolicyId(policyId, mapping);
//    //     if (updatedMapping != null) {
//    //         logger.info("Updated mapping with policyId: {}", policyId);
//    //         return ResponseEntity.ok(updatedMapping);
//    //     } else {
//    //         logger.warn("Mapping not found for policyId: {}", policyId);
//    //         return ResponseEntity.notFound().build();
//    //     }
//    // }
//
//    @DeleteMapping("/{pataId}")
//    public ResponseEntity<Void> deleteMapping(@PathVariable Integer pataId) {
//        logger.info("Received request to delete mapping with ID: {}", pataId);
//        mappingService.deleteMapping(pataId);
//        logger.info("Deleted mapping with ID: {}", pataId);
//        return ResponseEntity.noContent().build();
//    }
}