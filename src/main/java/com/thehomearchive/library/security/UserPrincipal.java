package com.thehomearchive.library.security;

import com.thehomearchive.library.entity.User;

/**
 * Interface representing the current authenticated user's principal.
 * Provides access to user ID and entity for authorization purposes.
 */
public interface UserPrincipal {
    /**
     * Get the unique identifier of the current user.
     * @return the user's ID
     */
    Long getId();

    /**
     * Get the User entity for the current user.
     * @return the User entity
     */
    User getUser();
}