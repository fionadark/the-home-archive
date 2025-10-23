package com.thehomearchive.library.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Configuration for Google Books API integration.
 * Provides RestTemplate and configuration properties for Google Books service.
 */
@Configuration
@ConfigurationProperties(prefix = "api.google.books")
public class GoogleBooksConfig {
    
    private String baseUrl = "https://www.googleapis.com/books/v1";
    private String apiKey;
    private int connectTimeout = 5000;
    private int readTimeout = 10000;
    private int maxResults = 40;
    private String defaultFields = "items(id,volumeInfo(title,authors,publisher,publishedDate,description,industryIdentifiers,pageCount,categories,imageLinks,language,previewLink,infoLink))";
    
    // Getters and Setters
    public String getBaseUrl() {
        return baseUrl;
    }
    
    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
    
    public String getApiKey() {
        return apiKey;
    }
    
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
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
    
    public int getMaxResults() {
        return maxResults;
    }
    
    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }
    
    public String getDefaultFields() {
        return defaultFields;
    }
    
    public void setDefaultFields(String defaultFields) {
        this.defaultFields = defaultFields;
    }
    
    /**
     * RestTemplate bean specifically configured for Google Books API calls.
     * 
     * @param restTemplateBuilder Spring's RestTemplateBuilder
     * @return Configured RestTemplate for Google Books API
     */
    @Bean("googleBooksRestTemplate")
    public RestTemplate googleBooksRestTemplate(RestTemplateBuilder restTemplateBuilder) {
        return restTemplateBuilder
                .rootUri(baseUrl)
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .readTimeout(Duration.ofMillis(readTimeout))
                .build();
    }
}