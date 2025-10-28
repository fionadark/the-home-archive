package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.search.BookRatingRequest;
import com.thehomearchive.library.dto.search.BookRatingResponse;
import com.thehomearchive.library.entity.Book;
import com.thehomearchive.library.entity.BookRating;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.exception.DuplicateRatingException;
import com.thehomearchive.library.exception.ResourceNotFoundException;
import com.thehomearchive.library.repository.BookRatingRepository;
import com.thehomearchive.library.repository.BookRepository;
import com.thehomearchive.library.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing book ratings and reviews.
 * Handles CRUD operations for user ratings, aggregations, and analytics.
 */
@Service
@Transactional
public class RatingService {
    
    private static final Logger logger = LoggerFactory.getLogger(RatingService.class);
    
    private final BookRatingRepository bookRatingRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    
    public RatingService(BookRatingRepository bookRatingRepository,
                        BookRepository bookRepository,
                        UserRepository userRepository) {
        this.bookRatingRepository = bookRatingRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }
    
    // ========== CRUD Operations ==========
    
    /**
     * Create a new rating for a book by a user.
     *
     * @param userId ID of the user creating the rating
     * @param bookId ID of the book being rated
     * @param request rating details (rating value and optional review)
     * @return the created rating response
     * @throws ResourceNotFoundException if user or book not found
     * @throws DuplicateRatingException if user has already rated this book
     */
    public BookRatingResponse createRating(Long userId, Long bookId, BookRatingRequest request) {
        logger.info("Creating rating for user {} on book {} with rating {}", userId, bookId, request.getRating());
        
        // Validate user exists
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        
        // Validate book exists
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book", "id", bookId));
        
        // Check if user has already rated this book
        if (bookRatingRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new DuplicateRatingException("User has already rated this book");
        }
        
        // Create and save rating
        BookRating rating = new BookRating();
        rating.setUser(user);
        rating.setBook(book);
        rating.setRating(request.getRating());
        if (request.hasReview()) {
            rating.setReview(request.getTrimmedReview());
        }
        
        BookRating savedRating = bookRatingRepository.save(rating);
        logger.info("Successfully created rating with ID {}", savedRating.getId());
        
        return mapToResponse(savedRating);
    }
    
