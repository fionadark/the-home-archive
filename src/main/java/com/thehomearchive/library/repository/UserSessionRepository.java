package com.thehomearchive.library.repository;

import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.entity.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for UserSession entity operations.
 * Provides data access methods for session management and JWT token handling.
 */
@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {
    
    /**
     * Find a session by session token.
     * Used for JWT token validation and session lookup.
     *
     * @param sessionToken the session token to search for
     * @return Optional containing the session if found, empty otherwise
     */
    Optional<UserSession> findBySessionToken(String sessionToken);
    
    /**
     * Find a session by refresh token.
     * Used for token refresh operations.
     *
     * @param refreshToken the refresh token to search for
     * @return Optional containing the session if found, empty otherwise
     */
    Optional<UserSession> findByRefreshToken(String refreshToken);
    
    /**
     * Find all active sessions for a user.
     * Used for session management and security monitoring.
     *
     * @param user the user to find sessions for
     * @return list of active sessions for the user
     */
    @Query("SELECT us FROM UserSession us WHERE us.user = :user AND us.active = true")
    List<UserSession> findActiveSessionsByUser(@Param("user") User user);
    
    /**
     * Find all sessions for a user (active and inactive).
     * Used for comprehensive session history and management.
     *
     * @param user the user to find sessions for
     * @return list of all sessions for the user
     */
    List<UserSession> findByUser(User user);
    
    /**
     * Find sessions by user ordered by creation date descending.
     * Used for displaying recent sessions to users.
     *
     * @param user the user to find sessions for
     * @return list of sessions ordered by most recent first
     */
    @Query("SELECT us FROM UserSession us WHERE us.user = :user ORDER BY us.createdAt DESC")
    List<UserSession> findByUserOrderByCreatedAtDesc(@Param("user") User user);
    
    /**
     * Find expired sessions.
     * Used for cleanup of expired sessions.
     *
     * @param currentTime the current timestamp to compare against
     * @return list of expired sessions
     */
    @Query("SELECT us FROM UserSession us WHERE us.expiryDate < :currentTime")
    List<UserSession> findExpiredSessions(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find sessions with expired refresh tokens.
     * Used for cleanup of sessions with expired refresh capabilities.
     *
     * @param currentTime the current timestamp to compare against
     * @return list of sessions with expired refresh tokens
     */
    @Query("SELECT us FROM UserSession us WHERE us.refreshExpiryDate < :currentTime")
    List<UserSession> findSessionsWithExpiredRefreshTokens(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find active sessions by IP address.
     * Used for security monitoring and suspicious activity detection.
     *
     * @param ipAddress the IP address to search for
     * @return list of active sessions from the specified IP
     */
    @Query("SELECT us FROM UserSession us WHERE us.ipAddress = :ipAddress AND us.active = true")
    List<UserSession> findActiveSessionsByIpAddress(@Param("ipAddress") String ipAddress);
    
    /**
     * Find sessions not accessed recently.
     * Used for identifying stale sessions for cleanup.
     *
     * @param lastAccessThreshold the threshold for last access time
     * @return list of sessions not accessed since the threshold
     */
    @Query("SELECT us FROM UserSession us WHERE us.lastAccessedAt < :threshold")
    List<UserSession> findStaleSessionsOlderThan(@Param("threshold") LocalDateTime lastAccessThreshold);
    
    /**
     * Check if a user has any active sessions.
     * Used for determining if a user is currently logged in.
     *
     * @param user the user to check
     * @return true if user has active sessions, false otherwise
     */
    @Query("SELECT COUNT(us) > 0 FROM UserSession us WHERE us.user = :user AND us.active = true AND us.expiryDate > :currentTime")
    boolean hasActiveSessions(@Param("user") User user, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Count active sessions for a user.
     * Used for session limit enforcement and monitoring.
     *
     * @param user the user to count sessions for
     * @return count of active sessions for the user
     */
    @Query("SELECT COUNT(us) FROM UserSession us WHERE us.user = :user AND us.active = true AND us.expiryDate > :currentTime")
    long countActiveSessionsByUser(@Param("user") User user, @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Revoke all active sessions for a user.
     * Used for logout all devices and security responses.
     *
     * @param user the user whose sessions should be revoked
     * @param revokedAt the timestamp when sessions were revoked
     * @return number of sessions revoked
     */
    @Modifying
    @Query("UPDATE UserSession us SET us.active = false, us.revokedAt = :revokedAt WHERE us.user = :user AND us.active = true")
    int revokeAllUserSessions(@Param("user") User user, @Param("revokedAt") LocalDateTime revokedAt);
    
    /**
     * Delete expired sessions.
     * Used for scheduled cleanup of expired sessions.
     *
     * @param currentTime the current timestamp to compare against
     * @return number of deleted sessions
     */
    @Modifying
    @Query("DELETE FROM UserSession us WHERE us.expiryDate < :currentTime")
    int deleteExpiredSessions(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Delete old revoked sessions.
     * Used for cleanup of old inactive sessions.
     *
     * @param cutoffDate the date threshold for deletion
     * @return number of deleted sessions
     */
    @Modifying
    @Query("DELETE FROM UserSession us WHERE us.active = false AND us.revokedAt < :cutoffDate")
    int deleteOldRevokedSessions(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Delete all sessions for a user.
     * Used when deleting a user account.
     *
     * @param user the user whose sessions should be deleted
     */
    @Modifying
    @Query("DELETE FROM UserSession us WHERE us.user = :user")
    void deleteByUser(@Param("user") User user);
    
    /**
     * Update last accessed time for a session.
     * Used for tracking session activity.
     *
     * @param sessionToken the session token to update
     * @param lastAccessedAt the new last accessed timestamp
     * @return number of updated records
     */
    @Modifying
    @Query("UPDATE UserSession us SET us.lastAccessedAt = :lastAccessedAt WHERE us.sessionToken = :sessionToken")
    int updateLastAccessedTime(@Param("sessionToken") String sessionToken, 
                              @Param("lastAccessedAt") LocalDateTime lastAccessedAt);
}