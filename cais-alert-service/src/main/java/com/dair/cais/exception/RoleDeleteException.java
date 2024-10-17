package com.dair.cais.exception;

import lombok.Getter;

@Getter
public class RoleDeleteException extends RuntimeException {
    private final long userCount;

    public RoleDeleteException(String message, long userCount) {
        super(message);
        this.userCount = userCount;
    }
}