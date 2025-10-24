package com.thehomearchive.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehomearchive.library.service.BookService;
import com.thehomearchive.library.service.CategoryService;
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

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contract tests for BookController endpoints (T050).
 * Following TDD approach - these tests define the expected API behavior for book management.
 */
@WebMvcTest(BookController.class)
@ActiveProfiles("test")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private CategoryService categoryService;

    @Nested
    @DisplayName("Book Management Operations")
    class BookManagementOperations {

        @Test
        @DisplayName("Should get book by ID")
        @WithMockUser(roles = "USER")
        void shouldGetBookById() throws Exception {
            // Given
            Long bookId = 1L;
            Map<String, Object> book = Map.of(
                "id", bookId,
                "title", "The Great Gatsby",
                "author", "F. Scott Fitzgerald",
                "isbn", "978-0-7432-7356-5",
                "description", "A classic American novel about the Jazz Age",
                "publicationYear", 1925,
                "publisher", "Scribner",
                "pageCount", 180,
                "category", Map.of("id", 1L, "name", "Fiction", "slug", "fiction"),
                "coverImageUrl", "https://example.com/cover.jpg",
                "averageRating", 4.2,
                "ratingCount", 156
            );

            when(bookService.getBookById(bookId)).thenReturn(book);

            // When & Then
            mockMvc.perform(get("/api/v1/books/{id}", bookId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(bookId))
                    .andExpect(jsonPath("$.data.title").value("The Great Gatsby"))
                    .andExpect(jsonPath("$.data.author").value("F. Scott Fitzgerald"))
                    .andExpect(jsonPath("$.data.isbn").value("978-0-7432-7356-5"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent book")
        @WithMockUser(roles = "USER")
        void shouldReturn404ForNonExistentBook() throws Exception {
            // Given
            Long nonExistentId = 999L;
            when(bookService.getBookById(nonExistentId))
                .thenThrow(new RuntimeException("Book not found"));

            // When & Then
            mockMvc.perform(get("/api/v1/books/{id}", nonExistentId)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Book not found"));
        }

        @Test
        @DisplayName("Should search books with pagination")
        @WithMockUser(roles = "USER")
        void shouldSearchBooksWithPagination() throws Exception {
            // Given
            List<Map<String, Object>> books = List.of(
                Map.of(
                    "id", 1L,
                    "title", "The Great Gatsby",
                    "author", "F. Scott Fitzgerald",
                    "category", Map.of("name", "Fiction")
                ),
                Map.of(
                    "id", 2L,
                    "title", "Gatsby's Journal",
                    "author", "John Doe",
                    "category", Map.of("name", "Biography")
                )
            );

            when(bookService.searchBooks(eq("Gatsby"), anyInt(), anyInt(), anyString(), anyString()))
                .thenReturn(Map.of(
                    "books", books,
                    "totalBooks", 2,
                    "page", 0,
                    "size", 20,
                    "totalPages", 1
                ));

            // When & Then
            mockMvc.perform(get("/api/v1/books/search")
                            .param("q", "Gatsby")
                            .param("page", "0")
                            .param("size", "20")
                            .param("sort", "title")
                            .param("direction", "asc")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.books").isArray())
                    .andExpect(jsonPath("$.data.books[0].title").value("The Great Gatsby"))
                    .andExpect(jsonPath("$.data.totalBooks").value(2));
        }

        @Test
        @DisplayName("Should filter books by category")
        @WithMockUser(roles = "USER")
        void shouldFilterBooksByCategory() throws Exception {
            // Given
            Long categoryId = 1L;
            List<Map<String, Object>> books = List.of(
                Map.of(
                    "id", 1L,
                    "title", "The Great Gatsby",
                    "author", "F. Scott Fitzgerald",
                    "category", Map.of("id", categoryId, "name", "Fiction")
                )
            );

            when(bookService.getBooksByCategory(categoryId, 0, 20, "title", "asc"))
                .thenReturn(Map.of(
                    "books", books,
                    "totalBooks", 1,
                    "page", 0,
                    "size", 20,
                    "totalPages", 1
                ));

            // When & Then
            mockMvc.perform(get("/api/v1/books")
                            .param("categoryId", categoryId.toString())
                            .param("page", "0")
                            .param("size", "20")
                            .param("sort", "title")
                            .param("direction", "asc")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.books").isArray())
                    .andExpect(jsonPath("$.data.books[0].category.name").value("Fiction"));
        }

        @Test
        @DisplayName("Should create new book")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateNewBook() throws Exception {
            // Given
            Map<String, Object> bookRequest = Map.of(
                "title", "New Book",
                "author", "New Author",
                "isbn", "978-1-234-56789-0",
                "description", "A new book description",
                "publicationYear", 2024,
                "publisher", "New Publisher",
                "pageCount", 250,
                "categoryId", 1L
            );

            Map<String, Object> createdBook = Map.of(
                "id", 1L,
                "title", "New Book",
                "author", "New Author",
                "isbn", "978-1-234-56789-0",
                "description", "A new book description",
                "publicationYear", 2024,
                "publisher", "New Publisher",
                "pageCount", 250,
                "category", Map.of("id", 1L, "name", "Fiction")
            );

            when(bookService.createBook(any(Map.class))).thenReturn(createdBook);

            // When & Then
            mockMvc.perform(post("/api/v1/books")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bookRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Book created successfully"))
                    .andExpect(jsonPath("$.data.title").value("New Book"))
                    .andExpect(jsonPath("$.data.author").value("New Author"));
        }

        @Test
        @DisplayName("Should update existing book")
        @WithMockUser(roles = "ADMIN")
        void shouldUpdateExistingBook() throws Exception {
            // Given
            Long bookId = 1L;
            Map<String, Object> updateRequest = Map.of(
                "title", "Updated Book Title",
                "description", "Updated description",
                "pageCount", 300
            );

            Map<String, Object> updatedBook = Map.of(
                "id", bookId,
                "title", "Updated Book Title",
                "author", "F. Scott Fitzgerald",
                "description", "Updated description",
                "pageCount", 300
            );

            when(bookService.updateBook(eq(bookId), any(Map.class))).thenReturn(updatedBook);

            // When & Then
            mockMvc.perform(put("/api/v1/books/{id}", bookId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Book updated successfully"))
                    .andExpect(jsonPath("$.data.title").value("Updated Book Title"));
        }

        @Test
        @DisplayName("Should delete book")
        @WithMockUser(roles = "ADMIN")
        void shouldDeleteBook() throws Exception {
            // Given
            Long bookId = 1L;
            when(bookService.deleteBook(bookId)).thenReturn(true);

            // When & Then
            mockMvc.perform(delete("/api/v1/books/{id}", bookId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Book deleted successfully"));
        }

        @Test
        @DisplayName("Should require admin role for book creation")
        @WithMockUser(roles = "USER")
        void shouldRequireAdminRoleForBookCreation() throws Exception {
            // Given
            Map<String, Object> bookRequest = Map.of(
                "title", "New Book",
                "author", "New Author"
            );

            // When & Then
            mockMvc.perform(post("/api/v1/books")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(bookRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return validation error for invalid book data")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnValidationErrorForInvalidBookData() throws Exception {
            // Given - missing required fields
            Map<String, Object> invalidRequest = Map.of(
                "description", "Book without title or author"
            );

            // When & Then
            mockMvc.perform(post("/api/v1/books")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());
        }
    }

    @Nested
    @DisplayName("Category Operations")
    class CategoryOperations {

        @Test
        @DisplayName("Should get all categories")
        @WithMockUser(roles = "USER")
        void shouldGetAllCategories() throws Exception {
            // Given
            List<Map<String, Object>> categories = List.of(
                Map.of("id", 1L, "name", "Fiction", "slug", "fiction", "description", "Fiction books"),
                Map.of("id", 2L, "name", "Non-Fiction", "slug", "non-fiction", "description", "Non-fiction books"),
                Map.of("id", 3L, "name", "Biography", "slug", "biography", "description", "Biographies")
            );

            when(categoryService.getAllCategories()).thenReturn(categories);

            // When & Then
            mockMvc.perform(get("/api/v1/books/categories")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].name").value("Fiction"))
                    .andExpect(jsonPath("$.data[1].name").value("Non-Fiction"));
        }

        @Test
        @DisplayName("Should create new category")
        @WithMockUser(roles = "ADMIN")
        void shouldCreateNewCategory() throws Exception {
            // Given
            Map<String, Object> categoryRequest = Map.of(
                "name", "Science Fiction",
                "description", "Science fiction books"
            );

            Map<String, Object> createdCategory = Map.of(
                "id", 4L,
                "name", "Science Fiction",
                "slug", "science-fiction",
                "description", "Science fiction books"
            );

            when(categoryService.createCategory(any(Map.class))).thenReturn(createdCategory);

            // When & Then
            mockMvc.perform(post("/api/v1/books/categories")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(categoryRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Category created successfully"))
                    .andExpect(jsonPath("$.data.name").value("Science Fiction"))
                    .andExpect(jsonPath("$.data.slug").value("science-fiction"));
        }
    }
}