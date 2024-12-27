package com.dair.cais.workflow.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception thrown when attempting to create a workflow that already exists.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class WorkflowAlreadyExistsException extends RuntimeException {
    public WorkflowAlreadyExistsException(String message) {
        super(message);
    }
}