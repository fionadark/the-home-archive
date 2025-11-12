package com.thehomearchive.library.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.HashMap;
import java.util.Map;

/**
 * Dashboard controller for user dashboard endpoints.
 * Provides basic dashboard data and user statistics.
 */
@RestController
@RequestMapping("/v1")
public class DashboardController {

    /**
     * Get dashboard data for authenticated users.
     * 
     * @return dashboard information
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Dashboard data retrieved successfully");
        
        Map<String, Object> data = new HashMap<>();
        data.put("totalBooks", 0);
        data.put("booksRead", 0);
        data.put("booksWishlist", 0);
        data.put("recentActivity", new Object[0]);
        
        response.put("data", data);
        
        return ResponseEntity.ok(response);
    }
}