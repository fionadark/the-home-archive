package com.homearchive.service;

import com.homearchive.dto.BookSearchDto;
import com.homearchive.dto.SearchRequest;
import com.homearchive.dto.SearchResponse;
import com.homearchive.dto.SortBy;
import com.homearchive.entity.Book;
import com.homearchive.mapper.BookMapper;
import com.homearchive.repository.BookRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service for book search operations.
 * Implements business logic for searching books with relevance scoring.
 */
@Service
@Transactional(readOnly = true)
public class BookSearchService {
    
    private static final Logger logger = LoggerFactory.getLogger(BookSearchService.class);
    
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    
    @Autowired
    public BookSearchService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }
    
    /**
     * Search books based on the provided search request.
     */
    public SearchResponse searchBooks(SearchRequest request) {
        logger.debug("Searching books with request: {}", request);
        
        // Validate and set defaults
        if (request == null) {
            request = new SearchRequest();
        }
        
        String query = request.getTrimmedQuery();
        int limit = Math.min(request.getLimit(), 50); // Enforce max limit
        Pageable pageable = PageRequest.of(0, limit);
        
        List<Book> books;
        int totalResults;
        
        if (query.isEmpty()) {
            // Empty query - return all books ordered by title
            books = bookRepository.findAllOrderedByTitle(pageable);
            totalResults = (int) bookRepository.count();
            logger.debug("Empty query - returning {} books out of {} total", books.size(), totalResults);
        } else {
            // Perform search with relevance scoring
            books = performSearch(query, request.getSortBy(), pageable);
            totalResults = bookRepository.countSearchResults(query);
            logger.debug("Search for '{}' returned {} books out of {} total matches", 
                        query, books.size(), totalResults);
        }
        
        // Convert to DTOs
        List<BookSearchDto> bookDtos = bookMapper.toSearchDtoList(books);
        
        // Create response
        SearchResponse response = new SearchResponse(bookDtos, totalResults, query, 
                                                   request.getSortBy(), request.getSortOrder());
        
        logger.debug("Search completed: {}", response);
        return response;
    }
    
    /**
     * Perform the actual search based on query and sort criteria.
     */
    private List<Book> performSearch(String query, SortBy sortBy, Pageable pageable) {
        // Preprocess query - remove special characters and trim
        String cleanQuery = preprocessQuery(query);
        
        if (sortBy == SortBy.RELEVANCE || sortBy == null) {
            // Use full-text search with relevance scoring
            return bookRepository.searchBooksWithRelevance(cleanQuery, pageable);
        } else {
            // For other sort types, use simple search and let database handle sorting
            return bookRepository.searchByTitleAndAuthor(cleanQuery, pageable);
        }
    }
    
    /**
     * Preprocess search query to improve search results.
     */
    private String preprocessQuery(String query) {
        if (query == null || query.trim().isEmpty()) {
            return "";
        }
        
        // Remove special characters that might interfere with search
        String cleaned = query.trim()
                             .replaceAll("[\"'`]", "") // Remove quotes
                             .replaceAll("\\s+", " "); // Normalize whitespace
        
        logger.debug("Preprocessed query: '{}' -> '{}'", query, cleaned);
        return cleaned;
    }
    
    /**
     * Search books by title only.
     */
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
     */
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