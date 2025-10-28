package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.search.BookSearchPageResponse;
import com.thehomearchive.library.dto.search.BookSearchRequest;
import com.thehomearchive.library.dto.search.BookSearchResponse;
import com.thehomearchive.library.entity.Book;
import com.thehomearchive.library.entity.SearchHistory;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.repository.BookRatingRepository;
import com.thehomearchive.library.repository.BookRepository;
import com.thehomearchive.library.repository.CategoryRepository;
import com.thehomearchive.library.repository.SearchHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
 * Service for comprehensive book search functionality.
 * Handles full-text search, filtering, sorting, pagination, and search history tracking.
 * 
 * This service integrates:
 * - BookRepository for book data and search queries
 * - BookRatingRepository for rating information and user preferences
 * - SearchHistoryRepository for tracking and analytics
 * - Category data for filtering and enrichment
 */
@Service
@Transactional
public class BookSearchService {

    private static final Logger logger = LoggerFactory.getLogger(BookSearchService.class);

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookRatingRepository bookRatingRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Perform a comprehensive book search with all features.
     * 
     * @param request the search request parameters
     * @param currentUser the authenticated user (optional for anonymous search)
     * @return paginated search results with enhanced information
     */
    @Transactional(readOnly = true)
    public BookSearchPageResponse searchBooks(BookSearchRequest request, User currentUser) {
        logger.debug("Performing book search: {}", request);
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Create pageable with sorting
            Pageable pageable = createPageable(request);
            
            // Execute search based on request type
            Page<Book> bookPage = executeSearch(request, pageable);
            
            // Convert to search responses with enhanced data
            List<BookSearchResponse> searchResults = convertToSearchResponses(
                bookPage.getContent(), request, currentUser);
            
            // Record search in history (if user is authenticated and has query)
            recordSearchHistory(request, currentUser, bookPage.getTotalElements());
            
            // Build response
            BookSearchPageResponse response = buildPageResponse(
                searchResults, bookPage, request, startTime);
            
            logger.debug("Search completed: {} results in {}ms", 
                bookPage.getTotalElements(), response.getSearchTimeMs());
            
            return response;
            
        } catch (Exception e) {
            logger.error("Error performing book search: {}", e.getMessage(), e);
            throw new RuntimeException("Search failed: " + e.getMessage(), e);
        }
    }

    /**
     * Get popular search queries for suggestions.
     * 
     * @param limit maximum number of queries to return
     * @return list of popular search queries
     */
    @Transactional(readOnly = true)
    public List<String> getPopularSearchQueries(int limit) {
        logger.debug("Fetching {} popular search queries", limit);
        
        try {
            Pageable pageable = PageRequest.of(0, limit);
            Page<String> popularQueries = searchHistoryRepository.findPopularQueries(pageable);
            return popularQueries.getContent();
        } catch (Exception e) {
            logger.error("Error fetching popular queries: {}", e.getMessage(), e);
            return List.of(); // Return empty list on error
        }
    }

    /**
     * Get search suggestions for a partial query.
     * 
     * @param partialQuery the partial search query
     * @param limit maximum number of suggestions
     * @return list of suggested queries
     */
    @Transactional(readOnly = true)
    public List<String> getSearchSuggestions(String partialQuery, int limit) {
        if (partialQuery == null || partialQuery.trim().isEmpty()) {
            return getPopularSearchQueries(limit);
        }
        
        logger.debug("Getting search suggestions for: {}", partialQuery);
        
        try {
            Pageable pageable = PageRequest.of(0, limit);
            Page<String> suggestions = searchHistoryRepository.findQueriesByPartialMatch(partialQuery.trim(), pageable);
            return suggestions.getContent();
        } catch (Exception e) {
            logger.error("Error getting search suggestions: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Get user's recent search history.
     * 
     * @param user the user whose history to retrieve
     * @param limit maximum number of entries to return
     * @return list of recent search queries
     */
    @Transactional(readOnly = true)
    public List<String> getUserSearchHistory(User user, int limit) {
        if (user == null) {
            return List.of();
        }
        
        logger.debug("Fetching search history for user: {}", user.getId());
        
        try {
            return searchHistoryRepository.findRecentQueriesByUser(user.getId(), limit);
        } catch (Exception e) {
            logger.error("Error fetching user search history: {}", e.getMessage(), e);
            return List.of();
        }
    }

    /**
     * Clear user's search history.
     * 
     * @param user the user whose history to clear
     * @return number of entries removed
     */
    public Long clearUserSearchHistory(User user) {
        if (user == null) {
            return 0L;
        }
        
        logger.info("Clearing search history for user: {}", user.getId());
        
        try {
            return searchHistoryRepository.deleteByUserId(user.getId());
        } catch (Exception e) {
            logger.error("Error clearing user search history: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to clear search history", e);
        }
    }

    /**
     * Find similar books based on a book ID.
     * Uses category, author, and rating patterns for recommendations.
     * 
     * @param bookId the reference book ID
     * @param limit maximum number of similar books to return
     * @return list of similar books
     */
    @Transactional(readOnly = true)
    public List<BookSearchResponse> findSimilarBooks(Long bookId, int limit) {
        logger.debug("Finding similar books for book ID: {}", bookId);
        
        try {
            // Get the reference book
            Optional<Book> referenceBook = bookRepository.findById(bookId);
            if (referenceBook.isEmpty()) {
                logger.warn("Reference book not found: {}", bookId);
                return List.of();
            }
            
            Book book = referenceBook.get();
            Pageable pageable = PageRequest.of(0, limit);
            
            // Find similar books by category and author
            List<Book> similarBooks = bookRepository.findBooksWithCriteria(
                null, // title
                book.getAuthor(), // same author
                book.getCategory() != null ? book.getCategory().getId() : null, // same category
                null, null, // year range
                pageable).getContent();
            
            // Remove the reference book from results
            similarBooks = similarBooks.stream()
                .filter(b -> !b.getId().equals(bookId))
                .collect(Collectors.toList());
            
            // Convert to search responses
            return convertToSearchResponses(similarBooks, null, null);
            
        } catch (Exception e) {
            logger.error("Error finding similar books: {}", e.getMessage(), e);
            return List.of();
        }
    }

    // Private helper methods

    /**
     * Create Spring Data Pageable from search request.
     */
    private Pageable createPageable(BookSearchRequest request) {
        String sortColumn = request.getSortColumn();
        String direction = request.getDirectionString();
        
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(direction) ? Sort.Direction.DESC : Sort.Direction.ASC,
            sortColumn
        );
        
        return PageRequest.of(
            request.getPage() != null ? request.getPage() : 0,
            request.getSize() != null ? request.getSize() : 20,
            sort
        );
    }

    /**
     * Execute the appropriate search based on request parameters.
     */
    private Page<Book> executeSearch(BookSearchRequest request, Pageable pageable) {
        if (request.hasQuery() && request.hasCategoryFilter()) {
            // Search with both query and category filter
            return bookRepository.findBooksWithCriteria(
                request.getNormalizedQuery(), // title search
                request.getNormalizedQuery(), // author search  
                request.getCategory(),
                null, null, // year range
                pageable
            );
        } else if (request.hasQuery()) {
            // Text search only
            return bookRepository.searchBooks(request.getNormalizedQuery(), pageable);
        } else if (request.hasCategoryFilter()) {
            // Category filter only
            return bookRepository.findByCategoryId(request.getCategory(), pageable);
        } else {
            // No filters - return all books
            return bookRepository.findAll(pageable);
        }
    }

    /**
     * Convert books to search responses with enhanced data.
     */
    private List<BookSearchResponse> convertToSearchResponses(
            List<Book> books, BookSearchRequest request, User currentUser) {
        
        if (books.isEmpty()) {
            return List.of();
        }
        
        // Get book IDs for batch loading
        List<Long> bookIds = books.stream()
            .map(Book::getId)
            .collect(Collectors.toList());
        
        // Batch load rating data if requested
        // Load rating data if needed
        final Map<Long, Double> averageRatings = loadAverageRatings(request, bookIds);
        final Map<Long, Long> ratingCounts = loadRatingCounts(request, bookIds);
        final Map<Long, Integer> userRatings = loadUserRatings(request, currentUser, bookIds);

        // Convert to search responses
        return books.stream()
            .map(book -> convertToSearchResponse(
                book, 
                averageRatings.get(book.getId()),
                ratingCounts.get(book.getId()),
                userRatings.get(book.getId()),
                request != null && request.shouldIncludeCategory()
            ))
            .collect(Collectors.toList());
    }

    /**
     * Load average ratings for books if needed.
     */
    private Map<Long, Double> loadAverageRatings(BookSearchRequest request, List<Long> bookIds) {
        if (request == null || request.shouldIncludeRatings()) {
            try {
                return bookRatingRepository.findAverageRatingsByBookIds(bookIds);
            } catch (Exception e) {
                logger.warn("Error loading average ratings: {}", e.getMessage());
            }
        }
        return Map.of();
    }

    /**
     * Load rating counts for books if needed.
     */
    private Map<Long, Long> loadRatingCounts(BookSearchRequest request, List<Long> bookIds) {
        if (request == null || request.shouldIncludeRatings()) {
            try {
                return bookRatingRepository.findRatingCountsByBookIds(bookIds);
            } catch (Exception e) {
                logger.warn("Error loading rating counts: {}", e.getMessage());
            }
        }
        return Map.of();
    }

    /**
     * Load user ratings for books if needed.
     */
    private Map<Long, Integer> loadUserRatings(BookSearchRequest request, User currentUser, List<Long> bookIds) {
        if ((request == null || request.shouldIncludeRatings()) && currentUser != null) {
            try {
                return bookRatingRepository.findUserRatingsByBookIds(currentUser.getId(), bookIds);
            } catch (Exception e) {
                logger.warn("Error loading user ratings: {}", e.getMessage());
            }
        }
        return Map.of();
    }

    /**
     * Convert a single book to search response.
     */
    private BookSearchResponse convertToSearchResponse(
            Book book, Double averageRating, Long ratingCount, Integer userRating, 
            boolean includeCategory) {
        
        BookSearchResponse response = new BookSearchResponse();
        
        // Copy basic book data
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setIsbn(book.getIsbn());
        response.setDescription(book.getDescription());
        response.setPublicationYear(book.getPublicationYear());
        response.setPublisher(book.getPublisher());
        response.setPageCount(book.getPageCount());
        response.setCreatedAt(book.getCreatedAt());
        response.setUpdatedAt(book.getUpdatedAt());
        
        // Add rating information
        response.setAverageRating(averageRating);
        response.setRatingCount(ratingCount != null ? ratingCount.intValue() : 0);
        response.setUserRating(userRating);
        
        // Add category if requested and available
        if (includeCategory && book.getCategory() != null) {
            // Convert category to CategoryResponse (simplified)
            // This would typically use a CategoryService or mapper
            response.setCategory(null); // TODO: Implement category conversion
        }
        
        return response;
    }

    /**
     * Record search in history for analytics.
     */
    private void recordSearchHistory(BookSearchRequest request, User currentUser, Long resultCount) {
        // Only record searches with actual queries
        if (!request.hasQuery()) {
            return;
        }
        
        try {
            SearchHistory history = new SearchHistory();
            history.setQuery(request.getNormalizedQuery());
            history.setUser(currentUser); // Can be null for anonymous searches
            history.setResultCount(resultCount.intValue());
            history.setSearchedAt(LocalDateTime.now());
            
            searchHistoryRepository.save(history);
            
            logger.debug("Recorded search history: query='{}', results={}, user={}", 
                request.getNormalizedQuery(), resultCount, 
                currentUser != null ? currentUser.getId() : "anonymous");
                
        } catch (Exception e) {
            logger.warn("Failed to record search history: {}", e.getMessage());
            // Don't fail the search if history recording fails
        }
    }

    /**
     * Build the paginated response.
     */
    private BookSearchPageResponse buildPageResponse(
            List<BookSearchResponse> content, Page<Book> bookPage, 
            BookSearchRequest request, long startTime) {
        
        long searchTime = System.currentTimeMillis() - startTime;
        
        BookSearchPageResponse response = new BookSearchPageResponse(
            content,
            bookPage.getNumber(),
            bookPage.getSize(),
            bookPage.getTotalElements(),
            bookPage.getTotalPages()
        );
        
        response.setQuery(request.getNormalizedQuery());
        response.setSearchTimeMs(searchTime);
        
        return response;
    }
}