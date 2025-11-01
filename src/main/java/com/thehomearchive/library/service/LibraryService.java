package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.book.AddToLibraryRequest;
import com.thehomearchive.library.dto.book.PersonalLibraryResponse;
import com.thehomearchive.library.dto.book.UpdateLibraryRequest;
import com.thehomearchive.library.entity.Book;
import com.thehomearchive.library.entity.PersonalLibrary;
import com.thehomearchive.library.entity.ReadingStatus;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.repository.BookRepository;
import com.thehomearchive.library.repository.PersonalLibraryRepository;
import com.thehomearchive.library.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing personal library operations.
 * Handles adding/removing books from user libraries and managing personal metadata.
 */
@Service
@Transactional
public class LibraryService {

    @Autowired
    private PersonalLibraryRepository personalLibraryRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all books in a user's personal library.
     */
    @Transactional(readOnly = true)
    public List<PersonalLibraryResponse> getUserLibrary(Long userId) {
        List<PersonalLibrary> personalLibrary = personalLibraryRepository.findByUserIdOrderByDateAddedDesc(userId);
        return personalLibrary.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get books in a user's personal library with pagination.
     */
    @Transactional(readOnly = true)
    public Page<PersonalLibraryResponse> getUserLibrary(Long userId, Pageable pageable) {
        Page<PersonalLibrary> personalLibrary = personalLibraryRepository.findByUserId(userId, pageable);
        return personalLibrary.map(this::convertToResponse);
    }

    /**
     * Search books in a user's personal library.
     */
    @Transactional(readOnly = true)
    public List<PersonalLibraryResponse> searchUserLibrary(Long userId, String searchTerm) {
        List<PersonalLibrary> results = personalLibraryRepository.searchUserLibrary(userId, searchTerm);
        return results.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search books in a user's personal library with pagination.
     */
    @Transactional(readOnly = true)
    public Page<PersonalLibraryResponse> searchUserLibrary(Long userId, String searchTerm, Pageable pageable) {
        Page<PersonalLibrary> results = personalLibraryRepository.searchUserLibrary(userId, searchTerm, pageable);
        return results.map(this::convertToResponse);
    }

    /**
     * Advanced search with multiple criteria.
     */
    @Transactional(readOnly = true)
    public Page<PersonalLibraryResponse> searchUserLibraryWithCriteria(
            Long userId, String title, String author, ReadingStatus readingStatus, 
            String physicalLocation, Pageable pageable) {
        Page<PersonalLibrary> results = personalLibraryRepository.findUserLibraryWithCriteria(
                userId, title, author, readingStatus, physicalLocation, pageable);
        return results.map(this::convertToResponse);
    }

    /**
     * Add a book to a user's personal library.
     */
    public PersonalLibraryResponse addBookToLibrary(Long userId, Long bookId, AddToLibraryRequest request) {
        // Check if book already exists in user's library
        if (personalLibraryRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new IllegalArgumentException("Book is already in your library");
        }

        // Get user and book entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found"));

        // Create personal library entry
        PersonalLibrary personalLibrary = new PersonalLibrary(user, book);
        personalLibrary.setPhysicalLocation(request.getPhysicalLocation());
        personalLibrary.setReadingStatus(request.getReadingStatus() != null ? request.getReadingStatus() : ReadingStatus.UNREAD);
        personalLibrary.setPersonalNotes(request.getPersonalNotes());

        PersonalLibrary saved = personalLibraryRepository.save(personalLibrary);
        return convertToResponse(saved);
    }

    /**
     * Update personal metadata for a book in user's library.
     */
    public PersonalLibraryResponse updateLibraryEntry(Long userId, Long bookId, UpdateLibraryRequest request) {
        PersonalLibrary personalLibrary = personalLibraryRepository.findByUserIdAndBookId(userId, bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book not found in your library"));

        // Update fields if provided
        if (request.getPhysicalLocation() != null) {
            personalLibrary.setPhysicalLocation(request.getPhysicalLocation());
        }
        if (request.getReadingStatus() != null) {
            updateReadingStatus(personalLibrary, request.getReadingStatus());
        }
        if (request.getPersonalNotes() != null) {
            personalLibrary.setPersonalNotes(request.getPersonalNotes());
        }

        PersonalLibrary saved = personalLibraryRepository.save(personalLibrary);
        return convertToResponse(saved);
    }

    /**
     * Remove a book from a user's personal library.
     */
    public void removeBookFromLibrary(Long userId, Long bookId) {
        if (!personalLibraryRepository.existsByUserIdAndBookId(userId, bookId)) {
            throw new IllegalArgumentException("Book not found in your library");
        }
        personalLibraryRepository.deleteByUserIdAndBookId(userId, bookId);
    }

    /**
     * Get books by reading status for a user.
     */
    @Transactional(readOnly = true)
    public List<PersonalLibraryResponse> getBooksByReadingStatus(Long userId, ReadingStatus readingStatus) {
        List<PersonalLibrary> books = personalLibraryRepository.findByUserIdAndReadingStatusOrderByDateAddedDesc(userId, readingStatus);
        return books.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get library statistics for a user.
     */
    @Transactional(readOnly = true)
    public Map<ReadingStatus, Long> getLibraryStatistics(Long userId) {
        List<Object[]> stats = personalLibraryRepository.getLibraryStatistics(userId);
        return stats.stream()
                .collect(Collectors.toMap(
                        row -> (ReadingStatus) row[0],
                        row -> (Long) row[1]
                ));
    }

    /**
     * Get count of books in user's library.
     */
    @Transactional(readOnly = true)
    public Long getLibraryCount(Long userId) {
        return personalLibraryRepository.countByUserId(userId);
    }

    /**
     * Get recently added books.
     */
    @Transactional(readOnly = true)
    public List<PersonalLibraryResponse> getRecentlyAddedBooks(Long userId, int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        List<PersonalLibrary> recentBooks = personalLibraryRepository.findRecentlyAddedBooks(userId, pageable);
        return recentBooks.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Check if a book is in user's library.
     */
    @Transactional(readOnly = true)
    public boolean isBookInUserLibrary(Long userId, Long bookId) {
        return personalLibraryRepository.existsByUserIdAndBookId(userId, bookId);
    }

    /**
     * Update reading status and related timestamps.
     */
    private void updateReadingStatus(PersonalLibrary personalLibrary, ReadingStatus newStatus) {
        ReadingStatus oldStatus = personalLibrary.getReadingStatus();
        personalLibrary.setReadingStatus(newStatus);

        // Update timestamps based on status change
        if (newStatus == ReadingStatus.READING && oldStatus != ReadingStatus.READING) {
            personalLibrary.setDateStarted(LocalDateTime.now());
        }
        if (newStatus == ReadingStatus.READ && oldStatus != ReadingStatus.READ) {
            personalLibrary.setDateCompleted(LocalDateTime.now());
        }
    }

    /**
     * Convert PersonalLibrary entity to response DTO.
     */
    private PersonalLibraryResponse convertToResponse(PersonalLibrary personalLibrary) {
        PersonalLibraryResponse response = new PersonalLibraryResponse();
        response.setId(personalLibrary.getId());
        response.setBookId(personalLibrary.getBook().getId());
        response.setTitle(personalLibrary.getBook().getTitle());
        response.setAuthor(personalLibrary.getBook().getAuthor());
        response.setIsbn(personalLibrary.getBook().getIsbn());
        response.setDescription(personalLibrary.getBook().getDescription());
        response.setPublicationYear(personalLibrary.getBook().getPublicationYear());
        response.setPublisher(personalLibrary.getBook().getPublisher());
        response.setPageCount(personalLibrary.getBook().getPageCount());
        response.setCoverImageUrl(personalLibrary.getBook().getCoverImageUrl());
        
        // Category information
        if (personalLibrary.getBook().getCategory() != null) {
            response.setCategoryName(personalLibrary.getBook().getCategory().getName());
        }
        
        // Personal metadata
        response.setPhysicalLocation(personalLibrary.getPhysicalLocation());
        response.setReadingStatus(personalLibrary.getReadingStatus());
        response.setPersonalNotes(personalLibrary.getPersonalNotes());
        response.setDateAdded(personalLibrary.getDateAdded());
        response.setDateStarted(personalLibrary.getDateStarted());
        response.setDateCompleted(personalLibrary.getDateCompleted());
        
        return response;
    }
}