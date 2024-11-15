package com.dair.cais.reports.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidQueryException extends RuntimeException {
    private final String details;

    public InvalidQueryException(String message) {
        super(message);
        this.details = null;
    }

    public InvalidQueryException(String message, String details) {
        super(message);
        this.details = details;
    }

    public InvalidQueryException(String message, Throwable cause) {
        super(message, cause);
        this.details = null;
    }

    public String getDetails() {
        return details;
    }
}