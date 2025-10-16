package com.homearchive.integration;

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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Performance tests for the book search system.
 * Tests response times and concurrent access with large datasets.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class BookSearchPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    private static final int TARGET_BOOK_COUNT = 1000; // Reduced for faster test execution
    private static final long MAX_RESPONSE_TIME_MS = 2000;
    private static final String[] SAMPLE_TITLES = {
        "The Great Gatsby", "To Kill a Mockingbird", "Pride and Prejudice", "The Catcher in the Rye",
        "1984", "Lord of the Flies", "Of Mice and Men", "The Scarlet Letter", "Wuthering Heights",
        "Jane Eyre", "The Adventures of Huckleberry Finn", "Great Expectations", "Moby Dick",
        "The Lord of the Rings", "Harry Potter and the Philosopher's Stone", "The Hobbit",
        "Fahrenheit 451", "Brave New World", "Animal Farm", "The Chronicles of Narnia"
    };
    
    private static final String[] SAMPLE_AUTHORS = {
        "F. Scott Fitzgerald", "Harper Lee", "Jane Austen", "J.D. Salinger", "George Orwell",
        "William Golding", "John Steinbeck", "Nathaniel Hawthorne", "Emily Brontë", "Charlotte Brontë",
        "Mark Twain", "Charles Dickens", "Herman Melville", "J.R.R. Tolkien", "J.K. Rowling",
        "Ray Bradbury", "Aldous Huxley", "C.S. Lewis", "Ernest Hemingway", "William Shakespeare"
    };

    private static final String[] SAMPLE_GENRES = {
        "Fiction", "Classic Literature", "Science Fiction", "Fantasy", "Mystery", "Romance",
        "Thriller", "Historical Fiction", "Young Adult", "Adventure", "Horror", "Biography",
        "Non-Fiction", "Drama", "Poetry", "Philosophy", "Self-Help", "Business", "Health", "Travel"
    };

    private void setupTestData() {
        System.out.println("Setting up performance test data with " + TARGET_BOOK_COUNT + " books...");
        
        // Clear existing data
        bookRepository.deleteAll();
        
        Random random = new Random(42); // Fixed seed for reproducible tests
        List<Book> books = new ArrayList<>();
        
        for (int i = 0; i < TARGET_BOOK_COUNT; i++) {
            Book book = new Book();
            book.setTitle(SAMPLE_TITLES[i % SAMPLE_TITLES.length] + " #" + (i + 1));
            book.setAuthor(SAMPLE_AUTHORS[i % SAMPLE_AUTHORS.length]);
            book.setGenre(SAMPLE_GENRES[i % SAMPLE_GENRES.length]);
            book.setIsbn("978-" + String.format("%010d", random.nextLong() % 10000000000L));
            book.setPublisher("Test Publisher " + ((i % 10) + 1));
            book.setPublicationYear(1950 + (i % 70)); // Years 1950-2019
            book.setPageCount(100 + random.nextInt(900)); // 100-999 pages
            book.setPersonalRating(1 + random.nextInt(5)); // 1-5 stars
            book.setReadingStatus(ReadingStatus.values()[i % ReadingStatus.values().length]);
            book.setPhysicalLocation(PhysicalLocation.values()[i % PhysicalLocation.values().length]);
            book.setDescription("Test description for book " + (i + 1));
            book.setDateAdded(LocalDateTime.now().minusDays(random.nextInt(365)));
            books.add(book);
            
            // Batch save every 100 books for better performance
            if (books.size() >= 100) {
                bookRepository.saveAll(books);
                books.clear();
            }
        }
        
        // Save remaining books
        if (!books.isEmpty()) {
            bookRepository.saveAll(books);
        }
        
        System.out.println("Performance test data setup complete!");
    }

    @Test
    @DisplayName("Performance: Basic search response time under 2 seconds")
    void testBasicSearchPerformance() throws Exception {
        setupTestData();
        
        long startTime = System.currentTimeMillis();
        
        MvcResult result = mockMvc.perform(get("/api/books/search")
                .param("query", "Test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Basic search response time: " + responseTime + "ms");
        assertTrue(responseTime < MAX_RESPONSE_TIME_MS, 
            "Basic search response time should be under " + MAX_RESPONSE_TIME_MS + "ms, was: " + responseTime + "ms");
        
        // Verify we got results
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("\"totalResults\""), "Response should contain totalResults");
    }

    @Test
    @Order(2)
    @DisplayName("Performance: Empty query (get all books) response time under 2 seconds")
    void testEmptyQueryPerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        
        MvcResult result = mockMvc.perform(get("/api/books/search")
                .param("query", "")
                .param("limit", "50") // Default limit
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn();
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Empty query response time: " + responseTime + "ms");
        assertTrue(responseTime < MAX_RESPONSE_TIME_MS, 
            "Empty query response time should be under " + MAX_RESPONSE_TIME_MS + "ms, was: " + responseTime + "ms");
        
        // Verify we got the expected number of results (should return 50 with hasMore=true)
        String content = result.getResponse().getContentAsString();
        assertTrue(content.contains("\"resultCount\":50") || content.contains("\"resultCount\":" + TARGET_BOOK_COUNT), 
            "Response should contain 50 results or all results if fewer than 50 total");
    }

    @Test
    @Order(3)
    @DisplayName("Performance: Title search response time under 2 seconds")
    void testTitleSearchPerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/books/search/title")
                .param("q", "Great")
                .param("limit", "50")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Title search response time: " + responseTime + "ms");
        assertTrue(responseTime < MAX_RESPONSE_TIME_MS, 
            "Title search response time should be under " + MAX_RESPONSE_TIME_MS + "ms, was: " + responseTime + "ms");
    }

    @Test
    @Order(4)
    @DisplayName("Performance: Genre search response time under 2 seconds")
    void testGenreSearchPerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/books/search/genre")
                .param("q", "Fiction")
                .param("limit", "50")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Genre search response time: " + responseTime + "ms");
        assertTrue(responseTime < MAX_RESPONSE_TIME_MS, 
            "Genre search response time should be under " + MAX_RESPONSE_TIME_MS + "ms, was: " + responseTime + "ms");
    }

    @Test
    @Order(5)
    @DisplayName("Performance: Sorted search response time under 2 seconds")
    void testSortedSearchPerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/books/search")
                .param("query", "")
                .param("sortBy", "TITLE")
                .param("sortOrder", "ASC")
                .param("limit", "50")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Sorted search response time: " + responseTime + "ms");
        assertTrue(responseTime < MAX_RESPONSE_TIME_MS, 
            "Sorted search response time should be under " + MAX_RESPONSE_TIME_MS + "ms, was: " + responseTime + "ms");
    }

    @Test
    @Order(6)
    @DisplayName("Performance: Physical location filter response time under 2 seconds")
    void testPhysicalLocationFilterPerformance() throws Exception {
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/api/books/search")
                .param("physicalLocation", "LIVING_ROOM")
                .param("limit", "50")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        
        long responseTime = System.currentTimeMillis() - startTime;
        
        System.out.println("Physical location filter response time: " + responseTime + "ms");
        assertTrue(responseTime < MAX_RESPONSE_TIME_MS, 
            "Physical location filter response time should be under " + MAX_RESPONSE_TIME_MS + "ms, was: " + responseTime + "ms");
    }

    @Test
    @Order(7)
    @DisplayName("Performance: Concurrent access test")
    void testConcurrentAccessPerformance() throws Exception {
        int numberOfThreads = 10;
        int requestsPerThread = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        
        List<Future<Long>> futures = new ArrayList<>();
        
        System.out.println("Starting concurrent access test with " + numberOfThreads + " threads, " + 
                          requestsPerThread + " requests each...");
        
        for (int i = 0; i < numberOfThreads; i++) {
            Future<Long> future = executor.submit(() -> {
                long maxResponseTime = 0;
                try {
                    for (int j = 0; j < requestsPerThread; j++) {
                        long startTime = System.currentTimeMillis();
                        
                        mockMvc.perform(get("/api/books/search")
                                .param("query", "Book")
                                .param("limit", "20")
                                .contentType(MediaType.APPLICATION_JSON))
                                .andExpect(status().isOk());
                        
                        long responseTime = System.currentTimeMillis() - startTime;
                        maxResponseTime = Math.max(maxResponseTime, responseTime);
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Concurrent test failed", e);
                }
                return maxResponseTime;
            });
            futures.add(future);
        }
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS), 
            "Concurrent test should complete within 30 seconds");
        
        long maxResponseTimeAcrossAllThreads = 0;
        for (Future<Long> future : futures) {
            long threadMaxTime = future.get();
            maxResponseTimeAcrossAllThreads = Math.max(maxResponseTimeAcrossAllThreads, threadMaxTime);
        }
        
        System.out.println("Concurrent access max response time: " + maxResponseTimeAcrossAllThreads + "ms");
        assertTrue(maxResponseTimeAcrossAllThreads < MAX_RESPONSE_TIME_MS * 2, // Allow 2x time for concurrent access
            "Concurrent access max response time should be reasonable, was: " + maxResponseTimeAcrossAllThreads + "ms");
    }

    @Test
    @Order(8)
    @DisplayName("Performance: Database query efficiency")
    void testDatabaseQueryEfficiency() throws Exception {
        // Test that we can handle various complex queries efficiently
        String[] testQueries = {
            "Gatsby", "Fiction", "Orwell", "2023", "Test Publisher", "Adventure"
        };
        
        for (String query : testQueries) {
            long startTime = System.currentTimeMillis();
            
            mockMvc.perform(get("/api/books/search")
                    .param("query", query)
                    .param("limit", "50")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            System.out.println("Query '" + query + "' response time: " + responseTime + "ms");
            assertTrue(responseTime < MAX_RESPONSE_TIME_MS, 
                "Query '" + query + "' response time should be under " + MAX_RESPONSE_TIME_MS + "ms, was: " + responseTime + "ms");
        }
    }

    // Helper methods for test data generation
    private String generateVariedTitle(int index, Random random) {
        String baseTitle = SAMPLE_TITLES[index % SAMPLE_TITLES.length];
        
        // Add some variation to make titles more unique
        if (index >= SAMPLE_TITLES.length) {
            return baseTitle + " " + (index / SAMPLE_TITLES.length + 1);
        }
        
        return baseTitle;
    }

    private String generateIsbn(int index) {
        return String.format("978-%d-%04d-%d", 
            (index % 9) + 1, 
            index % 9999, 
            index % 10);
    }
}