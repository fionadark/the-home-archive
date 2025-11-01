package com.thehomearchive.library.service;

import com.thehomearchive.library.entity.Book;
import com.thehomearchive.library.entity.Category;
import com.thehomearchive.library.repository.BookRepository;
import com.thehomearchive.library.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

/**
 * Integration test for BookMetadataService.
 * Tests the service with real OpenLibrary API calls.
 * These tests are conditional and only run when 'integration.test' system property is set.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@EnabledIfSystemProperty(named = "integration.test", matches = "true")
class BookMetadataServiceIntegrationTest {

    @Autowired
    private BookMetadataService bookMetadataService;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void enrichBookMetadata_WithValidIsbn_ShouldEnrichFromRealOpenLibraryAPI() {
        // Given - The Great Gatsby ISBN
        String isbn = "9780743273565";

        // When
        Optional<Book> result = bookMetadataService.enrichBookMetadata(isbn);

        // Then
        assertThat(result).isPresent();
        Book enrichedBook = result.get();
        
        // Verify basic required fields
        assertThat(enrichedBook.getTitle()).contains("Great Gatsby");
        assertThat(enrichedBook.getAuthor()).contains("Fitzgerald");
        assertThat(enrichedBook.getIsbn()).isEqualTo(isbn);
        
        // Verify the book was saved to database
        assertThat(enrichedBook.getId()).isNotNull();
        
        // Verify additional enriched metadata
        assertThat(enrichedBook.getDescription()).isNotBlank();
        assertThat(enrichedBook.getPublicationYear()).isNotNull();
        
        // Verify category was created/assigned
        assertThat(enrichedBook.getCategory()).isNotNull();
        assertThat(enrichedBook.getCategory().getName()).isNotBlank();
        
        System.out.println("✅ Successfully enriched book: " + enrichedBook.getTitle() + 
                          " by " + enrichedBook.getAuthor());
        System.out.println("   ISBN: " + enrichedBook.getIsbn());
        System.out.println("   Publication Year: " + enrichedBook.getPublicationYear());
        System.out.println("   Category: " + enrichedBook.getCategory().getName());
        System.out.println("   Description length: " + 
                          (enrichedBook.getDescription() != null ? enrichedBook.getDescription().length() : 0));
    }

    @Test
    void enrichBooksByTitle_ShouldFindMultipleBooksFromRealAPI() {
        // Given
        String title = "Pride and Prejudice";

        // When
        List<Book> results = bookMetadataService.enrichBooksByTitle(title);

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results.size()).isGreaterThan(0);
        
        // Verify at least one book contains the search title
        boolean foundMatchingTitle = results.stream()
            .anyMatch(book -> book.getTitle().toLowerCase().contains("pride"));
        assertThat(foundMatchingTitle).isTrue();
        
        // Verify all books have required fields
        for (Book book : results) {
            assertThat(book.getTitle()).isNotBlank();
            assertThat(book.getAuthor()).isNotBlank();
            assertThat(book.getId()).isNotNull(); // Saved to database
        }
        
        System.out.println("✅ Found " + results.size() + " books for title '" + title + "':");
        results.forEach(book -> 
            System.out.println("   - " + book.getTitle() + " by " + book.getAuthor()));
    }

    @Test
    void enrichBooksByAuthor_ShouldFindBooksFromRealAPI() {
        // Given
        String author = "Jane Austen";

        // When
        List<Book> results = bookMetadataService.enrichBooksByAuthor(author);

        // Then
        assertThat(results).isNotEmpty();
        
        // Verify at least one book is by the searched author
        boolean foundMatchingAuthor = results.stream()
            .anyMatch(book -> book.getAuthor().toLowerCase().contains("austen"));
        assertThat(foundMatchingAuthor).isTrue();
        
        // Verify all books have required fields
        for (Book book : results) {
            assertThat(book.getTitle()).isNotBlank();
            assertThat(book.getAuthor()).isNotBlank();
            assertThat(book.getId()).isNotNull(); // Saved to database
        }
        
        System.out.println("✅ Found " + results.size() + " books for author '" + author + "':");
        results.forEach(book -> 
            System.out.println("   - " + book.getTitle() + " by " + book.getAuthor()));
    }

    @Test
    void createCategoryIfNotExists_ShouldCreateNewCategory() {
        // Given
        String categoryName = "Test Integration Category";
        
        // Ensure category doesn't exist
        Optional<Category> existing = categoryRepository.findByNameIgnoreCase(categoryName);
        assertThat(existing).isEmpty();

        // When
        Category result = bookMetadataService.createCategoryIfNotExists(categoryName);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getName()).isEqualTo(categoryName);
        assertThat(result.getSlug()).isNotBlank();
        
        // Verify it was saved to database
        Optional<Category> saved = categoryRepository.findByNameIgnoreCase(categoryName);
        assertThat(saved).isPresent();
        assertThat(saved.get().getId()).isEqualTo(result.getId());
        
        System.out.println("✅ Created category: " + result.getName() + " (slug: " + result.getSlug() + ")");
    }

    @Test
    void updateBookFromOpenLibrary_ShouldEnrichExistingBook() {
        // Given - Create a minimal book first
        Book existingBook = new Book();
        existingBook.setTitle("Basic Title");
        existingBook.setAuthor("Basic Author");
        existingBook.setIsbn("9780141439518"); // Pride and Prejudice ISBN
        
        Category defaultCategory = bookMetadataService.createCategoryIfNotExists("Test");
        existingBook.setCategory(defaultCategory);
        
        Book savedBook = bookRepository.save(existingBook);
        
        // When
        Optional<Book> result = bookMetadataService.updateBookFromOpenLibrary(savedBook);

        // Then
        assertThat(result).isPresent();
        Book updatedBook = result.get();
        
        // Should have enriched metadata now
        assertThat(updatedBook.getDescription()).isNotBlank();
        assertThat(updatedBook.getPublicationYear()).isNotNull();
        
        // Basic fields should be updated with better data
        assertThat(updatedBook.getTitle()).contains("Pride");
        assertThat(updatedBook.getAuthor()).contains("Austen");
        
        System.out.println("✅ Updated book from basic info to enriched:");
        System.out.println("   Title: " + updatedBook.getTitle());
        System.out.println("   Author: " + updatedBook.getAuthor());
        System.out.println("   Publication Year: " + updatedBook.getPublicationYear());
        System.out.println("   Description length: " + 
                          (updatedBook.getDescription() != null ? updatedBook.getDescription().length() : 0));
    }

    @Test
    void enrichBookMetadata_WithDuplicateIsbn_ShouldReturnExistingBook() {
        // Given - First enrichment
        String isbn = "9780060935467"; // To Kill a Mockingbird
        Optional<Book> firstResult = bookMetadataService.enrichBookMetadata(isbn);
        assertThat(firstResult).isPresent();
        
        // When - Second enrichment with same ISBN
        Optional<Book> secondResult = bookMetadataService.enrichBookMetadata(isbn);

        // Then - Should return the existing book, not create a new one
        assertThat(secondResult).isPresent();
        assertThat(secondResult.get().getId()).isEqualTo(firstResult.get().getId());
        
        // Verify only one book exists with this ISBN
        List<Book> booksWithIsbn = bookRepository.findAll().stream()
            .filter(book -> isbn.equals(book.getIsbn()))
            .toList();
        assertThat(booksWithIsbn).hasSize(1);
        
        System.out.println("✅ Correctly returned existing book instead of creating duplicate");
        System.out.println("   Book: " + secondResult.get().getTitle() + " (ID: " + secondResult.get().getId() + ")");
    }
}