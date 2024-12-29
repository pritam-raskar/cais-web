package com.dair.cais.access.policy;

import com.dair.cais.access.PolicyEntityMapping.PolicyEntityMappingRepository;
import com.dair.cais.access.RolePolicyMapping.RolesPolicyMappingRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/policies")
@RequiredArgsConstructor
@Tag(name = "Policy Management", description = "APIs for managing policies")
public class PolicyService {

    @Autowired
    private PolicyRepository policyRepository;
    private final RolesPolicyMappingRepository rolesPolicyMappingRepository;
    private final PolicyEntityMappingRepository policyEntityMappingRepository;

    @Autowired
    private PolicyMapper policyMapper;

    public List<Policy> getAllPolicies() {
        return policyRepository.findAll().stream()
                .map(policyMapper::toModel)
                .collect(Collectors.toList());
    }

    public Policy getPolicyById(Integer id) {
        log.debug("Fetching policy with ID: {}", id);

        PolicyEntity policyEntity = policyRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Policy not found with ID: {}", id);
                    return new EntityNotFoundException("Policy not found with ID: " + id);
                });

        List<AssociatedRole> roleDetails = null;
        try {
            roleDetails = rolesPolicyMappingRepository.findRoleDetailsByPolicy(policyEntity);
            log.debug("Found {} associated roles for policy {}",
                    roleDetails.size(), id);
        } catch (Exception e) {
            log.error("Error fetching associated roles for policy {}: {}",
                    id, e.getMessage());
        }

        Policy policy = policyMapper.toDto(policyEntity, roleDetails);
        log.debug("Successfully fetched policy: {}", policy);

        return policy;
    }

    public Policy createPolicy(Policy policy) {
        PolicyEntity entity = policyMapper.toEntity(policy);
        entity.setIsActive(true);  // Set new policies as active by default
        PolicyEntity savedEntity = policyRepository.save(entity);
        return policyMapper.toModel(savedEntity);
    }

    public Policy updatePolicy(Integer id, Policy policy) {
        return policyRepository.findById(id)
                .map(existingEntity -> {
                    existingEntity.setName(policy.getName());
                    existingEntity.setDescription(policy.getDescription());
                    existingEntity.setType(policy.getType());
                    existingEntity.setIsActive(policy.getIsActive());
                    PolicyEntity updatedEntity = policyRepository.save(existingEntity);
                    return policyMapper.toModel(updatedEntity);
                })
                .orElse(null);
    }

    public Policy deactivatePolicy(Integer id) {
        return policyRepository.findById(id)
                .map(entity -> {
                    entity.setIsActive(false);
                    PolicyEntity updatedEntity = policyRepository.save(entity);
                    return policyMapper.toModel(updatedEntity);
                })
                .orElse(null);
    }

    public List<Policy> getActivePolicies() {
        return policyRepository.findByIsActiveTrue().stream()
                .map(policyMapper::toModel)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePolicy(Integer policyId) {
        log.debug("Attempting to delete policy with ID: {}", policyId);

        PolicyEntity policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new EntityNotFoundException("Policy not found with ID: " + policyId));

        Map<Integer, List<String>> rolesByPolicy = checkPolicyUsageInRoles(Collections.singletonList(policy));
        if (!rolesByPolicy.isEmpty()) {
            throw new PolicyInUseException("Policy is in use by roles", rolesByPolicy);
        }

        deletePolicyAndMappings(policy);
        log.info("Successfully deleted policy with ID: {}", policyId);
    }

    @Transactional
    public void bulkDeletePolicies(List<Integer> policyIds) {
        log.debug("Attempting to delete {} policies", policyIds.size());

        List<PolicyEntity> policies = policyRepository.findAllById(policyIds);
        if (policies.size() != policyIds.size()) {
            Set<Integer> foundIds = policies.stream()
                    .map(PolicyEntity::getPolicyId)
                    .collect(Collectors.toSet());
            List<Integer> notFound = policyIds.stream()
                    .filter(id -> !foundIds.contains(id))
                    .collect(Collectors.toList());
            throw new EntityNotFoundException("Some policies not found: " + notFound);
        }

        Map<Integer, List<String>> rolesByPolicy = checkPolicyUsageInRoles(policies);
        if (!rolesByPolicy.isEmpty()) {
            throw new PolicyInUseException("Some policies are in use by roles", rolesByPolicy);
        }

        for (PolicyEntity policy : policies) {
            deletePolicyAndMappings(policy);
        }
        log.info("Successfully deleted {} policies", policyIds.size());
    }

    private Map<Integer, List<String>> checkPolicyUsageInRoles(List<PolicyEntity> policies) {
        Map<Integer, List<String>> rolesByPolicy = new HashMap<>();

        for (PolicyEntity policy : policies) {
            List<String> associatedRoles = rolesPolicyMappingRepository.findRoleNamesByPolicy(policy);
            if (!associatedRoles.isEmpty()) {
                rolesByPolicy.put(policy.getPolicyId(), associatedRoles);
            }
        }

        return rolesByPolicy;
    }

    @Transactional
    protected void deletePolicyAndMappings(PolicyEntity policy) {
        log.debug("Deleting entity mappings for policy ID: {}", policy.getPolicyId());

        // Delete all entity mappings (alert types, modules, reports) using PolicyEntityMapping
        policyEntityMappingRepository.deleteByPolicyPolicyId(policy.getPolicyId());

        // Delete role mappings
        rolesPolicyMappingRepository.deleteByPolicy(policy);

        // Finally delete the policy
        policyRepository.delete(policy);
        log.debug("Deleted policy and all associated mappings for ID: {}", policy.getPolicyId());
    }
}