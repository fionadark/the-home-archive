package com.thehomearchive.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehomearchive.library.service.LibraryService;
import com.thehomearchive.library.service.BookService;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.entity.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contract tests for LibraryController endpoints (T049).
 * Following TDD approach - these tests define the expected API behavior for personal library management.
 */
@WebMvcTest(LibraryController.class)
@ActiveProfiles("test")
class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LibraryService libraryService;

    @MockBean
    private BookService bookService;

    @Nested
    @DisplayName("Personal Library Operations")
    class PersonalLibraryOperations {

        @Test
        @DisplayName("Should return user's personal library with books")
        @WithMockUser(roles = "USER")
        void shouldReturnPersonalLibraryWithBooks() throws Exception {
            // Given
            List<Map<String, Object>> mockBooks = List.of(
                Map.of(
                    "id", 1L,
                    "title", "The Great Gatsby",
                    "author", "F. Scott Fitzgerald",
                    "isbn", "978-0-7432-7356-5",
                    "description", "A classic American novel",
                    "publicationYear", 1925,
                    "category", Map.of("id", 1L, "name", "Fiction"),
                    "physicalLocation", "Living Room - Shelf A",
                    "personalRating", 5,
                    "dateAdded", "2024-01-15T10:30:00"
                )
            );

            when(libraryService.getUserLibrary(any(User.class), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(Map.of(
                    "books", mockBooks,
                    "totalBooks", 1,
                    "page", 0,
                    "size", 20,
                    "totalPages", 1
                ));

            // When & Then
            mockMvc.perform(get("/api/v1/library")
                            .param("page", "0")
                            .param("size", "20")
                            .param("sort", "title")
                            .param("direction", "asc")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.books").isArray())
                    .andExpect(jsonPath("$.data.books[0].title").value("The Great Gatsby"))
                    .andExpect(jsonPath("$.data.books[0].author").value("F. Scott Fitzgerald"))
                    .andExpect(jsonPath("$.data.totalBooks").value(1));
        }

        @Test
        @DisplayName("Should search user's personal library")
        @WithMockUser(roles = "USER")
        void shouldSearchPersonalLibrary() throws Exception {
            // Given
            List<Map<String, Object>> searchResults = List.of(
                Map.of(
                    "id", 1L,
                    "title", "The Great Gatsby",
                    "author", "F. Scott Fitzgerald",
                    "match", true
                )
            );

            when(libraryService.searchUserLibrary(any(User.class), eq("Gatsby"), anyInt(), anyInt()))
                .thenReturn(Map.of(
                    "books", searchResults,
                    "totalBooks", 1,
                    "query", "Gatsby"
                ));

            // When & Then
            mockMvc.perform(get("/api/v1/library/search")
                            .param("q", "Gatsby")
                            .param("page", "0")
                            .param("size", "20")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.books").isArray())
                    .andExpect(jsonPath("$.data.books[0].title").value("The Great Gatsby"))
                    .andExpect(jsonPath("$.data.query").value("Gatsby"));
        }

        @Test
        @DisplayName("Should add book to personal library")
        @WithMockUser(roles = "USER")
        void shouldAddBookToPersonalLibrary() throws Exception {
            // Given
            Map<String, Object> bookRequest = Map.of(
                "bookId", 1L,
                "physicalLocation", "Living Room - Shelf A",
                "personalRating", 4
            );

            Map<String, Object> addedBook = Map.of(
                "id", 1L,
                "bookId", 1L,
                "userId", 1L,
                "physicalLocation", "Living Room - Shelf A",
                "personalRating", 4,
                "dateAdded", "2024-01-15T10:30:00"
            );

            when(libraryService.addBookToUserLibrary(any(User.class), anyLong(), anyString(), anyInt()))
                .thenReturn(addedBook);

            // When & Then
            mockMvc.perform(post("/api/v1/library/books")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bookRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Book added to your library successfully"))
                    .andExpect(jsonPath("$.data.bookId").value(1))
                    .andExpect(jsonPath("$.data.physicalLocation").value("Living Room - Shelf A"));
        }

        @Test
        @DisplayName("Should update book in personal library")
        @WithMockUser(roles = "USER")
        void shouldUpdateBookInPersonalLibrary() throws Exception {
            // Given
            Long libraryBookId = 1L;
            Map<String, Object> updateRequest = Map.of(
                "physicalLocation", "Bedroom - Shelf B",
                "personalRating", 5
            );

            Map<String, Object> updatedBook = Map.of(
                "id", libraryBookId,
                "physicalLocation", "Bedroom - Shelf B",
                "personalRating", 5,
                "dateUpdated", "2024-01-15T11:30:00"
            );

            when(libraryService.updateBookInUserLibrary(any(User.class), eq(libraryBookId), anyString(), anyInt()))
                .thenReturn(updatedBook);

            // When & Then
            mockMvc.perform(put("/api/v1/library/books/{id}", libraryBookId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Book updated in your library successfully"))
                    .andExpect(jsonPath("$.data.physicalLocation").value("Bedroom - Shelf B"))
                    .andExpect(jsonPath("$.data.personalRating").value(5));
        }

        @Test
        @DisplayName("Should remove book from personal library")
        @WithMockUser(roles = "USER")
        void shouldRemoveBookFromPersonalLibrary() throws Exception {
            // Given
            Long libraryBookId = 1L;

            when(libraryService.removeBookFromUserLibrary(any(User.class), eq(libraryBookId)))
                .thenReturn(true);

            // When & Then
            mockMvc.perform(delete("/api/v1/library/books/{id}", libraryBookId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Book removed from your library successfully"));
        }

        @Test
        @DisplayName("Should return 404 when trying to update non-existent book")
        @WithMockUser(roles = "USER")
        void shouldReturn404ForNonExistentBook() throws Exception {
            // Given
            Long nonExistentId = 999L;
            Map<String, Object> updateRequest = Map.of(
                "physicalLocation", "New Location"
            );

            when(libraryService.updateBookInUserLibrary(any(User.class), eq(nonExistentId), anyString(), anyInt()))
                .thenThrow(new RuntimeException("Book not found in user's library"));

            // When & Then
            mockMvc.perform(put("/api/v1/library/books/{id}", nonExistentId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Book not found in user's library"));
        }

        @Test
        @DisplayName("Should require authentication for library access")
        void shouldRequireAuthenticationForLibraryAccess() throws Exception {
            // When & Then
            mockMvc.perform(get("/api/v1/library")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return validation error for invalid book addition")
        @WithMockUser(roles = "USER")
        void shouldReturnValidationErrorForInvalidBookAddition() throws Exception {
            // Given - missing required bookId
            Map<String, Object> invalidRequest = Map.of(
                "physicalLocation", "Living Room"
            );

            // When & Then
            mockMvc.perform(post("/api/v1/library/books")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("Library Statistics")
    class LibraryStatistics {

        @Test
        @DisplayName("Should return library statistics")
        @WithMockUser(roles = "USER")
        void shouldReturnLibraryStatistics() throws Exception {
            // Given
            Map<String, Object> stats = Map.of(
                "totalBooks", 25,
                "booksRead", 15,
                "booksReading", 3,
                "booksWantToRead", 7,
                "averageRating", 4.2,
                "favoriteCategory", "Fiction"
            );

            when(libraryService.getUserLibraryStatistics(any(User.class)))
                .thenReturn(stats);

            // When & Then
            mockMvc.perform(get("/api/v1/library/statistics")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalBooks").value(25))
                    .andExpect(jsonPath("$.data.booksRead").value(15))
                    .andExpect(jsonPath("$.data.averageRating").value(4.2));
        }
    }
}