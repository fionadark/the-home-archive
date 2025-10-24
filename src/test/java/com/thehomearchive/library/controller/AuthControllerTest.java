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
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contract tests for AuthController endpoints.
 * Following TDD approach - these tests define the expected API behavior.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
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
        @DisplayName("Should register new user successfully")
        void shouldRegisterNewUserSuccessfully() throws Exception {
            // Arrange
            UserRegistrationRequest request = UserRegistrationRequest.builder()
                    .email("newuser@example.com")
                    .password("SecurePassword123!")
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            User mockUser = new User();
            mockUser.setId(1L);
            mockUser.setEmail("newuser@example.com");
            mockUser.setFirstName("John");
            mockUser.setLastName("Doe");

            when(userService.emailExists(anyString())).thenReturn(false);
            when(userService.registerUser(any(UserRegistrationRequest.class))).thenReturn(mockUser);

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Registration successful. Please check your email for verification instructions."))
                    .andExpect(jsonPath("$.userId").value(1));

            verify(userService, times(1)).emailExists(anyString());
            verify(userService, times(1)).registerUser(any(UserRegistrationRequest.class));
        }

        @Test
        @DisplayName("Should return bad request for invalid registration data")
        void shouldReturnBadRequestForInvalidData() throws Exception {
            // Arrange
            UserRegistrationRequest request = UserRegistrationRequest.builder()
                    .email("invalid-email")
                    .password("weak")
                    .firstName("")
                    .lastName("")
                    .build();

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return conflict when email already exists")
        void shouldReturnConflictWhenEmailExists() throws Exception {
            // Arrange
            UserRegistrationRequest request = UserRegistrationRequest.builder()
                    .email("existing@example.com")
                    .password("SecurePassword123!")
                    .firstName("John")
                    .lastName("Doe")
                    .build();

            when(userService.emailExists(anyString())).thenReturn(true);

            // Act & Assert
            mockMvc.perform(post("/api/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Email address is already registered"));
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
            mockMvc.perform(post("/api/auth/login")
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
                    .thenThrow(new IllegalArgumentException("Invalid credentials"));

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid credentials"));
        }

        @Test
        @DisplayName("Should return 400 for missing login fields")
        void shouldReturnBadRequestForMissingFields() throws Exception {
            // Given - Invalid request with missing fields
            LoginRequest request = new LoginRequest("", "");

            // When & Then
            mockMvc.perform(post("/api/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.message").value("Invalid input provided"))
                    .andExpect(jsonPath("$.details.email").value("Email is required"));
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
            mockMvc.perform(post("/api/auth/verify-email")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Email verified successfully. You can now log in."));
        }

        @Test
        @DisplayName("Should return 400 for invalid verification token")
        void shouldReturnBadRequestForInvalidToken() throws Exception {
            // Given
            EmailVerificationRequest request = new EmailVerificationRequest("invalid_token");

            when(emailService.verifyEmail(anyString()))
                    .thenThrow(new IllegalArgumentException("Invalid or expired verification token"));

            // When & Then
            mockMvc.perform(post("/api/auth/verify-email")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Invalid or expired verification token"));
        }

        @Test
        @DisplayName("Should return 400 for missing verification token")
        void shouldReturnBadRequestForMissingToken() throws Exception {
            // Given
            EmailVerificationRequest request = new EmailVerificationRequest("");

            // When & Then
            mockMvc.perform(post("/api/auth/verify-email")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.error").value("Validation Failed"))
                    .andExpect(jsonPath("$.message").value("Invalid input provided"))
                    .andExpect(jsonPath("$.details.token").value("Verification token is required"));
        }
    }
}