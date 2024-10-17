package com.dair.cais.access.PolicyModuleMapping;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/policy-module-mappings")
@RequiredArgsConstructor
public class PolicyModuleMappingController {

    private final PolicyModuleMappingService policyModuleMappingService;

    @GetMapping
    public ResponseEntity<List<PolicyModuleMapping>> getAllMappings() {
        log.info("Received request to get all policy module mappings");
        List<PolicyModuleMapping> mappings = policyModuleMappingService.getAllMappings();
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyModuleMapping> getMappingById(@PathVariable Integer id) {
        log.info("Received request to get policy module mapping with id: {}", id);
        return policyModuleMappingService.getMappingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @PostMapping("/create")
    public ResponseEntity<List<PolicyModuleMapping>> createOrUpdateMappings(@RequestBody List<PolicyModuleMapping> mappings) {
        log.info("Received request to create or update {} mappings", mappings.size());

        List<PolicyModuleMapping> resultMappings = policyModuleMappingService.createOrUpdateMappings(mappings);

        log.info("Created or updated {} mappings", resultMappings.size());

        if (resultMappings.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(resultMappings);
        }
    }


    @DeleteMapping("/delete-policies-module-mapping/{policyId}")
    public ResponseEntity<Void> deleteMappingsByPolicyId(@PathVariable Integer policyId) {
        log.info("Received request to delete mappings for policyId: {}", policyId);

        policyModuleMappingService.deleteMappingsByPolicyId(policyId);

        log.info("Deleted mappings for policyId: {}", policyId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete-policies-module-mapping")
    public ResponseEntity<Void> deleteMappingsByPolicyIds(@RequestBody List<Integer> policyIds) {
        log.info("Received request to delete mappings for {} policyIds", policyIds.size());

        policyModuleMappingService.deleteMappingsByPolicyIds(policyIds);

        log.info("Deleted mappings for {} policyIds", policyIds.size());

        return ResponseEntity.noContent().build();
    }
    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<PolicyModuleMapping>> getMappingsByPolicyId(@PathVariable Integer policyId) {
        log.info("Received request to get policy module mappings for policy id: {}", policyId);
        List<PolicyModuleMapping> mappings = policyModuleMappingService.getMappingsByPolicyId(policyId);
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/module/{moduleId}")
    public ResponseEntity<List<PolicyModuleMapping>> getMappingsByModuleId(@PathVariable Integer moduleId) {
        log.info("Received request to get policy module mappings for module id: {}", moduleId);
        List<PolicyModuleMapping> mappings = policyModuleMappingService.getMappingsByModuleId(moduleId);
        return ResponseEntity.ok(mappings);
    }
}