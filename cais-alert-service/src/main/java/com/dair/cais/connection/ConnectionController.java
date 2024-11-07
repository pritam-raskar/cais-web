package com.dair.cais.connection;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/connection")
@Tag(name = "Connection Management", description = "APIs for managing database connections")
@RequiredArgsConstructor
@Validated
public class ConnectionController {
    private final ConnectionService connectionService;

    @GetMapping
    @Operation(
            summary = "Get all connections",
            description = "Retrieves all available database connections"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved connections")
    public ResponseEntity<List<Connection>> getAllConnections() {
        log.info("Received request to get all connections");
        return ResponseEntity.ok(connectionService.getAllConnections());
    }

    @PostMapping
    @Operation(
            summary = "Create a new connection",
            description = "Creates a new database connection with encrypted credentials"
    )
    @ApiResponse(responseCode = "201", description = "Connection created successfully")
    @ApiResponse(responseCode = "400", description = "Invalid connection details provided")
    public ResponseEntity<Connection> createConnection(
            @Valid @RequestBody Connection connection) {
        log.info("Received request to create new connection");
        return new ResponseEntity<>(connectionService.createConnection(connection), HttpStatus.CREATED);
    }

    @PostMapping("/{connectionId}/test")
    @Operation(
            summary = "Test a connection",
            description = "Tests if the specified connection is valid and can connect to the database"
    )
    @ApiResponse(responseCode = "200", description = "Connection test completed")
    public ResponseEntity<Map<String, Object>> testConnection(
            @Parameter(description = "ID of the connection to test")
            @PathVariable Long connectionId,
            @RequestBody(required = false) ConnectionDetails testDetails) {
        log.info("Received request to test connection with ID: {}", connectionId);
        boolean isValid = connectionService.testConnection(connectionId, testDetails);

        Map<String, Object> response = Map.of(
                "success", isValid,
                "message", isValid ? "Connection successful" : "Connection failed"
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{connectionId}")
    @Operation(
            summary = "Update an existing connection",
            description = "Updates the specified connection with new details"
    )
    @ApiResponse(responseCode = "200", description = "Connection updated successfully")
    @ApiResponse(responseCode = "404", description = "Connection not found")
    public ResponseEntity<Connection> updateConnection(
            @Parameter(description = "ID of the connection to update")
            @PathVariable Long connectionId,
            @Valid @RequestBody Connection connection) {
        log.info("Received request to update connection with ID: {}", connectionId);
        return ResponseEntity.ok(connectionService.updateConnection(connectionId, connection));
    }

    @GetMapping("/{connectionId}")
    @Operation(
            summary = "Get connection details",
            description = "Retrieves details of a specific connection"
    )
    @ApiResponse(responseCode = "200", description = "Successfully retrieved connection details")
    @ApiResponse(responseCode = "404", description = "Connection not found")
    public ResponseEntity<Connection> getConnection(
            @Parameter(description = "ID of the connection to retrieve")
            @PathVariable Long connectionId) {
        log.info("Received request to get connection with ID: {}", connectionId);
        return ResponseEntity.ok(connectionService.getConnection(connectionId));
    }

    @DeleteMapping("/{connectionId}")
    @Operation(
            summary = "Delete a connection",
            description = "Deletes the specified connection"
    )
    @ApiResponse(responseCode = "204", description = "Connection deleted successfully")
    @ApiResponse(responseCode = "404", description = "Connection not found")
    public ResponseEntity<Void> deleteConnection(
            @Parameter(description = "ID of the connection to delete")
            @PathVariable Long connectionId) {
        log.info("Received request to delete connection with ID: {}", connectionId);
        connectionService.deleteConnection(connectionId);
        return ResponseEntity.noContent().build();
    }
}