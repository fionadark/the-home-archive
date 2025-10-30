package com.thehomearchive.library.config;

import io.micrometer.core.aop.TimedAspect;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.audit.AuditEvent;
import org.springframework.boot.actuate.audit.listener.AuditApplicationEvent;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Performance Monitoring and Logging Configuration for The Home Archive
 * 
 * Provides comprehensive monitoring capabilities including:
 * - Custom metrics for business operations
 * - Request/response logging with performance tracking
 * - Health checks for critical system components
 * - Security audit logging
 * - API usage analytics
 * - Memory and performance monitoring
 * 
 * Integrates with Spring Boot Actuator for production-ready monitoring
 * and supports both development and production environments.
 */
@Slf4j
@Configuration
@EnableAsync
public class MonitoringConfig {

    private static final Logger PERFORMANCE_LOGGER = LoggerFactory.getLogger("PERFORMANCE");
    private static final Logger SECURITY_LOGGER = LoggerFactory.getLogger("SECURITY");
    private static final Logger API_LOGGER = LoggerFactory.getLogger("API");
    
    @Value("${spring.application.name:the-home-archive}")
    private String applicationName;
    
    @Value("${monitoring.enable-request-logging:true}")
    private boolean enableRequestLogging;
    
    @Value("${monitoring.slow-request-threshold:1000}")
    private long slowRequestThreshold;

    /**
     * Configure custom meter registry with application-specific tags
     */
    @Bean
    MeterRegistryCustomizer<MeterRegistry> metricsCommonTags(Environment environment) {
        return registry -> registry.config()
            .commonTags("application", applicationName)
            .commonTags("environment", environment.getActiveProfiles().length > 0 
                ? environment.getActiveProfiles()[0] : "unknown")
            .meterFilter(MeterFilter.deny(id -> {
                String uri = id.getTag("uri");
                return uri != null && (
                    uri.startsWith("/actuator") ||
                    uri.startsWith("/webjars") ||
                    uri.startsWith("/css") ||
                    uri.startsWith("/js") ||
                    uri.startsWith("/images")
                );
            }));
    }

    /**
     * Enable @Timed annotation support for method-level metrics
     */
    @Bean
    public TimedAspect timedAspect(MeterRegistry registry) {
        return new TimedAspect(registry);
    }

    /**
     * Request monitoring filter for performance tracking
     */
    @Bean
    public RequestMonitoringFilter requestMonitoringFilter() {
        return new RequestMonitoringFilter();
    }

    /**
     * Custom health indicator for library-specific checks
     */
    @Bean
    public HealthIndicator libraryHealthIndicator() {
        return () -> {
            try {
                // Check critical system components
                long memoryUsage = getMemoryUsagePercentage();
                boolean systemHealthy = memoryUsage < 90; // Consider unhealthy if >90% memory usage
                
                Map<String, Object> details = new HashMap<>();
                details.put("memoryUsage", memoryUsage + "%");
                details.put("timestamp", Instant.now());
                details.put("activeProfiles", System.getProperty("spring.profiles.active", "default"));
                
                if (systemHealthy) {
                    return Health.up().withDetails(details).build();
                } else {
                    return Health.down().withDetails(details).build();
                }
                    
            } catch (Exception e) {
                log.error("Health check failed", e);
                return Health.down().withException(e).build();
            }
        };
    }

    /**
     * Custom info contributor for application metadata
     */
    @Bean
    public InfoContributor libraryInfoContributor() {
        return builder -> {
            Map<String, Object> libraryInfo = new HashMap<>();
            libraryInfo.put("name", "Dark Academia Library");
            libraryInfo.put("description", "Personal library management system");
            libraryInfo.put("version", getClass().getPackage().getImplementationVersion());
            libraryInfo.put("buildTime", Instant.now());
            
            builder.withDetail("library", libraryInfo);
        };
    }

