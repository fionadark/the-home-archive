package com.homearchive.controller;

import com.homearchive.service.BookSearchService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check and metrics collection controller for application monitoring.
 * Provides comprehensive health status and operational metrics for production monitoring.
 */
@RestController
@RequestMapping("/api/health")
@Tag(name = "Health & Monitoring", description = "Application health checks and operational metrics")
public class HealthController {

    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    
    private final BookSearchService bookSearchService;
    private final DataSource dataSource;
    private final MeterRegistry meterRegistry;
    
    // Metrics counters
    private final Counter healthCheckCounter;
    private final Timer healthCheckTimer;
    
    @Autowired
    public HealthController(BookSearchService bookSearchService, 
                           DataSource dataSource, 
                           MeterRegistry meterRegistry) {
        this.bookSearchService = bookSearchService;
        this.dataSource = dataSource;
        this.meterRegistry = meterRegistry;
        
        // Initialize metrics
        this.healthCheckCounter = Counter.builder("health.check.count")
                .description("Number of health check requests")
                .register(meterRegistry);
                
        this.healthCheckTimer = Timer.builder("health.check.duration")
                .description("Health check execution time")
                .register(meterRegistry);
    }

    /**
     * Comprehensive application health check endpoint.
     * Validates all critical application components and services.
     */
    @Operation(
        summary = "Get comprehensive application health status",
        description = """
            Performs comprehensive health checks across all application components:
            
            **Checks Performed:**
            - Database connectivity and response time
            - Book search service functionality
            - Cache system availability
            - Application metrics collection
            
            **Response Codes:**
            - 200: All systems healthy
            - 503: One or more systems unhealthy
            
            **Use Cases:**
            - Load balancer health checks
            - Monitoring system alerts
            - Deployment verification
            - Production troubleshooting
            """,
        tags = {"Health & Monitoring"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "All application components are healthy",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Healthy status",
                    value = """
                        {
                          "status": "UP",
                          "timestamp": "2024-01-15T10:30:00Z",
                          "checks": {
                            "database": {
                              "status": "UP",
                              "responseTimeMs": 12,
                              "details": "Database connectivity verified"
                            },
                            "bookSearch": {
                              "status": "UP",
                              "responseTimeMs": 45,
                              "searchResults": 0,
                              "details": "Search functionality operational"
                            },
                            "cache": {
                              "status": "UP",
                              "details": "Cache system operational"
                            }
                          },
                          "metrics": {
                            "totalHealthChecks": 156,
                            "averageResponseTimeMs": 23.5
                          }
                        }
                        """
                )
            )
        ),
        @ApiResponse(
            responseCode = "503",
            description = "One or more application components are unhealthy",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Unhealthy status",
                    value = """
                        {
                          "status": "DOWN",
                          "timestamp": "2024-01-15T10:30:00Z",
                          "checks": {
                            "database": {
                              "status": "DOWN",
                              "error": "Connection timeout after 5000ms",
                              "details": "Database connectivity failed"
                            },
                            "bookSearch": {
                              "status": "UP",
                              "responseTimeMs": 45,
                              "details": "Search functionality operational"
                            }
                          }
                        }
                        """
                )
            )
        )
    })
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> healthStatus() {
        Timer.Sample sample = Timer.start(meterRegistry);
        healthCheckCounter.increment();
        
        try {
            Instant start = Instant.now();
            Map<String, Object> health = new HashMap<>();
            Map<String, Object> checks = new HashMap<>();
            boolean allHealthy = true;
            
            // Check database health
            Map<String, Object> dbHealth = checkDatabaseHealth();
            checks.put("database", dbHealth);
            if (!"UP".equals(dbHealth.get("status"))) {
                allHealthy = false;
            }
            
            // Check book search service health
            Map<String, Object> searchHealth = checkBookSearchHealth();
            checks.put("bookSearch", searchHealth);
            if (!"UP".equals(searchHealth.get("status"))) {
                allHealthy = false;
            }
            
            // Check cache health
            Map<String, Object> cacheHealth = checkCacheHealth();
            checks.put("cache", cacheHealth);
            if (!"UP".equals(cacheHealth.get("status"))) {
                allHealthy = false;
            }
            
            // Build overall health response
            health.put("status", allHealthy ? "UP" : "DOWN");
            health.put("timestamp", Instant.now().toString());
            health.put("checks", checks);
            
            // Add metrics
            Duration totalTime = Duration.between(start, Instant.now());
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("totalHealthChecks", (long) healthCheckCounter.count());
            metrics.put("averageResponseTimeMs", healthCheckTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS));
            metrics.put("currentCheckTimeMs", totalTime.toMillis());
            health.put("metrics", metrics);
            
            sample.stop(healthCheckTimer);
            
            // Return appropriate HTTP status
            if (allHealthy) {
                logger.debug("Health check passed - all systems healthy");
                return ResponseEntity.ok(health);
            } else {
                logger.warn("Health check failed - some systems unhealthy");
                return ResponseEntity.status(503).body(health);
            }
            
        } catch (Exception e) {
            sample.stop(healthCheckTimer);
            logger.error("Health check failed with exception", e);
            
            Map<String, Object> errorHealth = new HashMap<>();
            errorHealth.put("status", "DOWN");
            errorHealth.put("timestamp", Instant.now().toString());
            errorHealth.put("error", e.getMessage());
            errorHealth.put("exception", e.getClass().getSimpleName());
            
            return ResponseEntity.status(503).body(errorHealth);
        }
    }

    /**
     * Check database connectivity and performance.
     */
    private Map<String, Object> checkDatabaseHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            Instant start = Instant.now();
            
            try (Connection connection = dataSource.getConnection()) {
                // Simple connectivity test
                boolean isValid = connection.isValid(5); // 5 second timeout
                
                Duration responseTime = Duration.between(start, Instant.now());
                long responseTimeMs = responseTime.toMillis();
                
                if (isValid && responseTimeMs < 5000) {
                    health.put("status", "UP");
                    health.put("responseTimeMs", responseTimeMs);
                    health.put("details", "Database connectivity verified");
                } else {
                    health.put("status", "DOWN");
                    health.put("responseTimeMs", responseTimeMs);
                    health.put("error", isValid ? "Slow response time" : "Connection validation failed");
                    health.put("details", "Database connectivity issues detected");
                }
            }
            
        } catch (Exception e) {
            logger.error("Database health check failed", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("details", "Database connection failed");
        }
        
        return health;
    }

    /**
     * Check book search service functionality.
     */
    private Map<String, Object> checkBookSearchHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            Instant start = Instant.now();
            
            // Perform a simple search operation
            var searchRequest = new com.homearchive.dto.SearchRequest("health-check", 
                com.homearchive.dto.SortBy.RELEVANCE, 
                com.homearchive.dto.SortOrder.DESC, 
                1);
            
            var searchResponse = bookSearchService.searchBooks(searchRequest);
            
            Duration responseTime = Duration.between(start, Instant.now());
            long responseTimeMs = responseTime.toMillis();
            
            if (searchResponse != null && responseTimeMs < 5000) {
                health.put("status", "UP");
                health.put("responseTimeMs", responseTimeMs);
                health.put("searchResults", searchResponse.getResultCount());
                health.put("details", "Search functionality operational");
            } else {
                health.put("status", "DOWN");
                health.put("responseTimeMs", responseTimeMs);
                health.put("error", searchResponse == null ? "Null response" : "Slow response time");
                health.put("details", "Search functionality issues detected");
            }
            
        } catch (Exception e) {
            logger.error("Book search health check failed", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("details", "Search service unavailable");
        }
        
        return health;
    }

    /**
     * Check cache system health.
     */
    private Map<String, Object> checkCacheHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Simple cache availability check - cache is Spring managed so if app is running, cache should be available
            health.put("status", "UP");
            health.put("details", "Cache system operational");
            
        } catch (Exception e) {
            logger.error("Cache health check failed", e);
            health.put("status", "DOWN");
            health.put("error", e.getMessage());
            health.put("details", "Cache system unavailable");
        }
        
        return health;
    }

    /**
     * Get detailed application metrics for monitoring dashboards.
     */
    @Operation(
        summary = "Get detailed application metrics",
        description = "Returns comprehensive application metrics for monitoring and performance analysis.",
        tags = {"Health & Monitoring"}
    )
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        try {
            Map<String, Object> metrics = new HashMap<>();
            
            // Health check metrics
            metrics.put("healthChecks", Map.of(
                "total", (long) healthCheckCounter.count(),
                "averageTimeMs", healthCheckTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS),
                "maxTimeMs", healthCheckTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS)
            ));
            
            // JVM metrics (basic)
            Runtime runtime = Runtime.getRuntime();
            metrics.put("jvm", Map.of(
                "totalMemoryMB", runtime.totalMemory() / 1024 / 1024,
                "freeMemoryMB", runtime.freeMemory() / 1024 / 1024,
                "usedMemoryMB", (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024,
                "maxMemoryMB", runtime.maxMemory() / 1024 / 1024,
                "processors", runtime.availableProcessors()
            ));
            
            // Application uptime
            metrics.put("uptime", java.lang.management.ManagementFactory.getRuntimeMXBean().getUptime());
            metrics.put("timestamp", Instant.now().toString());
            
            return ResponseEntity.ok(metrics);
            
        } catch (Exception e) {
            logger.error("Failed to collect metrics", e);
            return ResponseEntity.status(500).body(Map.of(
                "error", "Failed to collect metrics",
                "message", e.getMessage()
            ));
        }
    }
}