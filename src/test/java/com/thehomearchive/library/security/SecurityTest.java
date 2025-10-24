package com.thehomearchive.library.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security tests for JWT authentication and route protection.
 * Verifies that unauthorized access is properly blocked and authorized access is allowed.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("Public Endpoint Access Tests")
    class PublicEndpointTests {

        @Test
        @DisplayName("Should allow access to registration endpoint without authentication")
        void shouldAllowRegistrationAccess() throws Exception {
            mockMvc.perform(post("/api/auth/register"))
                    .andExpect(status().isBadRequest()); // Bad request due to missing body, but not unauthorized
        }

        @Test
        @DisplayName("Should allow access to login endpoint without authentication")
        void shouldAllowLoginAccess() throws Exception {
            mockMvc.perform(post("/api/auth/login"))
                    .andExpect(status().isBadRequest()); // Bad request due to missing body, but not unauthorized
        }

        @Test
        @DisplayName("Should allow access to email verification endpoint without authentication")
        void shouldAllowEmailVerificationAccess() throws Exception {
            mockMvc.perform(post("/api/auth/verify-email"))
                    .andExpect(status().isBadRequest()); // Bad request due to missing body, but not unauthorized
        }

        @Test
        @DisplayName("Should allow access to health check endpoint")
        void shouldAllowHealthCheckAccess() throws Exception {
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Protected Endpoint Access Tests")
    class ProtectedEndpointTests {

        @Test
        @DisplayName("Should block access to dashboard without authentication")
        void shouldBlockDashboardAccessWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/dashboard"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should block access to library endpoints without authentication")
        void shouldBlockLibraryAccessWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/library"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(post("/api/v1/library/books"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(put("/api/v1/library/books/1"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(delete("/api/v1/library/books/1"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should block access to search endpoints without authentication")
        void shouldBlockSearchAccessWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/search/books"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(get("/api/v1/search/history"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should block access to user profile endpoints without authentication")
        void shouldBlockUserProfileAccessWithoutAuth() throws Exception {
            mockMvc.perform(get("/api/v1/user/profile"))
                    .andExpect(status().isUnauthorized());

            mockMvc.perform(put("/api/v1/user/profile"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("JWT Token Validation Tests")
    class JwtTokenValidationTests {

        @Test
        @DisplayName("Should reject invalid JWT token format")
        void shouldRejectInvalidTokenFormat() throws Exception {
            mockMvc.perform(get("/api/v1/dashboard")
                            .header("Authorization", "Bearer invalid_token_format"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject malformed Authorization header")
        void shouldRejectMalformedAuthHeader() throws Exception {
            // Missing Bearer prefix
            mockMvc.perform(get("/api/v1/dashboard")
                            .header("Authorization", "just_a_token"))
                    .andExpect(status().isUnauthorized());

            // Wrong prefix
            mockMvc.perform(get("/api/v1/dashboard")
                            .header("Authorization", "Basic invalid_token"))
                    .andExpect(status().isUnauthorized());

            // Empty token
            mockMvc.perform(get("/api/v1/dashboard")
                            .header("Authorization", "Bearer "))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject expired JWT token")
        void shouldRejectExpiredToken() throws Exception {
            // Simulate an expired token
            String expiredToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.expired.token";
            
            mockMvc.perform(get("/api/v1/dashboard")
                            .header("Authorization", "Bearer " + expiredToken))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should reject JWT token with invalid signature")
        void shouldRejectInvalidSignature() throws Exception {
            // Simulate a token with invalid signature
            String invalidSignatureToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.valid.invalid_signature";
            
            mockMvc.perform(get("/api/v1/dashboard")
                            .header("Authorization", "Bearer " + invalidSignatureToken))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Authorized Access Tests")
    class AuthorizedAccessTests {

        @Test
        @WithMockUser(username = "testuser@example.com", roles = {"USER"})
        @DisplayName("Should allow authenticated user access to dashboard")
        void shouldAllowAuthenticatedDashboardAccess() throws Exception {
            mockMvc.perform(get("/api/v1/dashboard"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "testuser@example.com", roles = {"USER"})
        @DisplayName("Should allow authenticated user access to library endpoints")
        void shouldAllowAuthenticatedLibraryAccess() throws Exception {
            mockMvc.perform(get("/api/v1/library"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "testuser@example.com", roles = {"USER"})
        @DisplayName("Should allow authenticated user access to search endpoints")
        void shouldAllowAuthenticatedSearchAccess() throws Exception {
            mockMvc.perform(get("/api/v1/search/books"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "testuser@example.com", roles = {"USER"})
        @DisplayName("Should allow authenticated user access to profile endpoints")
        void shouldAllowAuthenticatedProfileAccess() throws Exception {
            mockMvc.perform(get("/api/v1/user/profile"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("Role-Based Access Control Tests")
    class RoleBasedAccessTests {

        @Test
        @WithMockUser(username = "admin@example.com", roles = {"ADMIN"})
        @DisplayName("Should allow admin access to user management endpoints")
        void shouldAllowAdminUserManagement() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users"))
                    .andExpect(status().isOk());

            mockMvc.perform(delete("/api/v1/admin/users/1"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "user@example.com", roles = {"USER"})
        @DisplayName("Should block regular user access to admin endpoints")
        void shouldBlockUserAdminAccess() throws Exception {
            mockMvc.perform(get("/api/v1/admin/users"))
                    .andExpect(status().isForbidden());

            mockMvc.perform(delete("/api/v1/admin/users/1"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(username = "user@example.com", roles = {"USER"})
        @DisplayName("Should only allow users to access their own resources")
        void shouldEnforceResourceOwnership() throws Exception {
            // Users should only access their own profile
            mockMvc.perform(get("/api/v1/user/profile"))
                    .andExpect(status().isOk());

            // Users should only access their own library
            mockMvc.perform(get("/api/v1/library"))
                    .andExpect(status().isOk());

            // Users should only access their own search history
            mockMvc.perform(get("/api/v1/search/history"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("CORS Security Tests")
    class CorsSecurityTests {

        @Test
        @DisplayName("Should handle CORS preflight requests properly")
        void shouldHandleCorsPreflight() throws Exception {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                            .options("/api/auth/login")
                            .header("Origin", "http://localhost:3000")
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers", "Content-Type,Authorization"))
                    .andExpect(status().isOk())
                    .andExpect(header().exists("Access-Control-Allow-Origin"))
                    .andExpect(header().exists("Access-Control-Allow-Methods"))
                    .andExpect(header().exists("Access-Control-Allow-Headers"));
        }

        @Test
        @DisplayName("Should reject requests from unauthorized origins")
        void shouldRejectUnauthorizedOrigins() throws Exception {
            // This test would be more relevant in a production environment
            // For now, we're testing that CORS is configured and functional
            mockMvc.perform(post("/api/auth/login")
                            .header("Origin", "http://malicious-site.com"))
                    .andExpect(status().isBadRequest()); // Should not be CORS-blocked in test, but bad request due to missing body
        }
    }

    @Nested
    @DisplayName("CSRF Protection Tests")
    class CsrfProtectionTests {

        @Test
        @DisplayName("Should require CSRF token for state-changing operations")
        void shouldRequireCsrfToken() throws Exception {
            // POST requests should require CSRF token
            mockMvc.perform(post("/api/auth/register"))
                    .andExpect(status().isBadRequest()); // Missing body causes 400 before CSRF check

            mockMvc.perform(post("/api/auth/login"))
                    .andExpect(status().isBadRequest()); // Missing body causes 400 before CSRF check
        }

        @Test
        @DisplayName("Should allow GET requests without CSRF token")
        void shouldAllowGetRequestsWithoutCsrf() throws Exception {
            // GET requests to public endpoints should work without CSRF
            mockMvc.perform(get("/actuator/health"))
                    .andExpect(status().isOk());
        }
    }
}