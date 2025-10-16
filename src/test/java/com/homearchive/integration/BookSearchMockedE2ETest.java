package com.homearchive.integration;

import com.homearchive.dto.BookSearchDto;
import com.homearchive.dto.SearchResponse;
import com.homearchive.dto.SortBy;
import com.homearchive.dto.SortOrder;
import com.homearchive.service.BookSearchService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests with mocked service layer.
 * Validates the web layer integration and response handling.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookSearchMockedE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookSearchService bookSearchService;

    @Test
    @DisplayName("E2E Mock: Basic search functionality")
    void testBasicSearchFunctionality() throws Exception {
        // Create mock book data
        BookSearchDto mockBook = new BookSearchDto();
        mockBook.setId(1L);
        mockBook.setTitle("The Great Gatsby");
        mockBook.setAuthor("F. Scott Fitzgerald");
        mockBook.setGenre("Fiction");
        mockBook.setIsbn("978-0-7432-7356-5");
        mockBook.setPhysicalLocation("Living Room");

        SearchResponse mockResponse = new SearchResponse(
                Arrays.asList(mockBook),
                1,
                "Gatsby",
                SortBy.RELEVANCE,
                SortOrder.DESC
        );

        when(bookSearchService.searchBooks(any())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/books/search")
                .param("query", "Gatsby")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("Gatsby"))
                .andExpect(jsonPath("$.resultCount").value(1))
                .andExpect(jsonPath("$.totalResults").value(1))
                .andExpect(jsonPath("$.books[0].title").value("The Great Gatsby"))
                .andExpect(jsonPath("$.books[0].author").value("F. Scott Fitzgerald"));
    }

    @Test
    @DisplayName("E2E Mock: Search with sorting")
    void testSearchWithSorting() throws Exception {
        BookSearchDto book1 = new BookSearchDto();
        book1.setId(1L);
        book1.setTitle("1984");
        book1.setAuthor("George Orwell");

        BookSearchDto book2 = new BookSearchDto();
        book2.setId(2L);
        book2.setTitle("Animal Farm");
        book2.setAuthor("George Orwell");

        SearchResponse mockResponse = new SearchResponse(
                Arrays.asList(book1, book2),
                2,
                "",
                SortBy.TITLE,
                SortOrder.ASC
        );

        when(bookSearchService.searchBooks(any())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/books/search")
                .param("query", "")
                .param("sortBy", "TITLE")
                .param("sortOrder", "ASC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books").isArray())
                .andExpect(jsonPath("$.books[0].title").value("1984"))
                .andExpect(jsonPath("$.books[1].title").value("Animal Farm"))
                .andExpect(jsonPath("$.sortBy").value("TITLE"))
                .andExpect(jsonPath("$.sortOrder").value("ASC"));
    }

    @Test
    @DisplayName("E2E Mock: Search with physical location filter")
    void testSearchWithPhysicalLocationFilter() throws Exception {
        BookSearchDto mockBook = new BookSearchDto();
        mockBook.setId(1L);
        mockBook.setTitle("Pride and Prejudice");
        mockBook.setAuthor("Jane Austen");
        mockBook.setPhysicalLocation("Living Room");

        SearchResponse mockResponse = new SearchResponse(
                Arrays.asList(mockBook),
                1,
                "",
                SortBy.RELEVANCE,
                SortOrder.DESC
        );

        when(bookSearchService.searchBooks(any())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/books/search")
                .param("physicalLocation", "LIVING_ROOM")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount").value(1))
                .andExpect(jsonPath("$.books[0].physicalLocation").value("Living Room"));
    }

    @Test
    @DisplayName("E2E Mock: Search by title endpoint")
    void testSearchByTitleEndpoint() throws Exception {
        BookSearchDto mockBook = new BookSearchDto();
        mockBook.setId(1L);
        mockBook.setTitle("To Kill a Mockingbird");
        mockBook.setAuthor("Harper Lee");

        SearchResponse mockResponse = new SearchResponse(
                Arrays.asList(mockBook),
                1,
                "Mockingbird"
        );

        when(bookSearchService.searchByTitle(anyString(), anyInt())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/books/search/title")
                .param("q", "Mockingbird")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("Mockingbird"))
                .andExpect(jsonPath("$.books[0].title").value("To Kill a Mockingbird"));
    }

    @Test
    @DisplayName("E2E Mock: Search by author endpoint")
    void testSearchByAuthorEndpoint() throws Exception {
        BookSearchDto mockBook = new BookSearchDto();
        mockBook.setId(1L);
        mockBook.setTitle("1984");
        mockBook.setAuthor("George Orwell");

        SearchResponse mockResponse = new SearchResponse(
                Arrays.asList(mockBook),
                1,
                "Orwell"
        );

        when(bookSearchService.searchByAuthor(anyString(), anyInt())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/books/search/author")
                .param("q", "Orwell")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query").value("Orwell"))
                .andExpect(jsonPath("$.books[0].author").value("George Orwell"));
    }

    @Test
    @DisplayName("E2E Mock: No results found")
    void testNoResultsFound() throws Exception {
        SearchResponse mockResponse = new SearchResponse(
                Collections.emptyList(),
                0,
                "nonexistent"
        );

        when(bookSearchService.searchBooks(any())).thenReturn(mockResponse);

        mockMvc.perform(get("/api/books/search")
                .param("query", "nonexistent")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount").value(0))
                .andExpect(jsonPath("$.totalResults").value(0))
                .andExpect(jsonPath("$.books").isEmpty());
    }

    @Test
    @DisplayName("E2E Mock: Validation error handling")
    void testValidationErrorHandling() throws Exception {
        // Test invalid limit parameter - this should be caught by validation
        mockMvc.perform(get("/api/books/search")
                .param("query", "test")
                .param("limit", "999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("E2E Mock: Physical locations endpoint")
    void testPhysicalLocationsEndpoint() throws Exception {
        // This endpoint doesn't use the service, so we test it directly
        mockMvc.perform(get("/api/v1/locations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(9)); // Number of PhysicalLocation enum values
    }

    @Test
    @DisplayName("E2E Mock: Health check endpoint")
    void testHealthCheckEndpoint() throws Exception {
        // Health check endpoint doesn't use the search service
        mockMvc.perform(get("/api/books/search/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").exists());
    }
}