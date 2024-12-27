package com.dair.cais.steps.exception;

public class StepNameAlreadyExistsException extends RuntimeException {
    public StepNameAlreadyExistsException(String stepName) {
        super(String.format("Step with name '%s' already exists in the system.", stepName));
    }
}