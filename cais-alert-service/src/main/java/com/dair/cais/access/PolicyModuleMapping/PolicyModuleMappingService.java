package com.dair.cais.access.PolicyModuleMapping;


import com.dair.cais.access.Actions.ActionEntity;
import com.dair.cais.access.Actions.ActionRepository;
import com.dair.cais.access.modules.ModuleEntity;
import com.dair.cais.access.modules.ModuleRepository;
import com.dair.cais.access.policy.PolicyEntity;
import com.dair.cais.access.policy.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyModuleMappingService {

    private final PolicyModuleMappingRepository repository;
    private final PolicyRepository policyRepository;
    private final ModuleRepository moduleRepository;
    private final ActionRepository actionRepository;
    private final PolicyModuleMappingMapper mapper;

    @Transactional(readOnly = true)
    public List<PolicyModuleMapping> getAllMappings() {
        log.info("Fetching all policy module mappings");
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PolicyModuleMapping> getMappingById(Integer pmaId) {
        log.info("Fetching policy module mapping with ID: {}", pmaId);
        return repository.findById(pmaId).map(mapper::toDto);
    }

    @Transactional
    public List<PolicyModuleMapping> createOrUpdateMappings(List<PolicyModuleMapping> mappings) {
        List<PolicyModuleMapping> resultMappings = new ArrayList<>();

        for (PolicyModuleMapping mapping : mappings) {
            log.info("Creating/Updating mapping with policyId: {}, moduleId: {}, actionId: {}",
                    mapping.getPolicyId(), mapping.getModuleId(), mapping.getActionId());

            try {
                PolicyEntity policy = policyRepository.findById(mapping.getPolicyId())
                        .orElseThrow(() -> new RuntimeException("Policy not found with ID: " + mapping.getPolicyId()));
                log.info("Found policy: {}", policy.getPolicyId());

                ModuleEntity module = moduleRepository.findById(mapping.getModuleId())
                        .orElseThrow(() -> new RuntimeException("Module not found with ID: " + mapping.getModuleId()));
                log.info("Found module: {}", module.getModuleId());

                ActionEntity action = actionRepository.findById(mapping.getActionId())
                        .orElseThrow(() -> new RuntimeException("Action not found with ID: " + mapping.getActionId()));
                log.info("Found action: {}", action.getActionId());

                // Check if a mapping already exists
                PolicyModuleMappingEntity existingEntity = repository
                        .findByPolicyAndModuleAndAction(policy, module, action)
                        .orElse(null);

                PolicyModuleMappingEntity entity;

                if (existingEntity != null) {
                    // Update existing entity
                    log.info("Updating existing mapping with ID: {}", existingEntity.getPmaId());
                    existingEntity.setCondition(mapping.getCondition());
                    entity = existingEntity;
                } else {
                    // Create new entity
                    log.info("Creating new mapping");
                    entity = new PolicyModuleMappingEntity();
                    entity.setPolicy(policy);
                    entity.setModule(module);
                    entity.setAction(action);
                    entity.setCondition(mapping.getCondition());
                }

                PolicyModuleMappingEntity savedEntity = repository.save(entity);
                resultMappings.add(mapper.toDto(savedEntity));

                log.info("Saved mapping with ID: {}", savedEntity.getPmaId());

            } catch (RuntimeException e) {
                log.error("Error creating/updating mapping: {}", e.getMessage());
                // Optionally, you can choose to continue with the next mapping or throw an exception
                // If you want to stop processing on any error, you can throw the exception here
            }
        }

        return resultMappings;
    }

    @Transactional
    public void deleteMappingsByPolicyId(Integer policyId) {
        log.info("Deleting mappings for policyId: {}", policyId);

        try {
            PolicyEntity policy = policyRepository.findById(policyId)
                    .orElseThrow(() -> new RuntimeException("Policy not found with ID: " + policyId));

            List<PolicyModuleMappingEntity> mappingsToDelete = repository.findByPolicy(policy);

            log.info("Found {} mappings to delete for policyId: {}", mappingsToDelete.size(), policyId);

            repository.deleteAll(mappingsToDelete);

            log.info("Successfully deleted {} mappings for policyId: {}", mappingsToDelete.size(), policyId);
        } catch (RuntimeException e) {
            log.error("Error deleting mappings for policyId {}: {}", policyId, e.getMessage());
            throw e; // Re-throw the exception to rollback the transaction
        }
    }

    @Transactional
    public void deleteMappingsByPolicyIds(List<Integer> policyIds) {
        log.info("Deleting mappings for policyIds: {}", policyIds);

        try {
            List<PolicyEntity> policies = policyRepository.findAllById(policyIds);

            if (policies.size() != policyIds.size()) {
                log.warn("Not all policies were found. Found {} out of {} requested.", policies.size(), policyIds.size());
            }

            List<PolicyModuleMappingEntity> mappingsToDelete = repository.findByPolicyIn(policies);

            log.info("Found {} mappings to delete for {} policyIds", mappingsToDelete.size(), policyIds.size());

            repository.deleteAll(mappingsToDelete);

            log.info("Successfully deleted {} mappings for {} policyIds", mappingsToDelete.size(), policyIds.size());
        } catch (RuntimeException e) {
            log.error("Error deleting mappings for policyIds {}: {}", policyIds, e.getMessage());
            throw e; // Re-throw the exception to rollback the transaction
        }
    }

    @Transactional(readOnly = true)
    public List<PolicyModuleMapping> getMappingsByPolicyId(Integer policyId) {
        log.info("Fetching policy module mappings for policy ID: {}", policyId);
        return repository.findByPolicyPolicyId(policyId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PolicyModuleMapping> getMappingsByModuleId(Integer moduleId) {
        log.info("Fetching policy module mappings for module ID: {}", moduleId);
        return repository.findByModuleModuleId(moduleId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}