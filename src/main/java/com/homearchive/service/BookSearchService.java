package com.homearchive.service;

import com.homearchive.dto.BookSearchDto;
import com.homearchive.dto.SearchRequest;
import com.homearchive.dto.SearchResponse;
import com.homearchive.dto.SortBy;
import com.homearchive.dto.SortOrder;
import com.homearchive.entity.Book;
import com.homearchive.entity.PhysicalLocation;
import com.homearchive.mapper.BookMapper;
import com.homearchive.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for book search operations.
 * Implements business logic for searching books with relevance scoring.
 */
@Service
@Transactional(readOnly = true)
public class BookSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookSearchService.class);
    
    // Simple LRU cache for preprocessed queries to improve performance
    private static final Map<String, String> PREPROCESSED_QUERY_CACHE = new java.util.LinkedHashMap<String, String>(100, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, String> eldest) {
            return size() > 100; // Keep only 100 most recent queries
        }
    };
    
    // Common stop words that should be handled specially
    private static final java.util.Set<String> COMMON_STOP_WORDS = java.util.Set.of(
        "the", "a", "an", "and", "or", "but", "in", "on", "at", "to", "for", "of", "with", "by",
        "book", "books", "novel", "story", "tale", "reading", "read", "author", "writer"
    );
    
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    
    @Autowired
    public BookSearchService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }
    
    /**
     * Search books based on the provided search request.
     * Results are cached for performance optimization.
     */
    @Cacheable(value = "searchResults", 
               key = "#request.query + '_' + #request.sortBy + '_' + #request.sortOrder + '_' + #request.limit + '_' + #request.physicalLocation",
               condition = "#request.query != null && #request.query.length() > 0")
    public SearchResponse searchBooks(SearchRequest request) {
        logger.debug("Searching books with request: {}", request);
        
        // Validate and set defaults
        if (request == null) {
            request = new SearchRequest();
        }
        
        // Validate search parameters
        validateSearchRequest(request);
        
        String originalQuery = request.getTrimmedQuery();
        String query = originalQuery;
        int limit = Math.min(request.getLimit(), 50); // Enforce max limit
        Pageable pageable = PageRequest.of(0, limit);
        
        List<Book> books;
        int totalResults;
        
        if (query.isEmpty()) {
            // Empty query - return all books ordered by title
            if (request.hasPhysicalLocationFilter()) {
                books = bookRepository.findAllOrderedByTitleByLocation(request.getPhysicalLocation(), pageable);
                totalResults = bookRepository.countByPhysicalLocation(request.getPhysicalLocation());
                logger.debug("Empty query with location filter '{}' - returning {} books out of {} total", 
                           request.getPhysicalLocation(), books.size(), totalResults);
            } else {
                books = bookRepository.findAllOrderedByTitle(pageable);
                totalResults = (int) bookRepository.count();
                logger.debug("Empty query - returning {} books out of {} total", books.size(), totalResults);
            }
        } else {
            // Perform search with relevance scoring
            books = performSearch(query, request.getSortBy(), request.getPhysicalLocation(), pageable);
            if (request.hasPhysicalLocationFilter()) {
                try {
                    totalResults = bookRepository.countSearchResultsByLocation(preprocessQuery(query), request.getPhysicalLocation().name());
                } catch (Exception e) {
                    logger.debug("MySQL count search failed, falling back to H2-compatible count: {}", e.getMessage());
                    totalResults = bookRepository.countSearchResultsByLocationH2(preprocessQuery(query), request.getPhysicalLocation().name());
                }
                logger.debug("Search for '{}' with location '{}' returned {} books out of {} total matches", 
                           originalQuery, request.getPhysicalLocation(), books.size(), totalResults);
            } else {
                try {
                    totalResults = bookRepository.countSearchResults(preprocessQuery(query));
                } catch (Exception e) {
                    logger.debug("MySQL count search failed, falling back to H2-compatible count: {}", e.getMessage());
                    totalResults = bookRepository.countSearchResultsH2(preprocessQuery(query));
                }
                logger.debug("Search for '{}' returned {} books out of {} total matches", 
                           originalQuery, books.size(), totalResults);
            }
        }
        
        // Convert to DTOs
        List<BookSearchDto> bookDtos;
        if (!query.isEmpty()) {
            bookDtos = bookMapper.toSearchDtoList(books, originalQuery);
        } else {
            bookDtos = bookMapper.toSearchDtoList(books);
        }
        
        // Handle edge case of no results found
        if (bookDtos.isEmpty() && !query.isEmpty()) {
            logger.info("No results found for query: '{}'. Consider expanding search terms.", originalQuery);
            // For now, we'll just return empty results with appropriate message
            // In the future, we could suggest alternative queries or partial matches
        }
        
        // Create response
        SearchResponse response = new SearchResponse(bookDtos, totalResults, originalQuery, 
                                                   request.getSortBy(), request.getSortOrder());
        
        logger.debug("Search completed: {}", response);
        return response;
    }
    
    /**
     * Perform the actual search based on query and sort criteria.
     */
    private List<Book> performSearch(String query, SortBy sortBy, PhysicalLocation physicalLocation, Pageable pageable) {
        // Preprocess query - remove special characters and trim
        String cleanQuery = preprocessQuery(query);
        
        if (sortBy == SortBy.RELEVANCE || sortBy == null) {
            // Check if this is a multi-word query
            if (isMultiWordQuery(cleanQuery)) {
                return performMultiWordSearch(cleanQuery, physicalLocation, pageable);
            } else {
                // Use single-word full-text search with relevance scoring
                if (physicalLocation != null) {
                    return bookRepository.searchBooksWithRelevanceByLocation(cleanQuery, physicalLocation.name(), pageable);
                } else {
                    return bookRepository.searchBooksWithRelevance(cleanQuery, pageable);
                }
            }
        } else {
            // For other sort types, use simple search and let database handle sorting
            if (physicalLocation != null) {
                return bookRepository.searchByTitleAndAuthorByLocation(cleanQuery, physicalLocation, pageable);
            } else {
                return bookRepository.searchByTitleAndAuthor(cleanQuery, pageable);
            }
        }
    }
    
    /**
     * Preprocess search query to improve search results.
     * Removes special characters, normalizes whitespace, and handles ISBN formatting.
     * Uses caching to avoid repeated preprocessing of the same queries.
     */
    private String preprocessQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }
        
        // Check cache first for performance
        String cached = PREPROCESSED_QUERY_CACHE.get(query);
        if (cached != null) {
            return cached;
        }
        
        // Remove special characters that might interfere with search
        String cleaned = query.trim()
                             .replaceAll("[\"'`()\\[\\]{}]", "") // Remove quotes and brackets
                             .replaceAll("[;,!?]", " ") // Replace punctuation with space
                             .replaceAll("[-_]", "") // Remove hyphens and underscores (especially for ISBN)
                             .replaceAll("\\s+", " ") // Normalize whitespace
                             .toLowerCase(); // Convert to lowercase for consistent searching
        
        // Cache the result for performance
        PREPROCESSED_QUERY_CACHE.put(query, cleaned);
        
        logger.debug("Preprocessed query: '{}' -> '{}'", query, cleaned);
        return cleaned;
    }
    
    /**
     * Validate search request parameters.
     */
    private void validateSearchRequest(SearchRequest request) {
        if (request == null) {
            return;
        }
        
        // Validate query length (should be caught by controller validation, but double-check)
        String query = request.getQuery();
        if (query != null && query.length() > 100) {
            logger.warn("Search query exceeds maximum length: {} characters", query.length());
            // Truncate query to max length
            request.setQuery(query.substring(0, 100));
        }
        
        // Validate limit
        Integer limit = request.getLimit();
        if (limit != null && (limit < 1 || limit > 50)) {
            logger.warn("Invalid search limit: {}, setting to default", limit);
            request.setLimit(50);
        }
        
        // Validate sort parameters
        if (request.getSortBy() == null) {
            request.setSortBy(SortBy.RELEVANCE);
        }
        if (request.getSortOrder() == null) {
            request.setSortOrder(SortOrder.DESC);
        }
        
        logger.debug("Validated search request: {}", request);
    }
    
    /**
     * Check if the query contains multiple words.
     */
    private boolean isMultiWordQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return false;
        }
        return query.trim().split("\\s+").length > 1;
    }
    
    /**
     * Perform multi-word search with Boolean AND logic.
     */
    private List<Book> performMultiWordSearch(String query, PhysicalLocation physicalLocation, Pageable pageable) {
        String[] searchTerms = parseMultiWordQuery(query);
        logger.debug("Multi-word search with terms: {}", String.join(", ", searchTerms));
        
        // For multi-word queries, we use a combination approach:
        // 1. Try full-text search with the complete query first
        // 2. If that yields few results, search for individual terms and combine
        
        List<Book> fullTextResults;
        try {
            // Try MySQL full-text search first
            if (physicalLocation != null) {
                logger.debug("Attempting MySQL full-text search with location for query: {}", query);
                fullTextResults = bookRepository.searchBooksWithRelevanceByLocation(query, physicalLocation.name(), pageable);
            } else {
                logger.debug("Attempting MySQL full-text search for query: {}", query);
                fullTextResults = bookRepository.searchBooksWithRelevance(query, pageable);
            }
        } catch (Exception e) {
            // If MySQL full-text search fails (e.g., H2 database), fall back to LIKE-based search
            logger.debug("MySQL full-text search failed, falling back to H2-compatible search: {}", e.getMessage());
            if (physicalLocation != null) {
                fullTextResults = bookRepository.searchBooksWithRelevanceByLocationH2(query, physicalLocation.name(), pageable);
            } else {
                fullTextResults = bookRepository.searchBooksWithRelevanceH2(query, pageable);
            }
        }
        
        if (fullTextResults.size() >= 10) {
            // Good number of results from full-text search
            return fullTextResults;
        } else {
            // Enhance with individual term searches for better recall
            return performIndividualTermSearch(searchTerms, physicalLocation, pageable);
        }
    }
    
    /**
     * Parse multi-word query into individual search terms.
     */
    private String[] parseMultiWordQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return new String[0];
        }
        
        // Split by whitespace and filter out empty strings, stop words, and very short terms
        String[] terms = Arrays.stream(query.trim().split("\\s+"))
                     .filter(term -> !term.isEmpty())
                     .filter(term -> term.length() >= 2) // Ignore single character terms
                     .filter(term -> !COMMON_STOP_WORDS.contains(term.toLowerCase())) // Filter common stop words
                     .toArray(String[]::new);
        
        // If all terms were filtered out, return original split to prevent empty search
        if (terms.length == 0) {
            return Arrays.stream(query.trim().split("\\s+"))
                         .filter(term -> !term.isEmpty())
                         .filter(term -> term.length() >= 2)
                         .toArray(String[]::new);
        }
        
        return terms;
    }
    
    /**
     * Search for individual terms and combine results with compound scoring.
     */
    private List<Book> performIndividualTermSearch(String[] searchTerms, PhysicalLocation physicalLocation, Pageable pageable) {
        Map<Long, Book> bookMap = new HashMap<>();
        Map<Long, Double> compoundScores = new HashMap<>();
        
        // Search for each term individually
        for (String term : searchTerms) {
            List<Book> termResults;
            if (physicalLocation != null) {
                termResults = bookRepository.searchBooksWithRelevanceByLocation(term, physicalLocation.name(),
                    PageRequest.of(0, 100)); // Get more results for combining
            } else {
                termResults = bookRepository.searchBooksWithRelevance(term, 
                    PageRequest.of(0, 100)); // Get more results for combining
            }
            
            for (Book book : termResults) {
                Long bookId = book.getId();
                bookMap.put(bookId, book);
                
                // Calculate compound score (bonus for multiple term matches)
                double currentScore = compoundScores.getOrDefault(bookId, 0.0);
                double termScore = calculateTermRelevance(book, term);
                compoundScores.put(bookId, currentScore + termScore + 0.5); // Bonus for multi-match
            }
        }
        
        // Sort by compound score and return top results
        return bookMap.values().stream()
                .sorted((b1, b2) -> {
                    double score1 = compoundScores.getOrDefault(b1.getId(), 0.0);
                    double score2 = compoundScores.getOrDefault(b2.getId(), 0.0);
                    int scoreCompare = Double.compare(score2, score1); // Descending
                    return scoreCompare != 0 ? scoreCompare : b1.getTitle().compareTo(b2.getTitle());
                })
                .limit(pageable.getPageSize())
                .collect(Collectors.toList());
    }
    
    /**
     * Calculate relevance score for a specific term match in a book.
     */
    private double calculateTermRelevance(Book book, String term) {
        double score = 0.0;
        String lowerTerm = term.toLowerCase();
        
        if (book.getTitle() != null && book.getTitle().toLowerCase().contains(lowerTerm)) {
            score += 3.0;
        }
        if (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(lowerTerm)) {
            score += 2.0;
        }
        if (book.getGenre() != null && book.getGenre().toLowerCase().contains(lowerTerm)) {
            score += 1.5;
        }
        if (book.getIsbn() != null && book.getIsbn().toLowerCase().contains(lowerTerm)) {
            score += 4.0;
        }
        if (book.getPublisher() != null && book.getPublisher().toLowerCase().contains(lowerTerm)) {
            score += 1.0;
        }
        if (book.getDescription() != null && book.getDescription().toLowerCase().contains(lowerTerm)) {
            score += 1.0;
        }
        
        return score;
    }
    
    /**
     * Search books by title only.
     * Results are cached for performance.
     */
    @Cacheable(value = "booksByTitle", key = "#title + '_' + #limit")
    public SearchResponse searchByTitle(String title, int limit) {
        logger.debug("Searching books by title: '{}'", title);
        
        Pageable pageable = PageRequest.of(0, Math.min(limit, 50));
        List<Book> books = bookRepository.findByTitleContainingIgnoreCase(title, pageable);
        List<BookSearchDto> bookDtos = bookMapper.toSearchDtoList(books);
        
        SearchResponse response = new SearchResponse(bookDtos, books.size(), title);
        logger.debug("Title search completed: {}", response);
        return response;
    }
    
    /**
     * Search books by author only.
     * Results are cached for performance.
     */
    @Cacheable(value = "booksByAuthor", key = "#author + '_' + #limit")
    public SearchResponse searchByAuthor(String author, int limit) {
        logger.debug("Searching books by author: '{}'", author);
        
        Pageable pageable = PageRequest.of(0, Math.min(limit, 50));
        List<Book> books = bookRepository.findByAuthorContainingIgnoreCase(author, pageable);
        List<BookSearchDto> bookDtos = bookMapper.toSearchDtoList(books);
        
        SearchResponse response = new SearchResponse(bookDtos, books.size(), author);
        logger.debug("Author search completed: {}", response);
        return response;
    }
    
    /**
     * Search books by genre.
     */
    public SearchResponse searchByGenre(String genre, int limit) {
        logger.debug("Searching books by genre: '{}'", genre);
        
        Pageable pageable = PageRequest.of(0, Math.min(limit, 50));
        List<Book> books = bookRepository.findByGenreContainingIgnoreCase(genre, pageable);
        List<BookSearchDto> bookDtos = bookMapper.toSearchDtoList(books);
        
        SearchResponse response = new SearchResponse(bookDtos, books.size(), genre);
        logger.debug("Genre search completed: {}", response);
        return response;
    }
    
    /**
     * Get all books with pagination.
     */
    public SearchResponse getAllBooks(int limit, SortBy sortBy) {
        logger.debug("Getting all books with limit: {}, sortBy: {}", limit, sortBy);
        
        Pageable pageable = PageRequest.of(0, Math.min(limit, 50));
        List<Book> books;
        
        if (sortBy == SortBy.DATE_ADDED) {
            books = bookRepository.findAllOrderedByDateAdded(pageable);
        } else if (sortBy == SortBy.PUBLICATION_YEAR) {
            books = bookRepository.findAllOrderedByPublicationYear(pageable);
        } else {
            books = bookRepository.findAllOrderedByTitle(pageable);
        }
        
        int totalResults = (int) bookRepository.count();
        List<BookSearchDto> bookDtos = bookMapper.toSearchDtoList(books);
        
        SearchResponse response = new SearchResponse(bookDtos, totalResults, "");
        logger.debug("Get all books completed: {}", response);
        return response;
    }
}