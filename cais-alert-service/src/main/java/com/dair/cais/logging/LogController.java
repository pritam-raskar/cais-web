package com.dair.cais.logging;
import com.dair.cais.logging.LogResponse;
import com.dair.cais.logging.LogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor
@Validated
@Tag(name = "Logs Management", description = "APIs for managing application logs")
public class LogController {

    private final LogService logService;

    @Value("${logging.file.name}")
    private String logFilePath;

    @GetMapping
    @Operation(summary = "Get application logs with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved logs"),
            @ApiResponse(responseCode = "403", description = "Access denied"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LogResponse> getLogs(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "100") @Min(1) int limit) {

        log.debug("Fetching logs with page: {} and limit: {}", page, limit);

        // Force an initial update of in-memory logs
        logService.updateInMemoryLogs();

        LogResponse response = logService.getLogs(page, limit);

        if (response.getLogs().isEmpty() && page > 0) {
            // If requested page is empty but not first page, get last available page
            response = logService.getLogs(0, limit);
        }

        return ResponseEntity.ok(response);
    }

    @GetMapping("/health")
    @Operation(summary = "Check if log file is accessible")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> checkLogFileHealth() {
        Map<String, Object> health = new HashMap<>();
        health.put("timestamp", LocalDateTime.now());

        try {
            Path logFile = Paths.get(logFilePath);
            boolean exists = Files.exists(logFile);
            boolean readable = Files.isReadable(logFile);
            long size = exists ? Files.size(logFile) : -1;

            health.put("exists", exists);
            health.put("readable", readable);
            health.put("size", size);
            health.put("path", logFilePath);
            health.put("status", exists && readable ? "UP" : "DOWN");

            return ResponseEntity.ok(health);
        } catch (Exception e) {
            log.error("Error checking log file health: {}", e.getMessage(), e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(health);
        }
    }

    @GetMapping("/test")
    //@PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> testLogging() {
        log.trace("Test trace message");
        log.debug("Test debug message");
        log.info("Test info message");
        log.warn("Test warning message");
        try {
            throw new RuntimeException("Test exception");
        } catch (Exception e) {
            log.error("Test error message", e);
        }
        return ResponseEntity.ok("Test logs generated");
    }
}