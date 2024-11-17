package com.dair.cais.filter.exception;

public class FilterNotFoundException extends RuntimeException {
    public FilterNotFoundException(String message) {
        super(message);
    }

    public FilterNotFoundException(Long filterId) {
        super("Filter not found with ID: " + filterId);
    }
}
