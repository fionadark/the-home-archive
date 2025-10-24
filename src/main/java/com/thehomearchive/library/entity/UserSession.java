package com.thehomearchive.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * UserSession entity for tracking active user sessions and JWT tokens.
 * Used for session management, token validation, and security auditing.
 */
@Entity
@Table(name = "user_sessions")
@EntityListeners(AuditingEntityListener.class)
public class UserSession {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull(message = "User is required")
    private User user;
    
    @Column(nullable = false, unique = true, length = 1000)
    @NotBlank(message = "Session token is required")
    @Size(max = 1000, message = "Session token must not exceed 1000 characters")
    private String sessionToken;
    
    @Column(nullable = false, length = 1000)
    @NotBlank(message = "Refresh token is required")
    @Size(max = 1000, message = "Refresh token must not exceed 1000 characters")
    private String refreshToken;
    
    @Column(nullable = false)
    @NotNull(message = "Expiry date is required")
    private LocalDateTime expiryDate;
    
    @Column(nullable = false)
    @NotNull(message = "Refresh expiry date is required")
    private LocalDateTime refreshExpiryDate;
    
    @Column(nullable = false)
    private Boolean active = true;
    
    @Column(length = 500)
    @Size(max = 500, message = "User agent must not exceed 500 characters")
    private String userAgent;
    
    @Column(length = 45)
    @Size(max = 45, message = "IP address must not exceed 45 characters")
    private String ipAddress;
    
    @Column(nullable = false, updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime lastAccessedAt;
    
    @Column
    private LocalDateTime revokedAt;
    
    // Default constructor for JPA
    public UserSession() {}
    
    // Constructor for creating new sessions
    public UserSession(User user, String sessionToken, String refreshToken, 
                       LocalDateTime expiryDate, LocalDateTime refreshExpiryDate) {
        this.user = user;
        this.sessionToken = sessionToken;
        this.refreshToken = refreshToken;
        this.expiryDate = expiryDate;
        this.refreshExpiryDate = refreshExpiryDate;
        this.active = true;
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    // Getters
    public Long getId() {
        return id;
    }
    
    public User getUser() {
        return user;
    }
    
    public String getSessionToken() {
        return sessionToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }
    
    public LocalDateTime getRefreshExpiryDate() {
        return refreshExpiryDate;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public LocalDateTime getLastAccessedAt() {
        return lastAccessedAt;
    }
    
    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }
    
    // Setters
    public void setId(Long id) {
        this.id = id;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public void setSessionToken(String sessionToken) {
        this.sessionToken = sessionToken;
    }
    
    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
    
    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }
    
    public void setRefreshExpiryDate(LocalDateTime refreshExpiryDate) {
        this.refreshExpiryDate = refreshExpiryDate;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    public void setLastAccessedAt(LocalDateTime lastAccessedAt) {
        this.lastAccessedAt = lastAccessedAt;
    }
    
    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }
    
    // Business methods
    
    /**
     * Check if this session has expired.
     *
     * @return true if session is expired, false otherwise
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiryDate);
    }
    
    /**
     * Check if the refresh token has expired.
     *
     * @return true if refresh token is expired, false otherwise
     */
    public boolean isRefreshExpired() {
        return LocalDateTime.now().isAfter(refreshExpiryDate);
    }
    
    /**
     * Check if this session is valid (active and not expired).
     *
     * @return true if session is valid, false otherwise
     */
    public boolean isValid() {
        return Boolean.TRUE.equals(active) && !isExpired() && revokedAt == null;
    }
    
    /**
     * Check if the refresh token is valid.
     *
     * @return true if refresh token is valid, false otherwise
     */
    public boolean isRefreshValid() {
        return Boolean.TRUE.equals(active) && !isRefreshExpired() && revokedAt == null;
    }
    
    /**
     * Revoke this session, marking it as inactive.
     */
    public void revoke() {
        this.active = false;
        this.revokedAt = LocalDateTime.now();
    }
    
    /**
     * Update the last accessed timestamp to current time.
     */
    public void updateLastAccessed() {
        this.lastAccessedAt = LocalDateTime.now();
    }
    
    /**
     * Check if this session is currently active.
     *
     * @return true if session is active, false otherwise
     */
    public boolean isActive() {
        return Boolean.TRUE.equals(active) && revokedAt == null;
    }
    
    // Lifecycle callbacks
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
        if (lastAccessedAt == null) {
            lastAccessedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // equals and hashCode based on session token (unique identifier)
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserSession that = (UserSession) o;
        return Objects.equals(sessionToken, that.sessionToken);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(sessionToken);
    }
    
    @Override
    public String toString() {
        return "UserSession{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", active=" + active +
                ", expiryDate=" + expiryDate +
                ", refreshExpiryDate=" + refreshExpiryDate +
                ", ipAddress='" + ipAddress + '\'' +
                ", createdAt=" + createdAt +
                ", lastAccessedAt=" + lastAccessedAt +
                ", revokedAt=" + revokedAt +
                '}';
    }
}