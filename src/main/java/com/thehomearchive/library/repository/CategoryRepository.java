package com.thehomearchive.library.repository;

import com.thehomearchive.library.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Category entity.
 * Implements CRUD operations for book categories.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * Find a category by its unique slug.
     */
    Optional<Category> findBySlug(String slug);

    /**
     * Find a category by name (case-insensitive).
     */
    Optional<Category> findByNameIgnoreCase(String name);

    /**
     * Find categories by name containing the search term (case-insensitive).
     */
    List<Category> findByNameContainingIgnoreCase(String name);

    /**
     * Find categories ordered by name.
     */
    List<Category> findAllByOrderByNameAsc();

    /**
     * Check if a category with the given name already exists.
     */
    boolean existsByNameIgnoreCase(String name);

    /**
     * Check if a category with the given slug already exists.
     */
    boolean existsBySlug(String slug);

    /**
     * Find categories with books count.
     * This query includes the count of books in each category.
     */
    @Query("SELECT c, COUNT(b) as bookCount FROM Category c LEFT JOIN c.books b GROUP BY c ORDER BY c.name")
    List<Object[]> findCategoriesWithBookCount();

    /**
     * Find categories that have at least one book.
     */
    @Query("SELECT DISTINCT c FROM Category c JOIN c.books b ORDER BY c.name")
    List<Category> findCategoriesWithBooks();

    /**
     * Find categories that have no books.
     */
    @Query("SELECT c FROM Category c WHERE c.id NOT IN (SELECT DISTINCT b.category.id FROM Book b WHERE b.category IS NOT NULL)")
    List<Category> findCategoriesWithoutBooks();

    /**
     * Count the number of books in a specific category.
     */
    @Query("SELECT COUNT(b) FROM Book b WHERE b.category.id = :categoryId")
    Long countBooksInCategory(@Param("categoryId") Long categoryId);
}