package com.dair.cais.access.PolicyReportMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/policy-report-mappings")
@RequiredArgsConstructor
public class PolicyReportActionMappingController {

    private final PolicyReportActionMappingService mappingService;

    @GetMapping
    public ResponseEntity<List<PolicyReportActionMapping>> getAllMappings() {
        log.info("Received request to get all policy report action mappings");
        List<PolicyReportActionMapping> mappings = mappingService.getAllMappings();
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PolicyReportActionMapping> getMappingById(@PathVariable Integer id) {
        log.info("Received request to get policy report action mapping with id: {}", id);
        return mappingService.getMappingById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<PolicyReportActionMapping> createMapping(@Valid @RequestBody PolicyReportActionMapping mapping) {
        log.info("Received request to create a new policy report action mapping");
        PolicyReportActionMapping createdMapping = mappingService.createMapping(mapping);
        return new ResponseEntity<>(createdMapping, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PolicyReportActionMapping> updateMapping(@PathVariable Integer id, @Valid @RequestBody PolicyReportActionMapping mapping) {
        log.info("Received request to update policy report action mapping with id: {}", id);
        return mappingService.updateMapping(id, mapping)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMapping(@PathVariable Integer id) {
        log.info("Received request to delete policy report action mapping with id: {}", id);
        mappingService.deleteMapping(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/policy/{policyId}")
    public ResponseEntity<List<PolicyReportActionMapping>> getMappingsByPolicyId(@PathVariable Integer policyId) {
        log.info("Received request to get policy report action mappings for policy id: {}", policyId);
        List<PolicyReportActionMapping> mappings = mappingService.getMappingsByPolicyId(policyId);
        return ResponseEntity.ok(mappings);
    }

    @GetMapping("/report/{reportId}")
    public ResponseEntity<List<PolicyReportActionMapping>> getMappingsByReportId(@PathVariable Integer reportId) {
        log.info("Received request to get policy report action mappings for report id: {}", reportId);
        List<PolicyReportActionMapping> mappings = mappingService.getMappingsByReportId(reportId);
        return ResponseEntity.ok(mappings);
    }
}
