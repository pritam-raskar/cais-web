package com.dair.cais.access.PolicyAlertMapping;

import com.dair.cais.access.Actions.ActionEntity;
import com.dair.cais.access.Actions.ActionRepository;
import com.dair.cais.access.alertType.alertTypeEntity;
import com.dair.cais.access.alertType.alertTypeRepository1;
import com.dair.cais.access.policy.PolicyEntity;
import com.dair.cais.access.policy.PolicyRepository;
import com.dair.exception.CaisNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyAlertTypeActionMappingService {

    private final PolicyAlertTypeActionMappingRepository mappingRepository;
    private final PolicyRepository policyRepository;
    private final alertTypeRepository1 alertTypeRepository;
    private final ActionRepository actionRepository;
    private final PolicyAlertTypeActionMappingMapper mappingMapper;

    @Transactional(readOnly = true)
    public List<PolicyAlertTypeActionMapping> getMappingsByPolicyId(Integer policyId) {
        log.debug("Fetching mappings for policy ID: {}", policyId);
        List<PolicyAlertTypeActionMappingEntity> mappings = mappingRepository.findByPolicyPolicyId(policyId);
        if (mappings.isEmpty()) {
            throw new CaisNotFoundException("No mappings found for policy ID: " + policyId);
        }
        return mappings.stream()
                .map(mappingMapper::toModel)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PolicyAlertTypeActionMapping> createMappings(List<PolicyAlertTypeActionMapping> mappings) {
        log.debug("Creating {} new mappings", mappings.size());
        List<PolicyAlertTypeActionMappingEntity> entities = new ArrayList<>();

        Map<Integer, PolicyEntity> policyCache = policyRepository.findAllById(mappings.stream()
                        .map(PolicyAlertTypeActionMapping::getPolicyId)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(PolicyEntity::getPolicyId, Function.identity()));

        Map<String, alertTypeEntity> alertTypeCache = mappings.stream()
                .map(PolicyAlertTypeActionMapping::getAlertTypeId)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        alertTypeId -> alertTypeRepository.findByAlertTypeId(alertTypeId)
                                .orElseThrow(() -> new CaisNotFoundException("AlertType not found with ID: " + alertTypeId))
                ));

        Map<Integer, ActionEntity> actionCache = actionRepository.findAllById(mappings.stream()
                        .map(PolicyAlertTypeActionMapping::getActionId)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(ActionEntity::getActionId, Function.identity()));

        for (PolicyAlertTypeActionMapping mapping : mappings) {
            PolicyEntity policy = policyCache.get(mapping.getPolicyId());
            alertTypeEntity alertType = alertTypeCache.get(mapping.getAlertTypeId());
            ActionEntity action = actionCache.get(mapping.getActionId());

            if (policy == null || alertType == null || action == null) {
                throw new CaisNotFoundException("One or more related entities not found");
            }

            PolicyAlertTypeActionMappingEntity entity = new PolicyAlertTypeActionMappingEntity();
            entity.setPolicy(policy);
            entity.setAlertType(alertType);
            entity.setAction(action);
            entity.setCondition(mapping.getCondition());
            entities.add(entity);
        }

        List<PolicyAlertTypeActionMappingEntity> savedEntities = mappingRepository.saveAll(entities);
        log.info("Created {} new mappings", savedEntities.size());
        return savedEntities.stream()
                .map(mappingMapper::toModel)
                .collect(Collectors.toList());
    }

    @Transactional
    public List<PolicyAlertTypeActionMapping> updateMappings(List<PolicyAlertTypeActionMapping> mappings) {
        log.debug("Updating {} mappings", mappings.size());

        // Fetch existing entities
        List<PolicyAlertTypeActionMappingEntity> existingEntities = mappingRepository.findAllById(
                mappings.stream().map(PolicyAlertTypeActionMapping::getPataId).collect(Collectors.toList())
        );

        if (existingEntities.size() != mappings.size()) {
            throw new CaisNotFoundException("One or more mappings not found");
        }

        Map<Integer, PolicyEntity> policyCache = policyRepository.findAllById(mappings.stream()
                        .map(PolicyAlertTypeActionMapping::getPolicyId)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(PolicyEntity::getPolicyId, Function.identity()));

        Map<String, alertTypeEntity> alertTypeCache = mappings.stream()
                .map(PolicyAlertTypeActionMapping::getAlertTypeId)
                .distinct()
                .collect(Collectors.toMap(
                        Function.identity(),
                        alertTypeId -> alertTypeRepository.findByAlertTypeId(alertTypeId)
                                .orElseThrow(() -> new CaisNotFoundException("AlertType not found with ID: " + alertTypeId))
                ));

        Map<Integer, ActionEntity> actionCache = actionRepository.findAllById(mappings.stream()
                        .map(PolicyAlertTypeActionMapping::getActionId)
                        .collect(Collectors.toSet()))
                .stream()
                .collect(Collectors.toMap(ActionEntity::getActionId, Function.identity()));

        List<PolicyAlertTypeActionMappingEntity> updatedEntities = new ArrayList<>();

        for (PolicyAlertTypeActionMapping mapping : mappings) {
            PolicyAlertTypeActionMappingEntity entity = existingEntities.stream()
                    .filter(e -> e.getPataId().equals(mapping.getPataId()))
                    .findFirst()
                    .orElseThrow(() -> new CaisNotFoundException("Mapping not found with ID: " + mapping.getPataId()));

            PolicyEntity policy = policyCache.get(mapping.getPolicyId());
            alertTypeEntity alertType = alertTypeCache.get(mapping.getAlertTypeId());
            ActionEntity action = actionCache.get(mapping.getActionId());

            if (policy == null || alertType == null || action == null) {
                throw new CaisNotFoundException("One or more related entities not found");
            }

            entity.setPolicy(policy);
            entity.setAlertType(alertType);
            entity.setAction(action);
            entity.setCondition(mapping.getCondition());
            updatedEntities.add(entity);
        }

        List<PolicyAlertTypeActionMappingEntity> savedEntities = mappingRepository.saveAll(updatedEntities);
        log.info("Updated {} mappings", savedEntities.size());
        return savedEntities.stream()
                .map(mappingMapper::toModel)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteMappings(List<Integer> policyIds) {
        log.debug("Deleting mappings for {} policies", policyIds.size());

        List<PolicyAlertTypeActionMappingEntity> mappingsToDelete = mappingRepository.findByPolicyPolicyIdIn(policyIds);

        if (mappingsToDelete.isEmpty()) {
            log.warn("No mappings found for the given policy IDs: {}", policyIds);
            throw new CaisNotFoundException("No mappings found for the given policy IDs");
        }

        // Group mappings by policy ID for logging purposes
        Map<Integer, Long> deletionCounts = mappingsToDelete.stream()
                .collect(Collectors.groupingBy(
                        mapping -> mapping.getPolicy().getPolicyId(),
                        Collectors.counting()
                ));

        mappingRepository.deleteAll(mappingsToDelete);

        // Log deletion details
        deletionCounts.forEach((policyId, count) ->
                log.info("Deleted {} mappings for policy ID: {}", count, policyId)
        );

        log.info("Deleted a total of {} mappings for {} policies", mappingsToDelete.size(), deletionCounts.size());
    }

    @Transactional
    public void deleteMappingsByPolicyId(Integer policyId) {
        log.debug("Deleting mappings for policy ID: {}", policyId);

        List<PolicyAlertTypeActionMappingEntity> mappingsToDelete = mappingRepository.findByPolicyPolicyId(policyId);

        if (mappingsToDelete.isEmpty()) {
            log.warn("No mappings found for policy ID: {}", policyId);
            throw new CaisNotFoundException("No mappings found for policy ID: " + policyId);
        }

        mappingRepository.deleteAll(mappingsToDelete);

        log.info("Deleted {} mappings for policy ID: {}", mappingsToDelete.size(), policyId);
    }
}


