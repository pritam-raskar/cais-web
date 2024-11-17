package com.dair.cais.filter.exception;

public class FilterOperationException extends RuntimeException {
    public FilterOperationException(String message) {
        super(message);
    }

    public FilterOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
