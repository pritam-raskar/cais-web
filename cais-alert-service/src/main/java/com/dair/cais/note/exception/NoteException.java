package com.dair.cais.note.exception;

import com.dair.exception.CaisBaseException;

/**
 * Custom exception for note operations
 */
public class NoteException extends CaisBaseException {
    
    public NoteException(String message) {
        super(message);
    }
    
    public NoteException(String message, Throwable cause) {
        super(message, cause);
    }
}
