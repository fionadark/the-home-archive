package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.book.BookResponse;
import com.thehomearchive.library.entity.Book;
import com.thehomearchive.library.entity.Category;
import com.thehomearchive.library.repository.BookRepository;
import com.thehomearchive.library.repository.CategoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service for enriching book metadata using OpenLibrary API.
 * This service provides functionality to enrich existing books or create new books
 * with metadata from external sources, primarily OpenLibrary API.
 * 
 * Key responsibilities:
 * - Enrich book metadata from external APIs
 * - Create new books with enriched metadata
 * - Update existing books with additional metadata
 * - Manage category creation and association
 * - Handle duplicate detection and prevention
 */
@Service
@Transactional
public class BookMetadataService {

    private static final Logger logger = LoggerFactory.getLogger(BookMetadataService.class);
    private static final int DEFAULT_SEARCH_LIMIT = 10;

    private final OpenLibraryService openLibraryService;
    private final BookRepository bookRepository;
    private final CategoryRepository categoryRepository;

    public BookMetadataService(OpenLibraryService openLibraryService,
                              BookRepository bookRepository,
                              CategoryRepository categoryRepository) {
        this.openLibraryService = openLibraryService;
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
    }

    /**
     * Enrich book metadata using ISBN.
     * First checks if book already exists in database, if not, fetches from OpenLibrary
     * and creates a new book with enriched metadata.
     * 
     * @param isbn The ISBN to search for
     * @return Optional containing the enriched Book, or empty if not found
     */
    public Optional<Book> enrichBookMetadata(String isbn) {
        if (!StringUtils.hasText(isbn)) {
            logger.debug("ISBN is null or empty, cannot enrich metadata");
            return Optional.empty();
        }

        logger.info("Enriching book metadata for ISBN: {}", isbn);

        // First check if book already exists in our database
        Optional<Book> existingBook = bookRepository.findByIsbn(isbn);
        if (existingBook.isPresent()) {
            logger.debug("Book with ISBN {} already exists in database", isbn);
            return existingBook;
        }

        // Search OpenLibrary for book metadata
        List<BookResponse> searchResults = openLibraryService.searchByIsbn(isbn);
        if (searchResults.isEmpty()) {
            logger.warn("No results found in OpenLibrary for ISBN: {}", isbn);
            return Optional.empty();
        }

        // Use the first result to create and save the book
        BookResponse bookResponse = searchResults.get(0);
        Book enrichedBook = createBookFromResponse(bookResponse);
        Book savedBook = bookRepository.save(enrichedBook);

        logger.info("Successfully enriched and saved book: {} by {}", 
                   savedBook.getTitle(), savedBook.getAuthor());
        return Optional.of(savedBook);
    }

    /**
     * Enrich multiple books by title search.
     * Searches OpenLibrary for books matching the title and creates new book entries
     * for those not already in the database.
     * 
     * @param title The title to search for
     * @return List of enriched books
     */
    public List<Book> enrichBooksByTitle(String title) {
        if (!StringUtils.hasText(title)) {
            logger.debug("Title is null or empty, cannot enrich metadata");
            return new ArrayList<>();
        }

        logger.info("Enriching books by title: {}", title);

        // Check what books we already have for this title
        List<Book> existingBooks = bookRepository.findByTitleContainingIgnoreCase(title);
        List<Book> enrichedBooks = new ArrayList<>(existingBooks);

        // Search OpenLibrary for additional books
        List<BookResponse> searchResults = openLibraryService.searchByTitle(title, DEFAULT_SEARCH_LIMIT);
        
        for (BookResponse response : searchResults) {
            // Check if we already have this book (by ISBN or exact title/author match)
            if (isBookAlreadyExists(response, enrichedBooks)) {
                continue;
            }

            try {
                Book newBook = createBookFromResponse(response);
                Book savedBook = bookRepository.save(newBook);
                enrichedBooks.add(savedBook);
                logger.debug("Added new book from OpenLibrary: {} by {}", 
                           savedBook.getTitle(), savedBook.getAuthor());
            } catch (Exception e) {
                logger.warn("Failed to create book from OpenLibrary response: {}", e.getMessage());
            }
        }

        logger.info("Enrichment complete. Found {} books for title '{}'", enrichedBooks.size(), title);
        return enrichedBooks;
    }

