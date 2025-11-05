package com.thehomearchive.library.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify T084 and T085 repository implementations
 */
@DataJpaTest
@ActiveProfiles("test")
class RepositoryIntegrationTest {

    @Autowired
    private BookRatingRepository bookRatingRepository;

    @Autowired
    private SearchHistoryRepository searchHistoryRepository;

    @Test
    void testBookRatingRepositoryT084() {
        // Test that BookRatingRepository is properly loaded and functional
        assertThat(bookRatingRepository).isNotNull();
        
        // Test basic count operation
        long initialCount = bookRatingRepository.count();
        assertThat(initialCount).isEqualTo(0);
        
        // Verify repository extends BaseRepository correctly
        assertThat(bookRatingRepository.findAll()).isEmpty();
    }

    @Test
    void testSearchHistoryRepositoryT085() {
        // Test that SearchHistoryRepository is properly loaded and functional  
        assertThat(searchHistoryRepository).isNotNull();
        
        // Test basic count operation
        long initialCount = searchHistoryRepository.count();
        assertThat(initialCount).isEqualTo(0);
        
        // Verify repository extends BaseRepository correctly
        assertThat(searchHistoryRepository.findAll()).isEmpty();
    }

    @Test
    void testRepositoryQueryMethods() {
        // Test that custom query methods are properly parsed by Spring Data JPA
        
        // Test BookRatingRepository methods
        assertThat(bookRatingRepository.findByUserId(1L)).isEmpty();
        assertThat(bookRatingRepository.findByBookId(1L)).isEmpty();
        assertThat(bookRatingRepository.findRatingsWithReviews()).isEmpty();
        
        // Test SearchHistoryRepository methods  
        assertThat(searchHistoryRepository.findByUserId(1L)).isEmpty();
        assertThat(searchHistoryRepository.findByQueryIgnoreCase("test")).isEmpty();
        assertThat(searchHistoryRepository.findSearchesWithNoResults()).isEmpty();
    }
}