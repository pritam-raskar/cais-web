package com.dair.cais.access.RolePolicyMapping;

import com.dair.cais.access.Role.RoleEntity;
import com.dair.cais.access.Role.RoleRepository;
import com.dair.cais.access.policy.PolicyEntity;
import com.dair.cais.access.policy.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RolesPolicyMappingService {
    private final RolesPolicyMappingRepository repository;
    private final PolicyRepository policyRepository;
    private final RoleRepository roleRepository;
    private final RolesPolicyMappingMapper mapper;

    public List<RolesPolicyMapping> getAllMappings() {
        log.info("Fetching all roles-policy mappings");
        return repository.findAllWithPolicyAndRole().stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }

    public Optional<RolesPolicyMapping> getMappingById(Integer rpmId) {
        log.info("Fetching roles-policy mapping with ID: {}", rpmId);
        return repository.findByIdWithPolicyAndRole(rpmId)
                .map(mapper::toModel);
    }

    public List<RolesPolicyMapping> getMappingsByPolicyId(Integer policyId) {
        log.info("Fetching roles-policy mappings for policy ID: {}", policyId);
        return repository.findByPolicyPolicyIdWithPolicyAndRole(policyId).stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }

    public List<RolesPolicyMapping> getMappingsByRoleId(Integer roleId) {
        log.info("Fetching roles-policy mappings for role ID: {}", roleId);
        return repository.findByRoleRoleIdWithPolicyAndRole(roleId).stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }

    @Transactional
    public RolesPolicyMapping createMapping(Integer policyId, Integer roleId) {
        log.info("Creating new roles-policy mapping for policy ID: {} and role ID: {}", policyId, roleId);

        PolicyEntity policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found with ID: " + policyId));

        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

        // Check if mapping already exists
        Optional<RolesPolicyMappingEntity> existingMapping =
                repository.findByPolicyIdAndRoleIdWithPolicyAndRole(policyId, roleId);

        if (existingMapping.isPresent()) {
            log.info("Mapping already exists for policy ID: {} and role ID: {}", policyId, roleId);
            return mapper.toModel(existingMapping.get());
        }

        RolesPolicyMappingEntity entity = new RolesPolicyMappingEntity();
        entity.setPolicy(policy);
        entity.setRole(role);

        RolesPolicyMappingEntity savedEntity = repository.save(entity);
        return mapper.toModel(savedEntity);
    }


    @Transactional
    public void deletePolicyMappingsForRole(Integer roleId, List<Integer> policyIds) {
        log.debug("Deleting policy mappings for role: {} with policies: {}", roleId, policyIds);

        // Verify role exists
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.error("Role not found with ID: {}", roleId);
                    return new ResourceNotFoundException("Role not found with ID: " + roleId);
                });

        // Find existing mappings
        List<RolesPolicyMappingEntity> existingMappings =
                repository.findByRoleRoleIdAndPolicyPolicyIdIn(roleId, policyIds);

        if (existingMappings.isEmpty()) {
            log.warn("No mappings found for role: {} and policies: {}", roleId, policyIds);
            return;
        }

        try {
            repository.deleteAll(existingMappings);
            log.info("Successfully deleted {} policy mappings for role: {}",
                    existingMappings.size(), roleId);
        } catch (Exception e) {
            log.error("Error deleting policy mappings for role: {} with policies: {}",
                    roleId, policyIds, e);
            throw new RuntimeException("Error deleting policy mappings", e);
        }
    }

    @Transactional
    public void deleteSinglePolicyMappingForRole(Integer roleId, Integer policyId) {
        log.debug("Deleting policy mapping for role: {} and policy: {}", roleId, policyId);

        // Verify role exists
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.error("Role not found with ID: {}", roleId);
                    return new ResourceNotFoundException("Role not found with ID: " + roleId);
                });

        // Find and delete the mapping
        RolesPolicyMappingEntity mapping = repository.findByRoleRoleIdAndPolicyPolicyId(roleId, policyId)
                .orElseThrow(() -> {
                    log.error("Mapping not found for role: {} and policy: {}", roleId, policyId);
                    return new ResourceNotFoundException(
                            String.format("Mapping not found for role ID: %d and policy ID: %d",
                                    roleId, policyId));
                });

        try {
            repository.delete(mapping);
            log.info("Successfully deleted policy mapping for role: {} and policy: {}",
                    roleId, policyId);
        } catch (Exception e) {
            log.error("Error deleting policy mapping for role: {} and policy: {}",
                    roleId, policyId, e);
            throw new RuntimeException("Error deleting policy mapping", e);
        }
    }




    @Transactional
    public Optional<RolesPolicyMapping> updateMapping(Integer rpmId, Integer policyId, Integer roleId) {
        log.info("Updating roles-policy mapping with ID: {}", rpmId);
        return repository.findByIdWithPolicyAndRole(rpmId)
                .map(existingMapping -> {
                    PolicyEntity policy = policyRepository.findById(policyId)
                            .orElseThrow(() -> new RuntimeException("Policy not found with ID: " + policyId));

                    RoleEntity role = roleRepository.findById(roleId)
                            .orElseThrow(() -> new RuntimeException("Role not found with ID: " + roleId));

                    existingMapping.setPolicy(policy);
                    existingMapping.setRole(role);

                    RolesPolicyMappingEntity updatedEntity = repository.save(existingMapping);
                    return mapper.toModel(updatedEntity);
                });
    }







    @Transactional
    public void deleteMapping(Integer rpmId) {
        log.info("Deleting roles-policy mapping with ID: {}", rpmId);
        repository.deleteById(rpmId);
    }

    @Transactional
    public List<RolesPolicyMapping> bulkCreateOrUpdate(List<RolesPolicyMapping> requests) {
        log.info("Processing bulk create/update for {} mappings", requests.size());

        return requests.stream()
                .map(request -> {
                    if (request.getRpmId() != null) {
                        return updateMapping(request.getRpmId(), request.getPolicyId(), request.getRoleId())
                                .orElseGet(() -> createMapping(request.getPolicyId(), request.getRoleId()));
                    } else {
                        return createMapping(request.getPolicyId(), request.getRoleId());
                    }
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public List<RolesPolicyMapping> updateRolePolicyMappings(
            Integer roleId,
            List<RolesPolicyMappingController.RolePolicyRequest> requests) {

        log.debug("Starting update of policy mappings for role ID: {}", roleId);

        // Verify role exists
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> {
                    log.error("Role not found with ID: {}", roleId);
                    return new ResourceNotFoundException("Role not found with ID: " + roleId);
                });

        // Get all requested policy IDs
        Set<Integer> requestedPolicyIds = requests.stream()
                .map(RolesPolicyMappingController.RolePolicyRequest::policyId)
                .collect(Collectors.toSet());

        // Validate and get all policies
        Map<Integer, PolicyEntity> policyMap = validateAndGetPolicies(requestedPolicyIds);

        try {
            // Get existing mappings
            List<RolesPolicyMappingEntity> existingMappings =
                    repository.findByRoleRoleIdWithPolicyAndRole(roleId);

            // Create set of existing policy IDs
            Set<Integer> existingPolicyIds = existingMappings.stream()
                    .map(mapping -> mapping.getPolicy().getPolicyId())
                    .collect(Collectors.toSet());

            // Find mappings to delete (existing but not in request)
            Set<Integer> policyIdsToDelete = new HashSet<>(existingPolicyIds);
            policyIdsToDelete.removeAll(requestedPolicyIds);

            if (!policyIdsToDelete.isEmpty()) {
                log.debug("Deleting {} mappings for role ID: {}", policyIdsToDelete.size(), roleId);
                existingMappings.stream()
                        .filter(mapping -> policyIdsToDelete.contains(mapping.getPolicy().getPolicyId()))
                        .forEach(repository::delete);
            }

            // Find mappings to add (in request but not existing)
            Set<Integer> policyIdsToAdd = new HashSet<>(requestedPolicyIds);
            policyIdsToAdd.removeAll(existingPolicyIds);

            List<RolesPolicyMappingEntity> newMappings = policyIdsToAdd.stream()
                    .map(policyId -> {
                        RolesPolicyMappingEntity entity = new RolesPolicyMappingEntity();
                        entity.setRole(role);
                        entity.setPolicy(policyMap.get(policyId));
                        return entity;
                    })
                    .collect(Collectors.toList());

            if (!newMappings.isEmpty()) {
                log.debug("Adding {} new mappings for role ID: {}", newMappings.size(), roleId);
                repository.saveAll(newMappings);
            }

            // Fetch and return all current mappings
            List<RolesPolicyMappingEntity> finalMappings =
                    repository.findByRoleRoleIdWithPolicyAndRole(roleId);

            log.info("Successfully updated policy mappings for role ID: {}. Added: {}, Deleted: {}",
                    roleId, policyIdsToAdd.size(), policyIdsToDelete.size());

            return finalMappings.stream()
                    .map(mapper::toModel)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error updating policy mappings for role ID: {}", roleId, e);
            throw new RuntimeException("Failed to update policy mappings: " + e.getMessage(), e);
        }
    }

    private Map<Integer, PolicyEntity> validateAndGetPolicies(Set<Integer> policyIds) {
        List<PolicyEntity> policies = policyRepository.findAllById(policyIds);
        Map<Integer, PolicyEntity> policyMap = policies.stream()
                .collect(Collectors.toMap(PolicyEntity::getPolicyId, policy -> policy));

        // Check if any policies were not found
        Set<Integer> foundPolicyIds = policyMap.keySet();
        Set<Integer> missingPolicyIds = new HashSet<>(policyIds);
        missingPolicyIds.removeAll(foundPolicyIds);

        if (!missingPolicyIds.isEmpty()) {
            log.error("Policies not found with IDs: {}", missingPolicyIds);
            throw new ResourceNotFoundException("Policies not found with IDs: " + missingPolicyIds);
        }

        return policyMap;
    }
}




//package com.dair.cais.access.RolePolicyMapping;
//
//import com.dair.cais.access.Role.RoleEntity;
//import com.dair.cais.access.Role.RoleRepository;
//import com.dair.cais.access.policy.PolicyEntity;
//import com.dair.cais.access.policy.PolicyRepository;
//import jakarta.transaction.Transactional;
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.rest.webmvc.ResourceNotFoundException;
//import org.springframework.stereotype.Service;
//
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@Slf4j
//
//public class RolesPolicyMappingService {
//    private static final Logger logger = LoggerFactory.getLogger(RolesPolicyMappingService.class);
//
//    @Autowired
//    private RolesPolicyMappingRepository repository;
//
//    @Autowired
//    private PolicyRepository policyRepository;
//
//    @Autowired
//    private RoleRepository roleRepository;
//
//    @Autowired
//    private RolesPolicyMappingMapper mapper;
//
//    public List<RolesPolicyMapping> getAllMappings() {
//        logger.info("Fetching all roles-policy mappings");
//        return repository.findAll().stream()
//                .map(mapper::toModel)
//                .collect(Collectors.toList());
//    }
//
//    public Optional<RolesPolicyMapping> getMappingById(Integer rpmId) {
//        logger.info("Fetching roles-policy mapping with ID: {}", rpmId);
//        return repository.findById(rpmId).map(mapper::toModel);
//    }
//
//    public List<RolesPolicyMapping> getMappingsByPolicyId(Integer policyId) {
//        logger.info("Fetching roles-policy mappings for policy ID: {}", policyId);
//        return repository.findByPolicyPolicyId(policyId).stream()
//                .map(mapper::toModel)
//                .collect(Collectors.toList());
//    }
//
//    public List<RolesPolicyMapping> getMappingsByRoleId(Integer roleId) {
//        logger.info("Fetching roles-policy mappings for role ID: {}", roleId);
//        return repository.findByRoleRoleId(roleId).stream()
//                .map(mapper::toModel)
//                .collect(Collectors.toList());
//    }
//
//
//    @Transactional
//    public List<RolesPolicyMapping> bulkCreateOrUpdate(List<RolesPolicyMapping> requests) {
//        log.info("Processing bulk create/update for {} mappings", requests.size());
//
//        // First, validate and fetch all required policies and roles
//        Map<Integer, PolicyEntity> policyCache = new HashMap<>();
//        Map<Integer, RoleEntity> roleCache = new HashMap<>();
//
//        // Collect all unique policy and role IDs
//        List<Integer> policyIds = requests.stream()
//                .map(RolesPolicyMapping::getPolicyId)
//                .distinct()
//                .collect(Collectors.toList());
//
//        List<Integer> roleIds = requests.stream()
//                .map(RolesPolicyMapping::getRoleId)
//                .distinct()
//                .collect(Collectors.toList());
//
//        // Fetch all required policies
//        policyRepository.findAllById(policyIds)
//                .forEach(policy -> policyCache.put(policy.getPolicyId(), policy));
//
//        // Fetch all required roles
//        roleRepository.findAllById(roleIds)
//                .forEach(role -> roleCache.put(role.getRoleId(), role));
//
//        // Validate that all required policies and roles exist
//        validateEntities(requests, policyCache, roleCache);
//
//        // Process each request
//        List<RolesPolicyMappingEntity> entitiesToSave = new ArrayList<>();
//        for (RolesPolicyMapping request : requests) {
//            RolesPolicyMappingEntity entity;
//
//            if (request.getRpmId() != null) {
//                // Update existing mapping
//                entity = repository.findById(request.getRpmId())
//                        .orElseGet(RolesPolicyMappingEntity::new);
//            } else {
//                // Create new mapping
//                entity = new RolesPolicyMappingEntity();
//            }
//
//            // Set or update the entity fields
//            entity.setPolicy(policyCache.get(request.getPolicyId()));
//            entity.setRole(roleCache.get(request.getRoleId()));
//
//            entitiesToSave.add(entity);
//        }
//
//        // Bulk save all entities
//        List<RolesPolicyMappingEntity> savedEntities = repository.saveAll(entitiesToSave);
//
//        log.info("Successfully processed {} role-policy mappings", savedEntities.size());
//
//        // Convert and return the results
//        return savedEntities.stream()
//                .map(mapper::toModel)
//                .collect(Collectors.toList());
//    }
//
//    private void validateEntities(
//            List<RolesPolicyMapping> requests,
//            Map<Integer, PolicyEntity> policyCache,
//            Map<Integer, RoleEntity> roleCache) {
//
//        List<String> errors = new ArrayList<>();
//
//        for (RolesPolicyMapping request : requests) {
//            if (!policyCache.containsKey(request.getPolicyId())) {
//                errors.add("Policy not found with ID: " + request.getPolicyId());
//            }
//            if (!roleCache.containsKey(request.getRoleId())) {
//                errors.add("Role not found with ID: " + request.getRoleId());
//            }
//        }
//
//        if (!errors.isEmpty()) {
//            log.error("Validation errors during bulk processing: {}", errors);
//            throw new ResourceNotFoundException("Validation failed: " + String.join(", ", errors));
//        }
//    }
//
//    // Helper method to check for existing mapping
//    private Optional<RolesPolicyMappingEntity> findExistingMapping(Integer policyId, Integer roleId) {
//        return repository.findByPolicyPolicyIdAndRoleRoleId(policyId, roleId);
//    }
//
//    public RolesPolicyMapping createMapping(Integer policyId, Integer roleId) {
//        logger.info("Creating new roles-policy mapping for policy ID: {} and role ID: {}", policyId, roleId);
//        PolicyEntity policy = policyRepository.findById(policyId)
//                .orElseThrow(() -> new RuntimeException("Policy not found"));
//        RoleEntity role = roleRepository.findById(roleId)
//                .orElseThrow(() -> new RuntimeException("Role not found"));
//
//        RolesPolicyMappingEntity entity = new RolesPolicyMappingEntity();
//        entity.setPolicy(policy);
//        entity.setRole(role);
//
//        RolesPolicyMappingEntity savedEntity = repository.save(entity);
//        return mapper.toModel(savedEntity);
//    }
//
//    public Optional<RolesPolicyMapping> updateMapping(Integer rpmId, Integer policyId, Integer roleId) {
//        logger.info("Updating roles-policy mapping with ID: {}", rpmId);
//        return repository.findById(rpmId)
//                .map(existingMapping -> {
//                    PolicyEntity policy = policyRepository.findById(policyId)
//                            .orElseThrow(() -> new RuntimeException("Policy not found"));
//                    RoleEntity role = roleRepository.findById(roleId)
//                            .orElseThrow(() -> new RuntimeException("Role not found"));
//
//                    existingMapping.setPolicy(policy);
//                    existingMapping.setRole(role);
//                    RolesPolicyMappingEntity updatedEntity = repository.save(existingMapping);
//                    return mapper.toModel(updatedEntity);
//                });
//    }
//
//    public void deleteMapping(Integer rpmId) {
//        logger.info("Deleting roles-policy mapping with ID: {}", rpmId);
//        repository.deleteById(rpmId);
//    }
//}