    /**
     * Enrich multiple books by author search.
     * Searches OpenLibrary for books by the specified author and creates new book entries
     * for those not already in the database.
     * 
     * @param author The author to search for
     * @return List of enriched books
     */
    public List<Book> enrichBooksByAuthor(String author) {
        if (!StringUtils.hasText(author)) {
            logger.debug("Author is null or empty, cannot enrich metadata");
            return new ArrayList<>();
        }

        logger.info("Enriching books by author: {}", author);

        // Check what books we already have for this author
        List<Book> existingBooks = bookRepository.findByAuthorContainingIgnoreCase(author);
        List<Book> enrichedBooks = new ArrayList<>(existingBooks);

        // Search OpenLibrary for additional books
        List<BookResponse> searchResults = openLibraryService.searchByAuthor(author, DEFAULT_SEARCH_LIMIT);
        
        for (BookResponse response : searchResults) {
            // Check if we already have this book
            if (isBookAlreadyExists(response, enrichedBooks)) {
                continue;
            }

            try {
                Book newBook = createBookFromResponse(response);
                Book savedBook = bookRepository.save(newBook);
                enrichedBooks.add(savedBook);
                logger.debug("Added new book from OpenLibrary: {} by {}", 
                           savedBook.getTitle(), savedBook.getAuthor());
            } catch (Exception e) {
                logger.warn("Failed to create book from OpenLibrary response: {}", e.getMessage());
            }
        }

        logger.info("Enrichment complete. Found {} books for author '{}'", enrichedBooks.size(), author);
        return enrichedBooks;
    }

    /**
     * Update an existing book with metadata from OpenLibrary.
     * Uses the book's ISBN to fetch additional metadata and update the book.
     * 
     * @param book The book to update
     * @return Optional containing the updated Book, or empty if update failed
     */
    public Optional<Book> updateBookFromOpenLibrary(Book book) {
        if (book == null || !StringUtils.hasText(book.getIsbn())) {
            logger.debug("Book or ISBN is null/empty, cannot update from OpenLibrary");
            return Optional.empty();
        }

        logger.info("Updating book from OpenLibrary: {} (ISBN: {})", book.getTitle(), book.getIsbn());

        List<BookResponse> searchResults = openLibraryService.searchByIsbn(book.getIsbn());
        if (searchResults.isEmpty()) {
            logger.warn("No OpenLibrary results found for ISBN: {}", book.getIsbn());
            return Optional.empty();
        }

        BookResponse response = searchResults.get(0);
        updateBookFromResponse(book, response);
        Book updatedBook = bookRepository.save(book);

        logger.info("Successfully updated book from OpenLibrary: {}", updatedBook.getTitle());
        return Optional.of(updatedBook);
    }

    /**
     * Create or find a category by name.
     * If the category doesn't exist, creates a new one.
     * 
     * @param categoryName The name of the category
     * @return The category (existing or newly created)
     */
    public Category createCategoryIfNotExists(String categoryName) {
        if (!StringUtils.hasText(categoryName)) {
            // Return a default category for uncategorized books
            return createCategoryIfNotExists("Uncategorized");
        }

        Optional<Category> existingCategory = categoryRepository.findByNameIgnoreCase(categoryName);
        if (existingCategory.isPresent()) {
            return existingCategory.get();
        }

        // Create new category
        Category newCategory = new Category();
        newCategory.setName(categoryName);
        
        String slug = generateSlug(categoryName);
        // Ensure slug is unique
        String uniqueSlug = ensureUniqueSlug(slug);
        newCategory.setSlug(uniqueSlug);
        newCategory.setDescription("Auto-generated category from book metadata enrichment");

        try {
            Category savedCategory = categoryRepository.save(newCategory);
            logger.info("Created new category: {}", savedCategory.getName());
            return savedCategory;
        } catch (Exception e) {
            logger.warn("Failed to create category '{}', attempting to find existing: {}", categoryName, e.getMessage());
            // If creation failed, try to find by name again (race condition)
            Optional<Category> retryFind = categoryRepository.findByNameIgnoreCase(categoryName);
            if (retryFind.isPresent()) {
                return retryFind.get();
            }
            // If still not found, return default category
            logger.error("Failed to create or find category '{}', using default", categoryName);
            return createCategoryIfNotExists("Fiction");
        }
    }

