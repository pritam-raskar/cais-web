package com.dair.cais.access.entity;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/entities")
@RequiredArgsConstructor
@Tag(name = "System Entity Management", description = "APIs for managing system entities")
public class SystemEntityController {

    private final SystemEntityService systemEntityService;

    @GetMapping
    @Operation(summary = "Get all entities", description = "Retrieves all entities in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved entities")
    public ResponseEntity<List<SystemEntity>> getAllEntities() {
        log.info("REST request to get all entities");
        List<SystemEntity> entities = systemEntityService.getAllEntities();
        return ResponseEntity.ok(entities);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active entities", description = "Retrieves all active entities in the system")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved active entities")
    public ResponseEntity<List<SystemEntity>> getActiveEntities() {
        log.info("REST request to get active entities");
        List<SystemEntity> entities = systemEntityService.getActiveEntities();
        return ResponseEntity.ok(entities);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get entity by ID", description = "Retrieves an entity by its ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved entity")
    @ApiResponse(responseCode = "404", description = "Entity not found")
    public ResponseEntity<SystemEntity> getEntityById(@PathVariable Integer id) {
        log.info("REST request to get Entity : {}", id);
        SystemEntity entity = systemEntityService.getEntityById(id);
        return ResponseEntity.ok(entity);
    }

    @GetMapping("/type/{entityType}")
    @Operation(summary = "Get entity by type", description = "Retrieves an entity by its type")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved entity")
    @ApiResponse(responseCode = "404", description = "Entity not found")
    public ResponseEntity<SystemEntity> getEntityByType(@PathVariable String entityType) {
        log.info("REST request to get Entity by type : {}", entityType);
        SystemEntity entity = systemEntityService.getEntityByType(entityType);
        return ResponseEntity.ok(entity);
    }

    @PostMapping
    @Operation(summary = "Create new entity", description = "Creates a new entity in the system")
    @ApiResponse(responseCode = "201", description = "Entity created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public ResponseEntity<SystemEntity> createEntity(@Valid @RequestBody SystemEntity entity) {
        log.info("REST request to save Entity : {}", entity);
        SystemEntity result = systemEntityService.createEntity(entity);
        return new ResponseEntity<>(result, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update entity", description = "Updates an existing entity")
    @ApiResponse(responseCode = "200", description = "Entity updated successfully")
    @ApiResponse(responseCode = "404", description = "Entity not found")
    public ResponseEntity<SystemEntity> updateEntity(
            @PathVariable Integer id,
            @Valid @RequestBody SystemEntity entity) {
        log.info("REST request to update Entity : {}", entity);
        SystemEntity result = systemEntityService.updateEntity(id, entity);
        return ResponseEntity.ok(result);
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate entity", description = "Deactivates an existing entity")
    @ApiResponse(responseCode = "200", description = "Entity deactivated successfully")
    @ApiResponse(responseCode = "404", description = "Entity not found")
    public ResponseEntity<Void> deactivateEntity(@PathVariable Integer id) {
        log.info("REST request to deactivate Entity : {}", id);
        systemEntityService.deactivateEntity(id);
        return ResponseEntity.ok().build();
    }
}