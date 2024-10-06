package com.dair.cais.connector;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "connector", schema = "info_alert")
@Data
public class Connector {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "connector_id_seq")
    @SequenceGenerator(name = "connector_id_seq", sequenceName = "connector_id_seq", allocationSize = 1)
    private Long connectorId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DatabaseType databaseType;

    @Column(nullable = false)
    private String url;

    private String username;
    private String password;
    private String driverClassName;

    // Additional fields for MongoDB
    private String databaseName;
    private String collectionName;

    // Existing fields...

    public enum DatabaseType {
        POSTGRESQL, MYSQL, ORACLE, MONGODB, REDSHIFT, BIGQUERY, SNOWFLAKE, ATHENA, HIVE, S3, DATABRICKS, STARBURST, TRINO, MARIADB
    }

    // Getter methods for MongoDB-specific fields
    public String getDatabaseName() {
        return databaseName;
    }

    public String getCollectionName() {
        return collectionName;
    }
}