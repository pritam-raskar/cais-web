package com.dair.cais.access.RolePolicyMapping;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class RolePolicyMappingException extends RuntimeException {
    public RolePolicyMappingException(String message) {
        super(message);
    }

    public RolePolicyMappingException(String message, Throwable cause) {
        super(message, cause);
    }
}