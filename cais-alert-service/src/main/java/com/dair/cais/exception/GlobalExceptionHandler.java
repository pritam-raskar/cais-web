package com.dair.cais.exception;

import com.dair.cais.alert.exception.AlertOperationException;
import com.dair.cais.reports.exception.ReportRetrievalException;
import com.dair.cais.steps.exception.StepNameAlreadyExistsException;
import com.dair.cais.steps.exception.StepNotFoundException;
import com.dair.cais.workflow.exception.ErrorResponse;
import com.dair.cais.workflow.exception.ResourceAlreadyExistsException;
import com.dair.cais.workflow.exception.WorkflowUpdateException;
import com.dair.cais.workflow.exception.WorkflowValidationException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleEntityNotFoundException(EntityNotFoundException ex) {
        log.error("Entity not found: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(InvalidDataAccessApiUsageException.class)
    public ResponseEntity<Map<String, String>> handleInvalidDataAccessApiUsageException(InvalidDataAccessApiUsageException ex) {
        log.error("Data access error: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        String message = ex.getMessage();

        // Handle enum conversion errors
        if (message != null && message.contains("No enum constant")) {
            message = "Invalid connection type value in database. Please contact system administrator.";
        }

        errorResponse.put("error", message);
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalStateException(IllegalStateException ex) {
        log.error("Illegal state error: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", "An error occurred while processing the connection type. Please contact system administrator.");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }


    /**
     * Handles ReportRetrievalException and returns a standardized error response.
     *
     * @param ex the ReportRetrievalException
     * @param request the current web request
     * @return ResponseEntity containing error details
     */
    @ExceptionHandler(ReportRetrievalException.class)
    public ResponseEntity<Object> handleReportRetrievalException(
            ReportRetrievalException ex,
            WebRequest request) {

        log.error("Report retrieval failed", ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Report Retrieval Error");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }

    @ExceptionHandler(AlertOperationException.class)
    public ResponseEntity<Object> handleAlertOperationException(
            AlertOperationException ex,
            WebRequest request) {

        log.error("Alert operation failed", ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Alert Operation Error");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }

    @ExceptionHandler(StepNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleStepNotFoundException(StepNotFoundException ex) {
        log.error("Step not found: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(StepNameAlreadyExistsException.class)
    public ResponseEntity<Map<String, String>> handleStepNameAlreadyExistsException(StepNameAlreadyExistsException ex) {
        log.error("Step name already exists: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(WorkflowUpdateException.class)
    public ResponseEntity<Map<String, Object>> handleWorkflowUpdateException(
            WorkflowUpdateException ex,
            WebRequest request) {

        log.error("Workflow update failed", ex);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Workflow Update Error");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(body);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleResourceAlreadyExistsException(
            ResourceAlreadyExistsException ex, WebRequest request) {
        log.error("Resource already exists: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "CONFLICT",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }

    @ExceptionHandler(WorkflowValidationException.class)
    public ResponseEntity<ErrorResponse> handleWorkflowValidationException(
            WorkflowValidationException ex, WebRequest request) {
        log.error("Validation error: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(
                "VALIDATION_ERROR",
                ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}