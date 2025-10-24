package com.thehomearchive.library.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Data Transfer Object for email verification requests.
 * Contains validation rules for email verification tokens.
 */
public class EmailVerificationRequest {
    
    @NotBlank(message = "Verification token is required")
    @Size(min = 1, max = 500, message = "Verification token must be between 1 and 500 characters")
    private String token;
    
    // Default constructor for Jackson
    public EmailVerificationRequest() {}
    
    // Constructor
    public EmailVerificationRequest(String token) {
        this.token = token;
    }
    
    // Getter
    public String getToken() {
        return token;
    }
    
    // Setter
    public void setToken(String token) {
        this.token = token;
    }
    
    @Override
    public String toString() {
        return "EmailVerificationRequest{" +
                "token='[HIDDEN]'" + // Hide token for security
                '}';
    }
}