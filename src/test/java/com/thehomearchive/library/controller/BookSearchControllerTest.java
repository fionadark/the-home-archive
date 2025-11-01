package com.thehomearchive.library.controller;

import com.thehomearchive.library.dto.book.BookResponse;
import com.thehomearchive.library.dto.search.BookSearchPageResponse;
import com.thehomearchive.library.dto.search.BookSearchRequest;
import com.thehomearchive.library.dto.search.BookSearchResponse;
import com.thehomearchive.library.dto.search.BookRatingResponse;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.entity.UserRole;
import com.thehomearchive.library.service.BookSearchService;
import com.thehomearchive.library.service.BookService;
import com.thehomearchive.library.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookSearchService bookSearchService;

    @MockBean
    private BookService bookService;

    @MockBean
    private RatingService ratingService;

    private User testUser;
    private BookResponse testBook;
    private BookSearchResponse testSearchResponse;
    private BookSearchPageResponse testPageResponse;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(UserRole.USER);

        // Setup test book
        testBook = new BookResponse();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890");
        testBook.setDescription("Test description");
        testBook.setPublicationYear(2023);
        testBook.setPublisher("Test Publisher");
        testBook.setPageCount(200);
        testBook.setCategoryId(1L);
        testBook.setCategoryName("Fiction");
        testBook.setAverageRating(4.5);
        testBook.setRatingCount(10);
        testBook.setCreatedAt(LocalDateTime.now());
        testBook.setUpdatedAt(LocalDateTime.now());

        // Setup test search response
        testSearchResponse = new BookSearchResponse();
        testSearchResponse.setId(1L);
        testSearchResponse.setTitle("Test Book");
        testSearchResponse.setAuthor("Test Author");
        testSearchResponse.setAverageRating(4.5);
        testSearchResponse.setRatingCount(10);

        // Setup test page response
        testPageResponse = new BookSearchPageResponse();
        testPageResponse.setContent(Arrays.asList(testSearchResponse));
        testPageResponse.setPage(0);
        testPageResponse.setSize(20);
        testPageResponse.setTotalElements(1L);
        testPageResponse.setTotalPages(1);
        testPageResponse.setFirst(true);
        testPageResponse.setLast(true);
    }

    // ========== SEARCH BOOKS TESTS ==========

    @WithMockUser
    @Test
    void searchBooks_BasicQuery_Success() throws Exception {
        // Given
        when(bookSearchService.searchBooks(any(BookSearchRequest.class), any()))
                .thenReturn(testPageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/search/books")
                .param("q", "test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("Test Book"))
                .andExpect(jsonPath("$.data.totalElements").value(1));

        verify(bookSearchService).searchBooks(any(BookSearchRequest.class), any());
    }

    @WithMockUser
    @Test
    void searchBooks_WithAllParameters_Success() throws Exception {
        // Given
        when(bookSearchService.searchBooks(any(BookSearchRequest.class), any()))
                .thenReturn(testPageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/search/books")
                .param("q", "test")
                .param("category", "1")
                .param("page", "0")
                .param("size", "10")
                .param("sort", "title")
                .param("direction", "asc")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray());

        verify(bookSearchService).searchBooks(any(BookSearchRequest.class), any());
    }

    @WithMockUser
    @Test
    void searchBooks_InvalidPageSize_BadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/search/books")
                .param("size", "150") // Exceeds max of 100
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser
    @Test
    void searchBooks_NegativePage_BadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/search/books")
                .param("page", "-1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser
    @Test
    void searchBooks_ServiceException_InternalError() throws Exception {
        // Given
        when(bookSearchService.searchBooks(any(BookSearchRequest.class), any()))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/api/v1/search/books")
                .param("q", "test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("An error occurred while searching books"));
    }

    // ========== GET BOOK BY ID TESTS ==========

    @WithMockUser
    @Test
    void getBookById_Success() throws Exception {
        // Given
        when(bookService.getBookById(1L)).thenReturn(testBook);

        // When & Then
        mockMvc.perform(get("/api/v1/search/books/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Book"))
                .andExpect(jsonPath("$.data.author").value("Test Author"))
                .andExpect(jsonPath("$.data.averageRating").value(4.5))
                .andExpect(jsonPath("$.data.ratingCount").value(10));

        verify(bookService).getBookById(1L);
        verify(ratingService, never()).getUserRatingForBook(any(), any()); // No user context
    }

    @WithMockUser
    @Test
    void getBookById_WithUserRating_Success() throws Exception {
        // Given
        BookRatingResponse userRating = new BookRatingResponse();
        userRating.setRating(5);
        
        when(bookService.getBookById(1L)).thenReturn(testBook);
        when(ratingService.getUserRatingForBook(1L, 1L)).thenReturn(userRating);

        // When & Then
        mockMvc.perform(get("/api/v1/search/books/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("Test Book"));

        verify(bookService).getBookById(1L);
    }

    @WithMockUser
    @Test
    void getBookById_NotFound() throws Exception {
        // Given
        when(bookService.getBookById(1L)).thenThrow(new IllegalArgumentException("Book not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/search/books/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Book not found"));

        verify(bookService).getBookById(1L);
    }

    @WithMockUser
    @Test
    void getBookById_ServiceException_InternalError() throws Exception {
        // Given
        when(bookService.getBookById(1L)).thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/api/v1/search/books/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("An error occurred while retrieving book details"));

        verify(bookService).getBookById(1L);
    }

    // ========== SEARCH SUGGESTIONS TESTS ==========

    @WithMockUser
    @Test
    void getSearchSuggestions_Success() throws Exception {
        // Given
        List<String> suggestions = Arrays.asList("test book", "test author", "testing");
        when(bookSearchService.getSearchSuggestions("test", 10)).thenReturn(suggestions);

        // When & Then
        mockMvc.perform(get("/api/v1/search/books/suggestions")
                .param("q", "test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0]").value("test book"));

        verify(bookSearchService).getSearchSuggestions("test", 10);
    }

    @WithMockUser
    @Test
    void getSearchSuggestions_InvalidLimit_BadRequest() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/search/books/suggestions")
                .param("q", "test")
                .param("limit", "100") // Exceeds max of 50
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @WithMockUser
    @Test
    void getSearchSuggestions_ServiceException_InternalError() throws Exception {
        // Given
        when(bookSearchService.getSearchSuggestions("test", 10))
                .thenThrow(new RuntimeException("Service error"));

        // When & Then
        mockMvc.perform(get("/api/v1/search/books/suggestions")
                .param("q", "test")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("An error occurred while getting search suggestions"));
    }

    // ========== POPULAR SEARCHES TESTS ==========

    @WithMockUser
    @Test
    void getPopularSearches_Success() throws Exception {
        // Given
        List<String> popularQueries = Arrays.asList("fiction", "science", "history");
        when(bookSearchService.getPopularSearchQueries(10)).thenReturn(popularQueries);

        // When & Then
        mockMvc.perform(get("/api/v1/search/books/popular")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(3))
                .andExpect(jsonPath("$.data[0]").value("fiction"));

        verify(bookSearchService).getPopularSearchQueries(10);
    }

    @WithMockUser
    @Test
    void getPopularSearches_WithLimit_Success() throws Exception {
        // Given
        List<String> popularQueries = Arrays.asList("fiction", "science");
        when(bookSearchService.getPopularSearchQueries(2)).thenReturn(popularQueries);

        // When & Then
        mockMvc.perform(get("/api/v1/search/books/popular")
                .param("limit", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.length()").value(2));

        verify(bookSearchService).getPopularSearchQueries(2);
    }

    // ========== SIMILAR BOOKS TESTS ==========

    @WithMockUser
    @Test
    void getSimilarBooks_Success() throws Exception {
        // Given
        List<BookSearchResponse> similarBooks = Arrays.asList(testSearchResponse);
        when(bookSearchService.findSimilarBooks(1L, 10)).thenReturn(similarBooks);

        // When & Then
        mockMvc.perform(get("/api/v1/search/books/1/similar")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].title").value("Test Book"));

        verify(bookSearchService).findSimilarBooks(1L, 10);
    }

    @WithMockUser
    @Test
    void getSimilarBooks_BookNotFound() throws Exception {
        // Given
        when(bookSearchService.findSimilarBooks(1L, 10))
                .thenThrow(new IllegalArgumentException("Book not found"));

        // When & Then
        mockMvc.perform(get("/api/v1/search/books/1/similar")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Book not found"));

        verify(bookSearchService).findSimilarBooks(1L, 10);
    }

    // ========== SEARCH HISTORY TESTS ==========

    @Test
    void getUserSearchHistory_WithoutAuth_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/v1/search/history")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication required to access this resource"));
    }

    @Test
    void clearUserSearchHistory_WithoutAuth_Unauthorized() throws Exception {
        // When & Then
        mockMvc.perform(delete("/api/v1/search/history")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Authentication required to access this resource"));
    }

    // ========== PARAMETER CONVERSION TESTS ==========

    @WithMockUser
    @Test
    void searchBooks_DifferentSortCriteria_Success() throws Exception {
        // Given
        when(bookSearchService.searchBooks(any(BookSearchRequest.class), any()))
                .thenReturn(testPageResponse);

        // Test all sort criteria
        String[] sortOptions = {"title", "author", "publicationYear", "rating"};
        
        for (String sortOption : sortOptions) {
            mockMvc.perform(get("/api/v1/search/books")
                    .param("sort", sortOption)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        verify(bookSearchService, times(sortOptions.length))
                .searchBooks(any(BookSearchRequest.class), any());
    }

    @WithMockUser
    @Test
    void searchBooks_DifferentSortDirections_Success() throws Exception {
        // Given
        when(bookSearchService.searchBooks(any(BookSearchRequest.class), any()))
                .thenReturn(testPageResponse);

        // Test both directions
        String[] directions = {"asc", "desc"};
        
        for (String direction : directions) {
            mockMvc.perform(get("/api/v1/search/books")
                    .param("direction", direction)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        verify(bookSearchService, times(directions.length))
                .searchBooks(any(BookSearchRequest.class), any());
    }

    @WithMockUser
    @Test
    void searchBooks_InvalidSortCriteria_DefaultsToTitle() throws Exception {
        // Given
        when(bookSearchService.searchBooks(any(BookSearchRequest.class), any()))
                .thenReturn(testPageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/search/books")
                .param("sort", "invalid")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());        verify(bookSearchService).searchBooks(any(BookSearchRequest.class), any());
    }
}