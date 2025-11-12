package com.thehomearchive.library.controller;

import com.thehomearchive.library.dto.auth.EmailVerificationRequest;
import com.thehomearchive.library.dto.auth.LoginRequest;
import com.thehomearchive.library.dto.auth.UserRegistrationRequest;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.service.AuthenticationService;
import com.thehomearchive.library.service.EmailService;
import com.thehomearchive.library.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for authentication operations.
 * Handles user registration, login, logout, and email verification endpoints.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {
    
    private final UserService userService;
    private final AuthenticationService authenticationService;
    private final EmailService emailService;
    
    @Autowired
    public AuthController(UserService userService,
                         AuthenticationService authenticationService,
                         EmailService emailService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
        this.emailService = emailService;
    }
    
    /**
     * Register a new user account.
     *
     * @param registrationRequest the user registration details
     * @return ResponseEntity with registration result
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> registerUser(@Valid @RequestBody UserRegistrationRequest registrationRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Check if email already exists
            if (userService.emailExists(registrationRequest.getEmail())) {
                response.put("success", false);
                response.put("message", "Email address is already registered");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }
            
            // Register user
            User user = userService.registerUser(registrationRequest);
            
            // Prepare success response
            response.put("success", true);
            response.put("message", "Registration successful. Please check your email for verification instructions.");
            response.put("userId", user.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Registration failed. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Authenticate user login.
     *
     * @param loginRequest the login credentials
     * @return ResponseEntity with authentication result and JWT tokens
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Authenticate user
            Map<String, Object> authResult = authenticationService.login(loginRequest);
            
            // Prepare success response
            response.put("success", true);
            response.put("message", "Login successful");
            response.putAll(authResult);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Login failed. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Verify user email with verification token.
     *
     * @param verificationRequest the email verification token
     * @return ResponseEntity with verification result
     */
    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@Valid @RequestBody EmailVerificationRequest verificationRequest) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Verify email
            emailService.verifyEmail(verificationRequest.getToken());
            
            // Prepare success response
            response.put("success", true);
            response.put("message", "Email verified successfully. You can now log in.");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Email verification failed. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Logout user by invalidating their session.
     *
     * @param request the HTTP request containing Authorization header
     * @return ResponseEntity with logout result
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "No authentication token provided");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            String token = authHeader.substring(7);
            boolean loggedOut = authenticationService.logout(token);
            
            if (loggedOut) {
                response.put("success", true);
                response.put("message", "Logout successful");
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Logout failed. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Refresh JWT access token using refresh token.
     *
     * @param request the refresh token request
     * @return ResponseEntity with new tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<Map<String, Object>> refreshToken(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String refreshToken = request.get("refreshToken");
            
            if (refreshToken == null || refreshToken.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Refresh token is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            // Refresh tokens
            Map<String, Object> refreshResult = authenticationService.refreshToken(refreshToken);
            
            // Prepare success response
            response.put("success", true);
            response.put("message", "Token refreshed successfully");
            response.putAll(refreshResult);
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Token refresh failed. Please try again.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Get current user profile from JWT token.
     *
     * @param request the HTTP request containing Authorization header
     * @return ResponseEntity with user profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String authHeader = request.getHeader("Authorization");
            
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                response.put("success", false);
                response.put("message", "No authentication token provided");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
            String token = authHeader.substring(7);
            Optional<User> userOpt = authenticationService.getUserFromToken(token);
            
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("email", user.getEmail());
                userInfo.put("firstName", user.getFirstName());
                userInfo.put("lastName", user.getLastName());
                userInfo.put("role", user.getRole().toString());
                userInfo.put("emailVerified", user.getEmailVerified());
                
                response.put("success", true);
                response.put("user", userInfo);
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Invalid or expired token");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to get user profile");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * Resend email verification.
     *
     * @param request the request containing email address
     * @return ResponseEntity with result
     */
    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerification(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email address is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }
            
            Optional<User> userOpt = userService.findByEmail(email);
            
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "No account found with this email address");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }
            
            User user = userOpt.get();
            emailService.resendVerificationEmail(user);
            
            response.put("success", true);
            response.put("message", "Verification email sent successfully");
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to resend verification email");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}