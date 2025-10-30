package com.thehomearchive.library.controller;

import com.thehomearchive.library.config.MonitoringConfig.RequestMonitoringFilter;
import com.thehomearchive.library.service.MonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthComponent;
import org.springframework.boot.actuate.health.HealthEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller for monitoring and metrics endpoints
 * Provides administrative access to system monitoring data
 */
@RestController
@RequestMapping("/api/admin/monitoring")
@PreAuthorize("hasRole('ADMIN')")
public class MonitoringController {

    @Autowired(required = false)
    private MonitoringService monitoringService;
    
    @Autowired(required = false)
    private HealthEndpoint healthEndpoint;
    
    @Autowired(required = false)
    private RequestMonitoringFilter requestMonitoringFilter;

    /**
     * Get business metrics
     */
    @GetMapping("/metrics/business")
    public ResponseEntity<Map<String, Object>> getBusinessMetrics() {
        if (monitoringService == null) {
            return ResponseEntity.ok(Map.of("error", "Monitoring service not available"));
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("businessMetrics", monitoringService.getCurrentBusinessMetrics());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.ok(response);
    }

    /**
     * Get request statistics
     */
    @GetMapping("/metrics/requests")
    public ResponseEntity<Map<String, Object>> getRequestMetrics() {
        Map<String, Object> response = new HashMap<>();
        
        if (requestMonitoringFilter != null) {
            response.put("requestStats", requestMonitoringFilter.getStatistics());
        } else {
            response.put("requestStats", Map.of("error", "Request monitoring not available"));
        }
        
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Get system health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> response = new HashMap<>();
        
        if (healthEndpoint != null) {
            HealthComponent health = healthEndpoint.health();
            response.put("health", health);
        } else {
            response.put("health", Map.of("status", "UNKNOWN", "error", "Health endpoint not available"));
        }
        
        response.put("timestamp", System.currentTimeMillis());
        return ResponseEntity.ok(response);
    }

    /**
     * Test endpoints for recording metrics
     */
    @PostMapping("/test/user-registration")
    public ResponseEntity<String> testUserRegistration(@RequestParam String email) {
        if (monitoringService != null) {
            monitoringService.recordUserRegistration(email);
            return ResponseEntity.ok("User registration metric recorded for: " + email);
        }
        return ResponseEntity.ok("Monitoring service not available");
    }

    @PostMapping("/test/book-search")
    public ResponseEntity<String> testBookSearch(@RequestParam String type, @RequestParam String query) {
        if (monitoringService != null) {
            monitoringService.recordBookSearch(type, query);
            return ResponseEntity.ok("Book search metric recorded: " + type + " - " + query);
        }
        return ResponseEntity.ok("Monitoring service not available");
    }

    @PostMapping("/test/book-addition")
    public ResponseEntity<String> testBookAddition(@RequestParam String source, @RequestParam String title) {
        if (monitoringService != null) {
            monitoringService.recordBookAdded(source, title);
            return ResponseEntity.ok("Book addition metric recorded: " + source + " - " + title);
        }
        return ResponseEntity.ok("Monitoring service not available");
    }

    @PostMapping("/test/login-attempt")
    public ResponseEntity<String> testLoginAttempt(@RequestParam String email, @RequestParam boolean success) {
        if (monitoringService != null) {
            monitoringService.recordLoginAttempt(email, success);
            return ResponseEntity.ok("Login attempt metric recorded for: " + email + " (success: " + success + ")");
        }
        return ResponseEntity.ok("Monitoring service not available");
    }
}