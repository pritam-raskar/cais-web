package com.dair.cais.reports.exception;


public class ConnectionNotFoundException extends RuntimeException {
    public ConnectionNotFoundException(Long connectionId) {
        super("Connection not found with ID: " + connectionId);
    }
}