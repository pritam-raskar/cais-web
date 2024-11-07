package com.dair.cais.connection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum ConnectionType {
    POSTGRESQL("postgresql"),
    MYSQL("mysql"),
    ORACLE("oracle"),
    MONGODB("mongodb"),
    REDSHIFT("redshift"),
    SNOWFLAKE("snowflake"),
    MARIADB("mariadb");

    private final String value;

    ConnectionType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
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