package com.homearchive.exception;

/**
 * Exception thrown when a requested book is not found.
 * Supports book domain standards from constitution.
 */
public class BookNotFoundException extends HomeArchiveException {
    
    public BookNotFoundException(String message) {
        super(message);
    }
    
    public BookNotFoundException(Long bookId) {
        super("Book not found with ID: " + bookId);
    }
    
    public BookNotFoundException(String field, String value) {
        super("Book not found with " + field + ": " + value);
    }
}