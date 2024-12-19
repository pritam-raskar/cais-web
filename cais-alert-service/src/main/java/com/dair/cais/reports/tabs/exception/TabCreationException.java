package com.dair.cais.reports.tabs.exception;

/**
 * Exception thrown when tab creation fails
 */
public class TabCreationException extends RuntimeException {
    public TabCreationException(String message) {
        super(message);
    }

    public TabCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}

