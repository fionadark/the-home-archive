package com.thehomearchive.library.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;

/**
 * Authentication response DTO for login and token refresh endpoints.
 * Contains JWT tokens and user information.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthResponse {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;
    private UserInfo user;
    private LocalDateTime timestamp;
    
    // Private constructor to enforce builder usage
    private AuthResponse() {
        this.tokenType = "Bearer";
        this.timestamp = LocalDateTime.now();
    }
    
    // Getters
    public String getAccessToken() {
        return accessToken;
    }
    
    public String getRefreshToken() {
        return refreshToken;
    }
    
    public String getTokenType() {
        return tokenType;
    }
    
    public Long getExpiresIn() {
        return expiresIn;
    }
    
    public UserInfo getUser() {
        return user;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    /**
     * Create authentication response with tokens and user info.
     *
     * @param accessToken JWT access token
     * @param refreshToken JWT refresh token
     * @param expiresIn Token expiration time in seconds
     * @param user User information
     * @return AuthResponse with token and user data
     */
    public static AuthResponse of(@NonNull String accessToken, String refreshToken, 
                                  Long expiresIn, @NonNull UserInfo user) {
        AuthResponse response = new AuthResponse();
        response.accessToken = accessToken;
        response.refreshToken = refreshToken;
        response.expiresIn = expiresIn;
        response.user = user;
        return response;
    }
    
    /**
     * Create authentication response with access token only (for token refresh).
     *
     * @param accessToken JWT access token
     * @param expiresIn Token expiration time in seconds
     * @return AuthResponse with new access token
     */
    public static AuthResponse accessToken(@NonNull String accessToken, Long expiresIn) {
        AuthResponse response = new AuthResponse();
        response.accessToken = accessToken;
        response.expiresIn = expiresIn;
        return response;
    }
    
    /**
     * User information included in authentication responses.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        
        private Long id;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        private String role;
        private boolean emailVerified;
        private LocalDateTime lastLogin;
        
        // Private constructor to enforce builder usage
        private UserInfo() {}
        
        // Getters
        public Long getId() {
            return id;
        }
        
        public String getUsername() {
            return username;
        }
        
        public String getEmail() {
            return email;
        }
        
        public String getFirstName() {
            return firstName;
        }
        
        public String getLastName() {
            return lastName;
        }
        
        public String getRole() {
            return role;
        }
        
        public boolean isEmailVerified() {
            return emailVerified;
        }
        
        public LocalDateTime getLastLogin() {
            return lastLogin;
        }
        
        /**
         * Builder for UserInfo.
         *
         * @return UserInfoBuilder instance
         */
        public static UserInfoBuilder builder() {
            return new UserInfoBuilder();
        }
        
        /**
         * Builder class for UserInfo.
         */
        public static class UserInfoBuilder {
            
            private final UserInfo userInfo = new UserInfo();
            
            public UserInfoBuilder id(Long id) {
                userInfo.id = id;
                return this;
            }
            
            public UserInfoBuilder username(String username) {
                userInfo.username = username;
                return this;
            }
            
            public UserInfoBuilder email(String email) {
                userInfo.email = email;
                return this;
            }
            
            public UserInfoBuilder firstName(String firstName) {
                userInfo.firstName = firstName;
                return this;
            }
            
            public UserInfoBuilder lastName(String lastName) {
                userInfo.lastName = lastName;
                return this;
            }
            
            public UserInfoBuilder role(String role) {
                userInfo.role = role;
                return this;
            }
            
            public UserInfoBuilder emailVerified(boolean emailVerified) {
                userInfo.emailVerified = emailVerified;
                return this;
            }
            
            public UserInfoBuilder lastLogin(LocalDateTime lastLogin) {
                userInfo.lastLogin = lastLogin;
                return this;
            }
            
            public UserInfo build() {
                return userInfo;
            }
        }
    }
}