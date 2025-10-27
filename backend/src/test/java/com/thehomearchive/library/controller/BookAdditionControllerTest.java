package com.thehomearchive.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehomearchive.library.dto.book.BookDto;
import com.thehomearchive.library.dto.book.CreateBookRequest;
import com.thehomearchive.library.dto.book.UpdateBookRequest;
import com.thehomearchive.library.dto.book.BookValidationResponse;
import com.thehomearchive.library.entity.Book;
import com.thehomearchive.library.entity.Category;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.service.BookService;
import com.thehomearchive.library.service.UserService;
import com.thehomearchive.library.config.SecurityConfig;
import com.thehomearchive.library.exception.BookAlreadyExistsException;
import com.thehomearchive.library.exception.BookNotFoundException;
import com.thehomearchive.library.exception.CategoryNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contract tests for BookAdditionController
 * Tests the API contract for adding books to user libraries
 * 
 * Test Coverage:
 * - Adding books from search results to personal library
 * - Creating new book entries manually
 * - Updating book information
 * - Book validation and duplicate detection
 * - ISBN validation and enrichment
 * - Error handling for invalid requests
 * - User permission validation
 */
@WebMvcTest(BookAdditionController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("Book Addition Controller Contract Tests")
public class BookAdditionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private UserService userService;

    private User testUser;
    private Category fictionCategory;
    private Book existingBook;
    private BookDto bookDto;
    private CreateBookRequest createBookRequest;
    private UpdateBookRequest updateBookRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());

        // Setup test category
        fictionCategory = new Category();
        fictionCategory.setId(1L);
        fictionCategory.setName("Fiction");
        fictionCategory.setDescription("Fiction books");

        // Setup existing book
        existingBook = new Book();
        existingBook.setId(1L);
        existingBook.setTitle("The Great Gatsby");
        existingBook.setAuthor("F. Scott Fitzgerald");
        existingBook.setIsbn("978-0-7432-7356-5");
        existingBook.setDescription("A classic American novel set in the Jazz Age");
        existingBook.setPublicationYear(1925);
        existingBook.setPublisher("Scribner");
        existingBook.setPageCount(180);
        existingBook.setCategory(fictionCategory);
        existingBook.setAverageRating(4.5);
        existingBook.setRatingCount(150);
        existingBook.setCoverImageUrl("https://example.com/covers/gatsby.jpg");
        existingBook.setCreatedAt(LocalDateTime.now());
        existingBook.setUpdatedAt(LocalDateTime.now());

        // Setup BookDto
        bookDto = new BookDto();
        bookDto.setId(1L);
        bookDto.setTitle("The Great Gatsby");
        bookDto.setAuthor("F. Scott Fitzgerald");
        bookDto.setIsbn("978-0-7432-7356-5");
        bookDto.setDescription("A classic American novel set in the Jazz Age");
        bookDto.setPublicationYear(1925);
        bookDto.setPublisher("Scribner");
        bookDto.setPageCount(180);
        bookDto.setCategoryId(1L);
        bookDto.setCategoryName("Fiction");
        bookDto.setAverageRating(4.5);
        bookDto.setRatingCount(150);
        bookDto.setCoverImageUrl("https://example.com/covers/gatsby.jpg");
        bookDto.setCreatedAt(LocalDateTime.now());
        bookDto.setUpdatedAt(LocalDateTime.now());

        // Setup CreateBookRequest
        createBookRequest = new CreateBookRequest();
        createBookRequest.setTitle("To Kill a Mockingbird");
        createBookRequest.setAuthor("Harper Lee");
        createBookRequest.setIsbn("978-0-06-112008-4");
        createBookRequest.setDescription("A gripping tale of racial injustice and childhood innocence");
        createBookRequest.setPublicationYear(1960);
        createBookRequest.setPublisher("J.B. Lippincott & Co.");
        createBookRequest.setPageCount(281);
        createBookRequest.setCategoryId(1L);
        createBookRequest.setCoverImageUrl("https://example.com/covers/mockingbird.jpg");

        // Setup UpdateBookRequest
        updateBookRequest = new UpdateBookRequest();
        updateBookRequest.setDescription("Updated description for this classic novel");
        updateBookRequest.setPageCount(185);
        updateBookRequest.setCoverImageUrl("https://example.com/covers/gatsby-new.jpg");
    }

    @Nested
    @DisplayName("Add Existing Book Tests")
    class AddExistingBookTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should add existing book to user library successfully")
        void shouldAddExistingBookToUserLibrarySuccessfully() throws Exception {
            // Given
            when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(bookService.addBookToUserLibrary(anyLong(), anyLong())).thenReturn(bookDto);

            // When & Then
            mockMvc.perform(post("/api/books/{bookId}/add-to-library", 1L)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.title", is("The Great Gatsby")))
                    .andExpect(jsonPath("$.author", is("F. Scott Fitzgerald")))
                    .andExpect(jsonPath("$.isbn", is("978-0-7432-7356-5")))
                    .andExpect(jsonPath("$.categoryName", is("Fiction")));

            verify(userService).findByUsername("testuser");
            verify(bookService).addBookToUserLibrary(1L, 1L);
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should handle book not found when adding to library")
        void shouldHandleBookNotFoundWhenAddingToLibrary() throws Exception {
            // Given
            when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(bookService.addBookToUserLibrary(anyLong(), anyLong()))
                .thenThrow(new BookNotFoundException("Book not found with ID: 999"));

            // When & Then
            mockMvc.perform(post("/api/books/{bookId}/add-to-library", 999L)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("Book not found")))
                    .andExpected(jsonPath("$.timestamp", notNullValue()));

            verify(bookService).addBookToUserLibrary(999L, 1L);
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should handle book already in library")
        void shouldHandleBookAlreadyInLibrary() throws Exception {
            // Given
            when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(bookService.addBookToUserLibrary(anyLong(), anyLong()))
                .thenThrow(new BookAlreadyExistsException("Book already exists in user's library"));

            // When & Then
            mockMvc.perform(post("/api/books/{bookId}/add-to-library", 1L)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", containsString("already exists")))
                    .andExpected(jsonPath("$.timestamp", notNullValue()));

            verify(bookService).addBookToUserLibrary(1L, 1L);
        }
    }

    @Nested
    @DisplayName("Create New Book Tests")
    class CreateNewBookTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should create new book successfully")
        void shouldCreateNewBookSuccessfully() throws Exception {
            // Given
            BookDto newBookDto = new BookDto();
            newBookDto.setId(2L);
            newBookDto.setTitle("To Kill a Mockingbird");
            newBookDto.setAuthor("Harper Lee");
            newBookDto.setIsbn("978-0-06-112008-4");
            newBookDto.setDescription("A gripping tale of racial injustice and childhood innocence");
            newBookDto.setPublicationYear(1960);
            newBookDto.setPublisher("J.B. Lippincott & Co.");
            newBookDto.setPageCount(281);
            newBookDto.setCategoryId(1L);
            newBookDto.setCategoryName("Fiction");
            newBookDto.setCoverImageUrl("https://example.com/covers/mockingbird.jpg");
            newBookDto.setCreatedAt(LocalDateTime.now());
            newBookDto.setUpdatedAt(LocalDateTime.now());

            when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(bookService.createBook(any(CreateBookRequest.class), anyLong())).thenReturn(newBookDto);

            // When & Then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createBookRequest)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id", is(2)))
                    .andExpect(jsonPath("$.title", is("To Kill a Mockingbird")))
                    .andExpect(jsonPath("$.author", is("Harper Lee")))
                    .andExpect(jsonPath("$.isbn", is("978-0-06-112008-4")))
                    .andExpect(jsonPath("$.publicationYear", is(1960)))
                    .andExpect(jsonPath("$.categoryName", is("Fiction")));

            verify(userService).findByUsername("testuser");
            verify(bookService).createBook(any(CreateBookRequest.class), eq(1L));
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should validate required fields when creating book")
        void shouldValidateRequiredFieldsWhenCreatingBook() throws Exception {
            // Given - empty request
            CreateBookRequest invalidRequest = new CreateBookRequest();

            // When & Then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("validation failed")))
                    .andExpect(jsonPath("$.errors", notNullValue()));

            verifyNoInteractions(bookService);
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should validate ISBN format when creating book")
        void shouldValidateIsbnFormatWhenCreatingBook() throws Exception {
            // Given
            createBookRequest.setIsbn("invalid-isbn");

            // When & Then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createBookRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("ISBN")))
                    .andExpect(jsonPath("$.errors.isbn", notNullValue()));

            verifyNoInteractions(bookService);
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should handle category not found when creating book")
        void shouldHandleCategoryNotFoundWhenCreatingBook() throws Exception {
            // Given
            when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(bookService.createBook(any(CreateBookRequest.class), anyLong()))
                .thenThrow(new CategoryNotFoundException("Category not found with ID: 999"));

            createBookRequest.setCategoryId(999L);

            // When & Then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createBookRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("Category not found")))
                    .andExpected(jsonPath("$.timestamp", notNullValue()));

            verify(bookService).createBook(any(CreateBookRequest.class), eq(1L));
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should handle duplicate ISBN when creating book")
        void shouldHandleDuplicateIsbnWhenCreatingBook() throws Exception {
            // Given
            when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(bookService.createBook(any(CreateBookRequest.class), anyLong()))
                .thenThrow(new BookAlreadyExistsException("Book with ISBN 978-0-06-112008-4 already exists"));

            // When & Then
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createBookRequest)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message", containsString("already exists")))
                    .andExpected(jsonPath("$.timestamp", notNullValue()));

            verify(bookService).createBook(any(CreateBookRequest.class), eq(1L));
        }
    }

    @Nested
    @DisplayName("Update Book Tests")
    class UpdateBookTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should update book successfully")
        void shouldUpdateBookSuccessfully() throws Exception {
            // Given
            BookDto updatedBookDto = new BookDto();
            updatedBookDto.setId(1L);
            updatedBookDto.setTitle("The Great Gatsby");
            updatedBookDto.setAuthor("F. Scott Fitzgerald");
            updatedBookDto.setIsbn("978-0-7432-7356-5");
            updatedBookDto.setDescription("Updated description for this classic novel");
            updatedBookDto.setPublicationYear(1925);
            updatedBookDto.setPublisher("Scribner");
            updatedBookDto.setPageCount(185);
            updatedBookDto.setCategoryId(1L);
            updatedBookDto.setCategoryName("Fiction");
            updatedBookDto.setAverageRating(4.5);
            updatedBookDto.setRatingCount(150);
            updatedBookDto.setCoverImageUrl("https://example.com/covers/gatsby-new.jpg");
            updatedBookDto.setCreatedAt(LocalDateTime.now());
            updatedBookDto.setUpdatedAt(LocalDateTime.now());

            when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(bookService.updateBook(anyLong(), any(UpdateBookRequest.class), anyLong()))
                .thenReturn(updatedBookDto);

            // When & Then
            mockMvc.perform(put("/api/books/{bookId}", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateBookRequest)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpected(jsonPath("$.id", is(1)))
                    .andExpected(jsonPath("$.description", is("Updated description for this classic novel")))
                    .andExpected(jsonPath("$.pageCount", is(185)))
                    .andExpected(jsonPath("$.coverImageUrl", is("https://example.com/covers/gatsby-new.jpg")));

            verify(userService).findByUsername("testuser");
            verify(bookService).updateBook(eq(1L), any(UpdateBookRequest.class), eq(1L));
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should handle book not found when updating")
        void shouldHandleBookNotFoundWhenUpdating() throws Exception {
            // Given
            when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(bookService.updateBook(anyLong(), any(UpdateBookRequest.class), anyLong()))
                .thenThrow(new BookNotFoundException("Book not found with ID: 999"));

            // When & Then
            mockMvc.perform(put("/api/books/{bookId}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateBookRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("Book not found")))
                    .andExpected(jsonPath("$.timestamp", notNullValue()));

            verify(bookService).updateBook(eq(999L), any(UpdateBookRequest.class), eq(1L));
        }
    }

    @Nested
    @DisplayName("Book Validation Tests")
    class BookValidationTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should validate book by ISBN")
        void shouldValidateBookByIsbn() throws Exception {
            // Given
            BookValidationResponse validationResponse = new BookValidationResponse();
            validationResponse.setValid(true);
            validationResponse.setIsbn("978-0-7432-7356-5");
            validationResponse.setTitle("The Great Gatsby");
            validationResponse.setAuthor("F. Scott Fitzgerald");
            validationResponse.setPublisher("Scribner");
            validationResponse.setPublicationYear(1925);
            validationResponse.setPageCount(180);
            validationResponse.setCoverImageUrl("https://example.com/covers/gatsby.jpg");
            validationResponse.setExistsInDatabase(true);

            when(bookService.validateBookByIsbn(anyString())).thenReturn(validationResponse);

            // When & Then
            mockMvc.perform(get("/api/books/validate")
                    .param("isbn", "978-0-7432-7356-5")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpected(jsonPath("$.valid", is(true)))
                    .andExpected(jsonPath("$.isbn", is("978-0-7432-7356-5")))
                    .andExpected(jsonPath("$.title", is("The Great Gatsby")))
                    .andExpected(jsonPath("$.author", is("F. Scott Fitzgerald")))
                    .andExpected(jsonPath("$.existsInDatabase", is(true)));

            verify(bookService).validateBookByIsbn("978-0-7432-7356-5");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should handle invalid ISBN validation")
        void shouldHandleInvalidIsbnValidation() throws Exception {
            // Given
            BookValidationResponse validationResponse = new BookValidationResponse();
            validationResponse.setValid(false);
            validationResponse.setIsbn("invalid-isbn");
            validationResponse.setErrorMessage("Invalid ISBN format");

            when(bookService.validateBookByIsbn(anyString())).thenReturn(validationResponse);

            // When & Then
            mockMvc.perform(get("/api/books/validate")
                    .param("isbn", "invalid-isbn")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpected(jsonPath("$.valid", is(false)))
                    .andExpected(jsonPath("$.isbn", is("invalid-isbn")))
                    .andExpected(jsonPath("$.errorMessage", is("Invalid ISBN format")));

            verify(bookService).validateBookByIsbn("invalid-isbn");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should enrich book data from external sources")
        void shouldEnrichBookDataFromExternalSources() throws Exception {
            // Given
            BookValidationResponse enrichedResponse = new BookValidationResponse();
            enrichedResponse.setValid(true);
            enrichedResponse.setIsbn("978-0-06-112008-4");
            enrichedResponse.setTitle("To Kill a Mockingbird");
            enrichedResponse.setAuthor("Harper Lee");
            enrichedResponse.setPublisher("J.B. Lippincott & Co.");
            enrichedResponse.setPublicationYear(1960);
            enrichedResponse.setPageCount(281);
            enrichedResponse.setDescription("A gripping tale of racial injustice and childhood innocence");
            enrichedResponse.setCoverImageUrl("https://covers.openlibrary.org/b/isbn/9780061120084-L.jpg");
            enrichedResponse.setExistsInDatabase(false);
            enrichedResponse.setEnrichedFromExternalSource(true);
            enrichedResponse.setExternalSource("Open Library");

            when(bookService.validateBookByIsbn(anyString())).thenReturn(enrichedResponse);

            // When & Then
            mockMvc.perform(get("/api/books/validate")
                    .param("isbn", "978-0-06-112008-4")
                    .param("enrich", "true")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpected(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpected(jsonPath("$.valid", is(true)))
                    .andExpected(jsonPath("$.enrichedFromExternalSource", is(true)))
                    .andExpected(jsonPath("$.externalSource", is("Open Library")))
                    .andExpected(jsonPath("$.description", notNullValue()))
                    .andExpected(jsonPath("$.coverImageUrl", notNullValue()));

            verify(bookService).validateBookByIsbn("978-0-06-112008-4");
        }
    }

    @Nested
    @DisplayName("Permission Tests")
    class PermissionTests {

        @Test
        @DisplayName("Should require authentication for all endpoints")
        void shouldRequireAuthenticationForAllEndpoints() throws Exception {
            // Test add to library endpoint
            mockMvc.perform(post("/api/books/{bookId}/add-to-library", 1L)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());

            // Test create book endpoint
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createBookRequest)))
                    .andExpect(status().isUnauthorized());

            // Test update book endpoint
            mockMvc.perform(put("/api/books/{bookId}", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateBookRequest)))
                    .andExpect(status().isUnauthorized());

            // Test validate endpoint
            mockMvc.perform(get("/api/books/validate")
                    .param("isbn", "978-0-7432-7356-5")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @WithMockUser(username = "differentuser")
        @DisplayName("Should enforce user ownership for book operations")
        void shouldEnforceUserOwnershipForBookOperations() throws Exception {
            // Given
            User differentUser = new User();
            differentUser.setId(2L);
            differentUser.setUsername("differentuser");

            when(userService.findByUsername("differentuser")).thenReturn(Optional.of(differentUser));
            when(bookService.updateBook(anyLong(), any(UpdateBookRequest.class), anyLong()))
                .thenThrow(new RuntimeException("User does not have permission to modify this book"));

            // When & Then
            mockMvc.perform(put("/api/books/{bookId}", 1L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateBookRequest)))
                    .andExpect(status().isForbidden())
                    .andExpected(jsonPath("$.message", containsString("permission")));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should handle service exceptions gracefully")
        void shouldHandleServiceExceptionsGracefully() throws Exception {
            // Given
            when(userService.findByUsername("testuser")).thenReturn(Optional.of(testUser));
            when(bookService.addBookToUserLibrary(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(post("/api/books/{bookId}/add-to-library", 1L)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpected(jsonPath("$.message", containsString("An error occurred")))
                    .andExpected(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @WithMockUser(username = "nonexistentuser")
        @DisplayName("Should handle user not found gracefully")
        void shouldHandleUserNotFoundGracefully() throws Exception {
            // Given
            when(userService.findByUsername("nonexistentuser")).thenReturn(Optional.empty());

            // When & Then
            mockMvc.perform(post("/api/books/{bookId}/add-to-library", 1L)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpected(jsonPath("$.message", containsString("User not found")));

            verifyNoInteractions(bookService);
        }
    }
}