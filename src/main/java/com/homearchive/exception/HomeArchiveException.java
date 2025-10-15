package com.homearchive.exception;

/**
 * Base runtime exception for all Home Archive application exceptions.
 * Follows constitutional requirement for meaningful error messages.
 */
public class HomeArchiveException extends RuntimeException {
    
    public HomeArchiveException(String message) {
        super(message);
    }
    
    public HomeArchiveException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public HomeArchiveException(Throwable cause) {
        super(cause);
    }
}