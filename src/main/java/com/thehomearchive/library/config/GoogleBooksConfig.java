package com.thehomearchive.library.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuration for Google Books API integration.
 * Provides RestTemplate configuration and API settings for Google Books service.
 * 
 * Google Books API Documentation: https://developers.google.com/books/docs/v1/using
 */
@Configuration
public class GoogleBooksConfig {

    @Value("${external.api.google-books.base-url:https://www.googleapis.com/books/v1}")
    private String baseUrl;

    @Value("${external.api.google-books.api-key:}")
    private String apiKey;

    @Value("${external.api.google-books.timeout:10000}")
    private int timeout;

    @Value("${external.api.google-books.max-results:20}")
    private int maxResults;

    /**
     * Creates a configured RestTemplate for Google Books API calls.
     * 
     * @return RestTemplate configured for Google Books API
     */
    @Bean("googleBooksRestTemplate")
    public RestTemplate googleBooksRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        
        // Add timeout configuration
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().add("User-Agent", "TheHomeArchive/1.0");
            return execution.execute(request, body);
        });
        
        return restTemplate;
    }

    /**
     * Get the base URL for Google Books API.
     * 
     * @return base URL
     */
    public String getBaseUrl() {
        return baseUrl;
    }

    /**
     * Get the API key for Google Books API.
     * Note: API key is optional for basic searches but recommended for production.
     * 
     * @return API key (may be empty)
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Get the timeout for API calls in milliseconds.
     * 
     * @return timeout in milliseconds
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Get the maximum number of results per API call.
     * 
     * @return maximum results count
     */
    public int getMaxResults() {
        return maxResults;
    }

    /**
     * Check if API key is configured.
     * 
     * @return true if API key is available
     */
    public boolean hasApiKey() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }
}