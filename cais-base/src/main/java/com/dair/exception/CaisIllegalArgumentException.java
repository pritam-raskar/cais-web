package com.dair.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class CaisIllegalArgumentException extends CaisBaseException {

    public CaisIllegalArgumentException() {
        super();
    }

    public CaisIllegalArgumentException(final String message) {
        super(message);
    }

    public CaisIllegalArgumentException(final String message, final Throwable throwable) {
        super(message, throwable);
    }

}
