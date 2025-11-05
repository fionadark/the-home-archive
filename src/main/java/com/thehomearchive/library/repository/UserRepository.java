package com.thehomearchive.library.repository;

import com.thehomearchive.library.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity operations.
 * Provides data access methods for user management and authentication.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find a user by their email address.
     * Used for authentication and checking email uniqueness.
     *
     * @param email the email address to search for
     * @return Optional containing the user if found, empty otherwise
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Check if a user exists with the given email address.
     * Used for registration validation and duplicate checking.
     *
     * @param email the email address to check
     * @return true if a user exists with this email, false otherwise
     */
    boolean existsByEmail(String email);
    
    /**
     * Find all users with email verification status.
     * Used for admin operations and user management.
     *
     * @param emailVerified the email verification status to filter by
     * @return list of users with the specified verification status
     */
    List<User> findByEmailVerified(Boolean emailVerified);
    
    /**
     * Find users created after a specific date.
     * Used for analytics and recent user tracking.
     *
     * @param date the date threshold
     * @return list of users created after the specified date
     */
    List<User> findByCreatedAtAfter(LocalDateTime date);
    
    /**
     * Find users by role.
     * Used for role-based operations and admin functions.
     *
     * @param role the user role to filter by
     * @return list of users with the specified role
     */
    @Query("SELECT u FROM User u WHERE u.role = :role")
    List<User> findByRole(@Param("role") String role);
    
    /**
     * Find users who haven't logged in recently.
     * Used for identifying inactive users for cleanup or re-engagement.
     *
     * @param lastLoginThreshold the date threshold for last login
     * @return list of users who haven't logged in since the threshold
     */
    @Query("SELECT u FROM User u WHERE u.lastLoginAt IS NULL OR u.lastLoginAt < :threshold")
    List<User> findInactiveUsers(@Param("threshold") LocalDateTime lastLoginThreshold);
    
    /**
     * Find users with unverified emails older than a specific date.
     * Used for cleanup of stale unverified accounts.
     *
     * @param createdBefore the date threshold for account creation
     * @return list of unverified users created before the threshold
     */
    @Query("SELECT u FROM User u WHERE u.emailVerified = false AND u.createdAt < :createdBefore")
    List<User> findUnverifiedUsersOlderThan(@Param("createdBefore") LocalDateTime createdBefore);
    
    /**
     * Count total number of verified users.
     * Used for metrics and reporting.
     *
     * @return count of users with verified emails
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.emailVerified = true")
    long countVerifiedUsers();
    
    /**
     * Count users registered in a specific time period.
     * Used for registration analytics and reporting.
     *
     * @param startDate the start of the time period
     * @param endDate the end of the time period
     * @return count of users registered in the specified period
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :startDate AND :endDate")
    long countUsersRegisteredBetween(@Param("startDate") LocalDateTime startDate, 
                                   @Param("endDate") LocalDateTime endDate);
}