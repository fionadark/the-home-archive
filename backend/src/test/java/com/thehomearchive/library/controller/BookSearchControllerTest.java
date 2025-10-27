package com.thehomearchive.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehomearchive.library.dto.search.BookSearchRequest;
import com.thehomearchive.library.dto.search.BookSearchResponse;
import com.thehomearchive.library.dto.search.BookSearchResultDto;
import com.thehomearchive.library.entity.Book;
import com.thehomearchive.library.entity.Category;
import com.thehomearchive.library.service.BookSearchService;
import com.thehomearchive.library.config.SecurityConfig;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Contract tests for BookSearchController
 * Tests the API contract for book search and discovery endpoints
 * 
 * Test Coverage:
 * - Book search by title, author, ISBN, keyword
 * - Advanced search with multiple filters
 * - Category-based filtering
 * - Pagination and sorting
 * - Search suggestions and autocomplete
 * - Error handling and validation
 * - Rate limiting for external API calls
 */
@WebMvcTest(BookSearchController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("Book Search Controller Contract Tests")
public class BookSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookSearchService bookSearchService;

    private Book sampleBook1;
    private Book sampleBook2;
    private Category fictionCategory;
    private BookSearchResultDto searchResult1;
    private BookSearchResultDto searchResult2;

    @BeforeEach
    void setUp() {
        // Setup test data
        fictionCategory = new Category();
        fictionCategory.setId(1L);
        fictionCategory.setName("Fiction");
        fictionCategory.setDescription("Fiction books");

        sampleBook1 = new Book();
        sampleBook1.setId(1L);
        sampleBook1.setTitle("The Great Gatsby");
        sampleBook1.setAuthor("F. Scott Fitzgerald");
        sampleBook1.setIsbn("978-0-7432-7356-5");
        sampleBook1.setDescription("A classic American novel set in the Jazz Age");
        sampleBook1.setPublicationYear(1925);
        sampleBook1.setPublisher("Scribner");
        sampleBook1.setPageCount(180);
        sampleBook1.setCategory(fictionCategory);
        sampleBook1.setAverageRating(4.5);
        sampleBook1.setRatingCount(150);
        sampleBook1.setCoverImageUrl("https://example.com/covers/gatsby.jpg");
        sampleBook1.setCreatedAt(LocalDateTime.now());
        sampleBook1.setUpdatedAt(LocalDateTime.now());

        sampleBook2 = new Book();
        sampleBook2.setId(2L);
        sampleBook2.setTitle("To Kill a Mockingbird");
        sampleBook2.setAuthor("Harper Lee");
        sampleBook2.setIsbn("978-0-06-112008-4");
        sampleBook2.setDescription("A gripping tale of racial injustice and childhood innocence");
        sampleBook2.setPublicationYear(1960);
        sampleBook2.setPublisher("J.B. Lippincott & Co.");
        sampleBook2.setPageCount(281);
        sampleBook2.setCategory(fictionCategory);
        sampleBook2.setAverageRating(4.8);
        sampleBook2.setRatingCount(200);
        sampleBook2.setCoverImageUrl("https://example.com/covers/mockingbird.jpg");
        sampleBook2.setCreatedAt(LocalDateTime.now());
        sampleBook2.setUpdatedAt(LocalDateTime.now());

        // Setup DTOs
        searchResult1 = new BookSearchResultDto();
        searchResult1.setId(1L);
        searchResult1.setTitle("The Great Gatsby");
        searchResult1.setAuthor("F. Scott Fitzgerald");
        searchResult1.setIsbn("978-0-7432-7356-5");
        searchResult1.setDescription("A classic American novel set in the Jazz Age");
        searchResult1.setPublicationYear(1925);
        searchResult1.setPublisher("Scribner");
        searchResult1.setPageCount(180);
        searchResult1.setCategoryName("Fiction");
        searchResult1.setAverageRating(4.5);
        searchResult1.setRatingCount(150);
        searchResult1.setCoverImageUrl("https://example.com/covers/gatsby.jpg");
        searchResult1.setInUserLibrary(false);

        searchResult2 = new BookSearchResultDto();
        searchResult2.setId(2L);
        searchResult2.setTitle("To Kill a Mockingbird");
        searchResult2.setAuthor("Harper Lee");
        searchResult2.setIsbn("978-0-06-112008-4");
        searchResult2.setDescription("A gripping tale of racial injustice and childhood innocence");
        searchResult2.setPublicationYear(1960);
        searchResult2.setPublisher("J.B. Lippincott & Co.");
        searchResult2.setPageCount(281);
        searchResult2.setCategoryName("Fiction");
        searchResult2.setAverageRating(4.8);
        searchResult2.setRatingCount(200);
        searchResult2.setCoverImageUrl("https://example.com/covers/mockingbird.jpg");
        searchResult2.setInUserLibrary(true);
    }

    @Nested
    @DisplayName("Basic Search Tests")
    class BasicSearchTests {

        @Test
        @WithMockUser
        @DisplayName("Should search books by title successfully")
        void shouldSearchBooksByTitle() throws Exception {
            // Given
            List<BookSearchResultDto> results = Arrays.asList(searchResult1);
            Page<BookSearchResultDto> page = new PageImpl<>(results, PageRequest.of(0, 20), 1);
            
            BookSearchResponse response = new BookSearchResponse();
            response.setBooks(page);
            response.setTotalElements(1);
            response.setTotalPages(1);
            response.setCurrentPage(0);
            response.setSize(20);
            response.setFirst(true);
            response.setLast(true);

            when(bookSearchService.searchBooks(any(BookSearchRequest.class), any(Pageable.class)))
                .thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/books/search")
                    .param("q", "Gatsby")
                    .param("searchType", "title")
                    .param("page", "0")
                    .param("size", "20")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.books.content", hasSize(1)))
                    .andExpect(jsonPath("$.books.content[0].title", is("The Great Gatsby")))
                    .andExpect(jsonPath("$.books.content[0].author", is("F. Scott Fitzgerald")))
                    .andExpect(jsonPath("$.books.content[0].isbn", is("978-0-7432-7356-5")))
                    .andExpect(jsonPath("$.books.content[0].inUserLibrary", is(false)))
                    .andExpect(jsonPath("$.totalElements", is(1)))
                    .andExpect(jsonPath("$.totalPages", is(1)))
                    .andExpected(jsonPath("$.currentPage", is(0)));

            verify(bookSearchService).searchBooks(any(BookSearchRequest.class), any(Pageable.class));
        }

        @Test
        @WithMockUser
        @DisplayName("Should search books by author successfully")
        void shouldSearchBooksByAuthor() throws Exception {
            // Given
            List<BookSearchResultDto> results = Arrays.asList(searchResult2);
            Page<BookSearchResultDto> page = new PageImpl<>(results, PageRequest.of(0, 20), 1);
            
            BookSearchResponse response = new BookSearchResponse();
            response.setBooks(page);
            response.setTotalElements(1);
            response.setTotalPages(1);
            response.setCurrentPage(0);
            response.setSize(20);

            when(bookSearchService.searchBooks(any(BookSearchRequest.class), any(Pageable.class)))
                .thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/books/search")
                    .param("q", "Harper Lee")
                    .param("searchType", "author")
                    .param("page", "0")
                    .param("size", "20")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.books.content", hasSize(1)))
                    .andExpect(jsonPath("$.books.content[0].title", is("To Kill a Mockingbird")))
                    .andExpect(jsonPath("$.books.content[0].author", is("Harper Lee")))
                    .andExpect(jsonPath("$.books.content[0].inUserLibrary", is(true)));

            verify(bookSearchService).searchBooks(any(BookSearchRequest.class), any(Pageable.class));
        }

        @Test
        @WithMockUser
        @DisplayName("Should search books by ISBN successfully")
        void shouldSearchBooksByIsbn() throws Exception {
            // Given
            List<BookSearchResultDto> results = Arrays.asList(searchResult1);
            Page<BookSearchResultDto> page = new PageImpl<>(results, PageRequest.of(0, 20), 1);
            
            BookSearchResponse response = new BookSearchResponse();
            response.setBooks(page);
            response.setTotalElements(1);
            response.setTotalPages(1);
            response.setCurrentPage(0);
            response.setSize(20);

            when(bookSearchService.searchBooks(any(BookSearchRequest.class), any(Pageable.class)))
                .thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/books/search")
                    .param("q", "978-0-7432-7356-5")
                    .param("searchType", "isbn")
                    .param("page", "0")
                    .param("size", "20")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.books.content", hasSize(1)))
                    .andExpect(jsonPath("$.books.content[0].isbn", is("978-0-7432-7356-5")));
        }

        @Test
        @WithMockUser
        @DisplayName("Should handle empty search results")
        void shouldHandleEmptySearchResults() throws Exception {
            // Given
            Page<BookSearchResultDto> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
            
            BookSearchResponse response = new BookSearchResponse();
            response.setBooks(emptyPage);
            response.setTotalElements(0);
            response.setTotalPages(0);
            response.setCurrentPage(0);
            response.setSize(20);

            when(bookSearchService.searchBooks(any(BookSearchRequest.class), any(Pageable.class)))
                .thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/books/search")
                    .param("q", "NonexistentBook")
                    .param("searchType", "title")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.books.content", hasSize(0)))
                    .andExpect(jsonPath("$.totalElements", is(0)))
                    .andExpect(jsonPath("$.totalPages", is(0)));
        }
    }

    @Nested
    @DisplayName("Advanced Search Tests")
    class AdvancedSearchTests {

        @Test
        @WithMockUser
        @DisplayName("Should perform advanced search with multiple filters")
        void shouldPerformAdvancedSearchWithMultipleFilters() throws Exception {
            // Given
            List<BookSearchResultDto> results = Arrays.asList(searchResult1);
            Page<BookSearchResultDto> page = new PageImpl<>(results, PageRequest.of(0, 20), 1);
            
            BookSearchResponse response = new BookSearchResponse();
            response.setBooks(page);
            response.setTotalElements(1);
            response.setTotalPages(1);
            response.setCurrentPage(0);
            response.setSize(20);

            when(bookSearchService.searchBooks(any(BookSearchRequest.class), any(Pageable.class)))
                .thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/books/search")
                    .param("q", "Gatsby")
                    .param("author", "Fitzgerald")
                    .param("category", "1")
                    .param("yearFrom", "1920")
                    .param("yearTo", "1930")
                    .param("minRating", "4.0")
                    .param("page", "0")
                    .param("size", "20")
                    .param("sort", "title")
                    .param("direction", "asc")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.books.content", hasSize(1)))
                    .andExpect(jsonPath("$.books.content[0].title", is("The Great Gatsby")))
                    .andExpect(jsonPath("$.books.content[0].publicationYear", is(1925)))
                    .andExpect(jsonPath("$.books.content[0].averageRating", is(4.5)));
        }

        @Test
        @WithMockUser
        @DisplayName("Should filter books by category")
        void shouldFilterBooksByCategory() throws Exception {
            // Given
            List<BookSearchResultDto> results = Arrays.asList(searchResult1, searchResult2);
            Page<BookSearchResultDto> page = new PageImpl<>(results, PageRequest.of(0, 20), 2);
            
            BookSearchResponse response = new BookSearchResponse();
            response.setBooks(page);
            response.setTotalElements(2);
            response.setTotalPages(1);
            response.setCurrentPage(0);
            response.setSize(20);

            when(bookSearchService.searchBooks(any(BookSearchRequest.class), any(Pageable.class)))
                .thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/books/search")
                    .param("category", "1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.books.content", hasSize(2)))
                    .andExpect(jsonPath("$.books.content[0].categoryName", is("Fiction")))
                    .andExpect(jsonPath("$.books.content[1].categoryName", is("Fiction")));
        }

        @Test
        @WithMockUser
        @DisplayName("Should search with pagination")
        void shouldSearchWithPagination() throws Exception {
            // Given
            List<BookSearchResultDto> results = Arrays.asList(searchResult2);
            Page<BookSearchResultDto> page = new PageImpl<>(results, PageRequest.of(1, 1), 2);
            
            BookSearchResponse response = new BookSearchResponse();
            response.setBooks(page);
            response.setTotalElements(2);
            response.setTotalPages(2);
            response.setCurrentPage(1);
            response.setSize(1);

            when(bookSearchService.searchBooks(any(BookSearchRequest.class), any(Pageable.class)))
                .thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/books/search")
                    .param("q", "novel")
                    .param("page", "1")
                    .param("size", "1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.books.content", hasSize(1)))
                    .andExpect(jsonPath("$.totalElements", is(2)))
                    .andExpect(jsonPath("$.totalPages", is(2)))
                    .andExpect(jsonPath("$.currentPage", is(1)));
        }
    }

    @Nested
    @DisplayName("Search Suggestions Tests")
    class SearchSuggestionsTests {

        @Test
        @WithMockUser
        @DisplayName("Should get search suggestions")
        void shouldGetSearchSuggestions() throws Exception {
            // Given
            List<String> suggestions = Arrays.asList(
                "The Great Gatsby",
                "The Great Expectations",
                "The Great Depression Era Books"
            );

            when(bookSearchService.getSearchSuggestions(anyString(), anyInt()))
                .thenReturn(suggestions);

            // When & Then
            mockMvc.perform(get("/api/books/search/suggestions")
                    .param("q", "The Great")
                    .param("limit", "5")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(3)))
                    .andExpect(jsonPath("$[0]", is("The Great Gatsby")))
                    .andExpect(jsonPath("$[1]", is("The Great Expectations")))
                    .andExpect(jsonPath("$[2]", is("The Great Depression Era Books")));

            verify(bookSearchService).getSearchSuggestions("The Great", 5);
        }

        @Test
        @WithMockUser
        @DisplayName("Should get popular searches")
        void shouldGetPopularSearches() throws Exception {
            // Given
            List<String> popularSearches = Arrays.asList(
                "Harry Potter",
                "Lord of the Rings",
                "Pride and Prejudice",
                "1984",
                "The Catcher in the Rye"
            );

            when(bookSearchService.getPopularSearches(anyInt()))
                .thenReturn(popularSearches);

            // When & Then
            mockMvc.perform(get("/api/books/search/popular")
                    .param("limit", "5")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$", hasSize(5)))
                    .andExpect(jsonPath("$[0]", is("Harry Potter")))
                    .andExpected(jsonPath("$[4]", is("The Catcher in the Rye")));

            verify(bookSearchService).getPopularSearches(5);
        }
    }

    @Nested
    @DisplayName("Validation Tests")
    class ValidationTests {

        @Test
        @WithMockUser
        @DisplayName("Should validate search parameters")
        void shouldValidateSearchParameters() throws Exception {
            // Test invalid page size
            mockMvc.perform(get("/api/books/search")
                    .param("q", "test")
                    .param("page", "0")
                    .param("size", "1000") // exceeds maximum
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("size")));

            // Test negative page number
            mockMvc.perform(get("/api/books/search")
                    .param("q", "test")
                    .param("page", "-1")
                    .param("size", "20")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("page")));
        }

        @Test
        @WithMockUser
        @DisplayName("Should validate year range parameters")
        void shouldValidateYearRangeParameters() throws Exception {
            // Test invalid year range
            mockMvc.perform(get("/api/books/search")
                    .param("q", "test")
                    .param("yearFrom", "2020")
                    .param("yearTo", "2010") // yearTo < yearFrom
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("year")));
        }

        @Test
        @WithMockUser
        @DisplayName("Should validate rating parameters")
        void shouldValidateRatingParameters() throws Exception {
            // Test invalid rating range
            mockMvc.perform(get("/api/books/search")
                    .param("q", "test")
                    .param("minRating", "6.0") // exceeds maximum (5.0)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", containsString("rating")));
        }
    }

    @Nested
    @DisplayName("External API Integration Tests")
    class ExternalApiIntegrationTests {

        @Test
        @WithMockUser
        @DisplayName("Should search external APIs when no local results found")
        void shouldSearchExternalApisWhenNoLocalResults() throws Exception {
            // Given
            Page<BookSearchResultDto> emptyLocalPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 20), 0);
            List<BookSearchResultDto> externalResults = Arrays.asList(searchResult1);
            Page<BookSearchResultDto> externalPage = new PageImpl<>(externalResults, PageRequest.of(0, 20), 1);
            
            BookSearchResponse localResponse = new BookSearchResponse();
            localResponse.setBooks(emptyLocalPage);
            localResponse.setTotalElements(0);
            
            BookSearchResponse externalResponse = new BookSearchResponse();
            externalResponse.setBooks(externalPage);
            externalResponse.setTotalElements(1);
            externalResponse.setExternalApiUsed(true);
            externalResponse.setExternalApiSource("Google Books");

            when(bookSearchService.searchBooks(any(BookSearchRequest.class), any(Pageable.class)))
                .thenReturn(externalResponse);

            // When & Then
            mockMvc.perform(get("/api/books/search")
                    .param("q", "Rare Book Title")
                    .param("includeExternal", "true")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.books.content", hasSize(1)))
                    .andExpect(jsonPath("$.externalApiUsed", is(true)))
                    .andExpect(jsonPath("$.externalApiSource", is("Google Books")));
        }
    }

    @Nested
    @DisplayName("Error Handling Tests")
    class ErrorHandlingTests {

        @Test
        @WithMockUser
        @DisplayName("Should handle service exceptions gracefully")
        void shouldHandleServiceExceptionsGracefully() throws Exception {
            // Given
            when(bookSearchService.searchBooks(any(BookSearchRequest.class), any(Pageable.class)))
                .thenThrow(new RuntimeException("Database connection failed"));

            // When & Then
            mockMvc.perform(get("/api/books/search")
                    .param("q", "test")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.message", containsString("An error occurred")))
                    .andExpect(jsonPath("$.timestamp", notNullValue()));
        }

        @Test
        @DisplayName("Should require authentication for search endpoints")
        void shouldRequireAuthenticationForSearchEndpoints() throws Exception {
            mockMvc.perform(get("/api/books/search")
                    .param("q", "test")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Rate Limiting Tests")
    class RateLimitingTests {

        @Test
        @WithMockUser
        @DisplayName("Should handle external API rate limiting")
        void shouldHandleExternalApiRateLimiting() throws Exception {
            // Given - simulate rate limit exceeded
            BookSearchResponse response = new BookSearchResponse();
            response.setBooks(new PageImpl<>(Collections.emptyList()));
            response.setTotalElements(0);
            response.setExternalApiRateLimited(true);
            response.setMessage("External API rate limit exceeded. Please try again later.");

            when(bookSearchService.searchBooks(any(BookSearchRequest.class), any(Pageable.class)))
                .thenReturn(response);

            // When & Then
            mockMvc.perform(get("/api/books/search")
                    .param("q", "test")
                    .param("includeExternal", "true")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isTooManyRequests())
                    .andExpect(jsonPath("$.message", containsString("rate limit")))
                    .andExpect(jsonPath("$.externalApiRateLimited", is(true)));
        }
    }
}