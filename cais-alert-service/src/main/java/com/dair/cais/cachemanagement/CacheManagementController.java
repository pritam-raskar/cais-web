package com.dair.cais.cachemanagement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/cache")
@RequiredArgsConstructor
@Tag(name = "Cache Management", description = "APIs for managing application caches")
public class CacheManagementController {

    private final CacheManagementService cacheManagementService;

    @GetMapping("/summary")
    @Operation(summary = "Get summary of all caches")
    public ResponseEntity<List<CacheSummary>> getCacheSummaries() {
        return ResponseEntity.ok(cacheManagementService.getAllCacheSummaries());
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get detailed cache statistics")
    public ResponseEntity<CacheStatistics> getCacheStatistics() {
        return ResponseEntity.ok(cacheManagementService.getCacheStatistics());
    }

    @GetMapping("/names")
    @Operation(summary = "Get all cache names")
    public ResponseEntity<Set<String>> getCacheNames() {
        return ResponseEntity.ok(cacheManagementService.getAllCacheNames());
    }

    @GetMapping("/{cacheName}")
    @Operation(summary = "Get details of a specific cache")
    public ResponseEntity<CacheInfo> getCacheDetails(@PathVariable String cacheName) {
        return ResponseEntity.ok(cacheManagementService.getCacheDetails(cacheName));
    }

    @GetMapping("/{cacheName}/entries")
    @Operation(summary = "Get entries of a specific cache")
    public ResponseEntity<Map<String, Object>> getCacheEntries(@PathVariable String cacheName) {
        return ResponseEntity.ok(cacheManagementService.getCacheEntries(cacheName));
    }

    @PostMapping("/clear")
    @Operation(summary = "Clear specific caches or cache entries")
    public ResponseEntity<Void> clearCache(@RequestBody CacheClearRequest request) {
        cacheManagementService.clearCache(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/clear-all")
    @Operation(summary = "Clear all caches")
    public ResponseEntity<Void> clearAllCaches() {
        cacheManagementService.clearAllCaches();
        return ResponseEntity.ok().build();
    }
}