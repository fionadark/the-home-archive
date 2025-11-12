package com.thehomearchive.library.controller;

import com.thehomearchive.library.dto.book.BookDetailResponse;
import com.thehomearchive.library.dto.book.BookResponse;
import com.thehomearchive.library.dto.book.BookValidationResponse;
import com.thehomearchive.library.dto.response.ApiResponse;
import com.thehomearchive.library.dto.search.BookSearchPageResponse;
import com.thehomearchive.library.dto.search.BookSearchRequest;
import com.thehomearchive.library.dto.search.BookSearchResponse;
import com.thehomearchive.library.dto.search.EnhancedBookSearchResponse;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.service.BookSearchService;
import com.thehomearchive.library.service.BookService;
import com.thehomearchive.library.service.RatingService;
import com.thehomearchive.library.service.ExternalBookSearchService;
import com.thehomearchive.library.util.SecurityUtils;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for book search and discovery operations.
 * Handles comprehensive book search, filtering, and book detail retrieval.
 * 
 * This controller provides endpoints for:
 * - Advanced book search with filtering, sorting, and pagination
 * - Individual book detail retrieval with user-specific data
 * - Search suggestions and popular queries
 * - Search history management
 */
@RestController
@RequestMapping("/v1/search")
public class BookSearchController {

    private static final Logger logger = LoggerFactory.getLogger(BookSearchController.class);

    @Autowired
    private BookSearchService bookSearchService;

    @Autowired
    private BookService bookService;

    @Autowired
    private RatingService ratingService;

    @Autowired
    private ExternalBookSearchService externalBookSearchService;

