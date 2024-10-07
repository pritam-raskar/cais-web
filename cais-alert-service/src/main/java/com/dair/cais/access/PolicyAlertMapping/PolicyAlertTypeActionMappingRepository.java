package com.dair.cais.access.PolicyAlertMapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PolicyAlertTypeActionMappingRepository extends JpaRepository<PolicyAlertTypeActionMappingEntity, Integer> {
    List<PolicyAlertTypeActionMappingEntity> findByPolicyPolicyId(Integer policyId);
    List<PolicyAlertTypeActionMappingEntity> findByPolicyPolicyIdIn(List<Integer> policyIds);
}