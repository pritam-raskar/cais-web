// File: com/dair/cais/filter/controller/UserFilterController.java
package com.dair.cais.filter.controller;

import com.dair.cais.filter.domain.FilterEntityType;
import com.dair.cais.filter.dto.*;
import com.dair.cais.filter.exception.FilterNotFoundException;
import com.dair.cais.filter.repository.FilterEntityTypeRepository;
import com.dair.cais.filter.service.UserFilterService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing user-specific filters.
 * Provides endpoints for CRUD operations and filter management.
 */
@Slf4j
@Validated
@RestController
@RequestMapping("/api/filters")
@RequiredArgsConstructor
@Tag(name = "User Filters", description = "APIs for managing user-specific filters")
public class UserFilterController {

    private final UserFilterService userFilterService;
    private final FilterEntityTypeRepository filterEntityTypeRepository;

    @Operation(summary = "Get all filters")
    @GetMapping
    public ResponseEntity<List<FilterResponseDto>> getAllFilters() {
        log.debug("REST request to get all filters");
        return ResponseEntity.ok(userFilterService.getAllFilters());
    }

    @Operation(summary = "Create a new filter")
    @PostMapping
    public ResponseEntity<FilterResponseDto> createFilter(
            @Parameter(description = "User ID", required = true)
            @RequestParam String userId,
            @Valid @RequestBody UserFilterCreateDto createDto) {
        log.debug("REST request to create new filter for user: {}", userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userFilterService.createFilter(userId, createDto));
    }

    @Operation(summary = "Update an existing filter")
    @PutMapping("/{filterId}")
    public ResponseEntity<FilterResponseDto> updateFilter(
            @Parameter(description = "User ID", required = true)
            @RequestParam String userId,
            @Parameter(description = "Filter ID", required = true)
            @PathVariable Long filterId,
            @Valid @RequestBody UserFilterUpdateDto updateDto) {
        log.debug("REST request to update filter: {} for user: {}", filterId, userId);
        return ResponseEntity.ok(userFilterService.updateFilter(userId, filterId, updateDto));
    }

    @Operation(summary = "Delete a filter")
    @DeleteMapping("/{filterId}")
    public ResponseEntity<Void> deleteFilter(
            @Parameter(description = "User ID", required = true)
            @RequestParam String userId,
            @Parameter(description = "Filter ID", required = true)
            @PathVariable Long filterId) {
        log.debug("REST request to delete filter: {} for user: {}", filterId, userId);
        userFilterService.deleteFilter(userId, filterId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get filter by ID")
    @GetMapping("/{filterId}")
    public ResponseEntity<FilterResponseDto> getFilter(
            @Parameter(description = "User ID", required = true)
            @RequestParam String userId,
            @Parameter(description = "Filter ID", required = true)
            @PathVariable Long filterId) {
        log.debug("REST request to get filter: {} for user: {}", filterId, userId);
        return ResponseEntity.ok(userFilterService.getFilter(userId, filterId));
    }

    @Operation(summary = "Get user's filters for an entity")
    @GetMapping("/user")
    public ResponseEntity<List<FilterResponseDto>> getUserFilters(
            @Parameter(description = "User ID", required = true)
            @RequestParam String userId,
            @Parameter(description = "Entity Identifier", required = true)
            @RequestParam String entityIdentifier) {
        log.debug("REST request to get filters for user: {} and entity identifier: {}",
                userId, entityIdentifier);

        FilterEntityType entityType = filterEntityTypeRepository
                .findByEntityNameIgnoreCase(entityIdentifier)
                .orElseThrow(() -> new FilterNotFoundException("Entity type not found: " + entityIdentifier));

        return ResponseEntity.ok(userFilterService.getUserFilters(
                userId,
                entityType.getEntityTypeId(),
                entityIdentifier));
    }

    @Operation(summary = "Get public filters for an entity")
    @GetMapping("/public")
    public ResponseEntity<List<FilterResponseDto>> getPublicFilters(
            @Parameter(description = "Entity Identifier", required = true)
            @RequestParam String entityIdentifier) {
        log.debug("REST request to get public filters for entity identifier: {}",
                entityIdentifier);

        FilterEntityType entityType = filterEntityTypeRepository
                .findByEntityNameIgnoreCase(entityIdentifier)
                .orElseThrow(() -> new FilterNotFoundException("Entity type not found: " + entityIdentifier));

        return ResponseEntity.ok(userFilterService.getPublicFilters(
                entityType.getEntityTypeId(),
                entityIdentifier));
    }

    @Operation(summary = "Search filters")
    @GetMapping("/search")
    public ResponseEntity<Page<FilterSummaryDto>> searchFilters(
            @Parameter(description = "Search term")
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(size = 20) Pageable pageable) {
        log.debug("REST request to search filters with term: {}", searchTerm);
        return ResponseEntity.ok(userFilterService.searchFilters(searchTerm, pageable));
    }

    @Operation(summary = "Set filter as default")
    @PutMapping("/{filterId}/default")
    public ResponseEntity<FilterResponseDto> setDefaultFilter(
            @Parameter(description = "User ID", required = true)
            @RequestParam String userId,
            @Parameter(description = "Filter ID", required = true)
            @PathVariable Long filterId) {
        log.debug("REST request to set filter: {} as default for user: {}", filterId, userId);
        return ResponseEntity.ok(userFilterService.setDefaultFilter(userId, filterId));
    }

    @Operation(summary = "Toggle filter public access")
    @PutMapping("/{filterId}/public")
    public ResponseEntity<FilterResponseDto> togglePublicAccess(
            @Parameter(description = "User ID", required = true)
            @RequestParam String userId,
            @Parameter(description = "Filter ID", required = true)
            @PathVariable Long filterId) {
        log.debug("REST request to toggle public access for filter: {} by user: {}",
                filterId, userId);
        return ResponseEntity.ok(userFilterService.togglePublicAccess(userId, filterId));
    }

    @Operation(summary = "Copy a filter")
    @PostMapping("/{filterId}/copy")
    public ResponseEntity<FilterResponseDto> copyFilter(
            @Parameter(description = "User ID", required = true)
            @RequestParam String userId,
            @Parameter(description = "Filter ID", required = true)
            @PathVariable Long filterId,
            @Parameter(description = "New filter name", required = true)
            @RequestParam String newName) {
        log.debug("REST request to copy filter: {} for user: {} with new name: {}",
                filterId, userId, newName);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(userFilterService.copyFilter(userId, filterId, newName));
    }
}