package com.homearchive.exception;

/**
 * Exception thrown when search operations fail or encounter errors.
 * Supports constitutional requirement for proper error handling in search functionality.
 */
public class SearchException extends HomeArchiveException {
    
    public SearchException(String message) {
        super(message);
    }
    
    public SearchException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public SearchException(String operation, String reason) {
        super("Search operation '" + operation + "' failed: " + reason);
    }
}