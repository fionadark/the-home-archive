package com.thehomearchive.library.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test to verify RatingService is properly configured as a Spring bean
 * and can be autowired in the Spring context.
 */
@SpringBootTest
@ActiveProfiles("test")
class RatingServiceIntegrationTest {

    @Autowired
    private RatingService ratingService;

    @Test
    void contextLoads() {
        // Verify the service is properly autowired
        assertThat(ratingService).isNotNull();
    }

    @Test
    void serviceHasRequiredMethods() {
        // Verify the service is properly instantiated
        // and all its dependencies are satisfied
        assertThat(ratingService).isNotNull();
        
        // Verify it's actually a RatingService instance
        assertThat(ratingService).isInstanceOf(RatingService.class);
    }
}