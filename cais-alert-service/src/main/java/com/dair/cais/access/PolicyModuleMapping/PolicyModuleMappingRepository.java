package com.dair.cais.access.PolicyModuleMapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyModuleMappingRepository extends JpaRepository<PolicyModuleMappingEntity, Integer> {
    List<PolicyModuleMappingEntity> findByPolicyPolicyId(Integer policyId);
    List<PolicyModuleMappingEntity> findByModuleModuleId(Integer moduleId);
}