package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.auth.LoginRequest;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.entity.UserSession;
import com.thehomearchive.library.repository.UserSessionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service class for authentication operations.
 * Handles login, logout, token refresh, and authentication-related business logic.
 */
@Service
@Transactional
public class AuthenticationService {
    
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserSessionRepository userSessionRepository;
    
    @Autowired
    public AuthenticationService(UserService userService,
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService,
                               UserSessionRepository userSessionRepository) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userSessionRepository = userSessionRepository;
    }
    
    /**
     * Authenticate a user with the provided login credentials.
     *
     * @param request The login request containing email and password
     * @return Map containing JWT tokens and user information
     * @throws IllegalArgumentException if credentials are invalid or user is not verified
     */
    public Map<String, Object> login(@Valid LoginRequest request) {
        // Find user by email
        Optional<User> userOpt = userService.findByEmail(request.getEmail());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        User user = userOpt.get();
        
        // Check password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }
        
        // Check if email is verified
        if (!user.getEmailVerified()) {
            throw new IllegalArgumentException("Please verify your email before logging in");
        }
        
        // Generate JWT tokens
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        // Create user session
        LocalDateTime expiryDate = LocalDateTime.now().plusHours(1); // 1 hour for access token
        LocalDateTime refreshExpiryDate = LocalDateTime.now().plusDays(7); // 7 days for refresh token
        
        UserSession session = new UserSession(user, accessToken, refreshToken, expiryDate, refreshExpiryDate);
        userSessionRepository.save(session);
        
        // Update user's last login
        userService.updateLastLogin(user);
        
        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", accessToken);
        response.put("refreshToken", refreshToken);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", 3600); // 1 hour in seconds
        response.put("user", createUserInfo(user));
        
        return response;
    }
    
    /**
     * Logout a user by invalidating their session.
     *
     * @param token The access token to invalidate
     * @return true if logout successful
     */
    public boolean logout(String token) {
        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionToken(token);
        
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            session.revoke();
            userSessionRepository.save(session);
            return true;
        }
        
        return false;
    }
    
    /**
     * Refresh an access token using a refresh token.
     *
     * @param refreshToken The refresh token
     * @return Map containing new JWT tokens
     * @throws IllegalArgumentException if refresh token is invalid or expired
     */
    public Map<String, Object> refreshToken(String refreshToken) {
        Optional<UserSession> sessionOpt = userSessionRepository.findByRefreshToken(refreshToken);
        
        if (sessionOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid refresh token");
        }
        
        UserSession session = sessionOpt.get();
        
        if (!session.isRefreshValid()) {
            throw new IllegalArgumentException("Refresh token is expired or invalid");
        }
        
        User user = session.getUser();
        
        // Generate new tokens
        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        
        // Update session with new tokens
        session.setSessionToken(newAccessToken);
        session.setRefreshToken(newRefreshToken);
        session.setExpiryDate(LocalDateTime.now().plusHours(1));
        session.setRefreshExpiryDate(LocalDateTime.now().plusDays(7));
        session.updateLastAccessed();
        
        userSessionRepository.save(session);
        
        // Prepare response
        Map<String, Object> response = new HashMap<>();
        response.put("accessToken", newAccessToken);
        response.put("refreshToken", newRefreshToken);
        response.put("tokenType", "Bearer");
        response.put("expiresIn", 3600);
        
        return response;
    }
    
    /**
     * Logout all sessions for a user.
     *
     * @param user The user to logout all sessions for
     * @return number of sessions revoked
     */
    public int logoutAllSessions(User user) {
        return userSessionRepository.revokeAllUserSessions(user, LocalDateTime.now());
    }
    
    /**
     * Validate a JWT token and return the associated user session.
     *
     * @param token The JWT token to validate
     * @return Optional containing the user session if valid
     */
    @Transactional(readOnly = true)
    public Optional<UserSession> validateToken(String token) {
        if (!jwtService.isTokenValid(token)) {
            return Optional.empty();
        }
        
        Optional<UserSession> sessionOpt = userSessionRepository.findBySessionToken(token);
        
        if (sessionOpt.isPresent()) {
            UserSession session = sessionOpt.get();
            if (session.isValid()) {
                // Update last accessed time
                session.updateLastAccessed();
                userSessionRepository.save(session);
                return Optional.of(session);
            }
        }
        
        return Optional.empty();
    }
    
    /**
     * Get user information from a JWT token.
     *
     * @param token The JWT token
     * @return Optional containing the user if token is valid
     */
    @Transactional(readOnly = true)
    public Optional<User> getUserFromToken(String token) {
        Optional<UserSession> sessionOpt = validateToken(token);
        return sessionOpt.map(UserSession::getUser);
    }
    
    /**
     * Create user information for API responses.
     *
     * @param user The user entity
     * @return Map containing user information
     */
    private Map<String, Object> createUserInfo(User user) {
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("email", user.getEmail());
        userInfo.put("firstName", user.getFirstName());
        userInfo.put("lastName", user.getLastName());
        userInfo.put("role", user.getRole().toString());
        userInfo.put("emailVerified", user.getEmailVerified());
        return userInfo;
    }
}