    /**
     * Create a new Book entity from OpenLibrary BookResponse.
     * 
     * @param response The BookResponse from OpenLibrary
     * @return A new Book entity with enriched metadata
     */
    private Book createBookFromResponse(BookResponse response) {
        Book book = new Book();
        updateBookFromResponse(book, response);
        return book;
    }

    /**
     * Update an existing Book entity with data from OpenLibrary BookResponse.
     * 
     * @param book The book to update
     * @param response The BookResponse from OpenLibrary
     */
    private void updateBookFromResponse(Book book, BookResponse response) {
        // Required fields
        if (StringUtils.hasText(response.getTitle())) {
            book.setTitle(response.getTitle());
        }
        if (StringUtils.hasText(response.getAuthor())) {
            book.setAuthor(response.getAuthor());
        }

        // Optional fields - only update if not already set or if new value is provided
        if (StringUtils.hasText(response.getIsbn())) {
            book.setIsbn(response.getIsbn());
        }
        if (StringUtils.hasText(response.getDescription())) {
            book.setDescription(response.getDescription());
        }
        if (response.getPublicationYear() != null) {
            book.setPublicationYear(response.getPublicationYear());
        }
        if (StringUtils.hasText(response.getPublisher())) {
            book.setPublisher(response.getPublisher());
        }
        if (response.getPageCount() != null) {
            book.setPageCount(response.getPageCount());
        }
        if (StringUtils.hasText(response.getCoverImageUrl())) {
            book.setCoverImageUrl(response.getCoverImageUrl());
        }

        // Handle category
        if (StringUtils.hasText(response.getCategoryName())) {
            Category category = createCategoryIfNotExists(response.getCategoryName());
            book.setCategory(category);
        } else {
            // Assign default category if none provided
            Category defaultCategory = createCategoryIfNotExists("Fiction");
            book.setCategory(defaultCategory);
        }
    }

    /**
     * Check if a book already exists in the provided list.
     * Uses ISBN first, then falls back to title/author matching.
     * 
     * @param response The BookResponse to check
     * @param existingBooks List of existing books
     * @return true if book already exists, false otherwise
     */
    private boolean isBookAlreadyExists(BookResponse response, List<Book> existingBooks) {
        // Check by ISBN first (most reliable)
        if (StringUtils.hasText(response.getIsbn())) {
            for (Book book : existingBooks) {
                if (response.getIsbn().equals(book.getIsbn())) {
                    return true;
                }
            }
        }

        // Fall back to title/author matching
        if (StringUtils.hasText(response.getTitle()) && StringUtils.hasText(response.getAuthor())) {
            for (Book book : existingBooks) {
                if (response.getTitle().equalsIgnoreCase(book.getTitle()) &&
                    response.getAuthor().equalsIgnoreCase(book.getAuthor())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Generate a URL-friendly slug from category name.
     * 
     * @param name The category name
     * @return A URL-friendly slug
     */
    private String generateSlug(String name) {
        return name.toLowerCase()
                  .replaceAll("[^a-z0-9\\s-]", "")
                  .replaceAll("\\s+", "-")
                  .replaceAll("-+", "-")
                  .replaceAll("^-|-$", "");
    }

    /**
     * Ensure slug is unique by checking database and appending number if needed.
     * 
     * @param baseSlug The base slug to check
     * @return A unique slug
     */
    private String ensureUniqueSlug(String baseSlug) {
        String candidateSlug = baseSlug;
        int counter = 1;
        
        while (categoryRepository.existsBySlug(candidateSlug)) {
            candidateSlug = baseSlug + "-" + counter;
            counter++;
            if (counter > 100) { // Prevent infinite loops
                candidateSlug = baseSlug + "-" + System.currentTimeMillis();
                break;
            }
        }
        
        return candidateSlug;
    }
}