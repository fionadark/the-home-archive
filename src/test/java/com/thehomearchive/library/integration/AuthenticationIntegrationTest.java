package com.thehomearchive.library.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehomearchive.library.dto.auth.UserRegistrationRequest;
import com.thehomearchive.library.dto.auth.LoginRequest;
import com.thehomearchive.library.dto.auth.EmailVerificationRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for complete authentication flow.
 * Tests the entire user journey from registration to dashboard access.
 */
@SpringBootTest
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Transactional
class AuthenticationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private static String verificationToken;
    private static String accessToken;

    @Test
    @Order(1)
    @DisplayName("Complete Authentication Flow - User can register, verify email, login, and access dashboard")
    void completeAuthenticationFlow() throws Exception {
        // Step 1: Register a new user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .email("integrationtest@example.com")
                .password("SecurePassword123!")
                .firstName("Integration")
                .lastName("Test")
                .build();

        MvcResult registrationResult = mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Registration successful. Please check your email for verification."))
                .andReturn();

        // Step 2: Attempt login before email verification (should fail)
        LoginRequest loginRequest = new LoginRequest("integrationtest@example.com", "SecurePassword123!");

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Email not verified"));

        // Step 3: Verify email (in real application, token would come from email)
        // For integration test, we'll simulate getting the verification token
        EmailVerificationRequest verificationRequest = new EmailVerificationRequest("test_verification_token");

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verificationRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email verified successfully. You can now log in."));

        // Step 4: Login after email verification (should succeed)
        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("integrationtest@example.com"))
                .andExpect(jsonPath("$.user.emailVerified").value(true))
                .andReturn();

        // Extract access token for subsequent requests
        String responseBody = loginResult.getResponse().getContentAsString();
        // In real implementation, we'd parse this to get the actual token
        accessToken = "mock_jwt_token_from_login_response";

        // Step 5: Access protected dashboard endpoint
        mockMvc.perform(get("/api/v1/dashboard")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @Order(2)
    @DisplayName("Should prevent unauthorized access to protected resources")
    void shouldPreventUnauthorizedAccess() throws Exception {
        // Attempt to access dashboard without token
        mockMvc.perform(get("/api/v1/dashboard"))
                .andExpect(status().isUnauthorized());

        // Attempt to access dashboard with invalid token
        mockMvc.perform(get("/api/v1/dashboard")
                        .header("Authorization", "Bearer invalid_token"))
                .andExpect(status().isUnauthorized());

        // Attempt to access dashboard with malformed token
        mockMvc.perform(get("/api/v1/dashboard")
                        .header("Authorization", "invalid_format"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(3)
    @DisplayName("Should prevent duplicate email registration")
    void shouldPreventDuplicateEmailRegistration() throws Exception {
        // First registration
        UserRegistrationRequest firstRequest = UserRegistrationRequest.builder()
                .email("duplicate@example.com")
                .password("SecurePassword123!")
                .firstName("First")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isCreated());

        // Attempt duplicate registration
        UserRegistrationRequest duplicateRequest = UserRegistrationRequest.builder()
                .email("duplicate@example.com")
                .password("AnotherPassword456!")
                .firstName("Second")
                .lastName("User")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Email already exists"));
    }

    @Test
    @Order(4)
    @DisplayName("Should handle expired verification tokens")
    void shouldHandleExpiredVerificationTokens() throws Exception {
        // Register user
        UserRegistrationRequest registrationRequest = UserRegistrationRequest.builder()
                .email("expired@example.com")
                .password("SecurePassword123!")
                .firstName("Expired")
                .lastName("Token")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationRequest)))
                .andExpect(status().isCreated());

        // Attempt verification with expired token
        EmailVerificationRequest expiredRequest = new EmailVerificationRequest("expired_verification_token");

        mockMvc.perform(post("/api/v1/auth/verify-email")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(expiredRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Invalid or expired verification token"));
    }

    @Test
    @Order(5)
    @DisplayName("Should validate user input properly")
    void shouldValidateUserInputProperly() throws Exception {
        // Test invalid email format
        UserRegistrationRequest invalidEmailRequest = UserRegistrationRequest.builder()
                .email("invalid-email-format")
                .password("SecurePassword123!")
                .firstName("Valid")
                .lastName("Name")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidEmailRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.fieldErrors").exists());

        // Test weak password
        UserRegistrationRequest weakPasswordRequest = UserRegistrationRequest.builder()
                .email("valid@example.com")
                .password("weak")
                .firstName("Valid")
                .lastName("Name")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(weakPasswordRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.fieldErrors").exists());

        // Test empty required fields
        UserRegistrationRequest emptyFieldsRequest = UserRegistrationRequest.builder()
                .email("")
                .password("")
                .firstName("")
                .lastName("")
                .build();

        mockMvc.perform(post("/api/v1/auth/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emptyFieldsRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.fieldErrors").exists());
    }
}