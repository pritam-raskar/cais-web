// File: com/dair/cais/filter/exception/FilterValidationException.java
package com.dair.cais.filter.exception;

import java.util.List;

public class FilterValidationException extends RuntimeException {
    private final List<String> errors;

    public FilterValidationException(String message, List<String> errors) {
        super(message);
        this.errors = errors;
    }

    public List<String> getErrors() {
        return errors;
    }
}
