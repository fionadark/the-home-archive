package com.thehomearchive.library.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * Configuration for Resilience4j patterns (Circuit Breaker, Retry, Time Limiter).
 * Provides fault tolerance for external API calls.
 */
@Configuration
public class ResilienceConfig {
    
    /**
     * Circuit breaker configuration for Google Books API.
     * 
     * @return CircuitBreakerConfig for Google Books service
     */
    @Bean("googleBooksCircuitBreakerConfig")
    public CircuitBreakerConfig googleBooksCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(50) // 50% failure rate
                .waitDurationInOpenState(Duration.ofMillis(30000)) // 30 seconds wait
                .slidingWindowSize(10) // 10 requests sliding window
                .minimumNumberOfCalls(5) // Minimum 5 calls before calculating failure rate
                .permittedNumberOfCallsInHalfOpenState(3) // 3 calls in half-open state
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
    }
    
    /**
     * Circuit breaker configuration for OpenLibrary API.
     * 
     * @return CircuitBreakerConfig for OpenLibrary service
     */
    @Bean("openLibraryCircuitBreakerConfig")
    public CircuitBreakerConfig openLibraryCircuitBreakerConfig() {
        return CircuitBreakerConfig.custom()
                .failureRateThreshold(60) // 60% failure rate (more tolerant)
                .waitDurationInOpenState(Duration.ofMillis(45000)) // 45 seconds wait
                .slidingWindowSize(8) // 8 requests sliding window
                .minimumNumberOfCalls(4) // Minimum 4 calls before calculating failure rate
                .permittedNumberOfCallsInHalfOpenState(2) // 2 calls in half-open state
                .automaticTransitionFromOpenToHalfOpenEnabled(true)
                .build();
    }
    
    /**
     * Retry configuration for Google Books API.
     * 
     * @return RetryConfig for Google Books service
     */
    @Bean("googleBooksRetryConfig")
    public RetryConfig googleBooksRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(3) // Maximum 3 retry attempts
                .waitDuration(Duration.ofMillis(1000)) // 1 second wait between retries
                .retryExceptions(Exception.class) // Retry on any exception
                .build();
    }
    
    /**
     * Retry configuration for OpenLibrary API.
     * 
     * @return RetryConfig for OpenLibrary service
     */
    @Bean("openLibraryRetryConfig")
    public RetryConfig openLibraryRetryConfig() {
        return RetryConfig.custom()
                .maxAttempts(2) // Maximum 2 retry attempts
                .waitDuration(Duration.ofMillis(1500)) // 1.5 seconds wait between retries
                .retryExceptions(Exception.class) // Retry on any exception
                .build();
    }
    
    /**
     * Time limiter configuration for external API calls.
     * 
     * @return TimeLimiterConfig for all external services
     */
    @Bean("externalApiTimeLimiterConfig")
    public TimeLimiterConfig externalApiTimeLimiterConfig() {
        return TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofSeconds(10)) // 10 seconds timeout
                .cancelRunningFuture(true) // Cancel running future on timeout
                .build();
    }
}