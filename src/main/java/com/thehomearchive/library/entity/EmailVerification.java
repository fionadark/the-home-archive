package com.thehomearchive.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * EmailVerification entity for tracking email verification tokens.
 * Used to verify user email addresses during registration and other operations.
 */
@Entity
@Table(name = "email_verifications")
@EntityListeners(AuditingEntityListener.class)
public class EmailVerification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;
    
    @Column(nullable = false, unique = true, length = 500)
    @NotBlank(message = "Verification token is required")
    @Size(max = 500, message = "Verification token must not exceed 500 characters")
    private String verificationToken;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    @NotNull(message = "Verification type is required")
    private VerificationType verificationType;
    
    @Column(nullable = false)
    private Boolean verified = false;
    
    @Column(nullable = false)
    @NotNull(message = "Expiry date is required")
    private LocalDateTime expiryDate;
    
    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime verifiedAt;
    
    // Default constructor for JPA
    public EmailVerification() {}
    
    // Constructor for creating verification tokens
    public EmailVerification(User user, String verificationToken, VerificationType verificationType, LocalDateTime expiryDate) {
        this.user = user;
        this.verificationToken = verificationToken;
        this.verificationType = verificationType;
        this.expiryDate = expiryDate;
        this.verified = false;
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public String getVerificationToken() {
        return verificationToken;
    }
    
    public VerificationType getVerificationType() {
        return verificationType;
    }
    
    public Boolean getVerified() {
        return verified;
    }
    
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }
    
    public void setVerificationType(VerificationType verificationType) {
        this.verificationType = verificationType;
    }
    
    public void setVerified(Boolean verified) {
        this.verified = verified;
    }
    
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }
    
    // Business methods
    
    /**
     * Check if this verification token has expired.
     *
     * @return true if token is expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
    
    /**
     * Check if this verification token is valid (not expired and not already verified).
     *
     * @return true if token is valid, false otherwise
     */
    public boolean isValid() {
        return !isExpired() && !Boolean.TRUE.equals(verified);
    }
    
    /**
     * Mark this verification as completed.
     */
    public void markAsVerified() {
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
    }
    
    /**
     * Check if this verification has been completed.
     *
     * @return true if verification is completed, false otherwise
     */
    public boolean isVerified() {
        return Boolean.TRUE.equals(verified);
    }
    
    // Lifecycle callbacks
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
    
    // equals and hashCode based on verification token (unique identifier)
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmailVerification that = (EmailVerification) o;
        return Objects.equals(verificationToken, that.verificationToken);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(verificationToken);
    }
    
    @Override
    public String toString() {
        return "EmailVerification{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", verificationType=" + verificationType +
                ", verified=" + verified +
                ", expiryDate=" + expiryDate +
                ", createdAt=" + createdAt +
                ", verifiedAt=" + verifiedAt +
                '}';
    }
}