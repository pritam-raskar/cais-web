package com.dair.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class CaisBadRequestException extends CaisBaseException {

    public CaisBadRequestException() {
        super();
    }

    public CaisBadRequestException(final String message) {
        super(message);
    }

    public CaisBadRequestException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
