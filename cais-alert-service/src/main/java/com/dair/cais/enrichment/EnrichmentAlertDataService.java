package com.dair.cais.enrichment;

import com.dair.cais.connector.Connector;
import com.dair.cais.connector.ConnectorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EnrichmentAlertDataService {

    private static final Logger log = LogManager.getLogger(EnrichmentAlertDataService.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ConnectorService connectorService;

    // Existing method
    public List<Map<String, Object>> queryData(String fieldName, String schema, String tableName, String columnName, String alertId) {
        // Step 1: Fetch the alert from MongoDB
        Query query = new Query(Criteria.where("alertId").is(alertId));
        Map<String, Object> alert = mongoTemplate.findOne(query, Map.class, "alerts");
        if (alert == null) {
            throw new RuntimeException("Alert not found");
        }

        // Step 2: Extract the value from the alert
        Object value = extractValue(alert, fieldName);
        if (value == null) {
            throw new RuntimeException("Field not found in alert");
        }

        // Step 3: Query the RDBMS
        String sql = String.format("SELECT * FROM %s.%s WHERE %s = ?", escapeSQL(schema), escapeSQL(tableName), escapeSQL(columnName));
        return jdbcTemplate.queryForList(sql, value);
    }

    // New method for flexible query execution
    public List<Map<String, Object>> executeFlexibleQuery(Long connectorId, String queryString, Object parameters) {
        Connector connector = connectorService.getConnector(connectorId)
                .orElseThrow(() -> new RuntimeException("Connector not found"));

        switch (connector.getDatabaseType()) {
            case POSTGRESQL:
            case MYSQL:
            case ORACLE:
            case REDSHIFT:
            case SNOWFLAKE:
            case MARIADB:
            case TRINO:
                return executeJdbcQuery(connector, queryString, parameters);
            case MONGODB:
                return executeMongoQuery(connector, queryString, parameters);
            default:
                throw new UnsupportedOperationException("Unsupported database type: " + connector.getDatabaseType());
        }
    }

    private List<Map<String, Object>> executeJdbcQuery(Connector connector, String queryString, Object parameters) {
        try (Connection conn = DriverManager.getConnection(connector.getUrl(), connector.getUsername(), connector.getPassword())) {
            DataSource dataSource = new org.springframework.jdbc.datasource.SingleConnectionDataSource(conn, false);
            JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

            if (parameters instanceof List) {
                return jdbcTemplate.queryForList(queryString, ((List<?>) parameters).toArray());
            } else if (parameters instanceof Map) {
                // Implement named parameter handling if needed
                throw new UnsupportedOperationException("Named parameters not yet implemented for JDBC queries");
            } else {
                throw new IllegalArgumentException("Unsupported parameter type");
            }
        } catch (Exception e) {
            log.error("Error executing JDBC query", e);
            throw new RuntimeException("Error executing query: " + e.getMessage(), e);
        }
    }

    private List<Map<String, Object>> executeMongoQuery(Connector connector, String queryString, Object parameters) {
        String parsedQuery;
        if (parameters instanceof List) {
            parsedQuery = parseMongoQuery(queryString, (List<?>) parameters);
        } else if (parameters instanceof Map) {
            parsedQuery = parseMongoQueryWithNamedParams(queryString, (Map<String, ?>) parameters);
        } else {
            throw new IllegalArgumentException("Unsupported parameter type");
        }

        try (MongoClient mongoClient = MongoClients.create(connector.getUrl())) {
            MongoDatabase database = mongoClient.getDatabase(connector.getDatabaseName());
            Document query = Document.parse(parsedQuery);

            List<Document> results = database.getCollection(connector.getCollectionName())
                    .find(query)
                    .into(new ArrayList<>());

            return results.stream()
                    .map(doc -> doc.entrySet().stream()
                            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error executing MongoDB query", e);
            throw new RuntimeException("Error executing query: " + e.getMessage(), e);
        }
    }


    private String parseMongoQuery(String queryString, List<?> parameters) {
        // This is a very basic implementation and might need to be more sophisticated
        // depending on your query complexity
        String[] parts = queryString.split("\\?");
        StringBuilder result = new StringBuilder(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            result.append(formatValue(parameters.get(i-1)));
            result.append(parts[i]);
        }
        return result.toString();
    }

    private String parseMongoQueryWithNamedParams(String queryString, Map<String, ?> parameters) {
        // Implement named parameter handling for MongoDB queries
        throw new UnsupportedOperationException("Named parameters not yet implemented for MongoDB queries");
    }

    private String formatValue(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        } else if (value instanceof Number) {
            return value.toString();
        } else if (value instanceof Boolean) {
            return value.toString();
        } else if (value == null) {
            return "null";
        } else {
            // For other types, you might need to implement specific formatting
            return value.toString();
        }
    }

        private String escapeSQL(String input) {
        // Basic SQL injection prevention
        // In a production environment, consider using a more robust solution
        return input.replaceAll("[^a-zA-Z0-9_]", "");
    }

    private Object extractValue(Map<String, Object> alert, String fieldName) {
        String[] parts = fieldName.split("\\.");
        Object current = alert;
        for (String part : parts) {
            if (current instanceof Map) {
                current = ((Map<?, ?>) current).get(part);
            } else {
                return null;
            }
        }
        return current;
    }
}



//package com.dair.cais.enrichment;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.data.mongodb.core.MongoTemplate;
//import org.springframework.jdbc.core.JdbcTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//
//import java.util.List;
//import java.util.Map;
//
//@Service
//public class EnrichmentAlertDataService {
//
//    @Autowired
//    private MongoTemplate mongoTemplate;
//
//    @Autowired
//    private JdbcTemplate jdbcTemplate;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    public List<Map<String, Object>> queryData(String fieldName, String schema, String tableName, String columnName, String alertId) {
//        // Step 1: Fetch the alert from MongoDB
//        Query query = new Query(Criteria.where("alertId").is(alertId));
//        Map<String, Object> alert = mongoTemplate.findOne(query, Map.class, "alerts");
//
//        if (alert == null) {
//            throw new RuntimeException("Alert not found");
//        }
//
//        // Step 2: Extract the value from the alert
//        Object value = extractValue(alert, fieldName);
//
//        if (value == null) {
//            throw new RuntimeException("Field not found in alert");
//        }
//
//        // Step 3: Query the RDBMS
//        String sql = String.format("SELECT * FROM %s.%s WHERE %s = ?",
//                escapeSQL(schema),
//                escapeSQL(tableName),
//                escapeSQL(columnName));
//        return jdbcTemplate.queryForList(sql, value);
//    }
//
//    private Object extractValue(Map<String, Object> alert, String fieldName) {
//        String[] parts = fieldName.split("\\.");
//        Object current = alert;
//        for (String part : parts) {
//            if (current instanceof Map) {
//                current = ((Map<?, ?>) current).get(part);
//            } else {
//                return null;
//            }
//        }
//        return current;
//    }
//
//    private String escapeSQL(String input) {
//        // Basic SQL injection prevention
//        // In a production environment, consider using a more robust solution
//        return input.replaceAll("[^a-zA-Z0-9_]", "");
//    }
//}