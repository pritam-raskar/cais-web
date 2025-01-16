package com.dair.cais.connection.validation;

public enum ConnectionErrorType {
    AUTHENTICATION_FAILED("Authentication failed"),
    NETWORK_ERROR("Network error"),
    DATABASE_NOT_FOUND("Database not found"),
    SERVER_UNREACHABLE("Server unreachable"),
    PORT_BLOCKED("Port blocked"),
    PERMISSION_DENIED("Permission denied"),
    INVALID_CONFIGURATION("Invalid configuration"),
    DRIVER_ERROR("Driver error"),
    UNKNOWN("Unknown error");

    private final String description;

    ConnectionErrorType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}