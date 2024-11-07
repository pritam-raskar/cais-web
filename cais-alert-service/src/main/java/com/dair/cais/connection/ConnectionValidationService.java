package com.dair.cais.connection;

import com.dair.cais.exception.ConnectionValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class ConnectionValidationService {

    /**
     * Validates connection details based on connection type
     * @param details Connection details to validate
     * @throws ConnectionValidationException if validation fails
     */
    public void validateConnectionDetails(ConnectionDetails details) {
        log.debug("Validating connection details");
        List<String> errors = new ArrayList<>();

        validateBasicFields(details, errors);

        if (!errors.isEmpty()) {
            String errorMessage = "Connection validation failed";
            String errorDetails = String.join(", ", errors);
            log.error("{}: {}", errorMessage, errorDetails);
            throw new ConnectionValidationException(errorMessage, errorDetails);
        }
    }

    private void validateBasicFields(ConnectionDetails details, List<String> errors) {
        if (details == null) {
            errors.add("Connection details cannot be null");
            return;
        }

        // Host validation
        if (!StringUtils.hasText(details.getHost())) {
            errors.add("Host is required");
        }

        // Port validation
        if (details.getPort() == null) {
            errors.add("Port is required");
        } else if (details.getPort() <= 0 || details.getPort() > 65535) {
            errors.add("Port must be between 1 and 65535");
        }

        // Database validation
        if (!StringUtils.hasText(details.getDatabase())) {
            errors.add("Database name is required");
        }

        // Credential validation
        if (!StringUtils.hasText(details.getUsername())) {
            errors.add("Username is required");
        }
        if (!StringUtils.hasText(details.getPassword())) {
            errors.add("Password is required");
        }
    }

    /**
     * Validates a connection object
     * @param connection Connection to validate
     * @throws ConnectionValidationException if validation fails
     */
    public void validateConnection(Connection connection) {
        log.debug("Validating connection");
        List<String> errors = new ArrayList<>();

        if (connection == null) {
            throw new ConnectionValidationException("Connection cannot be null");
        }

        // Validate connection name
        if (!StringUtils.hasText(connection.getConnectionName())) {
            errors.add("Connection name is required");
        } else if (connection.getConnectionName().length() > 255) {
            errors.add("Connection name cannot exceed 255 characters");
        }

        // Validate connection type
        validateConnectionType(connection, errors);

        // Additional validation based on connection type
        if (connection.getConnectionType() != null) {
            switch (connection.getConnectionType()) {
                case SNOWFLAKE:
                    validateSnowflakeSpecifics(connection, errors);
                    break;
                case MONGODB:
                    validateMongoDBSpecifics(connection, errors);
                    break;
                // Add other specific validations as needed
            }
        }

        if (!errors.isEmpty()) {
            String errorMessage = "Connection validation failed";
            String errorDetails = String.join(", ", errors);
            log.error("{}: {}", errorMessage, errorDetails);
            throw new ConnectionValidationException(errorMessage, errorDetails);
        }
    }

    private void validateConnectionType(Connection connection, List<String> errors) {
        if (connection.getConnectionType() == null) {
            errors.add("Connection type is required");
            return;
        }

        try {
            // This will validate if the connection type is supported
            ConnectionType.valueOf(connection.getConnectionType().name());
        } catch (IllegalArgumentException e) {
            log.warn("Invalid connection type provided: {}", connection.getConnectionType());
            errors.add("Invalid connection type: " + connection.getConnectionType() +
                    ". Supported types are: " + java.util.Arrays.toString(ConnectionType.values()));
        }
    }

    /**
     * Validates Snowflake-specific connection requirements
     */
    private void validateSnowflakeSpecifics(Connection connection, List<String> errors) {
        if (connection.getConnectionDetails() != null) {
            ConnectionDetails details = connection.getConnectionDetails();
            if (details.getAdditionalParams() == null ||
                    !details.getAdditionalParams().contains("warehouse=")) {
                errors.add("Warehouse parameter is required for Snowflake connections");
            }
        }
    }

    /**
     * Validates MongoDB-specific connection requirements
     */
    private void validateMongoDBSpecifics(Connection connection, List<String> errors) {
        if (connection.getConnectionDetails() != null) {
            ConnectionDetails details = connection.getConnectionDetails();
            if (details.getDatabase() != null && details.getDatabase().contains(" ")) {
                errors.add("MongoDB database name cannot contain spaces");
            }
        }
    }

    /**
     * Validates connection details based on connection type
     */
    public void validateConnectionDetailsByType(ConnectionType type, ConnectionDetails details) {
        log.debug("Validating connection details for type: {}", type);
        List<String> errors = new ArrayList<>();

        validateBasicFields(details, errors);

        switch (type) {
            case SNOWFLAKE:
                validateSnowflakeSpecifics(details, errors);
                break;
            case MONGODB:
                validateMongoDBSpecifics(details, errors);
                break;
            // Add other cases as needed
        }

        if (!errors.isEmpty()) {
            String errorMessage = "Connection validation failed for type: " + type;
            String errorDetails = String.join(", ", errors);
            log.error("{}: {}", errorMessage, errorDetails);
            throw new ConnectionValidationException(errorMessage, errorDetails);
        }
    }

    private void validateSnowflakeSpecifics(ConnectionDetails details, List<String> errors) {
        if (details.getAdditionalParams() == null ||
                !details.getAdditionalParams().contains("warehouse=")) {
            errors.add("Warehouse parameter is required for Snowflake connections");
        }
    }

    private void validateMongoDBSpecifics(ConnectionDetails details, List<String> errors) {
        if (details.getDatabase() != null && details.getDatabase().contains(" ")) {
            errors.add("MongoDB database name cannot contain spaces");
        }
    }
}