package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.book.BookResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service that orchestrates multiple external book APIs with sophisticated fallback strategies.
 * Provides unified search capabilities across OpenLibrary and Google Books APIs.
 * 
 * Features:
 * - Multi-API search with parallel execution
 * - Intelligent result merging and deduplication
 * - Fallback strategies when APIs fail
 * - Performance optimization and caching
 * - Error handling and service degradation
 */
@Service
public class ExternalBookSearchService {

    private static final Logger logger = LoggerFactory.getLogger(ExternalBookSearchService.class);
    
    private static final int DEFAULT_EXTERNAL_SEARCH_TIMEOUT_SECONDS = 10;
    private static final int DEFAULT_MAX_RESULTS_PER_API = 20;

    @Autowired
    private OpenLibraryService openLibraryService;

    @Autowired
    private GoogleBooksService googleBooksService;

    /**
     * Search for books across all external APIs with fallback strategies.
     * 
     * @param query Search query
     * @param limit Maximum number of results to return
     * @return List of BookResponse objects from external APIs
     */
    public List<BookResponse> searchBooks(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        logger.info("Starting external API search for query: '{}', limit: {}", query, limit);

        List<BookResponse> allResults = new ArrayList<>();
        Set<String> seenIsbns = new HashSet<>();
        Set<String> seenTitles = new HashSet<>();

        try {
            // Execute searches in parallel for better performance
            CompletableFuture<List<BookResponse>> openLibraryFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return openLibraryService.searchBooks(query, Math.min(limit, DEFAULT_MAX_RESULTS_PER_API));
                } catch (Exception e) {
                    logger.warn("OpenLibrary search failed for query '{}': {}", query, e.getMessage());
                    return Collections.emptyList();
                }
            });

            CompletableFuture<List<BookResponse>> googleBooksFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return googleBooksService.searchBooks(query, Math.min(limit, DEFAULT_MAX_RESULTS_PER_API));
                } catch (Exception e) {
                    logger.warn("Google Books search failed for query '{}': {}", query, e.getMessage());
                    return Collections.emptyList();
                }
            });

            // Wait for both searches to complete with timeout
            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(openLibraryFuture, googleBooksFuture);
            combinedFuture.get(DEFAULT_EXTERNAL_SEARCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // Collect and merge results
            List<BookResponse> openLibraryResults = openLibraryFuture.get();
            List<BookResponse> googleBooksResults = googleBooksFuture.get();

            logger.debug("External API results - OpenLibrary: {}, Google Books: {}", 
                        openLibraryResults.size(), googleBooksResults.size());

            // Merge results with deduplication (prioritize OpenLibrary for consistency)
            allResults.addAll(deduplicateAndMerge(openLibraryResults, googleBooksResults, seenIsbns, seenTitles));

            // Limit results to requested amount
            if (allResults.size() > limit) {
                allResults = allResults.subList(0, limit);
            }

            logger.info("External API search completed for query '{}': {} results", query, allResults.size());
            return allResults;

        } catch (Exception e) {
            logger.error("External API search failed for query '{}': {}", query, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Search for books by ISBN across all external APIs.
     * 
     * @param isbn ISBN to search for
     * @return List of BookResponse objects (usually 0 or 1)
     */
    public List<BookResponse> searchByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return Collections.emptyList();
        }

        logger.info("Starting external API ISBN search for: {}", isbn);

        try {
            // Try OpenLibrary first (often has better ISBN data)
            List<BookResponse> openLibraryResults = openLibraryService.searchByIsbn(isbn);
            if (!openLibraryResults.isEmpty()) {
                logger.debug("Found book by ISBN in OpenLibrary: {}", isbn);
                return openLibraryResults;
            }

            // Fallback to Google Books
            List<BookResponse> googleBooksResults = googleBooksService.searchByIsbn(isbn);
            if (!googleBooksResults.isEmpty()) {
                logger.debug("Found book by ISBN in Google Books: {}", isbn);
                return googleBooksResults;
            }

            logger.debug("No book found by ISBN in any external API: {}", isbn);
            return Collections.emptyList();

        } catch (Exception e) {
            logger.error("External API ISBN search failed for '{}': {}", isbn, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Search for books by title across all external APIs.
     * 
     * @param title Title to search for
     * @param limit Maximum number of results to return
     * @return List of BookResponse objects
     */
    public List<BookResponse> searchByTitle(String title, int limit) {
        if (title == null || title.trim().isEmpty()) {
            return Collections.emptyList();
        }

        logger.info("Starting external API title search for: '{}', limit: {}", title, limit);

        List<BookResponse> allResults = new ArrayList<>();
        Set<String> seenIsbns = new HashSet<>();
        Set<String> seenTitles = new HashSet<>();

        try {
            // Execute searches in parallel
            CompletableFuture<List<BookResponse>> openLibraryFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return openLibraryService.searchByTitle(title, Math.min(limit, DEFAULT_MAX_RESULTS_PER_API));
                } catch (Exception e) {
                    logger.warn("OpenLibrary title search failed for '{}': {}", title, e.getMessage());
                    return Collections.emptyList();
                }
            });

            CompletableFuture<List<BookResponse>> googleBooksFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return googleBooksService.searchByTitle(title, Math.min(limit, DEFAULT_MAX_RESULTS_PER_API));
                } catch (Exception e) {
                    logger.warn("Google Books title search failed for '{}': {}", title, e.getMessage());
                    return Collections.emptyList();
                }
            });

            // Wait for completion with timeout
            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(openLibraryFuture, googleBooksFuture);
            combinedFuture.get(DEFAULT_EXTERNAL_SEARCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // Merge results
            List<BookResponse> openLibraryResults = openLibraryFuture.get();
            List<BookResponse> googleBooksResults = googleBooksFuture.get();

            allResults.addAll(deduplicateAndMerge(openLibraryResults, googleBooksResults, seenIsbns, seenTitles));

            if (allResults.size() > limit) {
                allResults = allResults.subList(0, limit);
            }

            logger.info("External API title search completed for '{}': {} results", title, allResults.size());
            return allResults;

        } catch (Exception e) {
            logger.error("External API title search failed for '{}': {}", title, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Search for books by author across all external APIs.
     * 
     * @param author Author to search for
     * @param limit Maximum number of results to return
     * @return List of BookResponse objects
     */
    public List<BookResponse> searchByAuthor(String author, int limit) {
        if (author == null || author.trim().isEmpty()) {
            return Collections.emptyList();
        }

        logger.info("Starting external API author search for: '{}', limit: {}", author, limit);

        List<BookResponse> allResults = new ArrayList<>();
        Set<String> seenIsbns = new HashSet<>();
        Set<String> seenTitles = new HashSet<>();

        try {
            // Execute searches in parallel
            CompletableFuture<List<BookResponse>> openLibraryFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return openLibraryService.searchByAuthor(author, Math.min(limit, DEFAULT_MAX_RESULTS_PER_API));
                } catch (Exception e) {
                    logger.warn("OpenLibrary author search failed for '{}': {}", author, e.getMessage());
                    return Collections.emptyList();
                }
            });

            CompletableFuture<List<BookResponse>> googleBooksFuture = CompletableFuture.supplyAsync(() -> {
                try {
                    return googleBooksService.searchByAuthor(author, Math.min(limit, DEFAULT_MAX_RESULTS_PER_API));
                } catch (Exception e) {
                    logger.warn("Google Books author search failed for '{}': {}", author, e.getMessage());
                    return Collections.emptyList();
                }
            });

            // Wait for completion with timeout
            CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(openLibraryFuture, googleBooksFuture);
            combinedFuture.get(DEFAULT_EXTERNAL_SEARCH_TIMEOUT_SECONDS, TimeUnit.SECONDS);

            // Merge results
            List<BookResponse> openLibraryResults = openLibraryFuture.get();
            List<BookResponse> googleBooksResults = googleBooksFuture.get();

            allResults.addAll(deduplicateAndMerge(openLibraryResults, googleBooksResults, seenIsbns, seenTitles));

            if (allResults.size() > limit) {
                allResults = allResults.subList(0, limit);
            }

            logger.info("External API author search completed for '{}': {} results", author, allResults.size());
            return allResults;

        } catch (Exception e) {
            logger.error("External API author search failed for '{}': {}", author, e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * Get the health status of external APIs.
     * 
     * @return Map of API name to health status
     */
    public ExternalApiHealthStatus getHealthStatus() {
        ExternalApiHealthStatus status = new ExternalApiHealthStatus();

        // Test OpenLibrary
        try {
            openLibraryService.searchBooks("test", 1);
            status.setOpenLibraryHealthy(true);
            status.setOpenLibraryMessage("Service operational");
        } catch (Exception e) {
            status.setOpenLibraryHealthy(false);
            status.setOpenLibraryMessage("Service unavailable: " + e.getMessage());
        }

        // Test Google Books
        try {
            googleBooksService.searchBooks("test", 1);
            status.setGoogleBooksHealthy(true);
            status.setGoogleBooksMessage("Service operational");
        } catch (Exception e) {
            status.setGoogleBooksHealthy(false);
            status.setGoogleBooksMessage("Service unavailable: " + e.getMessage());
        }

        return status;
    }

    /**
     * Merge and deduplicate results from multiple APIs.
     * Prioritizes OpenLibrary results for consistency.
     * 
     * @param openLibraryResults Results from OpenLibrary
     * @param googleBooksResults Results from Google Books
     * @param seenIsbns Set to track seen ISBNs
     * @param seenTitles Set to track seen titles
     * @return Merged and deduplicated list
     */
    private List<BookResponse> deduplicateAndMerge(List<BookResponse> openLibraryResults, 
                                                   List<BookResponse> googleBooksResults,
                                                   Set<String> seenIsbns, 
                                                   Set<String> seenTitles) {
        List<BookResponse> mergedResults = new ArrayList<>();

        // Add OpenLibrary results first (higher priority)
        for (BookResponse book : openLibraryResults) {
            if (isUniqueBook(book, seenIsbns, seenTitles)) {
                mergedResults.add(book);
                markBookAsSeen(book, seenIsbns, seenTitles);
            }
        }

        // Add Google Books results that aren't duplicates
        for (BookResponse book : googleBooksResults) {
            if (isUniqueBook(book, seenIsbns, seenTitles)) {
                mergedResults.add(book);
                markBookAsSeen(book, seenIsbns, seenTitles);
            }
        }

        return mergedResults;
    }

    /**
     * Check if a book is unique based on ISBN and title.
     * 
     * @param book Book to check
     * @param seenIsbns Set of seen ISBNs
     * @param seenTitles Set of seen titles (normalized)
     * @return true if book is unique
     */
    private boolean isUniqueBook(BookResponse book, Set<String> seenIsbns, Set<String> seenTitles) {
        // Check ISBN uniqueness (if available)
        if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
            String normalizedIsbn = normalizeIsbn(book.getIsbn());
            if (seenIsbns.contains(normalizedIsbn)) {
                return false;
            }
        }

        // Check title uniqueness (normalized)
        if (book.getTitle() != null && !book.getTitle().trim().isEmpty()) {
            String normalizedTitle = normalizeTitle(book.getTitle());
            if (seenTitles.contains(normalizedTitle)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Mark a book as seen in the tracking sets.
     * 
     * @param book Book to mark
     * @param seenIsbns Set of seen ISBNs
     * @param seenTitles Set of seen titles
     */
    private void markBookAsSeen(BookResponse book, Set<String> seenIsbns, Set<String> seenTitles) {
        if (book.getIsbn() != null && !book.getIsbn().trim().isEmpty()) {
            seenIsbns.add(normalizeIsbn(book.getIsbn()));
        }
        if (book.getTitle() != null && !book.getTitle().trim().isEmpty()) {
            seenTitles.add(normalizeTitle(book.getTitle()));
        }
    }

    /**
     * Normalize ISBN for comparison (remove hyphens, spaces, etc.).
     * 
     * @param isbn Raw ISBN
     * @return Normalized ISBN
     */
    private String normalizeIsbn(String isbn) {
        return isbn.replaceAll("[^0-9X]", "").toUpperCase();
    }

    /**
     * Normalize title for comparison (lowercase, remove punctuation, trim).
     * 
     * @param title Raw title
     * @return Normalized title
     */
    private String normalizeTitle(String title) {
        return title.toLowerCase()
                   .replaceAll("[^a-z0-9\\s]", "")
                   .replaceAll("\\s+", " ")
                   .trim();
    }

    /**
     * DTO for external API health status.
     */
    public static class ExternalApiHealthStatus {
        private boolean openLibraryHealthy;
        private String openLibraryMessage;
        private boolean googleBooksHealthy;
        private String googleBooksMessage;

        // Getters and setters
        public boolean isOpenLibraryHealthy() {
            return openLibraryHealthy;
        }

        public void setOpenLibraryHealthy(boolean openLibraryHealthy) {
            this.openLibraryHealthy = openLibraryHealthy;
        }

        public String getOpenLibraryMessage() {
            return openLibraryMessage;
        }

        public void setOpenLibraryMessage(String openLibraryMessage) {
            this.openLibraryMessage = openLibraryMessage;
        }

        public boolean isGoogleBooksHealthy() {
            return googleBooksHealthy;
        }

        public void setGoogleBooksHealthy(boolean googleBooksHealthy) {
            this.googleBooksHealthy = googleBooksHealthy;
        }

        public String getGoogleBooksMessage() {
            return googleBooksMessage;
        }

        public void setGoogleBooksMessage(String googleBooksMessage) {
            this.googleBooksMessage = googleBooksMessage;
        }

        public boolean isAnyServiceHealthy() {
            return openLibraryHealthy || googleBooksHealthy;
        }

        public boolean areAllServicesHealthy() {
            return openLibraryHealthy && googleBooksHealthy;
        }
    }
}