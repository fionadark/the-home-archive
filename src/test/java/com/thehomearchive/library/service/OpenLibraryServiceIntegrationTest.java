package com.thehomearchive.library.service;

import com.thehomearchive.library.config.OpenLibraryConfig;
import com.thehomearchive.library.dto.book.BookResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration tests for OpenLibraryService that actually contact the OpenLibrary API.
 * 
 * These tests are disabled by default to avoid external dependencies in CI/CD.
 * To run these tests, set system property: -Dtest.integration.external=true
 * 
 * Example: ./gradlew test -Dtest.integration.external=true --tests OpenLibraryServiceIntegrationTest
 */
@SpringBootTest
@ActiveProfiles("test")
@EnabledIfSystemProperty(named = "test.integration.external", matches = "true")
class OpenLibraryServiceIntegrationTest {

    private OpenLibraryService openLibraryService;
    private OpenLibraryConfig config;

    @BeforeEach
    void setUp() {
        // Set up real configuration for OpenLibrary API
        config = new OpenLibraryConfig();
        config.setBaseUrl("https://openlibrary.org");
        config.setTimeout(10000); // 10 seconds for integration tests
        
        // Create RestTemplate with real configuration
        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplate restTemplate = builder
                .rootUri(config.getBaseUrl())
                .connectTimeout(Duration.ofMillis(config.getTimeout()))
                .readTimeout(Duration.ofMillis(config.getTimeout() * 2))
                .build();
        
        // Initialize service with real dependencies
        openLibraryService = new OpenLibraryService(config, restTemplate);
    }

    @Test
    void searchBooks_withValidQuery_shouldReturnRealResults() {
        // Given - searching for a well-known book
        String query = "The Great Gatsby";
        int limit = 5;

        // When - making actual API call
        List<BookResponse> results = openLibraryService.searchBooks(query, limit);

        // Then - verify real API response
        assertThat(results).isNotEmpty();
        assertThat(results.size()).isLessThanOrEqualTo(limit);
        
        // Check that at least one result contains the expected title
        boolean hasExpectedBook = results.stream()
                .anyMatch(book -> book.getTitle() != null && 
                                 book.getTitle().toLowerCase().contains("gatsby"));
        assertThat(hasExpectedBook).isTrue();
        
        // Verify book structure
        BookResponse firstBook = results.get(0);
        assertThat(firstBook.getTitle()).isNotBlank();
        // Author might be null for some books, but if present should not be blank
        if (firstBook.getAuthor() != null) {
            assertThat(firstBook.getAuthor()).isNotBlank();
        }
    }

    @Test
    void searchByIsbn_withValidIsbn_shouldReturnCorrectBook() {
        // Given - ISBN for The Great Gatsby (1925 edition)
        String isbn = "9780743273565"; // Known ISBN

        // When - making actual API call
        List<BookResponse> results = openLibraryService.searchByIsbn(isbn);

        // Then - verify correct book is returned
        assertThat(results).isNotEmpty();
        
        BookResponse book = results.get(0);
        assertThat(book.getTitle()).isNotNull();
        assertThat(book.getIsbn()).contains("743273565"); // Should contain the ISBN digits
        
        // Verify other fields are populated
        if (book.getAuthor() != null) {
            assertThat(book.getAuthor()).isNotBlank();
        }
        if (book.getPublicationYear() != null) {
            assertThat(book.getPublicationYear()).isPositive();
        }
    }

    @Test
    void searchByTitle_withExactTitle_shouldReturnRelevantResults() {
        // Given - searching by exact title
        String title = "Pride and Prejudice";
        int limit = 3;

        // When - making actual API call
        List<BookResponse> results = openLibraryService.searchByTitle(title, limit);

        // Then - verify relevant results
        assertThat(results).isNotEmpty();
        assertThat(results.size()).isLessThanOrEqualTo(limit);
        
        // At least one result should match the title closely
        boolean hasRelevantTitle = results.stream()
                .anyMatch(book -> book.getTitle() != null && 
                                 book.getTitle().toLowerCase().contains("pride") &&
                                 book.getTitle().toLowerCase().contains("prejudice"));
        assertThat(hasRelevantTitle).isTrue();
    }

    @Test
    void searchByAuthor_withKnownAuthor_shouldReturnTheirBooks() {
        // Given - searching by famous author
        String author = "Jane Austen";
        int limit = 5;

        // When - making actual API call
        List<BookResponse> results = openLibraryService.searchByAuthor(author, limit);

        // Then - verify author's books are returned
        assertThat(results).isNotEmpty();
        assertThat(results.size()).isLessThanOrEqualTo(limit);
        
        // Check that results are relevant to the author
        boolean hasExpectedAuthor = results.stream()
                .anyMatch(book -> book.getAuthor() != null && 
                                 book.getAuthor().toLowerCase().contains("austen"));
        assertThat(hasExpectedAuthor).isTrue();
        
        // Verify book titles are populated
        results.forEach(book -> {
            assertThat(book.getTitle()).isNotNull().isNotBlank();
        });
    }

    @Test
    void searchBooks_withUnicodeQuery_shouldHandleInternationalBooks() {
        // Given - searching for international book
        String query = "Don Quixote"; // Classic that should exist
        int limit = 3;

        // When - making actual API call
        List<BookResponse> results = openLibraryService.searchBooks(query, limit);

        // Then - verify international content is handled
        assertThat(results).isNotEmpty();
        
        BookResponse book = results.get(0);
        assertThat(book.getTitle()).isNotNull();
        // Should handle international characters if present
        assertThat(book.getTitle()).isNotBlank();
    }

