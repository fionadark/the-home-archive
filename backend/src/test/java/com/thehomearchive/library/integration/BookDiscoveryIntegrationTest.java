package com.thehomearchive.library.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thehomearchive.library.dto.book.BookDto;
import com.thehomearchive.library.dto.search.BookSearchRequest;
import com.thehomearchive.library.dto.search.BookSearchResponse;
import com.thehomearchive.library.dto.book.CreateBookRequest;
import com.thehomearchive.library.entity.Book;
import com.thehomearchive.library.entity.Category;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.entity.UserBook;
import com.thehomearchive.library.repository.BookRepository;
import com.thehomearchive.library.repository.CategoryRepository;
import com.thehomearchive.library.repository.UserRepository;
import com.thehomearchive.library.repository.UserBookRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for book discovery and addition functionality
 * Tests the complete flow from searching for books to adding them to user libraries
 * 
 * Test Coverage:
 * - End-to-end book discovery flow
 * - Search to library addition integration
 * - External API integration for book discovery
 * - User workflow integration
 * - Data persistence verification
 * - Performance testing for discovery operations
 * - Cross-cutting concerns (security, validation)
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "logging.level.org.springframework.security=DEBUG"
})
@Transactional
@DisplayName("Book Discovery Integration Tests")
public class BookDiscoveryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserBookRepository userBookRepository;

    private User testUser;
    private Category fictionCategory;
    private Category nonFictionCategory;
    private Book existingBook1;
    private Book existingBook2;

    @BeforeEach
    void setUp() {
        // Clean up database
        userBookRepository.deleteAll();
        bookRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();

        // Setup test user
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedpassword");
        testUser.setCreatedAt(LocalDateTime.now());
        testUser.setUpdatedAt(LocalDateTime.now());
        testUser = userRepository.save(testUser);

        // Setup categories
        fictionCategory = new Category();
        fictionCategory.setName("Fiction");
        fictionCategory.setDescription("Fiction books");
        fictionCategory = categoryRepository.save(fictionCategory);

        nonFictionCategory = new Category();
        nonFictionCategory.setName("Non-Fiction");
        nonFictionCategory.setDescription("Non-fiction books");
        nonFictionCategory = categoryRepository.save(nonFictionCategory);

        // Setup existing books
        existingBook1 = new Book();
        existingBook1.setTitle("The Great Gatsby");
        existingBook1.setAuthor("F. Scott Fitzgerald");
        existingBook1.setIsbn("978-0-7432-7356-5");
        existingBook1.setDescription("A classic American novel set in the Jazz Age");
        existingBook1.setPublicationYear(1925);
        existingBook1.setPublisher("Scribner");
        existingBook1.setPageCount(180);
        existingBook1.setCategory(fictionCategory);
        existingBook1.setAverageRating(4.5);
        existingBook1.setRatingCount(150);
        existingBook1.setCoverImageUrl("https://example.com/covers/gatsby.jpg");
        existingBook1.setCreatedAt(LocalDateTime.now());
        existingBook1.setUpdatedAt(LocalDateTime.now());
        existingBook1 = bookRepository.save(existingBook1);

        existingBook2 = new Book();
        existingBook2.setTitle("To Kill a Mockingbird");
        existingBook2.setAuthor("Harper Lee");
        existingBook2.setIsbn("978-0-06-112008-4");
        existingBook2.setDescription("A gripping tale of racial injustice and childhood innocence");
        existingBook2.setPublicationYear(1960);
        existingBook2.setPublisher("J.B. Lippincott & Co.");
        existingBook2.setPageCount(281);
        existingBook2.setCategory(fictionCategory);
        existingBook2.setAverageRating(4.8);
        existingBook2.setRatingCount(200);
        existingBook2.setCoverImageUrl("https://example.com/covers/mockingbird.jpg");
        existingBook2.setCreatedAt(LocalDateTime.now());
        existingBook2.setUpdatedAt(LocalDateTime.now());
        existingBook2 = bookRepository.save(existingBook2);
    }

    @Nested
    @DisplayName("Complete Discovery Flow Tests")
    class CompleteDiscoveryFlowTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should complete full book discovery and addition workflow")
        void shouldCompleteFullBookDiscoveryAndAdditionWorkflow() throws Exception {
            // Step 1: Search for books
            mockMvc.perform(get("/api/books/search")
                    .param("q", "Gatsby")
                    .param("searchType", "title")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.books.content", hasSize(1)))
                    .andExpected(jsonPath("$.books.content[0].title", is("The Great Gatsby")))
                    .andExpected(jsonPath("$.books.content[0].inUserLibrary", is(false)));

            // Step 2: Add book to user library
            mockMvc.perform(post("/api/books/{bookId}/add-to-library", existingBook1.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpected(status().isCreated())
                    .andExpected(jsonPath("$.title", is("The Great Gatsby")))
                    .andExpected(jsonPath("$.author", is("F. Scott Fitzgerald")));

            // Step 3: Verify book is now in user library
            mockMvc.perform(get("/api/books/search")
                    .param("q", "Gatsby")
                    .param("searchType", "title")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.books.content[0].inUserLibrary", is(true)));

            // Step 4: Verify in user's personal library
            mockMvc.perform(get("/api/books/my-library")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.content", hasSize(1)))
                    .andExpected(jsonPath("$.content[0].title", is("The Great Gatsby")));

            // Verify database state
            List<UserBook> userBooks = userBookRepository.findByUserId(testUser.getId());
            assertThat(userBooks).hasSize(1);
            assertThat(userBooks.get(0).getBook().getTitle()).isEqualTo("The Great Gatsby");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should handle creating and adding new book in one flow")
        void shouldHandleCreatingAndAddingNewBookInOneFlow() throws Exception {
            // Step 1: Search for non-existent book
            mockMvc.perform(get("/api/books/search")
                    .param("q", "Nonexistent Book")
                    .param("searchType", "title")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.books.content", hasSize(0)));

            // Step 2: Create new book
            CreateBookRequest newBook = new CreateBookRequest();
            newBook.setTitle("New Amazing Book");
            newBook.setAuthor("New Author");
            newBook.setIsbn("978-1-234-56789-0");
            newBook.setDescription("An amazing new book");
            newBook.setPublicationYear(2023);
            newBook.setPublisher("New Publisher");
            newBook.setPageCount(300);
            newBook.setCategoryId(fictionCategory.getId());
            newBook.setCoverImageUrl("https://example.com/covers/newbook.jpg");

            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(newBook)))
                    .andDo(print())
                    .andExpected(status().isCreated())
                    .andExpected(jsonPath("$.title", is("New Amazing Book")))
                    .andExpected(jsonPath("$.author", is("New Author")));

            // Step 3: Verify book is searchable and in user library
            mockMvc.perform(get("/api/books/search")
                    .param("q", "New Amazing Book")
                    .param("searchType", "title")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.books.content", hasSize(1)))
                    .andExpected(jsonPath("$.books.content[0].inUserLibrary", is(true)));

            // Verify database state
            Book savedBook = bookRepository.findByIsbn("978-1-234-56789-0").orElse(null);
            assertThat(savedBook).isNotNull();
            assertThat(savedBook.getTitle()).isEqualTo("New Amazing Book");

            List<UserBook> userBooks = userBookRepository.findByUserId(testUser.getId());
            assertThat(userBooks).hasSize(1);
            assertThat(userBooks.get(0).getBook().getTitle()).isEqualTo("New Amazing Book");
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should handle advanced search to addition workflow")
        void shouldHandleAdvancedSearchToAdditionWorkflow() throws Exception {
            // Step 1: Advanced search with multiple filters
            mockMvc.perform(get("/api/books/search")
                    .param("author", "Harper Lee")
                    .param("category", fictionCategory.getId().toString())
                    .param("yearFrom", "1950")
                    .param("yearTo", "1970")
                    .param("minRating", "4.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.books.content", hasSize(1)))
                    .andExpected(jsonPath("$.books.content[0].title", is("To Kill a Mockingbird")))
                    .andExpected(jsonPath("$.books.content[0].author", is("Harper Lee")))
                    .andExpected(jsonPath("$.books.content[0].publicationYear", is(1960)))
                    .andExpected(jsonPath("$.books.content[0].categoryName", is("Fiction")));

            // Step 2: Add filtered result to library
            mockMvc.perform(post("/api/books/{bookId}/add-to-library", existingBook2.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isCreated())
                    .andExpected(jsonPath("$.title", is("To Kill a Mockingbird")));

            // Step 3: Verify advanced search now shows book in library
            mockMvc.perform(get("/api/books/search")
                    .param("author", "Harper Lee")
                    .param("category", fictionCategory.getId().toString())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.books.content[0].inUserLibrary", is(true)));
        }
    }

    @Nested
    @DisplayName("Multi-User Discovery Tests")
    class MultiUserDiscoveryTests {

        private User secondUser;

        @BeforeEach
        void setUpSecondUser() {
            secondUser = new User();
            secondUser.setUsername("seconduser");
            secondUser.setEmail("second@example.com");
            secondUser.setPassword("hashedpassword");
            secondUser.setCreatedAt(LocalDateTime.now());
            secondUser.setUpdatedAt(LocalDateTime.now());
            secondUser = userRepository.save(secondUser);
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should maintain user isolation in discovery workflow")
        void shouldMaintainUserIsolationInDiscoveryWorkflow() throws Exception {
            // Step 1: First user adds book to library
            mockMvc.perform(post("/api/books/{bookId}/add-to-library", existingBook1.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isCreated());

            // Step 2: Verify first user sees book in their library during search
            mockMvc.perform(get("/api/books/search")
                    .param("q", "Gatsby")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.books.content[0].inUserLibrary", is(true)));

            // Step 3: Second user should see same book but not in their library
            mockMvc.perform(get("/api/books/search")
                    .param("q", "Gatsby")
                    .contentType(MediaType.APPLICATION_JSON)
                    .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("seconduser")))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.books.content[0].inUserLibrary", is(false)));

            // Verify database isolation
            List<UserBook> firstUserBooks = userBookRepository.findByUserId(testUser.getId());
            List<UserBook> secondUserBooks = userBookRepository.findByUserId(secondUser.getId());
            
            assertThat(firstUserBooks).hasSize(1);
            assertThat(secondUserBooks).hasSize(0);
        }
    }

    @Nested
    @DisplayName("External API Integration Tests")
    class ExternalApiIntegrationTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should integrate external API search with local addition")
        void shouldIntegrateExternalApiSearchWithLocalAddition() throws Exception {
            // Step 1: Search for book not in local database (with external API enabled)
            mockMvc.perform(get("/api/books/search")
                    .param("q", "978-0-14-243723-0") // ISBN not in local DB
                    .param("searchType", "isbn")
                    .param("includeExternal", "true")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpected(status().isOk());
            // Note: Actual external API results would depend on implementation
            // This test verifies the flow can handle external results

            // Step 2: Validate ISBN from external source
            mockMvc.perform(get("/api/books/validate")
                    .param("isbn", "978-0-14-243723-0")
                    .param("enrich", "true")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk());
            // This would return enriched data from external sources

            // Step 3: Create book from external data
            CreateBookRequest externalBook = new CreateBookRequest();
            externalBook.setTitle("External Book Title");
            externalBook.setAuthor("External Author");
            externalBook.setIsbn("978-0-14-243723-0");
            externalBook.setDescription("Book from external source");
            externalBook.setPublicationYear(2020);
            externalBook.setPublisher("External Publisher");
            externalBook.setPageCount(250);
            externalBook.setCategoryId(fictionCategory.getId());

            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(externalBook)))
                    .andExpected(status().isCreated())
                    .andExpected(jsonPath("$.isbn", is("978-0-14-243723-0")));

            // Verify book is now searchable locally
            mockMvc.perform(get("/api/books/search")
                    .param("q", "External Book Title")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.books.content", hasSize(1)))
                    .andExpected(jsonPath("$.books.content[0].inUserLibrary", is(true)));
        }
    }

    @Nested
    @DisplayName("Performance and Scalability Tests")
    class PerformanceAndScalabilityTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should handle large result sets efficiently")
        void shouldHandleLargeResultSetsEfficiently() throws Exception {
            // Create multiple books for pagination testing
            for (int i = 0; i < 25; i++) {
                Book book = new Book();
                book.setTitle("Test Book " + i);
                book.setAuthor("Test Author " + i);
                book.setIsbn("978-0-000-0000-" + String.format("%02d", i));
                book.setDescription("Test description " + i);
                book.setPublicationYear(2000 + i);
                book.setPublisher("Test Publisher");
                book.setPageCount(200);
                book.setCategory(fictionCategory);
                book.setAverageRating(4.0);
                book.setRatingCount(10);
                book.setCreatedAt(LocalDateTime.now());
                book.setUpdatedAt(LocalDateTime.now());
                bookRepository.save(book);
            }

            // Test paginated search
            mockMvc.perform(get("/api/books/search")
                    .param("q", "Test Book")
                    .param("page", "0")
                    .param("size", "10")
                    .param("sort", "title")
                    .param("direction", "asc")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.books.content", hasSize(10)))
                    .andExpected(jsonPath("$.totalElements", is(25)))
                    .andExpected(jsonPath("$.totalPages", is(3)))
                    .andExpected(jsonPath("$.currentPage", is(0)));

            // Test second page
            mockMvc.perform(get("/api/books/search")
                    .param("q", "Test Book")
                    .param("page", "1")
                    .param("size", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.books.content", hasSize(10)))
                    .andExpected(jsonPath("$.currentPage", is(1)));
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should handle concurrent discovery operations")
        void shouldHandleConcurrentDiscoveryOperations() throws Exception {
            // Simulate concurrent search operations
            // This would be more meaningful with actual load testing tools
            // but demonstrates the endpoint can handle multiple requests

            for (int i = 0; i < 5; i++) {
                mockMvc.perform(get("/api/books/search")
                        .param("q", "Gatsby")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpected(status().isOk())
                        .andExpected(jsonPath("$.books.content", hasSize(1)));
            }

            // All searches should return consistent results
            assertThat(bookRepository.count()).isGreaterThan(0);
        }
    }

    @Nested
    @DisplayName("Error Recovery Tests")
    class ErrorRecoveryTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should recover gracefully from duplicate addition attempts")
        void shouldRecoverGracefullyFromDuplicateAdditionAttempts() throws Exception {
            // Step 1: Add book to library successfully
            mockMvc.perform(post("/api/books/{bookId}/add-to-library", existingBook1.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isCreated());

            // Step 2: Attempt to add same book again
            mockMvc.perform(post("/api/books/{bookId}/add-to-library", existingBook1.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isConflict())
                    .andExpected(jsonPath("$.message", containsString("already exists")));

            // Step 3: Verify search still works correctly
            mockMvc.perform(get("/api/books/search")
                    .param("q", "Gatsby")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.books.content[0].inUserLibrary", is(true)));

            // Verify only one entry in database
            List<UserBook> userBooks = userBookRepository.findByUserId(testUser.getId());
            assertThat(userBooks).hasSize(1);
        }

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should handle invalid category during book creation")
        void shouldHandleInvalidCategoryDuringBookCreation() throws Exception {
            CreateBookRequest invalidBook = new CreateBookRequest();
            invalidBook.setTitle("Invalid Category Book");
            invalidBook.setAuthor("Test Author");
            invalidBook.setIsbn("978-1-111-11111-1");
            invalidBook.setDescription("Book with invalid category");
            invalidBook.setPublicationYear(2023);
            invalidBook.setPublisher("Test Publisher");
            invalidBook.setPageCount(200);
            invalidBook.setCategoryId(999L); // Non-existent category

            // Should fail to create book
            mockMvc.perform(post("/api/books")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidBook)))
                    .andExpected(status().isNotFound())
                    .andExpected(jsonPath("$.message", containsString("Category not found")));

            // Verify book was not created
            assertThat(bookRepository.findByIsbn("978-1-111-11111-1")).isEmpty();

            // Verify search still works for existing books
            mockMvc.perform(get("/api/books/search")
                    .param("q", "Gatsby")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.books.content", hasSize(1)));
        }
    }

    @Nested
    @DisplayName("Data Consistency Tests")
    class DataConsistencyTests {

        @Test
        @WithMockUser(username = "testuser")
        @DisplayName("Should maintain data consistency across discovery operations")
        void shouldMaintainDataConsistencyAcrossDiscoveryOperations() throws Exception {
            // Step 1: Add book to library
            mockMvc.perform(post("/api/books/{bookId}/add-to-library", existingBook1.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isCreated());

            // Step 2: Update book information
            String updateRequest = """
                {
                    "description": "Updated description",
                    "pageCount": 185
                }
                """;

            mockMvc.perform(put("/api/books/{bookId}", existingBook1.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(updateRequest))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.description", is("Updated description")))
                    .andExpected(jsonPath("$.pageCount", is(185)));

            // Step 3: Verify search reflects updated information
            mockMvc.perform(get("/api/books/search")
                    .param("q", "Gatsby")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.books.content[0].description", is("Updated description")))
                    .andExpected(jsonPath("$.books.content[0].pageCount", is(185)));

            // Step 4: Verify library view shows updated information
            mockMvc.perform(get("/api/books/my-library")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpected(status().isOk())
                    .andExpected(jsonPath("$.content[0].description", is("Updated description")))
                    .andExpected(jsonPath("$.content[0].pageCount", is(185)));

            // Verify database consistency
            Book updatedBook = bookRepository.findById(existingBook1.getId()).orElse(null);
            assertThat(updatedBook).isNotNull();
            assertThat(updatedBook.getDescription()).isEqualTo("Updated description");
            assertThat(updatedBook.getPageCount()).isEqualTo(185);
        }
    }
}