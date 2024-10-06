package com.dair.cais.access.policy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PolicyService {

    @Autowired
    private PolicyRepository policyRepository;

    @Autowired
    private PolicyMapper policyMapper;

    public List<Policy> getAllPolicies() {
        return policyRepository.findAll().stream()
                .map(policyMapper::toModel)
                .collect(Collectors.toList());
    }

    public Policy getPolicyById(Integer id) {
        return policyRepository.findById(id)
                .map(policyMapper::toModel)
                .orElse(null);
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
}