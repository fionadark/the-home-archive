package com.thehomearchive.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehomearchive.library.dto.search.BookRatingRequest;
import com.thehomearchive.library.dto.search.BookRatingResponse;
import com.thehomearchive.library.exception.DuplicateRatingException;
import com.thehomearchive.library.exception.ResourceNotFoundException;
import com.thehomearchive.library.service.RatingService;
import com.thehomearchive.library.util.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for RatingController.
 * Tests all endpoints with various scenarios including success and error cases.
 */
@ExtendWith(MockitoExtension.class)
class RatingControllerUnitTest {
    
    @Mock
    private RatingService ratingService;
    
    @InjectMocks
    private RatingController ratingController;
    
    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    
    private static final Long USER_ID = 1L;
    private static final Long BOOK_ID = 1L;
    private static final Long RATING_ID = 1L;
    
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(ratingController).build();
        objectMapper = new ObjectMapper();
    }
    
    // ========== Create Rating Tests ==========
    
    @Test
    void rateBook_Success() throws Exception {
        // Given
        BookRatingRequest request = new BookRatingRequest(5, "Excellent book!");
        BookRatingResponse response = createSampleRatingResponse();
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
            when(ratingService.createRating(eq(USER_ID), eq(BOOK_ID), any(BookRatingRequest.class))).thenReturn(response);
            
            // When & Then
            mockMvc.perform(post("/api/books/{id}/ratings", BOOK_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(RATING_ID))
                    .andExpect(jsonPath("$.rating").value(5))
                    .andExpect(jsonPath("$.review").value("Excellent book!"));
            
            verify(ratingService).createRating(eq(USER_ID), eq(BOOK_ID), any(BookRatingRequest.class));
        }
    }
    
    @Test
    void rateBook_DuplicateRating_Conflict() throws Exception {
        // Given
        BookRatingRequest request = new BookRatingRequest(5, "Excellent book!");
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
            when(ratingService.createRating(eq(USER_ID), eq(BOOK_ID), any(BookRatingRequest.class)))
                .thenThrow(new DuplicateRatingException("User already rated this book"));
            
            // When & Then
            mockMvc.perform(post("/api/books/{id}/ratings", BOOK_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
            
            verify(ratingService).createRating(eq(USER_ID), eq(BOOK_ID), any(BookRatingRequest.class));
        }
    }
    
    @Test
    void rateBook_BookNotFound_NotFound() throws Exception {
        // Given
        BookRatingRequest request = new BookRatingRequest(5, "Excellent book!");
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
            when(ratingService.createRating(eq(USER_ID), eq(BOOK_ID), any(BookRatingRequest.class)))
                .thenThrow(new ResourceNotFoundException("Book not found"));
            
            // When & Then
            mockMvc.perform(post("/api/books/{id}/ratings", BOOK_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
    
    @Test
    void rateBook_InvalidRating_BadRequest() throws Exception {
        // Given
        BookRatingRequest request = new BookRatingRequest(6, "Invalid rating"); // Rating > 5
        
        // When & Then
        mockMvc.perform(post("/api/books/{id}/ratings", BOOK_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    void rateBook_ServiceException_InternalError() throws Exception {
        // Given
        BookRatingRequest request = new BookRatingRequest(5, "Excellent book!");
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
            when(ratingService.createRating(eq(USER_ID), eq(BOOK_ID), any(BookRatingRequest.class)))
                .thenThrow(new RuntimeException("Service error"));
            
            // When & Then
            mockMvc.perform(post("/api/books/{id}/ratings", BOOK_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isInternalServerError());
        }
    }
    
    // ========== Update Rating Tests ==========
    
    @Test
    void updateRating_Success() throws Exception {
        // Given
        BookRatingRequest request = new BookRatingRequest(4, "Updated review");
        BookRatingResponse response = createSampleRatingResponse();
        response.setRating(4);
        response.setReview("Updated review");
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
            when(ratingService.updateRating(eq(USER_ID), eq(BOOK_ID), any(BookRatingRequest.class))).thenReturn(response);
            
            // When & Then
            mockMvc.perform(put("/api/books/{id}/ratings", BOOK_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.rating").value(4))
                    .andExpect(jsonPath("$.review").value("Updated review"));
            
            verify(ratingService).updateRating(eq(USER_ID), eq(BOOK_ID), any(BookRatingRequest.class));
        }
    }
    
    @Test
    void updateRating_NotFound() throws Exception {
        // Given
        BookRatingRequest request = new BookRatingRequest(4, "Updated review");
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
            when(ratingService.updateRating(eq(USER_ID), eq(BOOK_ID), any(BookRatingRequest.class)))
                .thenThrow(new ResourceNotFoundException("Rating not found"));
            
            // When & Then
            mockMvc.perform(put("/api/books/{id}/ratings", BOOK_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }
    }
    
    // ========== Delete Rating Tests ==========
    
    @Test
    void deleteRating_Success() throws Exception {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
            doNothing().when(ratingService).deleteRating(USER_ID, BOOK_ID);
            
            // When & Then
            mockMvc.perform(delete("/api/books/{id}/ratings", BOOK_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Rating deleted successfully"));
            
            verify(ratingService).deleteRating(USER_ID, BOOK_ID);
        }
    }
    
    @Test
    void deleteRating_NotFound() throws Exception {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
            doThrow(new ResourceNotFoundException("Rating not found"))
                .when(ratingService).deleteRating(USER_ID, BOOK_ID);
            
            // When & Then
            mockMvc.perform(delete("/api/books/{id}/ratings", BOOK_ID))
                    .andExpect(status().isNotFound());
        }
    }
    
    // ========== Get Book Ratings Tests ==========
    
    @Test
    void getBookRatings_Success() throws Exception {
        // Given
        List<BookRatingResponse> ratings = List.of(
            createSampleRatingResponse(),
            createSampleRatingResponse(2L, 4, "Good book")
        );
        Page<BookRatingResponse> page = new PageImpl<>(ratings);
        
        when(ratingService.getBookRatings(BOOK_ID, 0, 10, "createdAt", "desc"))
            .thenReturn(page);
        
        // When & Then
        mockMvc.perform(get("/api/books/{id}/ratings", BOOK_ID)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.pagination.totalElements").value(2));
        
        verify(ratingService).getBookRatings(BOOK_ID, 0, 10, "createdAt", "desc");
    }
    
    @Test
    void getBookRatings_WithCustomParameters() throws Exception {
        // Given
        Page<BookRatingResponse> page = new PageImpl<>(List.of());
        
        when(ratingService.getBookRatings(BOOK_ID, 1, 5, "rating", "asc"))
            .thenReturn(page);
        
        // When & Then
        mockMvc.perform(get("/api/books/{id}/ratings", BOOK_ID)
                .param("page", "1")
                .param("size", "5")
                .param("sortBy", "rating")
                .param("sortDirection", "asc"))
                .andExpect(status().isOk());
        
        verify(ratingService).getBookRatings(BOOK_ID, 1, 5, "rating", "asc");
    }
    
    @Test
    void getBookRatings_BookNotFound() throws Exception {
        when(ratingService.getBookRatings(BOOK_ID, 0, 10, "createdAt", "desc"))
            .thenThrow(new ResourceNotFoundException("Book not found"));
        
        // When & Then
        mockMvc.perform(get("/api/books/{id}/ratings", BOOK_ID))
                .andExpect(status().isNotFound());
    }
    
    // ========== Get User's Rating Tests ==========
    
    @Test
    void getMyRating_Success() throws Exception {
        // Given
        BookRatingResponse response = createSampleRatingResponse();
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
            when(ratingService.getUserRatingForBook(USER_ID, BOOK_ID)).thenReturn(response);
            
            // When & Then
            mockMvc.perform(get("/api/books/{id}/ratings/my", BOOK_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(RATING_ID))
                    .andExpect(jsonPath("$.rating").value(5));
            
            verify(ratingService).getUserRatingForBook(USER_ID, BOOK_ID);
        }
    }
    
    @Test
    void getMyRating_NotFound() throws Exception {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
            when(ratingService.getUserRatingForBook(USER_ID, BOOK_ID))
                .thenThrow(new ResourceNotFoundException("Rating not found"));
            
            // When & Then
            mockMvc.perform(get("/api/books/{id}/ratings/my", BOOK_ID))
                    .andExpect(status().isNotFound());
        }
    }
    
    // ========== Get Current User Ratings Tests ==========
    
    @Test
    void getCurrentUserRatings_Success() throws Exception {
        // Given
        List<BookRatingResponse> ratings = List.of(createSampleRatingResponse());
        Page<BookRatingResponse> page = new PageImpl<>(ratings);
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
            when(ratingService.getUserRatings(USER_ID, 0, 10, "createdAt", "desc"))
                .thenReturn(page);
            
            // When & Then
            mockMvc.perform(get("/api/users/ratings"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1));
            
            verify(ratingService).getUserRatings(USER_ID, 0, 10, "createdAt", "desc");
        }
    }
    
    // ========== Create or Update Rating Tests ==========
    
    @Test
    void createOrUpdateRating_Success() throws Exception {
        // Given
        BookRatingRequest request = new BookRatingRequest(5, "Excellent book!");
        BookRatingResponse response = createSampleRatingResponse();
        
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
            when(ratingService.createOrUpdateRating(eq(USER_ID), eq(BOOK_ID), any(BookRatingRequest.class))).thenReturn(response);
            
            // When & Then
            mockMvc.perform(put("/api/books/{id}/ratings/upsert", BOOK_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(RATING_ID));
            
            verify(ratingService).createOrUpdateRating(eq(USER_ID), eq(BOOK_ID), any(BookRatingRequest.class));
        }
    }
    
    // ========== Check User Rating Tests ==========
    
    @Test
    void hasUserRatedBook_HasRated() throws Exception {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
            when(ratingService.hasUserRatedBook(USER_ID, BOOK_ID)).thenReturn(true);
            
            // When & Then
            mockMvc.perform(get("/api/books/{id}/ratings/check", BOOK_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User has rated this book"));
            
            verify(ratingService).hasUserRatedBook(USER_ID, BOOK_ID);
        }
    }
    
    @Test
    void hasUserRatedBook_HasNotRated() throws Exception {
        try (MockedStatic<SecurityUtils> mockedSecurityUtils = mockStatic(SecurityUtils.class)) {
            mockedSecurityUtils.when(SecurityUtils::getCurrentUserId).thenReturn(USER_ID);
            when(ratingService.hasUserRatedBook(USER_ID, BOOK_ID)).thenReturn(false);
            
            // When & Then
            mockMvc.perform(get("/api/books/{id}/ratings/check", BOOK_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("User has not rated this book"));
        }
    }
    
    // ========== Helper Methods ==========
    
    private BookRatingResponse createSampleRatingResponse() {
        return createSampleRatingResponse(RATING_ID, 5, "Excellent book!");
    }
    
    private BookRatingResponse createSampleRatingResponse(Long id, Integer rating, String review) {
        BookRatingResponse response = new BookRatingResponse();
        response.setId(id);
        response.setBookId(BOOK_ID);
        response.setUserId(USER_ID);
        response.setRating(rating);
        response.setReview(review);
        response.setBookTitle("Sample Book");
        response.setUserFullName("John Doe");
        response.setCreatedAt(LocalDateTime.now());
        response.setUpdatedAt(LocalDateTime.now());
        return response;
    }
}