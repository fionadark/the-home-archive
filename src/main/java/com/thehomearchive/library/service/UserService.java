package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.auth.UserRegistrationRequest;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.entity.UserRole;
import com.thehomearchive.library.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service class for user management operations.
 * Handles user registration, profile management, and user-related business logic.
 */
@Service
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    
    @Autowired
    public UserService(UserRepository userRepository, 
                      PasswordEncoder passwordEncoder,
                      EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }
    
    /**
     * Register a new user with email verification.
     * Creates user account and sends verification email.
     *
     * @param registrationRequest the registration details
     * @return the created user
     * @throws IllegalArgumentException if email already exists
     */
    public User registerUser(@Valid UserRegistrationRequest registrationRequest) {
        // Check if email already exists
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists: " + registrationRequest.getEmail());
        }
        
        // Create new user
        User user = new User();
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registrationRequest.getPassword()));
        user.setFirstName(registrationRequest.getFirstName());
        user.setLastName(registrationRequest.getLastName());
        user.setRole(UserRole.USER);
        user.setEmailVerified(false);
        
        // Save user
        User savedUser = userRepository.save(user);
        
        // Send verification email
        emailService.sendVerificationEmail(savedUser);
        
        return savedUser;
    }
    
    /**
     * Find a user by email address.
     *
     * @param email the email address to search for
     * @return Optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    /**
     * Find a user by ID.
     *
     * @param id the user ID to search for
     * @return Optional containing the user if found
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    /**
     * Check if an email address is already registered.
     *
     * @param email the email address to check
     * @return true if email exists, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
    
    /**
     * Verify a user's email address.
     * Marks the user as email verified.
     *
     * @param user the user to verify
     * @return the updated user
     */
    public User verifyUserEmail(User user) {
        user.setEmailVerified(true);
        user.setEmailVerifiedAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    /**
     * Update user's last login timestamp.
     *
     * @param user the user who logged in
     * @return the updated user
     */
    public User updateLastLogin(User user) {
        user.setLastLoginAt(LocalDateTime.now());
        return userRepository.save(user);
    }
    
    /**
     * Update user's password.
     * Encodes the new password before saving.
     *
     * @param user the user to update
     * @param newPassword the new password (plain text)
     * @return the updated user
     */
    public User updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
    
    /**
     * Update user's profile information.
     *
     * @param user the user to update
     * @param firstName the new first name
     * @param lastName the new last name
     * @return the updated user
     */
    public User updateProfile(User user, String firstName, String lastName) {
        user.setFirstName(firstName);
        user.setLastName(lastName);
        return userRepository.save(user);
    }
    
    /**
     * Get all verified users.
     * Used for admin operations and analytics.
     *
     * @return list of verified users
     */
    @Transactional(readOnly = true)
    public List<User> getVerifiedUsers() {
        return userRepository.findByEmailVerified(true);
    }
    
    /**
     * Get users who haven't verified their email.
     * Used for cleanup and re-engagement campaigns.
     *
     * @return list of unverified users
     */
    @Transactional(readOnly = true)
    public List<User> getUnverifiedUsers() {
        return userRepository.findByEmailVerified(false);
    }
    
    /**
     * Get recently registered users.
     * Used for analytics and monitoring.
     *
     * @param since the date threshold for "recent"
     * @return list of users registered since the specified date
     */
    @Transactional(readOnly = true)
    public List<User> getRecentUsers(LocalDateTime since) {
        return userRepository.findByCreatedAtAfter(since);
    }
    
    /**
     * Get inactive users who haven't logged in recently.
     * Used for cleanup and re-engagement.
     *
     * @param lastLoginThreshold the date threshold for inactivity
     * @return list of inactive users
     */
    @Transactional(readOnly = true)
    public List<User> getInactiveUsers(LocalDateTime lastLoginThreshold) {
        return userRepository.findInactiveUsers(lastLoginThreshold);
    }
    
    /**
     * Get count of verified users.
     * Used for metrics and reporting.
     *
     * @return count of verified users
     */
    @Transactional(readOnly = true)
    public long getVerifiedUserCount() {
        return userRepository.countVerifiedUsers();
    }
    
    /**
     * Get count of users registered in a time period.
     * Used for analytics and growth metrics.
     *
     * @param startDate the start of the time period
     * @param endDate the end of the time period
     * @return count of users registered in the period
     */
    @Transactional(readOnly = true)
    public long getUserRegistrationCount(LocalDateTime startDate, LocalDateTime endDate) {
        return userRepository.countUsersRegisteredBetween(startDate, endDate);
    }
    
    /**
     * Delete a user account.
     * This will cascade to related entities.
     *
     * @param user the user to delete
     */
    public void deleteUser(User user) {
        userRepository.delete(user);
    }
    
    /**
     * Save or update a user.
     *
     * @param user the user to save
     * @return the saved user
     */
    public User saveUser(User user) {
        return userRepository.save(user);
    }
}