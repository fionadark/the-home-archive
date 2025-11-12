package com.thehomearchive.library.controller;

import com.thehomearchive.library.dto.book.AddToLibraryRequest;
import com.thehomearchive.library.dto.book.PersonalLibraryResponse;
import com.thehomearchive.library.dto.book.UpdateLibraryRequest;
import com.thehomearchive.library.dto.response.ApiResponse;
import com.thehomearchive.library.dto.response.PagedResponse;
import com.thehomearchive.library.entity.ReadingStatus;
import com.thehomearchive.library.service.LibraryService;
import com.thehomearchive.library.util.SecurityUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for personal library management operations.
 * Handles user's personal book collection management.
 */
@RestController
@RequestMapping("/v1/library")
@PreAuthorize("hasRole('USER')")
public class LibraryController {

    @Autowired
    private LibraryService libraryService;

        /**
     * Get user's personal library with pagination and sorting.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<PersonalLibraryResponse>>> getUserLibrary(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "dateAdded") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size, 
            Sort.Direction.fromString(sortDir), sortBy);
        Page<PersonalLibraryResponse> books = libraryService.getUserLibrary(userId, pageable);
        
        PagedResponse<PersonalLibraryResponse> response = PagedResponse.of(books);
        return ResponseEntity.ok(ApiResponse.success(response, "Library retrieved successfully"));
    }

    /**
     * Search within user's personal library.
     */
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PagedResponse<PersonalLibraryResponse>>> searchUserLibrary(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Long userId = SecurityUtils.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, size);
        Page<PersonalLibraryResponse> books = libraryService.searchUserLibrary(userId, q, pageable);
        
        PagedResponse<PersonalLibraryResponse> response = PagedResponse.of(books);
        return ResponseEntity.ok(ApiResponse.success(response, "Search results retrieved successfully"));
    }

    /**
     * Advanced search with multiple criteria.
     */
    @GetMapping("/books/advanced-search")
    public ResponseEntity<PagedResponse<PersonalLibraryResponse>> advancedSearchUserLibrary(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "readingStatus", required = false) ReadingStatus readingStatus,
            @RequestParam(value = "physicalLocation", required = false) String physicalLocation,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", defaultValue = "dateAdded") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {

        Long userId = SecurityUtils.getCurrentUserId();
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<PersonalLibraryResponse> books = libraryService.searchUserLibraryWithCriteria(
                userId, title, author, readingStatus, physicalLocation, pageable);

        return ResponseEntity.ok(PagedResponse.of(books));
    }

    /**
     * Add a book to the user's personal library.
     */
    @PostMapping("/books/{bookId}")
    public ResponseEntity<ApiResponse<PersonalLibraryResponse>> addBookToLibrary(
            @PathVariable Long bookId,
            @Valid @RequestBody AddToLibraryRequest request) {

        try {
            Long userId = SecurityUtils.getCurrentUserId();
            PersonalLibraryResponse response = libraryService.addBookToLibrary(userId, bookId, request);
            return ResponseEntity.ok(ApiResponse.success(response, "Book added to your library successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Update personal metadata for a book in the user's library.
     */
    @PutMapping("/books/{bookId}")
    public ResponseEntity<ApiResponse<PersonalLibraryResponse>> updateLibraryEntry(
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateLibraryRequest request) {

        try {
            Long userId = SecurityUtils.getCurrentUserId();
            PersonalLibraryResponse response = libraryService.updateLibraryEntry(userId, bookId, request);
            return ResponseEntity.ok(ApiResponse.success(response, "Book updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Remove a book from the current user's personal library.
     */
    @DeleteMapping("/books/{bookId}")
    public ResponseEntity<ApiResponse<Void>> removeBookFromLibrary(
            @PathVariable Long bookId) {

        try {
            Long userId = SecurityUtils.getCurrentUserId();
            libraryService.removeBookFromLibrary(userId, bookId);
            return ResponseEntity.ok(ApiResponse.success("Book removed from your library successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * Get books by reading status.
     */
    @GetMapping("/books/status/{readingStatus}")
    public ResponseEntity<ApiResponse<List<PersonalLibraryResponse>>> getBooksByReadingStatus(
            @PathVariable ReadingStatus readingStatus) {

        Long userId = SecurityUtils.getCurrentUserId();
        List<PersonalLibraryResponse> books = libraryService.getBooksByReadingStatus(userId, readingStatus);
        return ResponseEntity.ok(ApiResponse.success(books, "Books retrieved successfully"));
    }

    /**
     * Get library statistics for the current user.
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<ReadingStatus, Long>>> getLibraryStatistics() {

        Long userId = SecurityUtils.getCurrentUserId();
        Map<ReadingStatus, Long> statistics = libraryService.getLibraryStatistics(userId);
        return ResponseEntity.ok(ApiResponse.success(statistics, "Statistics retrieved successfully"));
    }

    /**
     * Get count of books in user's library.
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getLibraryCount() {

        Long userId = SecurityUtils.getCurrentUserId();
        Long count = libraryService.getLibraryCount(userId);
        return ResponseEntity.ok(ApiResponse.success(count, "Library count retrieved successfully"));
    }

    /**
     * Get recently added books.
     */
    @GetMapping("/books/recent")
    public ResponseEntity<ApiResponse<List<PersonalLibraryResponse>>> getRecentlyAddedBooks(
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        Long userId = SecurityUtils.getCurrentUserId();
        List<PersonalLibraryResponse> books = libraryService.getRecentlyAddedBooks(userId, limit);
        return ResponseEntity.ok(ApiResponse.success(books, "Recent books retrieved successfully"));
    }

    /**
     * Check if a book is in the current user's library.
     */
    @GetMapping("/books/{bookId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> isBookInLibrary(
            @PathVariable Long bookId) {

        Long userId = SecurityUtils.getCurrentUserId();
        boolean exists = libraryService.isBookInUserLibrary(userId, bookId);
        return ResponseEntity.ok(ApiResponse.success(exists, "Check completed successfully"));
    }
}