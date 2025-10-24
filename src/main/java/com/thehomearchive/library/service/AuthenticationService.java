package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.auth.LoginRequest;
import com.thehomearchive.library.dto.response.AuthResponse;

/**
 * Service interface for authentication operations.
 * Handles login, logout, token refresh, and authentication-related business logic.
 */
public interface AuthenticationService {
    
    /**
     * Authenticate a user with the provided login credentials.
     *
     * @param request The login request containing email and password
     * @return AuthResponse containing JWT tokens and user information
     * @throws RuntimeException if credentials are invalid or user is not verified
     */
    AuthResponse login(LoginRequest request);
}