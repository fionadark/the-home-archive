package com.thehomearchive.library.service;

import com.thehomearchive.library.config.GoogleBooksConfig;
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
 * Service for integrating with Google Books API.
 * Provides search capabilities and book metadata enrichment using the Google Books API.
 * 
 * Google Books API Documentation: https://developers.google.com/books/docs/v1/using
 */
@Service
public class GoogleBooksService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleBooksService.class);

    private final GoogleBooksConfig config;
    private final RestTemplate restTemplate;

    public GoogleBooksService(GoogleBooksConfig config, 
                             @Qualifier("googleBooksRestTemplate") RestTemplate restTemplate) {
        this.config = config;
        this.restTemplate = restTemplate;
    }

    /**
     * Search for books using Google Books API.
     * 
     * @param query Search query (title, author, or general search)
     * @param limit Maximum number of results to return
     * @return List of BookResponse objects mapped from Google Books data
     */
    @CircuitBreaker(name = "google-books", fallbackMethod = "searchBooksFallback")
    @Retry(name = "external-api")
    public List<BookResponse> searchBooks(String query, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            logger.debug("Searching Google Books for query: '{}', limit: {}", query, limit);

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(config.getBaseUrl() + "/volumes")
                    .queryParam("q", query.trim())
                    .queryParam("maxResults", Math.min(limit, config.getMaxResults()));

            if (config.hasApiKey()) {
                builder.queryParam("key", config.getApiKey());
            }

            URI uri = builder.build().toUri();
            logger.debug("Google Books API URL: {}", uri);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);

            return parseGoogleBooksResponse(response);

        } catch (Exception e) {
            logger.error("Error searching Google Books API for query '{}': {}", query, e.getMessage(), e);
            throw e; // Let circuit breaker handle the exception
        }
    }

    /**
     * Search for books by ISBN using Google Books API.
     * 
     * @param isbn The ISBN to search for
     * @return List of BookResponse objects (usually 0 or 1)
     */
    @CircuitBreaker(name = "google-books", fallbackMethod = "searchByIsbnFallback")
    @Retry(name = "external-api")
    public List<BookResponse> searchByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            logger.debug("Searching Google Books by ISBN: {}", isbn);

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(config.getBaseUrl() + "/volumes")
                    .queryParam("q", "isbn:" + isbn.trim())
                    .queryParam("maxResults", 1);

            if (config.hasApiKey()) {
                builder.queryParam("key", config.getApiKey());
            }

            URI uri = builder.build().toUri();
            logger.debug("Google Books API URL: {}", uri);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);

            return parseGoogleBooksResponse(response);

        } catch (Exception e) {
            logger.error("Error searching Google Books API by ISBN '{}': {}", isbn, e.getMessage(), e);
            throw e; // Let circuit breaker handle the exception
        }
    }

    /**
     * Search for books by title using Google Books API.
     * 
     * @param title The title to search for
     * @param limit Maximum number of results to return
     * @return List of BookResponse objects
     */
    @CircuitBreaker(name = "google-books", fallbackMethod = "searchByTitleFallback")
    @Retry(name = "external-api")
    public List<BookResponse> searchByTitle(String title, int limit) {
        if (title == null || title.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            logger.debug("Searching Google Books by title: '{}', limit: {}", title, limit);

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(config.getBaseUrl() + "/volumes")
                    .queryParam("q", "intitle:" + title.trim())
                    .queryParam("maxResults", Math.min(limit, config.getMaxResults()));

            if (config.hasApiKey()) {
                builder.queryParam("key", config.getApiKey());
            }

            URI uri = builder.build().toUri();
            logger.debug("Google Books API URL: {}", uri);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);

            return parseGoogleBooksResponse(response);

        } catch (Exception e) {
            logger.error("Error searching Google Books API by title '{}': {}", title, e.getMessage(), e);
            throw e; // Let circuit breaker handle the exception
        }
    }

    /**
     * Search for books by author using Google Books API.
     * 
     * @param author The author to search for
     * @param limit Maximum number of results to return
     * @return List of BookResponse objects
     */
    @CircuitBreaker(name = "google-books", fallbackMethod = "searchByAuthorFallback")
    @Retry(name = "external-api")
    public List<BookResponse> searchByAuthor(String author, int limit) {
        if (author == null || author.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            logger.debug("Searching Google Books by author: '{}', limit: {}", author, limit);

            UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(config.getBaseUrl() + "/volumes")
                    .queryParam("q", "inauthor:" + author.trim())
                    .queryParam("maxResults", Math.min(limit, config.getMaxResults()));

            if (config.hasApiKey()) {
                builder.queryParam("key", config.getApiKey());
            }

            URI uri = builder.build().toUri();
            logger.debug("Google Books API URL: {}", uri);

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restTemplate.getForObject(uri, Map.class);

            return parseGoogleBooksResponse(response);

        } catch (Exception e) {
            logger.error("Error searching Google Books API by author '{}': {}", author, e.getMessage(), e);
            throw e; // Let circuit breaker handle the exception
        }
    }

    /**
     * Parse Google Books API response and convert to BookResponse objects.
     * 
     * @param response Raw response from Google Books API
     * @return List of BookResponse objects
     */
    private List<BookResponse> parseGoogleBooksResponse(Map<String, Object> response) {
        List<BookResponse> books = new ArrayList<>();

        if (response == null) {
            logger.warn("Received null response from Google Books API");
            return books;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        
        if (items == null || items.isEmpty()) {
            logger.debug("No books found in Google Books API response");
            return books;
        }

        logger.debug("Processing {} books from Google Books API response", items.size());

        for (Map<String, Object> item : items) {
            try {
                BookResponse book = mapGoogleBooksItemToBook(item);
                if (book != null) {
                    books.add(book);
                }
            } catch (Exception e) {
                logger.warn("Error mapping Google Books item to book: {}", e.getMessage());
                // Continue processing other items
            }
        }

        logger.debug("Successfully mapped {} books from Google Books API", books.size());
        return books;
    }

    /**
     * Map a single Google Books API item to a BookResponse object.
     * 
     * @param item Google Books API item
     * @return BookResponse object or null if mapping fails
     */
    @SuppressWarnings("unchecked")
    private BookResponse mapGoogleBooksItemToBook(Map<String, Object> item) {
        try {
            Map<String, Object> volumeInfo = (Map<String, Object>) item.get("volumeInfo");
            if (volumeInfo == null) {
                return null;
            }

            BookResponse book = new BookResponse();

            // Basic information
            book.setTitle((String) volumeInfo.get("title"));
            
            // Authors
            List<String> authors = (List<String>) volumeInfo.get("authors");
            if (authors != null && !authors.isEmpty()) {
                book.setAuthor(String.join(", ", authors));
            }

            // ISBN
            List<Map<String, Object>> industryIdentifiers = (List<Map<String, Object>>) volumeInfo.get("industryIdentifiers");
            if (industryIdentifiers != null) {
                for (Map<String, Object> identifier : industryIdentifiers) {
                    String type = (String) identifier.get("type");
                    if ("ISBN_13".equals(type) || "ISBN_10".equals(type)) {
                        book.setIsbn((String) identifier.get("identifier"));
                        break;
                    }
                }
            }

            // Publication year
            String publishedDate = (String) volumeInfo.get("publishedDate");
            if (publishedDate != null && publishedDate.length() >= 4) {
                try {
                    book.setPublicationYear(Integer.parseInt(publishedDate.substring(0, 4)));
                } catch (NumberFormatException e) {
                    logger.debug("Could not parse publication year from: {}", publishedDate);
                }
            }

            // Description
            book.setDescription((String) volumeInfo.get("description"));

            // Page count
            Integer pageCount = (Integer) volumeInfo.get("pageCount");
            if (pageCount != null) {
                book.setPageCount(pageCount);
            }

            // Publisher
            book.setPublisher((String) volumeInfo.get("publisher"));

            // Categories (use first category if available)
            List<String> categories = (List<String>) volumeInfo.get("categories");
            if (categories != null && !categories.isEmpty()) {
                book.setCategoryName(categories.get(0));
            }

            // Cover URL
            Map<String, Object> imageLinks = (Map<String, Object>) volumeInfo.get("imageLinks");
            if (imageLinks != null) {
                String thumbnail = (String) imageLinks.get("thumbnail");
                if (thumbnail != null) {
                    book.setCoverImageUrl(thumbnail.replace("http://", "https://"));
                }
            }

            // Average rating
            Double averageRating = (Double) volumeInfo.get("averageRating");
            if (averageRating != null) {
                book.setAverageRating(averageRating);
            }

            // Rating count
            Integer ratingsCount = (Integer) volumeInfo.get("ratingsCount");
            if (ratingsCount != null) {
                book.setRatingCount(ratingsCount);
            }

            // Validate that we have minimum required data
            if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
                logger.debug("Skipping book with no title");
                return null;
            }

            return book;

        } catch (Exception e) {
            logger.error("Error mapping Google Books item to book: {}", e.getMessage(), e);
            return null;
        }
    }

    // Fallback methods for circuit breaker

    /**
     * Fallback method for general search when Google Books is unavailable.
     */
    public List<BookResponse> searchBooksFallback(String query, int limit, Exception ex) {
        logger.warn("Google Books search fallback triggered for query '{}': {}", query, ex.getMessage());
        return Collections.emptyList();
    }

    /**
     * Fallback method for ISBN search when Google Books is unavailable.
     */
    public List<BookResponse> searchByIsbnFallback(String isbn, Exception ex) {
        logger.warn("Google Books ISBN search fallback triggered for ISBN '{}': {}", isbn, ex.getMessage());
        return Collections.emptyList();
    }

    /**
     * Fallback method for title search when Google Books is unavailable.
     */
    public List<BookResponse> searchByTitleFallback(String title, int limit, Exception ex) {
        logger.warn("Google Books title search fallback triggered for title '{}': {}", title, ex.getMessage());
        return Collections.emptyList();
    }

    /**
     * Fallback method for author search when Google Books is unavailable.
     */
    public List<BookResponse> searchByAuthorFallback(String author, int limit, Exception ex) {
        logger.warn("Google Books author search fallback triggered for author '{}': {}", author, ex.getMessage());
        return Collections.emptyList();
    }
}