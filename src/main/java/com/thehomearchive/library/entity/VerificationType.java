package com.thehomearchive.library.entity;

/**
 * Enum representing different types of email verification.
 * Used to categorize different verification scenarios in the system.
 */
public enum VerificationType {
    
    /**
     * Email verification for new user registration.
     * Required to activate a newly registered account.
     */
    REGISTRATION("Registration Verification"),
    
    /**
     * Email verification for password reset requests.
     * Used to verify identity before allowing password reset.
     */
    PASSWORD_RESET("Password Reset Verification"),
    
    /**
     * Email verification for email address changes.
     * Required when a user wants to change their email address.
     */
    EMAIL_CHANGE("Email Change Verification"),
    
    /**
     * Email verification for account reactivation.
     * Used when reactivating a previously deactivated account.
     */
    ACCOUNT_REACTIVATION("Account Reactivation Verification");
    
    private final String displayName;
    
    VerificationType(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Get the human-readable display name for this verification type.
     *
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Get the default verification type for new registrations.
     *
     * @return The default verification type
     */
    public static VerificationType getDefaultType() {
        return REGISTRATION;
    }
    
    /**
     * Check if this verification type is for critical security operations.
     *
     * @return true if this is a security-critical verification, false otherwise
     */
    public boolean isSecurityCritical() {
        return this == PASSWORD_RESET || this == EMAIL_CHANGE || this == ACCOUNT_REACTIVATION;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}