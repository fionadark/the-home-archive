package com.thehomearchive.library.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * User Controller
 * 
 * Handles user profile management endpoints.
 * Requires authentication for all operations.
 */
@RestController
@RequestMapping("/v1/user")
@PreAuthorize("hasRole('USER')")
public class UserController {

    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User profile retrieved successfully");
        response.put("userId", 1);
        response.put("username", "testuser@example.com");
        response.put("role", "USER");
        return ResponseEntity.ok(response);
    }

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateUserProfile(@RequestBody Map<String, Object> profileData) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "User profile updated successfully");
        response.put("userId", 1);
        response.put("updatedFields", profileData.keySet());
        return ResponseEntity.ok(response);
    }
}