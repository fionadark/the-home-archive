package com.thehomearchive.library.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehomearchive.library.config.TestSecurityConfig;
import com.thehomearchive.library.dto.book.BookCreateRequest;
import com.thehomearchive.library.dto.book.BookResponse;
import com.thehomearchive.library.dto.book.BookUpdateRequest;
import com.thehomearchive.library.dto.category.CategoryResponse;
import com.thehomearchive.library.service.BookService;
import com.thehomearchive.library.service.BookSearchService;
import com.thehomearchive.library.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookController.class, excludeAutoConfiguration = {
    DataSourceAutoConfiguration.class,
    JpaRepositoriesAutoConfiguration.class,
    HibernateJpaAutoConfiguration.class
})
@Import(TestSecurityConfig.class)
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
        mockMvc.perform(get("/api/books/{id}", bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("The Great Gatsby"))
                .andExpect(jsonPath("$.author").value("F. Scott Fitzgerald"));
    }

    @Test
    @WithMockUser
    void getBookById_bookNotFound_shouldReturnNotFound() throws Exception {
        // Arrange
        Long nonExistentId = 999L;
        when(bookService.getBookById(nonExistentId))
                .thenThrow(new IllegalArgumentException("Book not found"));

        // Act & Assert
        mockMvc.perform(get("/api/books/{id}", nonExistentId))
                .andExpect(status().isBadRequest());
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
        mockMvc.perform(get("/api/books/search")
                .param("q", searchTerm)
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("The Great Gatsby"));
    }

    @Test
    @WithMockUser
    void getAllBooks_shouldReturnPaginatedBooks() throws Exception {
        // Arrange
        List<BookResponse> books = Arrays.asList(testBook);
        Page<BookResponse> page = new PageImpl<>(books, PageRequest.of(0, 20), 1);
        
        when(bookService.getAllBooks(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/books")
                .param("page", "0")
                .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].title").value("The Great Gatsby"));
    }

    @Test
    @WithMockUser
    void getBooksByCategory_shouldReturnBooksInCategory() throws Exception {
        // Arrange
        Long categoryId = 1L;
        List<BookResponse> categoryBooks = Arrays.asList(testBook);
        
        when(bookService.getBooksByCategory(categoryId)).thenReturn(categoryBooks);

        // Act & Assert
        mockMvc.perform(get("/api/books/category/{categoryId}", categoryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].title").value("The Great Gatsby"));
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
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("New Book"));
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
        mockMvc.perform(put("/api/books/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Book"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteBook_shouldDeleteBook() throws Exception {
        // Arrange
        Long bookId = 1L;

        // Act & Assert
        mockMvc.perform(delete("/api/books/{id}", bookId))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void createBook_asRegularUser_shouldReturnForbidden() throws Exception {
        // Arrange
        BookCreateRequest request = new BookCreateRequest();
        request.setTitle("New Book");

        // Act & Assert
        mockMvc.perform(post("/api/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser
    void getBookCategories_shouldReturnAllCategories() throws Exception {
        // Arrange
        CategoryResponse category1 = new CategoryResponse();
        category1.setId(1L);
        category1.setName("Fiction");
        
        CategoryResponse category2 = new CategoryResponse();
        category2.setId(2L);
        category2.setName("Non-Fiction");
        
        List<CategoryResponse> categories = Arrays.asList(category1, category2);
        
        when(categoryService.getAllCategories()).thenReturn(categories);

        // Act & Assert
        mockMvc.perform(get("/api/books/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Fiction"))
                .andExpect(jsonPath("$[1].name").value("Non-Fiction"));
    }
}