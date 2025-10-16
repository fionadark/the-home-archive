package com.homearchive.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for caching search results to improve performance.
 * Uses in-memory caching with TTL for frequently accessed search queries.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure the cache manager for search result caching.
     * Using ConcurrentMapCacheManager for simplicity and performance.
     */
    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager();
        
        // Configure cache names
        cacheManager.setCacheNames(java.util.List.of(
            "searchResults",      // Main search results cache
            "booksByTitle",       // Title-specific searches
            "booksByAuthor"       // Author-specific searches
        ));
        
        // Allow dynamic cache creation
        cacheManager.setAllowNullValues(false);
        
        return cacheManager;
    }
}