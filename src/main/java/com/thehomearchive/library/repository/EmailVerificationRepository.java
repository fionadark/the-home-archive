package com.thehomearchive.library.repository;

import com.thehomearchive.library.entity.EmailVerification;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.entity.VerificationType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for EmailVerification entity operations.
 * Provides data access methods for email verification token management.
 */
@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, Long> {
    
    /**
     * Find an email verification by token.
     * Used for token validation during verification process.
     *
     * @param token the verification token to search for
     * @return Optional containing the verification if found, empty otherwise
     */
    Optional<EmailVerification> findByToken(String token);
    
    /**
     * Find email verifications by user.
     * Used for managing user's verification tokens.
     *
     * @param user the user to search verifications for
     * @return list of email verifications for the user
     */
    List<EmailVerification> findByUser(User user);
    
    /**
     * Find email verifications by user and verification type.
     * Used for finding specific type of verifications for a user.
     *
     * @param user the user to search for
     * @param verificationType the type of verification
     * @return list of verifications matching user and type
     */
    List<EmailVerification> findByUserAndVerificationType(User user, VerificationType verificationType);
    
    /**
     * Find the most recent unverified token for a user and verification type.
     * Used for resending verification tokens or checking pending verifications.
     *
     * @param user the user to search for
     * @param verificationType the type of verification
     * @return Optional containing the most recent unverified token if found
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.user = :user " +
           "AND ev.verificationType = :verificationType " +
           "AND ev.verified = false " +
           "ORDER BY ev.createdAt DESC")
    Optional<EmailVerification> findLatestUnverifiedByUserAndType(@Param("user") User user, 
                                                                  @Param("verificationType") VerificationType verificationType);
    
    /**
     * Find expired email verifications.
     * Used for cleanup of expired tokens.
     *
     * @param currentTime the current timestamp to compare against
     * @return list of expired email verifications
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.expiryDate < :currentTime")
    List<EmailVerification> findExpiredVerifications(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Find unverified tokens older than a specific date.
     * Used for cleanup of stale verification tokens.
     *
     * @param cutoffDate the date threshold for token creation
     * @return list of unverified tokens created before the cutoff date
     */
    @Query("SELECT ev FROM EmailVerification ev WHERE ev.verified = false AND ev.createdAt < :cutoffDate")
    List<EmailVerification> findUnverifiedTokensOlderThan(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Check if a user has any pending verifications of a specific type.
     * Used for preventing duplicate verification requests.
     *
     * @param user the user to check
     * @param verificationType the type of verification
     * @return true if user has pending verifications of the specified type
     */
    @Query("SELECT COUNT(ev) > 0 FROM EmailVerification ev WHERE ev.user = :user " +
           "AND ev.verificationType = :verificationType " +
           "AND ev.verified = false " +
           "AND ev.expiryDate > :currentTime")
    boolean hasPendingVerification(@Param("user") User user, 
                                 @Param("verificationType") VerificationType verificationType,
                                 @Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Delete all verifications for a user.
     * Used when deleting a user account.
     *
     * @param user the user whose verifications should be deleted
     */
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.user = :user")
    void deleteByUser(@Param("user") User user);
    
    /**
     * Delete expired email verifications.
     * Used for scheduled cleanup of expired tokens.
     *
     * @param currentTime the current timestamp to compare against
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.expiryDate < :currentTime")
    int deleteExpiredVerifications(@Param("currentTime") LocalDateTime currentTime);
    
    /**
     * Delete old verified tokens to keep database clean.
     * Used for scheduled cleanup of old successful verifications.
     *
     * @param cutoffDate the date threshold for deletion
     * @return number of deleted records
     */
    @Modifying
    @Query("DELETE FROM EmailVerification ev WHERE ev.verified = true AND ev.updatedAt < :cutoffDate")
    int deleteOldVerifiedTokens(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Count pending verifications by type.
     * Used for monitoring and metrics.
     *
     * @param verificationType the type of verification to count
     * @return count of pending verifications of the specified type
     */
    @Query("SELECT COUNT(ev) FROM EmailVerification ev WHERE ev.verificationType = :verificationType " +
           "AND ev.verified = false " +
           "AND ev.expiryDate > :currentTime")
    long countPendingVerificationsByType(@Param("verificationType") VerificationType verificationType,
                                       @Param("currentTime") LocalDateTime currentTime);
}