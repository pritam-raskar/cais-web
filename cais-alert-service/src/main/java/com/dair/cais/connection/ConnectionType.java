package com.dair.cais.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum ConnectionType {
    POSTGRESQL("postgresql", "org.postgresql.Driver"),
    MYSQL("mysql", "com.mysql.cj.jdbc.Driver"),
    ORACLE("oracle", "oracle.jdbc.OracleDriver"),
    MONGODB("mongodb", "mongodb.jdbc.MongoDriver"),
    REDSHIFT("redshift", "com.amazon.redshift.jdbc42.Driver"),
    SNOWFLAKE("snowflake", "net.snowflake.client.jdbc.SnowflakeDriver"),
    MARIADB("mariadb", "org.mariadb.jdbc.Driver");

    private final String value;
    private final String driverClass;

    ConnectionType(String value, String driverClass) {
        this.value = value;
        this.driverClass = driverClass;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public String getDatabaseType() {
        return value;
    }

    @JsonCreator
    public static ConnectionType fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            log.error("Attempted to create ConnectionType from null or empty value");
            throw new IllegalArgumentException("Connection type cannot be null or empty");
        }

        String normalizedValue = value.trim().toLowerCase();
        for (ConnectionType type : ConnectionType.values()) {
            if (type.value.equalsIgnoreCase(normalizedValue)) {
                return type;
            }
        }

        log.error("Invalid connection type provided: {}", value);
        throw new IllegalArgumentException(
                String.format("Unknown connection type: %s. Supported types are: %s",
                        value, java.util.Arrays.toString(ConnectionType.values()))
        );
    }

    public String getJdbcUrl(ConnectionDetails details) {
        switch (this) {
            case POSTGRESQL:
            case MYSQL:
            case MARIADB:
            case REDSHIFT:
                return String.format("jdbc:%s://%s:%d/%s",
                        this.getDatabaseType(),
                        details.getHost(),
                        details.getPort(),
                        details.getDatabase());

            case SNOWFLAKE:
                return String.format("jdbc:snowflake://%s.snowflakecomputing.com/?db=%s&warehouse=%s",
                        details.getHost(),
                        details.getDatabase(),
                        details.getAdditionalParams());

            case ORACLE:
                return String.format("jdbc:oracle:thin:@%s:%d/%s",
                        details.getHost(),
                        details.getPort(),
                        details.getDatabase());

            case MONGODB:
                return String.format("mongodb://%s:%d/%s",
                        details.getHost(),
                        details.getPort(),
                        details.getDatabase());

            default:
                throw new IllegalStateException("Unsupported database type: " + this.name());
        }
    }

    public static boolean isSupported(String value) {
        try {
            fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    public String toString() {
        return value;
    }

    @Converter(autoApply = true)
    public static class ConnectionTypeConverter implements AttributeConverter<ConnectionType, String> {
        @Override
        public String convertToDatabaseColumn(ConnectionType attribute) {
            if (attribute == null) {
                log.warn("Null ConnectionType being converted to database column");
                return null;
            }
            return attribute.getValue();
        }

        @Override
        public ConnectionType convertToEntityAttribute(String dbData) {
            if (dbData == null || dbData.trim().isEmpty()) {
                log.warn("Null or empty database value being converted to ConnectionType");
                return null;
            }
            try {
                return ConnectionType.fromString(dbData);
            } catch (IllegalArgumentException e) {
                log.error("Error converting database value '{}' to ConnectionType", dbData, e);
                throw new IllegalStateException("Invalid connection type in database: " + dbData, e);
            }
        }
    }
}


//package com.dair.cais.connection;
//
//import com.fasterxml.jackson.annotation.JsonCreator;
//import com.fasterxml.jackson.annotation.JsonValue;
//import jakarta.persistence.AttributeConverter;
//import jakarta.persistence.Converter;
//import lombok.extern.slf4j.Slf4j;
//
//@Slf4j
//public enum ConnectionType {
//    POSTGRESQL("postgresql"),
//    MYSQL("mysql"),
//    ORACLE("oracle"),
//    MONGODB("mongodb"),
//    REDSHIFT("redshift"),
//    SNOWFLAKE("snowflake"),
//    MARIADB("mariadb");
//
//    private final String value;
//
//    ConnectionType(String value) {
//        this.value = value;
//    }
//
//    @JsonValue
//    public String getValue() {
//        return value;
//    }
//
//    @JsonCreator
//    public static ConnectionType fromString(String value) {
//        if (value == null || value.trim().isEmpty()) {
//            log.error("Attempted to create ConnectionType from null or empty value");
//            throw new IllegalArgumentException("Connection type cannot be null or empty");
//        }
//
//        String normalizedValue = value.trim().toLowerCase();
//        for (ConnectionType type : ConnectionType.values()) {
//            if (type.value.equalsIgnoreCase(normalizedValue)) {
//                return type;
//            }
//        }
//
//        log.error("Invalid connection type provided: {}", value);
//        throw new IllegalArgumentException(
//                String.format("Unknown connection type: %s. Supported types are: %s",
//                        value, java.util.Arrays.toString(ConnectionType.values()))
//        );
//    }
//
//    public static boolean isSupported(String value) {
//        try {
//            fromString(value);
//            return true;
//        } catch (IllegalArgumentException e) {
//            return false;
//        }
//    }
//
//    @Override
//    public String toString() {
//        return value;
//    }
//
//    @Converter(autoApply = true)
//    public static class ConnectionTypeConverter implements AttributeConverter<ConnectionType, String> {
//        @Override
//        public String convertToDatabaseColumn(ConnectionType attribute) {
//            if (attribute == null) {
//                log.warn("Null ConnectionType being converted to database column");
//                return null;
//            }
//            return attribute.getValue();
//        }
//
//        @Override
//        public ConnectionType convertToEntityAttribute(String dbData) {
//            if (dbData == null || dbData.trim().isEmpty()) {
//                log.warn("Null or empty database value being converted to ConnectionType");
//                return null;
//            }
//            try {
//                return ConnectionType.fromString(dbData);
//            } catch (IllegalArgumentException e) {
//                log.error("Error converting database value '{}' to ConnectionType", dbData, e);
//                throw new IllegalStateException("Invalid connection type in database: " + dbData, e);
//            }
//        }
//    }
//}