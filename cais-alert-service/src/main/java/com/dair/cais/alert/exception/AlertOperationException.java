package com.dair.cais.alert.exception;

/**
 * Exception thrown for alert-related operations failures.
 * This can include database operations, filtering issues, or any other alert processing errors.
 */
public class AlertOperationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new AlertOperationException with the specified detail message.
     *
     * @param message the detail message
     */
    public AlertOperationException(String message) {
        super(message);
    }

    /**
     * Constructs a new AlertOperationException with the specified detail message and cause.
     *
     * @param message the detail message
     * @param cause the cause of the exception
     */
    public AlertOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}