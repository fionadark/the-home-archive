package com.thehomearchive.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehomearchive.library.dto.book.BookCreateRequest;
import com.thehomearchive.library.dto.book.BookResponse;
import com.thehomearchive.library.dto.book.BookUpdateRequest;
import com.thehomearchive.library.service.BookService;
import com.thehomearchive.library.service.BookSearchService;
import com.thehomearchive.library.service.CategoryService;
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
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BookService bookService;

    @MockBean
    private CategoryService categoryService;

    @MockBean
    private BookSearchService bookSearchService;

    private BookResponse testBook;

    @BeforeEach
    void setUp() {
        testBook = new BookResponse();
        testBook.setId(1L);
        testBook.setTitle("The Great Gatsby");
        testBook.setAuthor("F. Scott Fitzgerald");
        testBook.setIsbn("9780743273565");
        testBook.setDescription("A classic American novel");
        testBook.setPublicationYear(1925);
        testBook.setPublisher("Scribner");
        testBook.setPageCount(180);
        testBook.setCoverImageUrl("http://example.com/cover.jpg");
        testBook.setCategoryId(1L);
        testBook.setCategoryName("Fiction");
        testBook.setCreatedAt(LocalDateTime.now());
        testBook.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    @WithMockUser
    void getBookById_shouldReturnBook() throws Exception {
        // Arrange
        Long bookId = 1L;
        when(bookService.getBookById(bookId)).thenReturn(testBook);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("The Great Gatsby"))
                .andExpect(jsonPath("$.data.author").value("F. Scott Fitzgerald"));
    }

    @Test
    @WithMockUser
    void getBookById_bookNotFound_shouldReturnNotFound() throws Exception {
        // Arrange
        Long nonExistentId = 999L;
        when(bookService.getBookById(nonExistentId))
                .thenThrow(new IllegalArgumentException("Book not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/{id}", nonExistentId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Book not found"));
    }

    @Test
    @WithMockUser
    void searchBooks_shouldReturnSearchResults() throws Exception {
        // Arrange
        String searchTerm = "Gatsby";
        List<BookResponse> searchResults = Arrays.asList(testBook);
        Page<BookResponse> page = new PageImpl<>(searchResults, PageRequest.of(0, 20), 1);
        
        when(bookService.searchBooks(eq("Gatsby"), any(Pageable.class)))
                .thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/search")
                .param("q", searchTerm)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("The Great Gatsby"));
    }

    @Test
    @WithMockUser
    void getAllBooks_shouldReturnPaginatedBooks() throws Exception {
        // Arrange
        List<BookResponse> books = Arrays.asList(testBook);
        Page<BookResponse> page = new PageImpl<>(books, PageRequest.of(0, 20), 1);
        
        when(bookService.getAllBooks(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("The Great Gatsby"));
    }

    @Test
    @WithMockUser
    void getBooksByCategory_shouldReturnBooksInCategory() throws Exception {
        // Arrange
        Long categoryId = 1L;
        List<BookResponse> categoryBooks = Arrays.asList(testBook);
        
        when(bookService.getBooksByCategory(categoryId)).thenReturn(categoryBooks);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books")
                .param("categoryId", categoryId.toString())
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("The Great Gatsby"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createBook_shouldCreateNewBook() throws Exception {
        // Arrange
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("New Book");
        request.setAuthor("New Author");
        request.setIsbn("9781234567890");
        request.setCategoryId(1L);
        
        BookResponse createdBook = new BookResponse();
        createdBook.setId(2L);
        createdBook.setTitle("New Book");
        createdBook.setAuthor("New Author");
        
        when(bookService.createBook(any(BookCreateRequest.class))).thenReturn(createdBook);

        // Act & Assert
        mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("New Book"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void updateBook_shouldUpdateExistingBook() throws Exception {
        // Arrange
        Long bookId = 1L;
        BookUpdateRequest request = new BookUpdateRequest();
        request.setTitle("Updated Book");
        request.setAuthor("Updated Author");
        
        BookResponse updatedBook = new BookResponse();
        updatedBook.setId(bookId);
        updatedBook.setTitle("Updated Book");
        updatedBook.setAuthor("Updated Author");
        
        when(bookService.updateBook(eq(bookId), any(BookUpdateRequest.class))).thenReturn(updatedBook);

        // Act & Assert
        mockMvc.perform(put("/api/v1/books/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Updated Book"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBook_shouldDeleteBook() throws Exception {
        // Arrange
        Long bookId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/api/v1/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Book deleted successfully"));
    }

    @Test
    @WithMockUser
    void createBook_asRegularUser_shouldReturnForbidden() throws Exception {
        // Arrange
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("New Book");
        request.setAuthor("New Author");
        request.setIsbn("9781234567890");
        request.setCategoryId(1L);

        // Act & Assert
        mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

            @Test
    @WithMockUser
    void getBookCategories_shouldReturnAllCategories() throws Exception {
        // Arrange
        List<Map<String, Object>> categories = Arrays.asList(
                Map.of("id", 1L, "name", "Fiction"),
                Map.of("id", 2L, "name", "Non-Fiction")
        );
        
        when(categoryService.getAllCategoriesAsMap()).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/api/v1/books/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].name").value("Fiction"))
                .andExpect(jsonPath("$.data[1].name").value("Non-Fiction"));
    }
}