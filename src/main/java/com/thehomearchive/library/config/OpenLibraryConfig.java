package com.thehomearchive.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for OpenLibrary API integration.
 * Provides RestTemplate and configuration properties for OpenLibrary service.
 */
@Configuration
@ConfigurationProperties(prefix = "external.api.open-library")
public class OpenLibraryConfig {
    
    private String baseUrl = "https://openlibrary.org";
    private int timeout = 5000;
    
    // Derived URLs based on baseUrl
    public String getSearchUrl() {
        return baseUrl + "/search.json";
    }
    
    public String getWorksUrl() {
        return baseUrl + "/works";
    }
    
    public String getCoversUrl() {
        return "https://covers.openlibrary.org/b";
    }
    
    public int getLimit() {
        return 20; // Default limit for search results
    }
    
    public String getDefaultFields() {
        return "key,title,author_name,first_publish_year,isbn,publisher,language,subject,cover_i,cover_edition_key,number_of_pages_median";
    }
    
    // Getters and Setters
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public int getTimeout() {
        return timeout;
    }
    
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
    
    /**
     * RestTemplate bean specifically configured for OpenLibrary API calls.
     * 
     * @param restTemplateBuilder Spring's RestTemplateBuilder
     * @return Configured RestTemplate for OpenLibrary API
     */
    @Bean("openLibraryRestTemplate")
    public RestTemplate openLibraryRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(baseUrl)
                .connectTimeout(Duration.ofMillis(timeout))
                .readTimeout(Duration.ofMillis(timeout * 2)) // Read timeout longer than connect
                .build();
    }
}