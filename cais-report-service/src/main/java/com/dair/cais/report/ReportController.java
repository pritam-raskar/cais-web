package com.dair.cais.report;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/reports")
@Tag(name = "reports")

public class ReportController {

   @Autowired
   private ReportService reportService;

   @PostMapping
   @Operation(summary = "Create a report")
   public ResponseEntity<Report> createReport(@RequestBody Report alert) {
      Report createdReport = reportService.createReport(alert);
      return ResponseEntity.ok().body(createdReport);
   }

   @Hidden
   @PostMapping("/bulk")
   @Operation(summary = "Create bulk reports")
   public ResponseEntity<List<Report>> createReports(@RequestBody List<Report> reports) {
      List<Report> createdReports = reportService.createReports(reports);
      return ResponseEntity.ok().body(createdReports);
   }

   @GetMapping("{reportId}")
   @Operation(summary = "Get a report by its id")
   public ResponseEntity<Report> getReportById(@PathVariable final String reportId) {
      Report reportById = reportService.getReportById(reportId);
      return ResponseEntity.ok().body(reportById);
   }

   @GetMapping("")
   @Operation(summary = "Get all reports; Use query params for search options like offset ,limit ,fuzzy search")
   public ResponseEntity<Map<String, Object>> getAllReports(@RequestParam(required = false) String name,
         @Valid @RequestParam(defaultValue = "0") int offset,
         @Valid @RequestParam(defaultValue = "10") int limit) {
      return ResponseEntity.ok().body(reportService.getAllReports(name, offset, limit));
   }
}
