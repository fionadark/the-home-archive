package com.thehomearchive.library.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

/**
 * Library Controller
 * 
 * Handles library-related endpoints for managing books and collections.
 * Requires authentication for all operations.
 */
@RestController
@RequestMapping("/api/v1/library")
@PreAuthorize("hasRole('USER')")
public class LibraryController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getLibrary() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Library endpoint accessed successfully");
        response.put("books", new ArrayList<>());
        response.put("totalBooks", 0);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/books")
    public ResponseEntity<Map<String, Object>> addBook(@RequestBody Map<String, Object> bookData) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Book added successfully");
        response.put("bookId", 1);
        response.put("book", bookData);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/books/{id}")
    public ResponseEntity<Map<String, Object>> updateBook(@PathVariable Long id, @RequestBody Map<String, Object> bookData) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Book updated successfully");
        response.put("bookId", id);
        response.put("book", bookData);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/books/{id}")
    public ResponseEntity<Map<String, Object>> deleteBook(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Book deleted successfully");
        response.put("bookId", id);
        return ResponseEntity.ok(response);
    }
}