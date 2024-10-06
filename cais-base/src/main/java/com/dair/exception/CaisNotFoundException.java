package com.dair.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class CaisNotFoundException extends CaisBaseException {

    public CaisNotFoundException() {
        super();
    }

    public CaisNotFoundException(final String message) {
        super(message);
    }

    public CaisNotFoundException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
