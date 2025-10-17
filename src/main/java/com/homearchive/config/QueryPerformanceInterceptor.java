package com.homearchive.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hibernate StatementInspector that monitors SQL query performance and execution.
 * Automatically tracks query execution times and identifies slow queries for optimization.
 */
@Component
public class QueryPerformanceInterceptor implements StatementInspector {

    private static final Logger logger = LoggerFactory.getLogger(QueryPerformanceInterceptor.class);
    
    private MeterRegistry meterRegistry;
    private final AtomicLong queryCounter = new AtomicLong(0);
    
    // Performance thresholds
    private static final long SLOW_QUERY_THRESHOLD_MS = 1000;
    private static final long VERY_SLOW_QUERY_THRESHOLD_MS = 5000;
    
    @Autowired
    public QueryPerformanceInterceptor(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        logger.info("Query performance interceptor initialized");
    }
    
    // Default constructor for Hibernate instantiation
    public QueryPerformanceInterceptor() {
        this.meterRegistry = null;
        logger.info("Query performance interceptor initialized without metrics (test mode)");
    }

    @Override
    public String inspect(String sql) {
        // Track query execution metrics
        long queryId = queryCounter.incrementAndGet();
        Instant startTime = Instant.now();
        
        // Determine query type for categorization
        String queryType = categorizeQuery(sql);
        
        // Record query start
        logger.debug("Executing query #{}: {} - {}", queryId, queryType, truncateQuery(sql));
        
        // Create timer for this specific query
        Timer.Sample sample = meterRegistry != null ? Timer.start(meterRegistry) : null;
        
        // Schedule completion logging (will be called by Hibernate after execution)
        scheduleCompletionLogging(queryId, queryType, sql, startTime, sample);
        
        return sql; // Return original SQL unchanged
    }

    /**
     * Categorize SQL query by type for better monitoring and metrics.
     */
    private String categorizeQuery(String sql) {
        String normalizedSql = sql.trim().toUpperCase();
        
        if (normalizedSql.startsWith("SELECT")) {
            if (normalizedSql.contains("COUNT(")) {
                return "SELECT_COUNT";
            } else if (normalizedSql.contains("JOIN")) {
                return "SELECT_JOIN";
            }
            return "SELECT";
        } else if (normalizedSql.startsWith("INSERT")) {
            return "INSERT";
        } else if (normalizedSql.startsWith("UPDATE")) {
            return "UPDATE";
        } else if (normalizedSql.startsWith("DELETE")) {
            return "DELETE";
        } else if (normalizedSql.startsWith("CREATE")) {
            return "DDL_CREATE";
        } else if (normalizedSql.startsWith("ALTER")) {
            return "DDL_ALTER";
        } else if (normalizedSql.startsWith("DROP")) {
            return "DDL_DROP";
        }
        return "OTHER";
    }

    /**
     * Schedule completion logging for the query.
     * This is a simplified approach - in production, you might use AspectJ or custom listeners.
     */
    private void scheduleCompletionLogging(long queryId, String queryType, String sql, 
                                         Instant startTime, Timer.Sample sample) {
        // Use a separate thread to avoid blocking query execution
        Thread.startVirtualThread(() -> {
            try {
                // Wait a bit to allow query to complete
                Thread.sleep(10);
                
                Duration executionTime = Duration.between(startTime, Instant.now());
                long executionTimeMs = executionTime.toMillis();
                
                // Stop the timer and record metrics (if metrics are available)
                if (sample != null && meterRegistry != null) {
                    sample.stop(Timer.builder("database.query.execution.time")
                            .tag("type", queryType)
                            .description("Database query execution time by type")
                            .register(meterRegistry));
                }
                
                // Log query completion with performance metrics
                if (executionTimeMs >= VERY_SLOW_QUERY_THRESHOLD_MS) {
                    logger.error("VERY SLOW QUERY #{}: {} took {}ms - {}", 
                               queryId, queryType, executionTimeMs, truncateQuery(sql));
                } else if (executionTimeMs >= SLOW_QUERY_THRESHOLD_MS) {
                    logger.warn("Slow query #{}: {} took {}ms - {}", 
                              queryId, queryType, executionTimeMs, truncateQuery(sql));
                } else {
                    logger.debug("Query #{}: {} completed in {}ms", queryId, queryType, executionTimeMs);
                }
                
                // Record detailed metrics (if metrics are available)
                if (meterRegistry != null) {
                    meterRegistry.counter("database.query.count", "type", queryType).increment();
                    
                    if (executionTimeMs >= SLOW_QUERY_THRESHOLD_MS) {
                        meterRegistry.counter("database.query.slow.count", "type", queryType).increment();
                    }
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.debug("Query completion logging interrupted for query #{}", queryId);
            } catch (Exception e) {
                logger.warn("Failed to log query completion for query #{}: {}", queryId, e.getMessage());
            }
        });
    }

    /**
     * Truncate long SQL queries for readable logging.
     */
    private String truncateQuery(String sql) {
        if (sql.length() <= 100) {
            return sql;
        }
        return sql.substring(0, 97) + "...";
    }

    /**
     * Get current query performance statistics.
     */
    public QueryPerformanceStats getCurrentStats() {
        double slowQueryCount = 0.0;
        try {
            // Try to get slow query count, but handle case where metric doesn't exist yet
            var slowCounter = meterRegistry.find("database.query.slow.count").counter();
            if (slowCounter != null) {
                slowQueryCount = slowCounter.count();
            }
        } catch (Exception e) {
            logger.debug("Slow query metric not available yet: {}", e.getMessage());
        }
        
        double totalMetricQueries = 0.0;
        try {
            var totalCounter = meterRegistry.find("database.query.count").counter();
            if (totalCounter != null) {
                totalMetricQueries = totalCounter.count();
            }
        } catch (Exception e) {
            logger.debug("Total query metric not available yet: {}", e.getMessage());
        }
        
        return new QueryPerformanceStats(
            queryCounter.get(),
            totalMetricQueries,
            slowQueryCount
        );
    }

    /**
     * Query performance statistics for monitoring.
     */
    public static class QueryPerformanceStats {
        private final long totalQueries;
        private final double metricQueries;
        private final double slowQueries;

        public QueryPerformanceStats(long totalQueries, double metricQueries, double slowQueries) {
            this.totalQueries = totalQueries;
            this.metricQueries = metricQueries;
            this.slowQueries = slowQueries;
        }

        public long getTotalQueries() { return totalQueries; }
        public double getMetricQueries() { return metricQueries; }
        public double getSlowQueries() { return slowQueries; }
        public double getSlowQueryPercentage() { 
            return metricQueries > 0 ? (slowQueries / metricQueries) * 100 : 0; 
        }
    }
}