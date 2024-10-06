package com.dair.cais.access.PolicyModuleMapping;


import com.dair.cais.access.Actions.ActionRepository;
import com.dair.cais.access.modules.ModuleRepository;
import com.dair.cais.access.policy.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public PolicyModuleMapping createMapping(PolicyModuleMapping mapping) {
        log.info("Creating new policy module mapping");
        PolicyModuleMappingEntity entity = mapper.toEntity(mapping);
        entity.setPolicy(policyRepository.findById(mapping.getPolicyId())
                .orElseThrow(() -> new RuntimeException("Policy not found")));
        entity.setModule(moduleRepository.findById(mapping.getModuleId())
                .orElseThrow(() -> new RuntimeException("Module not found")));
        entity.setAction(actionRepository.findById(mapping.getActionId())
                .orElseThrow(() -> new RuntimeException("Action not found")));
        PolicyModuleMappingEntity savedEntity = repository.save(entity);
        log.debug("Created policy module mapping with ID: {}", savedEntity.getPmaId());
        return mapper.toDto(savedEntity);
    }

    @Transactional
    public Optional<PolicyModuleMapping> updateMapping(Integer pmaId, PolicyModuleMapping updatedMapping) {
        log.info("Updating policy module mapping with ID: {}", pmaId);
        return repository.findById(pmaId)
                .map(existingEntity -> {
                    existingEntity.setPolicy(policyRepository.findById(updatedMapping.getPolicyId())
                            .orElseThrow(() -> new RuntimeException("Policy not found")));
                    existingEntity.setModule(moduleRepository.findById(updatedMapping.getModuleId())
                            .orElseThrow(() -> new RuntimeException("Module not found")));
                    existingEntity.setAction(actionRepository.findById(updatedMapping.getActionId())
                            .orElseThrow(() -> new RuntimeException("Action not found")));
                    existingEntity.setCondition(updatedMapping.getCondition());
                    PolicyModuleMappingEntity savedEntity = repository.save(existingEntity);
                    log.debug("Updated policy module mapping with ID: {}", savedEntity.getPmaId());
                    return mapper.toDto(savedEntity);
                });
    }

    @Transactional
    public void deleteMapping(Integer pmaId) {
        log.info("Deleting policy module mapping with ID: {}", pmaId);
        repository.deleteById(pmaId);
        log.debug("Deleted policy module mapping with ID: {}", pmaId);
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