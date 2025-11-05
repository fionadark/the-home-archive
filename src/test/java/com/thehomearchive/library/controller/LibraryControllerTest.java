package com.thehomearchive.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehomearchive.library.dto.book.AddToLibraryRequest;
import com.thehomearchive.library.dto.book.PersonalLibraryResponse;
import com.thehomearchive.library.dto.book.UpdateLibraryRequest;
import com.thehomearchive.library.entity.ReadingStatus;
import com.thehomearchive.library.service.LibraryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LibraryService libraryService;

    private PersonalLibraryResponse testBook;

    @BeforeEach
    void setUp() {
        testBook = new PersonalLibraryResponse();
        testBook.setId(1L);
        testBook.setBookId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setReadingStatus(ReadingStatus.UNREAD);
        testBook.setDateAdded(LocalDateTime.now());
    }

    @Test
    @WithMockUser(username = "testuser")
    void getUserLibrary_shouldReturnLibraryWithPagination() throws Exception {
        // Arrange
        List<PersonalLibraryResponse> books = Arrays.asList(testBook);
        Page<PersonalLibraryResponse> page = new PageImpl<>(books, PageRequest.of(0, 20), 1);
        
        when(libraryService.getUserLibrary(eq(1L), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/library")
                .param("page", "0")
                .param("size", "20")
                .param("sortBy", "title")
                .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("Test Book"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void searchUserLibrary_shouldReturnSearchResults() throws Exception {
        // Arrange
        String searchTerm = "Gatsby";
        List<PersonalLibraryResponse> searchResults = Arrays.asList(testBook);
        Page<PersonalLibraryResponse> page = new PageImpl<>(searchResults, PageRequest.of(0, 20), 1);
        
        when(libraryService.searchUserLibrary(eq(1L), eq("Gatsby"), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/library/search")
                .param("q", searchTerm)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("Test Book"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void addBookToLibrary_shouldAddBook() throws Exception {
        // Arrange
        Long bookId = 1L;
        AddToLibraryRequest request = new AddToLibraryRequest();
        request.setPhysicalLocation("Shelf A");
        request.setReadingStatus(ReadingStatus.UNREAD);
        
        when(libraryService.addBookToLibrary(eq(1L), eq(bookId), any(AddToLibraryRequest.class)))
                .thenReturn(testBook);

        // Act & Assert
        mockMvc.perform(post("/api/v1/library/books/{bookId}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Book"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void updateBookInLibrary_shouldUpdateBook() throws Exception {
        // Arrange
        Long libraryBookId = 1L;
        UpdateLibraryRequest request = new UpdateLibraryRequest();
        request.setPhysicalLocation("Shelf B");
        request.setReadingStatus(ReadingStatus.READING);
        
        when(libraryService.updateLibraryEntry(eq(1L), eq(libraryBookId), any(UpdateLibraryRequest.class)))
                .thenReturn(testBook);

        // Act & Assert
        mockMvc.perform(put("/api/v1/library/books/{libraryBookId}", libraryBookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Book"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void removeBookFromLibrary_shouldRemoveBook() throws Exception {
        // Arrange
        Long libraryBookId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/api/v1/library/books/{libraryBookId}", libraryBookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getLibraryByStatus_shouldReturnBooksByStatus() throws Exception {
        // Arrange
        ReadingStatus status = ReadingStatus.READING;
        List<PersonalLibraryResponse> books = Arrays.asList(testBook);
        
        when(libraryService.getBooksByReadingStatus(1L, status))
                .thenReturn(books);

        // Act & Assert
        mockMvc.perform(get("/api/v1/library/books/status/{status}", status.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("Test Book"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getLibraryStatistics_shouldReturnStatistics() throws Exception {
        // Arrange
        Map<ReadingStatus, Long> stats = new HashMap<>();
        stats.put(ReadingStatus.UNREAD, 5L);
        stats.put(ReadingStatus.READING, 2L);
        stats.put(ReadingStatus.READ, 3L);
        
        when(libraryService.getLibraryStatistics(eq(1L)))
                .thenReturn(stats);

        // Act & Assert
        mockMvc.perform(get("/api/v1/library/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.UNREAD").value(5))
                .andExpect(jsonPath("$.data.READING").value(2))
                .andExpect(jsonPath("$.data.READ").value(3));
    }
}