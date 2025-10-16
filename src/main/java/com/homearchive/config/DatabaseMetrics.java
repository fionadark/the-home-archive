package com.homearchive.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;

/**
 * Database performance monitoring component that tracks query execution times,
 * connection pool health, and database responsiveness for production monitoring.
 */
@Component
public class DatabaseMetrics implements HealthIndicator {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseMetrics.class);
    
    private final DataSource dataSource;
    private final MeterRegistry meterRegistry;
    
    // Performance tracking metrics
    private final Timer queryTimer;
    private final Timer connectionTimer;
    
    @Autowired
    public DatabaseMetrics(DataSource dataSource, MeterRegistry meterRegistry) {
        this.dataSource = dataSource;
        this.meterRegistry = meterRegistry;
        
        // Initialize performance timers
        this.queryTimer = Timer.builder("database.query.execution.time")
                .description("Database query execution time")
                .register(meterRegistry);
                
        this.connectionTimer = Timer.builder("database.connection.acquisition.time")
                .description("Database connection acquisition time")
                .register(meterRegistry);
        
        logger.info("Database metrics monitoring initialized");
    }

    /**
     * Health check that monitors database connectivity and response time.
     * Fails if database is unreachable or response time exceeds threshold.
     */
    @Override
    public Health health() {
        try {
            Instant start = Instant.now();
            
            // Test database connectivity with a simple query
            Timer.Sample connectionSample = Timer.start(meterRegistry);
            try (Connection connection = dataSource.getConnection()) {
                connectionSample.stop(connectionTimer);
                
                Timer.Sample querySample = Timer.start(meterRegistry);
                try (PreparedStatement stmt = connection.prepareStatement("SELECT 1");
                     ResultSet rs = stmt.executeQuery()) {
                    
                    querySample.stop(queryTimer);
                    
                    Duration responseTime = Duration.between(start, Instant.now());
                    long responseTimeMs = responseTime.toMillis();
                    
                    // Log slow health checks for monitoring
                    if (responseTimeMs > 100) {
                        logger.warn("Slow database health check: {}ms", responseTimeMs);
                    }
                    
                    // Fail health check if response time is too slow
                    if (responseTimeMs > 5000) {
                        return Health.down()
                                .withDetail("responseTime", responseTimeMs + "ms")
                                .withDetail("status", "Database response time exceeded 5 seconds")
                                .build();
                    }
                    
                    return Health.up()
                            .withDetail("responseTime", responseTimeMs + "ms")
                            .withDetail("database", "responsive")
                            .build();
                            
                } catch (SQLException queryEx) {
                    logger.error("Database query failed during health check", queryEx);
                    return Health.down()
                            .withDetail("error", queryEx.getMessage())
                            .withDetail("status", "Query execution failed")
                            .build();
                }
                
            } catch (SQLException connectionEx) {
                logger.error("Database connection failed during health check", connectionEx);
                return Health.down()
                        .withDetail("error", connectionEx.getMessage())
                        .withDetail("status", "Connection acquisition failed")
                        .build();
            }
            
        } catch (Exception ex) {
            logger.error("Database health check failed unexpectedly", ex);
            return Health.down()
                    .withDetail("error", ex.getMessage())
                    .withDetail("status", "Unexpected error during health check")
                    .build();
        }
    }

    /**
     * Records query execution time for monitoring and alerting.
     * Call this method around database operations to track performance.
     */
    public void recordQueryExecution(String queryType, Duration executionTime) {
        Timer.builder("database.query.execution")
                .tag("type", queryType)
                .description("Database query execution time by type")
                .register(meterRegistry)
                .record(executionTime);
                
        long executionTimeMs = executionTime.toMillis();
        logger.debug("Query {} executed in {}ms", queryType, executionTimeMs);
        
        // Log slow queries for optimization
        if (executionTimeMs > 1000) {
            logger.warn("Slow query detected: {} took {}ms", queryType, executionTimeMs);
        }
    }

    /**
     * Records database connection acquisition time for pool monitoring.
     */
    public void recordConnectionAcquisition(Duration acquisitionTime) {
        connectionTimer.record(acquisitionTime);
        
        long acquisitionTimeMs = acquisitionTime.toMillis();
        logger.debug("Database connection acquired in {}ms", acquisitionTimeMs);
        
        // Log slow connection acquisition
        if (acquisitionTimeMs > 1000) {
            logger.warn("Slow connection acquisition: {}ms (possible pool exhaustion)", acquisitionTimeMs);
        }
    }

    /**
     * Get current database performance statistics for monitoring dashboards.
     */
    public DatabasePerformanceStats getCurrentStats() {
        return new DatabasePerformanceStats(
            queryTimer.count(),
            queryTimer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS),
            queryTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS),
            connectionTimer.count(),
            connectionTimer.totalTime(java.util.concurrent.TimeUnit.MILLISECONDS),
            connectionTimer.max(java.util.concurrent.TimeUnit.MILLISECONDS)
        );
    }

    /**
     * Database performance statistics for monitoring and alerting.
     */
    public static class DatabasePerformanceStats {
        private final long totalQueries;
        private final double totalQueryTimeMs;
        private final double maxQueryTimeMs;
        private final long totalConnections;
        private final double totalConnectionTimeMs;
        private final double maxConnectionTimeMs;

        public DatabasePerformanceStats(long totalQueries, double totalQueryTimeMs, double maxQueryTimeMs,
                                      long totalConnections, double totalConnectionTimeMs, double maxConnectionTimeMs) {
            this.totalQueries = totalQueries;
            this.totalQueryTimeMs = totalQueryTimeMs;
            this.maxQueryTimeMs = maxQueryTimeMs;
            this.totalConnections = totalConnections;
            this.totalConnectionTimeMs = totalConnectionTimeMs;
            this.maxConnectionTimeMs = maxConnectionTimeMs;
        }

        // Getters
        public long getTotalQueries() { return totalQueries; }
        public double getTotalQueryTimeMs() { return totalQueryTimeMs; }
        public double getMaxQueryTimeMs() { return maxQueryTimeMs; }
        public double getAverageQueryTimeMs() { 
            return totalQueries > 0 ? totalQueryTimeMs / totalQueries : 0; 
        }
        public long getTotalConnections() { return totalConnections; }
        public double getTotalConnectionTimeMs() { return totalConnectionTimeMs; }
        public double getMaxConnectionTimeMs() { return maxConnectionTimeMs; }
        public double getAverageConnectionTimeMs() { 
            return totalConnections > 0 ? totalConnectionTimeMs / totalConnections : 0; 
        }
    }
}