package com.dair.cais.connector;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
public class ConnectorService {

    private static final Logger log = LogManager.getLogger(ConnectorService.class);

    private final ConnectorRepository connectorRepository;

    @Autowired
    public ConnectorService(ConnectorRepository connectorRepository) {
        this.connectorRepository = connectorRepository;
    }

    public Connector saveConnector(Connector connector) {
        return connectorRepository.save(connector);
    }

    public Optional<Connector> getConnector(Long connectorId) {
        return connectorRepository.findById(connectorId);
    }

    public List<Connector> getAllConnectors() {
        return connectorRepository.findAll();
    }

    public void deleteConnector(Long connectorId) {
        connectorRepository.deleteById(connectorId);
    }

    public boolean testConnection(Connector connector) {
        try {
            switch (connector.getDatabaseType()) {
                case POSTGRESQL:
                case MYSQL:
                case ORACLE:
                case REDSHIFT:
                case SNOWFLAKE:
                case MARIADB:
//                case HIVE:
                case TRINO:
                    return testJdbcConnection(connector);
                case MONGODB:
                    return testMongoConnection(connector);
                default:
                    throw new UnsupportedOperationException("Unsupported database type: " + connector.getDatabaseType());
            }
        } catch (Exception e) {
            log.error("Error testing connection for " + connector.getDatabaseType(), e);
            return false;
        }
    }

    private boolean testJdbcConnection(Connector connector) {
        try (Connection conn = DriverManager.getConnection(connector.getUrl(), connector.getUsername(), connector.getPassword())) {
            return true;
        } catch (SQLException e) {
            log.error("JDBC connection test failed", e);
            return false;
        }
    }

    private boolean testMongoConnection(Connector connector) {
        try (MongoClient mongoClient = MongoClients.create(connector.getUrl())) {
            MongoDatabase database = mongoClient.getDatabase("testDatabase");  // Adjust the database name as needed
            // Perform a simple query to check the connection
            database.listCollectionNames();
            log.info("MongoDB connection test succeeded for URL: " + connector.getUrl());
            return true;
        } catch (Exception e) {
            log.error("MongoDB connection test failed", e);
            return false;
        }
    }
}




//package com.dair.connector;
//
//import org.apache.logging.log4j.LogManager;
//import org.apache.logging.log4j.Logger;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.sql.Connection;
//import java.sql.DriverManager;
//import java.sql.SQLException;
//import java.util.List;
//import java.util.Optional;
//
//@Service
//public class ConnectorService {
//
//    private static final Logger log = LogManager.getLogger(ConnectorService.class);
//
//    private final ConnectorRepository connectorRepository;
//
//    @Autowired
//    public ConnectorService(ConnectorRepository connectorRepository) {
//        this.connectorRepository = connectorRepository;
//    }
//
//    public Connector saveConnector(Connector connector) {
//        return connectorRepository.save(connector);
//    }
//
//    public Optional<Connector> getConnector(Long connectorId) {
//        return connectorRepository.findById(connectorId);
//    }
//
//    public List<Connector> getAllConnectors() {
//        return connectorRepository.findAll();
//    }
//
//    public void deleteConnector(Long connectorId) {
//        connectorRepository.deleteById(connectorId);
//    }
//
//    public boolean testConnection(Connector connector) {
//        try {
//            switch (connector.getDatabaseType()) {
//                case POSTGRESQL:
//                case MYSQL:
//                case ORACLE:
//                case REDSHIFT:
//                case SNOWFLAKE:
//                case MARIADB:
//                case HIVE:
//                case TRINO:
//                    return testJdbcConnection(connector);
////                case MONGODB:
////                    return testMongoConnection(connector);
//                default:
//                    throw new UnsupportedOperationException("Unsupported database type: " + connector.getDatabaseType());
//            }
//        } catch (Exception e) {
//            log.error("Error testing connection for " + connector.getDatabaseType(), e);
//            return false;
//        }
//    }
//
//    private boolean testJdbcConnection(Connector connector) {
//        try (Connection conn = DriverManager.getConnection(connector.getUrl(), connector.getUsername(), connector.getPassword())) {
//            return true;
//        } catch (SQLException e) {
//            log.error("JDBC connection test failed", e);
//            return false;
//        }
//    }
//
////    private boolean testMongoConnection(Connector connector) {
////        // Since we've removed the MongoDB client, we'll just check if the URL starts with "mongodb://"
////        if (connector.getUrl().startsWith("mongodb://")) {
////            log.info("MongoDB URL format is correct");
////            return true;
////        } else {
////            log.error("Invalid MongoDB URL format");
////            return false;
////        }
////    }
//}