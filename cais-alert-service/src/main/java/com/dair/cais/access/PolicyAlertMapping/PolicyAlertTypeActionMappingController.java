package com.dair.cais.access.PolicyAlertMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/policy-mappings")
public class PolicyAlertTypeActionMappingController {
    private static final Logger logger = LoggerFactory.getLogger(PolicyAlertTypeActionMappingController.class);

    @Autowired
    private PolicyAlertTypeActionMappingService mappingService;

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<PolicyAlertTypeActionMapping>> getMappingsByPolicyId(@PathVariable Integer policyId) {
        logger.info("Received request to get mappings for policy ID: {}", policyId);
        List<PolicyAlertTypeActionMapping> mappings = mappingService.getMappingsByPolicyId(policyId);
        logger.info("Returning {} mappings for policy ID: {}", mappings.size(), policyId);
        return ResponseEntity.ok(mappings);
    }

    @PostMapping
    public ResponseEntity<PolicyAlertTypeActionMapping> createMapping(@RequestBody PolicyAlertTypeActionMapping mapping) {
        logger.info("Received request to create new mapping for policy ID: {}", mapping.getPolicyId());
        PolicyAlertTypeActionMapping createdMapping = mappingService.createMapping(mapping);
        logger.info("Created new mapping with ID: {} for policy ID: {}", createdMapping.getPataId(), createdMapping.getPolicyId());
        return ResponseEntity.ok(createdMapping);
    }

    @PutMapping("/{pataId}")
    public ResponseEntity<PolicyAlertTypeActionMapping> updateMapping(@PathVariable Integer pataId, @RequestBody PolicyAlertTypeActionMapping mapping) {
        logger.info("Received request to update mapping with ID: {}", pataId);
        PolicyAlertTypeActionMapping updatedMapping = mappingService.updateMapping(pataId, mapping);
        if (updatedMapping != null) {
            logger.info("Updated mapping with ID: {}", pataId);
            return ResponseEntity.ok(updatedMapping);
        } else {
            logger.warn("Mapping not found for ID: {}", pataId);
            return ResponseEntity.notFound().build();
        }
    }

    // @PutMapping("/policyid/{policyId}")
    // public ResponseEntity<PolicyAlertTypeActionMapping> updateMappingPolicyId(@PathVariable Integer policyId, @RequestBody PolicyAlertTypeActionMapping mapping) {
    //     logger.info("Received request to update mapping with policyId: {}", policyId);
    //     PolicyAlertTypeActionMapping updatedMapping = mappingService.updateMappingPolicyId(policyId, mapping);
    //     if (updatedMapping != null) {
    //         logger.info("Updated mapping with policyId: {}", policyId);
    //         return ResponseEntity.ok(updatedMapping);
    //     } else {
    //         logger.warn("Mapping not found for policyId: {}", policyId);
    //         return ResponseEntity.notFound().build();
    //     }
    // }

    @DeleteMapping("/{pataId}")
    public ResponseEntity<Void> deleteMapping(@PathVariable Integer pataId) {
        logger.info("Received request to delete mapping with ID: {}", pataId);
        mappingService.deleteMapping(pataId);
        logger.info("Deleted mapping with ID: {}", pataId);
        return ResponseEntity.noContent().build();
    }
}