    /**
     * Search and list books with advanced filtering and pagination.
     * 
     * @param q Search query (title, author, ISBN)
     * @param category Filter by category ID
     * @param page Page number (0-based)
     * @param size Page size (1-100)
     * @param sort Sort criteria (title, author, publicationYear, rating)
     * @param direction Sort direction (asc, desc)
     * @return Paginated book search results
     */
    @GetMapping("/books")
    public ResponseEntity<ApiResponse<BookSearchPageResponse>> searchBooks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long category,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String direction) {
        
        try {
            logger.info("Book search request: q='{}', category={}, page={}, size={}, sort={}, direction={}",
                    q, category, page, size, sort, direction);

            // Create search request
            BookSearchRequest request = new BookSearchRequest();
            request.setQ(q);
            request.setCategory(category);
            request.setPage(page);
            request.setSize(size);
            
            // Convert sort string to enum
            BookSearchRequest.SortCriteria sortCriteria = convertToSortCriteria(sort);
            request.setSort(sortCriteria);
            
            // Convert direction string to enum
            BookSearchRequest.SortDirection sortDirection = convertToSortDirection(direction);
            request.setDirection(sortDirection);

            // Get current user for personalized results (if authenticated)
            User currentUser = getCurrentUser();

            // Perform search
            BookSearchPageResponse searchResults = bookSearchService.searchBooks(request, currentUser);

            logger.info("Book search completed: {} results found", searchResults.getTotalElements());
            return ResponseEntity.ok(ApiResponse.success(searchResults));

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid search parameters: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid search parameters: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error performing book search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while searching books"));
        }
    }

    /**
     * Enhanced search with external API fallback.
     * When local search returns insufficient results, this endpoint automatically 
     * searches external APIs (OpenLibrary, Google Books) and provides unified results.
     * 
     * @param q Search query (title, author, ISBN)
     * @param category Filter by category ID  
     * @param page Page number (0-based)
     * @param size Page size (1-100)
     * @param sort Sort criteria (title, author, publicationYear, rating)
     * @param direction Sort direction (asc, desc)
     * @param includeExternal Whether to include external API search (default: true)
     * @param minLocalResults Minimum local results before triggering external search (default: 5)
     * @return Enhanced search results with external API fallback
     */
    @GetMapping("/books/enhanced")
    public ResponseEntity<ApiResponse<EnhancedBookSearchResponse>> searchBooksEnhanced(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long category,
            @RequestParam(defaultValue = "0") @Min(0) Integer page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size,
            @RequestParam(defaultValue = "title") String sort,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(defaultValue = "true") Boolean includeExternal,
            @RequestParam(defaultValue = "5") @Min(0) @Max(50) Integer minLocalResults) {
        
        try {
            logger.info("Enhanced book search request: q='{}', category={}, page={}, size={}, includeExternal={}, minLocalResults={}",
                    q, category, page, size, includeExternal, minLocalResults);

            // Create search request
            BookSearchRequest request = new BookSearchRequest();
            request.setQ(q);
            request.setCategory(category);
            request.setPage(page);
            request.setSize(size);
            
            // Convert sort string to enum
            BookSearchRequest.SortCriteria sortCriteria = convertToSortCriteria(sort);
            request.setSort(sortCriteria);
            
            // Convert direction string to enum
            BookSearchRequest.SortDirection sortDirection = convertToSortDirection(direction);
            request.setDirection(sortDirection);

            // Get current user for personalized results (if authenticated)
            User currentUser = getCurrentUser();

            // Perform enhanced search with external API fallback
            EnhancedBookSearchResponse enhancedResults = bookSearchService.searchBooksWithExternalFallback(
                request, currentUser, includeExternal, minLocalResults);

            logger.info("Enhanced book search completed: {} local results, {} external results, external search performed: {}", 
                       enhancedResults.getTotalLocalResults(), 
                       enhancedResults.getTotalExternalResults(),
                       enhancedResults.isExternalSearchPerformed());

            return ResponseEntity.ok(ApiResponse.success(enhancedResults));

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid enhanced search parameters: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error("Invalid search parameters: " + e.getMessage()));
        } catch (Exception e) {
            logger.error("Error performing enhanced book search", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while performing enhanced search"));
        }
    }

    /**
     * Get detailed information about a specific book by ID.
     * Includes user-specific data like personal rating if authenticated.
     * 
     * @param id Book ID
     * @return Detailed book information
     */
    @GetMapping("/books/{id}")
    public ResponseEntity<ApiResponse<BookDetailResponse>> getBookById(@PathVariable Long id) {
        try {
            logger.info("Retrieving book details for ID: {}", id);

            // Get basic book information
            BookResponse book = bookService.getBookById(id);
            
            // Convert to detailed response
            BookDetailResponse detailResponse = new BookDetailResponse(book);
            
            // Add timestamps if available from the book entity
            // Note: BookService.getBookById should be enhanced to include these
            
            // Get current user for user-specific data
            User currentUser = getCurrentUser();
            if (currentUser != null) {
                // Get user's rating for this book
                var userRating = ratingService.getUserRatingForBook(currentUser.getId(), id);
                if (userRating != null) {
                    detailResponse.setUserRating(userRating.getRating());
                }
            }

            logger.info("Book details retrieved successfully for ID: {}", id);
            return ResponseEntity.ok(ApiResponse.success(detailResponse));

        } catch (IllegalArgumentException e) {
            logger.warn("Book not found with ID: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Book not found"));
        } catch (Exception e) {
            logger.error("Error retrieving book details for ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while retrieving book details"));
        }
    }

    /**
     * Get search suggestions based on partial query.
     * 
     * @param q Partial search query
     * @param limit Maximum number of suggestions (default 10)
     * @return List of search suggestions
     */
    @GetMapping("/books/suggestions")
    public ResponseEntity<ApiResponse<List<String>>> getSearchSuggestions(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer limit) {
        
        try {
            logger.info("Getting search suggestions for query: '{}', limit: {}", q, limit);

            List<String> suggestions = bookSearchService.getSearchSuggestions(q, limit);

            logger.info("Found {} search suggestions for query: '{}'", suggestions.size(), q);
            return ResponseEntity.ok(ApiResponse.success(suggestions));

        } catch (Exception e) {
            logger.error("Error getting search suggestions for query: " + q, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while getting search suggestions"));
        }
    }

    /**
     * Get popular search queries.
     * 
     * @param limit Maximum number of popular queries (default 10)
     * @return List of popular search queries
     */
    @GetMapping("/books/popular")
    public ResponseEntity<ApiResponse<List<String>>> getPopularSearches(
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer limit) {
        
        try {
            logger.info("Getting popular search queries, limit: {}", limit);

            List<String> popularQueries = bookSearchService.getPopularSearchQueries(limit);

            logger.info("Found {} popular search queries", popularQueries.size());
            return ResponseEntity.ok(ApiResponse.success(popularQueries));

        } catch (Exception e) {
            logger.error("Error getting popular search queries", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while getting popular searches"));
        }
    }

    /**
     * Get similar books based on a specific book.
     * 
     * @param id Book ID to find similar books for
     * @param limit Maximum number of similar books (default 10)
     * @return List of similar books
     */
    @GetMapping("/books/{id}/similar")
    public ResponseEntity<ApiResponse<List<BookSearchResponse>>> getSimilarBooks(
            @PathVariable Long id,
            @RequestParam(defaultValue = "10") @Min(1) @Max(50) Integer limit) {
        
        try {
            logger.info("Finding similar books for book ID: {}, limit: {}", id, limit);

            List<BookSearchResponse> similarBooks = bookSearchService.findSimilarBooks(id, limit);

            logger.info("Found {} similar books for book ID: {}", similarBooks.size(), id);
            return ResponseEntity.ok(ApiResponse.success(similarBooks));

        } catch (IllegalArgumentException e) {
            logger.warn("Book not found for similar books search: {}", id);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Book not found"));
        } catch (Exception e) {
            logger.error("Error finding similar books for ID: " + id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while finding similar books"));
        }
    }

    /**
     * Get user's search history (requires authentication).
     * 
     * @param limit Maximum number of search history entries (default 20)
     * @return List of user's recent search queries
     */
    @GetMapping("/history")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<List<String>>> getUserSearchHistory(
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer limit) {
        
        try {
            Long userId = SecurityUtils.getCurrentUserId();
            // Create a minimal User object for the service call
            User user = new User();
            user.setId(userId);

            logger.info("Getting search history for user: {}, limit: {}", userId, limit);

            List<String> searchHistory = bookSearchService.getUserSearchHistory(user, limit);

            logger.info("Found {} search history entries for user: {}", searchHistory.size(), userId);
            return ResponseEntity.ok(ApiResponse.success(searchHistory));

        } catch (IllegalStateException e) {
            // Handle authentication/authorization errors
            logger.warn("Authentication error getting user search history: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Authentication required"));
        } catch (Exception e) {
            logger.error("Error getting user search history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while retrieving search history"));
        }
    }

    /**
     * Clear user's search history (requires authentication).
     * 
     * @return Success message with count of cleared entries
     */
    @DeleteMapping("/history")
    public ResponseEntity<ApiResponse<String>> clearUserSearchHistory() {
        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(ApiResponse.error("Authentication required"));
            }

            logger.info("Clearing search history for user: {}", currentUser.getId());

            Long clearedCount = bookSearchService.clearUserSearchHistory(currentUser);

            logger.info("Cleared {} search history entries for user: {}", clearedCount, currentUser.getId());
            return ResponseEntity.ok(ApiResponse.success("Cleared " + clearedCount + " search history entries"));

        } catch (Exception e) {
            logger.error("Error clearing user search history", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while clearing search history"));
        }
    }

    /**
     * Get health status of external APIs.
     * Provides information about availability of OpenLibrary and Google Books APIs.
     * 
     * @return Health status of external APIs
     */
    @GetMapping("/external-apis/health")
    public ResponseEntity<ApiResponse<ExternalBookSearchService.ExternalApiHealthStatus>> getExternalApiHealth() {
        try {
            logger.info("Checking external API health status");

            ExternalBookSearchService.ExternalApiHealthStatus healthStatus = 
                externalBookSearchService.getHealthStatus();

            logger.info("External API health check completed - OpenLibrary: {}, Google Books: {}", 
                       healthStatus.isOpenLibraryHealthy(), healthStatus.isGoogleBooksHealthy());

            return ResponseEntity.ok(ApiResponse.success(healthStatus));

        } catch (Exception e) {
            logger.error("Error checking external API health", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.error("An error occurred while checking external API health"));
        }
    }

    /**
     * Validate a book by ISBN and check if it exists in database.
     * Also enriches book data from external sources if available.
     * 
     * @param isbn ISBN to validate
     * @return Book validation response with enriched data
     */
    @GetMapping("/books/validate")
    public ResponseEntity<BookValidationResponse> validateBookByIsbn(@RequestParam String isbn) {
        try {
            logger.info("Validating book with ISBN: {}", isbn);

            BookValidationResponse validationResponse = bookService.validateBookByIsbn(isbn);

            logger.info("Book validation completed for ISBN: {}, valid: {}", isbn, validationResponse.isValid());
            return ResponseEntity.ok(validationResponse);

        } catch (IllegalArgumentException e) {
            logger.warn("Invalid ISBN format: {}", isbn);
            BookValidationResponse errorResponse = new BookValidationResponse();
            errorResponse.setValid(false);
            errorResponse.setIsbn(isbn);
            errorResponse.setErrorMessage(e.getMessage());
            return ResponseEntity.ok(errorResponse);
        } catch (Exception e) {
            logger.error("Error validating ISBN: " + isbn, e);
            BookValidationResponse errorResponse = new BookValidationResponse();
            errorResponse.setValid(false);
            errorResponse.setIsbn(isbn);
            errorResponse.setErrorMessage("Unable to validate ISBN at this time");
            return ResponseEntity.ok(errorResponse);
        }
    }

    /**
     * Get the currently authenticated user.
     * 
     * @return Current user or null if not authenticated
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Convert string sort criteria to enum.
     * 
     * @param sort Sort criteria string
     * @return SortCriteria enum
     */
    private BookSearchRequest.SortCriteria convertToSortCriteria(String sort) {
        if (sort == null) {
            return BookSearchRequest.SortCriteria.TITLE;
        }
        
        switch (sort.toLowerCase()) {
            case "title":
                return BookSearchRequest.SortCriteria.TITLE;
            case "author":
                return BookSearchRequest.SortCriteria.AUTHOR;
            case "publicationyear":
            case "publication_year":
                return BookSearchRequest.SortCriteria.PUBLICATION_YEAR;
            case "rating":
            case "averagerating":
            case "average_rating":
                return BookSearchRequest.SortCriteria.RATING;
            case "createdat":
            case "created_at":
                return BookSearchRequest.SortCriteria.CREATED_AT;
            default:
                return BookSearchRequest.SortCriteria.TITLE;
        }
    }

    /**
     * Convert string sort direction to enum.
     * 
     * @param direction Sort direction string
     * @return SortDirection enum
     */
    private BookSearchRequest.SortDirection convertToSortDirection(String direction) {
        if (direction == null) {
            return BookSearchRequest.SortDirection.ASC;
        }
        
        switch (direction.toLowerCase()) {
            case "desc":
            case "descending":
                return BookSearchRequest.SortDirection.DESC;
            case "asc":
            case "ascending":
            default:
                return BookSearchRequest.SortDirection.ASC;
        }
    }
}