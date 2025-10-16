package com.homearchive.integration;

import com.homearchive.entity.Book;
import com.homearchive.entity.PhysicalLocation;
import com.homearchive.entity.ReadingStatus;
import com.homearchive.repository.BookRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Simple performance test for the book search system.
 * Tests response times with a moderate dataset size.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class SimpleBookSearchPerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookRepository bookRepository;

    private static final int TEST_BOOK_COUNT = 1000;
    private static final long MAX_RESPONSE_TIME_MS = 2000;
    
    private static final String[] SAMPLE_TITLES = {
        "The Great Gatsby", "To Kill a Mockingbird", "Pride and Prejudice", "The Catcher in the Rye",
        "1984", "Lord of the Flies", "Of Mice and Men", "The Scarlet Letter", "Wuthering Heights",
        "Jane Eyre", "The Adventures of Huckleberry Finn", "Great Expectations", "Moby Dick"
    };
    
    private static final String[] SAMPLE_AUTHORS = {
        "F. Scott Fitzgerald", "Harper Lee", "Jane Austen", "J.D. Salinger", "George Orwell",
        "William Golding", "John Steinbeck", "Nathaniel Hawthorne", "Emily Brontë", "Charlotte Brontë"
    };

    private static final String[] SAMPLE_GENRES = {
        "Fiction", "Classic Literature", "Science Fiction", "Fantasy", "Mystery", "Romance"
    };

    private void setupTestData() {
        System.out.println("Setting up performance test data with " + TEST_BOOK_COUNT + " books...");
        
        // Ensure we have a clean state
        try {
            bookRepository.deleteAll();
        } catch (Exception e) {
            // Ignore if table doesn't exist yet
            System.out.println("Initial cleanup failed (expected on first run): " + e.getMessage());
        }
        
        Random random = new Random(42); // Fixed seed for reproducible tests
        List<Book> books = new ArrayList<>();
        
        for (int i = 0; i < TEST_BOOK_COUNT; i++) {
            Book book = new Book();
            book.setTitle(SAMPLE_TITLES[i % SAMPLE_TITLES.length] + " #" + (i + 1));
            book.setAuthor(SAMPLE_AUTHORS[i % SAMPLE_AUTHORS.length]);
            book.setGenre(SAMPLE_GENRES[i % SAMPLE_GENRES.length]);
            book.setIsbn("978-" + String.format("%010d", Math.abs(random.nextLong()) % 10000000000L));
            book.setPublisher("Test Publisher " + ((i % 5) + 1));
            book.setPublicationYear(1950 + (i % 70)); // Years 1950-2019
            book.setPageCount(100 + random.nextInt(900)); // 100-999 pages
            book.setPersonalRating(1 + random.nextInt(5)); // 1-5 stars
            book.setReadingStatus(ReadingStatus.values()[i % ReadingStatus.values().length]);
            book.setPhysicalLocation(PhysicalLocation.values()[i % PhysicalLocation.values().length]);
            book.setDescription("Test description for book " + (i + 1));
            book.setDateAdded(LocalDateTime.now().minusDays(random.nextInt(365)));
            books.add(book);
            
            // Batch save every 50 books for better performance
            if (books.size() >= 50) {
                try {
                    bookRepository.saveAll(books);
                    books.clear();
                } catch (Exception e) {
                    System.err.println("Error saving books batch: " + e.getMessage());
                    throw e;
                }
            }
        }
        
        // Save remaining books
        if (!books.isEmpty()) {
            try {
                bookRepository.saveAll(books);
            } catch (Exception e) {
                System.err.println("Error saving final books batch: " + e.getMessage());
                throw e;
            }
        }
        
        long actualCount = bookRepository.count();
        System.out.println("Performance test data setup complete! Created " + actualCount + " books.");
    }

    @Test
    @DisplayName("Performance: Basic search response time under 2 seconds with 1000 books")
    void testBasicSearchPerformance() throws Exception {
        setupTestData();
        
        long startTime = System.currentTimeMillis();
        
        MvcResult result = mockMvc.perform(get("/api/books/search")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.books").isArray())
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
        setupTestData();
        
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
        setupTestData();
        
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