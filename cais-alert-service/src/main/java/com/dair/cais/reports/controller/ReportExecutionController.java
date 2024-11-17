package com.dair.cais.reports.controller;

import com.dair.cais.reports.dto.*;
import com.dair.cais.reports.service.ReportExecutionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Report Execution API")
public class ReportExecutionController {

    private final ReportExecutionService executionService;

    @Operation(summary = "Execute report with dynamic filters")
    @PostMapping("/execute/{reportId}")
    public ResponseEntity<ReportExecutionResultDto> executeReport(
            @PathVariable Integer reportId,
            @Validated @RequestBody ReportQueryRequestDto request) {

        log.info("Executing report {} with filters for user {}", reportId, request.getUserId());
        ReportExecutionResultDto result = executionService.executeReport(reportId, request);
        return ResponseEntity.ok(result);
    }
}

