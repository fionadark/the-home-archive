package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.book.BookResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ExternalBookSearchService.
 * Tests orchestration of multiple external APIs and fallback strategies.
 */
@ExtendWith(MockitoExtension.class)
class ExternalBookSearchServiceTest {

    @Mock
    private OpenLibraryService openLibraryService;

    @Mock
    private GoogleBooksService googleBooksService;

    private ExternalBookSearchService externalBookSearchService;

    @BeforeEach
    void setUp() {
        externalBookSearchService = new ExternalBookSearchService();
        // Use reflection to set the private fields
        try {
            java.lang.reflect.Field openLibraryField = ExternalBookSearchService.class.getDeclaredField("openLibraryService");
            openLibraryField.setAccessible(true);
            openLibraryField.set(externalBookSearchService, openLibraryService);
            
            java.lang.reflect.Field googleBooksField = ExternalBookSearchService.class.getDeclaredField("googleBooksService");
            googleBooksField.setAccessible(true);
            googleBooksField.set(externalBookSearchService, googleBooksService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test", e);
        }
    }

    @Test
    void searchBooks_WithValidQuery_ReturnsMergedResults() {
        // Given
        String query = "Java programming";
        List<BookResponse> openLibraryResults = createMockBooks("OpenLibrary Book", "OL123456789");
        List<BookResponse> googleBooksResults = createMockBooks("Google Book", "GB987654321");

        when(openLibraryService.searchBooks(eq(query), anyInt())).thenReturn(openLibraryResults);
        when(googleBooksService.searchBooks(eq(query), anyInt())).thenReturn(googleBooksResults);

        // When
        List<BookResponse> results = externalBookSearchService.searchBooks(query, 20);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        
        // OpenLibrary results should come first (higher priority)
        assertEquals("OpenLibrary Book", results.get(0).getTitle());
        assertEquals("Google Book", results.get(1).getTitle());
    }

    @Test
    void searchBooks_WithDuplicateISBNs_RemovesDuplicates() {
        // Given
        String query = "test";
        String duplicateIsbn = "1234567890";
        
        List<BookResponse> openLibraryResults = createMockBooks("OpenLibrary Book", duplicateIsbn);
        List<BookResponse> googleBooksResults = createMockBooks("Google Book", duplicateIsbn);

        when(openLibraryService.searchBooks(eq(query), anyInt())).thenReturn(openLibraryResults);
        when(googleBooksService.searchBooks(eq(query), anyInt())).thenReturn(googleBooksResults);

        // When
        List<BookResponse> results = externalBookSearchService.searchBooks(query, 20);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size()); // Duplicate should be removed
        assertEquals("OpenLibrary Book", results.get(0).getTitle()); // OpenLibrary has priority
    }

