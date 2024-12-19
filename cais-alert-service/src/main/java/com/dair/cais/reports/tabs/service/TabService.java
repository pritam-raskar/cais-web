package com.dair.cais.reports.tabs.service;

import com.dair.cais.reports.ReportColumnEntity;
import com.dair.cais.reports.ReportDesignerService;
import com.dair.cais.reports.ReportParameterEntity;
import com.dair.cais.reports.ReportsEntity;
import com.dair.cais.reports.dto.*;
import com.dair.cais.reports.repository.ReportColumnRepository;
import com.dair.cais.reports.repository.ReportParameterRepository;
import com.dair.cais.reports.repository.ReportsRepository;
import com.dair.cais.reports.tabs.dto.TabCreateRequestDto;
import com.dair.cais.reports.tabs.dto.TabDetailsDto;
import com.dair.cais.reports.tabs.dto.TabSummaryDto;
import com.dair.cais.reports.tabs.exception.TabCreationException;
import com.dair.cais.reports.tabs.exception.TabNotFoundException;
import com.dair.cais.reports.tabs.exception.TabRetrievalException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for managing tabs (reports with isTab=true)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TabService {
    private final ReportDesignerService reportService;
    private final ReportsRepository reportsRepository;
    private final ReportColumnRepository columnRepository;
    private final ReportParameterRepository parameterRepository;
    private final ObjectMapper objectMapper;

    /**
     * Creates a new tab using the report creation mechanism
     *
     * @param createRequest Tab creation request
     * @return Created tab details
     */
    @Transactional
    public TabDetailsDto createTab(TabCreateRequestDto createRequest) {
        log.info("Creating new tab with identifier: {}", createRequest.getReportIdentifier());
        MDC.put("tabIdentifier", createRequest.getReportIdentifier());

        try {
            // Convert tab request to report request
            ReportCreateDto reportCreateDto = new ReportCreateDto();
            reportCreateDto.setReportIdentifier(createRequest.getReportIdentifier());
            reportCreateDto.setReportName(createRequest.getReportName());
            reportCreateDto.setReportDescription(createRequest.getReportDescription());
            reportCreateDto.setConnectionId(createRequest.getConnectionId());
            reportCreateDto.setTableViewName(createRequest.getTableViewName());
            reportCreateDto.setReportType(createRequest.getReportType());
            reportCreateDto.setCacheDuration(createRequest.getCacheDuration());
            reportCreateDto.setMaxRows(createRequest.getMaxRows());
            reportCreateDto.setColumns(createRequest.getColumns());
            reportCreateDto.setParameters(createRequest.getParameters());
            reportCreateDto.setIsTab(true); // Always set isTab to true

            // Use existing report service to create the tab
            ReportDto createdReport = reportService.createReport(reportCreateDto);

            return mapToTabDetails(createdReport);
        } catch (Exception e) {
            log.error("Error creating tab: {}", e.getMessage(), e);
            throw new TabCreationException("Failed to create tab: " + e.getMessage(), e);
        } finally {
            MDC.remove("tabIdentifier");
        }
    }

    /**
     * Retrieves all tabs (reports with isTab=true)
     *
     * @return List of tab details
     */
    @Transactional(readOnly = true)
    public List<TabDetailsDto> getAllTabs() {
        log.debug("Retrieving all tabs");

        try {
            List<ReportsEntity> tabs = reportsRepository.findByIsTabTrue();
            List<TabDetailsDto> tabDetails = tabs.stream()
                    .map(report -> {
                        List<ReportColumnEntity> columns =
                                columnRepository.findByReportIdOrderBySortPriorityAsc(report.getReportId());
                        List<ReportParameterEntity> parameters =
                                parameterRepository.findByReportId(report.getReportId());
                        return mapToTabDetails(report, columns, parameters);
                    })
                    .collect(Collectors.toList());

            log.debug("Retrieved {} tabs", tabDetails.size());
            return tabDetails;
        } catch (Exception e) {
            log.error("Error retrieving tabs: {}", e.getMessage(), e);
            throw new TabRetrievalException("Failed to retrieve tabs", e);
        }
    }

    /**
     * Retrieves summary information for all tabs
     *
     * @return List of tab summaries
     */
    @Transactional(readOnly = true)
    public List<TabSummaryDto> getTabsSummary() {
        log.debug("Retrieving tabs summary");

        try {
            List<ReportsEntity> tabs = reportsRepository.findByIsTabTrue();
            List<TabSummaryDto> summaries = tabs.stream()
                    .map(report -> TabSummaryDto.builder()
                            .reportId(report.getReportId())
                            .tabName(report.getReportName())
                            .tabDescription(report.getReportDescription())
                            .build())
                    .collect(Collectors.toList());

            log.debug("Retrieved {} tab summaries", summaries.size());
            return summaries;
        } catch (Exception e) {
            log.error("Error retrieving tab summaries: {}", e.getMessage(), e);
            throw new TabRetrievalException("Failed to retrieve tab summaries", e);
        }
    }

    // Helper methods for mapping
    private TabDetailsDto mapToTabDetails(ReportDto reportDto) {
        return TabDetailsDto.builder()
                .reportId(reportDto.getReportId())
                .reportIdentifier(reportDto.getReportIdentifier())
                .tabName(reportDto.getReportName())
                .tabDescription(reportDto.getReportDescription())
                .reportType(reportDto.getReportType())
                .tableViewName(reportDto.getTableViewName())
                .isPublished(reportDto.getIsPublished())
                .columns(reportDto.getColumns())
                .parameters(reportDto.getParameters())
                .build();
    }

    private TabDetailsDto mapToTabDetails(ReportsEntity report,
                                          List<ReportColumnEntity> columns,
                                          List<ReportParameterEntity> parameters) {
        return TabDetailsDto.builder()
                .reportId(report.getReportId())
                .reportIdentifier(report.getReportIdentifier())
                .tabName(report.getReportName())
                .tabDescription(report.getReportDescription())
                .reportType(report.getReportType())
                .tableViewName(report.getTableViewName())
                .isPublished(report.getIsPublished())
                .columns(columns.stream()
                        .map(this::mapColumnToDto)
                        .collect(Collectors.toList()))
                .parameters(parameters.stream()
                        .map(this::mapParameterToDto)
                        .collect(Collectors.toList()))
                .build();
    }

    /**
     * Maps a ReportColumnEntity to ReportColumnDto
     */
    private ReportColumnDto mapColumnToDto(ReportColumnEntity entity) {
        ReportColumnDto dto = new ReportColumnDto();
        dto.setCrcId(entity.getCrcId());
        dto.setReportId(entity.getReportId());
        dto.setSourceColumn(entity.getSourceColumn());
        dto.setDisplayName(entity.getDisplayName());
        dto.setDataType(entity.getDataType());
        dto.setIsSortable(entity.getIsSortable());
        dto.setIsFilterable(entity.getIsFilterable());
        dto.setIsExportable(entity.getIsExportable());
        dto.setIsVisible(entity.getIsVisible());
        dto.setSortPriority(entity.getSortPriority());
        dto.setSortDirection(entity.getSortDirection());
        dto.setColumnWidth(entity.getColumnWidth());
        dto.setAlignment(entity.getAlignment());

        // Handle formatting JSON
        if (entity.getFormattingJson() != null) {
            try {
                FormattingConfigDto formattingConfig = objectMapper.convertValue(
                        entity.getFormattingJson(),
                        FormattingConfigDto.class
                );
                dto.setFormattingJson(formattingConfig);
            } catch (Exception e) {
                log.error("Error converting formatting JSON for column {}: {}",
                        entity.getCrcId(), e.getMessage());
            }
        }

        return dto;
    }

    /**
     * Maps a ReportParameterEntity to ReportParameterDto
     */
    private ReportParameterDto mapParameterToDto(ReportParameterEntity entity) {
        ReportParameterDto dto = new ReportParameterDto();
        dto.setParameterId(entity.getParameterId());
        dto.setParameterName(entity.getParameterName());
        dto.setParameterLabel(entity.getParameterLabel());
        dto.setParameterType(entity.getParameterType());
        dto.setIsRequired(entity.getIsRequired());
        dto.setDefaultValue(entity.getDefaultValue());

        if (entity.getValidationRules() != null) {
            dto.setValidationRules(entity.getValidationRules());
        }

        return dto;
    }
}