package com.dair.cais.access.PolicyReportMapping;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/batch")
    public ResponseEntity<List<PolicyReportActionMapping>> createOrUpdateMappings(@RequestBody List<PolicyReportActionMapping> mappings) {
        log.info("Received request to create or update {} mappings", mappings.size());

        List<PolicyReportActionMapping> resultMappings = mappingService.createOrUpdateMappings(mappings);

        log.info("Created or updated {} mappings", resultMappings.size());

        if (resultMappings.isEmpty()) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.ok(resultMappings);
        }
    }

    @DeleteMapping("/policy/{policyId}")
    public ResponseEntity<Void> deleteMappingsByPolicyId(@PathVariable Integer policyId) {
        log.info("Received request to delete mappings for policyId: {}", policyId);

        mappingService.deleteMappingsByPolicyId(policyId);

        log.info("Deleted mappings for policyId: {}", policyId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/policies")
    public ResponseEntity<Void> deleteMappingsByPolicyIds(@RequestBody List<Integer> policyIds) {
        log.info("Received request to delete mappings for {} policyIds", policyIds.size());

        mappingService.deleteMappingsByPolicyIds(policyIds);

        log.info("Deleted mappings for {} policyIds", policyIds.size());

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
