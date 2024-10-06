package com.dair.cais.access.policy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/policies")
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
}
