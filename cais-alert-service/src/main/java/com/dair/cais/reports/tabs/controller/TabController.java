package com.dair.cais.reports.tabs.controller;

import com.dair.cais.reports.tabs.dto.TabCreateRequestDto;
import com.dair.cais.reports.tabs.dto.TabDetailsDto;
import com.dair.cais.reports.tabs.dto.TabSummaryDto;
import com.dair.cais.reports.tabs.service.TabService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing tabs (reports with isTab=true)
 */
@RestController
@RequestMapping("/tabs")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Tab Management API", description = "APIs for managing tabs (specialized reports)")
public class TabController {
    private final TabService tabService;

    /**
     * Creates a new tab
     */
    @Operation(summary = "Create new tab",
            description = "Creates a new tab using the report mechanism with isTab=true")
    @ApiResponse(responseCode = "200", description = "Successfully created tab")
    @ApiResponse(responseCode = "400", description = "Invalid request")
    @PostMapping
    public ResponseEntity<TabDetailsDto> createTab(@Valid @RequestBody TabCreateRequestDto createRequest) {
        log.info("REST request to create tab: {}", createRequest.getReportIdentifier());
        return ResponseEntity.ok(tabService.createTab(createRequest));
    }

    /**
     * Gets all tabs
     */
    @Operation(summary = "Get all tabs",
            description = "Retrieves all reports that are marked as tabs")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved tabs")
    @GetMapping
    public ResponseEntity<List<TabDetailsDto>> getAllTabs() {
        log.info("REST request to get all tabs");
        return ResponseEntity.ok(tabService.getAllTabs());
    }

    /**
     * Gets tab summaries
     */
    @Operation(summary = "Get tab summaries",
            description = "Retrieves basic information about all tabs")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved tab summaries")
    @GetMapping("/summary")
    public ResponseEntity<List<TabSummaryDto>> getTabsSummary() {
        log.info("REST request to get tab summaries");
        return ResponseEntity.ok(tabService.getTabsSummary());
    }
}