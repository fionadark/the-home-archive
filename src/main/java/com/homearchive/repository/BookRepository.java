package com.homearchive.repository;

import com.homearchive.entity.Book;
import com.homearchive.entity.PhysicalLocation;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for Book entity with search capabilities.
 * Implements full-text search across multiple fields with relevance scoring.
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    /**
     * Find all books ordered by title (for empty query).
     */
    @Query("SELECT b FROM Book b ORDER BY b.title ASC")
    List<Book> findAllOrderedByTitle(Pageable pageable);
    
    /**
     * Search books using full-text search with relevance scoring.
     * Title and author matches are prioritized with higher scores.
     */
    @Query(value = """
        SELECT b.*, 
               (CASE 
                   WHEN MATCH(b.title, b.author, b.description) AGAINST(:query IN NATURAL LANGUAGE MODE) > 0 
                   THEN MATCH(b.title, b.author, b.description) AGAINST(:query IN NATURAL LANGUAGE MODE) * 2.0
                   ELSE 0
               END +
               CASE 
                   WHEN MATCH(b.genre, b.publisher) AGAINST(:query IN NATURAL LANGUAGE MODE) > 0 
                   THEN MATCH(b.genre, b.publisher) AGAINST(:query IN NATURAL LANGUAGE MODE) * 1.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) THEN 3.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) THEN 2.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%')) THEN 1.5
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.isbn) = LOWER(:query) THEN 10.0
                   WHEN LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%')) THEN 4.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%')) THEN 1.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%')) THEN 1.0
                   ELSE 0
               END) as relevance_score
        FROM books b
        WHERE (
            MATCH(b.title, b.author, b.description) AGAINST(:query IN NATURAL LANGUAGE MODE)
            OR MATCH(b.genre, b.publisher) AGAINST(:query IN NATURAL LANGUAGE MODE)
            OR LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        HAVING relevance_score > 0
        ORDER BY relevance_score DESC, b.title ASC
        """, nativeQuery = true)
    List<Book> searchBooksWithRelevance(@Param("query") String query, Pageable pageable);
    
    /**
     * H2-compatible search method using only LIKE operations for testing.
     * Used when full-text search is not available (e.g., H2 test database).
     */
    @Query(value = """
        SELECT b.*, 
               (CASE 
                   WHEN LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) THEN 3.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) THEN 2.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%')) THEN 1.5
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.isbn) = LOWER(:query) THEN 10.0
                   WHEN LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%')) THEN 4.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%')) THEN 1.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%')) THEN 1.0
                   ELSE 0
               END) as relevance_score
        FROM books b
        WHERE (
            LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        HAVING relevance_score > 0
        ORDER BY relevance_score DESC, b.title ASC
        """, nativeQuery = true)
    List<Book> searchBooksWithRelevanceH2(@Param("query") String query, Pageable pageable);
    
    /**
     * Simple title/author search for basic functionality.
     */
    @Query("SELECT b FROM Book b WHERE " +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) " +
           "ORDER BY " +
           "CASE WHEN LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) THEN 1 ELSE 2 END, " +
           "b.title ASC")
    List<Book> searchByTitleAndAuthor(@Param("query") String query, Pageable pageable);
    
    /**
     * Count total results for a search query.
     */
    @Query(value = """
        SELECT COUNT(*) 
        FROM books b
        WHERE (
            MATCH(b.title, b.author, b.description) AGAINST(:query IN NATURAL LANGUAGE MODE)
            OR MATCH(b.genre, b.publisher) AGAINST(:query IN NATURAL LANGUAGE MODE)
            OR LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        """, nativeQuery = true)
    int countSearchResults(@Param("query") String query);
    
    /**
     * H2-compatible count method using only LIKE operations.
     */
    @Query(value = """
        SELECT COUNT(*) 
        FROM books b
        WHERE (
            LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        """, nativeQuery = true)
    int countSearchResultsH2(@Param("query") String query);
    
    /**
     * Search books by title only.
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY b.title ASC")
    List<Book> findByTitleContainingIgnoreCase(@Param("title") String title, Pageable pageable);
    
    /**
     * Search books by author only.
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')) ORDER BY b.author ASC, b.title ASC")
    List<Book> findByAuthorContainingIgnoreCase(@Param("author") String author, Pageable pageable);
    
    /**
     * Search books by genre.
     */
    @Query("SELECT b FROM Book b WHERE LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%')) ORDER BY b.title ASC")
    List<Book> findByGenreContainingIgnoreCase(@Param("genre") String genre, Pageable pageable);
    
    /**
     * Find books ordered by date added (most recent first).
     */
    @Query("SELECT b FROM Book b ORDER BY b.dateAdded DESC")
    List<Book> findAllOrderedByDateAdded(Pageable pageable);
    
    /**
     * Find books ordered by publication year.
     */
    @Query("SELECT b FROM Book b WHERE b.publicationYear IS NOT NULL ORDER BY b.publicationYear DESC")
    List<Book> findAllOrderedByPublicationYear(Pageable pageable);
    
    /**
     * Find books by physical location.
     */
    @Query("SELECT b FROM Book b WHERE b.physicalLocation = :location ORDER BY b.title ASC")
    List<Book> findByPhysicalLocation(@Param("location") PhysicalLocation location, Pageable pageable);
    
    /**
     * Search books with relevance scoring filtered by physical location.
     */
    @Query(value = """
        SELECT b.*, 
               (CASE 
                   WHEN MATCH(b.title, b.author, b.description) AGAINST(:query IN NATURAL LANGUAGE MODE) > 0 
                   THEN MATCH(b.title, b.author, b.description) AGAINST(:query IN NATURAL LANGUAGE MODE) * 2.0
                   ELSE 0
               END +
               CASE 
                   WHEN MATCH(b.genre, b.publisher) AGAINST(:query IN NATURAL LANGUAGE MODE) > 0 
                   THEN MATCH(b.genre, b.publisher) AGAINST(:query IN NATURAL LANGUAGE MODE) * 1.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) THEN 3.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) THEN 2.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%')) THEN 1.5
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.isbn) = LOWER(:query) THEN 10.0
                   WHEN LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%')) THEN 4.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%')) THEN 1.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%')) THEN 1.0
                   ELSE 0
               END) as relevance_score
        FROM books b
        WHERE (
            MATCH(b.title, b.author, b.description) AGAINST(:query IN NATURAL LANGUAGE MODE)
            OR MATCH(b.genre, b.publisher) AGAINST(:query IN NATURAL LANGUAGE MODE)
            OR LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))
        ) AND b.physical_location = :location
        HAVING relevance_score > 0
        ORDER BY relevance_score DESC, b.title ASC
        """, nativeQuery = true)
    List<Book> searchBooksWithRelevanceByLocation(@Param("query") String query, @Param("location") String location, Pageable pageable);
    
    /**
     * H2-compatible search method with location filter using only LIKE operations.
     */
    @Query(value = """
        SELECT b.*, 
               (CASE 
                   WHEN LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) THEN 3.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%')) THEN 2.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%')) THEN 1.5
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.isbn) = LOWER(:query) THEN 10.0
                   WHEN LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%')) THEN 4.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%')) THEN 1.0
                   ELSE 0
               END +
               CASE 
                   WHEN LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%')) THEN 1.0
                   ELSE 0
               END) as relevance_score
        FROM books b
        WHERE (
            LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))
        ) AND b.physical_location = :location
        HAVING relevance_score > 0
        ORDER BY relevance_score DESC, b.title ASC
        """, nativeQuery = true)
    List<Book> searchBooksWithRelevanceByLocationH2(@Param("query") String query, @Param("location") String location, Pageable pageable);
    
    /**
     * Simple title/author search filtered by physical location.
     */
    @Query("SELECT b FROM Book b WHERE b.physicalLocation = :location AND (" +
           "LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))" +
           ") ORDER BY " +
           "CASE WHEN LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%')) THEN 1 ELSE 2 END, " +
           "b.title ASC")
    List<Book> searchByTitleAndAuthorByLocation(@Param("query") String query, @Param("location") PhysicalLocation location, Pageable pageable);
    
    /**
     * Count total results for a search query filtered by physical location.
     */
    @Query(value = """
        SELECT COUNT(*) 
        FROM books b
        WHERE (
            MATCH(b.title, b.author, b.description) AGAINST(:query IN NATURAL LANGUAGE MODE)
            OR MATCH(b.genre, b.publisher) AGAINST(:query IN NATURAL LANGUAGE MODE)
            OR LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))
        ) AND b.physical_location = :location
        """, nativeQuery = true)
    int countSearchResultsByLocation(@Param("query") String query, @Param("location") String location);
    
    /**
     * H2-compatible count method for location search using only LIKE operations.
     */
    @Query(value = """
        SELECT COUNT(*) 
        FROM books b
        WHERE (
            LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :query, '%'))
            OR LOWER(b.description) LIKE LOWER(CONCAT('%', :query, '%'))
        ) AND b.physical_location = :location
        """, nativeQuery = true)
    int countSearchResultsByLocationH2(@Param("query") String query, @Param("location") String location);
    
    /**
     * Find all books ordered by title filtered by physical location.
     */
    @Query("SELECT b FROM Book b WHERE b.physicalLocation = :location ORDER BY b.title ASC")
    List<Book> findAllOrderedByTitleByLocation(@Param("location") PhysicalLocation location, Pageable pageable);
    
    /**
     * Count books by physical location.
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.physicalLocation = :location")
    int countByPhysicalLocation(@Param("location") PhysicalLocation location);
}