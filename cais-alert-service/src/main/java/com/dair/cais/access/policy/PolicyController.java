package com.dair.cais.access.policy;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/policies")
@RequiredArgsConstructor
@Tag(name = "Policy Management", description = "APIs for managing policies")
public class PolicyController {

    @Autowired
    private PolicyService policyService;

    @GetMapping
    public ResponseEntity<List<Policy>> getAllPolicies() {
        List<Policy> policies = policyService.getAllPolicies();
        return ResponseEntity.ok(policies);
    }

    @GetMapping("/active")
    public ResponseEntity<List<Policy>> getActivePolicies() {
        List<Policy> activePolicies = policyService.getActivePolicies();
        return ResponseEntity.ok(activePolicies);
    }

    @GetMapping("/{policyid}")
    public ResponseEntity<Policy> getPolicyById(@PathVariable Integer policyid) {
        Policy policy = policyService.getPolicyById(policyid);
        if (policy != null) {
            return ResponseEntity.ok(policy);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/create")
    public ResponseEntity<Policy> createPolicy(@RequestBody Policy policy) {
        Policy createdPolicy = policyService.createPolicy(policy);
        return ResponseEntity.ok(createdPolicy);
    }

    @PutMapping("/{policyid}")
    public ResponseEntity<Policy> updatePolicy(@PathVariable Integer policyid, @RequestBody Policy policy) {
        Policy updatedPolicy = policyService.updatePolicy(policyid, policy);
        if (updatedPolicy != null) {
            return ResponseEntity.ok(updatedPolicy);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{policyid}/deactivate")
    public ResponseEntity<Policy> deactivatePolicy(@PathVariable Integer policyid) {
        Policy deactivatedPolicy = policyService.deactivatePolicy(policyid);
        if (deactivatedPolicy != null) {
            return ResponseEntity.ok(deactivatedPolicy);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{policyId}")
    @Operation(summary = "Delete a policy by ID",
            description = "Deletes a policy after validating it's not in use by any roles")
    @ApiResponse(responseCode = "204", description = "Policy successfully deleted")
    @ApiResponse(responseCode = "400", description = "Policy is in use by roles")
    @ApiResponse(responseCode = "404", description = "Policy not found")
    public ResponseEntity<?> deletePolicy(@PathVariable Integer policyId) {
        log.info("Received request to delete policy with ID: {}", policyId);

        try {
            policyService.deletePolicy(policyId);
            return ResponseEntity.noContent().build();
        } catch (PolicyInUseException e) {
            log.warn("Policy {} is in use by roles: {}", policyId, e.getRolesByPolicy());
            return ResponseEntity.badRequest().body(new PolicyDeletionError(e.getMessage(), e.getRolesByPolicy()));
        }
    }

    @DeleteMapping("/bulk")
    @Operation(summary = "Bulk delete policies",
            description = "Deletes multiple policies after validating none are in use by roles")
    @ApiResponse(responseCode = "204", description = "Policies successfully deleted")
    @ApiResponse(responseCode = "400", description = "One or more policies are in use by roles")
    public ResponseEntity<?> bulkDeletePolicies(@RequestBody List<Integer> policyIds) {
        log.info("Received request to delete {} policies", policyIds.size());

        try {
            policyService.bulkDeletePolicies(policyIds);
            return ResponseEntity.noContent().build();
        } catch (PolicyInUseException e) {
            log.warn("Some policies are in use by roles: {}", e.getRolesByPolicy());
            return ResponseEntity.badRequest().body(new PolicyDeletionError(e.getMessage(), e.getRolesByPolicy()));
        }
    }

    record PolicyDeletionError(String message, Map<Integer, List<String>> rolesByPolicy) {}
}
