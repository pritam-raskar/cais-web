package com.dair.cais.steps.exception;

public class StepNotFoundException extends RuntimeException {
    public StepNotFoundException(String message) {
        super(message);
    }
}