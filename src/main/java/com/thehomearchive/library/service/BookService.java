package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.book.BookCreateRequest;
import com.thehomearchive.library.dto.book.BookResponse;
import com.thehomearchive.library.dto.book.BookUpdateRequest;
import com.thehomearchive.library.dto.book.BookValidationResponse;
import com.thehomearchive.library.entity.Book;
import com.thehomearchive.library.entity.Category;
import com.thehomearchive.library.repository.BookRepository;
import com.thehomearchive.library.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing book catalog operations.
 * Handles CRUD operations for books in the global catalog.
 */
@Service
@Transactional
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Get all books with pagination.
     */
    @Transactional(readOnly = true)
    public Page<BookResponse> getAllBooks(Pageable pageable) {
        Page<Book> books = bookRepository.findAll(pageable);
        return books.map(this::convertToResponse);
    }

    /**
     * Get a book by ID.
     */
    @Transactional(readOnly = true)
    public BookResponse getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + id));
        return convertToResponse(book);
    }

    /**
     * Get a book by ISBN.
     */
    @Transactional(readOnly = true)
    public BookResponse getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ISBN: " + isbn));
        return convertToResponse(book);
    }

    /**
     * Search books by title.
     */
    @Transactional(readOnly = true)
    public List<BookResponse> searchByTitle(String title) {
        List<Book> books = bookRepository.findByTitleContainingIgnoreCase(title);
        return books.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search books by author.
     */
    @Transactional(readOnly = true)
    public List<BookResponse> searchByAuthor(String author) {
        List<Book> books = bookRepository.findByAuthorContainingIgnoreCase(author);
        return books.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Search books by category.
     */
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByCategory(Long categoryId) {
        List<Book> books = bookRepository.findByCategoryId(categoryId);
        return books.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Full-text search across multiple fields.
     */
    @Transactional(readOnly = true)
    public List<BookResponse> searchBooks(String searchTerm) {
        List<Book> books = bookRepository.searchBooks(searchTerm);
        return books.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Full-text search with pagination.
     */
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooks(String searchTerm, Pageable pageable) {
        Page<Book> books = bookRepository.searchBooks(searchTerm, pageable);
        return books.map(this::convertToResponse);
    }

    /**
     * Advanced search with multiple criteria.
     */
    @Transactional(readOnly = true)
    public Page<BookResponse> searchBooksWithCriteria(
            String title, String author, Long categoryId, 
            Integer startYear, Integer endYear, Pageable pageable) {
        Page<Book> books = bookRepository.findBooksWithCriteria(
                title, author, categoryId, startYear, endYear, pageable);
        return books.map(this::convertToResponse);
    }

    /**
     * Get books not in a user's library.
     */
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksNotInUserLibrary(Long userId) {
        List<Book> books = bookRepository.findBooksNotInUserLibrary(userId);
        return books.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get books not in a user's library with pagination.
     */
    @Transactional(readOnly = true)
    public Page<BookResponse> getBooksNotInUserLibrary(Long userId, Pageable pageable) {
        Page<Book> books = bookRepository.findBooksNotInUserLibrary(userId, pageable);
        return books.map(this::convertToResponse);
    }

    /**
     * Create a new book in the catalog.
     */
    public BookResponse createBook(BookCreateRequest request) {
        // Check if book already exists by ISBN
        if (request.getIsbn() != null && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new IllegalArgumentException("Book with ISBN " + request.getIsbn() + " already exists");
        }

        // Check if book already exists by title and author
        if (bookRepository.existsByTitleAndAuthor(request.getTitle(), request.getAuthor())) {
            throw new IllegalArgumentException("Book with title '" + request.getTitle() + 
                    "' by " + request.getAuthor() + " already exists");
        }

        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setPublicationYear(request.getPublicationYear());
        book.setPublisher(request.getPublisher());
        book.setPageCount(request.getPageCount());
        book.setCoverImageUrl(request.getCoverImageUrl());

        // Set category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + request.getCategoryId()));
            book.setCategory(category);
        }

        Book saved = bookRepository.save(book);
        return convertToResponse(saved);
    }

    /**
     * Update an existing book.
     */
    public BookResponse updateBook(Long id, BookUpdateRequest request) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found with ID: " + id));

        // Check for ISBN conflicts if ISBN is being updated
        if (request.getIsbn() != null && !request.getIsbn().equals(book.getIsbn()) 
                && bookRepository.existsByIsbn(request.getIsbn())) {
            throw new IllegalArgumentException("Book with ISBN " + request.getIsbn() + " already exists");
        }

        // Update fields
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setDescription(request.getDescription());
        book.setPublicationYear(request.getPublicationYear());
        book.setPublisher(request.getPublisher());
        book.setPageCount(request.getPageCount());
        book.setCoverImageUrl(request.getCoverImageUrl());

        // Update category if provided
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + request.getCategoryId()));
            book.setCategory(category);
        } else {
            book.setCategory(null);
        }

        Book saved = bookRepository.save(book);
        return convertToResponse(saved);
    }

    /**
     * Delete a book from the catalog.
     * Note: This will also remove the book from all personal libraries.
     */
    public void deleteBook(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new IllegalArgumentException("Book not found with ID: " + id);
        }
        bookRepository.deleteById(id);
    }

    /**
     * Check if a book exists.
     */
    @Transactional(readOnly = true)
    public boolean bookExists(Long id) {
        return bookRepository.existsById(id);
    }

    /**
     * Check if a book with ISBN exists.
     */
    @Transactional(readOnly = true)
    public boolean bookExistsByIsbn(String isbn) {
        return bookRepository.existsByIsbn(isbn);
    }

    /**
     * Get books by publication year range.
     */
    @Transactional(readOnly = true)
    public List<BookResponse> getBooksByPublicationYearRange(Integer startYear, Integer endYear) {
        List<Book> books = bookRepository.findByPublicationYearBetween(startYear, endYear);
        return books.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert Book entity to response DTO.
     */
    private BookResponse convertToResponse(Book book) {
        BookResponse response = new BookResponse();
        response.setId(book.getId());
        response.setTitle(book.getTitle());
        response.setAuthor(book.getAuthor());
        response.setIsbn(book.getIsbn());
        response.setDescription(book.getDescription());
        response.setPublicationYear(book.getPublicationYear());
        response.setPublisher(book.getPublisher());
        response.setPageCount(book.getPageCount());
        response.setCoverImageUrl(book.getCoverImageUrl());
        response.setCreatedAt(book.getCreatedAt());
        response.setUpdatedAt(book.getUpdatedAt());

        if (book.getCategory() != null) {
            response.setCategoryId(book.getCategory().getId());
            response.setCategoryName(book.getCategory().getName());
        }

        return response;
    }

    /**
     * Validate a book by ISBN and check if it exists in database.
     * Also enriches book data from external sources if available.
     */
    @Transactional(readOnly = true)
    public BookValidationResponse validateBookByIsbn(String isbn) {
        if (isbn == null || isbn.trim().isEmpty()) {
            BookValidationResponse response = new BookValidationResponse(false, isbn);
            response.setErrorMessage("ISBN cannot be empty");
            return response;
        }

        // Clean and validate ISBN format
        String cleanIsbn = isbn.replaceAll("[^0-9X]", "").toUpperCase();
        if (!isValidIsbnFormat(cleanIsbn)) {
            BookValidationResponse response = new BookValidationResponse(false, isbn);
            response.setErrorMessage("Invalid ISBN format");
            return response;
        }

        BookValidationResponse response = new BookValidationResponse(true, isbn);

        // Check if book exists in database
        Optional<Book> existingBook = bookRepository.findByIsbn(cleanIsbn);
        if (existingBook.isPresent()) {
            Book book = existingBook.get();
            response.setExistsInDatabase(true);
            response.setBookId(book.getId());
            response.setTitle(book.getTitle());
            response.setAuthor(book.getAuthor());
            response.setPublisher(book.getPublisher());
            response.setPublicationYear(book.getPublicationYear());
            response.setPageCount(book.getPageCount());
            response.setDescription(book.getDescription());
            response.setCoverImageUrl(book.getCoverImageUrl());
        } else {
            // Try to enrich from external sources (e.g., Open Library API)
            enrichBookDataFromExternalSources(response, cleanIsbn);
        }

        return response;
    }

    /**
     * Validate ISBN format (both ISBN-10 and ISBN-13).
     */
    private boolean isValidIsbnFormat(String isbn) {
        if (isbn == null) return false;
        
        // Remove any remaining hyphens or spaces
        String cleanIsbn = isbn.replaceAll("[^0-9X]", "");
        
        // Check length
        if (cleanIsbn.length() != 10 && cleanIsbn.length() != 13) {
            return false;
        }
        
        // Basic format validation
        if (cleanIsbn.length() == 10) {
            // ISBN-10: Can have X as last character
            return cleanIsbn.matches("\\d{9}[\\dX]");
        } else {
            // ISBN-13: All digits
            return cleanIsbn.matches("\\d{13}");
        }
    }

    /**
     * Enrich book data from external sources like Open Library.
     * This is a simplified version - in production you'd integrate with actual APIs.
     */
    private void enrichBookDataFromExternalSources(BookValidationResponse response, String isbn) {
        // This is a mock implementation
        // In a real application, you would call external APIs like Open Library, Google Books, etc.
        
        response.setExistsInDatabase(false);
        response.setEnrichedFromExternalSource(true);
        response.setExternalSource("Open Library (Mock)");
        
        // Mock data based on well-known ISBNs for demo purposes
        if ("9780743273565".equals(isbn) || "0743273567".equals(isbn)) {
            response.setTitle("The Great Gatsby");
            response.setAuthor("F. Scott Fitzgerald");
            response.setPublisher("Scribner");
            response.setPublicationYear(1925);
            response.setPageCount(180);
            response.setDescription("A classic American novel about the Jazz Age");
            response.setCoverImageUrl("https://covers.openlibrary.org/b/isbn/9780743273565-L.jpg");
        } else if ("9780451524935".equals(isbn) || "0451524934".equals(isbn)) {
            response.setTitle("1984");
            response.setAuthor("George Orwell");
            response.setPublisher("Signet Classics");
            response.setPublicationYear(1949);
            response.setPageCount(328);
            response.setDescription("A dystopian social science fiction novel");
            response.setCoverImageUrl("https://covers.openlibrary.org/b/isbn/9780451524935-L.jpg");
        } else {
            // For unknown ISBNs, set minimal data
            response.setTitle("Unknown Title");
            response.setAuthor("Unknown Author");
            response.setDescription("Book data not available from external sources");
        }
    }
}