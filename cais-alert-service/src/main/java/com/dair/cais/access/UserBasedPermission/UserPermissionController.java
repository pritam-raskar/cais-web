package com.dair.cais.access.UserBasedPermission;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
public class UserPermissionController {

    private final UserPermissionService userPermissionService;

    @PostMapping("/user/refresh/{userId}")
    public ResponseEntity<String> refreshPermissions(@PathVariable String userId) {
        try {
            userPermissionService.refreshUserPermissions(userId);
            return ResponseEntity.ok("Permissions refreshed successfully");
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Failed to refresh permissions: " + e.getMessage());
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Prepare User permission from RDBMS ")
    public ResponseEntity<UserPermissionDto> getStructuredDataForUser(@PathVariable String userId) {
        log.info("Received request to get structured data for user with ID: {}", userId);
        try {
            UserPermissionDto data = userPermissionService.generateStructuredDataForUser(userId);
            if (data != null) {
                log.info("Successfully retrieved structured data for user with ID: {}", userId);
                return ResponseEntity.ok(data);
            } else {
                log.warn("No data found for user with ID: {}", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error occurred while retrieving structured data for user with ID: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/user/{userId}")
    @Operation(summary = "Save or recreate User permission to Document DB")
    public ResponseEntity<String> saveUserPermissions(@PathVariable String userId) {
        log.info("Received request to save permissions for user with ID: {}", userId);
        try {
            UserPermissionDto userPermissionDto = userPermissionService.generateStructuredDataForUser(userId);
            userPermissionService.saveUserPermissionToMongo(userId, userPermissionDto);
            log.info("Successfully saved permissions for user with ID: {}", userId);
            return ResponseEntity.ok().body(userId);
        } catch (Exception e) {
            log.error("Error occurred while saving permissions for user with ID: {}", userId, e);
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }

    @GetMapping("/user/document/{userId}")
    @Operation(summary = "Get User permission from Mongo DB")
    public ResponseEntity<ObjectNode> getUserPermissionFromDocument(@PathVariable String userId) {
        log.info("Received request to get structured data for user with ID: {}", userId);
        try {
            ObjectNode data = userPermissionService.getUserPermissionFromMongo(userId);
            if (data != null) {
                log.info("Successfully retrieved structured data for role with ID: {}", userId);
                return ResponseEntity.ok(data);
            } else {
                log.warn("No data found for role with ID: {}", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error occurred while retrieving structured data for role with ID: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/user/org-ids/{userId}")
    @Operation(summary = "Get distinct org IDs for a user")
    public ResponseEntity<List<String>> getDistinctOrgIdsForUser(@PathVariable String userId) {
        log.info("Received request to get distinct org IDs for user with ID: {}", userId);
        try {
            List<String> orgIds = userPermissionService.getDistinctOrgIdsForUser(userId);
            if (!orgIds.isEmpty()) {
                log.info("Successfully retrieved distinct org IDs for user with ID: {}", userId);
                return ResponseEntity.ok(orgIds);
            } else {
                log.warn("No org IDs found for user with ID: {}", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error occurred while retrieving distinct org IDs for user with ID: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }


    @GetMapping("/user/org-keys/{userId}")
    @Operation(summary = "Get distinct org Keys for a user")
    public ResponseEntity<List<String>> getDistinctOrgKeysForUser(@PathVariable String userId) {
        log.info("Received request to get distinct org IDs for user with ID: {}", userId);
        try {
            List<String> orgKeys = userPermissionService.getDistinctOrgKeysForUser(userId);
            if (!orgKeys.isEmpty()) {
                log.info("Successfully retrieved distinct org IDs for user with ID: {}", userId);
                return ResponseEntity.ok(orgKeys);
            } else {
                log.warn("No org IDs found for user with ID: {}", userId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error occurred while retrieving distinct org IDs for user with ID: {}", userId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}