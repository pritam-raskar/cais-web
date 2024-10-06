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
    public PolicyAlertTypeActionMapping createMapping(PolicyAlertTypeActionMapping mapping) {
        logger.info("Creating mapping with policyId: {}, alertTypeId: {}, actionId: {}",
                mapping.getPolicyId(), mapping.getAlertTypeId(), mapping.getActionId());

        PolicyEntity policy = policyRepository.findById(mapping.getPolicyId())
                .orElseThrow(() -> new RuntimeException("Policy not found with ID: " + mapping.getPolicyId()));
        logger.info("Found policy: {}", policy.getPolicyId());

        alertTypeEntity alertType = alertTypeRepository.findByAlertTypeId(mapping.getAlertTypeId())
                .orElseThrow(() -> new RuntimeException("Alert type not found with ID: " + mapping.getAlertTypeId()));
        logger.info("Found alert type: {}", alertType.getAlertTypeId());

        ActionEntity action = actionRepository.findById(mapping.getActionId())
                .orElseThrow(() -> new RuntimeException("Action not found with ID: " + mapping.getActionId()));
        logger.info("Found action: {}", action.getActionId());

        PolicyAlertTypeActionMappingEntity entity = new PolicyAlertTypeActionMappingEntity();
        entity.setPolicy(policy);
        entity.setAlertType(alertType);
        entity.setAction(action);
        entity.setCondition(mapping.getCondition());

        PolicyAlertTypeActionMappingEntity savedEntity = mappingRepository.save(entity);
        return mappingMapper.toModel(savedEntity);
    }

    @Transactional
    public PolicyAlertTypeActionMapping updateMapping(Integer pataId, PolicyAlertTypeActionMapping mapping) {
        logger.info("Updating mapping with ID: {}", pataId);
        return mappingRepository.findById(pataId)
                .map(existingEntity -> {
                    PolicyEntity policy = policyRepository.findById(mapping.getPolicyId())
                            .orElseThrow(() -> {
                                logger.error("Policy not found for ID: {}", mapping.getPolicyId());
                                return new RuntimeException("Policy not found");
                            });

                    alertTypeEntity alertType = alertTypeRepository.findByAlertTypeId(mapping.getAlertTypeId())
                            .orElseThrow(() -> {
                                logger.error("Alert type Id not found for ID: {}", mapping.getAlertTypeId());
                                return new RuntimeException("Alert type Id not found");
                            });

                    ActionEntity action = actionRepository.findById(mapping.getActionId())
                            .orElseThrow(() -> {
                                logger.error("Action Id not found for ID: {}", mapping.getAlertTypeId());
                                return new RuntimeException("Action Id not found");
                            });

                    existingEntity.setPolicy(policy);
                    existingEntity.setAlertType(alertType);
                    existingEntity.setAction(action);
                    existingEntity.setCondition(mapping.getCondition());

                    PolicyAlertTypeActionMappingEntity updatedEntity = mappingRepository.save(existingEntity);
                    logger.info("Updated mapping with ID: {}", pataId);
                    return mappingMapper.toModel(updatedEntity);
                })
                .orElseGet(() -> {
                    logger.error("Mapping not found for ID: {}", pataId);
                    return null;
                });
    }

    public void deleteMapping(Integer pataId) {
        logger.info("Deleting mapping with ID: {}", pataId);
        mappingRepository.deleteById(pataId);
        logger.info("Deleted mapping with ID: {}", pataId);
    }
}