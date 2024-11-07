package com.dair.cais.exception;

import lombok.Getter;

@Getter
public class ConnectionValidationException extends RuntimeException {
    private final String details;

    public ConnectionValidationException(String message) {
        super(message);
        this.details = null;
    }

    public ConnectionValidationException(String message, String details) {
        super(message);
        this.details = details;
    }

    public ConnectionValidationException(String message, Throwable cause) {
        super(message, cause);
        this.details = null;
    }

    public ConnectionValidationException(String message, String details, Throwable cause) {
        super(message, cause);
        this.details = details;
    }
}