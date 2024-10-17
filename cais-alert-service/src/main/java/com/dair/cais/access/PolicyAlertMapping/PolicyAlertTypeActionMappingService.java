package com.dair.cais.access.PolicyAlertMapping;

import com.dair.cais.access.Actions.ActionEntity;
import com.dair.cais.access.Actions.ActionRepository;
import com.dair.cais.access.alertType.alertTypeEntity;
import com.dair.cais.access.alertType.alertTypeRepository1;
import com.dair.cais.access.policy.PolicyEntity;
import com.dair.cais.access.policy.PolicyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
@Service
public class PolicyAlertTypeActionMappingService {
    private static final Logger logger = LoggerFactory.getLogger(PolicyAlertTypeActionMappingService.class);

    @Autowired
    private PolicyAlertTypeActionMappingRepository mappingRepository;

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private alertTypeRepository1 alertTypeRepository;

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private PolicyAlertTypeActionMappingMapper mappingMapper;

    public List<PolicyAlertTypeActionMapping> getMappingsByPolicyId(Integer policyId) {
        List<PolicyAlertTypeActionMappingEntity> mappings = mappingRepository.findByPolicyPolicyId(policyId);
        return mappings.stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    private PolicyAlertTypeActionMapping mapEntityToDto(PolicyAlertTypeActionMappingEntity entity) {
        PolicyAlertTypeActionMapping dto = new PolicyAlertTypeActionMapping();
        dto.setPataId(entity.getPataId());
        dto.setPolicyId(entity.getPolicy().getPolicyId());
        dto.setAlertTypeId(entity.getAlertType().getAlertTypeId());
        dto.setActionId(entity.getAction().getActionId());
        dto.setActionName(entity.getAction().getActionName());
        dto.setActionCategory(entity.getAction().getActionCategory());
        dto.setActionType(entity.getAction().getActionType());
        dto.setActionDescription(entity.getAction().getActionDescription());
        dto.setCondition(entity.getCondition());
        return dto;
    }


    @Transactional
    public List<PolicyAlertTypeActionMapping> createMappings(List<PolicyAlertTypeActionMapping> mappings) {
        List<PolicyAlertTypeActionMapping> resultMappings = new ArrayList<>();

        for (PolicyAlertTypeActionMapping mapping : mappings) {
            logger.info("Creating/Updating mapping with policyId: {}, alertTypeId: {}, actionId: {}",
                    mapping.getPolicyId(), mapping.getAlertTypeId(), mapping.getActionId());

            try {
                PolicyEntity policy = policyRepository.findById(mapping.getPolicyId())
                        .orElseThrow(() -> new RuntimeException("Policy not found with ID: " + mapping.getPolicyId()));
                logger.info("Found policy: {}", policy.getPolicyId());

                alertTypeEntity alertType = alertTypeRepository.findByAlertTypeId(mapping.getAlertTypeId())
                        .orElseThrow(() -> new RuntimeException("Alert type not found with ID: " + mapping.getAlertTypeId()));
                logger.info("Found alert type: {}", alertType.getAlertTypeId());

                ActionEntity action = actionRepository.findById(mapping.getActionId())
                        .orElseThrow(() -> new RuntimeException("Action not found with ID: " + mapping.getActionId()));
                logger.info("Found action: {}", action.getActionId());

                // Check if a mapping already exists
                PolicyAlertTypeActionMappingEntity existingEntity = mappingRepository
                        .findByPolicyAndAlertTypeAndAction(policy, alertType, action)
                        .orElse(null);

                PolicyAlertTypeActionMappingEntity entity;

                if (existingEntity != null) {
                    // Update existing entity
                    logger.info("Updating existing mapping with ID: {}", existingEntity.getPataId());
                    existingEntity.setCondition(mapping.getCondition());
                    entity = existingEntity;
                } else {
                    // Create new entity
                    logger.info("Creating new mapping");
                    entity = new PolicyAlertTypeActionMappingEntity();
                    entity.setPolicy(policy);
                    entity.setAlertType(alertType);
                    entity.setAction(action);
                    entity.setCondition(mapping.getCondition());
                }

                PolicyAlertTypeActionMappingEntity savedEntity = mappingRepository.save(entity);
                resultMappings.add(mappingMapper.toModel(savedEntity));

                logger.info("Saved mapping with ID: {}", savedEntity.getPataId());

            } catch (RuntimeException e) {
                logger.error("Error creating/updating mapping: {}", e.getMessage());
                // Optionally, you can choose to continue with the next mapping or throw an exception
                // If you want to stop processing on any error, you can throw the exception here
            }
        }

        return resultMappings;
    }



    @Transactional
    public List<PolicyAlertTypeActionMapping> upsertMappings(List<PolicyAlertTypeActionMapping> mappingsToUpsert) {
        List<PolicyAlertTypeActionMapping> resultMappings = new ArrayList<>();

        for (PolicyAlertTypeActionMapping mapping : mappingsToUpsert) {
            logger.info("Upserting mapping for policy: {}, alertType: {}, action: {}",
                    mapping.getPolicyId(), mapping.getAlertTypeId(), mapping.getActionId());

            try {
                PolicyEntity policy = policyRepository.findById(mapping.getPolicyId())
                        .orElseThrow(() -> {
                            logger.error("Policy not found for ID: {}", mapping.getPolicyId());
                            return new RuntimeException("Policy not found");
                        });

                alertTypeEntity alertType = alertTypeRepository.findByAlertTypeId(mapping.getAlertTypeId())
                        .orElseThrow(() -> {
                            logger.error("Alert type not found for ID: {}", mapping.getAlertTypeId());
                            return new RuntimeException("Alert type not found");
                        });

                ActionEntity action = actionRepository.findById(mapping.getActionId())
                        .orElseThrow(() -> {
                            logger.error("Action not found for ID: {}", mapping.getActionId());
                            return new RuntimeException("Action not found");
                        });

                PolicyAlertTypeActionMappingEntity entity = mappingRepository
                        .findByPolicyAndAlertTypeAndAction(policy, alertType, action)
                        .orElse(new PolicyAlertTypeActionMappingEntity());

                entity.setPolicy(policy);
                entity.setAlertType(alertType);
                entity.setAction(action);
                entity.setCondition(mapping.getCondition());

                PolicyAlertTypeActionMappingEntity savedEntity = mappingRepository.save(entity);
                logger.info("Upserted mapping with ID: {}", savedEntity.getPataId());

                resultMappings.add(mappingMapper.toModel(savedEntity));
            } catch (RuntimeException e) {
                logger.error("Error upserting mapping: {}", e.getMessage());
                // Optionally, you can choose to continue with the next mapping or throw an exception
                // If you want to stop processing on any error, you can throw the exception here
            }
        }

        return resultMappings;
    }

    @Transactional
    public void deleteMappingsByPolicyId(Integer policyId) {
        logger.info("Deleting mappings for policyId: {}", policyId);

        try {
            PolicyEntity policy = policyRepository.findById(policyId)
                    .orElseThrow(() -> new RuntimeException("Policy not found with ID: " + policyId));

            List<PolicyAlertTypeActionMappingEntity> mappingsToDelete = mappingRepository.findByPolicy(policy);

            logger.info("Found {} mappings to delete for policyId: {}", mappingsToDelete.size(), policyId);

            mappingRepository.deleteAll(mappingsToDelete);

            logger.info("Successfully deleted {} mappings for policyId: {}", mappingsToDelete.size(), policyId);
        } catch (RuntimeException e) {
            logger.error("Error deleting mappings for policyId {}: {}", policyId, e.getMessage());
            throw e; // Re-throw the exception to rollback the transaction
        }
    }

    @Transactional
    public void deleteMappingsByPolicyIds(List<Integer> policyIds) {
        logger.info("Deleting mappings for policyIds: {}", policyIds);

        try {
            List<PolicyEntity> policies = policyRepository.findAllById(policyIds);

            if (policies.size() != policyIds.size()) {
                logger.warn("Not all policies were found. Found {} out of {} requested.", policies.size(), policyIds.size());
            }

            List<PolicyAlertTypeActionMappingEntity> mappingsToDelete = mappingRepository.findByPolicyIn(policies);

            logger.info("Found {} mappings to delete for {} policyIds", mappingsToDelete.size(), policyIds.size());

            mappingRepository.deleteAll(mappingsToDelete);

            logger.info("Successfully deleted {} mappings for {} policyIds", mappingsToDelete.size(), policyIds.size());
        } catch (RuntimeException e) {
            logger.error("Error deleting mappings for policyIds {}: {}", policyIds, e.getMessage());
            throw e; // Re-throw the exception to rollback the transaction
        }
    }


}