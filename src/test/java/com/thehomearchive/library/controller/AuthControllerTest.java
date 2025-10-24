package com.thehomearchive.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehomearchive.library.dto.auth.UserRegistrationRequest;
import com.thehomearchive.library.dto.auth.LoginRequest;
import com.thehomearchive.library.dto.auth.EmailVerificationRequest;
import com.thehomearchive.library.entity.EmailVerification;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.service.AuthenticationService;
import com.thehomearchive.library.service.UserService;
import com.thehomearchive.library.service.EmailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contract tests for AuthController endpoints.
 * Following TDD approach - these tests define the expected API behavior.
 */
@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationService authenticationService;

    @MockBean
    private UserService userService;

    @MockBean
    private EmailService emailService;

    @Nested
    @DisplayName("User Registration Tests (T020)")
    class UserRegistrationTests {

        @Test
        @DisplayName("Should register user successfully with valid data")
        void shouldRegisterUserSuccessfully() throws Exception {
            // Given
            UserRegistrationRequest request = UserRegistrationRequest.builder()
                    .email("john.doe@example.com")
                    .password("SecurePassword123!")
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            User mockUser = new User();
            mockUser.setId(1L);
            mockUser.setEmail("john.doe@example.com");

            when(userService.registerUser(any(UserRegistrationRequest.class)))
                    .thenReturn(mockUser);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Registration successful. Please check your email for verification."))
                    .andExpect(jsonPath("$.data").doesNotExist())
                    .andExpect(jsonPath("$.error").doesNotExist())
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should return 400 for invalid registration data")
        void shouldReturnBadRequestForInvalidData() throws Exception {
            // Given - Invalid request with missing required fields
            UserRegistrationRequest request = UserRegistrationRequest.builder()
                    .email("invalid-email")
                    .password("weak")
                    .firstName("")
                    .lastName("")
                    .build();

            // When & Then
            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists())
                    .andExpect(jsonPath("$.fieldErrors").exists());
        }

        @Test
        @DisplayName("Should return 409 for duplicate email")
        void shouldReturnConflictForDuplicateEmail() throws Exception {
            // Given
            UserRegistrationRequest request = UserRegistrationRequest.builder()
                    .email("existing@example.com")
                    .password("SecurePassword123!")
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            when(userService.registerUser(any(UserRegistrationRequest.class)))
                    .thenThrow(new RuntimeException("Email already exists"));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("Email already exists"));
        }

        @Test
        @DisplayName("Should require CSRF token")
        void shouldRequireCsrfToken() throws Exception {
            // Given
            UserRegistrationRequest request = UserRegistrationRequest.builder()
                    .email("john.doe@example.com")
                    .password("SecurePassword123!")
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            // When & Then - Request without CSRF token should fail
            mockMvc.perform(post("/api/v1/auth/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("User Login Tests (T021)")
    class UserLoginTests {

        @Test
        @DisplayName("Should login user successfully with valid credentials")
        void shouldLoginUserSuccessfully() throws Exception {
            // Given
            LoginRequest request = new LoginRequest();
            request.setEmail("john.doe@example.com");
            request.setPassword("SecurePassword123!");

            Map<String, Object> mockAuthResult = new HashMap<>();
            mockAuthResult.put("accessToken", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...");
            mockAuthResult.put("refreshToken", "refresh_token_here");
            mockAuthResult.put("tokenType", "Bearer");
            mockAuthResult.put("expiresIn", 3600);
            mockAuthResult.put("user", Map.of(
                "id", 1L,
                "email", "john.doe@example.com",
                "firstName", "John",
                "lastName", "Doe",
                "role", "USER",
                "emailVerified", true
            ));

            when(authenticationService.login(any(LoginRequest.class)))
                    .thenReturn(mockAuthResult);            // When & Then
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists())
                    .andExpect(jsonPath("$.tokenType").value("Bearer"))
                    .andExpect(jsonPath("$.expiresIn").value(3600))
                    .andExpect(jsonPath("$.user.id").value(1))
                    .andExpect(jsonPath("$.user.email").value("john.doe@example.com"))
                    .andExpect(jsonPath("$.user.firstName").value("John"))
                    .andExpect(jsonPath("$.user.lastName").value("Doe"))
                    .andExpect(jsonPath("$.user.role").value("USER"))
                    .andExpect(jsonPath("$.user.emailVerified").value(true));
        }

        @Test
        @DisplayName("Should return 401 for invalid credentials")
        void shouldReturnUnauthorizedForInvalidCredentials() throws Exception {
            // Given
            LoginRequest request = new LoginRequest("invalid@example.com", "wrongpassword");

            when(authenticationService.login(any(LoginRequest.class)))
                    .thenThrow(new RuntimeException("Invalid credentials"));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("Invalid credentials"));
        }

        @Test
        @DisplayName("Should return 400 for missing login fields")
        void shouldReturnBadRequestForMissingFields() throws Exception {
            // Given - Invalid request with missing fields
            LoginRequest request = new LoginRequest("", "");

            // When & Then
            mockMvc.perform(post("/api/v1/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists());
        }
    }

    @Nested
    @DisplayName("Email Verification Tests (T022)")
    class EmailVerificationTests {

        @Test
        @DisplayName("Should verify email successfully with valid token")
        void shouldVerifyEmailSuccessfully() throws Exception {
            // Given
            EmailVerificationRequest request = new EmailVerificationRequest("valid_verification_token");
            
            EmailVerification mockVerification = new EmailVerification();
            mockVerification.setId(1L);
            mockVerification.setVerified(true);

            when(emailService.verifyEmail(anyString()))
                    .thenReturn(mockVerification);

            // When & Then
            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Email verified successfully. You can now log in."))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @Test
        @DisplayName("Should return 400 for invalid verification token")
        void shouldReturnBadRequestForInvalidToken() throws Exception {
            // Given
            EmailVerificationRequest request = new EmailVerificationRequest("invalid_token");

            when(emailService.verifyEmail(anyString()))
                    .thenThrow(new RuntimeException("Invalid or expired verification token"));

            // When & Then
            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").value("Invalid or expired verification token"));
        }

        @Test
        @DisplayName("Should return 400 for missing verification token")
        void shouldReturnBadRequestForMissingToken() throws Exception {
            // Given
            EmailVerificationRequest request = new EmailVerificationRequest("");

            // When & Then
            mockMvc.perform(post("/api/v1/auth/verify-email")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.error").exists());
        }
    }
}