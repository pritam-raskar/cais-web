package com.dair.cais.access.reports;

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
public class ReportService {

    private final ReportRepository repository;
    private final ReportMapper mapper;

    @Transactional(readOnly = true)
    public List<Report> getAllReports() {
        log.info("Fetching all reports");
        return repository.findAll().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<Report> getReportById(Integer reportId) {
        log.info("Fetching report with ID: {}", reportId);
        return repository.findById(reportId).map(mapper::toDto);
    }

    @Transactional
    public Report createReport(Report Report) {
        log.info("Creating new report");
        ReportEntity entity = mapper.toEntity(Report);
        ReportEntity savedEntity = repository.save(entity);
        log.debug("Created report with ID: {}", savedEntity.getReportId());
        return mapper.toDto(savedEntity);
    }

    @Transactional
    public Optional<Report> updateReport(Integer reportId, Report Report) {
        log.info("Updating report with ID: {}", reportId);
        return repository.findById(reportId)
                .map(existingEntity -> {
                    ReportEntity updatedEntity = mapper.toEntity(Report);
                    updatedEntity.setReportId(existingEntity.getReportId());
                    ReportEntity savedEntity = repository.save(updatedEntity);
                    log.debug("Updated report with ID: {}", savedEntity.getReportId());
                    return mapper.toDto(savedEntity);
                });
    }

    @Transactional
    public void deleteReport(Integer reportId) {
        log.info("Deleting report with ID: {}", reportId);
        repository.deleteById(reportId);
        log.debug("Deleted report with ID: {}", reportId);
    }
}
