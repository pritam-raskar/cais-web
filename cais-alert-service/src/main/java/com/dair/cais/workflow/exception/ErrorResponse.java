package com.dair.cais.workflow.exception;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

/**
 * Response object for error scenarios in workflow operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
}
