package com.thehomearchive.library.repository;

import com.thehomearchive.library.entity.Book;
import com.thehomearchive.library.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Book entity with search capabilities.
 * Implements CRUD operations and search functionality for the book catalog.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {

    /**
     * Find a book by ISBN.
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Find books by title containing the search term (case-insensitive).
     */
    List<Book> findByTitleContainingIgnoreCase(String title);

    /**
     * Find books by author containing the search term (case-insensitive).
     */
    List<Book> findByAuthorContainingIgnoreCase(String author);

    /**
     * Find books by category.
     */
    List<Book> findByCategory(Category category);

    /**
     * Find books by category ID.
     */
    List<Book> findByCategoryId(Long categoryId);

    /**
     * Find books by category ID with pagination.
     */
    Page<Book> findByCategoryId(Long categoryId, Pageable pageable);

    /**
     * Find books by publication year.
     */
    List<Book> findByPublicationYear(Integer publicationYear);

    /**
     * Find books by publication year range.
     */
    List<Book> findByPublicationYearBetween(Integer startYear, Integer endYear);

    /**
     * Find books by publisher containing the search term (case-insensitive).
     */
    List<Book> findByPublisherContainingIgnoreCase(String publisher);

    /**
     * Full-text search across title, author, and description.
     * Uses database full-text search capabilities.
     */
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Book> searchBooks(@Param("searchTerm") String searchTerm);

    /**
     * Full-text search with pagination.
     */
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Book> searchBooks(@Param("searchTerm") String searchTerm, Pageable pageable);

    /**
     * Find books with detailed search criteria.
     */
    @Query("SELECT b FROM Book b WHERE " +
           "(:title IS NULL OR LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:author IS NULL OR LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))) AND " +
           "(:categoryId IS NULL OR b.category.id = :categoryId) AND " +
           "(:startYear IS NULL OR b.publicationYear >= :startYear) AND " +
           "(:endYear IS NULL OR b.publicationYear <= :endYear)")
    Page<Book> findBooksWithCriteria(
            @Param("title") String title,
            @Param("author") String author,
            @Param("categoryId") Long categoryId,
            @Param("startYear") Integer startYear,
            @Param("endYear") Integer endYear,
            Pageable pageable);

    /**
     * Find books that are not in any user's personal library.
     */
    @Query("SELECT b FROM Book b WHERE b.id NOT IN " +
           "(SELECT pl.book.id FROM PersonalLibrary pl WHERE pl.user.id = :userId)")
    List<Book> findBooksNotInUserLibrary(@Param("userId") Long userId);

    /**
     * Find books that are not in any user's personal library with pagination.
     */
    @Query("SELECT b FROM Book b WHERE b.id NOT IN " +
           "(SELECT pl.book.id FROM PersonalLibrary pl WHERE pl.user.id = :userId)")
    Page<Book> findBooksNotInUserLibrary(@Param("userId") Long userId, Pageable pageable);

    /**
     * Count books by category.
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.category.id = :categoryId")
    Long countBooksByCategory(@Param("categoryId") Long categoryId);

    /**
     * Find books with no category assigned.
     */
    List<Book> findByCategoryIsNull();

    /**
     * Check if a book with the given ISBN already exists.
     */
    boolean existsByIsbn(String isbn);

    /**
     * Check if a book with the given title and author already exists.
     */
    boolean existsByTitleAndAuthor(String title, String author);
}