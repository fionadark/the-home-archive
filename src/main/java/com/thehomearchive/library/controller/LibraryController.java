package com.thehomearchive.library.controller;

import com.thehomearchive.library.dto.book.AddToLibraryRequest;
import com.thehomearchive.library.dto.book.PersonalLibraryResponse;
import com.thehomearchive.library.dto.book.UpdateLibraryRequest;
import com.thehomearchive.library.dto.response.ApiResponse;
import com.thehomearchive.library.dto.response.PagedResponse;
import com.thehomearchive.library.entity.ReadingStatus;
import com.thehomearchive.library.security.CurrentUser;
import com.thehomearchive.library.security.UserPrincipal;
import com.thehomearchive.library.service.LibraryService;
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
@RequestMapping("/api/library")
@PreAuthorize("hasRole('USER')")
public class LibraryController {

    @Autowired
    private LibraryService libraryService;

    /**
     * Get all books in the current user's personal library.
     */
    @GetMapping("/books")
    public ResponseEntity<PagedResponse<PersonalLibraryResponse>> getUserLibrary(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", defaultValue = "dateAdded") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<PersonalLibraryResponse> books = libraryService.getUserLibrary(currentUser.getId(), pageable);

        return ResponseEntity.ok(PagedResponse.of(books));
    }

    /**
     * Search books in the current user's personal library.
     */
    @GetMapping("/books/search")
    public ResponseEntity<PagedResponse<PersonalLibraryResponse>> searchUserLibrary(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam("q") String searchTerm,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<PersonalLibraryResponse> books = libraryService.searchUserLibrary(currentUser.getId(), searchTerm, pageable);

        return ResponseEntity.ok(PagedResponse.of(books));
    }

    /**
     * Advanced search with multiple criteria.
     */
    @GetMapping("/books/advanced-search")
    public ResponseEntity<PagedResponse<PersonalLibraryResponse>> advancedSearchUserLibrary(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "author", required = false) String author,
            @RequestParam(value = "readingStatus", required = false) ReadingStatus readingStatus,
            @RequestParam(value = "physicalLocation", required = false) String physicalLocation,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size,
            @RequestParam(value = "sortBy", defaultValue = "dateAdded") String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "desc") String sortDir) {

        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<PersonalLibraryResponse> books = libraryService.searchUserLibraryWithCriteria(
                currentUser.getId(), title, author, readingStatus, physicalLocation, pageable);

        return ResponseEntity.ok(PagedResponse.of(books));
    }

    /**
     * Add a book to the user's personal library.
     */
    @PostMapping("/books/{bookId}")
    public ResponseEntity<ApiResponse<PersonalLibraryResponse>> addBookToLibrary(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long bookId,
            @Valid @RequestBody AddToLibraryRequest request) {

        try {
            PersonalLibraryResponse response = libraryService.addBookToLibrary(currentUser.getId(), bookId, request);
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
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long bookId,
            @Valid @RequestBody UpdateLibraryRequest request) {

        try {
            PersonalLibraryResponse response = libraryService.updateLibraryEntry(currentUser.getId(), bookId, request);
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
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long bookId) {

        try {
            libraryService.removeBookFromLibrary(currentUser.getId(), bookId);
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
            @CurrentUser UserPrincipal currentUser,
            @PathVariable ReadingStatus readingStatus) {

        List<PersonalLibraryResponse> books = libraryService.getBooksByReadingStatus(currentUser.getId(), readingStatus);
        return ResponseEntity.ok(ApiResponse.success(books, "Books retrieved successfully"));
    }

    /**
     * Get library statistics for the current user.
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Map<ReadingStatus, Long>>> getLibraryStatistics(
            @CurrentUser UserPrincipal currentUser) {

        Map<ReadingStatus, Long> statistics = libraryService.getLibraryStatistics(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(statistics, "Statistics retrieved successfully"));
    }

    /**
     * Get count of books in user's library.
     */
    @GetMapping("/count")
    public ResponseEntity<ApiResponse<Long>> getLibraryCount(
            @CurrentUser UserPrincipal currentUser) {

        Long count = libraryService.getLibraryCount(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(count, "Library count retrieved successfully"));
    }

    /**
     * Get recently added books.
     */
    @GetMapping("/books/recent")
    public ResponseEntity<ApiResponse<List<PersonalLibraryResponse>>> getRecentlyAddedBooks(
            @CurrentUser UserPrincipal currentUser,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {

        List<PersonalLibraryResponse> books = libraryService.getRecentlyAddedBooks(currentUser.getId(), limit);
        return ResponseEntity.ok(ApiResponse.success(books, "Recent books retrieved successfully"));
    }

    /**
     * Check if a book is in the current user's library.
     */
    @GetMapping("/books/{bookId}/exists")
    public ResponseEntity<ApiResponse<Boolean>> isBookInLibrary(
            @CurrentUser UserPrincipal currentUser,
            @PathVariable Long bookId) {

        boolean exists = libraryService.isBookInUserLibrary(currentUser.getId(), bookId);
        return ResponseEntity.ok(ApiResponse.success(exists, "Check completed successfully"));
    }
}