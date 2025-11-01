package com.thehomearchive.library.service;

import com.thehomearchive.library.config.MonitoringConfig.LibraryMetrics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Service for monitoring and metrics integration
 * Demonstrates how to use the MonitoringConfig in business logic
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final LibraryMetrics libraryMetrics;

    /**
     * Record user registration event
     */
    public void recordUserRegistration(String email) {
        log.info("Recording user registration for email: {}", email);
        libraryMetrics.recordUserRegistration();
    }

    /**
     * Record book search event
     */
    public void recordBookSearch(String searchType, String query) {
        log.debug("Recording book search: type={}, query={}", searchType, query);
        libraryMetrics.recordBookSearch(searchType);
    }

    /**
     * Record book addition to library
     */
    public void recordBookAdded(String source, String bookTitle) {
        log.info("Recording book addition: source={}, title={}", source, bookTitle);
        libraryMetrics.recordBookAdded(source);
    }

    /**
     * Record login attempt
     */
    public void recordLoginAttempt(String email, boolean success) {
        if (success) {
            log.info("Successful login for user: {}", email);
        } else {
            log.warn("Failed login attempt for user: {}", email);
        }
        libraryMetrics.recordLoginAttempt(success);
    }

    /**
     * Record API call metrics
     */
    public void recordApiCall(String endpoint, String method, int status, long duration) {
        log.debug("Recording API call: {} {} - status: {}, duration: {}ms", 
            method, endpoint, status, duration);
        libraryMetrics.recordApiCall(endpoint, method, status, duration);
    }

    /**
     * Get current business metrics
     */
    public java.util.Map<String, Object> getCurrentBusinessMetrics() {
        return libraryMetrics.getCurrentMetrics();
    }
}