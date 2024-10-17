package com.dair.cais.access.PolicyReportMapping;

import com.dair.cais.access.Actions.ActionEntity;
import com.dair.cais.access.policy.PolicyEntity;
import com.dair.cais.access.reports.ReportEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyReportActionMappingRepository extends JpaRepository<PolicyReportActionMappingEntity, Integer> {
    List<PolicyReportActionMappingEntity> findByPolicyPolicyId(Integer policyId);
    List<PolicyReportActionMappingEntity> findByReportReportId(Integer reportId);

    Optional<PolicyReportActionMappingEntity> findByPolicyAndReportAndAction(PolicyEntity policy, ReportEntity report, ActionEntity action);
    List<PolicyReportActionMappingEntity> findByPolicy(PolicyEntity policy);
    List<PolicyReportActionMappingEntity> findByPolicyIn(List<PolicyEntity> policies);
}
