package com.dair.cais.exception;

import com.dair.cais.alert.exception.AlertOperationException;
import com.dair.cais.attachment.exception.AttachmentException;
import com.dair.cais.note.exception.NoteException;
import com.dair.cais.reports.exception.ReportRetrievalException;
import com.dair.cais.steps.exception.StepNameAlreadyExistsException;
import com.dair.cais.steps.exception.StepNotFoundException;
import com.dair.cais.workflow.exception.ErrorResponse;
import com.dair.cais.workflow.exception.ResourceAlreadyExistsException;
import com.dair.cais.workflow.exception.WorkflowUpdateException;
import com.dair.cais.workflow.exception.WorkflowValidationException;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
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

    private static final Map<String, String> CONSTRAINT_MAP = new HashMap<>();

    static {
        // Map constraint names to user-friendly error messages
        CONSTRAINT_MAP.put("fk_case_type_workflow", "The specified workflow does not exist");
        // Add more constraint mappings as needed
    }

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
     * Handles DataIntegrityViolationException which occurs on DB constraint violations.
     *
     * @param ex the exception
     * @return the error response
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, String>> handleDataIntegrityViolationException(DataIntegrityViolationException ex) {
        String message = "Data integrity violation occurred";
        HttpStatus status = HttpStatus.BAD_REQUEST;

        // Extract the specific constraint that was violated
        if (ex.getCause() instanceof ConstraintViolationException) {
            ConstraintViolationException constraintEx = (ConstraintViolationException) ex.getCause();
            String constraintName = constraintEx.getConstraintName();
            log.warn("Database constraint violation: {}", constraintName);

            // Special handling for the workflow foreign key constraint
            if (constraintName != null && constraintName.equals("fk_case_type_workflow")) {
                message = "The specified workflow does not exist";
            }
            // Handle other known constraints
            else if (constraintName != null && CONSTRAINT_MAP.containsKey(constraintName)) {
                message = CONSTRAINT_MAP.get(constraintName);
            }
            // Generic message for other foreign key constraints
            else if (constraintName != null &&
                    (constraintName.toLowerCase().startsWith("fk_") ||
                            constraintName.toLowerCase().contains("foreign"))) {
                message = "Referenced record does not exist";
            }
        } else {
            log.error("Data integrity violation occurred", ex);
        }

        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", message);
        return new ResponseEntity<>(errorResponse, status);
    }

    /**
     * Handles IllegalArgumentException for validation errors.
     *
     * @param ex the exception
     * @return the error response
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgumentException(IllegalArgumentException ex) {
        log.warn("Invalid argument: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
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

    @ExceptionHandler(AttachmentException.class)
    public ResponseEntity<Map<String, String>> handleAttachmentException(AttachmentException ex) {
        log.error("Attachment operation failed: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(NoteException.class)
    public ResponseEntity<Map<String, String>> handleNoteException(NoteException ex) {
        log.error("Note operation failed: {}", ex.getMessage());
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", ex.getMessage());
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handles step transition validation errors
     */
    @ExceptionHandler(com.dair.cais.alert.exception.AlertValidationException.class)
    public ResponseEntity<Object> handleAlertValidationException(
            com.dair.cais.alert.exception.AlertValidationException ex,
            WebRequest request) {
        log.warn("Alert validation failed: {}", ex.getMessage());
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Validation Error");
        body.put("message", ex.getMessage());
        body.put("validationErrors", ex.getErrors());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
    
    /**
     * Handles permission denied errors
     */
    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Object> handleSecurityException(
            SecurityException ex,
            WebRequest request) {
        log.warn("Security/Permission error: {}", ex.getMessage());
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        body.put("error", "Permission Denied");
        body.put("message", "You do not have permission to perform this operation");
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
    
    /**
     * Handles unexpected application errors (excluding framework/system errors)
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Object> handleApplicationException(
            RuntimeException ex,
            WebRequest request) {
        // Only handle application-level runtime exceptions
        // Let framework exceptions bubble up to container
        if (isFrameworkException(ex)) {
            throw ex; // Re-throw framework exceptions
        }
        
        log.error("Application error occurred: {}", ex.getClass().getSimpleName(), ex);
        
        String message = "An application error occurred. Please contact system administrator.";
        String errorCode = "APPLICATION_ERROR";
        
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", errorCode);
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
    
    /**
     * Check if exception is a framework/system exception that should bubble up
     */
    private boolean isFrameworkException(RuntimeException ex) {
        String className = ex.getClass().getName();
        return className.startsWith("org.springframework.beans.") ||
               className.startsWith("org.springframework.boot.") ||
               className.startsWith("org.springframework.context.") ||
               className.startsWith("org.springframework.dao.") ||
               className.startsWith("org.springframework.transaction.") ||
               className.startsWith("org.hibernate.") ||
               className.startsWith("javax.persistence.") ||
               className.startsWith("jakarta.persistence.") ||
               className.startsWith("java.lang.OutOfMemoryError") ||
               className.startsWith("java.lang.StackOverflowError");
    }
}