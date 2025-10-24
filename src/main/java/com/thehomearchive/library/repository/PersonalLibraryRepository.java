package com.thehomearchive.library.repository;

import com.thehomearchive.library.entity.PersonalLibrary;
import com.thehomearchive.library.entity.ReadingStatus;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for PersonalLibrary entity.
 * Manages the relationship between users and their personal book collections.
 */
@Repository
public interface PersonalLibraryRepository extends JpaRepository<PersonalLibrary, Long> {

    /**
     * Find all books in a user's personal library.
     */
    List<PersonalLibrary> findByUserOrderByDateAddedDesc(User user);

    /**
     * Find all books in a user's personal library by user ID.
     */
    List<PersonalLibrary> findByUserIdOrderByDateAddedDesc(Long userId);

    /**
     * Find all books in a user's personal library with pagination.
     */
    Page<PersonalLibrary> findByUserId(Long userId, Pageable pageable);

    /**
     * Find a specific book in a user's personal library.
     */
    Optional<PersonalLibrary> findByUserIdAndBookId(Long userId, Long bookId);

    /**
     * Find books by reading status for a user.
     */
    List<PersonalLibrary> findByUserIdAndReadingStatusOrderByDateAddedDesc(Long userId, ReadingStatus readingStatus);

    /**
     * Find books by physical location for a user.
     */
    List<PersonalLibrary> findByUserIdAndPhysicalLocationContainingIgnoreCaseOrderByDateAddedDesc(Long userId, String location);

    /**
     * Search books in a user's library by title or author.
     */
    @Query("SELECT pl FROM PersonalLibrary pl WHERE pl.user.id = :userId AND " +
           "(LOWER(pl.book.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pl.book.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<PersonalLibrary> searchUserLibrary(@Param("userId") Long userId, @Param("searchTerm") String searchTerm);

    /**
     * Search books in a user's library with pagination.
     */
    @Query("SELECT pl FROM PersonalLibrary pl WHERE pl.user.id = :userId AND " +
           "(LOWER(pl.book.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(pl.book.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<PersonalLibrary> searchUserLibrary(@Param("userId") Long userId, @Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Advanced search with multiple criteria.
     */
    @Query("SELECT pl FROM PersonalLibrary pl WHERE pl.user.id = :userId AND " +
           "(:title IS NULL OR LOWER(pl.book.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:author IS NULL OR LOWER(pl.book.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:readingStatus IS NULL OR pl.readingStatus = :readingStatus) AND " +
           "(:physicalLocation IS NULL OR LOWER(pl.physicalLocation) LIKE LOWER(CONCAT('%', :physicalLocation, '%')))")
    Page<PersonalLibrary> findUserLibraryWithCriteria(
            @Param("userId") Long userId,
            @Param("title") String title,
            @Param("author") String author,
            @Param("readingStatus") ReadingStatus readingStatus,
            @Param("physicalLocation") String physicalLocation,
            Pageable pageable);

    /**
     * Check if a book is already in a user's personal library.
     */
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    /**
     * Count books in a user's library.
     */
    Long countByUserId(Long userId);

    /**
     * Count books by reading status for a user.
     */
    Long countByUserIdAndReadingStatus(Long userId, ReadingStatus readingStatus);

    /**
     * Find books in a user's library by category.
     */
    @Query("SELECT pl FROM PersonalLibrary pl WHERE pl.user.id = :userId AND pl.book.category.id = :categoryId")
    List<PersonalLibrary> findByUserIdAndBookCategoryId(@Param("userId") Long userId, @Param("categoryId") Long categoryId);

    /**
     * Find recently added books to a user's library.
     */
    @Query("SELECT pl FROM PersonalLibrary pl WHERE pl.user.id = :userId ORDER BY pl.dateAdded DESC")
    List<PersonalLibrary> findRecentlyAddedBooks(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find currently reading books for a user.
     */
    List<PersonalLibrary> findByUserIdAndReadingStatus(Long userId, ReadingStatus readingStatus);

    /**
     * Get library statistics for a user.
     */
    @Query("SELECT pl.readingStatus, COUNT(pl) FROM PersonalLibrary pl WHERE pl.user.id = :userId GROUP BY pl.readingStatus")
    List<Object[]> getLibraryStatistics(@Param("userId") Long userId);

    /**
     * Delete a book from a user's personal library.
     */
    void deleteByUserIdAndBookId(Long userId, Long bookId);

    /**
     * Find all personal library entries for a specific book (across all users).
     */
    List<PersonalLibrary> findByBook(Book book);

    /**
     * Find all personal library entries for a specific book ID (across all users).
     */
    List<PersonalLibrary> findByBookId(Long bookId);
}