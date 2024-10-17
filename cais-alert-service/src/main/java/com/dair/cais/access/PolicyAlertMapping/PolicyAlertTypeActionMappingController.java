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


    @PostMapping("/create")
    public ResponseEntity<List<PolicyAlertTypeActionMapping>> createMappings(@RequestBody List<PolicyAlertTypeActionMapping> mappings) {
        logger.info("Received request to create {} new mappings", mappings.size());

        List<PolicyAlertTypeActionMapping> createdMappings = mappingService.createMappings(mappings);

        logger.info("Created {} new mappings", createdMappings.size());

        if (createdMappings.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(createdMappings);
        }
    }

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


    @PutMapping("/update")
    public ResponseEntity<List<PolicyAlertTypeActionMapping>> upsertMappings(@RequestBody List<PolicyAlertTypeActionMapping> mappingsToUpsert) {
        logger.info("Received request to upsert {} mappings", mappingsToUpsert.size());

        List<PolicyAlertTypeActionMapping> upsertedMappings = mappingService.upsertMappings(mappingsToUpsert);

        logger.info("Upserted {} mappings", upsertedMappings.size());

        if (upsertedMappings.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(upsertedMappings);
        }
    }

    @DeleteMapping("/delete-policy-alert-mapping/{policyId}")
    public ResponseEntity<Void> deleteMappingsByPolicyId(@PathVariable Integer policyId) {
        logger.info("Received request to delete mappings for policyId: {}", policyId);

        mappingService.deleteMappingsByPolicyId(policyId);

        logger.info("Deleted mappings for policyId: {}", policyId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping ("/delete-policies-alert-mapping")
    public ResponseEntity<Void> deleteMappingsByPolicyIds(@RequestBody List<Integer> policyIds) {
        logger.info("Received request to delete mappings for {} policyIds", policyIds.size());

        mappingService.deleteMappingsByPolicyIds(policyIds);

        logger.info("Deleted mappings for {} policyIds", policyIds.size());

        return ResponseEntity.noContent().build();
    }
}