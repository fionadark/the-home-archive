package com.thehomearchive.library.exception;

/**
 * Exception thrown when attempting to create a duplicate rating.
 * A user can only rate a book once, enforced by unique constraint.
 */
public class DuplicateRatingException extends RuntimeException {
    
    public DuplicateRatingException(String message) {
        super(message);
    }
    
    public DuplicateRatingException(String message, Throwable cause) {
        super(message, cause);
    }
}