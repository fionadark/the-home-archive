package com.thehomearchive.library.service;

import com.thehomearchive.library.config.GoogleBooksConfig;
import com.thehomearchive.library.dto.book.BookResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GoogleBooksService.
 * Tests search functionality and circuit breaker fallback behavior.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoogleBooksServiceTest {

    @Mock
    private GoogleBooksConfig config;

    @Mock
    private RestTemplate restTemplate;

    private GoogleBooksService googleBooksService;

    @BeforeEach
    void setUp() {
        // Only set up minimal configuration that all tests need
        when(config.getMaxResults()).thenReturn(20);
        
        googleBooksService = new GoogleBooksService(config, restTemplate);
    }

    @Test
    void searchBooks_WithValidQuery_ReturnsResults() {
        // Given
        String query = "Java programming";
        when(config.getBaseUrl()).thenReturn("https://www.googleapis.com/books/v1");
        when(config.hasApiKey()).thenReturn(false);
        Map<String, Object> mockResponse = createMockGoogleBooksResponse();
        when(restTemplate.getForObject(any(), eq(Map.class))).thenReturn(mockResponse);

        // When
        List<BookResponse> results = googleBooksService.searchBooks(query, 10);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        
        BookResponse book = results.get(0);
        assertEquals("Test Book", book.getTitle());
        assertEquals("Test Author", book.getAuthor());
        assertEquals("1234567890", book.getIsbn());
    }

    @Test
    void searchBooks_WithEmptyQuery_ReturnsEmptyList() {
        // When
        List<BookResponse> results = googleBooksService.searchBooks("", 10);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void searchBooks_WithNullQuery_ReturnsEmptyList() {
        // When
        List<BookResponse> results = googleBooksService.searchBooks(null, 10);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verifyNoInteractions(restTemplate);
    }

    @Test
    void searchBooks_WhenApiThrowsException_CircuitBreakerHandles() {
        // Given
        String query = "test";
        when(config.getBaseUrl()).thenReturn("https://www.googleapis.com/books/v1");
        when(config.hasApiKey()).thenReturn(false);
        when(restTemplate.getForObject(any(), eq(Map.class)))
            .thenThrow(new RestClientException("API unavailable"));

        // When & Then
        assertThrows(RestClientException.class, () -> {
            googleBooksService.searchBooks(query, 10);
        });
    }

    @Test
    void searchByIsbn_WithValidIsbn_ReturnsResults() {
        // Given
        String isbn = "9781234567890";
        when(config.getBaseUrl()).thenReturn("https://www.googleapis.com/books/v1");
        when(config.hasApiKey()).thenReturn(false);
        Map<String, Object> mockResponse = createMockGoogleBooksResponse();
        when(restTemplate.getForObject(any(), eq(Map.class))).thenReturn(mockResponse);

        // When
        List<BookResponse> results = googleBooksService.searchByIsbn(isbn);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Book", results.get(0).getTitle());
    }

    @Test
    void searchByTitle_WithValidTitle_ReturnsResults() {
        // Given
        String title = "Test Book";
        when(config.getBaseUrl()).thenReturn("https://www.googleapis.com/books/v1");
        when(config.hasApiKey()).thenReturn(false);
        Map<String, Object> mockResponse = createMockGoogleBooksResponse();
        when(restTemplate.getForObject(any(), eq(Map.class))).thenReturn(mockResponse);

        // When
        List<BookResponse> results = googleBooksService.searchByTitle(title, 10);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Book", results.get(0).getTitle());
    }

    @Test
    void searchByAuthor_WithValidAuthor_ReturnsResults() {
        // Given
        String author = "Test Author";
        when(config.getBaseUrl()).thenReturn("https://www.googleapis.com/books/v1");
        when(config.hasApiKey()).thenReturn(false);
        Map<String, Object> mockResponse = createMockGoogleBooksResponse();
        when(restTemplate.getForObject(any(), eq(Map.class))).thenReturn(mockResponse);

        // When
        List<BookResponse> results = googleBooksService.searchByAuthor(author, 10);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Test Author", results.get(0).getAuthor());
    }

    @Test
    void searchBooks_WithApiKey_IncludesKeyInRequest() {
        // Given
        when(config.hasApiKey()).thenReturn(true);
        when(config.getApiKey()).thenReturn("test-api-key");
        
        String query = "test";
        Map<String, Object> mockResponse = createMockGoogleBooksResponse();
        when(restTemplate.getForObject(any(), eq(Map.class))).thenReturn(mockResponse);

        // When
        googleBooksService.searchBooks(query, 10);

        // Then
        verify(restTemplate).getForObject(argThat(uri -> 
            uri.toString().contains("key=test-api-key")), eq(Map.class));
    }

    @Test
    void searchBooks_WithNullResponse_ReturnsEmptyList() {
        // Given
        String query = "test";
        when(config.getBaseUrl()).thenReturn("https://www.googleapis.com/books/v1");
        when(config.hasApiKey()).thenReturn(false);
        when(restTemplate.getForObject(any(), eq(Map.class))).thenReturn(null);

        // When
        List<BookResponse> results = googleBooksService.searchBooks(query, 10);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void searchBooks_WithEmptyItemsResponse_ReturnsEmptyList() {
        // Given
        String query = "test";
        when(config.getBaseUrl()).thenReturn("https://www.googleapis.com/books/v1");
        when(config.hasApiKey()).thenReturn(false);
        Map<String, Object> emptyResponse = new HashMap<>();
        emptyResponse.put("items", Collections.emptyList());
        when(restTemplate.getForObject(any(), eq(Map.class))).thenReturn(emptyResponse);

        // When
        List<BookResponse> results = googleBooksService.searchBooks(query, 10);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    // Fallback method tests (these would be handled by circuit breaker in integration)
    @Test
    void searchBooksFallback_ReturnsEmptyList() {
        // When
        List<BookResponse> results = googleBooksService.searchBooksFallback("test", 10, new RuntimeException("Test"));

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void searchByIsbnFallback_ReturnsEmptyList() {
        // When
        List<BookResponse> results = googleBooksService.searchByIsbnFallback("1234567890", new RuntimeException("Test"));

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    private Map<String, Object> createMockGoogleBooksResponse() {
        Map<String, Object> response = new HashMap<>();
        
        Map<String, Object> volumeInfo = new HashMap<>();
        volumeInfo.put("title", "Test Book");
        volumeInfo.put("authors", List.of("Test Author"));
        volumeInfo.put("description", "Test Description");
        volumeInfo.put("publisher", "Test Publisher");
        volumeInfo.put("publishedDate", "2023");
        volumeInfo.put("pageCount", 200);
        volumeInfo.put("categories", List.of("Programming"));
        
        // Industry identifiers (ISBN)
        Map<String, Object> isbn13 = new HashMap<>();
        isbn13.put("type", "ISBN_13");
        isbn13.put("identifier", "1234567890");
        volumeInfo.put("industryIdentifiers", List.of(isbn13));
        
        // Image links
        Map<String, Object> imageLinks = new HashMap<>();
        imageLinks.put("thumbnail", "http://example.com/cover.jpg");
        volumeInfo.put("imageLinks", imageLinks);
        
        // Ratings
        volumeInfo.put("averageRating", 4.5);
        volumeInfo.put("ratingsCount", 100);
        
        Map<String, Object> item = new HashMap<>();
        item.put("volumeInfo", volumeInfo);
        
        response.put("items", List.of(item));
        return response;
    }
}