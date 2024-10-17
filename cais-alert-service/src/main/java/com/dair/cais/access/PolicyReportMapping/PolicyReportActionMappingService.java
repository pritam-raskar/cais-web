package com.dair.cais.access.PolicyReportMapping;

import com.dair.cais.access.Actions.ActionEntity;
import com.dair.cais.access.Actions.ActionRepository;
import com.dair.cais.access.policy.PolicyEntity;
import com.dair.cais.access.policy.PolicyRepository;
import com.dair.cais.access.reports.ReportEntity;
import com.dair.cais.access.reports.ReportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PolicyReportActionMappingService {

    private final PolicyReportActionMappingRepository repository;
    private final PolicyRepository policyRepository;
    private final ReportRepository reportRepository;
    private final ActionRepository actionRepository;
    private final PolicyReportActionMappingMapper mapper;

    @Transactional(readOnly = true)
    public List<PolicyReportActionMapping> getAllMappings() {
        log.info("Fetching all policy report action mappings");
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PolicyReportActionMapping> getMappingById(Integer praId) {
        log.info("Fetching policy report action mapping with ID: {}", praId);
        return repository.findById(praId).map(mapper::toDto);
    }


    @Transactional
    public List<PolicyReportActionMapping> createOrUpdateMappings(List<PolicyReportActionMapping> mappings) {
        List<PolicyReportActionMapping> resultMappings = new ArrayList<>();

        for (PolicyReportActionMapping mapping : mappings) {
            log.info("Creating/Updating mapping with policyId: {}, reportId: {}, actionId: {}",
                    mapping.getPolicyId(), mapping.getReportId(), mapping.getActionId());

            try {
                PolicyEntity policy = policyRepository.findById(mapping.getPolicyId())
                        .orElseThrow(() -> new RuntimeException("Policy not found with ID: " + mapping.getPolicyId()));
                log.info("Found policy: {}", policy.getPolicyId());

                ReportEntity report = reportRepository.findById(mapping.getReportId())
                        .orElseThrow(() -> new RuntimeException("Report not found with ID: " + mapping.getReportId()));
                log.info("Found report: {}", report.getReportId());

                ActionEntity action = actionRepository.findById(mapping.getActionId())
                        .orElseThrow(() -> new RuntimeException("Action not found with ID: " + mapping.getActionId()));
                log.info("Found action: {}", action.getActionId());

                // Check if a mapping already exists
                PolicyReportActionMappingEntity existingEntity = repository
                        .findByPolicyAndReportAndAction(policy, report, action)
                        .orElse(null);

                PolicyReportActionMappingEntity entity;

                if (existingEntity != null) {
                    // Update existing entity
                    log.info("Updating existing mapping with ID: {}", existingEntity.getPraId());
                    existingEntity.setCondition(mapping.getCondition());
                    entity = existingEntity;
                } else {
                    // Create new entity
                    log.info("Creating new mapping");
                    entity = new PolicyReportActionMappingEntity();
                    entity.setPolicy(policy);
                    entity.setReport(report);
                    entity.setAction(action);
                    entity.setCondition(mapping.getCondition());
                }

                PolicyReportActionMappingEntity savedEntity = repository.save(entity);
                resultMappings.add(mapper.toDto(savedEntity));

                log.info("Saved mapping with ID: {}", savedEntity.getPraId());

            } catch (RuntimeException e) {
                log.error("Error creating/updating mapping: {}", e.getMessage());
                // Optionally, you can choose to continue with the next mapping or throw an exception
                // If you want to stop processing on any error, you can throw the exception here
            }
        }

        return resultMappings;
    }

    @Transactional
    public void deleteMappingsByPolicyId(Integer policyId) {
        log.info("Deleting mappings for policyId: {}", policyId);

        try {
            PolicyEntity policy = policyRepository.findById(policyId)
                    .orElseThrow(() -> new RuntimeException("Policy not found with ID: " + policyId));

            List<PolicyReportActionMappingEntity> mappingsToDelete = repository.findByPolicy(policy);

            log.info("Found {} mappings to delete for policyId: {}", mappingsToDelete.size(), policyId);

            repository.deleteAll(mappingsToDelete);

            log.info("Successfully deleted {} mappings for policyId: {}", mappingsToDelete.size(), policyId);
        } catch (RuntimeException e) {
            log.error("Error deleting mappings for policyId {}: {}", policyId, e.getMessage());
            throw e; // Re-throw the exception to rollback the transaction
        }
    }

    @Transactional
    public void deleteMappingsByPolicyIds(List<Integer> policyIds) {
        log.info("Deleting mappings for policyIds: {}", policyIds);

        try {
            List<PolicyEntity> policies = policyRepository.findAllById(policyIds);

            if (policies.size() != policyIds.size()) {
                log.warn("Not all policies were found. Found {} out of {} requested.", policies.size(), policyIds.size());
            }

            List<PolicyReportActionMappingEntity> mappingsToDelete = repository.findByPolicyIn(policies);

            log.info("Found {} mappings to delete for {} policyIds", mappingsToDelete.size(), policyIds.size());

            repository.deleteAll(mappingsToDelete);

            log.info("Successfully deleted {} mappings for {} policyIds", mappingsToDelete.size(), policyIds.size());
        } catch (RuntimeException e) {
            log.error("Error deleting mappings for policyIds {}: {}", policyIds, e.getMessage());
            throw e; // Re-throw the exception to rollback the transaction
        }
    }

    @Transactional(readOnly = true)
    public List<PolicyReportActionMapping> getMappingsByPolicyId(Integer policyId) {
        log.info("Fetching policy report action mappings for policy ID: {}", policyId);
        return repository.findByPolicyPolicyId(policyId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<PolicyReportActionMapping> getMappingsByReportId(Integer reportId) {
        log.info("Fetching policy report action mappings for report ID: {}", reportId);
        return repository.findByReportReportId(reportId).stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }
}