package com.dair.cais.reports.service;

import com.dair.cais.connection.ConnectionService;
import com.dair.cais.reports.*;
import com.dair.cais.reports.dto.*;
import com.dair.cais.reports.exception.*;
import com.dair.cais.reports.repository.ReportColumnRepository;
import com.dair.cais.reports.repository.ReportsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportExecutionService {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_DATE;
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ISO_DATE_TIME;

    private final ReportsRepository reportRepository;
    private final ReportColumnRepository columnRepository;
    private final ConnectionService connectionService;
    private final QueryBuilderService queryBuilderService;
    private final ObjectMapper objectMapper;

    @Transactional(readOnly = true)
    public ReportExecutionResultDto executeReport(Integer reportId, ReportQueryRequestDto request) {
        long startTime = System.currentTimeMillis();
        int totalFilteredRows = 0;
        String executedQuery = null;

        log.info("Starting report execution for reportId: {}", reportId);

        try {
            // Fetch report configuration
            ReportsEntity report = reportRepository.findById(reportId)
                    .orElseThrow(() -> new ReportNotFoundException(reportId));

            // Validate report status
            if (!Boolean.TRUE.equals(report.getIsPublished())) {
                throw new InvalidReportStateException("Cannot execute unpublished report: " + reportId);
            }

            // Get configured columns
            List<ReportColumnEntity> columns = columnRepository.findByReportIdOrderBySortPriorityAsc(reportId);
            if (columns.isEmpty()) {
                throw new InvalidReportStateException("Report has no configured columns: " + reportId);
            }

            // Validate connection exists
            connectionService.validateConnectionExists(report.getConnectionId());

            // Build and validate query
            List<Object> queryParams = new ArrayList<>();
            String sql = buildExecutionQuery(report, columns, request.getQuery(), queryParams);
            executedQuery = sql; // Store for metadata

            // Execute query with timing
            long queryStartTime = System.currentTimeMillis();
            List<Map<String, Object>> rawData;

            try {
                rawData = connectionService.executeQuery(
                        report.getConnectionId(),
                        sql,
                        queryParams.toArray()
                );
                totalFilteredRows = rawData.size(); // Store for metadata
            } catch (Exception e) {
                log.error("Query execution failed for report {}: {}", reportId, e.getMessage());
                throw new ReportExecutionException("Query execution failed", e);
            }

            long queryEndTime = System.currentTimeMillis();
            log.debug("Query executed in {}ms, returned {} rows",
                    queryEndTime - queryStartTime, rawData.size());

            // Format results
            List<Map<String, Object>> formattedData = formatResults(rawData, columns);

            // Build cache key if caching is enabled
            String cacheKey = null;
            ZonedDateTime cacheExpiry = null;
            if (report.getCacheDuration() != null && report.getCacheDuration() > 0) {
                cacheKey = buildCacheKey(reportId, request);
                cacheExpiry = ZonedDateTime.now().plusMinutes(report.getCacheDuration());
            }

            // Build execution metadata
            ReportExecutionResultDto.ExecutionMetadata metadata = ReportExecutionResultDto.ExecutionMetadata.builder()
                    .executionTimeMs(queryEndTime - queryStartTime)
                    .queryString(executedQuery)
                    .returnedRows(formattedData.size())
                    .filteredRows(totalFilteredRows)
                    .cacheExpiry(cacheExpiry)
                    .build();

            // Build final result
            ReportExecutionResultDto result = ReportExecutionResultDto.builder()
                    .reportId(reportId)
                    .reportName(report.getReportName())
                    .columns(columns.stream()
                            .filter(col -> Boolean.TRUE.equals(col.getIsVisible()))
                            .map(this::mapColumnToDto)
                            .collect(Collectors.toList()))
                    .data(formattedData)
                    .totalRows(formattedData.size())
                    .executionTime(ZonedDateTime.now())
                    .metadata(metadata)
                    .build();

            // Store in cache if enabled
            if (cacheKey != null) {
                cacheReport(cacheKey, result, report.getCacheDuration());
            }

            long totalTime = System.currentTimeMillis() - startTime;
            log.info("Report {} executed successfully in {}ms, returned {} rows",
                    reportId, totalTime, formattedData.size());

            return result;

        } catch (ReportNotFoundException | InvalidReportStateException | InvalidQueryException e) {
            log.error("Report execution failed with validation error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during report execution for ID {}: {}", reportId, e.getMessage(), e);
            throw new ReportExecutionException("Failed to execute report: " + e.getMessage(), e);
        } finally {
            // Log execution metrics
            logExecutionMetrics(reportId, startTime, totalFilteredRows, executedQuery);
        }
    }

    private String buildExecutionQuery(ReportsEntity report, List<ReportColumnEntity> columns,
                                       QueryFilterDto filters, List<Object> params) {
        StringBuilder query = new StringBuilder("SELECT ");

        // Add column selections
        String columnList = columns.stream()
                .filter(col -> Boolean.TRUE.equals(col.getIsVisible()))
                .map(col -> String.format("%s AS \"%s\"",
                        sanitizeColumnName(col.getSourceColumn()),
                        sanitizeColumnName(col.getSourceColumn())))
                .collect(Collectors.joining(", "));
        query.append(columnList);

        // Add FROM clause
        query.append(" FROM ").append(sanitizeTableName(report.getTableViewName()));

        // Add WHERE clause if filters exist
        if (filters != null && filters.getRules() != null && !filters.getRules().isEmpty()) {
            String whereClause = queryBuilderService.buildWhereClause(filters, params);
            if (!whereClause.isEmpty()) {
                query.append(" WHERE ").append(whereClause);
            }
        }

        // Add ORDER BY clause
        List<String> orderClauses = columns.stream()
                .filter(col -> col.getSortPriority() != null && col.getSortPriority() > 0)
                .sorted(Comparator.comparing(ReportColumnEntity::getSortPriority))
                .map(col -> String.format("%s %s",
                        sanitizeColumnName(col.getSourceColumn()),
                        col.getSortDirection() != null ? col.getSortDirection() : "ASC"))
                .collect(Collectors.toList());

        if (!orderClauses.isEmpty()) {
            query.append(" ORDER BY ").append(String.join(", ", orderClauses));
        }

        // Add row limit
        if (report.getMaxRows() != null && report.getMaxRows() > 0) {
            query.append(" LIMIT ").append(report.getMaxRows());
        }

        String finalQuery = query.toString();
        log.debug("Built query: {}", finalQuery);
        return finalQuery;
    }

    private String sanitizeColumnName(String columnName) {
        // Basic SQL injection prevention
        if (!columnName.matches("^[a-zA-Z0-9_\\.]+$")) {
            throw new InvalidQueryException("Invalid column name: " + columnName);
        }
        return columnName;
    }

    private String sanitizeTableName(String tableName) {
        // Basic SQL injection prevention for table names
        if (!tableName.matches("^[a-zA-Z0-9_\\.]+$")) {
            throw new InvalidQueryException("Invalid table name: " + tableName);
        }
        return tableName;
    }

    private List<Map<String, Object>> formatResults(List<Map<String, Object>> rawData,
                                                    List<ReportColumnEntity> columns) {
        return rawData.stream()
                .map(row -> formatRow(row, columns))
                .collect(Collectors.toList());
    }

    private Map<String, Object> formatRow(Map<String, Object> row, List<ReportColumnEntity> columns) {
        Map<String, Object> formattedRow = new LinkedHashMap<>(); // Maintain column order

        columns.stream()
                .filter(col -> Boolean.TRUE.equals(col.getIsVisible()))
                .forEach(column -> {
                    Object value = row.get(column.getSourceColumn());
                    formattedRow.put(
                            column.getSourceColumn(),
                            formatValue(value, column.getDataType(), column.getFormattingJson())
                    );
                });

        return formattedRow;
    }

    private Object formatValue(Object value, String dataType, Map<String, Object> formatting) {
        if (value == null) {
            return null;
        }

        try {
            switch (dataType.toLowerCase()) {
                case "number":
                case "decimal":
                case "numeric":
                    return formatNumericValue(value, formatting);
                case "date":
                    return formatDateValue(value, formatting);
                case "timestamp":
                    return formatTimestampValue(value, formatting);
                case "boolean":
                    return formatBooleanValue(value, formatting);
                default:
                    return value.toString();
            }
        } catch (Exception e) {
            log.warn("Error formatting value of type {}: {}", dataType, e.getMessage());
            return value;
        }
    }

    private Object formatNumericValue(Object value, Map<String, Object> formatting) {
        if (formatting == null) {
            return value;
        }

        try {
            Number number = (Number) value;
            Integer decimalPlaces = (Integer) formatting.get("decimalPlaces");
            if (decimalPlaces != null) {
                return String.format("%." + decimalPlaces + "f", number.doubleValue());
            }
            return number;
        } catch (Exception e) {
            log.warn("Error formatting numeric value: {}", e.getMessage());
            return value;
        }
    }

    private Object formatDateValue(Object value, Map<String, Object> formatting) {
        if (formatting == null || !(value instanceof LocalDate || value instanceof LocalDateTime)) {
            return value;
        }

        try {
            String format = (String) formatting.get("dateFormat");
            if (format != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return formatter.format((LocalDate) value);
            }
        } catch (Exception e) {
            log.warn("Error formatting date value: {}", e.getMessage());
        }
        return value;
    }

    private Object formatTimestampValue(Object value, Map<String, Object> formatting) {
        if (formatting == null || !(value instanceof LocalDateTime || value instanceof ZonedDateTime)) {
            return value;
        }

        try {
            String format = (String) formatting.get("timestampFormat");
            if (format != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
                return formatter.format((value instanceof LocalDateTime) ?
                        (LocalDateTime) value :
                        (ZonedDateTime) value);
            }
        } catch (Exception e) {
            log.warn("Error formatting timestamp value: {}", e.getMessage());
        }
        return value;
    }

    private Object formatBooleanValue(Object value, Map<String, Object> formatting) {
        if (formatting == null || !(value instanceof Boolean)) {
            return value;
        }

        try {
            String trueDisplay = (String) formatting.get("trueDisplay");
            String falseDisplay = (String) formatting.get("falseDisplay");

            return (Boolean) value ?
                    (trueDisplay != null ? trueDisplay : "Yes") :
                    (falseDisplay != null ? falseDisplay : "No");
        } catch (Exception e) {
            log.warn("Error formatting boolean value: {}", e.getMessage());
            return value;
        }
    }

    private ReportColumnDto mapColumnToDto(ReportColumnEntity entity) {
        return ReportColumnDto.builder()
                .crcId(entity.getCrcId())
                .reportId(entity.getReportId())
                .sourceColumn(entity.getSourceColumn())
                .displayName(entity.getDisplayName())
                .dataType(entity.getDataType())
                .isSortable(entity.getIsSortable())
                .isFilterable(entity.getIsFilterable())
                .isExportable(entity.getIsExportable())
                .isVisible(entity.getIsVisible())
                .sortPriority(entity.getSortPriority())
                .sortDirection(entity.getSortDirection())
                .columnWidth(entity.getColumnWidth())
                .alignment(entity.getAlignment())
                .formattingJson(mapFormattingJson(entity.getFormattingJson()))
                .build();
    }

    private FormattingConfigDto mapFormattingJson(Map<String, Object> formattingJson) {
        if (formattingJson == null) {
            return null;
        }

        try {
            return objectMapper.convertValue(formattingJson, FormattingConfigDto.class);
        } catch (Exception e) {
            log.warn("Error mapping formatting JSON: {}", e.getMessage());
            return null;
        }
    }

    private String buildCacheKey(Integer reportId, ReportQueryRequestDto request) {
        return String.format("report:%d:query:%s",
                reportId,
                DigestUtils.md5Hex(Objects.toString(request.getQuery()))
        );
    }

    private void cacheReport(String cacheKey, ReportExecutionResultDto result, Integer duration) {
        try {
            // Implementation depends on your caching solution (Redis, Caffeine, etc.)
            log.debug("Caching report result with key: {}", cacheKey);
        } catch (Exception e) {
            log.warn("Failed to cache report result: {}", e.getMessage());
        }
    }

    private void logExecutionMetrics(Integer reportId, long startTime, int rowCount, String query) {
        long executionTime = System.currentTimeMillis() - startTime;

        try {
            MDC.put("reportId", reportId.toString());
            MDC.put("executionTime", String.valueOf(executionTime));
            MDC.put("rowCount", String.valueOf(rowCount));

            log.info("Report execution metrics - Time: {}ms, Rows: {}", executionTime, rowCount);
            if (log.isDebugEnabled()) {
                log.debug("Executed query: {}", query);
            }
        } finally {
            MDC.clear();
        }
    }
}

