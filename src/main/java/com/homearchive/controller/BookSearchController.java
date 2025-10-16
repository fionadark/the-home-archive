package com.homearchive.controller;

import com.homearchive.dto.SearchRequest;
import com.homearchive.dto.SearchResponse;
import com.homearchive.dto.SortBy;
import com.homearchive.dto.SortOrder;
import com.homearchive.service.BookSearchService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for book search operations.
 * Provides endpoints for searching books with various criteria.
 */
@RestController
@RequestMapping("/api/books")
@Validated
@CrossOrigin(origins = "*") // Temporary - will be configured properly in security config
public class BookSearchController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookSearchController.class);
    
    private final BookSearchService bookSearchService;
    
    @Autowired
    public BookSearchController(BookSearchService bookSearchService) {
        this.bookSearchService = bookSearchService;
    }
    
    /**
     * Search books endpoint.
     * GET /api/books/search?query=...&sortBy=...&sortOrder=...&limit=...
     */
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> searchBooks(
            @RequestParam(value = "query", required = false) 
            @Size(max = 100, message = "Search query must not exceed 100 characters") 
            String query,
            
            @RequestParam(value = "sortBy", required = false, defaultValue = "RELEVANCE") 
            SortBy sortBy,
            
            @RequestParam(value = "sortOrder", required = false, defaultValue = "DESC") 
            SortOrder sortOrder,
            
            @RequestParam(value = "limit", required = false, defaultValue = "50") 
            @Min(value = 1, message = "Limit must be at least 1")
            @Max(value = 50, message = "Limit must not exceed 50")
            Integer limit) {
        
        logger.info("Search request - query: '{}', sortBy: {}, sortOrder: {}, limit: {}", 
                   query, sortBy, sortOrder, limit);
        
        try {
            // Create search request
            SearchRequest request = new SearchRequest(query, sortBy, sortOrder, limit);
            
            // Perform search
            SearchResponse response = bookSearchService.searchBooks(request);
            
            logger.info("Search completed - found {} results out of {} total", 
                       response.getResultCount(), response.getTotalResults());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error performing search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SearchResponse());
        }
    }
    
    /**
     * Search books by title only.
     * GET /api/books/search/title?q=...&limit=...
     */
    @GetMapping("/search/title")
    public ResponseEntity<SearchResponse> searchByTitle(
            @RequestParam(value = "q") 
            @Size(max = 100, message = "Title query must not exceed 100 characters") 
            String title,
            
            @RequestParam(value = "limit", required = false, defaultValue = "50") 
            @Min(value = 1, message = "Limit must be at least 1")
            @Max(value = 50, message = "Limit must not exceed 50")
            Integer limit) {
        
        logger.info("Title search request - title: '{}', limit: {}", title, limit);
        
        try {
            SearchResponse response = bookSearchService.searchByTitle(title, limit);
            
            logger.info("Title search completed - found {} results", response.getResultCount());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error performing title search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SearchResponse());
        }
    }
    
    /**
     * Search books by author only.
     * GET /api/books/search/author?q=...&limit=...
     */
    @GetMapping("/search/author")
    public ResponseEntity<SearchResponse> searchByAuthor(
            @RequestParam(value = "q") 
            @Size(max = 100, message = "Author query must not exceed 100 characters") 
            String author,
            
            @RequestParam(value = "limit", required = false, defaultValue = "50") 
            @Min(value = 1, message = "Limit must be at least 1")
            @Max(value = 50, message = "Limit must not exceed 50")
            Integer limit) {
        
        logger.info("Author search request - author: '{}', limit: {}", author, limit);
        
        try {
            SearchResponse response = bookSearchService.searchByAuthor(author, limit);
            
            logger.info("Author search completed - found {} results", response.getResultCount());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error performing author search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SearchResponse());
        }
    }
    
    /**
     * Search books by genre.
     * GET /api/books/search/genre?q=...&limit=...
     */
    @GetMapping("/search/genre")
    public ResponseEntity<SearchResponse> searchByGenre(
            @RequestParam(value = "q") 
            @Size(max = 100, message = "Genre query must not exceed 100 characters") 
            String genre,
            
            @RequestParam(value = "limit", required = false, defaultValue = "50") 
            @Min(value = 1, message = "Limit must be at least 1")
            @Max(value = 50, message = "Limit must not exceed 50")
            Integer limit) {
        
        logger.info("Genre search request - genre: '{}', limit: {}", genre, limit);
        
        try {
            SearchResponse response = bookSearchService.searchByGenre(genre, limit);
            
            logger.info("Genre search completed - found {} results", response.getResultCount());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error performing genre search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SearchResponse());
        }
    }
    
    /**
     * Get all books with optional sorting.
     * GET /api/books?sortBy=...&limit=...
     */
    @GetMapping
    public ResponseEntity<SearchResponse> getAllBooks(
            @RequestParam(value = "sortBy", required = false, defaultValue = "TITLE") 
            SortBy sortBy,
            
            @RequestParam(value = "limit", required = false, defaultValue = "50") 
            @Min(value = 1, message = "Limit must be at least 1")
            @Max(value = 50, message = "Limit must not exceed 50")
            Integer limit) {
        
        logger.info("Get all books request - sortBy: {}, limit: {}", sortBy, limit);
        
        try {
            SearchResponse response = bookSearchService.getAllBooks(limit, sortBy);
            
            logger.info("Get all books completed - returned {} results out of {} total", 
                       response.getResultCount(), response.getTotalResults());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting all books", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SearchResponse());
        }
    }
    
    /**
     * Health check endpoint for the book search functionality.
     */
    @GetMapping("/search/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Book search service is healthy");
    }
}