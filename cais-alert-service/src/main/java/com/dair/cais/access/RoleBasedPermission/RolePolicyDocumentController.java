package com.dair.cais.access.RoleBasedPermission;

import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/permissions")
public class RolePolicyDocumentController {

    @Autowired
    private RolePolicyDocumentService rolePolicyDocumentService;

    @GetMapping("/role/{roleId}")
    @Operation(summary = "Prepare Role permission from RDBMS DB")
    public ResponseEntity<ObjectNode> getStructuredDataForRole(@PathVariable Integer roleId) {
        log.info("Received request to get structured data for role with ID: {}", roleId);
        try {
            ObjectNode data = rolePolicyDocumentService.generateStructuredDataForRole(roleId);
            if (data != null) {
                log.info("Successfully retrieved structured data for role with ID: {}", roleId);
                return ResponseEntity.ok(data);
            } else {
                log.warn("No data found for role with ID: {}", roleId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error occurred while retrieving structured data for role with ID: {}", roleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/role/{roleId}")
    @Operation(summary = "Create or replace Role permission from Mongo DB")
    public ResponseEntity<String> saveRolePermissions(@PathVariable Integer roleId) {
        log.info("Received request to save permissions for role with ID: {}", roleId);
        try {
            ObjectNode rolePermissionData = rolePolicyDocumentService.generateStructuredDataForRole(roleId);
            rolePolicyDocumentService.saveRolePermissionToMongo(roleId, rolePermissionData);
            log.info("Successfully saved permissions for role with ID: {}", roleId);
            return ResponseEntity.ok().body(roleId.toString());
        } catch (Exception e) {
            log.error("Error occurred while saving permissions for role with ID: {}", roleId, e);
            return ResponseEntity.internalServerError().body(e.toString());
        }
    }


    @GetMapping("/role/document/{roleId}")
    @Operation(summary = "Get Role permission from Mongo DB")
    public ResponseEntity<ObjectNode> getStructuredDataForRoleFromMongo(@PathVariable Integer roleId) {
        log.info("Received request to get structured data for role with ID: {}", roleId);
        try {
            ObjectNode data = rolePolicyDocumentService.getRolePermissionFromMongo(roleId);
            if (data != null) {
                log.info("Successfully retrieved structured data for role with ID: {}", roleId);
                return ResponseEntity.ok(data);
            } else {
                log.warn("No data found for role with ID: {}", roleId);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error occurred while retrieving structured data for role with ID: {}", roleId, e);
            return ResponseEntity.internalServerError().build();
        }
    }


}