package com.dair.cais.reports;

import com.dair.cais.reports.dto.*;
import com.dair.cais.reports.exception.ReportRetrievalException;
import com.dair.cais.reports.mapper.ReportSummaryMapper;
import com.dair.cais.reports.repository.ReportsRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Report Designer API", description = "APIs for managing and executing reports")
public class ReportDesignerController {
    private final ReportDesignerService reportService;
    private final ReportMetadataService metadataService;
    private final ReportsRepository reportRepository;
    private final ReportSummaryMapper reportSummaryMapper;

    /**
     * Get all reports
     */
    @Operation(summary = "Get all reports",
            description = "Retrieves all available reports without pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all reports",
                    content = @Content(schema = @Schema(implementation = List.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    })
    @GetMapping
    public ResponseEntity<List<ReportDto>> getAllReports() {
        log.debug("REST request to get all reports");

        try {
            List<ReportDto> reports = reportService.getAllReports();
            log.debug("Retrieved {} reports successfully", reports.size());
            return ResponseEntity.ok(reports);
        } catch (Exception e) {
            log.error("Failed to retrieve reports", e);
            throw new ReportRetrievalException("Failed to retrieve reports", e);
        }
    }

    @Operation(summary = "Create new report")
    @PostMapping
    public ResponseEntity<ReportDto> createReport(
            @Valid @RequestBody ReportCreateDto createDto) {
        log.info("REST request to create report: {}", createDto.getReportName());
        return ResponseEntity.ok(reportService.createReport(createDto));
    }

    @Operation(summary = "Update existing report")
    @PutMapping("/{reportId}")
    public ResponseEntity<ReportDto> updateReport(
            @PathVariable Integer reportId,
            @Valid @RequestBody ReportUpdateDto updateDto) {
        log.info("REST request to update report: {}", reportId);
        return ResponseEntity.ok(reportService.updateReport(reportId, updateDto));
    }



    @Operation(summary = "Get report by ID")
    @GetMapping("/{reportId}")
    public ResponseEntity<ReportDto> getReport(@PathVariable Integer reportId) {
        log.info("REST request to get report: {}", reportId);
        return ResponseEntity.ok(reportService.getReport(reportId));
    }

    @Operation(summary = "Delete report")
    @DeleteMapping("/{reportId}")
    public ResponseEntity<Void> deleteReport(@PathVariable Integer reportId) {
        log.info("REST request to delete report: {}", reportId);
        reportService.deleteReport(reportId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search reports with filters")
    @GetMapping("/search")
    public ResponseEntity<Page<ReportDto>> searchReports(
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Boolean isPublished,
            @Parameter(description = "Pagination parameters") Pageable pageable) {
        log.info("REST request to search reports");
        return ResponseEntity.ok(reportService.searchReports(searchTerm, status, isPublished, pageable));
    }

    @Operation(summary = "Publish report")
    @PostMapping("/{reportId}/publish")
    public ResponseEntity<ReportDto> publishReport(@PathVariable Integer reportId) {
        log.info("REST request to publish report: {}", reportId);
        return ResponseEntity.ok(reportService.publishReport(reportId));
    }

    @Operation(summary = "Execute report")
    @PostMapping("/{reportId}/execute")
    public ResponseEntity<ReportExecutionResultDto> executeReport(
            @PathVariable Integer reportId,
            @RequestBody(required = false) Map<String, Object> parameters) {
        log.info("REST request to execute report: {}", reportId);
        return ResponseEntity.ok(reportService.executeReport(reportId, parameters));
    }

//    @Operation(summary = "Export report")
//    @PostMapping("/{reportId}/export/{format}")
//    public ResponseEntity<Resource> exportReport(
//            @PathVariable Integer reportId,
//            @PathVariable String format,
//            @RequestBody(required = false) Map<String, Object> parameters) {
//        log.info("REST request to export report: {} in format: {}", reportId, format);
//
//        byte[] exportData = reportService.exportReport(reportId, format, parameters);
//        ByteArrayResource resource = new ByteArrayResource(exportData);
//
//        String filename = String.format("report_%d.%s", reportId, format.toLowerCase());
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.APPLICATION_OCTET_STREAM)
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=\"" + filename + "\"")
//                .body(resource);
//    }

    // Metadata endpoints
    @Operation(summary = "Get available tables for connection")
    @GetMapping("/metadata/tables/{connectionId}")
    public ResponseEntity<List<TableMetadataDto>> getAvailableTables(
            @PathVariable Long connectionId) {
        log.info("REST request to get available tables for connection: {}", connectionId);
        return ResponseEntity.ok(metadataService.getTableMetadata(connectionId));
    }

    @Operation(summary = "Get columns for table")
    @GetMapping("/metadata/tables/columns/{connectionId}/{tableSchema}/{tableName}")
    public ResponseEntity<List<ColumnMetadataDto>> getTableColumns(
            @PathVariable Long connectionId,
            @PathVariable String tableSchema,
            @PathVariable String tableName
            ) {
        log.info("REST request to get columns for table: {}", tableName);
        return ResponseEntity.ok(metadataService.getColumnMetadata(connectionId, tableSchema , tableName));
    }

    @Operation(summary = "Update column metadata")
    @PutMapping("/metadata/tables/{connectionId}/{tableName}/columns")
    public ResponseEntity<Void> updateColumnMetadata(
            @PathVariable Long connectionId,
            @PathVariable String tableName,
            @Valid @RequestBody List<ColumnMetadataUpdateDto> updates) {
        log.info("REST request to update column metadata for table: {}", tableName);
        metadataService.updateColumnMetadata(connectionId, tableName, updates);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get formatting suggestions")
    @GetMapping("/metadata/formatting/{dataType}")
    public ResponseEntity<ColumnFormattingDto> getFormattingSuggestions(
            @PathVariable String dataType) {
        log.info("REST request to get formatting suggestions for data type: {}", dataType);
        return ResponseEntity.ok(metadataService.getFormattingSuggestions(dataType));
    }

    // Schema endpoints
    @Operation(summary = "Get database schemas")
    @GetMapping("/metadata/schemas/{connectionId}")
    public ResponseEntity<List<DatabaseSchemaDto>> getDatabaseSchemas(
            @PathVariable Long connectionId) {
        log.info("REST request to get database schemas for connection: {}", connectionId);
        return ResponseEntity.ok(metadataService.getDatabaseSchemas(connectionId));
    }

    @Operation(summary = "Get schema details")
    @GetMapping("/metadata/schemas/{connectionId}/{schemaName}")
    public ResponseEntity<DatabaseSchemaDto> getSchemaDetails(
            @PathVariable Long connectionId,
            @PathVariable String schemaName) {
        log.info("REST request to get schema details: {}", schemaName);
        return ResponseEntity.ok(metadataService.getSchemaDetails(connectionId, schemaName));
    }

    @Operation(summary = "Get all reports with metadata",
            description = "Retrieves a list of all reports with basic information and system-wide report statistics")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved reports",
            content = @Content(schema = @Schema(implementation = ReportListResponseDto.class)))
    @ApiResponse(responseCode = "500", description = "Internal server error occurred")
    @GetMapping("/summary")
    public ResponseEntity<ReportListResponseDto> getReportsSummary() {
        log.debug("REST request to get all reports summary");

        try {
            // Get report statistics
            Map<String, Object> stats = reportRepository.getReportStatistics();

            // Build metadata
            ReportMetadataStatsDto metadata = ReportMetadataStatsDto.builder()
                    .totalReports(((Number) stats.get("totalReports")).longValue())
                    .publishedReports(((Number) stats.get("publishedReports")).longValue())
                    .draftReports(((Number) stats.get("draftReports")).longValue())
                    .archivedReports(((Number) stats.get("archivedReports")).longValue())
                    .lastUpdated((ZonedDateTime) stats.get("lastUpdated"))
                    .build();

            // Get all reports and map to summary DTOs
            List<ReportSummaryDto> reports = reportRepository.findAllReportsBasicInfo()
                    .stream()
                    .map(reportSummaryMapper::toSummaryDto)
                    .toList();

            // Build final response
            ReportListResponseDto response = ReportListResponseDto.builder()
                    .reports(reports)
                    .metadata(metadata)
                    .build();

            log.debug("Retrieved {} reports with metadata", reports.size());

            // Return response with cache control
            return ResponseEntity.ok()
                    .cacheControl(CacheControl.maxAge(5, TimeUnit.MINUTES))
                    .body(response);

        } catch (Exception e) {
            log.error("Failed to retrieve reports summary", e);
            throw new ReportRetrievalException("Failed to retrieve reports summary", e);
        }
    }
}

