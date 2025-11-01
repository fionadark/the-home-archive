package com.thehomearchive.library.service;

import com.thehomearchive.library.config.OpenLibraryConfig;
import com.thehomearchive.library.dto.book.BookResponse;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Service for integrating with OpenLibrary API.
 * Provides search capabilities and book metadata enrichment using the OpenLibrary API.
 * 
 * OpenLibrary API Documentation: https://openlibrary.org/dev/docs/api/search
 * All available fields: https://github.com/internetarchive/openlibrary/blob/master/openlibrary/plugins/worksearch/schemes/works.py
 */
@Service
public class OpenLibraryService {

    private static final Logger logger = LoggerFactory.getLogger(OpenLibraryService.class);

    private final OpenLibraryConfig config;
    private final RestTemplate restTemplate;

    public OpenLibraryService(OpenLibraryConfig config, 
                             @Qualifier("openLibraryRestTemplate") RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    /**
     * Search for books using OpenLibrary API.
     * 
     * @param query Search query (title, author, or general search)
     * @param limit Maximum number of results to return
     * @return List of BookResponse objects mapped from OpenLibrary data
     */
    @CircuitBreaker(name = "open-library", fallbackMethod = "searchBooksFallback")
    @Retry(name = "external-api")
    public List<BookResponse> searchBooks(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            logger.warn("Empty search query provided to OpenLibrary search");
            return Collections.emptyList();
        }

        try {
            logger.debug("Searching OpenLibrary for query: '{}' with limit: {}", query, limit);
            
            URI searchUri = buildSearchUri(query, limit);
            logger.debug("OpenLibrary search URI: {}", searchUri);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(searchUri, Map.class);
            
            if (response == null) {
                logger.warn("Received null response from OpenLibrary API");
                return Collections.emptyList();
            }

            return mapResponseToBooks(response);
            
        } catch (Exception e) {
            logger.error("Error searching OpenLibrary API for query '{}': {}", query, e.getMessage(), e);
            throw new RuntimeException("Failed to search OpenLibrary", e);
        }
    }

