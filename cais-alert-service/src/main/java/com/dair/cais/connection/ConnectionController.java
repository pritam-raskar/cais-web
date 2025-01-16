package com.dair.cais.connection;

import com.dair.cais.connection.validation.ConnectionTestResult;
import com.dair.cais.exception.ConnectionValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
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
    private final EncryptionService encryptionService;

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

        ConnectionTestResult testResult = connectionService.testConnection(connectionId, testDetails);

        Map<String, Object> response = new HashMap<>();
        response.put("success", testResult.isSuccess());
        response.put("message", testResult.isSuccess() ?
                "Connection successful" : testResult.getErrorMessage());

        if (!testResult.isSuccess()) {
            response.put("errorType", testResult.getErrorType());
            if (log.isDebugEnabled()) {
                response.put("technicalDetails", testResult.getTechnicalDetails());
            }
        }

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

    @PostMapping("/decrypt")
    @Operation(
            summary = "Decrypt encrypted data",
            description = "Decrypts data using provided IV and encrypted content, returns the decrypted string",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponse(responseCode = "200", description = "Data successfully decrypted")
    @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    @ApiResponse(responseCode = "403", description = "Access denied")
    public ResponseEntity<DecryptionResponse> decryptData(
            @Valid @RequestBody DecryptionRequest request) {
        log.debug("Received request to decrypt data");

        try {
            String decryptedData = encryptionService.decryptToString(
                    request.getEncryptedData(),
                    request.getIv()
            );

            log.debug("Successfully decrypted data");
            return ResponseEntity.ok(new DecryptionResponse(decryptedData));

        } catch (DecryptionException e) {
            log.error("Failed to decrypt data: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during decryption: {}", e.getMessage());
            throw new DecryptionException("Failed to decrypt data", e);
        }
    }

    @PostMapping("/encrypt")
    @Operation(
            summary = "Encrypt connection data",
            description = "Encrypts the provided connection data and returns IV and encrypted content",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponse(responseCode = "200", description = "Data successfully encrypted")
    @ApiResponse(responseCode = "400", description = "Invalid input parameters")
    @ApiResponse(responseCode = "403", description = "Access denied")
    public ResponseEntity<EncryptionResponse> encryptData(
            @Valid @RequestBody EncryptionRequest request) {
        log.debug("Received request to encrypt data");

        try {
            EncryptedData encryptedData = encryptionService.encryptObject(request.getData());

            log.debug("Successfully encrypted data");
            return ResponseEntity.ok(new EncryptionResponse(
                    encryptedData.getIv(),
                    encryptedData.getEncryptedData()
            ));

        } catch (RuntimeException e) {
            log.error("Failed to encrypt data: {}", e.getMessage());
            throw new EncryptionException("Failed to encrypt data", e);
        }
    }

    @PostMapping("/{connectionId}/query")
    @Operation(
            summary = "Test connection and execute query",
            description = "Tests the connection and executes a test query on the specified connection",
            security = @SecurityRequirement(name = "bearer-key")
    )
    @ApiResponse(responseCode = "200", description = "Query executed successfully")
    @ApiResponse(responseCode = "400", description = "Invalid query or connection error")
    @ApiResponse(responseCode = "404", description = "Connection not found")
    @ApiResponse(responseCode = "403", description = "Access denied")
    public ResponseEntity<QueryResponse> testQuery(
            @Parameter(description = "ID of the connection to test")
            @PathVariable @NotNull Long connectionId,
            @Valid @RequestBody TestQueryRequest request) {

        log.info("Received request to test connection and execute query for connection ID: {}", connectionId);

        try {
            List<Map<String, Object>> results = connectionService.getConnectionAndTestQuery(
                    connectionId,
                    request.getQuery()
            );

            QueryResponse response = new QueryResponse(
                    true,
                    "Query executed successfully",
                    results
            );

            log.debug("Successfully executed query for connection ID: {}", connectionId);
            return ResponseEntity.ok(response);

        } catch (ConnectionValidationException e) {
            log.error("Connection validation failed for ID {}: {}", connectionId, e.getMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new QueryResponse(false, e.getMessage(), null));

        } catch (Exception e) {
            log.error("Error executing query for connection ID {}: {}", connectionId, e.getMessage(), e);
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new QueryResponse(false, "Failed to execute query: " + e.getMessage(), null));
        }
    }
}