    /**
     * Audit event listener for security monitoring
     */
    @EventListener
    @Async
    public void onAuditEvent(AuditApplicationEvent event) {
        AuditEvent auditEvent = event.getAuditEvent();
        String eventType = auditEvent.getType();
        
        // Log security-related events
        if (isSecurityEvent(eventType)) {
            SECURITY_LOGGER.info("Security event: type={}, principal={}, timestamp={}, details={}", 
                eventType, 
                auditEvent.getPrincipal(), 
                auditEvent.getTimestamp(), 
                auditEvent.getData());
        }
    }

    /**
     * Request monitoring filter implementation
     */
    public class RequestMonitoringFilter extends OncePerRequestFilter {
        
        private final AtomicLong requestCounter = new AtomicLong(0);
        private final Map<String, AtomicLong> endpointCounters = new ConcurrentHashMap<>();
        private final Map<String, AtomicLong> statusCounters = new ConcurrentHashMap<>();

        @Override
        @SuppressWarnings("null")
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                FilterChain filterChain) throws ServletException, IOException {
            
            if (!enableRequestLogging || shouldSkipLogging(request)) {
                filterChain.doFilter(request, response);
                return;
            }

            long startTime = System.currentTimeMillis();
            String requestId = generateRequestId();
            String endpoint = getEndpointPattern(request);
            
            // Add request ID to MDC for correlation
            org.slf4j.MDC.put("requestId", requestId);
            org.slf4j.MDC.put("endpoint", endpoint);
            
            try {
                // Log request details
                logRequestStart(request, requestId, endpoint);
                
                // Increment counters
                requestCounter.incrementAndGet();
                endpointCounters.computeIfAbsent(endpoint, k -> new AtomicLong(0)).incrementAndGet();
                
                // Continue with request processing
                filterChain.doFilter(request, response);
                
            } finally {
                long duration = System.currentTimeMillis() - startTime;
                String status = String.valueOf(response.getStatus());
                
                // Update status counters
                statusCounters.computeIfAbsent(status, k -> new AtomicLong(0)).incrementAndGet();
                
                // Log response details
                logRequestEnd(request, response, requestId, endpoint, duration);
                
                // Log slow requests
                if (duration > slowRequestThreshold) {
                    logSlowRequest(request, response, requestId, endpoint, duration);
                }
                
                // Clear MDC
                org.slf4j.MDC.clear();
            }
        }

        private void logRequestStart(HttpServletRequest request, String requestId, String endpoint) {
            API_LOGGER.info("Request started: id={}, method={}, endpoint={}, ip={}, userAgent={}", 
                requestId,
                request.getMethod(),
                endpoint,
                getClientIP(request),
                request.getHeader("User-Agent"));
        }

        private void logRequestEnd(HttpServletRequest request, HttpServletResponse response, 
                String requestId, String endpoint, long duration) {
            API_LOGGER.info("Request completed: id={}, method={}, endpoint={}, status={}, duration={}ms", 
                requestId,
                request.getMethod(),
                endpoint,
                response.getStatus(),
                duration);
        }

        private void logSlowRequest(HttpServletRequest request, HttpServletResponse response, 
                String requestId, String endpoint, long duration) {
            PERFORMANCE_LOGGER.warn("Slow request detected: id={}, method={}, endpoint={}, status={}, duration={}ms", 
                requestId,
                request.getMethod(),
                endpoint,
                response.getStatus(),
                duration);
        }

        private boolean shouldSkipLogging(HttpServletRequest request) {
            String uri = request.getRequestURI();
            return uri.startsWith("/actuator") || 
                   uri.startsWith("/css") || 
                   uri.startsWith("/js") || 
                   uri.startsWith("/images") ||
                   uri.startsWith("/webjars") ||
                   uri.equals("/favicon.ico");
        }

        private String generateRequestId() {
            return String.format("%d-%d", System.currentTimeMillis(), requestCounter.get());
        }

        private String getEndpointPattern(HttpServletRequest request) {
            String uri = request.getRequestURI();
            String method = request.getMethod();
            
            // Normalize common patterns
            if (uri.matches("/api/books/\\d+")) {
                return method + " /api/books/{id}";
            } else if (uri.matches("/api/users/\\d+")) {
                return method + " /api/users/{id}";
            } else if (uri.matches("/api/library/\\d+")) {
                return method + " /api/library/{id}";
            }
            
            return method + " " + uri;
        }

