package com.dair.cais.workflow.exception;

/**
 * Exception thrown when a workflow update operation fails.
 */
public class WorkflowUpdateException extends RuntimeException {
    public WorkflowUpdateException(String message) {
        super(message);
    }

    public WorkflowUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}