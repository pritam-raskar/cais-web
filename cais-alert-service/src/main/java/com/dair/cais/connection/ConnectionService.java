package com.dair.cais.connection;

import com.dair.cais.exception.ConnectionValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.DriverManager;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionService {
    private final ConnectionRepository repository;
    private final ConnectionMapper mapper;
    private final EncryptionService encryptionService;
    private final ConnectionValidationService validationService;

    @Transactional(readOnly = true)
    public List<Connection> getAllConnections() {
        log.debug("Fetching all connections");
        try {
            return repository.findAll()
                    .stream()
                    .map(mapper::toModel)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving connections", e);
            throw new RuntimeException("Failed to retrieve connections", e);
        }
    }

    @Transactional
    public Connection createConnection(Connection connection) {
        log.info("Creating new connection with name: {}", connection.getConnectionName());

        try {
            validationService.validateConnection(connection);

            if (repository.existsByConnectionName(connection.getConnectionName())) {
                log.error("Connection name already exists: {}", connection.getConnectionName());
                throw new ConnectionValidationException("Connection name already exists");
            }

            ConnectionEntity entity = mapper.toEntity(connection);
            ConnectionEntity savedEntity = repository.save(entity);
            log.info("Successfully created connection with ID: {}", savedEntity.getConnectionId());

            return mapper.toModel(savedEntity);
        } catch (ConnectionValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error creating connection", e);
            throw new RuntimeException("Error creating connection", e);
        }
    }

    @Transactional(readOnly = true)
    public boolean testConnection(Long connectionId, ConnectionDetails testDetails) {
        log.info("Testing connection with ID: {}", connectionId);

        ConnectionEntity entity = repository.findById(connectionId)
                .orElseThrow(() -> {
                    log.error("Connection not found with ID: {}", connectionId);
                    return new ConnectionValidationException("Connection not found");
                });

        try {
            validationService.validateConnectionDetails(testDetails);

            if (testConnectionByType(entity.getConnectionType(), testDetails)) {
                saveConnectionDetails(entity, testDetails);
                return true;
            }
            return false;
        } catch (Exception e) {
            log.error("Error testing connection with ID: {}", connectionId, e);
            return false;
        }
    }

    @Transactional
    private void saveConnectionDetails(ConnectionEntity entity, ConnectionDetails details) {
        try {
            EncryptedData encryptedData = encryptionService.encryptObject(details);
            entity.setIv(encryptedData.getIv());
            entity.setEncryptedData(encryptedData.getEncryptedData());
            repository.save(entity);
            log.debug("Successfully saved connection details for ID: {}", entity.getConnectionId());
        } catch (Exception e) {
            log.error("Error saving connection details", e);
            throw new RuntimeException("Failed to save connection details", e);
        }
    }

    private boolean testConnectionByType(ConnectionType connectionType, ConnectionDetails details) {
        try {
            switch (connectionType) {
                case POSTGRESQL:
                    return testPostgresqlConnection(details);
                case MONGODB:
                    return testMongoDbConnection(details);
                case MYSQL:
                case MARIADB:
                    return testMysqlConnection(details);
                case SNOWFLAKE:
                    return testSnowflakeConnection(details);
                default:
                    log.error("Unsupported connection type: {}", connectionType);
                    return false;
            }
        } catch (Exception e) {
            log.error("Error testing connection of type {}: {}", connectionType, e.getMessage());
            return false;
        }
    }

    private boolean testPostgresqlConnection(ConnectionDetails details) {
        String url = String.format("jdbc:postgresql://%s:%d/%s",
                details.getHost(), details.getPort(), details.getDatabase());

        try {
            Class.forName("org.postgresql.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(
                    url,
                    details.getUsername(),
                    details.getPassword())) {
                return conn.isValid(5);
            }
        } catch (Exception e) {
            log.error("Error testing PostgreSQL connection: {}", e.getMessage());
            return false;
        }
    }

    private boolean testMongoDbConnection(ConnectionDetails details) {
        String connectionString = String.format("mongodb://%s:%s@%s:%d/%s",
                details.getUsername(),
                details.getPassword(),
                details.getHost(),
                details.getPort(),
                details.getDatabase());

        SimpleMongoClientDatabaseFactory factory = null;
        try {
            // Create a temporary MongoTemplate to test the connection
            factory = new SimpleMongoClientDatabaseFactory(connectionString);
            MongoTemplate mongoTemplate = new MongoTemplate(factory);

            // Test the connection by executing a ping command
            mongoTemplate.executeCommand("{ ping: 1 }");

            log.debug("Successfully connected to MongoDB at {}:{}", details.getHost(), details.getPort());
            return true;
        } catch (Exception e) {
            log.error("Failed to connect to MongoDB: {}", e.getMessage());
            return false;
        } finally {
            // Clean up resources
            if (factory != null) {
                try {
                    factory.destroy();
                } catch (Exception e) {
                    log.warn("Error closing MongoDB connection factory: {}", e.getMessage());
                }
            }
        }
    }

    private boolean testMysqlConnection(ConnectionDetails details) {
        String url = String.format("jdbc:mysql://%s:%d/%s",
                details.getHost(), details.getPort(), details.getDatabase());

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(
                    url,
                    details.getUsername(),
                    details.getPassword())) {
                return conn.isValid(5);
            }
        } catch (Exception e) {
            log.error("Error testing MySQL connection: {}", e.getMessage());
            return false;
        }
    }

    private boolean testSnowflakeConnection(ConnectionDetails details) {
        String url = String.format("jdbc:snowflake://%s.snowflakecomputing.com/?db=%s&warehouse=%s",
                details.getHost(), details.getDatabase(), details.getAdditionalParams());

        try {
            Class.forName("net.snowflake.client.jdbc.SnowflakeDriver");
            try (java.sql.Connection conn = DriverManager.getConnection(
                    url,
                    details.getUsername(),
                    details.getPassword())) {
                return conn.isValid(5);
            }
        } catch (Exception e) {
            log.error("Error testing Snowflake connection: {}", e.getMessage());
            return false;
        }
    }

    @Transactional
    public Connection updateConnection(Long connectionId, Connection connection) {
        log.info("Updating connection with ID: {}", connectionId);

        try {
            validationService.validateConnection(connection);

            ConnectionEntity entity = repository.findById(connectionId)
                    .orElseThrow(() -> new ConnectionValidationException("Connection not found"));

            if (!entity.getConnectionName().equals(connection.getConnectionName()) &&
                    repository.existsByConnectionName(connection.getConnectionName())) {
                throw new ConnectionValidationException("Connection name already exists");
            }

            mapper.updateEntity(entity, connection);
            ConnectionEntity savedEntity = repository.save(entity);
            log.info("Successfully updated connection with ID: {}", savedEntity.getConnectionId());

            return mapper.toModel(savedEntity);
        } catch (ConnectionValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error updating connection", e);
            throw new RuntimeException("Error updating connection", e);
        }
    }

    @Transactional(readOnly = true)
    public Connection getConnection(Long connectionId) {
        log.debug("Fetching connection with ID: {}", connectionId);
        try {
            return repository.findById(connectionId)
                    .map(mapper::toModel)
                    .orElseThrow(() -> new ConnectionValidationException("Connection not found"));
        } catch (ConnectionValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving connection", e);
            throw new RuntimeException("Error retrieving connection", e);
        }
    }

    @Transactional
    public void deleteConnection(Long connectionId) {
        log.info("Deleting connection with ID: {}", connectionId);
        try {
            if (!repository.existsById(connectionId)) {
                throw new ConnectionValidationException("Connection not found");
            }
            repository.deleteById(connectionId);
            log.info("Successfully deleted connection with ID: {}", connectionId);
        } catch (ConnectionValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error deleting connection", e);
            throw new RuntimeException("Error deleting connection", e);
        }
    }
}