        private String getClientIP(HttpServletRequest request) {
            String xForwardedFor = request.getHeader("X-Forwarded-For");
            if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                return xForwardedFor.split(",")[0].trim();
            }
            
            String xRealIP = request.getHeader("X-Real-IP");
            if (xRealIP != null && !xRealIP.isEmpty()) {
                return xRealIP;
            }
            
            return request.getRemoteAddr();
        }

        /**
         * Get current monitoring statistics
         */
        public Map<String, Object> getStatistics() {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalRequests", requestCounter.get());
            stats.put("endpointCounts", new HashMap<>(endpointCounters));
            stats.put("statusCounts", new HashMap<>(statusCounters));
            stats.put("memoryUsage", getMemoryUsagePercentage());
            return stats;
        }
    }

    /**
     * Custom metrics for business operations
     */
    @Bean
    public LibraryMetrics libraryMetrics(MeterRegistry meterRegistry) {
        return new LibraryMetrics(meterRegistry);
    }

    /**
     * Business metrics tracking
     */
    public static class LibraryMetrics {
        
        private final MeterRegistry meterRegistry;
        private final AtomicLong userRegistrations = new AtomicLong(0);
        private final AtomicLong bookSearches = new AtomicLong(0);
        private final AtomicLong booksAdded = new AtomicLong(0);
        private final AtomicLong loginAttempts = new AtomicLong(0);
        private final AtomicLong loginFailures = new AtomicLong(0);

        public LibraryMetrics(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
            
            // Register gauges for current values
            meterRegistry.gauge("library.users.registrations.total", userRegistrations);
            meterRegistry.gauge("library.books.searches.total", bookSearches);
            meterRegistry.gauge("library.books.added.total", booksAdded);
            meterRegistry.gauge("library.auth.login.attempts.total", loginAttempts);
            meterRegistry.gauge("library.auth.login.failures.total", loginFailures);
        }

        public void recordUserRegistration() {
            userRegistrations.incrementAndGet();
            meterRegistry.counter("library.users.registrations").increment();
        }

        public void recordBookSearch(String searchType) {
            bookSearches.incrementAndGet();
            meterRegistry.counter("library.books.searches", "type", searchType).increment();
        }

        public void recordBookAdded(String source) {
            booksAdded.incrementAndGet();
            meterRegistry.counter("library.books.added", "source", source).increment();
        }

        public void recordLoginAttempt(boolean success) {
            loginAttempts.incrementAndGet();
            if (success) {
                meterRegistry.counter("library.auth.login.success").increment();
            } else {
                loginFailures.incrementAndGet();
                meterRegistry.counter("library.auth.login.failure").increment();
            }
        }

        public void recordApiCall(String endpoint, String method, int status, long duration) {
            meterRegistry.timer("library.api.requests", 
                "endpoint", endpoint, 
                "method", method, 
                "status", String.valueOf(status))
                .record(duration, java.util.concurrent.TimeUnit.MILLISECONDS);
        }

        /**
         * Get current business metrics
         */
        public Map<String, Object> getCurrentMetrics() {
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("userRegistrations", userRegistrations.get());
            metrics.put("bookSearches", bookSearches.get());
            metrics.put("booksAdded", booksAdded.get());
            metrics.put("loginAttempts", loginAttempts.get());
            metrics.put("loginFailures", loginFailures.get());
            metrics.put("loginSuccessRate", calculateSuccessRate());
            return metrics;
        }

        private double calculateSuccessRate() {
            long attempts = loginAttempts.get();
            if (attempts == 0) return 0.0;
            long successes = attempts - loginFailures.get();
            return (double) successes / attempts * 100.0;
        }
    }

    /**
     * Utility methods
     */
    private boolean isSecurityEvent(String eventType) {
        return eventType.contains("AUTHENTICATION") || 
               eventType.contains("AUTHORIZATION") || 
               eventType.contains("LOGIN") || 
               eventType.contains("LOGOUT") ||
               eventType.contains("ACCESS_DENIED");
    }

    private long getMemoryUsagePercentage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        return (usedMemory * 100) / totalMemory;
    }
}