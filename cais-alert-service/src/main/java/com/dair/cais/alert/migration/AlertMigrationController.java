package com.dair.cais.alert.migration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/alerts/migration")
@Tag(name = "Alert Data Migration", description = "APIs for alert data migration tasks")
@RequiredArgsConstructor
public class AlertMigrationController {

    private final AlertDataMigrationService migrationService;

    @PostMapping("/org-keys")
    @Operation(summary = "Migrate existing alerts to include org keys",
            description = "Updates all existing alerts in both MongoDB and RDBMS to include organization keys")
    @ApiResponse(responseCode = "200", description = "Migration completed successfully")
    @ApiResponse(responseCode = "500", description = "Error occurred during migration")
    public ResponseEntity<String> migrateOrgKeys() {
        log.info("Received request to migrate org keys for existing alerts");
        try {
            migrationService.migrateOrgKeyData();
            return ResponseEntity.ok("Migration completed successfully");
        } catch (Exception e) {
            log.error("Migration failed: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body("Migration failed: " + e.getMessage());
        }
    }
}