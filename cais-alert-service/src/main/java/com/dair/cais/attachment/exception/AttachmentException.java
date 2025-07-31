package com.dair.cais.attachment.exception;

import com.dair.exception.CaisBaseException;

/**
 * Custom exception for attachment operations
 */
public class AttachmentException extends CaisBaseException {
    
    public AttachmentException(String message) {
        super(message);
    }
    
    public AttachmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
