package com.dair.cais.reports.tabs.exception;

/**
 * Exception thrown when tab retrieval fails
 */
public class TabRetrievalException extends RuntimeException {
    public TabRetrievalException(String message) {
        super(message);
    }

    public TabRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
