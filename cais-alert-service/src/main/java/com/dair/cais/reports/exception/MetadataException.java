package com.dair.cais.reports.exception;

/**
 * Exception thrown when metadata operations fail
 */
public class MetadataException extends ReportException {
    public MetadataException(String message) {
        super(message);
    }

    public MetadataException(String message, Throwable cause) {
        super(message, cause);
    }
}