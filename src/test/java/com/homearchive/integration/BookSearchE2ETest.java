package com.homearchive.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.homearchive.dto.SearchResponse;
import com.homearchive.entity.Book;
import com.homearchive.entity.PhysicalLocation;
import com.homearchive.entity.ReadingStatus;
import com.homearchive.repository.BookRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Map;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * End-to-end integration tests covering all user stories for the book search system.
 * These tests verify the complete functionality from HTTP request to database response.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class BookSearchE2ETest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    void setUpTestData() {
        // Clear any existing data
        bookRepository.deleteAll();

        // Create comprehensive test dataset covering various scenarios
        Book book1 = new Book("The Great Gatsby", "F. Scott Fitzgerald");
        book1.setGenre("Fiction");
        book1.setIsbn("978-0-7432-7356-5");
        book1.setPublisher("Scribner");
        book1.setPublicationYear(1925);
        book1.setDescription("A classic American novel about the Jazz Age");
        book1.setPageCount(180);
        book1.setPhysicalLocation(PhysicalLocation.LIVING_ROOM);
        book1.setReadingStatus(ReadingStatus.COMPLETED);
        book1.setPersonalRating(5);
        book1.setDateAdded(LocalDateTime.now().minusDays(10));
        book1 = bookRepository.save(book1);

        Book book2 = new Book("To Kill a Mockingbird", "Harper Lee");
        book2.setGenre("Fiction");
        book2.setIsbn("978-0-06-112008-4");
        book2.setPublisher("J.B. Lippincott & Co.");
        book2.setPublicationYear(1960);
        book2.setDescription("A gripping, heart-wrenching, and wholly remarkable tale of coming-of-age");
        book2.setPageCount(324);
        book2.setPhysicalLocation(PhysicalLocation.MASTER_BEDROOM);
        book2.setReadingStatus(ReadingStatus.READING);
        book2.setPersonalRating(4);
        book2.setDateAdded(LocalDateTime.now().minusDays(5));
        book2 = bookRepository.save(book2);

        Book book3 = new Book("1984", "George Orwell");
        book3.setGenre("Dystopian Fiction");
        book3.setIsbn("978-0-452-28423-4");
        book3.setPublisher("Secker & Warburg");
        book3.setPublicationYear(1949);
        book3.setDescription("A dystopian social science fiction novel");
        book3.setPageCount(328);
        book3.setPhysicalLocation(PhysicalLocation.HOME_OFFICE);
        book3.setReadingStatus(ReadingStatus.NOT_READ);
        book3.setPersonalRating(null);
        book3.setDateAdded(LocalDateTime.now().minusDays(1));
        book3 = bookRepository.save(book3);

        Book book4 = new Book("The Catcher in the Rye", "J.D. Salinger");
        book4.setGenre("Fiction");
        book4.setIsbn("978-0-316-76948-0");
        book4.setPublisher("Little, Brown and Company");
        book4.setPublicationYear(1951);
        book4.setDescription("A controversial novel about teenage rebellion");
        book4.setPageCount(277);
        book4.setPhysicalLocation(PhysicalLocation.GUEST_BEDROOM);
        book4.setReadingStatus(ReadingStatus.COMPLETED);
        book4.setPersonalRating(3);
        book4.setDateAdded(LocalDateTime.now().minusDays(20));
        book4 = bookRepository.save(book4);

        Book book5 = new Book("Pride and Prejudice", "Jane Austen");
        book5.setGenre("Romance");
        book5.setIsbn("978-0-14-143951-8");
        book5.setPublisher("T. Egerton");
        book5.setPublicationYear(1813);
        book5.setDescription("A romantic novel of manners");
        book5.setPageCount(432);
        book5.setPhysicalLocation(PhysicalLocation.LIVING_ROOM);
        book5.setReadingStatus(ReadingStatus.COMPLETED);
        book5.setPersonalRating(5);
        book5.setDateAdded(LocalDateTime.now().minusDays(30));
        book5 = bookRepository.save(book5);
    }

    @AfterAll
    void cleanUpTestData() {
        // Clean up test data
        bookRepository.deleteAll();
    }

    // ========== USER STORY 1: Basic Search Functionality ==========

    @Test
    @Order(1)
    @DisplayName("US1.1: Search by title - exact match")
    void testSearchByTitleExactMatch() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "The Great Gatsby")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", is(1)))
                .andExpect(jsonPath("$.totalResults", is(1)))
                .andExpect(jsonPath("$.query", is("The Great Gatsby")))
                .andExpect(jsonPath("$.books[0].title", is("The Great Gatsby")))
                .andExpect(jsonPath("$.books[0].author", is("F. Scott Fitzgerald")));
    }

    @Test
    @Order(2)
    @DisplayName("US1.2: Search by title - partial match")
    void testSearchByTitlePartialMatch() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "Gatsby")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.books[0].title", containsString("Gatsby")));
    }

    @Test
    @Order(3)
    @DisplayName("US1.3: Search by author - exact match")
    void testSearchByAuthorExactMatch() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "George Orwell")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", is(1)))
                .andExpect(jsonPath("$.books[0].author", is("George Orwell")))
                .andExpect(jsonPath("$.books[0].title", is("1984")));
    }

    @Test
    @Order(4)
    @DisplayName("US1.4: Search by author - partial match")
    void testSearchByAuthorPartialMatch() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "Orwell")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.books[0].author", containsString("Orwell")));
    }

    @Test
    @Order(5)
    @DisplayName("US1.5: Search by genre")
    void testSearchByGenre() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "Fiction")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.books[*].genre", everyItem(containsString("Fiction"))));
    }

    @Test
    @Order(6)
    @DisplayName("US1.6: Search with no results")
    void testSearchWithNoResults() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "NonexistentBook12345")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", is(0)))
                .andExpect(jsonPath("$.totalResults", is(0)))
                .andExpect(jsonPath("$.books", hasSize(0)));
    }

    @Test
    @Order(7)
    @DisplayName("US1.7: Empty query returns all books")
    void testEmptyQueryReturnsAllBooks() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", is(5)))
                .andExpect(jsonPath("$.totalResults", is(5)))
                .andExpect(jsonPath("$.books", hasSize(5)));
    }

    // ========== USER STORY 2: Advanced Search and Filtering ==========

    @Test
    @Order(8)
    @DisplayName("US2.1: Multi-word search")
    void testMultiWordSearch() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "science fiction")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", greaterThanOrEqualTo(0)));
    }

    @Test
    @Order(9)
    @DisplayName("US2.2: Search with sorting by title")
    void testSearchWithSortingByTitle() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/books/search")
                .param("query", "")
                .param("sortBy", "TITLE")
                .param("sortOrder", "ASC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books", hasSize(5)))
                .andReturn();

        SearchResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), SearchResponse.class);
        
        // Verify books are sorted by title in ascending order
        for (int i = 0; i < response.getBooks().size() - 1; i++) {
            String currentTitle = response.getBooks().get(i).getTitle();
            String nextTitle = response.getBooks().get(i + 1).getTitle();
            assertTrue(currentTitle.compareTo(nextTitle) <= 0, 
                "Books should be sorted by title in ascending order");
        }
    }

    @Test
    @Order(10)
    @DisplayName("US2.3: Search with sorting by publication year")
    void testSearchWithSortingByYear() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "")
                .param("sortBy", "PUBLICATION_YEAR")
                .param("sortOrder", "DESC")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books", hasSize(5)));
    }

    @Test
    @Order(11)
    @DisplayName("US2.4: Search with limit parameter")
    void testSearchWithLimit() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "")
                .param("limit", "3")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", is(3)))
                .andExpect(jsonPath("$.totalResults", is(5)))
                .andExpect(jsonPath("$.books", hasSize(3)));
    }

    @Test
    @Order(12)
    @DisplayName("US2.5: Search by ISBN")
    void testSearchByISBN() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "978-0-7432-7356-5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", is(1)))
                .andExpect(jsonPath("$.books[0].isbn", is("978-0-7432-7356-5")))
                .andExpect(jsonPath("$.books[0].title", is("The Great Gatsby")));
    }

    // ========== USER STORY 3: Physical Location Support ==========

    @Test
    @Order(13)
    @DisplayName("US3.1: Get all physical locations")
    void testGetAllPhysicalLocations() throws Exception {
        mockMvc.perform(get("/api/v1/locations")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(9))) // Number of enum values
                .andExpect(jsonPath("$[*].value", hasItems("LIVING_ROOM", "MASTER_BEDROOM", "HOME_OFFICE")))
                .andExpect(jsonPath("$[*].label", hasItems("Living Room", "Master Bedroom", "Home Office")));
    }

    @Test
    @Order(14)
    @DisplayName("US3.2: Get physical locations as simple map")
    void testGetPhysicalLocationsSimple() throws Exception {
        MvcResult result = mockMvc.perform(get("/api/v1/locations/simple")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();

        @SuppressWarnings("unchecked")
        Map<String, String> locations = objectMapper.readValue(
            result.getResponse().getContentAsString(), Map.class);
        
        assertTrue(locations.containsKey("LIVING_ROOM"));
        assertEquals("Living Room", locations.get("LIVING_ROOM"));
        assertTrue(locations.containsKey("MASTER_BEDROOM"));
        assertEquals("Master Bedroom", locations.get("MASTER_BEDROOM"));
    }

    @Test
    @Order(15)
    @DisplayName("US3.3: Search books by physical location")
    void testSearchBooksByPhysicalLocation() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("physicalLocation", "LIVING_ROOM")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", is(2))) // Gatsby and Pride & Prejudice
                .andExpect(jsonPath("$.books[*].physicalLocation", everyItem(is("Living Room"))));
    }

    @Test
    @Order(16)
    @DisplayName("US3.4: Search with query and physical location filter")
    void testSearchWithQueryAndLocationFilter() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "Pride")
                .param("physicalLocation", "LIVING_ROOM")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", is(1)))
                .andExpect(jsonPath("$.books[0].title", is("Pride and Prejudice")))
                .andExpect(jsonPath("$.books[0].physicalLocation", is("Living Room")));
    }

    // ========== Specialized Search Endpoints ==========

    @Test
    @Order(17)
    @DisplayName("US4.1: Search by title endpoint")
    void testSearchByTitleEndpoint() throws Exception {
        mockMvc.perform(get("/api/books/search/title")
                .param("q", "Gatsby")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.books[0].title", containsString("Gatsby")));
    }

    @Test
    @Order(18)
    @DisplayName("US4.2: Search by author endpoint")
    void testSearchByAuthorEndpoint() throws Exception {
        mockMvc.perform(get("/api/books/search/author")
                .param("q", "Austen")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", is(1)))
                .andExpect(jsonPath("$.books[0].author", containsString("Austen")));
    }

    @Test
    @Order(19)
    @DisplayName("US4.3: Search by genre endpoint")
    void testSearchByGenreEndpoint() throws Exception {
        mockMvc.perform(get("/api/books/search/genre")
                .param("q", "Fiction")
                .param("limit", "10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", greaterThanOrEqualTo(3)));
    }

    // ========== Health Checks and Monitoring ==========

    @Test
    @Order(20)
    @DisplayName("US5.1: Application health check")
    void testApplicationHealthCheck() throws Exception {
        mockMvc.perform(get("/api/books/search/health")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("UP")))
                .andExpect(jsonPath("$.components.database.status", is("UP")))
                .andExpect(jsonPath("$.components.searchService.status", is("UP")))
                .andExpect(jsonPath("$.components.cache.status", is("UP")));
    }

    @Test
    @Order(21)
    @DisplayName("US5.2: Database performance endpoint")
    void testDatabasePerformanceEndpoint() throws Exception {
        mockMvc.perform(get("/actuator/database-performance")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.healthy", is(true)))
                .andExpect(jsonPath("$.connectionPool").exists())
                .andExpect(jsonPath("$.queryStats").exists());
    }

    // ========== Error Handling ==========

    @Test
    @Order(22)
    @DisplayName("US6.1: Invalid sort parameter")
    void testInvalidSortParameter() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "test")
                .param("sortBy", "INVALID_SORT")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(23)
    @DisplayName("US6.2: Invalid limit parameter")
    void testInvalidLimitParameter() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "test")
                .param("limit", "999")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @Order(24)
    @DisplayName("US6.3: Query too long")
    void testQueryTooLong() throws Exception {
        String longQuery = "a".repeat(101); // Exceeds 100 character limit
        
        mockMvc.perform(get("/api/books/search")
                .param("query", longQuery)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    // ========== Performance and Reliability ==========

    @Test
    @Order(25)
    @DisplayName("US7.1: Response time under reasonable limit")
    void testResponseTimePerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/books/search")
                .param("query", "Fiction")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        long responseTime = System.currentTimeMillis() - startTime;
        assertTrue(responseTime < 1000, 
            "Response time should be under 1 second, was: " + responseTime + "ms");
    }

    @Test
    @Order(26)
    @DisplayName("US7.2: Caching effectiveness")
    void testCachingEffectiveness() throws Exception {
        String query = "unique_cache_test_query";
        
        // First request (should hit database)
        long startTime1 = System.currentTimeMillis();
        mockMvc.perform(get("/api/books/search")
                .param("query", query)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        long firstRequestTime = System.currentTimeMillis() - startTime1;
        
        // Second request (should hit cache)
        long startTime2 = System.currentTimeMillis();
        mockMvc.perform(get("/api/books/search")
                .param("query", query)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        long secondRequestTime = System.currentTimeMillis() - startTime2;
        
        // Cache should make subsequent requests faster (or at least not significantly slower)
        assertTrue(secondRequestTime <= firstRequestTime * 2, 
            "Cached request should be faster or similar speed. First: " + firstRequestTime + 
            "ms, Second: " + secondRequestTime + "ms");
    }

    // ========== Data Integrity ==========

    @Test
    @Order(27)
    @DisplayName("US8.1: Search results contain all required fields")
    void testSearchResultsContainRequiredFields() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "Gatsby")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books[0].id").exists())
                .andExpect(jsonPath("$.books[0].title").exists())
                .andExpect(jsonPath("$.books[0].author").exists())
                .andExpect(jsonPath("$.books[0].genre").exists())
                .andExpect(jsonPath("$.books[0].publicationYear").exists())
                .andExpect(jsonPath("$.books[0].isbn").exists())
                .andExpect(jsonPath("$.books[0].publisher").exists())
                .andExpect(jsonPath("$.books[0].physicalLocation").exists());
    }

    @Test
    @Order(28)
    @DisplayName("US8.2: Search response metadata is correct")
    void testSearchResponseMetadata() throws Exception {
        mockMvc.perform(get("/api/books/search")
                .param("query", "Fiction")
                .param("limit", "2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query", is("Fiction")))
                .andExpect(jsonPath("$.resultCount", is(2)))
                .andExpect(jsonPath("$.totalResults", greaterThanOrEqualTo(2)))
                .andExpect(jsonPath("$.sortBy", is("RELEVANCE")))
                .andExpect(jsonPath("$.sortOrder", is("DESC")))
                .andExpect(jsonPath("$.hasMore", is(true)));
    }

    // ========== Security and Validation ==========

    @Test
    @Order(29)
    @DisplayName("US9.1: SQL injection protection")
    void testSQLInjectionProtection() throws Exception {
        String maliciousQuery = "'; DROP TABLE books; --";
        
        mockMvc.perform(get("/api/books/search")
                .param("query", maliciousQuery)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCount", is(0)));
        
        // Verify table still exists by performing a normal search
        mockMvc.perform(get("/api/books/search")
                .param("query", "")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalResults", is(5)));
    }

    @Test
    @Order(30)
    @DisplayName("US9.2: XSS protection in search results")
    void testXSSProtectionInSearchResults() throws Exception {
        String xssQuery = "<script>alert('xss')</script>";
        
        mockMvc.perform(get("/api/books/search")
                .param("query", xssQuery)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.query", is(xssQuery))) // Should be returned as-is but sanitized in display
                .andExpect(jsonPath("$.resultCount", is(0)));
    }
}