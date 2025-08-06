package com.dair.cais.reports;

import com.dair.cais.reports.dto.*;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.*;

/**
 * Test data factory for Reports module testing
 * Provides comprehensive test data creation patterns following established conventions
 * from Alert and Workflow test data factories
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ReportsTestDataFactory {

    // Default test constants
    public static final Integer DEFAULT_REPORT_ID = 1001;
    public static final String DEFAULT_REPORT_IDENTIFIER = "TEST_REPORT_001";
    public static final String DEFAULT_REPORT_NAME = "Test Report";
    public static final String DEFAULT_REPORT_DESCRIPTION = "Test report description for testing purposes";
    public static final Long DEFAULT_CONNECTION_ID = 101L;
    public static final String DEFAULT_TABLE_VIEW_NAME = "test_view";
    public static final String DEFAULT_STATUS = "DRAFT";
    public static final String DEFAULT_REPORT_TYPE = "TABLE";
    public static final Integer DEFAULT_CACHE_DURATION = 0;
    public static final Integer DEFAULT_MAX_ROWS = 1000;
    public static final String DEFAULT_CREATED_BY = "test_user";
    public static final String DEFAULT_UPDATED_BY = "test_user";

    // Report Column defaults
    public static final Integer DEFAULT_COLUMN_ID = 2001;
    public static final String DEFAULT_SOURCE_COLUMN = "test_column";
    public static final String DEFAULT_DATA_TYPE = "VARCHAR";
    public static final String DEFAULT_DISPLAY_NAME = "Test Column";
    public static final String DEFAULT_ALIGNMENT = "left";
    public static final Integer DEFAULT_SORT_PRIORITY = 0;

    // Report Parameter defaults
    public static final String DEFAULT_PARAMETER_NAME = "test_param";
    public static final String DEFAULT_PARAMETER_TYPE = "STRING";
    public static final String DEFAULT_PARAMETER_DEFAULT_VALUE = "default_value";

    private static int uniqueIdCounter = 1000;

    /**
     * Generates unique identifier for test objects
     */
    public static String generateUniqueId() {
        return String.valueOf(System.currentTimeMillis() + (++uniqueIdCounter));
    }

    // ========================================
    // ReportsEntity Creation Methods
    // ========================================

    /**
     * Creates a basic ReportsEntity with default values
     */
    public static ReportsEntity createTestReportsEntity() {
        ReportsEntity entity = new ReportsEntity();
        entity.setReportId(DEFAULT_REPORT_ID);
        entity.setReportIdentifier(DEFAULT_REPORT_IDENTIFIER);
        entity.setReportName(DEFAULT_REPORT_NAME);
        entity.setReportDescription(DEFAULT_REPORT_DESCRIPTION);
        entity.setConnectionId(DEFAULT_CONNECTION_ID);
        entity.setTableViewName(DEFAULT_TABLE_VIEW_NAME);
        entity.setStatus(DEFAULT_STATUS);
        entity.setReportType(DEFAULT_REPORT_TYPE);
        entity.setCacheDuration(DEFAULT_CACHE_DURATION);
        entity.setMaxRows(DEFAULT_MAX_ROWS);
        entity.setIsPublished(false);
        entity.setIsTab(false);
        entity.setCreatedBy(DEFAULT_CREATED_BY);
        entity.setUpdatedBy(DEFAULT_UPDATED_BY);
        entity.setCreatedAt(ZonedDateTime.now());
        entity.setUpdatedAt(ZonedDateTime.now());
        entity.setColumns(new ArrayList<>());
        entity.setParameters(new ArrayList<>());
        return entity;
    }

    /**
     * Creates a ReportsEntity with minimal data for creation tests
     */
    public static ReportsEntity createTestReportsEntityWithMinimalData() {
        ReportsEntity entity = new ReportsEntity();
        entity.setReportIdentifier("TEST_" + generateUniqueId());
        entity.setReportName("Minimal Test Report");
        entity.setConnectionId(DEFAULT_CONNECTION_ID);
        entity.setTableViewName(DEFAULT_TABLE_VIEW_NAME);
        entity.setCreatedBy(DEFAULT_CREATED_BY);
        return entity;
    }

    /**
     * Creates a ReportsEntity for update operations
     */
    public static ReportsEntity createTestReportsEntityForUpdate() {
        ReportsEntity entity = createTestReportsEntity();
        entity.setReportId(DEFAULT_REPORT_ID + 1);
        entity.setReportIdentifier("UPDATED_" + DEFAULT_REPORT_IDENTIFIER);
        entity.setReportName("Updated Test Report");
        entity.setReportDescription("Updated test report description");
        entity.setStatus("PUBLISHED");
        entity.setIsPublished(true);
        entity.setUpdatedBy("updated_user");
        return entity;
    }

    /**
     * Creates a published ReportsEntity
     */
    public static ReportsEntity createPublishedReportsEntity() {
        ReportsEntity entity = createTestReportsEntity();
        entity.setReportIdentifier("PUBLISHED_" + generateUniqueId());
        entity.setStatus("PUBLISHED");
        entity.setIsPublished(true);
        entity.setCacheDuration(60);
        return entity;
    }

    /**
     * Creates an archived ReportsEntity
     */
    public static ReportsEntity createArchivedReportsEntity() {
        ReportsEntity entity = createTestReportsEntity();
        entity.setReportIdentifier("ARCHIVED_" + generateUniqueId());
        entity.setStatus("ARCHIVED");
        entity.setIsPublished(false);
        return entity;
    }

    // ========================================
    // ReportColumnEntity Creation Methods
    // ========================================

    /**
     * Creates a basic ReportColumnEntity
     */
    public static ReportColumnEntity createTestReportColumnEntity() {
        ReportColumnEntity column = new ReportColumnEntity();
        column.setCrcId(DEFAULT_COLUMN_ID);
        column.setReportId(DEFAULT_REPORT_ID);
        column.setSourceColumn(DEFAULT_SOURCE_COLUMN);
        column.setDataType(DEFAULT_DATA_TYPE);
        column.setDisplayName(DEFAULT_DISPLAY_NAME);
        column.setFormattingJson(createDefaultFormattingJson());
        column.setIsSortable(true);
        column.setIsFilterable(true);
        column.setIsExportable(true);
        column.setIsVisible(true);
        column.setSortPriority(DEFAULT_SORT_PRIORITY);
        column.setAlignment(DEFAULT_ALIGNMENT);
        column.setCreatedAt(ZonedDateTime.now());
        column.setUpdatedAt(ZonedDateTime.now());
        return column;
    }

    /**
     * Creates a ReportColumnEntity with specific data type
     */
    public static ReportColumnEntity createTestReportColumnEntity(String dataType, String sourceColumn) {
        ReportColumnEntity column = createTestReportColumnEntity();
        column.setCrcId(DEFAULT_COLUMN_ID + 1);
        column.setDataType(dataType);
        column.setSourceColumn(sourceColumn);
        column.setDisplayName(sourceColumn.replace("_", " ").toUpperCase());
        return column;
    }

    /**
     * Creates a list of test report columns
     */
    public static List<ReportColumnEntity> createTestReportColumns(Integer reportId) {
        List<ReportColumnEntity> columns = new ArrayList<>();
        
        ReportColumnEntity idColumn = createTestReportColumnEntity("INTEGER", "id");
        idColumn.setReportId(reportId);
        idColumn.setCrcId(2001);
        idColumn.setDisplayName("ID");
        idColumn.setSortPriority(1);
        columns.add(idColumn);
        
        ReportColumnEntity nameColumn = createTestReportColumnEntity("VARCHAR", "name");
        nameColumn.setReportId(reportId);
        nameColumn.setCrcId(2002);
        nameColumn.setDisplayName("Name");
        nameColumn.setSortPriority(2);
        columns.add(nameColumn);
        
        ReportColumnEntity dateColumn = createTestReportColumnEntity("TIMESTAMP", "created_date");
        dateColumn.setReportId(reportId);
        dateColumn.setCrcId(2003);
        dateColumn.setDisplayName("Created Date");
        dateColumn.setSortPriority(3);
        columns.add(dateColumn);
        
        return columns;
    }

    // ========================================
    // DTO Creation Methods
    // ========================================

    /**
     * Creates a basic ReportDto
     */
    public static ReportDto createTestReportDto() {
        ReportDto dto = new ReportDto();
        dto.setReportId(DEFAULT_REPORT_ID);
        dto.setReportIdentifier(DEFAULT_REPORT_IDENTIFIER);
        dto.setReportName(DEFAULT_REPORT_NAME);
        dto.setReportDescription(DEFAULT_REPORT_DESCRIPTION);
        dto.setConnectionId(DEFAULT_CONNECTION_ID);
        dto.setTableViewName(DEFAULT_TABLE_VIEW_NAME);
        dto.setStatus(DEFAULT_STATUS);
        dto.setReportType(DEFAULT_REPORT_TYPE);
        dto.setCacheDuration(DEFAULT_CACHE_DURATION);
        dto.setMaxRows(DEFAULT_MAX_ROWS);
        dto.setIsPublished(false);
        dto.setIsTab(false);
        dto.setColumns(new ArrayList<>());
        dto.setParameters(new ArrayList<>());
        return dto;
    }

    /**
     * Creates a ReportCreateDto for creation tests
     */
    public static ReportCreateDto createTestReportCreateDto() {
        ReportCreateDto dto = new ReportCreateDto();
        dto.setReportIdentifier("CREATE_" + generateUniqueId());
        dto.setReportName("Create Test Report");
        dto.setReportDescription("Report for creation testing");
        dto.setConnectionId(DEFAULT_CONNECTION_ID);
        dto.setTableViewName(DEFAULT_TABLE_VIEW_NAME);
        dto.setReportType(DEFAULT_REPORT_TYPE);
        dto.setCacheDuration(DEFAULT_CACHE_DURATION);
        dto.setMaxRows(DEFAULT_MAX_ROWS);
        return dto;
    }

    /**
     * Creates a ReportUpdateDto for update tests
     */
    public static ReportUpdateDto createTestReportUpdateDto() {
        ReportUpdateDto dto = new ReportUpdateDto();
        dto.setReportName("Updated Test Report");
        dto.setReportDescription("Updated report description");
        dto.setReportType("TABLE");
        dto.setCacheDuration(120);
        dto.setMaxRows(2000);
        dto.setIsTab(true);
        return dto;
    }

    /**
     * Creates a ReportColumnDto
     */
    public static ReportColumnDto createTestReportColumnDto() {
        ReportColumnDto dto = new ReportColumnDto();
        dto.setCrcId(DEFAULT_COLUMN_ID);
        dto.setSourceColumn(DEFAULT_SOURCE_COLUMN);
        dto.setDataType(DEFAULT_DATA_TYPE);
        dto.setDisplayName(DEFAULT_DISPLAY_NAME);
        // Note: FormattingJson may need specific DTO structure
        dto.setIsSortable(true);
        dto.setIsFilterable(true);
        dto.setIsExportable(true);
        dto.setIsVisible(true);
        dto.setSortPriority(DEFAULT_SORT_PRIORITY);
        dto.setAlignment(DEFAULT_ALIGNMENT);
        return dto;
    }

    /**
     * Creates a ReportParameterDto
     */
    public static ReportParameterDto createTestReportParameterDto() {
        ReportParameterDto dto = new ReportParameterDto();
        dto.setParameterName(DEFAULT_PARAMETER_NAME);
        // Note: ParameterType may be enum, using string for now
        dto.setDefaultValue(DEFAULT_PARAMETER_DEFAULT_VALUE);
        dto.setIsRequired(false);
        return dto;
    }

    /**
     * Creates a ReportQueryRequestDto for execution tests
     */
    public static ReportQueryRequestDto createTestReportQueryRequestDto() {
        ReportQueryRequestDto dto = new ReportQueryRequestDto();
        dto.setUserId(123L);
        dto.setPageNumber(1);
        dto.setPageSize(100);
        // Note: Query filter may need specific structure
        return dto;
    }

    /**
     * Creates a ReportExecutionResultDto for result testing
     */
    public static ReportExecutionResultDto createTestReportExecutionResultDto() {
        ReportExecutionResultDto dto = new ReportExecutionResultDto();
        dto.setReportId(DEFAULT_REPORT_ID);
        dto.setReportName(DEFAULT_REPORT_NAME);
        dto.setExecutionTime(ZonedDateTime.now());
        dto.setTotalRows(50);
        dto.setCurrentPage(1);
        dto.setPageSize(100);
        dto.setTotalPages(1);
        dto.setData(createSampleReportData());
        dto.setColumns(Arrays.asList(createTestReportColumnDto()));
        dto.setIsCached(false);
        return dto;
    }

    // ========================================
    // Helper Methods
    // ========================================

    /**
     * Creates default formatting JSON for columns
     */
    public static Map<String, Object> createDefaultFormattingJson() {
        Map<String, Object> formatting = new HashMap<>();
        formatting.put("format", "default");
        formatting.put("precision", 2);
        formatting.put("showThousandsSeparator", false);
        return formatting;
    }

    /**
     * Creates sample report data for execution results
     */
    public static List<Map<String, Object>> createSampleReportData() {
        List<Map<String, Object>> data = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Map<String, Object> row = new HashMap<>();
            row.put("id", i);
            row.put("name", "Test Item " + i);
            row.put("description", "Description for test item " + i);
            row.put("created_date", ZonedDateTime.now().minusDays(i));
            data.add(row);
        }
        
        return data;
    }

    /**
     * Creates a list of test reports for bulk operations
     */
    public static List<ReportsEntity> createTestReportsList(int count) {
        List<ReportsEntity> reports = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            ReportsEntity entity = createTestReportsEntity();
            entity.setReportId(DEFAULT_REPORT_ID + i);
            entity.setReportIdentifier(DEFAULT_REPORT_IDENTIFIER + "_" + i);
            entity.setReportName(DEFAULT_REPORT_NAME + " " + (i + 1));
            reports.add(entity);
        }
        return reports;
    }

    /**
     * Creates a complete report with columns and parameters
     */
    public static ReportsEntity createCompleteTestReport() {
        ReportsEntity entity = createTestReportsEntity();
        entity.setColumns(createTestReportColumns(entity.getReportId()));
        
        // Add test parameters
        List<ReportParameterEntity> parameters = new ArrayList<>();
        ReportParameterEntity param = new ReportParameterEntity();
        param.setParameterName("status_filter");
        param.setParameterType(com.dair.cais.reports.enums.enums.ParameterType.TEXT);
        param.setDefaultValue("ACTIVE");
        param.setIsRequired(false);
        param.setReport(entity);
        parameters.add(param);
        
        entity.setParameters(parameters);
        return entity;
    }

    /**
     * Creates test report with specific status
     */
    public static ReportsEntity createTestReportWithStatus(String status) {
        ReportsEntity entity = createTestReportsEntity();
        entity.setReportIdentifier(status + "_" + generateUniqueId());
        entity.setStatus(status);
        entity.setIsPublished("PUBLISHED".equals(status));
        return entity;
    }

    /**
     * Creates test report with specific type
     */
    public static ReportsEntity createTestReportWithType(String reportType) {
        ReportsEntity entity = createTestReportsEntity();
        entity.setReportIdentifier(reportType + "_" + generateUniqueId());
        entity.setReportType(reportType);
        return entity;
    }

    /**
     * Creates a ReportSummaryDto for summary tests
     */
    public static ReportSummaryDto createTestReportSummaryDto() {
        return ReportSummaryDto.builder()
                .reportId(DEFAULT_REPORT_ID)
                .reportIdentifier(DEFAULT_REPORT_IDENTIFIER)
                .reportName(DEFAULT_REPORT_NAME)
                .reportDescription(DEFAULT_REPORT_DESCRIPTION)
                .reportType(DEFAULT_REPORT_TYPE)
                .status(DEFAULT_STATUS)
                .isPublished(true)
                .isTab(false)
                .createdBy("testuser")
                .createdAt(ZonedDateTime.now())
                .updatedBy("testuser")
                .updatedAt(ZonedDateTime.now())
                .build();
    }
}