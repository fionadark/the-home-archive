package com.homearchive.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Global exception handler for the Home Archive application.
 * Provides consistent error responses and follows constitutional requirements for error handling.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handle book not found exceptions.
     */
    @ExceptionHandler(BookNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleBookNotFoundException(BookNotFoundException ex) {
        String errorId = UUID.randomUUID().toString();
        logger.warn("Book not found exception [{}]: {}", errorId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "BOOK_NOT_FOUND",
            ex.getMessage(),
            LocalDateTime.now(),
            errorId
        );
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }
    
    /**
     * Handle book validation exceptions.
     */
    @ExceptionHandler(BookValidationException.class)
    public ResponseEntity<ErrorResponse> handleBookValidationException(BookValidationException ex) {
        String errorId = UUID.randomUUID().toString();
        logger.warn("Book validation exception [{}]: {}", errorId, ex.getMessage());
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            ex.getMessage(),
            LocalDateTime.now(),
            errorId
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle search exceptions.
     */
    @ExceptionHandler(SearchException.class)
    public ResponseEntity<ErrorResponse> handleSearchException(SearchException ex) {
        String errorId = UUID.randomUUID().toString();
        logger.error("Search exception [{}]: {}", errorId, ex.getMessage(), ex);
        
        ErrorResponse error = new ErrorResponse(
            "SEARCH_ERROR",
            ex.getMessage(),
            LocalDateTime.now(),
            errorId
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorId = UUID.randomUUID().toString();
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        
        logger.warn("Validation exception [{}]: {} field errors", errorId, errors.size());
        if (logger.isDebugEnabled()) {
            logger.debug("Validation errors [{}]: {}", errorId, errors);
        }
        
        ErrorResponse error = new ErrorResponse(
            "VALIDATION_ERROR",
            "Input validation failed",
            LocalDateTime.now(),
            errors
        );
        error.setErrorId(errorId);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Handle general exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex) {
        String errorId = UUID.randomUUID().toString();
        logger.error("Unexpected exception [{}]: {}", errorId, ex.getMessage(), ex);
        
        ErrorResponse error = new ErrorResponse(
            "INTERNAL_ERROR",
            "An unexpected error occurred",
            LocalDateTime.now(),
            errorId
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle database access exceptions.
     */
    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
        String errorId = UUID.randomUUID().toString();
        logger.error("Database access exception [{}]: {}", errorId, ex.getMessage(), ex);
        
        ErrorResponse error = new ErrorResponse(
            "DATABASE_ERROR",
            "Database operation failed",
            LocalDateTime.now(),
            errorId
        );
        return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * Handle method argument type mismatch exceptions.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String errorId = UUID.randomUUID().toString();
        Class<?> requiredType = ex.getRequiredType();
        String typeName = requiredType != null ? requiredType.getSimpleName() : "unknown";
        
        logger.warn("Method argument type mismatch [{}]: parameter '{}' with value '{}' could not be converted to type '{}'", 
                   errorId, ex.getName(), ex.getValue(), typeName);
        
        String message = String.format("Invalid value '%s' for parameter '%s'. Expected type: %s", 
                                     ex.getValue(), ex.getName(), typeName);
        
        ErrorResponse error = new ErrorResponse(
            "INVALID_PARAMETER",
            message,
            LocalDateTime.now(),
            errorId
        );
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }
    
    /**
     * Standardized error response structure.
     */
    public static class ErrorResponse {
        private String errorCode;
        private String message;
        private LocalDateTime timestamp;
        private String errorId;
        private Map<String, String> validationErrors;
        
        public ErrorResponse(String errorCode, String message, LocalDateTime timestamp) {
            this.errorCode = errorCode;
            this.message = message;
            this.timestamp = timestamp;
            this.errorId = UUID.randomUUID().toString();
        }
        
        public ErrorResponse(String errorCode, String message, LocalDateTime timestamp, String errorId) {
            this.errorCode = errorCode;
            this.message = message;
            this.timestamp = timestamp;
            this.errorId = errorId;
        }
        
        public ErrorResponse(String errorCode, String message, LocalDateTime timestamp, Map<String, String> validationErrors) {
            this.errorCode = errorCode;
            this.message = message;
            this.timestamp = timestamp;
            this.errorId = UUID.randomUUID().toString();
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
        
        public String getErrorId() {
            return errorId;
        }
        
        public void setErrorId(String errorId) {
            this.errorId = errorId;
        }
        
        public Map<String, String> getValidationErrors() {
            return validationErrors;
        }
        
        public void setValidationErrors(Map<String, String> validationErrors) {
            this.validationErrors = validationErrors;
        }
    }
}