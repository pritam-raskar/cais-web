package com.dair.cais.reports.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when there is an error retrieving reports from the system.
 * This is a runtime exception that maps to HTTP 500 (Internal Server Error).
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class ReportRetrievalException extends RuntimeException {

    /**
     * Constructs a new report retrieval exception with the specified detail message.
     *
     * @param message the detail message
     */
    public ReportRetrievalException(String message) {
        super(message);
    }

    /**
     * Constructs a new report retrieval exception with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public ReportRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs a new report retrieval exception with the specified cause.
     *
     * @param cause the cause of the exception
     */
    public ReportRetrievalException(Throwable cause) {
        super(cause);
    }
}