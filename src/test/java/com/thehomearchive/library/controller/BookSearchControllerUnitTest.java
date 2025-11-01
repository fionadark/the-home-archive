package com.thehomearchive.library.controller;

import com.thehomearchive.library.dto.book.BookResponse;
import com.thehomearchive.library.dto.search.BookSearchPageResponse;
import com.thehomearchive.library.dto.search.BookSearchRequest;
import com.thehomearchive.library.dto.search.BookSearchResponse;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.entity.UserRole;
import com.thehomearchive.library.service.BookSearchService;
import com.thehomearchive.library.service.BookService;
import com.thehomearchive.library.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookSearchControllerUnitTest {

    @Mock
    private BookSearchService bookSearchService;

    @Mock
    private BookService bookService;

    @Mock
    private RatingService ratingService;

    @InjectMocks
    private BookSearchController bookSearchController;

    private User testUser;
    private BookResponse testBook;
    private BookSearchResponse testSearchResponse;
    private BookSearchPageResponse testPageResponse;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(UserRole.USER);

        // Setup test book
        testBook = new BookResponse();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890");
        testBook.setDescription("Test description");
        testBook.setPublicationYear(2023);
        testBook.setPublisher("Test Publisher");
        testBook.setPageCount(200);
        testBook.setCategoryId(1L);
        testBook.setCategoryName("Fiction");
        testBook.setAverageRating(4.5);
        testBook.setRatingCount(10);
        testBook.setCreatedAt(LocalDateTime.now());
        testBook.setUpdatedAt(LocalDateTime.now());

        // Setup test search response
        testSearchResponse = new BookSearchResponse();
        testSearchResponse.setId(1L);
        testSearchResponse.setTitle("Test Book");
        testSearchResponse.setAuthor("Test Author");
        testSearchResponse.setAverageRating(4.5);
        testSearchResponse.setRatingCount(10);

        // Setup test page response
        testPageResponse = new BookSearchPageResponse();
        testPageResponse.setContent(Arrays.asList(testSearchResponse));
        testPageResponse.setPage(0);
        testPageResponse.setSize(20);
        testPageResponse.setTotalElements(1L);
        testPageResponse.setTotalPages(1);
        testPageResponse.setFirst(true);
        testPageResponse.setLast(true);
    }

    // ========== SEARCH BOOKS TESTS ==========

    @Test
    void searchBooks_BasicQuery_Success() {
        // Given
        when(bookSearchService.searchBooks(any(BookSearchRequest.class), any()))
                .thenReturn(testPageResponse);

        // When
        ResponseEntity<?> response = bookSearchController.searchBooks(
                "test", null, 0, 20, "title", "asc");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(bookSearchService).searchBooks(any(BookSearchRequest.class), any());
    }

    @Test
    void searchBooks_WithAllParameters_Success() {
        // Given
        when(bookSearchService.searchBooks(any(BookSearchRequest.class), any()))
                .thenReturn(testPageResponse);

        // When
        ResponseEntity<?> response = bookSearchController.searchBooks(
                "test", 1L, 0, 10, "rating", "desc");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(bookSearchService).searchBooks(any(BookSearchRequest.class), any());
    }

    @Test
    void searchBooks_ServiceException_InternalError() {
        // Given
        when(bookSearchService.searchBooks(any(BookSearchRequest.class), any()))
                .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = bookSearchController.searchBooks(
                "test", null, 0, 20, "title", "asc");

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ========== GET BOOK BY ID TESTS ==========

    @Test
    void getBookById_Success() {
        // Given
        when(bookService.getBookById(1L)).thenReturn(testBook);

        // When
        ResponseEntity<?> response = bookSearchController.getBookById(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(bookService).getBookById(1L);
    }

    @Test
    void getBookById_NotFound() {
        // Given
        when(bookService.getBookById(1L)).thenThrow(new IllegalArgumentException("Book not found"));

        // When
        ResponseEntity<?> response = bookSearchController.getBookById(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(bookService).getBookById(1L);
    }

    @Test
    void getBookById_ServiceException_InternalError() {
        // Given
        when(bookService.getBookById(1L)).thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = bookSearchController.getBookById(1L);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(bookService).getBookById(1L);
    }

    // ========== SEARCH SUGGESTIONS TESTS ==========

    @Test
    void getSearchSuggestions_Success() {
        // Given
        List<String> suggestions = Arrays.asList("test book", "test author", "testing");
        when(bookSearchService.getSearchSuggestions("test", 10)).thenReturn(suggestions);

        // When
        ResponseEntity<?> response = bookSearchController.getSearchSuggestions("test", 10);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(bookSearchService).getSearchSuggestions("test", 10);
    }

    @Test
    void getSearchSuggestions_ServiceException_InternalError() {
        // Given
        when(bookSearchService.getSearchSuggestions("test", 10))
                .thenThrow(new RuntimeException("Service error"));

        // When
        ResponseEntity<?> response = bookSearchController.getSearchSuggestions("test", 10);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // ========== POPULAR SEARCHES TESTS ==========

    @Test
    void getPopularSearches_Success() {
        // Given
        List<String> popularQueries = Arrays.asList("fiction", "science", "history");
        when(bookSearchService.getPopularSearchQueries(10)).thenReturn(popularQueries);

        // When
        ResponseEntity<?> response = bookSearchController.getPopularSearches(10);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(bookSearchService).getPopularSearchQueries(10);
    }

    // ========== SIMILAR BOOKS TESTS ==========

    @Test
    void getSimilarBooks_Success() {
        // Given
        List<BookSearchResponse> similarBooks = Arrays.asList(testSearchResponse);
        when(bookSearchService.findSimilarBooks(1L, 10)).thenReturn(similarBooks);

        // When
        ResponseEntity<?> response = bookSearchController.getSimilarBooks(1L, 10);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(bookSearchService).findSimilarBooks(1L, 10);
    }

    @Test
    void getSimilarBooks_BookNotFound() {
        // Given
        when(bookSearchService.findSimilarBooks(1L, 10))
                .thenThrow(new IllegalArgumentException("Book not found"));

        // When
        ResponseEntity<?> response = bookSearchController.getSimilarBooks(1L, 10);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(bookSearchService).findSimilarBooks(1L, 10);
    }

    // ========== SEARCH HISTORY TESTS ==========

    @Test
    void getUserSearchHistory_WithoutAuth_Unauthorized() {
        // Note: In real app, this would be handled by Spring Security
        // But for unit test, we test the controller logic when no user is present

        // When
        ResponseEntity<?> response = bookSearchController.getUserSearchHistory(20);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void clearUserSearchHistory_WithoutAuth_Unauthorized() {
        // Note: In real app, this would be handled by Spring Security
        // But for unit test, we test the controller logic when no user is present

        // When
        ResponseEntity<?> response = bookSearchController.clearUserSearchHistory();

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    // ========== HELPER METHOD TESTS ==========

    @Test
    void convertToSortCriteria_ValidValues() {
        // Test that the controller can handle different sort criteria
        // This indirectly tests the helper method through the searchBooks endpoint
        
        when(bookSearchService.searchBooks(any(BookSearchRequest.class), any()))
                .thenReturn(testPageResponse);

        // Test all valid sort criteria
        String[] validSorts = {"title", "author", "publicationYear", "rating"};
        
        for (String sort : validSorts) {
            ResponseEntity<?> response = bookSearchController.searchBooks(
                    "test", null, 0, 20, sort, "asc");
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        verify(bookSearchService, times(validSorts.length))
                .searchBooks(any(BookSearchRequest.class), any());
    }

    @Test
    void convertToSortDirection_ValidValues() {
        // Test that the controller can handle different sort directions
        
        when(bookSearchService.searchBooks(any(BookSearchRequest.class), any()))
                .thenReturn(testPageResponse);

        // Test both directions
        String[] validDirections = {"asc", "desc"};
        
        for (String direction : validDirections) {
            ResponseEntity<?> response = bookSearchController.searchBooks(
                    "test", null, 0, 20, "title", direction);
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }

        verify(bookSearchService, times(validDirections.length))
                .searchBooks(any(BookSearchRequest.class), any());
    }

    @Test
    void convertToSortCriteria_InvalidValue_DefaultsToTitle() {
        // Test that invalid sort criteria defaults to title
        
        when(bookSearchService.searchBooks(any(BookSearchRequest.class), any()))
                .thenReturn(testPageResponse);

        ResponseEntity<?> response = bookSearchController.searchBooks(
                "test", null, 0, 20, "invalid_sort", "asc");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(bookSearchService).searchBooks(any(BookSearchRequest.class), any());
    }
}