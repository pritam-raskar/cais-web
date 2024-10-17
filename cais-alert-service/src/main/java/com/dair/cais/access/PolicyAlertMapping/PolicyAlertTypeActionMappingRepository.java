package com.dair.cais.access.PolicyAlertMapping;

import com.dair.cais.access.Actions.ActionEntity;
import com.dair.cais.access.alertType.alertTypeEntity;
import com.dair.cais.access.policy.PolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyAlertTypeActionMappingRepository extends JpaRepository<PolicyAlertTypeActionMappingEntity, Integer> {
    List<PolicyAlertTypeActionMappingEntity> findByPolicyPolicyId(Integer policyId);

    Optional<PolicyAlertTypeActionMappingEntity> findByPolicyAndAlertTypeAndAction(PolicyEntity policy, alertTypeEntity alertType, ActionEntity action);

    List<PolicyAlertTypeActionMappingEntity> findByPolicy(PolicyEntity policy);
    List<PolicyAlertTypeActionMappingEntity> findByPolicyIn(List<PolicyEntity> policies);
}