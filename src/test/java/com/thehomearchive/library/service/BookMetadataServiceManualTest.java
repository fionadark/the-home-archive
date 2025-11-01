package com.thehomearchive.library.service;

import com.thehomearchive.library.entity.Category;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * Manual test for BookMetadataService.
 * Run this test manually to verify the service works correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BookMetadataServiceManualTest {

    private static final Logger logger = LoggerFactory.getLogger(BookMetadataServiceManualTest.class);

    @Autowired
    private BookMetadataService bookMetadataService;

    @Test
    void manualTest_CreateCategory() {
        logger.info("=== Manual Test: BookMetadataService - Create Category ===");
        
        // Test: Create category
        logger.info("Test: Creating category...");
        Category category = bookMetadataService.createCategoryIfNotExists("Test Category");
        logger.info("✅ Created/found category: {} (ID: {}, Slug: {})", 
                   category.getName(), category.getId(), category.getSlug());

        // Test another category with special characters
        Category category2 = bookMetadataService.createCategoryIfNotExists("Science Fiction & Fantasy");
        logger.info("✅ Created/found category: {} (ID: {}, Slug: {})", 
                   category2.getName(), category2.getId(), category2.getSlug());

        logger.info("=== Manual Test Complete ===");
    }
}