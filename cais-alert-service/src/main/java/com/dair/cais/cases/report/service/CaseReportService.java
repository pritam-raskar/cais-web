package com.dair.cais.cases.report.service;

import com.dair.cais.cases.entity.CaseEntity;
import com.dair.cais.cases.report.CaseReport;
import com.dair.cais.cases.report.CaseStatistics;
import com.dair.cais.cases.repository.CaseRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for case reporting and statistics.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CaseReportService {

    private final CaseRepository caseRepository;

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Generate a case report based on filters.
     *
     * @param filters the report filters
     * @return list of case reports
     */
    @Transactional(readOnly = true)
    public List<CaseReport> generateReport(Map<String, Object> filters) {
        log.debug("Generating case report with filters: {}", filters);

        List<CaseEntity> cases = findCasesWithFilters(filters);
        List<CaseReport> reports = convertToReports(cases);

        log.info("Generated report with {} cases", reports.size());
        return reports;
    }

    /**
     * Export case report to a file format.
     *
     * @param filters the report filters
     * @param format  the export format (e.g., "excel", "csv", "pdf")
     * @return the report file as byte array
     * @throws IllegalArgumentException if format is not supported
     * @throws IOException if export fails
     */
    @Transactional(readOnly = true)
    public byte[] exportReport(Map<String, Object> filters, String format) throws IOException {
        log.debug("Exporting case report with filters: {} in format: {}", filters, format);

        List<CaseReport> reports = generateReport(filters);

        if ("excel".equalsIgnoreCase(format)) {
            return exportToExcel(reports);
        } else if ("csv".equalsIgnoreCase(format)) {
            return exportToCsv(reports);
        } else if ("pdf".equalsIgnoreCase(format)) {
            return exportToPdf(reports);
        } else {
            log.error("Unsupported export format: {}", format);
            throw new IllegalArgumentException("Unsupported export format: " + format);
        }
    }

    /**
     * Get case statistics based on filters.
     *
     * @param filters the statistics filters
     * @return case statistics
     */
    @Transactional(readOnly = true)
    public CaseStatistics getCaseStatistics(Map<String, Object> filters) {
        log.debug("Getting case statistics with filters: {}", filters);

        List<CaseEntity> allCases = findCasesWithFilters(filters);

        CaseStatistics statistics = new CaseStatistics();

        // Calculate basic statistics
        statistics.setTotalCases((long) allCases.size());

        List<CaseEntity> openCases = allCases.stream()
                .filter(c -> !"Closed".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());
        statistics.setOpenCases((long) openCases.size());

        List<CaseEntity> closedCases = allCases.stream()
                .filter(c -> "Closed".equalsIgnoreCase(c.getStatus()))
                .collect(Collectors.toList());
        statistics.setClosedCases((long) closedCases.size());

        // Calculate average resolution time for closed cases
        OptionalDouble avgResolutionHours = closedCases.stream()
                .filter(c -> c.getCreatedAt() != null && c.getClosedAt() != null)
                .mapToLong(c -> ChronoUnit.HOURS.between(c.getCreatedAt(), c.getClosedAt()))
                .average();

        if (avgResolutionHours.isPresent()) {
            statistics.setAverageResolutionTime(Duration.ofHours(Math.round(avgResolutionHours.getAsDouble())));
        }

        // Group cases by various dimensions
        statistics.setCasesByStatus(groupAndCount(allCases, CaseEntity::getStatus));
        statistics.setCasesByPriority(groupAndCount(allCases, CaseEntity::getPriority));
        statistics.setCasesByType(groupAndCount(allCases, CaseEntity::getCaseType));
        statistics.setCasesByOrgUnit(groupAndCount(allCases, CaseEntity::getOrgUnitId));
        statistics.setCasesByOwner(groupAndCount(allCases, CaseEntity::getOwnerName));

        // Calculate SLA meet percentage (example implementation)
        long casesWithSla = allCases.stream()
                .filter(c -> c.getDueDate() != null)
                .count();

        if (casesWithSla > 0) {
            long casesMeetingSla = allCases.stream()
                    .filter(c -> c.getDueDate() != null &&
                            (c.getClosedAt() != null && c.getClosedAt().isBefore(c.getDueDate())))
                    .count();

            statistics.setSlaMeetPercentage((double) casesMeetingSla / casesWithSla * 100);
        }

        // Time-based metrics
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime startOfWeek = now.toLocalDate().minusDays(now.getDayOfWeek().getValue() - 1).atStartOfDay();
        LocalDateTime startOfMonth = now.toLocalDate().withDayOfMonth(1).atStartOfDay();

        statistics.setCasesCreatedToday(countCasesCreatedBetween(allCases, startOfDay, now));
        statistics.setCasesCreatedThisWeek(countCasesCreatedBetween(allCases, startOfWeek, now));
        statistics.setCasesCreatedThisMonth(countCasesCreatedBetween(allCases, startOfMonth, now));

        statistics.setCasesClosedToday(countCasesClosedBetween(allCases, startOfDay, now));
        statistics.setCasesClosedThisWeek(countCasesClosedBetween(allCases, startOfWeek, now));
        statistics.setCasesClosedThisMonth(countCasesClosedBetween(allCases, startOfMonth, now));

        log.info("Generated case statistics");
        return statistics;
    }

    /**
     * Get dashboard data for case overview.
     *
     * @return dashboard data
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getCaseDashboardData() {
        log.debug("Getting case dashboard data");

        Map<String, Object> dashboardData = new HashMap<>();

        // Get basic statistics with no filters
        CaseStatistics statistics = getCaseStatistics(Collections.emptyMap());
        dashboardData.put("statistics", statistics);

        // Get recent cases
        /*List<CaseEntity> recentCases = caseRepository.findTop10ByOrderByCreatedAtDesc();
        dashboardData.put("recentCases", convertToReports(recentCases));

        // Get overdue cases
        List<CaseEntity> overdueCases = caseRepository.findByStatusNotAndDueDateBeforeOrderByDueDateAsc(
                "Closed", LocalDateTime.now());
        dashboardData.put("overdueCases", convertToReports(overdueCases));

        // Calculate trend data (example: cases by month for last 6 months)
        Map<String, Long> casesByMonth = new LinkedHashMap<>();
        LocalDate now = LocalDate.now();

        for (int i = 5; i >= 0; i--) {
            LocalDate monthStart = now.minusMonths(i).withDayOfMonth(1);
            LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);

            List<CaseEntity> casesInMonth = caseRepository.findByCreatedAtBetween(
                    monthStart.atStartOfDay(), monthEnd.atTime(23, 59, 59));

            String monthLabel = monthStart.getMonth().toString() + " " + monthStart.getYear();
            casesByMonth.put(monthLabel, (long) casesInMonth.size());
        }

        dashboardData.put("casesByMonth", casesByMonth);*/

        log.info("Generated dashboard data");
        return dashboardData;
    }

    /**
     * Find cases with filters using criteria API.
     *
     * @param filters the filters to apply
     * @return list of filtered cases
     */
    private List<CaseEntity> findCasesWithFilters(Map<String, Object> filters) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<CaseEntity> query = cb.createQuery(CaseEntity.class);
        Root<CaseEntity> root = query.from(CaseEntity.class);

        List<Predicate> predicates = new ArrayList<>();

        // Apply filters
        if (filters != null) {
            if (filters.containsKey("status")) {
                predicates.add(cb.equal(root.get("status"), filters.get("status")));
            }

            if (filters.containsKey("priority")) {
                predicates.add(cb.equal(root.get("priority"), filters.get("priority")));
            }

            if (filters.containsKey("caseType")) {
                predicates.add(cb.equal(root.get("caseType"), filters.get("caseType")));
            }

            if (filters.containsKey("ownerId")) {
                predicates.add(cb.equal(root.get("ownerId"), filters.get("ownerId")));
            }

            if (filters.containsKey("orgUnitId")) {
                predicates.add(cb.equal(root.get("orgUnitId"), filters.get("orgUnitId")));
            }

            if (filters.containsKey("startDate") && filters.get("startDate") instanceof LocalDateTime) {
                predicates.add(cb.greaterThanOrEqualTo(
                        root.get("createdAt"), (LocalDateTime) filters.get("startDate")));
            }

            if (filters.containsKey("endDate") && filters.get("endDate") instanceof LocalDateTime) {
                predicates.add(cb.lessThanOrEqualTo(
                        root.get("createdAt"), (LocalDateTime) filters.get("endDate")));
            }

            if (filters.containsKey("isActive")) {
                predicates.add(cb.equal(root.get("isActive"), filters.get("isActive")));
            }
        }

        if (!predicates.isEmpty()) {
            query.where(cb.and(predicates.toArray(new Predicate[0])));
        }

        return entityManager.createQuery(query).getResultList();
    }

    /**
     * Convert case entities to case reports.
     *
     * @param cases the case entities
     * @return list of case reports
     */
    private List<CaseReport> convertToReports(List<CaseEntity> cases) {
        return cases.stream()
                .map(this::convertToReport)
                .collect(Collectors.toList());
    }

    /**
     * Convert a case entity to a case report.
     *
     * @param entity the case entity
     * @return the case report
     */
    private CaseReport convertToReport(CaseEntity entity) {
        CaseReport report = new CaseReport();
        report.setCaseId(entity.getCaseId());
        report.setCaseNumber(entity.getCaseNumber());
        report.setTitle(entity.getTitle());
        report.setStatus(entity.getStatus());
        report.setPriority(entity.getPriority());
        report.setCaseType(entity.getCaseType());
        report.setOrgUnitId(entity.getOrgUnitId());
        report.setOrgFamily(entity.getOrgFamily());
        report.setOwnerId(entity.getOwnerId());
        report.setOwnerName(entity.getOwnerName());
        report.setCreatedAt(entity.getCreatedAt());
        report.setUpdatedAt(entity.getUpdatedAt());
        report.setClosedAt(entity.getClosedAt());
        report.setResolution(entity.getResolution());
        report.setCurrentStepName(entity.getCurrentStepName());

        // Calculate days open
        if (entity.getCreatedAt() != null) {
            LocalDateTime endDate = entity.getClosedAt() != null ? entity.getClosedAt() : LocalDateTime.now();
            report.setDaysOpen((int) ChronoUnit.DAYS.between(entity.getCreatedAt(), endDate));

            // Calculate resolution time for closed cases
            if (entity.getClosedAt() != null) {
                report.setResolutionTime(Duration.between(entity.getCreatedAt(), entity.getClosedAt()));
            }
        }

        // Determine if case is within SLA (example implementation)
        if (entity.getDueDate() != null) {
            if (entity.getClosedAt() != null) {
                report.setIsWithinSla(entity.getClosedAt().isBefore(entity.getDueDate()));
            } else {
                report.setIsWithinSla(LocalDateTime.now().isBefore(entity.getDueDate()));
            }
        }

        return report;
    }

    /**
     * Group and count cases by a property.
     *
     * @param cases    the list of cases
     * @param extractor the property extractor
     * @return map of property values to counts
     */
    private <T> Map<String, Long> groupAndCount(List<CaseEntity> cases, java.util.function.Function<CaseEntity, T> extractor) {
        return cases.stream()
                .collect(Collectors.groupingBy(
                        case1 -> {
                            T value = extractor.apply(case1);
                            return value != null ? value.toString() : "Unknown";
                        },
                        Collectors.counting()
                ));
    }

    /**
     * Count cases created between dates.
     *
     * @param cases     the list of cases
     * @param startDate the start date
     * @param endDate   the end date
     * @return count of cases created between dates
     */
    private Long countCasesCreatedBetween(List<CaseEntity> cases, LocalDateTime startDate, LocalDateTime endDate) {
        return cases.stream()
                .filter(c -> c.getCreatedAt() != null &&
                        !c.getCreatedAt().isBefore(startDate) &&
                        !c.getCreatedAt().isAfter(endDate))
                .count();
    }

    /**
     * Count cases closed between dates.
     *
     * @param cases     the list of cases
     * @param startDate the start date
     * @param endDate   the end date
     * @return count of cases closed between dates
     */
    private Long countCasesClosedBetween(List<CaseEntity> cases, LocalDateTime startDate, LocalDateTime endDate) {
        return cases.stream()
                .filter(c -> c.getClosedAt() != null &&
                        !c.getClosedAt().isBefore(startDate) &&
                        !c.getClosedAt().isAfter(endDate))
                .count();
    }

    /**
     * Export report to Excel format.
     *
     * @param reports the list of case reports
     * @return Excel file as byte array
     * @throws IOException if export fails
     */
    private byte[] exportToExcel(List<CaseReport> reports) throws IOException {
        log.debug("Exporting {} cases to Excel", reports.size());

        /*try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Case Report");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {
                    "Case ID", "Case Number", "Title", "Status", "Priority", "Case Type",
                    "Owner", "Created Date", "Closed Date", "Days Open", "Resolution"
            };

            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
            }

            // Create data rows
            int rowNum = 1;
            for (CaseReport report : reports) {
                Row row = sheet.createRow(rowNum++);

                row.createCell(0).setCellValue(report.getCaseId() != null ? report.getCaseId() : 0);
                row.createCell(1).setCellValue(report.getCaseNumber() != null ? report.getCaseNumber() : "");
                row.createCell(2).setCellValue(report.getTitle() != null ? report.getTitle() : "");
                row.createCell(3).setCellValue(report.getStatus() != null ? report.getStatus() : "");
                row.createCell(4).setCellValue(report.getPriority() != null ? report.getPriority() : "");
                row.createCell(5).setCellValue(report.getCaseType() != null ? report.getCaseType() : "");
                row.createCell(6).setCellValue(report.getOwnerName() != null ? report.getOwnerName() : "");
                row.createCell(7).setCellValue(report.getCreatedAt() != null ? report.getCreatedAt().toString() : "");
                row.createCell(8).setCellValue(report.getClosedAt() != null ? report.getClosedAt().toString() : "");
                row.createCell(9).setCellValue(report.getDaysOpen() != null ? report.getDaysOpen() : 0);
                row.createCell(10).setCellValue(report.getResolution() != null ? report.getResolution() : "");
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }*/
        return new ByteArrayOutputStream().toByteArray();
    }

    /**
     * Export report to CSV format.
     *
     * @param reports the list of case reports
     * @return CSV file as byte array
     */
    private byte[] exportToCsv(List<CaseReport> reports) {
        log.debug("Exporting {} cases to CSV", reports.size());

        StringBuilder csv = new StringBuilder();

        // Add header
        csv.append("Case ID,Case Number,Title,Status,Priority,Case Type,Owner,Created Date,Closed Date,Days Open,Resolution\n");

        // Add data rows
        for (CaseReport report : reports) {
            csv.append(report.getCaseId() != null ? report.getCaseId() : "").append(",");
            csv.append(escapeForCsv(report.getCaseNumber())).append(",");
            csv.append(escapeForCsv(report.getTitle())).append(",");
            csv.append(escapeForCsv(report.getStatus())).append(",");
            csv.append(escapeForCsv(report.getPriority())).append(",");
            csv.append(escapeForCsv(report.getCaseType())).append(",");
            csv.append(escapeForCsv(report.getOwnerName())).append(",");
            csv.append(report.getCreatedAt() != null ? report.getCreatedAt() : "").append(",");
            csv.append(report.getClosedAt() != null ? report.getClosedAt() : "").append(",");
            csv.append(report.getDaysOpen() != null ? report.getDaysOpen() : "").append(",");
            csv.append(escapeForCsv(report.getResolution())).append("\n");
        }

        return csv.toString().getBytes();
    }

    /**
     * Escape a string for CSV format.
     *
     * @param value the string to escape
     * @return escaped string
     */
    private String escapeForCsv(String value) {
        if (value == null) {
            return "";
        }

        // Escape quotes and wrap in quotes if needed
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    /**
     * Export report to PDF format.
     * Note: This is a placeholder implementation, actual PDF generation
     * would typically use a library like iText or OpenPDF.
     *
     * @param reports the list of case reports
     * @return PDF file as byte array
     * @throws IOException if export fails
     */
    private byte[] exportToPdf(List<CaseReport> reports) throws IOException {
        log.debug("Exporting {} cases to PDF", reports.size());

        // Placeholder implementation - in a real application, you would use a PDF library
        // For now, we'll just return a basic text representation
        StringBuilder text = new StringBuilder();
        text.append("Case Report\n\n");

        for (CaseReport report : reports) {
            text.append("Case ID: ").append(report.getCaseId()).append("\n");
            text.append("Case Number: ").append(report.getCaseNumber()).append("\n");
            text.append("Title: ").append(report.getTitle()).append("\n");
            text.append("Status: ").append(report.getStatus()).append("\n");
            text.append("Priority: ").append(report.getPriority()).append("\n");
            text.append("Case Type: ").append(report.getCaseType()).append("\n");
            text.append("Owner: ").append(report.getOwnerName()).append("\n");
            text.append("Created: ").append(report.getCreatedAt()).append("\n");
            text.append("Closed: ").append(report.getClosedAt()).append("\n");
            text.append("Days Open: ").append(report.getDaysOpen()).append("\n");
            text.append("Resolution: ").append(report.getResolution()).append("\n");
            text.append("\n");
        }

        log.warn("PDF export is a placeholder implementation");
        return text.toString().getBytes();
    }
}