    /**
     * Search for books by ISBN using OpenLibrary API.
     * 
     * @param isbn ISBN to search for
     * @return List of BookResponse objects (typically one result)
     */
    @CircuitBreaker(name = "open-library", fallbackMethod = "searchByIsbnFallback")
    @Retry(name = "external-api")
    public List<BookResponse> searchByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            logger.warn("Empty ISBN provided to OpenLibrary ISBN search");
            return Collections.emptyList();
        }

        try {
            logger.debug("Searching OpenLibrary by ISBN: {}", isbn);
            
            // Clean ISBN (remove hyphens and spaces)
            String cleanIsbn = isbn.replaceAll("[\\s\\-]", "");
            
            URI searchUri = buildIsbnSearchUri(cleanIsbn);
            logger.debug("OpenLibrary ISBN search URI: {}", searchUri);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(searchUri, Map.class);
            
            if (response == null) {
                logger.warn("Received null response from OpenLibrary API for ISBN: {}", isbn);
                return Collections.emptyList();
            }

            return mapResponseToBooks(response);
            
        } catch (Exception e) {
            logger.error("Error searching OpenLibrary API by ISBN '{}': {}", isbn, e.getMessage(), e);
            throw new RuntimeException("Failed to search OpenLibrary by ISBN", e);
        }
    }

    /**
     * Search for books by title using OpenLibrary API.
     * 
     * @param title Book title to search for
     * @param limit Maximum number of results to return
     * @return List of BookResponse objects
     */
    @CircuitBreaker(name = "open-library", fallbackMethod = "searchByTitleFallback")
    @Retry(name = "external-api")
    public List<BookResponse> searchByTitle(String title, int limit) {
        if (title == null || title.trim().isEmpty()) {
            logger.warn("Empty title provided to OpenLibrary title search");
            return Collections.emptyList();
        }

        try {
            logger.debug("Searching OpenLibrary by title: '{}' with limit: {}", title, limit);
            
            URI searchUri = buildTitleSearchUri(title, limit);
            logger.debug("OpenLibrary title search URI: {}", searchUri);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(searchUri, Map.class);
            
            if (response == null) {
                logger.warn("Received null response from OpenLibrary API for title: {}", title);
                return Collections.emptyList();
            }

            return mapResponseToBooks(response);
            
        } catch (Exception e) {
            logger.error("Error searching OpenLibrary API by title '{}': {}", title, e.getMessage(), e);
            throw new RuntimeException("Failed to search OpenLibrary by title", e);
        }
    }

    /**
     * Search for books by author using OpenLibrary API.
     * 
     * @param author Author name to search for
     * @param limit Maximum number of results to return
     * @return List of BookResponse objects
     */
    @CircuitBreaker(name = "open-library", fallbackMethod = "searchByAuthorFallback")
    @Retry(name = "external-api")
    public List<BookResponse> searchByAuthor(String author, int limit) {
        if (author == null || author.trim().isEmpty()) {
            logger.warn("Empty author provided to OpenLibrary author search");
            return Collections.emptyList();
        }

        try {
            logger.debug("Searching OpenLibrary by author: '{}' with limit: {}", author, limit);
            
            URI searchUri = buildAuthorSearchUri(author, limit);
            logger.debug("OpenLibrary author URI: {}", searchUri);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(searchUri, Map.class);
            
            if (response == null) {
                logger.warn("Received null response from OpenLibrary API for author: {}", author);
                return Collections.emptyList();
            }

            return mapResponseToBooks(response);
            
        } catch (Exception e) {
            logger.error("Error searching OpenLibrary API by author '{}': {}", author, e.getMessage(), e);
            throw new RuntimeException("Failed to search OpenLibrary by author", e);
        }
    }

    /**
     * Build search URI for general query.
     */
    private URI buildSearchUri(String query, int limit) {
        return UriComponentsBuilder.fromUriString(config.getSearchUrl())
                .queryParam("q", query)
                .queryParam("fields", config.getDefaultFields())
                .queryParam("limit", Math.min(limit, 100)) // OpenLibrary max is 100
                .queryParam("offset", 0)
                .build()
                .toUri();
    }

    /**
     * Build search URI for ISBN search.
     */
    private URI buildIsbnSearchUri(String isbn) {
        return UriComponentsBuilder.fromUriString(config.getSearchUrl())
                .queryParam("isbn", isbn)
                .queryParam("fields", config.getDefaultFields())
                .queryParam("limit", 10)
                .build()
                .toUri();
    }

    /**
     * Build search URI for title search.
     */
    private URI buildTitleSearchUri(String title, int limit) {
        return UriComponentsBuilder.fromUriString(config.getSearchUrl())
                .queryParam("title", title)
                .queryParam("fields", config.getDefaultFields())
                .queryParam("limit", Math.min(limit, 100))
                .queryParam("offset", 0)
                .build()
                .toUri();
    }

    /**
     * Build search URI for author search.
     */
    private URI buildAuthorSearchUri(String author, int limit) {
        return UriComponentsBuilder.fromUriString(config.getSearchUrl())
                .queryParam("author", author)
                .queryParam("fields", config.getDefaultFields())
                .queryParam("limit", Math.min(limit, 100))
                .queryParam("offset", 0)
                .build()
                .toUri();
    }

    /**
     * Map OpenLibrary API response to list of BookResponse objects.
     * Based on the complete field schema from OpenLibrary.
     */
    @SuppressWarnings("unchecked")
    private List<BookResponse> mapResponseToBooks(Map<String, Object> response) {
        List<BookResponse> books = new ArrayList<>();
        
        try {
            Object docsObj = response.get("docs");
            if (!(docsObj instanceof List)) {
                logger.warn("OpenLibrary response 'docs' field is not a list: {}", docsObj);
                return books;
            }

            List<Map<String, Object>> docs = (List<Map<String, Object>>) docsObj;
            logger.debug("Processing {} books from OpenLibrary response", docs.size());

            for (Map<String, Object> doc : docs) {
                BookResponse book = mapDocumentToBook(doc);
                if (book != null) {
                    books.add(book);
                }
            }

            logger.debug("Successfully mapped {} books from OpenLibrary", books.size());
            
        } catch (Exception e) {
            logger.error("Error mapping OpenLibrary response to books: {}", e.getMessage(), e);
        }

        return books;
    }

    /**
     * Map a single OpenLibrary document to BookResponse.
     * Based on complete field analysis from OpenLibrary schema.
     */
    @SuppressWarnings("unchecked")
    private BookResponse mapDocumentToBook(Map<String, Object> doc) {
        try {
            BookResponse book = new BookResponse();

            // Title (required field)
            String title = (String) doc.get("title");
            if (title == null || title.trim().isEmpty()) {
                logger.debug("Skipping book with no title: {}", doc);
                return null;
            }
            book.setTitle(title.trim());

            // Author (author_name is an array)
            Object authorNamesObj = doc.get("author_name");
            if (authorNamesObj instanceof List) {
                List<String> authorNames = (List<String>) authorNamesObj;
                if (!authorNames.isEmpty()) {
                    // Take the first author, or join multiple authors
                    if (authorNames.size() == 1) {
                        book.setAuthor(authorNames.get(0));
                    } else {
                        book.setAuthor(String.join(", ", authorNames));
                    }
                }
            }

            // ISBN (isbn is an array)
            Object isbnObj = doc.get("isbn");
            if (isbnObj instanceof List) {
                List<String> isbns = (List<String>) isbnObj;
                if (!isbns.isEmpty()) {
                    // Prefer ISBN-13, fallback to first available
                    String isbn = isbns.stream()
                            .filter(i -> i.length() == 13)
                            .findFirst()
                            .orElse(isbns.get(0));
                    book.setIsbn(isbn);
                }
            }

            // Publication Year (first_publish_year)
            Object publishYearObj = doc.get("first_publish_year");
            if (publishYearObj instanceof Number) {
                book.setPublicationYear(((Number) publishYearObj).intValue());
            }

            // Publisher (publisher is an array)
            Object publisherObj = doc.get("publisher");
            if (publisherObj instanceof List) {
                List<String> publishers = (List<String>) publisherObj;
                if (!publishers.isEmpty()) {
                    book.setPublisher(publishers.get(0));
                }
            }

            // Page Count (number_of_pages_median)
            Object pageCountObj = doc.get("number_of_pages_median");
            if (pageCountObj instanceof Number) {
                book.setPageCount(((Number) pageCountObj).intValue());
            }

            // Cover Image URL (cover_i)
            Object coverIdObj = doc.get("cover_i");
            if (coverIdObj instanceof Number) {
                int coverId = ((Number) coverIdObj).intValue();
                String coverUrl = config.getCoversUrl() + "/id/" + coverId + "-M.jpg";
                book.setCoverImageUrl(coverUrl);
            }

            // Description/Summary (not typically available in search results)
            // OpenLibrary search doesn't return descriptions, would need separate work detail call

            logger.debug("Mapped book: {} by {}", book.getTitle(), book.getAuthor());
            return book;

        } catch (Exception e) {
            logger.error("Error mapping OpenLibrary document to book: {}", e.getMessage(), e);
            return null;
        }
    }

    // Fallback methods for circuit breaker

    /**
     * Fallback method for general search when OpenLibrary is unavailable.
     */
    public List<BookResponse> searchBooksFallback(String query, int limit, Exception ex) {
        logger.warn("OpenLibrary search fallback triggered for query '{}': {}", query, ex.getMessage());
        return Collections.emptyList();
    }

    /**
     * Fallback method for ISBN search when OpenLibrary is unavailable.
     */
    public List<BookResponse> searchByIsbnFallback(String isbn, Exception ex) {
        logger.warn("OpenLibrary ISBN search fallback triggered for ISBN '{}': {}", isbn, ex.getMessage());
        return Collections.emptyList();
    }

    /**
     * Fallback method for title search when OpenLibrary is unavailable.
     */
    public List<BookResponse> searchByTitleFallback(String title, int limit, Exception ex) {
        logger.warn("OpenLibrary title search fallback triggered for title '{}': {}", title, ex.getMessage());
        return Collections.emptyList();
    }

    /**
     * Fallback method for author search when OpenLibrary is unavailable.
     */
    public List<BookResponse> searchByAuthorFallback(String author, int limit, Exception ex) {
        logger.warn("OpenLibrary author search fallback triggered for author '{}': {}", author, ex.getMessage());
        return Collections.emptyList();
    }
}