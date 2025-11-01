package com.thehomearchive.library.controller;

import com.thehomearchive.library.dto.response.ApiResponse;
import com.thehomearchive.library.dto.response.PagedResponse;
import com.thehomearchive.library.dto.search.BookRatingRequest;
import com.thehomearchive.library.dto.search.BookRatingResponse;
import com.thehomearchive.library.exception.DuplicateRatingException;
import com.thehomearchive.library.exception.ResourceNotFoundException;
import com.thehomearchive.library.service.RatingService;
import com.thehomearchive.library.util.SecurityUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for book rating operations.
 * Handles creating, reading, updating, and deleting book ratings.
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class RatingController {
    
    private static final Logger logger = LoggerFactory.getLogger(RatingController.class);
    
    private final RatingService ratingService;
    
    public RatingController(RatingService ratingService) {
        this.ratingService = ratingService;
    }
    
    /**
     * Rate a book.
     * Creates a new rating for the authenticated user and specified book.
     * 
     * @param bookId the ID of the book to rate
     * @param request the rating request containing rating value and optional review
     * @return ResponseEntity containing the created rating response
     */
    @PostMapping("/books/{id}/ratings")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookRatingResponse> rateBook(
            @PathVariable("id") Long bookId,
            @Valid @RequestBody BookRatingRequest request) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("Creating rating for book ID: {} by user ID: {}, rating: {}", 
                   bookId, userId, request.getRating());
        
        try {
            BookRatingResponse response = ratingService.createRating(userId, bookId, request);
            logger.info("Rating created successfully for book ID: {} by user ID: {}", bookId, userId);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
            
        } catch (DuplicateRatingException e) {
            logger.warn("User {} already rated book {}: {}", userId, bookId, e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
            
        } catch (ResourceNotFoundException e) {
            logger.warn("Resource not found while rating book {}: {}", bookId, e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            logger.error("Error creating rating for book ID: {} by user ID: {}", bookId, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Update an existing book rating.
     * Updates the rating for the authenticated user and specified book.
     * 
     * @param bookId the ID of the book
     * @param request the updated rating request
     * @return ResponseEntity containing the updated rating response
     */
    @PutMapping("/books/{id}/ratings")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookRatingResponse> updateRating(
            @PathVariable("id") Long bookId,
            @Valid @RequestBody BookRatingRequest request) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("Updating rating for book ID: {} by user ID: {}, new rating: {}", 
                   bookId, userId, request.getRating());
        
        try {
            BookRatingResponse response = ratingService.updateRating(userId, bookId, request);
            logger.info("Rating updated successfully for book ID: {} by user ID: {}", bookId, userId);
            return ResponseEntity.ok(response);
            
        } catch (ResourceNotFoundException e) {
            logger.warn("Rating or resource not found while updating book {}: {}", bookId, e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            logger.error("Error updating rating for book ID: {} by user ID: {}", bookId, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Delete a book rating.
     * Removes the rating for the authenticated user and specified book.
     * 
     * @param bookId the ID of the book
     * @return ResponseEntity with success/error status
     */
    @DeleteMapping("/books/{id}/ratings")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<String>> deleteRating(@PathVariable("id") Long bookId) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("Deleting rating for book ID: {} by user ID: {}", bookId, userId);
        
        try {
            ratingService.deleteRating(userId, bookId);
            logger.info("Rating deleted successfully for book ID: {} by user ID: {}", bookId, userId);
            return ResponseEntity.ok(ApiResponse.success("Rating deleted successfully"));
            
        } catch (ResourceNotFoundException e) {
            logger.warn("Rating not found while deleting for book {}: {}", bookId, e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            logger.error("Error deleting rating for book ID: {} by user ID: {}", bookId, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all ratings for a specific book.
     * Retrieves paginated ratings for the specified book.
     * 
     * @param bookId the ID of the book
     * @param page the page number (0-based, default: 0)
     * @param size the page size (default: 10, max: 100)
     * @param sortBy field to sort by (default: "createdAt")
     * @param sortDirection sort direction: "asc" or "desc" (default: "desc")
     * @return ResponseEntity containing paginated rating responses
     */
    @GetMapping("/books/{id}/ratings")
    public ResponseEntity<PagedResponse<BookRatingResponse>> getBookRatings(
            @PathVariable("id") Long bookId,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        logger.info("Getting ratings for book ID: {}, page: {}, size: {}, sortBy: {}, direction: {}", 
                   bookId, page, size, sortBy, sortDirection);
        
        try {
            Page<BookRatingResponse> ratings = ratingService.getBookRatings(
                bookId, page, size, sortBy, sortDirection);
            
            PagedResponse<BookRatingResponse> response = PagedResponse.of(ratings);
            logger.info("Retrieved {} ratings for book ID: {} (page {} of {})", 
                       ratings.getNumberOfElements(), bookId, page + 1, ratings.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (ResourceNotFoundException e) {
            logger.warn("Book not found while getting ratings: {}", e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            logger.error("Error getting ratings for book ID: {}", bookId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get the current user's rating for a specific book.
     * Retrieves the authenticated user's rating for the specified book.
     * 
     * @param bookId the ID of the book
     * @return ResponseEntity containing the user's rating response
     */
    @GetMapping("/books/{id}/ratings/my")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookRatingResponse> getMyRating(@PathVariable("id") Long bookId) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("Getting rating for book ID: {} by user ID: {}", bookId, userId);
        
        try {
            BookRatingResponse response = ratingService.getUserRatingForBook(userId, bookId);
            logger.info("Retrieved rating for book ID: {} by user ID: {}", bookId, userId);
            return ResponseEntity.ok(response);
            
        } catch (ResourceNotFoundException e) {
            logger.info("No rating found for book ID: {} by user ID: {}", bookId, userId);
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            logger.error("Error getting rating for book ID: {} by user ID: {}", bookId, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get all ratings by the current user.
     * Retrieves paginated ratings created by the authenticated user.
     * 
     * @param page the page number (0-based, default: 0)
     * @param size the page size (default: 10, max: 100)
     * @param sortBy field to sort by (default: "createdAt")
     * @param sortDirection sort direction: "asc" or "desc" (default: "desc")
     * @return ResponseEntity containing paginated rating responses
     */
    @GetMapping("/users/ratings")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PagedResponse<BookRatingResponse>> getCurrentUserRatings(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDirection) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("Getting ratings for user ID: {}, page: {}, size: {}, sortBy: {}, direction: {}", 
                   userId, page, size, sortBy, sortDirection);
        
        try {
            Page<BookRatingResponse> ratings = ratingService.getUserRatings(
                userId, page, size, sortBy, sortDirection);
            
            PagedResponse<BookRatingResponse> response = PagedResponse.of(ratings);
            logger.info("Retrieved {} ratings for user ID: {} (page {} of {})", 
                       ratings.getNumberOfElements(), userId, page + 1, ratings.getTotalPages());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error getting ratings for user ID: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Create or update a book rating.
     * If the user has already rated the book, updates the existing rating.
     * Otherwise, creates a new rating.
     * 
     * @param bookId the ID of the book to rate
     * @param request the rating request containing rating value and optional review
     * @return ResponseEntity containing the rating response
     */
    @PutMapping("/books/{id}/ratings/upsert")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BookRatingResponse> createOrUpdateRating(
            @PathVariable("id") Long bookId,
            @Valid @RequestBody BookRatingRequest request) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("Creating or updating rating for book ID: {} by user ID: {}, rating: {}", 
                   bookId, userId, request.getRating());
        
        try {
            BookRatingResponse response = ratingService.createOrUpdateRating(userId, bookId, request);
            logger.info("Rating created/updated successfully for book ID: {} by user ID: {}", bookId, userId);
            return ResponseEntity.ok(response);
            
        } catch (ResourceNotFoundException e) {
            logger.warn("Resource not found while creating/updating rating for book {}: {}", bookId, e.getMessage());
            return ResponseEntity.notFound().build();
            
        } catch (Exception e) {
            logger.error("Error creating/updating rating for book ID: {} by user ID: {}", bookId, userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Check if the current user has rated a specific book.
     * 
     * @param bookId the ID of the book
     * @return ResponseEntity containing boolean response
     */
    @GetMapping("/books/{id}/ratings/check")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<ApiResponse<Boolean>> hasUserRatedBook(@PathVariable("id") Long bookId) {
        
        Long userId = SecurityUtils.getCurrentUserId();
        logger.info("Checking if user ID: {} has rated book ID: {}", userId, bookId);
        
        try {
            boolean hasRated = ratingService.hasUserRatedBook(userId, bookId);
            logger.info("User ID: {} has rated book ID: {}: {}", userId, bookId, hasRated);
            
            if (hasRated) {
                return ResponseEntity.ok(ApiResponse.success("User has rated this book"));
            } else {
                return ResponseEntity.ok(ApiResponse.success("User has not rated this book"));
            }
            
        } catch (Exception e) {
            logger.error("Error checking if user ID: {} has rated book ID: {}", userId, bookId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}