//package com.dair.cais.access.PolicyAlertMapping;
//
//import com.dair.cais.access.Actions.ActionEntity;
//import com.dair.cais.access.Actions.ActionRepository;
//import com.dair.cais.access.alertType.alertTypeEntity;
//import com.dair.cais.access.alertType.alertTypeRepository1;
//import com.dair.cais.access.policy.PolicyEntity;
//import com.dair.cais.access.policy.PolicyRepository;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import java.util.stream.Collectors;
//@Service
//public class PolicyAlertTypeActionMappingService {
//    private static final Logger logger = LoggerFactory.getLogger(PolicyAlertTypeActionMappingService.class);
//
//    @Autowired
//    private PolicyAlertTypeActionMappingRepository mappingRepository;
//
//    @Autowired
//    private PolicyRepository policyRepository;
//
//    @Autowired
//    private alertTypeRepository1 alertTypeRepository;
//
//    @Autowired
//    private ActionRepository actionRepository;
//
//    @Autowired
//    private PolicyAlertTypeActionMappingMapper mappingMapper;
//
//    public List<PolicyAlertTypeActionMapping> getMappingsByPolicyId(Integer policyId) {
//        List<PolicyAlertTypeActionMappingEntity> mappings = mappingRepository.findByPolicyPolicyId(policyId);
//        return mappings.stream()
//                .map(this::mapEntityToDto)
//                .collect(Collectors.toList());
//    }
//
//    private PolicyAlertTypeActionMapping mapEntityToDto(PolicyAlertTypeActionMappingEntity entity) {
//        PolicyAlertTypeActionMapping dto = new PolicyAlertTypeActionMapping();
//        dto.setPataId(entity.getPataId());
//        dto.setPolicyId(entity.getPolicy().getPolicyId());
//        dto.setAlertTypeId(entity.getAlertType().getAlertTypeId());
//        dto.setActionId(entity.getAction().getActionId());
//        dto.setActionName(entity.getAction().getActionName());
//        dto.setActionCategory(entity.getAction().getActionCategory());
//        dto.setActionType(entity.getAction().getActionType());
//        dto.setActionDescription(entity.getAction().getActionDescription());
//        dto.setCondition(entity.getCondition());
//        return dto;
//    }
//
//    @Transactional
//    public PolicyAlertTypeActionMapping createMapping(PolicyAlertTypeActionMapping mapping) {
//        logger.info("Creating mapping with policyId: {}, alertTypeId: {}, actionId: {}",
//                mapping.getPolicyId(), mapping.getAlertTypeId(), mapping.getActionId());
//
//        PolicyEntity policy = policyRepository.findById(mapping.getPolicyId())
//                .orElseThrow(() -> new RuntimeException("Policy not found with ID: " + mapping.getPolicyId()));
//        logger.info("Found policy: {}", policy.getPolicyId());
//
//        alertTypeEntity alertType = alertTypeRepository.findByAlertTypeId(mapping.getAlertTypeId())
//                .orElseThrow(() -> new RuntimeException("Alert type not found with ID: " + mapping.getAlertTypeId()));
//        logger.info("Found alert type: {}", alertType.getAlertTypeId());
//
//        ActionEntity action = actionRepository.findById(mapping.getActionId())
//                .orElseThrow(() -> new RuntimeException("Action not found with ID: " + mapping.getActionId()));
//        logger.info("Found action: {}", action.getActionId());
//
//        PolicyAlertTypeActionMappingEntity entity = new PolicyAlertTypeActionMappingEntity();
//        entity.setPolicy(policy);
//        entity.setAlertType(alertType);
//        entity.setAction(action);
//        entity.setCondition(mapping.getCondition());
//
//        PolicyAlertTypeActionMappingEntity savedEntity = mappingRepository.save(entity);
//        return mappingMapper.toModel(savedEntity);
//    }
//
//    @Transactional
//    public PolicyAlertTypeActionMapping updateMapping(Integer pataId, PolicyAlertTypeActionMapping mapping) {
//        logger.info("Updating mapping with ID: {}", pataId);
//        return mappingRepository.findById(pataId)
//                .map(existingEntity -> {
//                    PolicyEntity policy = policyRepository.findById(mapping.getPolicyId())
//                            .orElseThrow(() -> {
//                                logger.error("Policy not found for ID: {}", mapping.getPolicyId());
//                                return new RuntimeException("Policy not found");
//                            });
//
//                    alertTypeEntity alertType = alertTypeRepository.findByAlertTypeId(mapping.getAlertTypeId())
//                            .orElseThrow(() -> {
//                                logger.error("Alert type Id not found for ID: {}", mapping.getAlertTypeId());
//                                return new RuntimeException("Alert type Id not found");
//                            });
//
//                    ActionEntity action = actionRepository.findById(mapping.getActionId())
//                            .orElseThrow(() -> {
//                                logger.error("Action Id not found for ID: {}", mapping.getAlertTypeId());
//                                return new RuntimeException("Action Id not found");
//                            });
//
//                    existingEntity.setPolicy(policy);
//                    existingEntity.setAlertType(alertType);
//                    existingEntity.setAction(action);
//                    existingEntity.setCondition(mapping.getCondition());
//
//                    PolicyAlertTypeActionMappingEntity updatedEntity = mappingRepository.save(existingEntity);
//                    logger.info("Updated mapping with ID: {}", pataId);
//                    return mappingMapper.toModel(updatedEntity);
//                })
//                .orElseGet(() -> {
//                    logger.error("Mapping not found for ID: {}", pataId);
//                    return null;
//                });
//    }
//
//    public void deleteMapping(Integer pataId) {
//        logger.info("Deleting mapping with ID: {}", pataId);
//        mappingRepository.deleteById(pataId);
//        logger.info("Deleted mapping with ID: {}", pataId);
//    }
//}