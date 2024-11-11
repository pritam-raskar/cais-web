package com.dair.cais.reports;

import com.dair.cais.connection.ConnectionService;
import com.dair.cais.reports.dto.*;
import com.dair.cais.reports.exception.*;
import com.dair.cais.reports.repository.ReportColumnRepository;
import com.dair.cais.reports.repository.ReportParameterRepository;
import com.dair.cais.reports.repository.ReportsRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for managing report definitions and executions.
 * Handles CRUD operations, report execution, and metadata management.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportDesignerService {
    private final ReportsRepository reportRepository;
    private final ReportColumnRepository columnRepository;
    private final ConnectionService connectionService;
    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final ReportParameterRepository parameterRepository;  // Add this


    @Transactional
    public ReportDto createReport(ReportCreateDto createDto) {
        log.info("Creating new report with identifier: {}", createDto.getReportIdentifier());

        // Validate unique identifier
        if (reportRepository.existsByReportIdentifier(createDto.getReportIdentifier())) {
            throw new DuplicateReportIdentifierException(createDto.getReportIdentifier());
        }

        // Validate connection
        validateConnection(createDto.getConnectionId());

        try {
            // Create report entity
            ReportsEntity report = new ReportsEntity();
            report.setReportIdentifier(createDto.getReportIdentifier());
            report.setReportName(createDto.getReportName());
            report.setReportDescription(createDto.getReportDescription());
            report.setConnectionId(createDto.getConnectionId());
            report.setTableViewName(createDto.getTableViewName());
            report.setReportType(createDto.getReportType() != null ? createDto.getReportType() : "TABLE");
            report.setCacheDuration(createDto.getCacheDuration() != null ? createDto.getCacheDuration() : 0);
            report.setMaxRows(createDto.getMaxRows() != null ? createDto.getMaxRows() : 1000);
            report.setStatus("DRAFT");
            report.setIsPublished(false);
            report.setCreatedAt(ZonedDateTime.now());
            report.setUpdatedAt(ZonedDateTime.now());

            // Save report
            ReportsEntity savedReport = reportRepository.save(report);
            log.debug("Created report entity with ID: {}", savedReport.getReportId());

            // Create and save columns
            List<ReportColumnEntity> columns = new ArrayList<>();
            if (createDto.getColumns() != null && !createDto.getColumns().isEmpty()) {
                columns = createDto.getColumns().stream()
                        .map(colDto -> createReportColumn(colDto, savedReport.getReportId()))
                        .toList();
                columnRepository.saveAll(columns);
                log.debug("Created {} column configurations", columns.size());
            }

            // Create and save parameters
            if (createDto.getParameters() != null && !createDto.getParameters().isEmpty()) {
                List<ReportParameterEntity> parameters = createDto.getParameters().stream()
                        .map(paramDto -> {
                            ReportParameterEntity param = createParameterEntity(paramDto, savedReport);
                            param.setCreatedAt(ZonedDateTime.now());
                            param.setUpdatedAt(ZonedDateTime.now());
                            return param;
                        })
                        .toList();
                parameterRepository.saveAll(parameters);
                log.debug("Created {} parameter configurations", parameters.size());
            }

            // Return complete report with columns and parameters
            List<ReportParameterEntity> savedParameters = parameterRepository.findByReportId(savedReport.getReportId());
            return mapToDto(savedReport, columns, savedParameters);

        } catch (Exception e) {
            log.error("Error creating report: {}", e.getMessage(), e);
            throw new ReportCreationException("Failed to create report", e);
        }
    }



    @Transactional
    public ReportDto updateReport(Integer reportId, ReportUpdateDto updateDto) {
        log.info("Updating report with ID: {}", reportId);

        ReportsEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));

        try {
            // Update report properties
            if (updateDto.getReportName() != null) {
                report.setReportName(updateDto.getReportName());
            }
            if (updateDto.getReportDescription() != null) {
                report.setReportDescription(updateDto.getReportDescription());
            }
            if (updateDto.getTableViewName() != null) {
                report.setTableViewName(updateDto.getTableViewName());
            }
            if (updateDto.getReportType() != null) {
                report.setReportType(updateDto.getReportType());
            }
            if (updateDto.getCacheDuration() != null) {
                report.setCacheDuration(updateDto.getCacheDuration());
            }
            if (updateDto.getMaxRows() != null) {
                report.setMaxRows(updateDto.getMaxRows());
            }
            report.setUpdatedAt(ZonedDateTime.now());

            // Update columns if provided
            List<ReportColumnEntity> updatedColumns = null;
            if (updateDto.getColumns() != null) {
                updateReportColumns(report.getReportId(), updateDto.getColumns());
                updatedColumns = columnRepository.findByReportIdOrderBySortPriorityAsc(reportId);
            }

            // Update parameters if provided
            List<ReportParameterEntity> updatedParameters = null;
            if (updateDto.getParameters() != null) {
                // Delete existing parameters
                parameterRepository.deleteByReportId(reportId);

                // Create and save new parameters
                List<ReportParameterEntity> newParameters = updateDto.getParameters().stream()
                        .map(paramDto -> {
                            ReportParameterEntity param = createParameterEntity(paramDto, report);
                            param.setCreatedAt(ZonedDateTime.now());
                            param.setUpdatedAt(ZonedDateTime.now());
                            return param;
                        })
                        .toList();
                parameterRepository.saveAll(newParameters);
                updatedParameters = parameterRepository.findByReportId(reportId);
                log.debug("Updated {} parameters for report {}", newParameters.size(), reportId);
            }

            // Save changes
            ReportsEntity updatedReport = reportRepository.save(report);
            log.debug("Updated report successfully");

            // Get latest columns and parameters if not updated
            if (updatedColumns == null) {
                updatedColumns = columnRepository.findByReportIdOrderBySortPriorityAsc(reportId);
            }
            if (updatedParameters == null) {
                updatedParameters = parameterRepository.findByReportId(reportId);
            }

            // Return updated report
            return mapToDto(updatedReport, updatedColumns, updatedParameters);
        } catch (Exception e) {
            log.error("Error updating report {}: {}", reportId, e.getMessage(), e);
            throw new ReportUpdateException("Failed to update report", e);
        }
    }

    // Update the mapToDto method to include parameters
    private ReportDto mapToDto(ReportsEntity report, List<ReportColumnEntity> columns, List<ReportParameterEntity> parameters) {
        ReportDto dto = new ReportDto();
        dto.setReportId(report.getReportId());
        dto.setReportIdentifier(report.getReportIdentifier());
        dto.setReportName(report.getReportName());
        dto.setReportDescription(report.getReportDescription());
        dto.setConnectionId(report.getConnectionId());
        dto.setTableViewName(report.getTableViewName());
        dto.setReportType(report.getReportType());
        dto.setStatus(report.getStatus());
        dto.setCacheDuration(report.getCacheDuration());
        dto.setMaxRows(report.getMaxRows());
        dto.setIsPublished(report.getIsPublished());
        dto.setCreatedBy(report.getCreatedBy());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedBy(report.getUpdatedBy());
        dto.setUpdatedAt(report.getUpdatedAt());

        if (columns != null) {
            dto.setColumns(columns.stream()
                    .map(this::mapColumnToDto)
                    .toList());
        }

        if (parameters != null) {
            dto.setParameters(parameters.stream()
                    .map(this::mapParameterToDto)
                    .toList());
        }

        return dto;
    }

    private ReportParameterDto mapParameterToDto(ReportParameterEntity entity) {
        ReportParameterDto dto = new ReportParameterDto();
        dto.setParameterId(entity.getParameterId());
        dto.setParameterName(entity.getParameterName());
        dto.setParameterLabel(entity.getParameterLabel());
        dto.setParameterType(entity.getParameterType());
        dto.setIsRequired(entity.getIsRequired());
        dto.setDefaultValue(entity.getDefaultValue());

        if (entity.getValidationRules() != null) {
            try {
                Map<String, Object> rules = objectMapper.readValue(
                        entity.getValidationRules(),
                        new TypeReference<Map<String, Object>>() {}
                );
                dto.setValidationRules(rules);
            } catch (Exception e) {
                log.error("Error parsing validation rules for parameter {}: {}",
                        entity.getParameterId(), e.getMessage());
            }
        }

        return dto;
    }

    @Transactional(readOnly = true)
    public ReportDto getReport(Integer reportId) {
        log.debug("Fetching report with ID: {}", reportId);

        ReportsEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));

        List<ReportColumnEntity> columns = columnRepository.findByReportIdOrderBySortPriorityAsc(reportId);

        return mapToDto(report, columns ,null);
    }

    @Transactional(readOnly = true)
    public Page<ReportDto> searchReports(String searchTerm, String status,
                                         Boolean isPublished, Pageable pageable) {
        log.debug("Searching reports with term: {}, status: {}, published: {}",
                searchTerm, status, isPublished);

        return reportRepository.findByFilters(status, isPublished, searchTerm, pageable)
                .map(report -> mapToDto(report, report.getColumns() , null));
    }

    @Transactional
    public ReportDto publishReport(Integer reportId) {
        log.info("Publishing report with ID: {}", reportId);

        ReportsEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));

        // Get columns explicitly to validate
        List<ReportColumnEntity> columns = columnRepository.findByReportIdOrderBySortPriorityAsc(reportId);
        if (columns.isEmpty()) {
            throw new InvalidReportStateException("Report must have at least one column");
        }

        // Update status
        report.setStatus("PUBLISHED");
        report.setIsPublished(true);
        report.setUpdatedAt(ZonedDateTime.now());

        ReportsEntity publishedReport = reportRepository.save(report);
        log.info("Successfully published report: {}", reportId);

        return mapToDto(publishedReport, columns, null);
    }

    @Transactional(readOnly = true)
    public ReportExecutionResultDto executeReport(Integer reportId, Map<String, Object> parameters) {
        log.info("Executing report: {} with parameters: {}", reportId, parameters);

        try {
            // Get report configuration
            ReportsEntity report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ReportNotFoundException(reportId));
            List<ReportColumnEntity> columns = columnRepository
                    .findByReportIdOrderBySortPriorityAsc(reportId);

            // Validate report is published
            if (!Boolean.TRUE.equals(report.getIsPublished())) {
                throw new InvalidReportStateException("Cannot execute unpublished report");
            }

            // Validate parameters
            validateParameters(reportId, parameters);

            // Build and execute query
            String sql = buildReportQuery(report, columns, parameters);
            List<Map<String, Object>> rawData = executeReportQuery(sql, parameters);

            // Format results
            List<Map<String, Object>> formattedData = formatReportData(rawData, columns);

            // Build result
            ReportExecutionResultDto result = new ReportExecutionResultDto();
            result.setReportId(reportId);
            result.setReportName(report.getReportName());
            result.setColumns(columns.stream().map(this::mapColumnToDto).toList());
            result.setData(formattedData);
            result.setTotalRows(formattedData.size());
            result.setExecutionTime(ZonedDateTime.now());

            return result;
        } catch (Exception e) {
            log.error("Error executing report {}: {}", reportId, e.getMessage(), e);
            throw new ReportExecutionException("Failed to execute report", e);
        }
    }

    @Transactional
    public void deleteReport(Integer reportId) {
        log.info("Deleting report with ID: {}", reportId);

        ReportsEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));

        try {
            // Delete associated columns first
            columnRepository.deleteByReportId(Long.valueOf(reportId));

            // Delete the report
            reportRepository.delete(report);

            log.info("Successfully deleted report: {}", reportId);
        } catch (Exception e) {
            log.error("Error deleting report {}: {}", reportId, e.getMessage());
            throw new ReportCreationException("Failed to delete report", e);
        }
    }


    private void validateConnection(Long connectionId) {
        if (!connectionService.existsById(connectionId)) {
            throw new ConnectionNotFoundException(connectionId);
        }
    }

    private ReportColumnEntity createReportColumn(ReportColumnCreateDto columnDto, Integer reportId) {
        log.debug("Creating column configuration for report {}: {}", reportId, columnDto.getSourceColumn());

        try {
            ReportColumnEntity column = new ReportColumnEntity();
            column.setReportId(reportId);
            column.setSourceColumn(columnDto.getSourceColumn());
            column.setDisplayName(columnDto.getDisplayName());
            column.setDataType(columnDto.getDataType());
            column.setIsSortable(columnDto.getIsSortable());
            column.setIsFilterable(columnDto.getIsFilterable());
            column.setIsExportable(columnDto.getIsExportable());
            column.setIsVisible(columnDto.getIsVisible());
            column.setSortPriority(columnDto.getSortPriority());
            column.setSortDirection(columnDto.getSortDirection());
            column.setColumnWidth(columnDto.getColumnWidth());
            column.setAlignment(columnDto.getAlignment());

            if (columnDto.getFormattingJson() != null) {
                column.setFormattingJson(objectMapper.convertValue(
                        columnDto.getFormattingJson(),
                        new TypeReference<Map<String, Object>>() {}
                ));
            }

            return column;
        } catch (Exception e) {
            log.error("Error creating column configuration: {}", e.getMessage());
            throw new ReportCreationException("Failed to create column configuration", e);
        }
    }


    private void updateReportColumns(Integer reportId, List<ReportColumnUpdateDto> updateColumns) {
        log.debug("Updating columns for report {}", reportId);

        try {
            // Get existing columns
            List<ReportColumnEntity> existingColumns = columnRepository.findByReportIdOrderBySortPriorityAsc(reportId);

            // Create a map of existing columns by ID
            Map<Integer, ReportColumnEntity> existingColumnMap = existingColumns.stream()
                    .collect(Collectors.toMap(
                            ReportColumnEntity::getCrcId,
                            column -> column,
                            (existing, replacement) -> existing
                    ));

            // Process each column update
            List<ReportColumnEntity> columnsToSave = new ArrayList<>();

            for (ReportColumnUpdateDto updateDto : updateColumns) {
                if (updateDto.getCrcId() != null && existingColumnMap.containsKey(updateDto.getCrcId())) {
                    // Update existing column
                    ReportColumnEntity existingColumn = existingColumnMap.get(updateDto.getCrcId());
                    updateColumnEntity(existingColumn, updateDto);
                    columnsToSave.add(existingColumn);
                    existingColumnMap.remove(updateDto.getCrcId());
                }
            }

            // Delete columns that weren't in the update list
            if (!existingColumnMap.isEmpty()) {
                columnRepository.deleteAll(existingColumnMap.values());
            }

            // Save updated and new columns
            columnRepository.saveAll(columnsToSave);

            log.debug("Updated {} columns for report {}", columnsToSave.size(), reportId);
        } catch (Exception e) {
            log.error("Error updating report columns: {}", e.getMessage());
            throw new ReportUpdateException("Failed to update report columns", e);
        }
    }

    private void updateColumnEntity(ReportColumnEntity entity, ReportColumnUpdateDto dto) {
        if (dto.getDisplayName() != null) {
            entity.setDisplayName(dto.getDisplayName());
        }
        if (dto.getIsSortable() != null) {
            entity.setIsSortable(dto.getIsSortable());
        }
        if (dto.getIsFilterable() != null) {
            entity.setIsFilterable(dto.getIsFilterable());
        }
        if (dto.getIsExportable() != null) {
            entity.setIsExportable(dto.getIsExportable());
        }
        if (dto.getIsVisible() != null) {
            entity.setIsVisible(dto.getIsVisible());
        }
        if (dto.getSortPriority() != null) {
            entity.setSortPriority(dto.getSortPriority());
        }
        if (dto.getSortDirection() != null) {
            entity.setSortDirection(dto.getSortDirection());
        }
        if (dto.getColumnWidth() != null) {
            entity.setColumnWidth(dto.getColumnWidth());
        }
        if (dto.getAlignment() != null) {
            entity.setAlignment(dto.getAlignment());
        }
        if (dto.getFormattingJson() != null) {
            entity.setFormattingJson(objectMapper.convertValue(
                    dto.getFormattingJson(),
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
            ));
        }
        entity.setUpdatedAt(ZonedDateTime.now());
    }



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

    private String buildReportQuery(ReportsEntity report,
                                    List<ReportColumnEntity> columns,
                                    Map<String, Object> parameters) {
        StringBuilder query = new StringBuilder();
        query.append("SELECT ");

        // Add column selections
        String columnList = columns.stream()
                .map(col -> col.getSourceColumn() + " AS \"" + col.getSourceColumn() + "\"")
                .collect(Collectors.joining(", "));
        query.append(columnList);

        // Add FROM clause
        query.append(" FROM ").append(report.getTableViewName());

        // Add WHERE clause based on parameters
        List<String> conditions = new ArrayList<>();
        if (parameters != null) {
            if (parameters.containsKey("date_range")) {
                DateRange range = (DateRange) parameters.get("date_range");
                conditions.add("execution_local_date_time BETWEEN ? AND ?");
            }

            if (parameters.containsKey("direction")) {
                List<String> directions = (List<String>) parameters.get("direction");
                conditions.add("direction_cd IN (" +
                        directions.stream().map(d -> "?").collect(Collectors.joining(",")) + ")");
            }

            if (parameters.containsKey("min_amount")) {
                conditions.add("base_curr_amount >= ?");
            }

            if (parameters.containsKey("currency")) {
                List<String> currencies = (List<String>) parameters.get("currency");
                conditions.add("base_curr_cd IN (" +
                        currencies.stream().map(c -> "?").collect(Collectors.joining(",")) + ")");
            }

            if (parameters.containsKey("account_numbers")) {
                String accounts = (String) parameters.get("account_numbers");
                List<String> accountList = Arrays.asList(accounts.split(","));
                conditions.add("account_number IN (" +
                        accountList.stream().map(a -> "?").collect(Collectors.joining(",")) + ")");
            }
        }

        if (!conditions.isEmpty()) {
            query.append(" WHERE ").append(String.join(" AND ", conditions));
        }

        // Add ORDER BY clause based on column sort priorities
        List<ReportColumnEntity> sortedColumns = columns.stream()
                .filter(col -> col.getSortPriority() != null && col.getSortPriority() > 0)
                .sorted((c1, c2) -> c1.getSortPriority().compareTo(c2.getSortPriority()))
                .toList();

        if (!sortedColumns.isEmpty()) {
            query.append(" ORDER BY ");
            String orderBy = sortedColumns.stream()
                    .map(col -> col.getSourceColumn() + " " +
                            (col.getSortDirection() != null ? col.getSortDirection() : "ASC"))
                    .collect(Collectors.joining(", "));
            query.append(orderBy);
        }

        // Add LIMIT if maxRows is specified
        if (report.getMaxRows() != null && report.getMaxRows() > 0) {
            query.append(" LIMIT ").append(report.getMaxRows());
        }

        return query.toString();
    }

    private List<Map<String, Object>> executeReportQuery(String sql, Map<String, Object> parameters) {
        try {
            if (parameters != null && !parameters.isEmpty()) {
                Object[] paramArray = parameters.values().toArray();
                return jdbcTemplate.queryForList(sql, paramArray);
            } else {
                return jdbcTemplate.queryForList(sql);
            }
        } catch (Exception e) {
            log.error("Error executing query: {}", e.getMessage());
            throw new ReportExecutionException("Failed to execute query", e);
        }
    }

    private List<Map<String, Object>> formatReportData(List<Map<String, Object>> rawData,
                                                       List<ReportColumnEntity> columns) {
        return rawData.stream()
                .map(row -> formatRow(row, columns))
                .collect(Collectors.toList());
    }

    private Map<String, Object> formatRow(Map<String, Object> row, List<ReportColumnEntity> columns) {
        Map<String, Object> formattedRow = new HashMap<>();

        columns.forEach(column -> {
            Object value = row.get(column.getSourceColumn());
            if (value != null) {
                formattedRow.put(column.getSourceColumn(), formatValue(value, column));
            } else {
                formattedRow.put(column.getSourceColumn(), null);
            }
        });

        return formattedRow;
    }

    private Object formatValue(Object value, ReportColumnEntity column) {
        if (value == null) return null;

        try {
            Map<String, Object> formatting = column.getFormattingJson();
            if (formatting == null) return value;

            // Apply formatting based on data type and formatting configuration
            switch (column.getDataType().toLowerCase()) {
                case "number":
                case "decimal":
                case "numeric":
                    return formatNumericValue(value, formatting);
                case "date":
                case "timestamp":
                    return formatDateValue(value, formatting);
                default:
                    return value.toString();
            }
        } catch (Exception e) {
            log.error("Error formatting value: {}", e.getMessage());
            return value;
        }
    }

    private Object formatNumericValue(Object value, Map<String, Object> formatting) {
        // Implementation for numeric formatting
        return value;
    }

    private Object formatDateValue(Object value, Map<String, Object> formatting) {
        // Implementation for date formatting
        return value;
    }

    private byte[] exportToCsv(ReportExecutionResultDto data) {
        log.debug("Converting report data to CSV format");
        StringBuilder csv = new StringBuilder();

        try {
            // Add headers
            if (data.getColumns() != null && !data.getColumns().isEmpty()) {
                csv.append(data.getColumns().stream()
                        .map(ReportColumnDto::getDisplayName)
                        .collect(Collectors.joining(","))).append("\n");

                // Add data rows
                for (Map<String, Object> row : data.getData()) {
                    String rowData = data.getColumns().stream()
                            .map(col -> formatCsvValue(row.get(col.getSourceColumn())))
                            .collect(Collectors.joining(","));
                    csv.append(rowData).append("\n");
                }
            }

            return csv.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Error generating CSV: {}", e.getMessage());
            throw new ReportExecutionException("Failed to generate CSV", e);
        }
    }

    private String formatCsvValue(Object value) {
        if (value == null) {
            return "";
        }

        String str = value.toString();
        // Escape special characters
        if (str.contains(",") || str.contains("\"") || str.contains("\n") || str.contains("\r")) {
            str = str.replace("\"", "\"\""); // Escape quotes
            return "\"" + str + "\"";
        }

        return str;
    }


    @Transactional
    public void addReportParameters(Integer reportId, List<ReportParameterDto> parameters) {
        ReportsEntity report = reportRepository.findById(reportId)
                .orElseThrow(() -> new ReportNotFoundException(reportId));

        List<ReportParameterEntity> parameterEntities = parameters.stream()
                .map(param -> createParameterEntity(param, report))
                .toList();

        parameterRepository.saveAll(parameterEntities);
    }


    private ReportParameterEntity createParameterEntity(ReportParameterDto dto, ReportsEntity report) {
        ReportParameterEntity entity = new ReportParameterEntity();
        entity.setReport(report);
        entity.setParameterName(dto.getParameterName());
        entity.setParameterLabel(dto.getParameterLabel());
        entity.setParameterType(dto.getParameterType());
        entity.setIsRequired(dto.getIsRequired());
        entity.setDefaultValue(dto.getDefaultValue());

        if (dto.getValidationRules() != null) {
            try {
                String validationRulesJson = objectMapper.writeValueAsString(dto.getValidationRules());
                entity.setValidationRules(validationRulesJson);
            } catch (Exception e) {
                log.error("Error converting validation rules to JSON: {}", e.getMessage());
                throw new ReportCreationException("Failed to create parameter validation rules", e);
            }
        }

        return entity;
    }



    private void validateParameters(Integer reportId, Map<String, Object> parameters) {
        List<ReportParameterEntity> reportParams = parameterRepository.findByReportId(reportId);

        for (ReportParameterEntity param : reportParams) {
            if (param.getIsRequired() && !parameters.containsKey(param.getParameterName())) {
                throw new InvalidParameterException(
                        "Required parameter missing: " + param.getParameterLabel());
            }

            validateParameterValue(param, parameters.get(param.getParameterName()));
        }
    }


    private void validateParameterValue(ReportParameterEntity param, Object value) {
        if (value == null) {
            if (param.getIsRequired()) {
                throw new InvalidParameterException(
                        "Required parameter missing: " + param.getParameterLabel());
            }
            return;
        }

        try {
            switch (param.getParameterType()) {
                case NUMBER -> validateNumberParameter(param, value);
                case DATE, DATETIME -> validateDateParameter(param, value);
                case DATE_RANGE -> validateDateRangeParameter(param, value);
                case LIST, MULTI_SELECT -> validateListParameter(param, value);
                case TEXT -> validateTextParameter(param, value);
                case BOOLEAN -> validateBooleanParameter(value);
            }
        } catch (Exception e) {
            throw new InvalidParameterException(
                    "Invalid value for parameter " + param.getParameterLabel() + ": " + e.getMessage());
        }
    }

    private void validateNumberParameter(ReportParameterEntity param, Object value) {
        double numValue;
        if (value instanceof Number) {
            numValue = ((Number) value).doubleValue();
        } else {
            try {
                numValue = Double.parseDouble(value.toString());
            } catch (NumberFormatException e) {
                throw new InvalidParameterException("Value must be a number");
            }
        }

        Map<String, Object> rules = getValidationRules(param);
        if (rules.containsKey("min")) {
            double min = ((Number) rules.get("min")).doubleValue();
            if (numValue < min) {
                throw new InvalidParameterException("Value must be >= " + min);
            }
        }
        if (rules.containsKey("max")) {
            double max = ((Number) rules.get("max")).doubleValue();
            if (numValue > max) {
                throw new InvalidParameterException("Value must be <= " + max);
            }
        }
    }

    private void validateDateParameter(ReportParameterEntity param, Object value) {
        LocalDate date;
        if (value instanceof LocalDate) {
            date = (LocalDate) value;
        } else {
            try {
                date = LocalDate.parse(value.toString());
            } catch (DateTimeParseException e) {
                throw new InvalidParameterException("Invalid date format");
            }
        }

        Map<String, Object> rules = getValidationRules(param);
        if (rules.containsKey("minDate")) {
            LocalDate minDate = LocalDate.parse(rules.get("minDate").toString());
            if (date.isBefore(minDate)) {
                throw new InvalidParameterException("Date must be after " + minDate);
            }
        }
        if (rules.containsKey("maxDate")) {
            LocalDate maxDate = LocalDate.parse(rules.get("maxDate").toString());
            if (date.isAfter(maxDate)) {
                throw new InvalidParameterException("Date must be before " + maxDate);
            }
        }
    }

    private void validateDateRangeParameter(ReportParameterEntity param, Object value) {
        DateRange range;
        if (value instanceof DateRange) {
            range = (DateRange) value;
        } else if (value instanceof Map) {
            // Handle case where value comes as a Map (e.g., from JSON)
            try {
                range = objectMapper.convertValue(value, DateRange.class);
            } catch (Exception e) {
                throw new InvalidParameterException("Invalid date range format");
            }
        } else {
            throw new InvalidParameterException("Invalid date range format");
        }

        if (range.getStart() == null || range.getEnd() == null) {
            throw new InvalidParameterException("Both start and end dates are required");
        }

        if (range.getEnd().isBefore(range.getStart())) {
            throw new InvalidParameterException("End date must be after start date");
        }

        Map<String, Object> rules = getValidationRules(param);
        if (rules.containsKey("maxDays")) {
            int maxDays = (Integer) rules.get("maxDays");
            // Calculate days between dates without ChronoUnit
            long daysBetween = calculateDaysBetween(range.getStart(), range.getEnd());
            if (daysBetween > maxDays) {
                throw new InvalidParameterException(
                        "Date range cannot exceed " + maxDays + " days");
            }
        }

        if (rules.containsKey("allowFutureDates") &&
                !((Boolean) rules.get("allowFutureDates"))) {
            LocalDate today = LocalDate.now();
            if (range.getEnd().isAfter(today)) {
                throw new InvalidParameterException("Future dates are not allowed");
            }
        }
    }

    // Helper method to calculate days between dates
    private long calculateDaysBetween(LocalDate startDate, LocalDate endDate) {
        // Convert to epoch days and calculate difference
        long startEpochDay = startDate.toEpochDay();
        long endEpochDay = endDate.toEpochDay();
        return endEpochDay - startEpochDay;
    }

    private void validateListParameter(ReportParameterEntity param, Object value) {
        Map<String, Object> rules = getValidationRules(param);
        List<?> allowedValues = (List<?>) rules.get("allowedValues");

        if (value instanceof List) {
            List<?> values = (List<?>) value;
            if (!allowedValues.containsAll(values)) {
                throw new InvalidParameterException("Invalid list values");
            }
        } else {
            if (!allowedValues.contains(value)) {
                throw new InvalidParameterException("Invalid value");
            }
        }
    }

    private void validateTextParameter(ReportParameterEntity param, Object value) {
        String textValue = value.toString();
        Map<String, Object> rules = getValidationRules(param);

        if (rules.containsKey("pattern")) {
            String pattern = (String) rules.get("pattern");
            if (!textValue.matches(pattern)) {
                throw new InvalidParameterException("Value does not match required pattern");
            }
        }

        if (rules.containsKey("maxLength")) {
            int maxLength = (Integer) rules.get("maxLength");
            if (textValue.length() > maxLength) {
                throw new InvalidParameterException("Value exceeds maximum length of " + maxLength);
            }
        }
    }

    private void validateBooleanParameter(Object value) {
        if (!(value instanceof Boolean) &&
                !value.toString().equalsIgnoreCase("true") &&
                !value.toString().equalsIgnoreCase("false")) {
            throw new InvalidParameterException("Value must be true or false");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getValidationRules(ReportParameterEntity param) {
        try {
            if (param.getValidationRules() != null) {
                return objectMapper.readValue(
                        param.getValidationRules().toString(),
                        new TypeReference<Map<String, Object>>() {}
                );
            }
        } catch (Exception e) {
            log.error("Error parsing validation rules: {}", e.getMessage());
        }
        return new HashMap<>();
    }

}