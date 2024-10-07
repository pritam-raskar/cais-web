package com.dair.cais.alert;

import java.util.List;

public class AlertValidationException extends RuntimeException {
    private final List<String> errors;

    public AlertValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}