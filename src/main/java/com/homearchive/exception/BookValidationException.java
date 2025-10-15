package com.homearchive.exception;

/**
 * Exception thrown when book validation fails.
 * Supports constitutional requirement for input validation at service layer.
 */
public class BookValidationException extends HomeArchiveException {
    
    public BookValidationException(String message) {
        super(message);
    }
    
    public BookValidationException(String field, String reason) {
        super("Validation failed for field '" + field + "': " + reason);
    }
    
    public BookValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}