//package com.dair.cais.reports.service;
//
//import com.dair.cais.connection.ConnectionService;
//import com.dair.cais.reports.ReportColumnEntity;
//import com.dair.cais.reports.ReportsEntity;
//import com.dair.cais.reports.dto.QueryFilterDto;
//import com.dair.cais.reports.dto.ReportColumnDto;
//import com.dair.cais.reports.dto.ReportExecutionResultDto;
//import com.dair.cais.reports.dto.ReportQueryRequestDto;
//import com.dair.cais.reports.exception.InvalidReportStateException;
//import com.dair.cais.reports.exception.ReportExecutionException;
//import com.dair.cais.reports.exception.ReportNotFoundException;
//import com.dair.cais.reports.repository.ReportColumnRepository;
//import com.dair.cais.reports.repository.ReportsRepository;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.time.ZonedDateTime;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//@RequiredArgsConstructor
//@Slf4j
//public class ReportExecutionService {
//
//    private final ReportsRepository reportRepository;
//    private final ReportColumnRepository columnRepository;
//    private final QueryBuilderService queryBuilderService;
//    private final ConnectionService connectionService;
//    private final JdbcTemplate jdbcTemplate;
//
//    @Transactional(readOnly = true)
//    public ReportExecutionResultDto executeReport(Integer reportId, ReportQueryRequestDto request) {
//        try {
//            // Fetch report configuration
//            ReportsEntity report = reportRepository.findById(reportId)
//                    .orElseThrow(() -> new ReportNotFoundException(reportId));
//
//            if (!Boolean.TRUE.equals(report.getIsPublished())) {
//                throw new InvalidReportStateException("Cannot execute unpublished report");
//            }
//
//            List<ReportColumnEntity> columns = columnRepository.findByReportIdOrderBySortPriorityAsc(reportId);
//            if (columns.isEmpty()) {
//                throw new InvalidReportStateException("Report has no configured columns");
//            }
//
//            // Build query
//            String sql = buildExecutionQuery(report, columns, request.getQuery());
//            List<Map<String, Object>> data = executeQuery(sql, report.getConnectionId());
//
//            // Format results
//            List<Map<String, Object>> formattedData = formatResults(data, columns);
//
//            // Build response
//            return ReportExecutionResultDto.builder()
//                    .reportId(reportId)
//                    .reportName(report.getReportName())
//                    .columns(columns.stream().map(this::mapColumnToDto).collect(Collectors.toList()))
//                    .data(formattedData)
//                    .totalRows(formattedData.size())
//                    .executionTime(ZonedDateTime.now())
//                    .build();
//
//        } catch (Exception e) {
//            log.error("Error executing report {}: {}", reportId, e.getMessage(), e);
//            throw new ReportExecutionException("Failed to execute report", e);
//        }
//    }
//
//    private String buildExecutionQuery(ReportsEntity report, List<ReportColumnEntity> columns,
//                                       QueryFilterDto filters) {
//        StringBuilder query = new StringBuilder("SELECT ");
//
//        // Add column selections
//        String columnList = columns.stream()
//                .map(col -> col.getSourceColumn() + " AS \"" + col.getSourceColumn() + "\"")
//                .collect(Collectors.joining(", "));
//        query.append(columnList);
//
//        // Add FROM clause
//        query.append(" FROM ").append(report.getTableViewName());
//
//        // Add WHERE clause if filters exist
//        List<Object> params = new ArrayList<>();
//        if (filters != null && filters.getRules() != null && !filters.getRules().isEmpty()) {
//            String whereClause = queryBuilderService.buildWhereClause(filters, params);
//            if (!whereClause.isEmpty()) {
//                query.append(" WHERE ").append(whereClause);
//            }
//        }
//
//        // Add ORDER BY clause based on column configurations
//        List<String> orderByClauses = columns.stream()
//                .filter(col -> col.getSortPriority() != null && col.getSortPriority() > 0)
//                .sorted(Comparator.comparing(ReportColumnEntity::getSortPriority))
//                .map(col -> col.getSourceColumn() + " " +
//                        (col.getSortDirection() != null ? col.getSortDirection() : "ASC"))
//                .collect(Collectors.toList());
//
//        if (!orderByClauses.isEmpty()) {
//            query.append(" ORDER BY ").append(String.join(", ", orderByClauses));
//        }
//
//        // Add row limit if configured
//        if (report.getMaxRows() != null && report.getMaxRows() > 0) {
//            query.append(" LIMIT ").append(report.getMaxRows());
//        }
//
//        log.debug("Built query: {}", query);
//        return query.toString();
//    }
//
//    private List<Map<String, Object>> executeQuery(String sql, Long connectionId) {
//        try {
//            return connectionService.executeQuery(connectionId, sql);
//        } catch (Exception e) {
//            log.error("Error executing query: {}", e.getMessage());
//            throw new ReportExecutionException("Failed to execute query", e);
//        }
//    }
//
//    private List<Map<String, Object>> formatResults(List<Map<String, Object>> data,
//                                                    List<ReportColumnEntity> columns) {
//        return data.stream()
//                .map(row -> formatRow(row, columns))
//                .collect(Collectors.toList());
//    }
//
//    private Map<String, Object> formatRow(Map<String, Object> row, List<ReportColumnEntity> columns) {
//        Map<String, Object> formattedRow = new HashMap<>();
//
//        for (ReportColumnEntity column : columns) {
//            if (Boolean.TRUE.equals(column.getIsVisible())) {
//                Object value = row.get(column.getSourceColumn());
//                formattedRow.put(column.getSourceColumn(),
//                        formatValue(value, column.getDataType(), column.getFormattingJson()));
//            }
//        }
//
//        return formattedRow;
//    }
//
//    private Object formatValue(Object value, String dataType, Map<String, Object> formatting) {
//        if (value == null) return null;
//
//        try {
//            if (formatting == null) return value;
//
//            return switch (dataType.toLowerCase()) {
//                case "number", "decimal", "numeric" -> formatNumericValue(value, formatting);
//                case "date", "timestamp" -> formatDateValue(value, formatting);
//                default -> value.toString();
//            };
//        } catch (Exception e) {
//            log.warn("Error formatting value: {}", e.getMessage());
//            return value;
//        }
//    }
//
//    // Helper methods for specific data type formatting
//    private Object formatNumericValue(Object value, Map<String, Object> formatting) {
//        // Implementation for numeric formatting
//        return value;
//    }
//
//    private Object formatDateValue(Object value, Map<String, Object> formatting) {
//        // Implementation for date formatting
//        return value;
//    }
//
//    private ReportColumnDto mapColumnToDto(ReportColumnEntity entity) {
//        // Implementation of column mapping
//        return new ReportColumnDto(); // Add mapping logic
//    }
//}