    /**
     * Update an existing rating by a user.
     *
     * @param userId ID of the user updating the rating
     * @param bookId ID of the book being rated
     * @param request updated rating details
     * @return the updated rating response
     * @throws ResourceNotFoundException if rating not found
     */
    public BookRatingResponse updateRating(Long userId, Long bookId, BookRatingRequest request) {
        logger.info("Updating rating for user {} on book {} with new rating {}", userId, bookId, request.getRating());
        
        BookRating existingRating = bookRatingRepository.findByUserIdAndBookId(userId, bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Rating not found for user " + userId + " and book " + bookId));
        
        // Update rating fields
        existingRating.setRating(request.getRating());
        if (request.hasReview()) {
            existingRating.setReview(request.getTrimmedReview());
        } else {
            existingRating.setReview(null);
        }
        
        BookRating savedRating = bookRatingRepository.save(existingRating);
        logger.info("Successfully updated rating with ID {}", savedRating.getId());
        
        return mapToResponse(savedRating);
    }
    
    /**
     * Delete a rating by a user.
     *
     * @param userId ID of the user deleting the rating
     * @param bookId ID of the book rating to delete
     * @throws ResourceNotFoundException if rating not found
     */
    public void deleteRating(Long userId, Long bookId) {
        logger.info("Deleting rating for user {} on book {}", userId, bookId);
        
        BookRating rating = bookRatingRepository.findByUserIdAndBookId(userId, bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Rating not found for user " + userId + " and book " + bookId));
        
        bookRatingRepository.delete(rating);
        logger.info("Successfully deleted rating with ID {}", rating.getId());
    }
    
    // ========== Query Operations ==========
    
    /**
     * Get a specific rating by user and book.
     *
     * @param userId ID of the user
     * @param bookId ID of the book
     * @return the rating response if found
     * @throws ResourceNotFoundException if rating not found
     */
    @Transactional(readOnly = true)
    public BookRatingResponse getRating(Long userId, Long bookId) {
        BookRating rating = bookRatingRepository.findByUserIdAndBookId(userId, bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Rating not found for user " + userId + " and book " + bookId));
        
        return mapToResponse(rating);
    }
    
    /**
     * Get a user's rating for a book, returning null if not found.
     *
     * @param userId ID of the user
     * @param bookId ID of the book
     * @return the rating response if found, null otherwise
     */
    @Transactional(readOnly = true)
    public BookRatingResponse getUserRatingForBook(Long userId, Long bookId) {
        Optional<BookRating> rating = bookRatingRepository.findByUserIdAndBookId(userId, bookId);
        return rating.map(this::mapToResponse).orElse(null);
    }
    
    /**
     * Get all ratings for a specific book with pagination.
     *
     * @param bookId ID of the book
     * @param page page number (0-based)
     * @param size page size
     * @param sortBy field to sort by (default: createdAt)
     * @param sortDirection sort direction (ASC or DESC, default: DESC)
     * @return page of rating responses
     */
    @Transactional(readOnly = true)
    public Page<BookRatingResponse> getBookRatings(Long bookId, int page, int size, String sortBy, String sortDirection) {
        // Validate book exists
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book", "id", bookId);
        }
        
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = sortBy != null ? sortBy : "createdAt";
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<BookRating> ratings = bookRatingRepository.findByBookId(bookId, pageable);
        
        return ratings.map(this::mapToResponse);
    }
    
    /**
     * Get all ratings by a specific user with pagination.
     *
     * @param userId ID of the user
     * @param page page number (0-based)
     * @param size page size
     * @param sortBy field to sort by (default: createdAt)
     * @param sortDirection sort direction (ASC or DESC, default: DESC)
     * @return page of rating responses
     */
    @Transactional(readOnly = true)
    public Page<BookRatingResponse> getUserRatings(Long userId, int page, int size, String sortBy, String sortDirection) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection) ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortField = sortBy != null ? sortBy : "createdAt";
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortField));
        Page<BookRating> ratings = bookRatingRepository.findByUserId(userId, pageable);
        
        return ratings.map(this::mapToResponse);
    }
    
    /**
     * Get ratings with reviews (pagination supported).
     *
     * @param page page number (0-based)
     * @param size page size
     * @return page of rating responses with reviews
     */
    @Transactional(readOnly = true)
    public Page<BookRatingResponse> getRatingsWithReviews(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookRating> ratings = bookRatingRepository.findRatingsWithReviews(pageable);
        
        return ratings.map(this::mapToResponse);
    }
    
    /**
     * Get recent ratings within a time period.
     *
     * @param hours number of hours back from now
     * @param page page number (0-based)
     * @param size page size
     * @return page of recent rating responses
     */
    @Transactional(readOnly = true)
    public Page<BookRatingResponse> getRecentRatings(int hours, int page, int size) {
        LocalDateTime since = LocalDateTime.now().minusHours(hours);
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<BookRating> ratings = bookRatingRepository.findByCreatedAtAfter(since, pageable);
        
        return ratings.map(this::mapToResponse);
    }
    
    // ========== Analytics and Aggregations ==========
    
    /**
     * Get rating statistics for a book.
     *
     * @param bookId ID of the book
     * @return rating statistics containing average, count, distribution
     */
    @Transactional(readOnly = true)
    public BookRatingStatistics getBookRatingStatistics(Long bookId) {
        // Validate book exists
        if (!bookRepository.existsById(bookId)) {
            throw new ResourceNotFoundException("Book", "id", bookId);
        }
        
        Double averageRating = bookRatingRepository.findAverageRatingByBookId(bookId);
        Long totalCount = bookRatingRepository.countByBookId(bookId);
        List<Object[]> distribution = bookRatingRepository.findRatingDistributionByBookId(bookId);
        
        return new BookRatingStatistics(bookId, averageRating, totalCount, distribution);
    }
    
    /**
     * Get rating statistics for a user.
     *
     * @param userId ID of the user
     * @return user rating statistics
     */
    @Transactional(readOnly = true)
    public UserRatingStatistics getUserRatingStatistics(Long userId) {
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User", "id", userId);
        }
        
        Object[] stats = bookRatingRepository.findUserRatingStatistics(userId);
        
        return new UserRatingStatistics(
            userId,
            stats[0] != null ? ((Number) stats[0]).longValue() : 0L,
            stats[1] != null ? ((Number) stats[1]).doubleValue() : null,
            stats[2] != null ? ((Number) stats[2]).intValue() : null,
            stats[3] != null ? ((Number) stats[3]).intValue() : null
        );
    }
    
    /**
     * Get top-rated books with minimum rating count.
     *
     * @param minRatingCount minimum number of ratings required
     * @param page page number (0-based)
     * @param size page size
     * @return page of top-rated book IDs with statistics
     */
    @Transactional(readOnly = true)
    public Page<TopRatedBook> getTopRatedBooks(Long minRatingCount, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Object[]> results = bookRatingRepository.findTopRatedBooks(minRatingCount, pageable);
        
        return results.map(arr -> new TopRatedBook(
            (Long) arr[0],
            ((Number) arr[1]).doubleValue(),
            ((Number) arr[2]).longValue()
        ));
    }
    
    /**
     * Batch operation to get average ratings for multiple books.
     *
     * @param bookIds list of book IDs
     * @return map of book ID to average rating
     */
    @Transactional(readOnly = true)
    public Map<Long, Double> getBatchAverageRatings(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }
        
        return bookRatingRepository.findAverageRatingsByBookIds(bookIds);
    }
    
    /**
     * Batch operation to get rating counts for multiple books.
     *
     * @param bookIds list of book IDs
     * @return map of book ID to rating count
     */
    @Transactional(readOnly = true)
    public Map<Long, Long> getBatchRatingCounts(List<Long> bookIds) {
        if (bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }
        
        return bookRatingRepository.findRatingCountsByBookIds(bookIds);
    }
    
    /**
     * Batch operation to get user's ratings for multiple books.
     *
     * @param userId ID of the user
     * @param bookIds list of book IDs
     * @return map of book ID to user's rating
     */
    @Transactional(readOnly = true)
    public Map<Long, Integer> getBatchUserRatings(Long userId, List<Long> bookIds) {
        if (userId == null || bookIds == null || bookIds.isEmpty()) {
            return Map.of();
        }
        
        return bookRatingRepository.findUserRatingsByBookIds(userId, bookIds);
    }
    
    // ========== Business Logic Methods ==========
    
    /**
     * Check if a user has rated a specific book.
     *
     * @param userId ID of the user
     * @param bookId ID of the book
     * @return true if user has rated the book, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean hasUserRatedBook(Long userId, Long bookId) {
        return bookRatingRepository.existsByUserIdAndBookId(userId, bookId);
    }
    
    /**
     * Get rating distribution for a book as percentages.
     *
     * @param bookId ID of the book
     * @return map of rating (1-5) to percentage
     */
    @Transactional(readOnly = true)
    public Map<Integer, Double> getRatingDistributionPercentages(Long bookId) {
        List<Object[]> distribution = bookRatingRepository.findRatingDistributionByBookId(bookId);
        long totalRatings = distribution.stream()
            .mapToLong(arr -> ((Number) arr[1]).longValue())
            .sum();
        
        if (totalRatings == 0) {
            return Map.of();
        }
        
        return distribution.stream()
            .collect(Collectors.toMap(
                arr -> (Integer) arr[0],
                arr -> (((Number) arr[1]).doubleValue() / totalRatings) * 100.0
            ));
    }
    
    /**
     * Find users with similar rating patterns for recommendation purposes.
     *
     * @param userId ID of the reference user
     * @param bookId ID of the book to compare ratings on
     * @param tolerance maximum difference in rating (e.g., 1 = within 1 star)
     * @return list of user IDs with similar ratings
     */
    @Transactional(readOnly = true)
    public List<Long> findSimilarUsers(Long userId, Long bookId, Integer tolerance) {
        return bookRatingRepository.findUsersWithSimilarRating(userId, bookId, tolerance);
    }
    
    /**
     * Find books rated by both users (for collaborative filtering).
     *
     * @param userId1 ID of the first user
     * @param userId2 ID of the second user
     * @return list of book IDs rated by both users
     */
    @Transactional(readOnly = true)
    public List<Long> findCommonlyRatedBooks(Long userId1, Long userId2) {
        return bookRatingRepository.findBooksRatedByBothUsers(userId1, userId2);
    }
    
    /**
     * Create or update a rating (upsert operation).
     *
     * @param userId ID of the user
     * @param bookId ID of the book
     * @param request rating details
     * @return the created or updated rating response
     */
    public BookRatingResponse createOrUpdateRating(Long userId, Long bookId, BookRatingRequest request) {
        Optional<BookRating> existingRating = bookRatingRepository.findByUserIdAndBookId(userId, bookId);
        
        if (existingRating.isPresent()) {
            logger.info("Updating existing rating for user {} on book {}", userId, bookId);
            return updateRating(userId, bookId, request);
        } else {
            logger.info("Creating new rating for user {} on book {}", userId, bookId);
            return createRating(userId, bookId, request);
        }
    }
    
    // ========== Helper Methods ==========
    
    /**
     * Map BookRating entity to BookRatingResponse DTO.
     */
    private BookRatingResponse mapToResponse(BookRating rating) {
        BookRatingResponse response = new BookRatingResponse();
        response.setId(rating.getId());
        response.setBookId(rating.getBook().getId());
        response.setBookTitle(rating.getBook().getTitle());
        response.setUserId(rating.getUser().getId());
        response.setUserFullName(rating.getUser().getFirstName() + " " + rating.getUser().getLastName());
        response.setRating(rating.getRating());
        response.setReview(rating.getReview());
        response.setCreatedAt(rating.getCreatedAt());
        response.setUpdatedAt(rating.getUpdatedAt());
        return response;
    }
    
    // ========== Statistics Classes ==========
    
    /**
     * Statistics for a book's ratings.
     */
    public static class BookRatingStatistics {
        private final Long bookId;
        private final Double averageRating;
        private final Long totalRatings;
        private final Map<Integer, Long> distribution;
        
        public BookRatingStatistics(Long bookId, Double averageRating, Long totalRatings, List<Object[]> distribution) {
            this.bookId = bookId;
            this.averageRating = averageRating;
            this.totalRatings = totalRatings;
            this.distribution = distribution.stream()
                .collect(Collectors.toMap(
                    arr -> (Integer) arr[0],
                    arr -> ((Number) arr[1]).longValue()
                ));
        }
        
        public Long getBookId() { return bookId; }
        public Double getAverageRating() { return averageRating; }
        public Long getTotalRatings() { return totalRatings; }
        public Map<Integer, Long> getDistribution() { return distribution; }
    }
    
    /**
     * Statistics for a user's ratings.
     */
    public static class UserRatingStatistics {
        private final Long userId;
        private final Long totalRatings;
        private final Double averageRating;
        private final Integer highestRating;
        private final Integer lowestRating;
        
        public UserRatingStatistics(Long userId, Long totalRatings, Double averageRating, 
                                  Integer highestRating, Integer lowestRating) {
            this.userId = userId;
            this.totalRatings = totalRatings;
            this.averageRating = averageRating;
            this.highestRating = highestRating;
            this.lowestRating = lowestRating;
        }
        
        public Long getUserId() { return userId; }
        public Long getTotalRatings() { return totalRatings; }
        public Double getAverageRating() { return averageRating; }
        public Integer getHighestRating() { return highestRating; }
        public Integer getLowestRating() { return lowestRating; }
    }
    
    /**
     * Top-rated book information.
     */
    public static class TopRatedBook {
        private final Long bookId;
        private final Double averageRating;
        private final Long ratingCount;
        
        public TopRatedBook(Long bookId, Double averageRating, Long ratingCount) {
            this.bookId = bookId;
            this.averageRating = averageRating;
            this.ratingCount = ratingCount;
        }
        
        public Long getBookId() { return bookId; }
        public Double getAverageRating() { return averageRating; }
        public Long getRatingCount() { return ratingCount; }
    }
}