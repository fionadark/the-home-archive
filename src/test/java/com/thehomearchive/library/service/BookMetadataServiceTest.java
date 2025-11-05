package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.book.BookResponse;
import com.thehomearchive.library.entity.Book;
import com.thehomearchive.library.entity.Category;
import com.thehomearchive.library.repository.BookRepository;
import com.thehomearchive.library.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test class for BookMetadataService.
 * Tests book metadata enrichment functionality using OpenLibrary API.
 */
@ExtendWith(MockitoExtension.class)
class BookMetadataServiceTest {

    @Mock
    private OpenLibraryService openLibraryService;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookMetadataService bookMetadataService;

    private BookResponse sampleBookResponse;
    private Book sampleBook;
    private Category sampleCategory;

    @BeforeEach
    void setUp() {
        // Sample BookResponse from OpenLibrary
        sampleBookResponse = new BookResponse();
        sampleBookResponse.setTitle("The Great Gatsby");
        sampleBookResponse.setAuthor("F. Scott Fitzgerald");
        sampleBookResponse.setIsbn("9780743273565");
        sampleBookResponse.setDescription("A classic American novel about the Jazz Age.");
        sampleBookResponse.setPublicationYear(1925);
        sampleBookResponse.setPublisher("Scribner");
        sampleBookResponse.setPageCount(180);
        sampleBookResponse.setCoverImageUrl("https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg");

        // Sample Book entity
        sampleBook = new Book();
        sampleBook.setId(1L);
        sampleBook.setTitle("The Great Gatsby");
        sampleBook.setAuthor("F. Scott Fitzgerald");

        // Sample Category
        sampleCategory = new Category();
        sampleCategory.setId(1L);
        sampleCategory.setName("Fiction");
    }

