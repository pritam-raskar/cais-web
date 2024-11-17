package com.dair.cais.access.PolicyEntityMapping;

import com.dair.cais.access.Actions.ActionEntity;
import com.dair.cais.access.Actions.ActionRepository;
import com.dair.cais.access.alertType.alertTypeRepository1;
import com.dair.cais.access.entity.SystemEntityRepository;
import com.dair.cais.access.modules.ModuleRepository;
import com.dair.cais.access.policy.PolicyEntity;
import com.dair.cais.access.policy.PolicyRepository;
import com.dair.cais.reports.repository.ReportsRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;

import java.util.*;
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

    private final alertTypeRepository1 alertTypeRepository;
    private final ModuleRepository moduleRepository;
    private final ReportsRepository reportRepository;

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

//    @Transactional(readOnly = true)
//    public List<PolicyEntityMapping> getMappingsByPolicyId(Integer policyId) {
//        log.debug("Fetching mappings for policy ID: {}", policyId);
//        return mappingRepository.findByPolicyPolicyId(policyId).stream()
//                .map(mapper::toDto)
//                .collect(Collectors.toList());
//    }
    @Transactional(readOnly = true)
    public List<PolicyEntityMapping> getMappingsByPolicyId(Integer policyId) {
        log.debug("Fetching mappings for policy ID: {}", policyId);
        List<PolicyEntityMappingEntity> mappings = mappingRepository.findByPolicyPolicyId(policyId);

        return mappings.stream()
                .map(entity -> {
                    PolicyEntityMapping dto = mapper.toDto(entity);
                    enrichWithEntityName(dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    private void enrichWithEntityName(PolicyEntityMapping dto) {
        try {
            switch (dto.getEntityType()) {
                case "alert-types":
                    alertTypeRepository.findByAlertTypeId(dto.getEntityId())
                            .ifPresent(alertType -> dto.setEntityName(alertType.getTypeName()));
                    break;

                case "modules":
                    moduleRepository.findById(Integer.parseInt(dto.getEntityId()))
                            .ifPresent(module -> dto.setEntityName(module.getModuleName()));
                    break;

                case "reports":
                    reportRepository.findById(Integer.parseInt(dto.getEntityId()))
                            .ifPresent(report -> dto.setEntityName(report.getReportName()));
                    break;

                default:
                    log.warn("Unknown entity type: {}", dto.getEntityType());
                    dto.setEntityName("Unknown Entity Type");
            }
        } catch (Exception e) {
            log.error("Error enriching entity name for mapping {}: {}", dto.getMappingId(), e.getMessage());
            dto.setEntityName("Error fetching entity name");
        }
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


    @Transactional
    public List<PolicyEntityMapping> updatePolicyMappings(Integer policyId, List<PolicyEntityMapping> newMappings) {
        log.info("Updating mappings for policy ID: {}", policyId);

        // Validate policy exists
        PolicyEntity policy = policyRepository.findById(policyId)
                .orElseThrow(() -> {
                    log.error("Policy not found with ID: {}", policyId);
                    return new EntityNotFoundException("Policy not found with ID: " + policyId);
                });

        // Validate all newMappings are for the same policy
        validateMappingsForPolicy(policyId, newMappings);

        try {
            // Get existing mappings for the policy
            List<PolicyEntityMappingEntity> existingMappings = mappingRepository.findByPolicyPolicyId(policyId);
            log.debug("Found {} existing mappings", existingMappings.size());

            // Create maps for comparison and easy lookup
            Map<MappingKey, PolicyEntityMappingEntity> existingMappingsMap = existingMappings.stream()
                    .collect(Collectors.toMap(this::createMappingKey, mapping -> mapping));

            Map<MappingKey, PolicyEntityMapping> newMappingsMap = newMappings.stream()
                    .collect(Collectors.toMap(this::createMappingKey, mapping -> mapping));

            // Find mappings to delete (exist in DB but not in new request)
            Set<MappingKey> keysToDelete = new HashSet<>(existingMappingsMap.keySet());
            keysToDelete.removeAll(newMappingsMap.keySet());

            // Find mappings to add or update
            List<PolicyEntityMappingEntity> mappingsToSave = new ArrayList<>();
            for (Map.Entry<MappingKey, PolicyEntityMapping> entry : newMappingsMap.entrySet()) {
                MappingKey key = entry.getKey();
                PolicyEntityMapping newMapping = entry.getValue();
                PolicyEntityMappingEntity existingMapping = existingMappingsMap.get(key);

                if (existingMapping != null) {
                    // Update existing mapping
                    log.debug("Updating existing mapping for key: {}", key);
                    existingMapping.setCondition(newMapping.getCondition()); // Update non-mandatory fields
                    mappingsToSave.add(existingMapping);
                } else {
                    // Create new mapping
                    log.debug("Creating new mapping for key: {}", key);
                    try {
                        ActionEntity action = actionRepository.findById(newMapping.getActionId())
                                .orElseThrow(() -> new EntityNotFoundException("Action not found with ID: " + newMapping.getActionId()));

                        PolicyEntityMappingEntity newEntity = mapper.toEntity(newMapping);
                        newEntity.setPolicy(policy);
                        newEntity.setAction(action);
                        mappingsToSave.add(newEntity);
                    } catch (Exception e) {
                        log.error("Error processing new mapping: {}", e.getMessage());
                        throw new RuntimeException("Error processing new mapping", e);
                    }
                }
            }

            // Delete old mappings
            if (!keysToDelete.isEmpty()) {
                log.debug("Deleting {} old mappings", keysToDelete.size());
                deleteMappingsByKeys(policyId, keysToDelete);
            }

            // Save new and updated mappings
            if (!mappingsToSave.isEmpty()) {
                log.debug("Saving {} new/updated mappings", mappingsToSave.size());
                mappingRepository.saveAll(mappingsToSave);
            }

            // Fetch and return updated mappings
            List<PolicyEntityMapping> updatedMappings = getMappingsByPolicyId(policyId);
            log.info("Successfully updated mappings for policy ID: {}. Total mappings: {}",
                    policyId, updatedMappings.size());

            return updatedMappings;

        } catch (Exception e) {
            log.error("Error updating policy mappings for policy ID {}: {}", policyId, e.getMessage());
            throw new RuntimeException("Error updating policy mappings", e);
        }
    }

    @Data
    @AllArgsConstructor
    @ToString
    private static class MappingKey {
        private String entityType;
        private String entityId;
        private Integer actionId;

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            MappingKey that = (MappingKey) o;
            return Objects.equals(entityType, that.entityType) &&
                    Objects.equals(entityId, that.entityId) &&
                    Objects.equals(actionId, that.actionId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entityType, entityId, actionId);
        }
    }

    private void validateMappingsForPolicy(Integer policyId, List<PolicyEntityMapping> mappings) {
        log.debug("Validating {} mappings for policy ID: {}", mappings.size(), policyId);

        List<PolicyEntityMapping> invalidMappings = mappings.stream()
                .filter(m -> !policyId.equals(m.getPolicyId()))
                .collect(Collectors.toList());

        if (!invalidMappings.isEmpty()) {
            log.error("Invalid request: Found {} mappings that don't match policy ID: {}",
                    invalidMappings.size(), policyId);
            throw new IllegalArgumentException(String.format(
                    "All mappings must be for policy ID: %d. Found invalid policy IDs: %s",
                    policyId,
                    invalidMappings.stream()
                            .map(PolicyEntityMapping::getPolicyId)
                            .distinct()
                            .map(String::valueOf)
                            .collect(Collectors.joining(", "))
            ));
        }

        // Validate entity types exist
        Set<String> invalidEntityTypes = mappings.stream()
                .map(PolicyEntityMapping::getEntityType)
                .filter(entityType -> !isValidEntityType(entityType))
                .collect(Collectors.toSet());

        if (!invalidEntityTypes.isEmpty()) {
            log.error("Invalid entity types found: {}", invalidEntityTypes);
            throw new IllegalArgumentException("Invalid entity types: " + String.join(", ", invalidEntityTypes));
        }

        // Validate actions exist
        Set<Integer> invalidActionIds = mappings.stream()
                .map(PolicyEntityMapping::getActionId)
                .filter(actionId -> !actionRepository.existsById(actionId))
                .collect(Collectors.toSet());

        if (!invalidActionIds.isEmpty()) {
            log.error("Invalid action IDs found: {}", invalidActionIds);
            throw new IllegalArgumentException("Invalid action IDs: " +
                    invalidActionIds.stream()
                            .map(String::valueOf)
                            .collect(Collectors.joining(", ")));
        }
    }

    private boolean isValidEntityType(String entityType) {
        return Arrays.asList("alert-types", "modules", "reports").contains(entityType);
    }

    @Transactional
    private void deleteMappingsByKeys(Integer policyId, Set<MappingKey> keysToDelete) {
        log.debug("Deleting mappings for policy ID: {} with {} keys", policyId, keysToDelete.size());

        try {
            for (MappingKey key : keysToDelete) {
                log.debug("Deleting mapping with key: {}", key);
                int deletedCount = mappingRepository.deleteByPolicyIdAndEntityTypeAndEntityIdAndActionId(
                        policyId,
                        key.getEntityType(),
                        key.getEntityId(),
                        key.getActionId()
                );
                log.debug("Deleted {} mapping(s) for key: {}", deletedCount, key);
            }
        } catch (Exception e) {
            log.error("Error deleting mappings for policy ID {}: {}", policyId, e.getMessage());
            throw new RuntimeException("Error deleting policy mappings", e);
        }
    }

    private MappingKey createMappingKey(PolicyEntityMappingEntity entity) {
        return new MappingKey(
                entity.getEntityType(),
                entity.getEntityId(),
                entity.getAction().getActionId()
        );
    }

    private MappingKey createMappingKey(PolicyEntityMapping mapping) {
        return new MappingKey(
                mapping.getEntityType(),
                mapping.getEntityId(),
                mapping.getActionId()
        );
    }
}