    @Test
    void searchBooks_WithDuplicateTitles_RemovesDuplicates() {
        // Given
        String query = "test";
        String duplicateTitle = "Same Title";
        
        BookResponse openLibraryBook = new BookResponse();
        openLibraryBook.setTitle(duplicateTitle);
        openLibraryBook.setAuthor("Author 1");
        
        BookResponse googleBook = new BookResponse();
        googleBook.setTitle(duplicateTitle);
        googleBook.setAuthor("Author 2");

        when(openLibraryService.searchBooks(eq(query), anyInt())).thenReturn(List.of(openLibraryBook));
        when(googleBooksService.searchBooks(eq(query), anyInt())).thenReturn(List.of(googleBook));

        // When
        List<BookResponse> results = externalBookSearchService.searchBooks(query, 20);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size()); // Duplicate should be removed
        assertEquals("Author 1", results.get(0).getAuthor()); // OpenLibrary has priority
    }

    @Test
    void searchBooks_WithEmptyQuery_ReturnsEmptyList() {
        // When
        List<BookResponse> results = externalBookSearchService.searchBooks("", 10);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verifyNoInteractions(openLibraryService, googleBooksService);
    }

    @Test
    void searchBooks_WithNullQuery_ReturnsEmptyList() {
        // When
        List<BookResponse> results = externalBookSearchService.searchBooks(null, 10);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
        verifyNoInteractions(openLibraryService, googleBooksService);
    }

    @Test
    void searchBooks_WhenOpenLibraryFails_StillReturnsGoogleResults() {
        // Given
        String query = "test";
        List<BookResponse> googleBooksResults = createMockBooks("Google Book", "GB123456789");

        when(openLibraryService.searchBooks(eq(query), anyInt()))
            .thenThrow(new RuntimeException("OpenLibrary unavailable"));
        when(googleBooksService.searchBooks(eq(query), anyInt())).thenReturn(googleBooksResults);

        // When
        List<BookResponse> results = externalBookSearchService.searchBooks(query, 20);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Google Book", results.get(0).getTitle());
    }

    @Test
    void searchBooks_WhenGoogleBooksFails_StillReturnsOpenLibraryResults() {
        // Given
        String query = "test";
        List<BookResponse> openLibraryResults = createMockBooks("OpenLibrary Book", "OL123456789");

        when(openLibraryService.searchBooks(eq(query), anyInt())).thenReturn(openLibraryResults);
        when(googleBooksService.searchBooks(eq(query), anyInt()))
            .thenThrow(new RuntimeException("Google Books unavailable"));

        // When
        List<BookResponse> results = externalBookSearchService.searchBooks(query, 20);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("OpenLibrary Book", results.get(0).getTitle());
    }

    @Test
    void searchBooks_WhenBothAPIsFail_ReturnsEmptyList() {
        // Given
        String query = "test";

        when(openLibraryService.searchBooks(eq(query), anyInt()))
            .thenThrow(new RuntimeException("OpenLibrary unavailable"));
        when(googleBooksService.searchBooks(eq(query), anyInt()))
            .thenThrow(new RuntimeException("Google Books unavailable"));

        // When
        List<BookResponse> results = externalBookSearchService.searchBooks(query, 20);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void searchByIsbn_WithValidIsbn_ReturnsFirstAvailableResult() {
        // Given
        String isbn = "1234567890";
        List<BookResponse> openLibraryResults = createMockBooks("OpenLibrary Book", isbn);

        when(openLibraryService.searchByIsbn(isbn)).thenReturn(openLibraryResults);

        // When
        List<BookResponse> results = externalBookSearchService.searchByIsbn(isbn);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("OpenLibrary Book", results.get(0).getTitle());
        
        // Google Books should not be called since OpenLibrary returned results
        verifyNoInteractions(googleBooksService);
    }

    @Test
    void searchByIsbn_WhenOpenLibraryEmpty_FallsBackToGoogleBooks() {
        // Given
        String isbn = "1234567890";
        List<BookResponse> googleBooksResults = createMockBooks("Google Book", isbn);

        when(openLibraryService.searchByIsbn(isbn)).thenReturn(Collections.emptyList());
        when(googleBooksService.searchByIsbn(isbn)).thenReturn(googleBooksResults);

        // When
        List<BookResponse> results = externalBookSearchService.searchByIsbn(isbn);

        // Then
        assertNotNull(results);
        assertEquals(1, results.size());
        assertEquals("Google Book", results.get(0).getTitle());
    }

    @Test
    void getHealthStatus_ChecksBothServices() {
        // Given
        when(openLibraryService.searchBooks("test", 1)).thenReturn(Collections.emptyList());
        when(googleBooksService.searchBooks("test", 1)).thenReturn(Collections.emptyList());

        // When
        ExternalBookSearchService.ExternalApiHealthStatus status = externalBookSearchService.getHealthStatus();

        // Then
        assertNotNull(status);
        assertTrue(status.isOpenLibraryHealthy());
        assertTrue(status.isGoogleBooksHealthy());
        assertTrue(status.areAllServicesHealthy());
        assertTrue(status.isAnyServiceHealthy());
    }

    @Test
    void getHealthStatus_WhenOpenLibraryFails_ReportsCorrectly() {
        // Given
        when(openLibraryService.searchBooks("test", 1))
            .thenThrow(new RuntimeException("Service unavailable"));
        when(googleBooksService.searchBooks("test", 1)).thenReturn(Collections.emptyList());

        // When
        ExternalBookSearchService.ExternalApiHealthStatus status = externalBookSearchService.getHealthStatus();

        // Then
        assertNotNull(status);
        assertFalse(status.isOpenLibraryHealthy());
        assertTrue(status.isGoogleBooksHealthy());
        assertFalse(status.areAllServicesHealthy());
        assertTrue(status.isAnyServiceHealthy());
    }

    private List<BookResponse> createMockBooks(String title, String isbn) {
        BookResponse book = new BookResponse();
        book.setTitle(title);
        book.setAuthor("Test Author");
        book.setIsbn(isbn);
        book.setDescription("Test Description");
        book.setPublicationYear(2023);
        return List.of(book);
    }
}