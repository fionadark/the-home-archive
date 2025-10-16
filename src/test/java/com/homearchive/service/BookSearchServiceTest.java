package com.homearchive.service;

import com.homearchive.dto.SearchRequest;
import com.homearchive.dto.SearchResponse;
import com.homearchive.dto.SortBy;
import com.homearchive.entity.Book;
import com.homearchive.mapper.BookMapper;
import com.homearchive.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for BookSearchService.
 */
@ExtendWith(MockitoExtension.class)
class BookSearchServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @InjectMocks
    private BookSearchService bookSearchService;

    private Book testBook1;
    private Book testBook2;

    @BeforeEach
    void setUp() {
        testBook1 = new Book("The Great Gatsby", "F. Scott Fitzgerald");
        testBook1.setId(1L);
        testBook1.setGenre("Fiction");
        testBook1.setPublicationYear(1925);
        testBook1.setDateAdded(LocalDateTime.now());

        testBook2 = new Book("To Kill a Mockingbird", "Harper Lee");
        testBook2.setId(2L);
        testBook2.setGenre("Fiction");
        testBook2.setPublicationYear(1960);
        testBook2.setDateAdded(LocalDateTime.now());
    }

    @Test
    void searchBooks_EmptyQuery_ReturnsAllBooks() {
        // Arrange
        SearchRequest request = new SearchRequest("");
        List<Book> books = Arrays.asList(testBook1, testBook2);
        
        when(bookRepository.findAllOrderedByTitle(any(Pageable.class))).thenReturn(books);
        when(bookRepository.count()).thenReturn(2L);
        when(bookMapper.toSearchDtoList(books)).thenReturn(Collections.emptyList());

        // Act
        SearchResponse response = bookSearchService.searchBooks(request);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getTotalResults());
        assertEquals("", response.getQuery());
        verify(bookRepository).findAllOrderedByTitle(any(Pageable.class));
        verify(bookRepository).count();
        verify(bookMapper).toSearchDtoList(books);
    }

    @Test
    void searchBooks_WithQuery_PerformsSearch() {
        // Arrange
        SearchRequest request = new SearchRequest("Gatsby");
        List<Book> books = Arrays.asList(testBook1);
        
        when(bookRepository.searchBooksWithRelevance(eq("gatsby"), any(Pageable.class))).thenReturn(books);
        when(bookRepository.countSearchResults("gatsby")).thenReturn(1);
        when(bookMapper.toSearchDtoList(books)).thenReturn(Collections.emptyList());

        // Act
        SearchResponse response = bookSearchService.searchBooks(request);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalResults());
        assertEquals("Gatsby", response.getQuery());
        verify(bookRepository).searchBooksWithRelevance(eq("gatsby"), any(Pageable.class));
        verify(bookRepository).countSearchResults("gatsby");
        verify(bookMapper).toSearchDtoList(books);
    }

    @Test
    void searchBooks_NullRequest_HandlesGracefully() {
        // Arrange
        when(bookRepository.findAllOrderedByTitle(any(Pageable.class))).thenReturn(Collections.emptyList());
        when(bookRepository.count()).thenReturn(0L);
        when(bookMapper.toSearchDtoList(any())).thenReturn(Collections.emptyList());

        // Act
        SearchResponse response = bookSearchService.searchBooks(null);

        // Assert
        assertNotNull(response);
        assertEquals(0, response.getTotalResults());
        assertEquals("", response.getQuery());
    }

    @Test
    void searchBooks_LimitEnforced() {
        // Arrange
        SearchRequest request = new SearchRequest("test", SortBy.RELEVANCE, null, 100); // Exceeds max limit
        List<Book> books = Arrays.asList(testBook1);
        
        when(bookRepository.searchBooksWithRelevance(eq("test"), any(Pageable.class))).thenReturn(books);
        when(bookRepository.countSearchResults("test")).thenReturn(1);
        when(bookMapper.toSearchDtoList(books)).thenReturn(Collections.emptyList());

        // Act
        SearchResponse response = bookSearchService.searchBooks(request);

        // Assert
        assertNotNull(response);
        verify(bookRepository).searchBooksWithRelevance(eq("test"), argThat(pageable -> 
            pageable.getPageSize() == 50)); // Should be limited to 50
    }

    @Test
    void searchByTitle_ReturnsCorrectResults() {
        // Arrange
        String title = "Gatsby";
        int limit = 10;
        List<Book> books = Arrays.asList(testBook1);
        
        when(bookRepository.findByTitleContainingIgnoreCase(eq(title), any(Pageable.class))).thenReturn(books);
        when(bookMapper.toSearchDtoList(books)).thenReturn(Collections.emptyList());

        // Act
        SearchResponse response = bookSearchService.searchByTitle(title, limit);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalResults());
        assertEquals(title, response.getQuery());
        verify(bookRepository).findByTitleContainingIgnoreCase(eq(title), argThat(pageable -> 
            pageable.getPageSize() == limit));
    }

    @Test
    void searchByAuthor_ReturnsCorrectResults() {
        // Arrange
        String author = "Fitzgerald";
        int limit = 10;
        List<Book> books = Arrays.asList(testBook1);
        
        when(bookRepository.findByAuthorContainingIgnoreCase(eq(author), any(Pageable.class))).thenReturn(books);
        when(bookMapper.toSearchDtoList(books)).thenReturn(Collections.emptyList());

        // Act
        SearchResponse response = bookSearchService.searchByAuthor(author, limit);

        // Assert
        assertNotNull(response);
        assertEquals(1, response.getTotalResults());
        assertEquals(author, response.getQuery());
        verify(bookRepository).findByAuthorContainingIgnoreCase(eq(author), any(Pageable.class));
    }

    @Test
    void getAllBooks_ReturnsCorrectResults() {
        // Arrange
        List<Book> books = Arrays.asList(testBook1, testBook2);
        
        when(bookRepository.findAllOrderedByTitle(any(Pageable.class))).thenReturn(books);
        when(bookRepository.count()).thenReturn(2L);
        when(bookMapper.toSearchDtoList(books)).thenReturn(Collections.emptyList());

        // Act
        SearchResponse response = bookSearchService.getAllBooks(50, SortBy.TITLE);

        // Assert
        assertNotNull(response);
        assertEquals(2, response.getTotalResults());
        assertEquals("", response.getQuery());
        verify(bookRepository).findAllOrderedByTitle(any(Pageable.class));
        verify(bookRepository).count();
    }
}