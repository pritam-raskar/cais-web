package com.dair.cais.reports.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.ZonedDateTime;

@Data
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
public class ApiError {
    private String code;
    private String message;
    private ZonedDateTime timestamp = ZonedDateTime.now();

    public ApiError(String notFound, String message) {

    }



    // Exception handlers
    @ExceptionHandler(ReportNotFoundException.class)
    public ResponseEntity<ApiError> handleReportNotFound(ReportNotFoundException ex) {
        log.error("Report not found: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ApiError("NOT_FOUND", ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(InvalidReportStateException.class)
    public ResponseEntity<ApiError> handleInvalidState(InvalidReportStateException ex) {
        log.error("Invalid report state: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ApiError("INVALID_STATE", ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(MetadataException.class)
    public ResponseEntity<ApiError> handleMetadataError(MetadataException ex) {
        log.error("Metadata error: {}", ex.getMessage());
        return new ResponseEntity<>(
                new ApiError("METADATA_ERROR", ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericError(Exception ex) {
        log.error("Unexpected error: {}", ex.getMessage(), ex);
        return new ResponseEntity<>(
                new ApiError("INTERNAL_ERROR", "An unexpected error occurred"),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}