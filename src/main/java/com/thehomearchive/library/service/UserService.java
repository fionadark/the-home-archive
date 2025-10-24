package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.auth.UserRegistrationRequest;
import com.thehomearchive.library.dto.response.ApiResponse;

/**
 * Service interface for user management operations.
 * Handles user registration, profile management, and user-related business logic.
 */
public interface UserService {
    
    /**
     * Register a new user with the provided registration details.
     *
     * @param request The user registration request containing user details
     * @return ApiResponse indicating success or failure of registration
     * @throws RuntimeException if email already exists or registration fails
     */
    ApiResponse<String> registerUser(UserRegistrationRequest request);
}