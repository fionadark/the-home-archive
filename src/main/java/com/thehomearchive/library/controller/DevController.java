package com.thehomearchive.library.controller;

import com.thehomearchive.library.entity.EmailVerification;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.repository.EmailVerificationRepository;
import com.thehomearchive.library.repository.UserRepository;
import com.thehomearchive.library.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Development-only controller for testing and debugging purposes.
 * Only available when the 'dev' profile is active.
 */
@RestController
@RequestMapping("/dev")
@Profile("dev")
public class DevController {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DevController(EmailVerificationRepository emailVerificationRepository,
                        UserRepository userRepository,
                        EmailService emailService,
                        PasswordEncoder passwordEncoder) {
        this.emailVerificationRepository = emailVerificationRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * List all pending email verifications.
     * Useful for development/testing when emails aren't actually sent.
     *
     * @return List of pending verifications with tokens and user info
     */
    @GetMapping("/verifications")
    public ResponseEntity<Map<String, Object>> listPendingVerifications() {
        List<EmailVerification> pendingVerifications = emailVerificationRepository
                .findAll()
                .stream()
                .filter(verification -> !verification.isVerified() && !verification.isExpired())
                .collect(Collectors.toList());

        List<Map<String, Object>> verifications = pendingVerifications.stream()
                .map(verification -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", verification.getId());
                    info.put("token", verification.getVerificationToken());
                    info.put("userEmail", verification.getUser().getEmail());
                    info.put("userName", verification.getUser().getFirstName() + " " + verification.getUser().getLastName());
                    info.put("type", verification.getVerificationType().toString());
                    info.put("createdAt", verification.getCreatedAt());
                    info.put("expiryDate", verification.getExpiryDate());
                    info.put("verificationUrl", "http://localhost:8080/api/verify-email.html?token=" + verification.getVerificationToken());
                    return info;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", verifications.size());
        response.put("verifications", verifications);

        return ResponseEntity.ok(response);
    }

    /**
     * Manually verify a user's email by their email address.
     * Useful for development/testing purposes.
     *
     * @param request containing the email address to verify
     * @return verification result
     */
    @PostMapping("/verify-user")
    public ResponseEntity<Map<String, Object>> manuallyVerifyUser(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String email = request.get("email");
            if (email == null || email.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "Email address is required");
                return ResponseEntity.badRequest().body(response);
            }

            Optional<User> userOpt = userRepository.findByEmail(email);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "No user found with email: " + email);
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            
            if (user.getEmailVerified()) {
                response.put("success", false);
                response.put("message", "User email is already verified");
                return ResponseEntity.badRequest().body(response);
            }

            // Find pending verification
            List<EmailVerification> userVerifications = emailVerificationRepository.findByUser(user);
            List<EmailVerification> pendingVerifications = userVerifications.stream()
                    .filter(verification -> !verification.isVerified() && !verification.isExpired())
                    .collect(Collectors.toList());

            if (pendingVerifications.isEmpty()) {
                response.put("success", false);
                response.put("message", "No pending verification found for this user");
                return ResponseEntity.notFound().build();
            }

            // Use the first pending verification
            EmailVerification verification = pendingVerifications.get(0);
            verification.markAsVerified();
            emailVerificationRepository.save(verification);

            // Update user
            user.setEmailVerified(true);
            user.setEmailVerifiedAt(LocalDateTime.now());
            userRepository.save(user);

            response.put("success", true);
            response.put("message", "User email verified successfully");
            response.put("userEmail", user.getEmail());
            response.put("verifiedAt", user.getEmailVerifiedAt());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Failed to verify user: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * List all users with their verification status.
     *
     * @return List of users with verification info
     */
    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> listUsers() {
        List<User> users = userRepository.findAll();

        List<Map<String, Object>> userInfos = users.stream()
                .map(user -> {
                    Map<String, Object> info = new HashMap<>();
                    info.put("id", user.getId());
                    info.put("email", user.getEmail());
                    info.put("firstName", user.getFirstName());
                    info.put("lastName", user.getLastName());
                    info.put("emailVerified", user.getEmailVerified());
                    info.put("emailVerifiedAt", user.getEmailVerifiedAt());
                    info.put("createdAt", user.getCreatedAt());
                    info.put("role", user.getRole().toString());
                    return info;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("count", userInfos.size());
        response.put("users", userInfos);

        return ResponseEntity.ok(response);
    }

    /**
     * Clean up expired verification tokens.
     *
     * @return cleanup result
     */
    @PostMapping("/cleanup-expired")
    public ResponseEntity<Map<String, Object>> cleanupExpiredTokens() {
        Map<String, Object> response = new HashMap<>();

        try {
            int removedCount = emailService.cleanupExpiredTokens();
            response.put("success", true);
            response.put("message", "Cleanup completed");
            response.put("removedTokens", removedCount);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Cleanup failed: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * Generate BCrypt hash for a password.
     * Useful for generating password hashes for test data.
     *
     * @param request Map containing "password" key
     * @return BCrypt hash
     */
    @PostMapping("/hash-password")
    public ResponseEntity<Map<String, Object>> hashPassword(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        String password = request.get("password");
        if (password == null || password.isEmpty()) {
            response.put("success", false);
            response.put("message", "Password is required");
            return ResponseEntity.badRequest().body(response);
        }
        
        String hash = passwordEncoder.encode(password);
        response.put("success", true);
        response.put("password", password);
        response.put("hash", hash);
        
        return ResponseEntity.ok(response);
    }
}