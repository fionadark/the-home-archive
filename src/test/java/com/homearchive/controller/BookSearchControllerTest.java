package com.homearchive.controller;

import com.homearchive.dto.SearchRequest;
import com.homearchive.dto.SearchResponse;
import com.homearchive.dto.SortBy;
import com.homearchive.dto.SortOrder;
import com.homearchive.entity.PhysicalLocation;
import com.homearchive.service.BookSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookSearchController.
 * Focuses on testing the consolidated error handling patterns and method functionality.
 */
@ExtendWith(MockitoExtension.class)
class BookSearchControllerTest {

    @Mock
    private BookSearchService bookSearchService;

    @InjectMocks
    private BookSearchController bookSearchController;

    private SearchResponse mockSuccessResponse;

    @BeforeEach
    void setUp() {
        mockSuccessResponse = new SearchResponse();
        mockSuccessResponse.setQuery("test");
        mockSuccessResponse.setTotalResults(5);
        // Note: ResultCount is calculated from books.size(), so we don't set it directly
    }

    // Test successful operations

    @Test
    void testSearchBooks_Success() {
        // Given
        when(bookSearchService.searchBooks(any(SearchRequest.class))).thenReturn(mockSuccessResponse);

        // When
        ResponseEntity<SearchResponse> response = bookSearchController.searchBooks(
                "test query", SortBy.TITLE, SortOrder.ASC, 10, PhysicalLocation.LIVING_ROOM);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockSuccessResponse, response.getBody());
        verify(bookSearchService).searchBooks(any(SearchRequest.class));
    }

    @Test
    void testSearchByTitle_Success() {
        // Given
        when(bookSearchService.searchByTitle(anyString(), anyInt())).thenReturn(mockSuccessResponse);

        // When
        ResponseEntity<SearchResponse> response = bookSearchController.searchByTitle("Test Title", 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockSuccessResponse, response.getBody());
        verify(bookSearchService).searchByTitle("Test Title", 10);
    }

    @Test
    void testSearchByAuthor_Success() {
        // Given
        when(bookSearchService.searchByAuthor(anyString(), anyInt())).thenReturn(mockSuccessResponse);

        // When
        ResponseEntity<SearchResponse> response = bookSearchController.searchByAuthor("Test Author", 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockSuccessResponse, response.getBody());
        verify(bookSearchService).searchByAuthor("Test Author", 10);
    }

    @Test
    void testSearchByGenre_Success() {
        // Given
        when(bookSearchService.searchByGenre(anyString(), anyInt())).thenReturn(mockSuccessResponse);

        // When
        ResponseEntity<SearchResponse> response = bookSearchController.searchByGenre("Science Fiction", 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockSuccessResponse, response.getBody());
        verify(bookSearchService).searchByGenre("Science Fiction", 10);
    }

    @Test
    void testGetAllBooks_Success() {
        // Given
        when(bookSearchService.getAllBooks(anyInt(), any(SortBy.class))).thenReturn(mockSuccessResponse);

        // When
        ResponseEntity<SearchResponse> response = bookSearchController.getAllBooks(SortBy.TITLE, 10);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockSuccessResponse, response.getBody());
        verify(bookSearchService).getAllBooks(10, SortBy.TITLE);
    }

    // Test error handling - verifying the consolidated error handling works consistently

    @Test
    void testSearchBooks_ErrorHandling() {
        // Given
        when(bookSearchService.searchBooks(any(SearchRequest.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

        // When
        ResponseEntity<SearchResponse> response = bookSearchController.searchBooks(
                "test", SortBy.TITLE, SortOrder.ASC, 10, null);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof SearchResponse);
        verify(bookSearchService).searchBooks(any(SearchRequest.class));
    }

    @Test
    void testSearchByTitle_ErrorHandling() {
        // Given
        when(bookSearchService.searchByTitle(anyString(), anyInt()))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When
        ResponseEntity<SearchResponse> response = bookSearchController.searchByTitle("Test", 10);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof SearchResponse);
        verify(bookSearchService).searchByTitle("Test", 10);
    }

    @Test
    void testSearchByAuthor_ErrorHandling() {
        // Given
        when(bookSearchService.searchByAuthor(anyString(), anyInt()))
                .thenThrow(new IllegalArgumentException("Invalid author parameter"));

        // When
        ResponseEntity<SearchResponse> response = bookSearchController.searchByAuthor("Invalid", 10);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof SearchResponse);
        verify(bookSearchService).searchByAuthor("Invalid", 10);
    }

    @Test
    void testSearchByGenre_ErrorHandling() {
        // Given
        when(bookSearchService.searchByGenre(anyString(), anyInt()))
                .thenThrow(new NullPointerException("Null genre parameter"));

        // When
        ResponseEntity<SearchResponse> response = bookSearchController.searchByGenre("Invalid", 10);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof SearchResponse);
        verify(bookSearchService).searchByGenre("Invalid", 10);
    }

    @Test
    void testGetAllBooks_ErrorHandling() {
        // Given
        when(bookSearchService.getAllBooks(anyInt(), any(SortBy.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // When
        ResponseEntity<SearchResponse> response = bookSearchController.getAllBooks(SortBy.TITLE, 10);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof SearchResponse);
        verify(bookSearchService).getAllBooks(10, SortBy.TITLE);
    }

    // Test health check endpoint (no error handling needed here)

    @Test
    void testHealthCheck() {
        // When
        ResponseEntity<String> response = bookSearchController.healthCheck();

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Book search service is healthy", response.getBody());
        verifyNoInteractions(bookSearchService); // Health check shouldn't use the service
    }

    // Test parameter handling

    @Test
    void testSearchBooks_WithNullParameters() {
        // Given
        when(bookSearchService.searchBooks(any(SearchRequest.class))).thenReturn(mockSuccessResponse);

        // When
        ResponseEntity<SearchResponse> response = bookSearchController.searchBooks(
                null, null, null, null, null);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(bookSearchService).searchBooks(any(SearchRequest.class));
    }

    @Test
    void testSearchBooks_WithValidPhysicalLocation() {
        // Given
        when(bookSearchService.searchBooks(any(SearchRequest.class))).thenReturn(mockSuccessResponse);

        // When
        ResponseEntity<SearchResponse> response = bookSearchController.searchBooks(
                "query", SortBy.RELEVANCE, SortOrder.DESC, 20, PhysicalLocation.HOME_OFFICE);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockSuccessResponse, response.getBody());
        
        // Verify the SearchRequest was created with the correct physical location
        verify(bookSearchService).searchBooks(argThat(request -> 
            request.getPhysicalLocation() == PhysicalLocation.HOME_OFFICE
        ));
    }

    // Integration test to verify all methods use the same error handling pattern

    @Test
    void testConsistentErrorHandlingAcrossAllMethods() {
        // Given - All service methods throw the same exception
        RuntimeException testException = new RuntimeException("Test exception");
        when(bookSearchService.searchBooks(any(SearchRequest.class))).thenThrow(testException);
        when(bookSearchService.searchByTitle(anyString(), anyInt())).thenThrow(testException);
        when(bookSearchService.searchByAuthor(anyString(), anyInt())).thenThrow(testException);
        when(bookSearchService.searchByGenre(anyString(), anyInt())).thenThrow(testException);
        when(bookSearchService.getAllBooks(anyInt(), any(SortBy.class))).thenThrow(testException);

        // When - Call all search methods
        ResponseEntity<SearchResponse> searchResponse = bookSearchController.searchBooks("test", SortBy.TITLE, SortOrder.ASC, 10, null);
        ResponseEntity<SearchResponse> titleResponse = bookSearchController.searchByTitle("test", 10);
        ResponseEntity<SearchResponse> authorResponse = bookSearchController.searchByAuthor("test", 10);
        ResponseEntity<SearchResponse> genreResponse = bookSearchController.searchByGenre("test", 10);
        ResponseEntity<SearchResponse> allBooksResponse = bookSearchController.getAllBooks(SortBy.TITLE, 10);

        // Then - All should have identical error handling behavior
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, searchResponse.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, titleResponse.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, authorResponse.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, genreResponse.getStatusCode());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, allBooksResponse.getStatusCode());

        // All should return empty SearchResponse objects
        assertNotNull(searchResponse.getBody());
        assertNotNull(titleResponse.getBody());
        assertNotNull(authorResponse.getBody());
        assertNotNull(genreResponse.getBody());
        assertNotNull(allBooksResponse.getBody());
    }
}