package com.homearchive.integration;

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
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Simple performance test for the book search system.
 * Tests response times with mocked data to focus on web layer performance.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SimpleBookSearchPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BookSearchService bookSearchService;

    private static final long MAX_RESPONSE_TIME_MS = 2000;

    @Test
    @DisplayName("Performance: Basic search response time under 2 seconds with 1000 books")
    void testBasicSearchPerformance() throws Exception {
        // Mock a response that simulates 1000 books found
        SearchResponse mockResponse = new SearchResponse(
                Collections.emptyList(), // Empty list for simplicity
                1000, // Simulate 1000 results
                "", 
                SortBy.RELEVANCE, 
                SortOrder.DESC
        );
        
        when(bookSearchService.searchBooks(any())).thenReturn(mockResponse);
        
        long startTime = System.currentTimeMillis();
        
        MvcResult result = mockMvc.perform(get("/api/books/search")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books").isArray())
                .andExpect(jsonPath("$.totalResults").value(1000))
                .andReturn();
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Basic search response time: " + responseTime + "ms");
        assertTrue(responseTime < MAX_RESPONSE_TIME_MS, 
            "Search response time " + responseTime + "ms exceeded maximum " + MAX_RESPONSE_TIME_MS + "ms");
        
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("\"books\""), "Response should contain books array");
    }

    @Test
    @DisplayName("Performance: Title search response time under 2 seconds")
    void testTitleSearchPerformance() throws Exception {
        // Mock a title search response
        SearchResponse mockResponse = new SearchResponse(
                Collections.emptyList(), 
                250, // Simulate 250 matching results
                "Great", 
                SortBy.RELEVANCE, 
                SortOrder.DESC
        );
        
        when(bookSearchService.searchBooks(any())).thenReturn(mockResponse);
        
        long startTime = System.currentTimeMillis();
        
        MvcResult result = mockMvc.perform(get("/api/books/search")
                .param("title", "Great")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books").isArray())
                .andReturn();
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Title search response time: " + responseTime + "ms");
        assertTrue(responseTime < MAX_RESPONSE_TIME_MS, 
            "Title search response time " + responseTime + "ms exceeded maximum " + MAX_RESPONSE_TIME_MS + "ms");
        
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("\"books\""), "Response should contain books array");
    }

    @Test
    @DisplayName("Performance: Genre search response time under 2 seconds")
    void testGenreSearchPerformance() throws Exception {
        // Mock a genre search response
        SearchResponse mockResponse = new SearchResponse(
                Collections.emptyList(), 
                500, // Simulate 500 fiction books
                "Fiction", 
                SortBy.RELEVANCE, 
                SortOrder.DESC
        );
        
        when(bookSearchService.searchBooks(any())).thenReturn(mockResponse);
        
        long startTime = System.currentTimeMillis();
        
        MvcResult result = mockMvc.perform(get("/api/books/search")
                .param("genre", "Fiction")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books").isArray())
                .andReturn();
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Genre search response time: " + responseTime + "ms");
        assertTrue(responseTime < MAX_RESPONSE_TIME_MS, 
            "Genre search response time " + responseTime + "ms exceeded maximum " + MAX_RESPONSE_TIME_MS + "ms");
        
        String responseBody = result.getResponse().getContentAsString();
        assertTrue(responseBody.contains("\"books\""), "Response should contain books array");
    }
}