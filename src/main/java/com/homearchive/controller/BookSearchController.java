package com.homearchive.controller;

import com.homearchive.dto.SearchRequest;
import com.homearchive.dto.SearchResponse;
import com.homearchive.dto.SortBy;
import com.homearchive.dto.SortOrder;
import com.homearchive.entity.PhysicalLocation;
import com.homearchive.service.BookSearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Book Search", description = "Search and discovery operations for books in the collection")
public class BookSearchController {
    
    private static final Logger logger = LoggerFactory.getLogger(BookSearchController.class);
    
    private final BookSearchService bookSearchService;
    
    @Autowired
    public BookSearchController(BookSearchService bookSearchService) {
        this.bookSearchService = bookSearchService;
    }
    
    /**
     * Executes a search operation with common error handling.
     * This utility method consolidates duplicate error handling patterns across all search endpoints.
     * 
     * @param searchOperation The search operation to execute
     * @param operationName The name of the operation for logging purposes
     * @return ResponseEntity with the search result or error response
     */
    private ResponseEntity<SearchResponse> executeSearchWithErrorHandling(
            SearchOperation searchOperation, 
            String operationName) {
        
        try {
            SearchResponse response = searchOperation.execute();
            logger.info("{} completed - found {} results", operationName, response.getResultCount());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error performing {}", operationName.toLowerCase(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SearchResponse());
        }
    }
    
    /**
     * Functional interface for search operations.
     * Allows different search methods to be passed as lambda expressions.
     */
    @FunctionalInterface
    private interface SearchOperation {
        SearchResponse execute() throws Exception;
    }
    
    /**
     * Search books endpoint with multi-word query support and advanced filtering.
     * Supports fuzzy matching, relevance ranking, and multiple sorting options.
     */
    @Operation(
        summary = "Search books with multi-word queries",
        description = """
            Perform comprehensive book search across titles, authors, and genres. 
            Supports multi-word queries with intelligent matching and relevance-based ranking.
            
            **Search Features:**
            - Multi-word queries (e.g., "science fiction asimov")
            - Fuzzy matching for typo tolerance
            - Relevance-based ranking
            - Partial title/author matching
            - Genre filtering
            
            **Performance:**
            - Cached results for common queries
            - Response time: <200ms typical
            - Supports collections up to 10,000 books
            """,
        tags = {"Book Search"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Search completed successfully",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = SearchResponse.class),
                examples = @ExampleObject(
                    name = "Successful search",
                    summary = "Example search response with multiple results",
                    value = """
                        {
                          "query": "science fiction",
                          "resultCount": 3,
                          "totalResults": 3,
                          "results": [
                            {
                              "id": 1,
                              "title": "Foundation",
                              "author": "Isaac Asimov",
                              "genre": "Science Fiction",
                              "publicationYear": 1951,
                              "physicalLocation": "Living Room Shelf 2",
                              "match": true
                            },
                            {
                              "id": 2,
                              "title": "Dune",
                              "author": "Frank Herbert",
                              "genre": "Science Fiction",
                              "publicationYear": 1965,
                              "physicalLocation": "Study Bookcase A",
                              "match": true
                            }
                          ]
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Invalid search parameters",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Validation error",
                    value = """
                        {
                          "errorId": "ERR-1234567890123",
                          "message": "Search query must not exceed 100 characters",
                          "timestamp": "2024-01-15T10:30:00Z"
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Internal server error during search",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Server error",
                    value = """
                        {
                          "errorId": "ERR-1234567890124",
                          "message": "An unexpected error occurred while processing the search",
                          "timestamp": "2024-01-15T10:30:00Z"
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> searchBooks(
            @Parameter(
                name = "query",
                description = "Search query string. Supports multi-word searches across titles, authors, and genres. " +
                             "Leave empty to get all books.",
                example = "science fiction asimov",
                schema = @Schema(maxLength = 100)
            )
            @RequestParam(value = "query", required = false) 
            @Size(max = 100, message = "Search query must not exceed 100 characters") 
            String query,
            
            @Parameter(
                name = "sortBy",
                description = "Field to sort results by. RELEVANCE provides best matching results first.",
                example = "RELEVANCE"
            )
            @RequestParam(value = "sortBy", required = false, defaultValue = "RELEVANCE") 
            SortBy sortBy,
            
            @Parameter(
                name = "sortOrder",
                description = "Sort order for results",
                example = "DESC"
            )
            @RequestParam(value = "sortOrder", required = false, defaultValue = "DESC") 
            SortOrder sortOrder,
            
            @Parameter(
                name = "limit",
                description = "Maximum number of results to return",
                example = "20",
                schema = @Schema(minimum = "1", maximum = "50")
            )
            @RequestParam(value = "limit", required = false, defaultValue = "50") 
            @Min(value = 1, message = "Limit must be at least 1")
            @Max(value = 50, message = "Limit must not exceed 50")
            Integer limit,
            
            @Parameter(
                name = "physicalLocation",
                description = "Filter books by physical location. Use enum values like LIVING_ROOM, BEDROOM, etc.",
                example = "LIVING_ROOM"
            )
            @RequestParam(value = "physicalLocation", required = false) 
            PhysicalLocation physicalLocation) {
        
        logger.info("Search request - query: '{}', sortBy: {}, sortOrder: {}, limit: {}, physicalLocation: {}", 
                   query, sortBy, sortOrder, limit, physicalLocation);
        
        return executeSearchWithErrorHandling(() -> {
            // Create search request
            SearchRequest request = new SearchRequest(query, sortBy, sortOrder, limit);
            request.setPhysicalLocation(physicalLocation);
            
            // Perform search
            SearchResponse response = bookSearchService.searchBooks(request);
            
            logger.info("Search completed - found {} results out of {} total", 
                       response.getResultCount(), response.getTotalResults());
            
            return response;
        }, "Search");
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
        
        return executeSearchWithErrorHandling(
                () -> bookSearchService.searchByTitle(title, limit), 
                "Title search");
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
        
        return executeSearchWithErrorHandling(
                () -> bookSearchService.searchByAuthor(author, limit), 
                "Author search");
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
        
        return executeSearchWithErrorHandling(
                () -> bookSearchService.searchByGenre(genre, limit), 
                "Genre search");
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
        
        return executeSearchWithErrorHandling(() -> {
            SearchResponse response = bookSearchService.getAllBooks(limit, sortBy);
            
            logger.info("Get all books completed - returned {} results out of {} total", 
                       response.getResultCount(), response.getTotalResults());
            
            return response;
        }, "Get all books");
    }
    
    /**
     * Health check endpoint for the book search functionality.
     */
    @GetMapping("/search/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Book search service is healthy");
    }
}