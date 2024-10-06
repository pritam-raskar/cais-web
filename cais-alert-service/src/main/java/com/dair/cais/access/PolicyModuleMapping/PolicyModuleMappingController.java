package com.dair.cais.access.PolicyModuleMapping;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
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

    @PostMapping
    public ResponseEntity<PolicyModuleMapping> createMapping(@Valid @RequestBody PolicyModuleMapping mapping) {
        log.info("Received request to create a new policy module mapping");
        PolicyModuleMapping createdMapping = policyModuleMappingService.createMapping(mapping);
        return new ResponseEntity<>(createdMapping, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PolicyModuleMapping> updateMapping(@PathVariable Integer id, @Valid @RequestBody PolicyModuleMapping mapping) {
        log.info("Received request to update policy module mapping with id: {}", id);
        return policyModuleMappingService.updateMapping(id, mapping)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMapping(@PathVariable Integer id) {
        log.info("Received request to delete policy module mapping with id: {}", id);
        policyModuleMappingService.deleteMapping(id);
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