    @Test
    void enrichBookMetadata_WithValidIsbn_ShouldEnrichFromOpenLibrary() {
        // Given
        String isbn = "9780743273565";
        when(openLibraryService.searchByIsbn(isbn)).thenReturn(Arrays.asList(sampleBookResponse));
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());
        when(categoryRepository.findByNameIgnoreCase("Fiction")).thenReturn(Optional.of(sampleCategory));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(1L);
            return book;
        });

        // When
        Optional<Book> result = bookMetadataService.enrichBookMetadata(isbn);

        // Then
        assertThat(result).isPresent();
        Book enrichedBook = result.get();
        assertThat(enrichedBook.getTitle()).isEqualTo("The Great Gatsby");
        assertThat(enrichedBook.getAuthor()).isEqualTo("F. Scott Fitzgerald");
        assertThat(enrichedBook.getIsbn()).isEqualTo("9780743273565");
        assertThat(enrichedBook.getDescription()).isEqualTo("A classic American novel about the Jazz Age.");
        assertThat(enrichedBook.getPublicationYear()).isEqualTo(1925);
        assertThat(enrichedBook.getPublisher()).isEqualTo("Scribner");
        assertThat(enrichedBook.getPageCount()).isEqualTo(180);
        assertThat(enrichedBook.getCoverImageUrl()).isEqualTo("https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg");

        verify(openLibraryService).searchByIsbn(isbn);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void enrichBookMetadata_WithExistingBook_ShouldReturnExistingBook() {
        // Given
        String isbn = "9780743273565";
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(sampleBook));

        // When
        Optional<Book> result = bookMetadataService.enrichBookMetadata(isbn);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(sampleBook);

        verify(bookRepository).findByIsbn(isbn);
        verifyNoInteractions(openLibraryService);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void enrichBookMetadata_WithNoOpenLibraryResults_ShouldReturnEmpty() {
        // Given
        String isbn = "9999999999999";
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());
        when(openLibraryService.searchByIsbn(isbn)).thenReturn(Collections.emptyList());

        // When
        Optional<Book> result = bookMetadataService.enrichBookMetadata(isbn);

        // Then
        assertThat(result).isEmpty();

        verify(openLibraryService).searchByIsbn(isbn);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void enrichBookMetadata_WithInvalidIsbn_ShouldReturnEmpty() {
        // Given
        String invalidIsbn = "";

        // When
        Optional<Book> result = bookMetadataService.enrichBookMetadata(invalidIsbn);

        // Then
        assertThat(result).isEmpty();

        verifyNoInteractions(openLibraryService);
        verifyNoInteractions(bookRepository);
    }

    @Test
    void enrichBookMetadata_WithNullIsbn_ShouldReturnEmpty() {
        // When
        Optional<Book> result = bookMetadataService.enrichBookMetadata(null);

        // Then
        assertThat(result).isEmpty();

        verifyNoInteractions(openLibraryService);
        verifyNoInteractions(bookRepository);
    }

    @Test
    void enrichBooksByTitle_ShouldSearchAndEnrichMultipleBooks() {
        // Given
        String title = "The Great Gatsby";
        BookResponse response1 = new BookResponse();
        response1.setTitle("The Great Gatsby");
        response1.setAuthor("F. Scott Fitzgerald");
        response1.setIsbn("9780743273565");

        BookResponse response2 = new BookResponse();
        response2.setTitle("The Great Gatsby (Annotated)");
        response2.setAuthor("F. Scott Fitzgerald");
        response2.setIsbn("9781234567890");

        when(openLibraryService.searchByTitle(title, 10)).thenReturn(Arrays.asList(response1, response2));
        when(bookRepository.findByTitleContainingIgnoreCase(title)).thenReturn(Collections.emptyList());
        when(categoryRepository.findByNameIgnoreCase("Fiction")).thenReturn(Optional.of(sampleCategory));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(book.getId() != null ? book.getId() : 1L);
            return book;
        });

        // When
        List<Book> result = bookMetadataService.enrichBooksByTitle(title);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTitle()).isEqualTo("The Great Gatsby");
        assertThat(result.get(1).getTitle()).isEqualTo("The Great Gatsby (Annotated)");

        verify(openLibraryService).searchByTitle(title, 10);
        verify(bookRepository, times(2)).save(any(Book.class));
    }

    @Test
    void enrichBooksByAuthor_ShouldSearchAndEnrichBooksByAuthor() {
        // Given
        String author = "F. Scott Fitzgerald";
        when(openLibraryService.searchByAuthor(author, 10)).thenReturn(Arrays.asList(sampleBookResponse));
        when(bookRepository.findByAuthorContainingIgnoreCase(author)).thenReturn(Collections.emptyList());
        when(categoryRepository.findByNameIgnoreCase("Fiction")).thenReturn(Optional.of(sampleCategory));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(1L);
            return book;
        });

        // When
        List<Book> result = bookMetadataService.enrichBooksByAuthor(author);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAuthor()).isEqualTo("F. Scott Fitzgerald");

        verify(openLibraryService).searchByAuthor(author, 10);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBookFromOpenLibrary_ShouldUpdateExistingBookWithNewMetadata() {
        // Given
        Book existingBook = new Book();
        existingBook.setId(1L);
        existingBook.setTitle("Basic Title");
        existingBook.setAuthor("Basic Author");
        existingBook.setIsbn("9780743273565");

        when(openLibraryService.searchByIsbn("9780743273565")).thenReturn(Arrays.asList(sampleBookResponse));
        when(categoryRepository.findByNameIgnoreCase("Fiction")).thenReturn(Optional.of(sampleCategory));
        when(bookRepository.save(any(Book.class))).thenReturn(existingBook);

        // When
        Optional<Book> result = bookMetadataService.updateBookFromOpenLibrary(existingBook);

        // Then
        assertThat(result).isPresent();
        Book updatedBook = result.get();
        assertThat(updatedBook.getDescription()).isEqualTo("A classic American novel about the Jazz Age.");
        assertThat(updatedBook.getPublisher()).isEqualTo("Scribner");
        assertThat(updatedBook.getPageCount()).isEqualTo(180);

        verify(openLibraryService).searchByIsbn("9780743273565");
        verify(bookRepository).save(existingBook);
    }

    @Test
    void updateBookFromOpenLibrary_WithNoIsbn_ShouldReturnEmpty() {
        // Given
        Book bookWithoutIsbn = new Book();
        bookWithoutIsbn.setTitle("Some Book");
        bookWithoutIsbn.setAuthor("Some Author");

        // When
        Optional<Book> result = bookMetadataService.updateBookFromOpenLibrary(bookWithoutIsbn);

        // Then
        assertThat(result).isEmpty();

        verifyNoInteractions(openLibraryService);
        verifyNoInteractions(bookRepository);
    }

    @Test
    void createCategoryIfNotExists_WithNewCategory_ShouldCreateCategory() {
        // Given
        String categoryName = "Science Fiction";
        when(categoryRepository.findByNameIgnoreCase(categoryName)).thenReturn(Optional.empty());
        when(categoryRepository.existsBySlug("science-fiction")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(2L);
            return category;
        });

        // When
        Category result = bookMetadataService.createCategoryIfNotExists(categoryName);

        // Then
        assertThat(result.getName()).isEqualTo("Science Fiction");
        assertThat(result.getId()).isEqualTo(2L);

        ArgumentCaptor<Category> categoryCaptor = ArgumentCaptor.forClass(Category.class);
        verify(categoryRepository).save(categoryCaptor.capture());
        assertThat(categoryCaptor.getValue().getName()).isEqualTo("Science Fiction");
        assertThat(categoryCaptor.getValue().getSlug()).isEqualTo("science-fiction");
    }

    @Test
    void createCategoryIfNotExists_WithExistingCategory_ShouldReturnExisting() {
        // Given
        String categoryName = "Fiction";
        when(categoryRepository.findByNameIgnoreCase(categoryName)).thenReturn(Optional.of(sampleCategory));

        // When
        Category result = bookMetadataService.createCategoryIfNotExists(categoryName);

        // Then
        assertThat(result).isEqualTo(sampleCategory);

        verify(categoryRepository).findByNameIgnoreCase(categoryName);
        verify(categoryRepository, never()).save(any(Category.class));
    }
}