package com.thehomearchive.library.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehomearchive.library.entity.Book;
import com.thehomearchive.library.entity.Category;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.entity.UserRole;
import com.thehomearchive.library.repository.BookRepository;
import com.thehomearchive.library.repository.CategoryRepository;
import com.thehomearchive.library.repository.UserRepository;
import com.thehomearchive.library.service.AuthenticationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Library Management flow (T051).
 * Tests the complete flow from authentication to library management operations.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class LibraryManagementIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationService authenticationService;

    private User testUser;
    private Book testBook;
    private String accessToken;

    @BeforeEach
    void setUp() {
        // Create test category
        Category category = new Category();
        category.setName("Fiction");
        category.setDescription("Test fiction category");
        category.setSlug("fiction");
        category.setCreatedAt(LocalDateTime.now());
        category = categoryRepository.save(category);

        // Create test book
        testBook = new Book();
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("9781234567890");
        testBook.setDescription("A test book for integration testing");
        testBook.setPublicationYear(2024);
        testBook.setPublisher("Test Publisher");
        testBook.setPageCount(300);
        testBook.setCategory(category);
        testBook.setCreatedAt(LocalDateTime.now());
        testBook.setUpdatedAt(LocalDateTime.now());
        testBook = bookRepository.save(testBook);

        // Create test user
        testUser = new User();
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash(passwordEncoder.encode("SecurePassword123!"));
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(UserRole.USER);
        testUser.setEmailVerified(true);
        testUser.setCreatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // Get access token for authentication
        try {
            var loginRequest = new com.thehomearchive.library.dto.auth.LoginRequest();
            loginRequest.setEmail("test@example.com");
            loginRequest.setPassword("SecurePassword123!");
            
            Map<String, Object> authResult = authenticationService.login(loginRequest);
            accessToken = (String) authResult.get("accessToken");
        } catch (Exception e) {
            throw new RuntimeException("Failed to setup test authentication", e);
        }
    }

    @Test
    @DisplayName("Should complete full library management flow")
    void shouldCompleteFullLibraryManagementFlow() throws Exception {
        // Step 1: Access empty library
        mockMvc.perform(get("/api/v1/library")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pagination.totalElements").value(0));

        // Step 2: Add a book to personal library
        Map<String, Object> addBookRequest = Map.of(
            "physicalLocation", "Living Room - Shelf A"
        );

        mockMvc.perform(post("/api/v1/library/books/" + testBook.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addBookRequest)))
                .andExpect(status().isOk())  // Controller returns 200, not 201
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.bookId").value(testBook.getId()));

        // Step 3: View library with added book
        mockMvc.perform(get("/api/v1/library")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pagination.totalElements").value(1))
                .andExpect(jsonPath("$.data.content[0].physicalLocation").value("Living Room - Shelf A"));

        // Step 4: Search within personal library
        mockMvc.perform(get("/api/v1/library/search")
                        .header("Authorization", "Bearer " + accessToken)
                        .param("q", "Test")  // Search for our test book
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Step 5: Update book location and rating
        Map<String, Object> updateRequest = Map.of(
            "physicalLocation", "Bedroom - Shelf B",
            "personalRating", 4
        );

        mockMvc.perform(put("/api/v1/library/books/" + testBook.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.physicalLocation").value("Bedroom - Shelf B"));

        // Step 6: Get library statistics
        mockMvc.perform(get("/api/v1/library/statistics")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.UNREAD").value(1));

        // Step 7: Remove book from library
        mockMvc.perform(delete("/api/v1/library/books/" + testBook.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        // Step 8: Verify book is removed
        mockMvc.perform(get("/api/v1/library")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pagination.totalElements").value(0));
    }

    @Test
    @DisplayName("Should require authentication for library operations")
    void shouldRequireAuthenticationForLibraryOperations() throws Exception {
        // Test without authentication header
        mockMvc.perform(get("/api/v1/library")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/library/books/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle invalid authentication token")
    void shouldHandleInvalidAuthenticationToken() throws Exception {
        mockMvc.perform(get("/api/v1/library")
                        .header("Authorization", "Bearer invalid-token")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should isolate user libraries")
    void shouldIsolateUserLibraries() throws Exception {
        // Create second test user
        User secondUser = new User();
        secondUser.setEmail("second@example.com");
        secondUser.setPasswordHash(passwordEncoder.encode("SecurePassword123!"));
        secondUser.setFirstName("Jane");
        secondUser.setLastName("Smith");
        secondUser.setRole(UserRole.USER);
        secondUser.setEmailVerified(true);
        secondUser.setCreatedAt(LocalDateTime.now());
        secondUser = userRepository.save(secondUser);

        // Get access token for second user
        var loginRequest = new com.thehomearchive.library.dto.auth.LoginRequest();
        loginRequest.setEmail("second@example.com");
        loginRequest.setPassword("SecurePassword123!");
        
        Map<String, Object> authResult = authenticationService.login(loginRequest);
        String secondUserToken = (String) authResult.get("accessToken");

        // Add book to first user's library
        Map<String, Object> addBookRequest = Map.of(
            "physicalLocation", "Living Room - Shelf A"
        );

        mockMvc.perform(post("/api/v1/library/books/" + testBook.getId())
                        .header("Authorization", "Bearer " + accessToken)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addBookRequest)))
                .andExpect(status().isOk()); // Controller returns 200, not 201

        // Verify first user sees the book
        mockMvc.perform(get("/api/v1/library")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.totalElements").value(1));

        // Verify second user has empty library
        mockMvc.perform(get("/api/v1/library")
                        .header("Authorization", "Bearer " + secondUserToken)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.pagination.totalElements").value(0));
    }
}