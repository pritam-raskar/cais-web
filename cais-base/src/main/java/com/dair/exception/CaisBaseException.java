package com.dair.exception;

public class CaisBaseException extends RuntimeException {

    public CaisBaseException() {
        super();
    }

    public CaisBaseException(final String message) {
        super(message);
    }

    public CaisBaseException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
