package com.dair.cais.access.PolicyEntityMapping;

import com.dair.cais.access.Actions.ActionEntity;
import com.dair.cais.access.Actions.ActionRepository;
import com.dair.cais.access.entity.SystemEntityRepository;
import com.dair.cais.access.policy.PolicyEntity;
import com.dair.cais.access.policy.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyEntityMappingService {

    private final PolicyEntityMappingRepository mappingRepository;
    private final PolicyRepository policyRepository;
    private final ActionRepository actionRepository;
    private final SystemEntityRepository systemEntityRepository;
    private final PolicyEntityMappingMapper mapper;

    @Transactional(readOnly = true)
    public List<PolicyEntityMapping> getAllMappings() {
        log.debug("Fetching all policy entity mappings");
        return mappingRepository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PolicyEntityMapping getMappingById(Integer id) {
        log.debug("Fetching mapping with ID: {}", id);
        return mappingRepository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Mapping not found with ID: " + id));
    }

    @Transactional(readOnly = true)
    public List<PolicyEntityMapping> getMappingsByPolicyId(Integer policyId) {
        log.debug("Fetching mappings for policy ID: {}", policyId);
        return mappingRepository.findByPolicyPolicyId(policyId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PolicyEntityMapping> getMappingsByEntityType(String entityType) {
        log.debug("Fetching mappings for entity type: {}", entityType);
        return mappingRepository.findByEntityType(entityType).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PolicyEntityMapping> createMappings(List<PolicyEntityMapping> mappings) {
        log.debug("Creating {} new mappings", mappings.size());
        List<PolicyEntityMapping> result = new ArrayList<>();

        for (PolicyEntityMapping mapping : mappings) {
            try {
                validateEntityType(mapping.getEntityType());

                PolicyEntity policy = getPolicyEntity(mapping.getPolicyId());
                ActionEntity action = getActionEntity(mapping.getActionId());

                PolicyEntityMappingEntity entity = mapper.toEntity(mapping);
                entity.setPolicy(policy);
                entity.setAction(action);

                PolicyEntityMappingEntity savedEntity = mappingRepository.save(entity);
                result.add(mapper.toDto(savedEntity));

                log.info("Created mapping with ID: {}", savedEntity.getMappingId());
            } catch (Exception e) {
                log.error("Error creating mapping: {}", e.getMessage());
                throw new RuntimeException("Error creating mapping: " + e.getMessage());
            }
        }

        return result;
    }

    @Transactional
    public PolicyEntityMapping updateMapping(Integer id, PolicyEntityMapping mapping) {
        log.debug("Updating mapping with ID: {}", id);

        return mappingRepository.findById(id)
                .map(existingEntity -> {
                    validateEntityType(mapping.getEntityType());

                    ActionEntity action = getActionEntity(mapping.getActionId());

                    existingEntity.setEntityType(mapping.getEntityType());
                    existingEntity.setEntityId(mapping.getEntityId());
                    existingEntity.setAction(action);
                    existingEntity.setCondition(mapping.getCondition());

                    PolicyEntityMappingEntity updatedEntity = mappingRepository.save(existingEntity);
                    log.info("Updated mapping with ID: {}", updatedEntity.getMappingId());
                    return mapper.toDto(updatedEntity);
                })
                .orElseThrow(() -> new EntityNotFoundException("Mapping not found with ID: " + id));
    }

    @Transactional
    public void deleteMapping(Integer id) {
        log.debug("Deleting mapping with ID: {}", id);
        mappingRepository.deleteById(id);
        log.info("Deleted mapping with ID: {}", id);
    }

    @Transactional
    public void deleteMappingsByPolicyId(Integer policyId) {
        log.debug("Deleting all mappings for policy ID: {}", policyId);
        mappingRepository.deleteByPolicyPolicyId(policyId);
        log.info("Deleted all mappings for policy ID: {}", policyId);
    }

    private void validateEntityType(String entityType) {
        if (!systemEntityRepository.existsByEntityType(entityType)) {
            log.error("Invalid entity type: {}", entityType);
            throw new IllegalArgumentException("Invalid entity type: " + entityType);
        }
    }

    private PolicyEntity getPolicyEntity(Integer policyId) {
        return policyRepository.findById(policyId)
                .orElseThrow(() -> new EntityNotFoundException("Policy not found with ID: " + policyId));
    }

    private ActionEntity getActionEntity(Integer actionId) {
        return actionRepository.findById(actionId)
                .orElseThrow(() -> new EntityNotFoundException("Action not found with ID: " + actionId));
    }

    @Transactional
    public void deleteMappingsByPolicyIdAndEntityType(Integer policyId, String entityType) {
        log.debug("Deleting mappings for policy ID: {} and entity type: {}", policyId, entityType);
        mappingRepository.deleteByPolicyPolicyIdAndEntityType(policyId, entityType);
        log.info("Deleted mappings for policy ID: {} and entity type: {}", policyId, entityType);
    }
}