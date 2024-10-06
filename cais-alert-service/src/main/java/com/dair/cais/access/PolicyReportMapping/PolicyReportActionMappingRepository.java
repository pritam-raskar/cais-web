package com.dair.cais.access.PolicyReportMapping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyReportActionMappingRepository extends JpaRepository<PolicyReportActionMappingEntity, Integer> {
    List<PolicyReportActionMappingEntity> findByPolicyPolicyId(Integer policyId);
    List<PolicyReportActionMappingEntity> findByReportReportId(Integer reportId);
}
