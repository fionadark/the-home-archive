package com.thehomearchive.library.entity;

/**
 * Enum representing different user roles in the system.
 * Used for authorization and access control.
 */
public enum UserRole {
    
    /**
     * Regular user with standard access permissions.
     * Can manage their own library, search books, and perform standard user actions.
     */
    USER("User"),
    
    /**
     * Administrator with elevated privileges.
     * Can manage all users, access admin endpoints, and perform system administration tasks.
     */
    ADMIN("Administrator");
    
    private final String displayName;
    
    UserRole(String displayName) {
        this.displayName = displayName;
    }
    
    /**
     * Get the human-readable display name for this role.
     *
     * @return The display name
     */
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Check if this role has administrative privileges.
     *
     * @return true if this is an admin role, false otherwise
     */
    public boolean isAdmin() {
        return this == ADMIN;
    }
    
    /**
     * Get the default role for new users.
     *
     * @return The default user role
     */
    public static UserRole getDefaultRole() {
        return USER;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}