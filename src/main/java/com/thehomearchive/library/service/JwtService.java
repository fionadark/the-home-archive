package com.thehomearchive.library.service;

import com.thehomearchive.library.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Service class for JWT token management.
 * Handles JWT token generation, validation, and extraction of claims.
 */
@Service
public class JwtService {
    
    @Value("${jwt.secret:mySecretKey123456789012345678901234567890}")
    private String jwtSecret;
    
    @Value("${jwt.access-token-expiration:3600000}") // 1 hour in milliseconds
    private long accessTokenExpiration;
    
    @Value("${jwt.refresh-token-expiration:604800000}") // 7 days in milliseconds  
    private long refreshTokenExpiration;
    
    /**
     * Generate access token for user.
     *
     * @param user the user to generate token for
     * @return JWT access token
     */
    public String generateAccessToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("role", user.getRole().toString());
        claims.put("emailVerified", user.getEmailVerified());
        claims.put("tokenType", "access");
        
        return generateToken(claims, user.getEmail(), accessTokenExpiration);
    }
    
    /**
     * Generate refresh token for user.
     *
     * @param user the user to generate token for
     * @return JWT refresh token
     */
    public String generateRefreshToken(User user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", user.getId());
        claims.put("email", user.getEmail());
        claims.put("tokenType", "refresh");
        
        return generateToken(claims, user.getEmail(), refreshTokenExpiration);
    }
    
    /**
     * Generate JWT token with claims and subject.
     *
     * @param claims additional claims to include
     * @param subject the subject (usually username/email)
     * @param expiration expiration time in milliseconds
     * @return JWT token
     */
    private String generateToken(Map<String, Object> claims, String subject, long expiration) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);
        
        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * Extract username (email) from JWT token.
     *
     * @param token JWT token
     * @return username/email
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    /**
     * Extract user ID from JWT token.
     *
     * @param token JWT token
     * @return user ID
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }
    
    /**
     * Extract token type from JWT token.
     *
     * @param token JWT token
     * @return token type (access/refresh)
     */
    public String extractTokenType(String token) {
        return extractClaim(token, claims -> claims.get("tokenType", String.class));
    }
    
    /**
     * Extract expiration date from JWT token.
     *
     * @param token JWT token
     * @return expiration date
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    /**
     * Extract a specific claim from JWT token.
     *
     * @param token JWT token
     * @param claimsResolver function to extract specific claim
     * @param <T> type of claim
     * @return extracted claim
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    /**
     * Extract all claims from JWT token.
     *
     * @param token JWT token
     * @return all claims
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    
    /**
     * Check if JWT token is expired.
     *
     * @param token JWT token
     * @return true if expired, false otherwise
     */
    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (Exception e) {
            return true; // If we can't parse the token, consider it expired
        }
    }
    
    /**
     * Validate JWT token.
     *
     * @param token JWT token
     * @return true if valid, false otherwise
     */
    public boolean isTokenValid(String token) {
        try {
            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validate JWT token against a specific user.
     *
     * @param token JWT token
     * @param user user to validate against
     * @return true if valid for the user, false otherwise
     */
    public boolean isTokenValid(String token, User user) {
        try {
            final String username = extractUsername(token);
            final Long userId = extractUserId(token);
            return username.equals(user.getEmail()) 
                && userId.equals(user.getId()) 
                && !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get signing key for JWT tokens.
     *
     * @return signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Convert Date to LocalDateTime.
     *
     * @param date Date to convert
     * @return LocalDateTime
     */
    public LocalDateTime dateToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
    
    /**
     * Convert LocalDateTime to Date.
     *
     * @param localDateTime LocalDateTime to convert
     * @return Date
     */
    public Date localDateTimeToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
    
    /**
     * Get remaining time until token expiry in seconds.
     *
     * @param token JWT token
     * @return remaining seconds, or 0 if expired
     */
    public long getTokenRemainingTimeSeconds(String token) {
        try {
            Date expiration = extractExpiration(token);
            long remaining = (expiration.getTime() - System.currentTimeMillis()) / 1000;
            return Math.max(0, remaining);
        } catch (Exception e) {
            return 0;
        }
    }
}