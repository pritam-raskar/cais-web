package com.dair.cais.reports.exception;

public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(Integer reportId) {
        super("Report not found with ID: " + reportId);
    }
}