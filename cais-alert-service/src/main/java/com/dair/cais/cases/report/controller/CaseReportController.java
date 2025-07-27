package com.dair.cais.cases.report.controller;

import com.dair.cais.cases.report.CaseReport;
import com.dair.cais.cases.report.CaseStatistics;
import com.dair.cais.cases.report.service.CaseReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * REST controller for case reporting and statistics.
 */
@Slf4j
@RestController
@RequestMapping("/case-reports")
@Tag(name = "Case Reporting", description = "APIs for case reporting and statistics")
@RequiredArgsConstructor
public class CaseReportController {

    private final CaseReportService caseReportService;

    /**
     * Generate a case report based on filters.
     *
     * @param filters the report filters
     * @return list of case reports
     */
    @PostMapping("/generate")
    @Operation(summary = "Generate a case report")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report generated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<CaseReport>> generateReport(
            @Parameter(description = "Report filters", required = true)
            @RequestBody Map<String, Object> filters) {
        log.info("REST request to generate case report with filters: {}", filters);

        try {
            List<CaseReport> reports = caseReportService.generateReport(filters);
            return ResponseEntity.ok(reports);
        } catch (IllegalArgumentException e) {
            log.error("Invalid filter parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error generating report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Export a case report to a file.
     *
     * @param filters the report filters
     * @param format  the export format
     * @return the report file as a resource
     */
    @PostMapping("/export")
    @Operation(summary = "Export a case report to a file")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report exported successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters or format"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Resource> exportReport(
            @Parameter(description = "Report filters", required = true)
            @RequestBody Map<String, Object> filters,
            @Parameter(description = "Export format (excel, csv, pdf)", required = true)
            @RequestParam String format) {
        log.info("REST request to export case report with filters: {} in format: {}", filters, format);

        try {
            byte[] reportData = caseReportService.exportReport(filters, format);

            ByteArrayResource resource = new ByteArrayResource(reportData);

            // Set content type and headers based on format
            MediaType contentType;
            String fileExtension;

            if ("excel".equalsIgnoreCase(format)) {
                contentType = MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                fileExtension = ".xlsx";
            } else if ("csv".equalsIgnoreCase(format)) {
                contentType = MediaType.parseMediaType("text/csv");
                fileExtension = ".csv";
            } else if ("pdf".equalsIgnoreCase(format)) {
                contentType = MediaType.APPLICATION_PDF;
                fileExtension = ".pdf";
            } else {
                return ResponseEntity.badRequest().build();
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "case_report_" + timestamp + fileExtension;

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                    .body(resource);

        } catch (IllegalArgumentException e) {
            log.error("Invalid parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (IOException e) {
            log.error("Error exporting report: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("Error exporting report", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get case statistics based on filters.
     *
     * @param filters the statistics filters
     * @return case statistics
     */
    @PostMapping("/statistics")
    @Operation(summary = "Get case statistics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CaseStatistics.class))),
            @ApiResponse(responseCode = "400", description = "Invalid filter parameters"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<CaseStatistics> getCaseStatistics(
            @Parameter(description = "Statistics filters", required = false)
            @RequestBody Map<String, Object> filters) {
        log.info("REST request to get case statistics with filters: {}", filters);

        try {
            CaseStatistics statistics = caseReportService.getCaseStatistics(filters);
            return ResponseEntity.ok(statistics);
        } catch (IllegalArgumentException e) {
            log.error("Invalid filter parameters: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("Error retrieving statistics", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get dashboard data for case overview.
     *
     * @return dashboard data
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Get case dashboard data")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<Map<String, Object>> getCaseDashboardData() {
        log.info("REST request to get case dashboard data");

        try {
            Map<String, Object> dashboardData = caseReportService.getCaseDashboardData();
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            log.error("Error retrieving dashboard data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}