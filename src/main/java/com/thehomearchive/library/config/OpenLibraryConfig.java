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
@ConfigurationProperties(prefix = "api.openlibrary")
public class OpenLibraryConfig {
    
    private String baseUrl = "https://openlibrary.org";
    private String searchUrl = "https://openlibrary.org/search.json";
    private String worksUrl = "https://openlibrary.org/works";
    private String coversUrl = "https://covers.openlibrary.org/b";
    private int connectTimeout = 5000;
    private int readTimeout = 15000;
    private int limit = 20;
    private String defaultFields = "key,title,author_name,first_publish_year,isbn,publisher,language,subject,cover_i,cover_edition_key";
    
    // Getters and Setters
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getSearchUrl() {
        return searchUrl;
    }
    
    public void setSearchUrl(String searchUrl) {
        this.searchUrl = searchUrl;
    }
    
    public String getWorksUrl() {
        return worksUrl;
    }
    
    public void setWorksUrl(String worksUrl) {
        this.worksUrl = worksUrl;
    }
    
    public String getCoversUrl() {
        return coversUrl;
    }
    
    public void setCoversUrl(String coversUrl) {
        this.coversUrl = coversUrl;
    }
    
    public int getConnectTimeout() {
        return connectTimeout;
    }
    
    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }
    
    public int getReadTimeout() {
        return readTimeout;
    }
    
    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }
    
    public int getLimit() {
        return limit;
    }
    
    public void setLimit(int limit) {
        this.limit = limit;
    }
    
    public String getDefaultFields() {
        return defaultFields;
    }
    
    public void setDefaultFields(String defaultFields) {
        this.defaultFields = defaultFields;
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
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .readTimeout(Duration.ofMillis(readTimeout))
                .build();
    }
}