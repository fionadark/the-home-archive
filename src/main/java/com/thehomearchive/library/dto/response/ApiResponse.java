package com.thehomearchive.library.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;

/**
 * Standard API response wrapper for all API endpoints.
 * Provides consistent structure for success and error responses.
 *
 * @param <T> Type of the response data
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    private boolean success;
    private String message;
    private T data;
    private String error;
    private LocalDateTime timestamp;
    
    // Private constructor to enforce builder usage
    private ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public boolean isSuccess() {
        return success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public T getData() {
        return data;
    }
    
    public String getError() {
        return error;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Create a successful response with data and message.
     *
     * @param data Response data
     * @param message Success message
     * @param <T> Type of response data
     * @return ApiResponse with success status
     */
    public static <T> ApiResponse<T> success(@NonNull T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.message = message;
        return response;
    }
    
    /**
     * Create a successful response with data only.
     *
     * @param data Response data
     * @param <T> Type of response data
     * @return ApiResponse with success status
     */
    public static <T> ApiResponse<T> success(@NonNull T data) {
        return success(data, null);
    }
    
    /**
     * Create a successful response with message only (no data).
     *
     * @param message Success message
     * @return ApiResponse with success status
     */
    public static <T> ApiResponse<T> success(String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.message = message;
        return response;
    }
    
    /**
     * Create an error response with error message.
     *
     * @param error Error message
     * @param <T> Type of response data
     * @return ApiResponse with error status
     */
    public static <T> ApiResponse<T> error(@NonNull String error) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = error;
        return response;
    }
    
    /**
     * Create an error response with error message and optional data.
     *
     * @param error Error message
     * @param data Optional error data (e.g., validation details)
     * @param <T> Type of response data
     * @return ApiResponse with error status
     */
    public static <T> ApiResponse<T> error(@NonNull String error, T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = false;
        response.error = error;
        response.data = data;
        return response;
    }
}