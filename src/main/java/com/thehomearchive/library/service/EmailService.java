package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.response.ApiResponse;

/**
 * Service interface for email operations.
 * Handles email verification, sending verification emails, and email-related business logic.
 */
public interface EmailService {
    
    /**
     * Verify an email using the provided verification token.
     *
     * @param token The email verification token
     * @return ApiResponse indicating success or failure of verification
     * @throws RuntimeException if token is invalid or expired
     */
    ApiResponse<String> verifyEmail(String token);
}