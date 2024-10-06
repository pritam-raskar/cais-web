package com.dair.cais.access.RolePolicyMapping;

import com.dair.cais.access.Role.RoleEntity;
import com.dair.cais.access.Role.RoleRepository;
import com.dair.cais.access.policy.PolicyEntity;
import com.dair.cais.access.policy.PolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RolesPolicyMappingService {
    private static final Logger logger = LoggerFactory.getLogger(RolesPolicyMappingService.class);

    @Autowired
    private RolesPolicyMappingRepository repository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private RolesPolicyMappingMapper mapper;

    public List<RolesPolicyMapping> getAllMappings() {
        logger.info("Fetching all roles-policy mappings");
        return repository.findAll().stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }

    public Optional<RolesPolicyMapping> getMappingById(Integer rpmId) {
        logger.info("Fetching roles-policy mapping with ID: {}", rpmId);
        return repository.findById(rpmId).map(mapper::toModel);
    }

    public List<RolesPolicyMapping> getMappingsByPolicyId(Integer policyId) {
        logger.info("Fetching roles-policy mappings for policy ID: {}", policyId);
        return repository.findByPolicyPolicyId(policyId).stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }

    public List<RolesPolicyMapping> getMappingsByRoleId(Integer roleId) {
        logger.info("Fetching roles-policy mappings for role ID: {}", roleId);
        return repository.findByRoleRoleId(roleId).stream()
                .map(mapper::toModel)
                .collect(Collectors.toList());
    }

    public RolesPolicyMapping createMapping(Integer policyId, Integer roleId) {
        logger.info("Creating new roles-policy mapping for policy ID: {} and role ID: {}", policyId, roleId);
        PolicyEntity policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new RuntimeException("Policy not found"));
        RoleEntity role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        RolesPolicyMappingEntity entity = new RolesPolicyMappingEntity();
        entity.setPolicy(policy);
        entity.setRole(role);

        RolesPolicyMappingEntity savedEntity = repository.save(entity);
        return mapper.toModel(savedEntity);
    }

    public Optional<RolesPolicyMapping> updateMapping(Integer rpmId, Integer policyId, Integer roleId) {
        logger.info("Updating roles-policy mapping with ID: {}", rpmId);
        return repository.findById(rpmId)
                .map(existingMapping -> {
                    PolicyEntity policy = policyRepository.findById(policyId)
                            .orElseThrow(() -> new RuntimeException("Policy not found"));
                    RoleEntity role = roleRepository.findById(roleId)
                            .orElseThrow(() -> new RuntimeException("Role not found"));

                    existingMapping.setPolicy(policy);
                    existingMapping.setRole(role);
                    RolesPolicyMappingEntity updatedEntity = repository.save(existingMapping);
                    return mapper.toModel(updatedEntity);
                });
    }

    public void deleteMapping(Integer rpmId) {
        logger.info("Deleting roles-policy mapping with ID: {}", rpmId);
        repository.deleteById(rpmId);
    }
}