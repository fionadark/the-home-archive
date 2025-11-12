package com.thehomearchive.library.controller;

import com.thehomearchive.library.dto.book.BookCreateRequest;
import com.thehomearchive.library.dto.book.BookResponse;
import com.thehomearchive.library.dto.book.BookUpdateRequest;
import com.thehomearchive.library.dto.response.ApiResponse;
import com.thehomearchive.library.dto.response.PagedResponse;
import com.thehomearchive.library.service.BookService;
import com.thehomearchive.library.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST controller for book management operations.
 * Handles CRUD operations for books in the global catalog.
 */
@RestController
@RequestMapping("/v1/books")
@PreAuthorize("hasRole('USER')")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private CategoryService categoryService;

    /**
     * Get a book by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookResponse>> getBookById(@PathVariable Long id) {
        try {
            BookResponse book = bookService.getBookById(id);
            return ResponseEntity.ok(ApiResponse.success(book));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error("Book not found"));
        }
    }

    /**
     * Search books with pagination and sorting.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<BookResponse>>> searchBooks(
            @RequestParam("q") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "title") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<BookResponse> books = bookService.searchBooks(query, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(books)));
    }

    /**
     * Get books with optional category filtering and pagination.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<BookResponse>>> getBooks(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sort", defaultValue = "title") String sort,
            @RequestParam(value = "direction", defaultValue = "asc") String direction) {

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? 
                Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sort));
        
        Page<BookResponse> books;
        if (categoryId != null) {
            // For category filtering, we need to create a method in BookService
            List<BookResponse> categoryBooks = bookService.getBooksByCategory(categoryId);
            // Convert to Page - this is a simplified implementation
            // In a real scenario, you'd want to implement pagination at the repository level
            int start = (int) pageable.getOffset();
            int end = Math.min((start + pageable.getPageSize()), categoryBooks.size());
            List<BookResponse> pageContent = categoryBooks.subList(start, end);
            
            books = new org.springframework.data.domain.PageImpl<>(
                    pageContent, pageable, categoryBooks.size());
        } else {
            books = bookService.getAllBooks(pageable);
        }
        
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(books)));
    }

    /**
     * Create a new book (admin only).
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookResponse>> createBook(@Valid @RequestBody BookCreateRequest request) {
        try {
            BookResponse book = bookService.createBook(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(book, "Book created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update an existing book (admin only).
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookResponse>> updateBook(
            @PathVariable Long id, 
            @Valid @RequestBody BookUpdateRequest request) {
        try {
            BookResponse book = bookService.updateBook(id, request);
            return ResponseEntity.ok(ApiResponse.success(book, "Book updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Delete a book (admin only).
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteBook(@PathVariable Long id) {
        try {
            bookService.deleteBook(id);
            return ResponseEntity.ok(ApiResponse.success("Book deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get all categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<Map<String, Object>>>> getAllCategories() {
        List<Map<String, Object>> categories = categoryService.getAllCategoriesAsMap();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * Create a new category (admin only).
     */
    @PostMapping("/categories")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> createCategory(@Valid @RequestBody Map<String, Object> request) {
        try {
            Map<String, Object> category = categoryService.createCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.success(category, "Category created successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(e.getMessage()));
        }
    }
}