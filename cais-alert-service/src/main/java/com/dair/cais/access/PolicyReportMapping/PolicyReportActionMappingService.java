package com.dair.cais.access.PolicyReportMapping;

import com.dair.cais.access.Actions.ActionRepository;
import com.dair.cais.access.reports.ReportRepository;
import com.dair.cais.access.policy.PolicyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public PolicyReportActionMapping createMapping(PolicyReportActionMapping mappingDto) {
        log.info("Creating new policy report action mapping");
        PolicyReportActionMappingEntity entity = mapper.toEntity(mappingDto);
        entity.setPolicy(policyRepository.findById(mappingDto.getPolicyId())
                .orElseThrow(() -> new RuntimeException("Policy not found")));
        entity.setReport(reportRepository.findById(mappingDto.getReportId())
                .orElseThrow(() -> new RuntimeException("Report not found")));
        entity.setAction(actionRepository.findById(mappingDto.getActionId())
                .orElseThrow(() -> new RuntimeException("Action not found")));
        PolicyReportActionMappingEntity savedEntity = repository.save(entity);
        log.debug("Created policy report action mapping with ID: {}", savedEntity.getPraId());
        return mapper.toDto(savedEntity);
    }

    @Transactional
    public Optional<PolicyReportActionMapping> updateMapping(Integer praId, PolicyReportActionMapping updatedMappingDto) {
        log.info("Updating policy report action mapping with ID: {}", praId);
        return repository.findById(praId)
                .map(existingEntity -> {
                    existingEntity.setPolicy(policyRepository.findById(updatedMappingDto.getPolicyId())
                            .orElseThrow(() -> new RuntimeException("Policy not found")));
                    existingEntity.setReport(reportRepository.findById(updatedMappingDto.getReportId())
                            .orElseThrow(() -> new RuntimeException("Report not found")));
                    existingEntity.setAction(actionRepository.findById(updatedMappingDto.getActionId())
                            .orElseThrow(() -> new RuntimeException("Action not found")));
                    existingEntity.setCondition(updatedMappingDto.getCondition());
                    PolicyReportActionMappingEntity savedEntity = repository.save(existingEntity);
                    log.debug("Updated policy report action mapping with ID: {}", savedEntity.getPraId());
                    return mapper.toDto(savedEntity);
                });
    }

    @Transactional
    public void deleteMapping(Integer praId) {
        log.info("Deleting policy report action mapping with ID: {}", praId);
        repository.deleteById(praId);
        log.debug("Deleted policy report action mapping with ID: {}", praId);
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