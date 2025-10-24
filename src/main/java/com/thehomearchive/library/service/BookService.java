package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.book.BookCreateRequest;
import com.thehomearchive.library.dto.book.BookResponse;
import com.thehomearchive.library.dto.book.BookUpdateRequest;
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
}