package com.thehomearchive.library.service;

import com.thehomearchive.library.config.OpenLibraryConfig;
import com.thehomearchive.library.dto.book.BookResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Manual integration tests for OpenLibraryService.
 * 
 * These tests are @Disabled by default to avoid external API calls in CI/CD.
 * To run manually:
 * 1. Remove @Disabled annotation from the test method
 * 2. Run the specific test: ./gradlew test --tests OpenLibraryServiceManualTest
 * 
 * These tests verify that the OpenLibraryService works correctly with the real API.
 */
class OpenLibraryServiceManualTest {

    private OpenLibraryService openLibraryService;

    @BeforeEach
    void setUp() {
        // Set up real configuration
        OpenLibraryConfig config = new OpenLibraryConfig();
        config.setBaseUrl("https://openlibrary.org");
        config.setTimeout(10000);
        
        // Create real RestTemplate
        RestTemplateBuilder builder = new RestTemplateBuilder();
        RestTemplate restTemplate = builder
                .rootUri(config.getBaseUrl())
                .connectTimeout(Duration.ofMillis(config.getTimeout()))
                .readTimeout(Duration.ofMillis(config.getTimeout() * 2))
                .build();
        
        openLibraryService = new OpenLibraryService(config, restTemplate);
    }

    @Test
    @Disabled("Enable this test manually to verify real API integration")
    void manualTest_searchBooks_shouldContactRealAPI() {
        // This test demonstrates real API integration
        // Remove @Disabled to run manually
        
        System.out.println("Testing OpenLibrary API integration...");
        
        // Test 1: General search
        List<BookResponse> results = openLibraryService.searchBooks("The Great Gatsby", 3);
        System.out.println("General search results: " + results.size());
        if (!results.isEmpty()) {
            BookResponse book = results.get(0);
            System.out.println("First book: " + book.getTitle() + " by " + book.getAuthor());
        }
        assertThat(results).isNotEmpty();
        
        // Test 2: ISBN search
        results = openLibraryService.searchByIsbn("9780743273565");
        System.out.println("ISBN search results: " + results.size());
        if (!results.isEmpty()) {
            BookResponse book = results.get(0);
            System.out.println("ISBN book: " + book.getTitle() + " (ISBN: " + book.getIsbn() + ")");
        }
        
        // Test 3: Title search
        results = openLibraryService.searchByTitle("1984", 2);
        System.out.println("Title search results: " + results.size());
        
        // Test 4: Author search
        results = openLibraryService.searchByAuthor("Jane Austen", 3);
        System.out.println("Author search results: " + results.size());
        if (!results.isEmpty()) {
            results.forEach(book -> 
                System.out.println("  - " + book.getTitle() + " by " + book.getAuthor())
            );
        }
        
        System.out.println("All tests completed successfully!");
    }

    @Test
    @Disabled("Enable to test API error handling manually")
    void manualTest_errorHandling_shouldHandleGracefully() {
        // Test error scenarios
        System.out.println("Testing error handling...");
        
        // Empty query
        List<BookResponse> results = openLibraryService.searchBooks("", 5);
        assertThat(results).isEmpty();
        System.out.println("Empty query handled correctly");
        
        // Null query
        results = openLibraryService.searchBooks(null, 5);
        assertThat(results).isEmpty();
        System.out.println("Null query handled correctly");
        
        // Very obscure query (likely no results)
        results = openLibraryService.searchBooks("xyznonexistentbook123", 5);
        System.out.println("Obscure query results: " + results.size());
        
        System.out.println("Error handling tests completed!");
    }
}