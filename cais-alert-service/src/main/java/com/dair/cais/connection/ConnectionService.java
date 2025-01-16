package com.dair.cais.connection;

import com.dair.cais.connection.validation.ConnectionErrorType;
import com.dair.cais.connection.validation.ConnectionTestResult;
import com.dair.cais.exception.ConnectionValidationException;
import com.dair.cais.reports.exception.ConnectionNotFoundException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConnectionService  implements DisposableBean {
    private final ConnectionRepository repository;
    private final ConnectionMapper mapper;
    private final EncryptionService encryptionService;
    private final ConnectionValidationService validationService;
    private final Map<Long, HikariDataSource> connectionPools = new ConcurrentHashMap<>();

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
    public ConnectionTestResult testConnection(Long connectionId, ConnectionDetails testDetails) {
        log.info("Testing connection with ID: {}", connectionId);

        try {
            ConnectionEntity entity = repository.findById(connectionId)
                    .orElseThrow(() -> new ConnectionNotFoundException(connectionId));

            ConnectionDetails detailsToTest = testDetails;
            if (detailsToTest == null) {
                detailsToTest = encryptionService.decryptObject(
                        entity.getEncryptedData(),
                        entity.getIv(),
                        ConnectionDetails.class
                );
            }

            validationService.validateConnectionDetails(detailsToTest);
            return testConnectionByType(entity.getConnectionType(), detailsToTest, entity);

        } catch (ConnectionNotFoundException e) {
            log.error("Connection not found: {}", connectionId);
            return ConnectionTestResult.failure(
                    ConnectionErrorType.INVALID_CONFIGURATION,
                    "Connection not found",
                    e.getMessage(),
                    connectionId,
                    null
            );
        } catch (Exception e) {
            log.error("Error testing connection {}: {}", connectionId, e.getMessage(), e);
            return handleConnectionError(e, connectionId, null);
        }
    }

    private ConnectionTestResult testConnectionByType(ConnectionType connectionType,
                                                      ConnectionDetails details, ConnectionEntity entity) {
        switch (connectionType) {
            case POSTGRESQL:
                return testPostgresqlConnection(details, entity);
            /*case MONGODB:
                return testMongoDbConnection(details, entity);
            case MYSQL:
            case MARIADB:
                return testMysqlConnection(details, entity);*/
            /*case SNOWFLAKE:
                return testSnowflakeConnection(details);*/
            default:
                String message = "Unsupported connection type: " + connectionType;
                log.error(message);
                return ConnectionTestResult.failure(
                        ConnectionErrorType.INVALID_CONFIGURATION,
                        message,
                        null,
                        entity.getConnectionId(),
                        entity.getConnectionName()
                );
        }
    }

    private ConnectionTestResult testPostgresqlConnection(ConnectionDetails details, ConnectionEntity entity) {
        String url = String.format("jdbc:postgresql://%s:%d/%s",
                details.getHost(), details.getPort(), details.getDatabase());

        try {
            Class.forName("org.postgresql.Driver");
            try (java.sql.Connection conn = DriverManager.getConnection(
                    url,
                    details.getUsername(),
                    details.getPassword())) {

                if (conn.isValid(5)) {
                    // Test basic query execution
                    try (Statement stmt = conn.createStatement()) {
                        stmt.execute("SELECT 1");
                        return ConnectionTestResult.success(
                                entity.getConnectionId(),
                                entity.getConnectionName()
                        );
                    }
                }
                throw new SQLException("Connection validation failed");
            }
        } catch (ClassNotFoundException e) {
            log.error("PostgreSQL driver not found", e);
            return ConnectionTestResult.failure(
                    ConnectionErrorType.DRIVER_ERROR,
                    "PostgreSQL driver not found",
                    e.getMessage(),
                    entity.getConnectionId(),
                    entity.getConnectionName()
            );
        } catch (SQLException e) {
            return handlePostgresqlError(e, entity);
        }
    }

    private ConnectionTestResult handlePostgresqlError(SQLException e, ConnectionEntity entity) {
        String sqlState = e.getSQLState();
        if (sqlState != null) {
            switch (sqlState) {
                case "28P01": // Invalid password
                    return ConnectionTestResult.failure(
                            ConnectionErrorType.AUTHENTICATION_FAILED,
                            "Invalid username or password",
                            e.getMessage(),
                            entity.getConnectionId(),
                            entity.getConnectionName()
                    );
                case "3D000": // Database does not exist
                    return ConnectionTestResult.failure(
                            ConnectionErrorType.DATABASE_NOT_FOUND,
                            "Database does not exist",
                            e.getMessage(),
                            entity.getConnectionId(),
                            entity.getConnectionName()
                    );
                case "42501": // Permission denied
                    return ConnectionTestResult.failure(
                            ConnectionErrorType.PERMISSION_DENIED,
                            "User lacks required permissions",
                            e.getMessage(),
                            entity.getConnectionId(),
                            entity.getConnectionName()
                    );
                // Add more specific error codes as needed
            }
        }

        if (e.getMessage().contains("Connection refused")) {
            return ConnectionTestResult.failure(
                    ConnectionErrorType.PORT_BLOCKED,
                    "Connection refused - Port may be blocked or server is not running",
                    e.getMessage(),
                    entity.getConnectionId(),
                    entity.getConnectionName()
            );
        }

        if (e.getMessage().contains("timeout")) {
            return ConnectionTestResult.failure(
                    ConnectionErrorType.SERVER_UNREACHABLE,
                    "Connection timed out - Server may be down or unreachable",
                    e.getMessage(),
                    entity.getConnectionId(),
                    entity.getConnectionName()
            );
        }

        // Generic error handler
        return ConnectionTestResult.failure(
                ConnectionErrorType.UNKNOWN,
                "Database connection failed",
                e.getMessage(),
                entity.getConnectionId(),
                entity.getConnectionName()
        );
    }

    // Similar implementations for MySQL, MongoDB, and Snowflake...
    // Each with their specific error codes and messages

    private ConnectionTestResult handleConnectionError(Exception e, Long connectionId, String connectionName) {
        if (e instanceof ConnectException) {
            return ConnectionTestResult.failure(
                    ConnectionErrorType.NETWORK_ERROR,
                    "Network connection failed - Check if the server is reachable",
                    e.getMessage(),
                    connectionId,
                    connectionName
            );
        }

        if (e instanceof SocketTimeoutException) {
            return ConnectionTestResult.failure(
                    ConnectionErrorType.SERVER_UNREACHABLE,
                    "Connection timed out - Server may be down or network is slow",
                    e.getMessage(),
                    connectionId,
                    connectionName
            );
        }

        return ConnectionTestResult.failure(
                ConnectionErrorType.UNKNOWN,
                "An unexpected error occurred while testing the connection",
                e.getMessage(),
                connectionId,
                connectionName
        );
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

//    private boolean testConnectionByType(ConnectionType connectionType, ConnectionDetails details) {
//        try {
//            switch (connectionType) {
//                case POSTGRESQL:
//                    return testPostgresqlConnection(details);
//                case MONGODB:
//                    return testMongoDbConnection(details);
//                case MYSQL:
//                case MARIADB:
//                    return testMysqlConnection(details);
//                case SNOWFLAKE:
//                    return testSnowflakeConnection(details);
//                default:
//                    log.error("Unsupported connection type: {}", connectionType);
//                    return false;
//            }
//        } catch (Exception e) {
//            log.error("Error testing connection of type {}: {}", connectionType, e.getMessage());
//            return false;
//        }
//    }

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

    private Boolean testSnowflakeConnection(ConnectionDetails details) {
        String url = String.format("jdbc:snowflake://%s.snowflakecomputing.com/?db=%s&warehouse=%s",
                details.getHost(), details.getDatabase(), details.getAdditionalParams());

        try {
            Class.forName("net.snowflake.client.jdbc.SnowflakeDriver");
            try (java.sql.Connection conn = DriverManager.getConnection(
                    url,
                    details.getUsername(),
                    details.getPassword())) {
                if (conn.isValid(5)) {
                    try (java.sql.Statement stmt = conn.createStatement()) {
                        stmt.execute("SELECT 1");
                        //return ConnectionTestResult.success(null, null); // Update with actual connection details
                        return true;
                    }
                }
                throw new SQLException("Connection validation failed");
            }
        } catch (Exception e) {
            //return handleSnowflakeError(e, details);
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

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getConnectionAndTestQuery(Long connectionId, String testQuery) {
        log.info("Testing connection and executing query for connection ID: {}", connectionId);

        ConnectionEntity entity = repository.findById(connectionId)
                .orElseThrow(() -> {
                    log.error("Connection not found with ID: {}", connectionId);
                    return new ConnectionValidationException("Connection not found");
                });

        try {
            ConnectionDetails details = encryptionService.decryptObject(
                    entity.getEncryptedData(),
                    entity.getIv(),
                    ConnectionDetails.class
            );

            if (!testConnectionByType(entity.getConnectionType(), details)) {
                throw new ConnectionValidationException("Failed to establish connection");
            }

            return executeQuery(entity.getConnectionType(), details, testQuery);

        } catch (Exception e) {
            log.error("Error testing connection or executing query for ID: {}", connectionId, e);
            throw new ConnectionValidationException("Failed to test connection or execute query: " + e.getMessage());
        }
    }

    private List<Map<String, Object>> executeQuery(ConnectionType connectionType, ConnectionDetails details, String testQuery) {
//        if (connectionType == ConnectionType.MONGODB) {
//            return executeMongoQuery(details, testQuery);
//        }

        return executeJdbcQuery(connectionType, details, testQuery);
    }

//    private List<Map<String, Object>> executeMongoQuery(ConnectionDetails details, String testQuery) {
//        String connectionString = String.format("mongodb://%s:%s@%s:%d/%s",
//                details.getUsername(),
//                details.getPassword(),
//                details.getHost(),
//                details.getPort(),
//                details.getDatabase());
//
//        try (SimpleMongoClientDatabaseFactory factory = new SimpleMongoClientDatabaseFactory(connectionString)) {
//            MongoTemplate mongoTemplate = new MongoTemplate(factory);
//            // Note: This is a simplified implementation. You might want to add proper MongoDB query parsing
//            return mongoTemplate.find(org.springframework.data.mongodb.core.query.Query.class, Map.class, testQuery);
//        } catch (Exception e) {
//            log.error("Error executing MongoDB query: {}", e.getMessage());
//            throw new ConnectionValidationException("Failed to execute MongoDB query: " + e.getMessage());
//        }
//    }

    private List<Map<String, Object>> executeJdbcQuery(ConnectionType connectionType, ConnectionDetails details, String testQuery) {
        DataSource dataSource = null;
        try {
            dataSource = createDataSource(connectionType, details);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
            return jdbcTemplate.queryForList(testQuery);
        } catch (Exception e) {
            log.error("Error executing JDBC query: {}", e.getMessage());
            throw new ConnectionValidationException("Failed to execute query: " + e.getMessage());
        } finally {
            closeDataSource(dataSource);
        }
    }

    private DataSource createDataSource(ConnectionType connectionType, ConnectionDetails details) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(buildJdbcUrl(connectionType, details));
        config.setUsername(details.getUsername());
        config.setPassword(details.getPassword());
        config.setDriverClassName(connectionType.getDriverClass());

        // Connection pool settings
        config.setMaximumPoolSize(1);
        config.setMinimumIdle(0);
        config.setConnectionTimeout(5000); // 5 seconds
        config.setIdleTimeout(10000); // 10 seconds

        return new HikariDataSource(config);
    }

    private String buildJdbcUrl(ConnectionType connectionType, ConnectionDetails details) {
        String baseUrl = String.format("jdbc:%s://%s:%d/%s",
                connectionType.getDatabaseType(),
                details.getHost(),
                details.getPort(),
                details.getDatabase());

        if (connectionType == ConnectionType.SNOWFLAKE) {
            return String.format("jdbc:snowflake://%s.snowflakecomputing.com/?db=%s&warehouse=%s",
                    details.getHost(),
                    details.getDatabase(),
                    details.getAdditionalParams());
        }

        // Add additional parameters if they exist
        if (details.getAdditionalParams() != null && !details.getAdditionalParams().isEmpty()) {
            baseUrl += "?" + details.getAdditionalParams();
        }

        return baseUrl;
    }

    private void closeDataSource(DataSource dataSource) {
        if (dataSource instanceof HikariDataSource) {
            try {
                ((HikariDataSource) dataSource).close();
            } catch (Exception e) {
                log.warn("Error closing datasource: {}", e.getMessage());
            }
        }
    }

    @Transactional(readOnly = true)
    public boolean existsById(Long connectionId) {
        try {
            log.debug("Checking existence of connection with ID: {}", connectionId);
            return repository.existsById(connectionId);
        } catch (Exception e) {
            log.error("Error checking existence of connection with ID {}: {}", connectionId, e.getMessage());
            throw new ConnectionValidationException("Error verifying connection existence", e);
        }
    }
    // Convenience method that throws an exception if not found
    @Transactional(readOnly = true)
    public void validateConnectionExists(Long connectionId) {
        if (!existsById(connectionId)) {
            log.error("Connection not found with ID: {}", connectionId);
            throw new ConnectionValidationException("Connection not found with ID: " + connectionId);
        }
    }


    // changes for Report execution
    // Add this new method for report execution
    @Transactional(readOnly = true)
    public List<Map<String, Object>> executeQuery(Long connectionId, String sql, Object... params) {
        log.debug("Executing query on connection {}: {}", connectionId, sql);
        long startTime = System.currentTimeMillis();

        ConnectionEntity entity = repository.findById(connectionId)
                .orElseThrow(() -> new ConnectionValidationException("Connection not found: " + connectionId));

        try {
            HikariDataSource dataSource = getOrCreateConnectionPool(connectionId, entity);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            List<Map<String, Object>> results = params != null && params.length > 0
                    ? jdbcTemplate.queryForList(sql, params)
                    : jdbcTemplate.queryForList(sql);

            long executionTime = System.currentTimeMillis() - startTime;
            log.debug("Query executed successfully in {}ms, returned {} rows",
                    executionTime, results.size());

            return results;

        } catch (Exception e) {
            log.error("Error executing query on connection {}: {}", connectionId, e.getMessage(), e);
            throw new ConnectionValidationException(
                    "Failed to execute query on connection " + connectionId,
                    e.getMessage(),
                    e
            );
        }
    }

    private HikariDataSource getOrCreateConnectionPool(Long connectionId, ConnectionEntity entity) {
        return connectionPools.computeIfAbsent(connectionId, id -> {
            try {
                ConnectionDetails details = encryptionService.decryptObject(
                        entity.getEncryptedData(),
                        entity.getIv(),
                        ConnectionDetails.class
                );
                return createPooledDataSource(entity.getConnectionType(), details);
            } catch (Exception e) {
                log.error("Error creating connection pool for connection {}: {}",
                        connectionId, e.getMessage(), e);
                throw new ConnectionValidationException(
                        "Failed to create connection pool",
                        e.getMessage(),
                        e
                );
            }
        });
    }

    private HikariDataSource createPooledDataSource(ConnectionType connectionType, ConnectionDetails details) {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(buildJdbcUrl(connectionType, details));
        config.setUsername(details.getUsername());
        config.setPassword(details.getPassword());
        config.setDriverClassName(connectionType.getDriverClass());

        // Enhanced pool settings for report execution
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setIdleTimeout(300000); // 5 minutes
        config.setConnectionTimeout(20000); // 20 seconds
        config.setValidationTimeout(5000); // 5 seconds
        config.setLeakDetectionThreshold(60000); // 1 minute

        // Set pool name for better monitoring
        config.setPoolName("ReportPool-" + connectionType.name());

        // Add connection test query
        config.setConnectionTestQuery("SELECT 1");

        return new HikariDataSource(config);
    }

    // Add cleanup method
    public void closeConnectionPool(Long connectionId) {
        HikariDataSource dataSource = connectionPools.remove(connectionId);
        if (dataSource != null && !dataSource.isClosed()) {
            log.info("Closing connection pool for connection {}", connectionId);
            dataSource.close();
        }
    }

    // Add cleanup method for all pools
    public synchronized void closeAllConnectionPools() {
        log.info("Closing all connection pools");
        connectionPools.forEach((connectionId, dataSource) -> {
            if (dataSource != null && !dataSource.isClosed()) {
                try {
                    dataSource.close();
                    log.debug("Closed connection pool for connection {}", connectionId);
                } catch (Exception e) {
                    log.warn("Error closing connection pool {}: {}", connectionId, e.getMessage());
                }
            }
        });
        connectionPools.clear();
    }

//    // Override finalize to ensure pools are closed
//    @Override
//    protected void finalize() throws Throwable {
//        try {
//            closeAllConnectionPools();
//        } finally {
//            super.finalize();
//        }
//    }

    // Remove the finalize() method and replace with destroy() from DisposableBean
    @Override
    public void destroy() {
        log.info("Destroying ConnectionService, cleaning up connection pools");
        closeAllConnectionPools();
    }

    private ConnectionTestResult handleSnowflakeError(Exception e, ConnectionDetails details) {
        if (e instanceof SQLException) {
            SQLException sqlException = (SQLException) e;
            String sqlState = sqlException.getSQLState();
            String errorMessage = sqlException.getMessage();

            // Snowflake specific error codes
            if (sqlState != null) {
                switch (sqlState) {
                    case "28000": // Authentication failure
                        return ConnectionTestResult.failure(
                                ConnectionErrorType.AUTHENTICATION_FAILED,
                                "Invalid username or password for Snowflake",
                                errorMessage,
                                null,
                                null
                        );
                    case "02000": // No connection
                        return ConnectionTestResult.failure(
                                ConnectionErrorType.SERVER_UNREACHABLE,
                                "Unable to connect to Snowflake server",
                                errorMessage,
                                null,
                                null
                        );
                    case "42000": // Syntax error or access violation
                        return ConnectionTestResult.failure(
                                ConnectionErrorType.PERMISSION_DENIED,
                                "Access denied or invalid warehouse configuration",
                                errorMessage,
                                null,
                                null
                        );
                }
            }

            // Check error message patterns
            if (errorMessage.contains("warehouse") || errorMessage.contains("WAREHOUSE")) {
                return ConnectionTestResult.failure(
                        ConnectionErrorType.INVALID_CONFIGURATION,
                        "Invalid warehouse configuration",
                        errorMessage,
                        null,
                        null
                );
            }

            if (errorMessage.contains("timeout") || errorMessage.contains("TIMEOUT")) {
                return ConnectionTestResult.failure(
                        ConnectionErrorType.SERVER_UNREACHABLE,
                        "Connection timed out while connecting to Snowflake",
                        errorMessage,
                        null,
                        null
                );
            }
        }

        // Network related errors
        if (e instanceof ConnectException) {
            return ConnectionTestResult.failure(
                    ConnectionErrorType.NETWORK_ERROR,
                    "Network error while connecting to Snowflake",
                    e.getMessage(),
                    null,
                    null
            );
        }

        if (e instanceof SocketTimeoutException) {
            return ConnectionTestResult.failure(
                    ConnectionErrorType.SERVER_UNREACHABLE,
                    "Connection timed out - Snowflake server may be unreachable",
                    e.getMessage(),
                    null,
                    null
            );
        }

        // Default error handler
        log.error("Unexpected Snowflake connection error: {}", e.getMessage(), e);
        return ConnectionTestResult.failure(
                ConnectionErrorType.UNKNOWN,
                "Failed to connect to Snowflake",
                e.getMessage(),
                null,
                null
        );
    }

    private ConnectionTestResult handleMySQLError(Exception e, ConnectionDetails details) {
        if (e instanceof SQLException) {
            SQLException sqlException = (SQLException) e;
            int errorCode = sqlException.getErrorCode();
            String message = sqlException.getMessage();

            // MySQL specific error codes
            switch (errorCode) {
                case 1045: // Access denied
                    return ConnectionTestResult.failure(
                            ConnectionErrorType.AUTHENTICATION_FAILED,
                            "Invalid username or password for MySQL",
                            message,
                            null,
                            null
                    );
                case 1049: // Unknown database
                    return ConnectionTestResult.failure(
                            ConnectionErrorType.DATABASE_NOT_FOUND,
                            "Database does not exist: " + details.getDatabase(),
                            message,
                            null,
                            null
                    );
                case 1044: // Access denied for database
                    return ConnectionTestResult.failure(
                            ConnectionErrorType.PERMISSION_DENIED,
                            "User lacks permission to access database",
                            message,
                            null,
                            null
                    );
                case 1042: // Unable to connect
                    return ConnectionTestResult.failure(
                            ConnectionErrorType.SERVER_UNREACHABLE,
                            "Cannot connect to MySQL server",
                            message,
                            null,
                            null
                    );
            }
        }

        // Network related errors
        if (e instanceof ConnectException) {
            return ConnectionTestResult.failure(
                    ConnectionErrorType.NETWORK_ERROR,
                    "Network error while connecting to MySQL server",
                    e.getMessage(),
                    null,
                    null
            );
        }

        // Default error handler
        log.error("Unexpected MySQL connection error: {}", e.getMessage(), e);
        return ConnectionTestResult.failure(
                ConnectionErrorType.UNKNOWN,
                "Failed to connect to MySQL server",
                e.getMessage(),
                null,
                null
        );
    }

    private ConnectionTestResult handleMongoError(Exception e, ConnectionDetails details) {
        String message = e.getMessage();

        if (message != null) {
            if (message.contains("Authentication failed")) {
                return ConnectionTestResult.failure(
                        ConnectionErrorType.AUTHENTICATION_FAILED,
                        "Invalid username or password for MongoDB",
                        message,
                        null,
                        null
                );
            }

            if (message.contains("not authorized")) {
                return ConnectionTestResult.failure(
                        ConnectionErrorType.PERMISSION_DENIED,
                        "User lacks required permissions",
                        message,
                        null,
                        null
                );
            }

            if (message.contains("Connection refused")) {
                return ConnectionTestResult.failure(
                        ConnectionErrorType.PORT_BLOCKED,
                        "MongoDB server refused connection - check if port is open",
                        message,
                        null,
                        null
                );
            }

            if (message.contains("timed out")) {
                return ConnectionTestResult.failure(
                        ConnectionErrorType.SERVER_UNREACHABLE,
                        "Connection timed out - MongoDB server may be unreachable",
                        message,
                        null,
                        null
                );
            }
        }

        // Network related errors
        if (e instanceof ConnectException) {
            return ConnectionTestResult.failure(
                    ConnectionErrorType.NETWORK_ERROR,
                    "Network error while connecting to MongoDB",
                    e.getMessage(),
                    null,
                    null
            );
        }

        // Default error handler
        log.error("Unexpected MongoDB connection error: {}", e.getMessage(), e);
        return ConnectionTestResult.failure(
                ConnectionErrorType.UNKNOWN,
                "Failed to connect to MongoDB server",
                e.getMessage(),
                null,
                null
        );
    }

}
