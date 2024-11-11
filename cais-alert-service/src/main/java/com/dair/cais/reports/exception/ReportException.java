package com.dair.cais.reports.exception;

/**
 * Base exception class for report-related exceptions
 */
public class ReportException extends RuntimeException {
    public ReportException(String message) {
        super(message);
    }

    public ReportException(String message, Throwable cause) {
        super(message, cause);
    }
}