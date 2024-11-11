package com.dair.cais.reports.exception;


public class DuplicateReportIdentifierException extends RuntimeException {
    public DuplicateReportIdentifierException(String identifier) {
        super("Report identifier already exists: " + identifier);
    }
}