package com.homearchive.controller;

import com.homearchive.config.DatabaseMetrics;
import com.homearchive.config.QueryPerformanceInterceptor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * REST controller for database performance monitoring.
 * Provides detailed database performance metrics for monitoring and alerting.
 */
@RestController
@RequestMapping("/api/database-performance")
@Tag(name = "Database Performance", description = "Database query performance and optimization metrics")
public class DatabasePerformanceEndpoint {

    private final DatabaseMetrics databaseMetrics;
    private final QueryPerformanceInterceptor queryInterceptor;

    @Autowired
    public DatabasePerformanceEndpoint(DatabaseMetrics databaseMetrics, 
                                     QueryPerformanceInterceptor queryInterceptor) {
        this.databaseMetrics = databaseMetrics;
        this.queryInterceptor = queryInterceptor;
    }

    /**
     * Provides comprehensive database performance metrics including:
     * - Query execution statistics
     * - Connection pool metrics
     * - Query performance analysis
     * - Health status
     */
    @Operation(
        summary = "Get database performance metrics",
        description = """
            Returns comprehensive database performance metrics for monitoring and optimization.
            
            **Metrics Included:**
            - Query execution statistics (count, timing, slow queries)
            - Connection pool performance (acquisition time, pool status)
            - Database health and connectivity status
            - Performance recommendations based on current metrics
            
            **Use Cases:**
            - Production monitoring and alerting
            - Performance optimization analysis
            - Database health checks
            - Capacity planning
            """,
        tags = {"Database Performance"}
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Database performance metrics retrieved successfully",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Performance metrics",
                    summary = "Example database performance metrics",
                    value = """
                        {
                          "health": {
                            "status": "UP",
                            "details": {
                              "responseTime": "45ms",
                              "database": "responsive"
                            }
                          },
                          "database": {
                            "totalQueries": 1250,
                            "totalQueryTimeMs": 12500.5,
                            "averageQueryTimeMs": 10.0,
                            "maxQueryTimeMs": 850.2,
                            "totalConnections": 45,
                            "averageConnectionTimeMs": 2.5
                          },
                          "queries": {
                            "totalQueries": 1250,
                            "metricQueries": 1250.0,
                            "slowQueries": 12.0,
                            "slowQueryPercentage": "0.96%"
                          },
                          "recommendations": {
                            "status": "Database performance is within acceptable limits."
                          }
                        }
                        """
                )
            )
        )
    })
    @GetMapping
    public ResponseEntity<Map<String, Object>> databasePerformance() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Database health and connectivity
        var health = databaseMetrics.health();
        metrics.put("health", Map.of(
            "status", health.getStatus().getCode(),
            "details", health.getDetails()
        ));
        
        // Query performance statistics
        var dbStats = databaseMetrics.getCurrentStats();
        metrics.put("database", Map.of(
            "totalQueries", dbStats.getTotalQueries(),
            "totalQueryTimeMs", dbStats.getTotalQueryTimeMs(),
            "averageQueryTimeMs", dbStats.getAverageQueryTimeMs(),
            "maxQueryTimeMs", dbStats.getMaxQueryTimeMs(),
            "totalConnections", dbStats.getTotalConnections(),
            "totalConnectionTimeMs", dbStats.getTotalConnectionTimeMs(),
            "averageConnectionTimeMs", dbStats.getAverageConnectionTimeMs(),
            "maxConnectionTimeMs", dbStats.getMaxConnectionTimeMs()
        ));
        
        // Query interceptor statistics
        var queryStats = queryInterceptor.getCurrentStats();
        metrics.put("queries", Map.of(
            "totalQueries", queryStats.getTotalQueries(),
            "metricQueries", queryStats.getMetricQueries(),
            "slowQueries", queryStats.getSlowQueries(),
            "slowQueryPercentage", String.format("%.2f%%", queryStats.getSlowQueryPercentage())
        ));
        
        // Performance recommendations
        metrics.put("recommendations", generateRecommendations(dbStats, queryStats));
        
        return ResponseEntity.ok(metrics);
    }

    /**
     * Generate performance recommendations based on current metrics.
     */
    private Map<String, Object> generateRecommendations(
            DatabaseMetrics.DatabasePerformanceStats dbStats,
            QueryPerformanceInterceptor.QueryPerformanceStats queryStats) {
        
        Map<String, Object> recommendations = new HashMap<>();
        
        // Query performance recommendations
        if (queryStats.getSlowQueryPercentage() > 10) {
            recommendations.put("slowQueries", 
                "High percentage of slow queries (" + String.format("%.1f", queryStats.getSlowQueryPercentage()) + 
                "%). Consider adding database indexes or optimizing query logic.");
        }
        
        // Connection time recommendations
        if (dbStats.getAverageConnectionTimeMs() > 100) {
            recommendations.put("connectionTime", 
                "Average connection acquisition time is high (" + String.format("%.1f", dbStats.getAverageConnectionTimeMs()) + 
                "ms). Consider tuning connection pool settings.");
        }
        
        // Query time recommendations
        if (dbStats.getAverageQueryTimeMs() > 500) {
            recommendations.put("queryTime", 
                "Average query execution time is high (" + String.format("%.1f", dbStats.getAverageQueryTimeMs()) + 
                "ms). Consider optimizing queries or adding indexes.");
        }
        
        // If no issues, provide positive feedback
        if (recommendations.isEmpty()) {
            recommendations.put("status", "Database performance is within acceptable limits.");
        }
        
        return recommendations;
    }
}