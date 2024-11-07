package com.dair.cais.connection.metadata;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
class DuplicateConnectionException extends RuntimeException {
    public DuplicateConnectionException(String message) {
        super(message);
    }
}