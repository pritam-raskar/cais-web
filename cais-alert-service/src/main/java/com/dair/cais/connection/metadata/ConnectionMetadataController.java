package com.dair.cais.connection.metadata;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/connections/metadata")
@Slf4j
@Tag(name = "Metadata Connection Management", description = "APIs for managing metadata connections")
public class ConnectionMetadataController {

    private final ConnectionMetadataService service;

    @Autowired
    public ConnectionMetadataController(ConnectionMetadataService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(summary = "Get all metadata connections")
    public ResponseEntity<List<ConnectionMetadata>> getAllConnections() {
        log.debug("REST request to get all metadata connections");
        List<ConnectionMetadata> connections = service.getAllConnections();
        return ResponseEntity.ok(connections);
    }

    @GetMapping("/{connectionType}")
    @Operation(summary = "Get metadata connection by type")
    public ResponseEntity<ConnectionMetadata> getConnection(
            @PathVariable @NotBlank String connectionType) {
        log.debug("REST request to get metadata connection : {}", connectionType);
        return service.getConnection(connectionType)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Create new metadata connection")
    public ResponseEntity<ConnectionMetadata> createConnection(
            @RequestBody @Valid ConnectionMetadata connection) {
        log.debug("REST request to create metadata connection : {}", connection.getConnectionType());
        if (service.getConnection(connection.getConnectionType()).isPresent()) {
            throw new DuplicateConnectionException("Connection type already exists: " + connection.getConnectionType());
        }
        ConnectionMetadata result = service.createConnection(connection);
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @PutMapping("/{connectionType}")
    @Operation(summary = "Update existing metadata connection")
    public ResponseEntity<ConnectionMetadata> updateConnection(
            @PathVariable @NotBlank String connectionType,
            @RequestBody @Valid ConnectionMetadata connection) {
        log.debug("REST request to update metadata connection : {}", connectionType);
        ConnectionMetadata result = service.updateConnection(connectionType, connection);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{connectionType}")
    @Operation(summary = "Delete metadata connection")
    public ResponseEntity<Void> deleteConnection(
            @PathVariable @NotBlank String connectionType) {
        log.debug("REST request to delete metadata connection : {}", connectionType);
        service.deleteConnection(connectionType);
        return ResponseEntity.noContent().build();
    }
}