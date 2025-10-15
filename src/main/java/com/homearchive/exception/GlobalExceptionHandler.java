package com.homearchive.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the Home Archive application.
 * Provides consistent error responses and follows constitutional requirements for error handling.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * Handle book not found exceptions.
     */
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFoundException(BookNotFoundException ex) {
        ErrorResponse error = new ErrorResponse(
            "BOOK_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle book validation exceptions.
     */
    @ExceptionHandler(BookValidationException.class)
    public ResponseEntity<ErrorResponse> handleBookValidationException(BookValidationException ex) {
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle search exceptions.
     */
    @ExceptionHandler(SearchException.class)
    public ResponseEntity<ErrorResponse> handleSearchException(SearchException ex) {
        ErrorResponse error = new ErrorResponse(
            "SEARCH_ERROR",
            ex.getMessage(),
            LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            "Input validation failed",
            LocalDateTime.now(),
            errors
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle general exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            LocalDateTime.now()
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Standardized error response structure.
     */
    public static class ErrorResponse {
        private String errorCode;
        private String message;
        private LocalDateTime timestamp;
        private Map<String, String> validationErrors;
        
        public ErrorResponse(String errorCode, String message, LocalDateTime timestamp) {
            this.errorCode = errorCode;
            this.message = message;
            this.timestamp = timestamp;
        }
        
        public ErrorResponse(String errorCode, String message, LocalDateTime timestamp, Map<String, String> validationErrors) {
            this.errorCode = errorCode;
            this.message = message;
            this.timestamp = timestamp;
            this.validationErrors = validationErrors;
        }
        
        // Getters and setters
        public String getErrorCode() {
            return errorCode;
        }
        
        public void setErrorCode(String errorCode) {
            this.errorCode = errorCode;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public LocalDateTime getTimestamp() {
            return timestamp;
        }
        
        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
        
        public Map<String, String> getValidationErrors() {
            return validationErrors;
        }
        
        public void setValidationErrors(Map<String, String> validationErrors) {
            this.validationErrors = validationErrors;
        }
    }
}