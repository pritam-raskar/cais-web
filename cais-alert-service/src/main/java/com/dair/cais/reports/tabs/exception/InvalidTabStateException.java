package com.dair.cais.reports.tabs.exception;

/**
 * Exception thrown when tab is in an invalid state
 */
public class InvalidTabStateException extends RuntimeException {
    public InvalidTabStateException(String message) {
        super(message);
    }
}
