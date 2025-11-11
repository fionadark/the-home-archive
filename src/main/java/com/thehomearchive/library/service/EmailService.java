package com.thehomearchive.library.service;

import com.thehomearchive.library.entity.EmailVerification;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.entity.VerificationType;
import com.thehomearchive.library.repository.EmailVerificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service class for email operations.
 * Handles email verification, sending verification emails, and email-related business logic.
 */
@Service
@Transactional
public class EmailService {
    
    private final EmailVerificationRepository emailVerificationRepository;
    
    @Autowired
    public EmailService(EmailVerificationRepository emailVerificationRepository) {
        this.emailVerificationRepository = emailVerificationRepository;
    }
    
    /**
     * Send verification email to user.
     * Creates a verification token and sends email (mock implementation for now).
     *
     * @param user the user to send verification email to
     */
    public void sendVerificationEmail(User user) {
        // Check if user already has a pending verification
        boolean hasPending = emailVerificationRepository.hasPendingVerification(
            user, VerificationType.REGISTRATION, LocalDateTime.now()
        );
        
        if (hasPending) {
            // For now, we'll allow resending - in production you might want to rate limit
            // throw new IllegalStateException("User already has a pending verification");
        }
        
        // Generate verification token
        String token = UUID.randomUUID().toString();
        
        // Create verification record
        EmailVerification verification = new EmailVerification();
        verification.setUser(user);
        verification.setVerificationToken(token);
        verification.setVerificationType(VerificationType.REGISTRATION);
        verification.setExpiryDate(LocalDateTime.now().plusHours(24)); // 24 hour expiry
        verification.setVerified(false);
        
        emailVerificationRepository.save(verification);
        
        // TODO: Actually send email - for now we'll just log it
        System.out.println("Verification email would be sent to: " + user.getEmail());
        System.out.println("Verification token: " + token);
        System.out.println("Verification URL: http://localhost:8080/api/verify-email.html?token=" + token);
    }
    
    /**
     * Verify an email using the provided verification token.
     *
     * @param token The email verification token
     * @return EmailVerification if verification successful
     * @throws IllegalArgumentException if token is invalid or expired
     */
    @Transactional
    public EmailVerification verifyEmail(String token) {
        // Find verification by token
        Optional<EmailVerification> verificationOpt = emailVerificationRepository.findByVerificationToken(token);
        
        if (verificationOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid verification token");
        }
        
        EmailVerification verification = verificationOpt.get();
        
        // Check if already verified
        if (verification.isVerified()) {
            throw new IllegalArgumentException("Email already verified");
        }
        
        // Check if expired
        if (verification.isExpired()) {
            throw new IllegalArgumentException("Verification token has expired");
        }
        
        // Mark verification as complete
        verification.markAsVerified();
        emailVerificationRepository.save(verification);
        
        return verification;
    }
    
    /**
     * Resend verification email for a user.
     *
     * @param user the user to resend verification for
     */
    public void resendVerificationEmail(User user) {
        if (user.getEmailVerified()) {
            throw new IllegalArgumentException("User email is already verified");
        }
        
        sendVerificationEmail(user);
    }
    
    /**
     * Clean up expired verification tokens.
     * This method should be called periodically to remove expired tokens.
     *
     * @return number of expired tokens removed
     */
    public int cleanupExpiredTokens() {
        return emailVerificationRepository.deleteExpiredVerifications(LocalDateTime.now());
    }
}