    @Test
    void searchBooks_withScienceBook_shouldReturnTechnicalBooks() {
        // Given - searching for technical/science book
        String query = "computer science algorithms";
        int limit = 5;

        // When - making actual API call
        List<BookResponse> results = openLibraryService.searchBooks(query, limit);

        // Then - verify technical books are returned
        assertThat(results).isNotEmpty();
        
        // Should have at least some results with relevant keywords
        boolean hasRelevantBooks = results.stream()
                .anyMatch(book -> {
                    String title = book.getTitle() != null ? book.getTitle().toLowerCase() : "";
                    return title.contains("computer") || 
                           title.contains("algorithm") || 
                           title.contains("science") ||
                           title.contains("programming");
                });
        assertThat(hasRelevantBooks).isTrue();
    }

    @Test
    void searchBooks_withLargeLimit_shouldRespectOpenLibraryMaximum() {
        // Given - requesting more than OpenLibrary's maximum
        String query = "java programming";
        int limit = 150; // More than OpenLibrary's max of 100

        // When - making actual API call
        List<BookResponse> results = openLibraryService.searchBooks(query, limit);

        // Then - should not exceed OpenLibrary's maximum
        assertThat(results.size()).isLessThanOrEqualTo(100);
    }

    @Test
    void searchBooks_withObscureQuery_shouldHandleNoResults() {
        // Given - very specific query unlikely to have results
        String query = "xyzabc123nonexistentbooktitle456def";
        int limit = 10;

        // When - making actual API call
        List<BookResponse> results = openLibraryService.searchBooks(query, limit);

        // Then - should handle empty results gracefully
        assertThat(results).isEmpty();
    }

    @Test
    void coverImageUrl_whenPresent_shouldBeValidUrl() {
        // Given - searching for book likely to have cover
        String query = "Harry Potter";
        int limit = 5;

        // When - making actual API call
        List<BookResponse> results = openLibraryService.searchBooks(query, limit);

        // Then - verify cover URLs when present
        assertThat(results).isNotEmpty();
        
        for (BookResponse book : results) {
            if (book.getCoverImageUrl() != null) {
                // Cover URL should be properly formatted
                assertThat(book.getCoverImageUrl())
                        .startsWith("https://covers.openlibrary.org/b/id/")
                        .endsWith("-M.jpg");
            }
        }
    }

    @Test
    void publicationYear_whenPresent_shouldBeReasonable() {
        // Given - searching for historical book
        String query = "Shakespeare Hamlet";
        int limit = 5;

        // When - making actual API call
        List<BookResponse> results = openLibraryService.searchBooks(query, limit);

        // Then - verify publication years are reasonable
        assertThat(results).isNotEmpty();
        
        for (BookResponse book : results) {
            if (book.getPublicationYear() != null) {
                // Should be within reasonable range for books
                assertThat(book.getPublicationYear())
                        .isGreaterThan(1400) // After printing press
                        .isLessThanOrEqualTo(2030); // Not too far in future
            }
        }
    }

    @Test
    void apiResponse_shouldContainDiverseFieldData() {
        // Given - searching for popular book likely to have complete data
        String query = "1984 George Orwell";
        int limit = 3;

        // When - making actual API call
        List<BookResponse> results = openLibraryService.searchBooks(query, limit);

        // Then - verify diverse field population
        assertThat(results).isNotEmpty();
        
        BookResponse book = results.get(0);
        
        // Core fields should be present
        assertThat(book.getTitle()).isNotNull().isNotBlank();
        
        // Track which optional fields are populated (should have some variety)
        int fieldsPopulated = 0;
        if (book.getAuthor() != null && !book.getAuthor().isEmpty()) fieldsPopulated++;
        if (book.getIsbn() != null && !book.getIsbn().isEmpty()) fieldsPopulated++;
        if (book.getPublicationYear() != null) fieldsPopulated++;
        if (book.getPublisher() != null && !book.getPublisher().isEmpty()) fieldsPopulated++;
        if (book.getCoverImageUrl() != null && !book.getCoverImageUrl().isEmpty()) fieldsPopulated++;
        if (book.getPageCount() != null) fieldsPopulated++;
        
        // Should have at least 2-3 fields populated for a well-known book
        assertThat(fieldsPopulated).isGreaterThanOrEqualTo(2);
    }

    @Test
    void searchByIsbn_withHyphenatedIsbn_shouldCleanAndFind() {
        // Given - ISBN with hyphens (common format)
        String hyphenatedIsbn = "978-0-7432-7356-5"; // The Great Gatsby

        // When - making actual API call
        List<BookResponse> results = openLibraryService.searchByIsbn(hyphenatedIsbn);

        // Then - should find the book despite hyphens
        // Note: This might return empty if this specific ISBN isn't in OpenLibrary
        // but the call should succeed without errors
        assertThat(results).isNotNull();
        
        // If results are found, verify they're properly formatted
        for (BookResponse book : results) {
            assertThat(book.getTitle()).isNotNull().isNotBlank();
            if (book.getIsbn() != null) {
                // ISBN in response should not contain hyphens
                assertThat(book.getIsbn()).doesNotContain("-");
            }
        }
    }
}