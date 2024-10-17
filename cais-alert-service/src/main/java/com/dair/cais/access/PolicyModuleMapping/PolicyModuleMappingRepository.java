package com.dair.cais.access.PolicyModuleMapping;

import com.dair.cais.access.Actions.ActionEntity;
import com.dair.cais.access.modules.ModuleEntity;
import com.dair.cais.access.policy.PolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyModuleMappingRepository extends JpaRepository<PolicyModuleMappingEntity, Integer> {
    List<PolicyModuleMappingEntity> findByPolicyPolicyId(Integer policyId);
    List<PolicyModuleMappingEntity> findByModuleModuleId(Integer moduleId);

    Optional<PolicyModuleMappingEntity> findByPolicyAndModuleAndAction(PolicyEntity policy, ModuleEntity module, ActionEntity action);
    List<PolicyModuleMappingEntity> findByPolicy(PolicyEntity policy);
    List<PolicyModuleMappingEntity> findByPolicyIn(List<PolicyEntity> policies);
}