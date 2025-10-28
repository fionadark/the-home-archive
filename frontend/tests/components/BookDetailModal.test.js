import { BookDetailModal } from '../../src/js/components/BookDetailModal.js';

// Mock the services
jest.mock('../../src/js/services/libraryService.js', () => ({
    LibraryService: jest.fn().mockImplementation(() => ({
        isBookInLibrary: jest.fn(),
        addBookToLibrary: jest.fn(),
        removeBookFromLibrary: jest.fn()
    }))
}));

jest.mock('../../src/js/services/BookService.js', () => ({
    BookService: jest.fn().mockImplementation(() => ({
        // Mock methods as needed
    }))
}));

jest.mock('../../src/js/services/NotificationService.js', () => ({
    NotificationService: jest.fn().mockImplementation(() => ({
        showSuccess: jest.fn(),
        showError: jest.fn(),
        showWarning: jest.fn()
    }))
}));

describe('BookDetailModal', () => {
    let bookDetailModal;
    let mockBook;

    beforeEach(() => {
        // Create the HTML structure needed by the modal
        document.body.innerHTML = `
            <div id="book-modal-overlay" class="book-modal-overlay" role="dialog" aria-labelledby="book-modal-title" aria-hidden="true">
                <div id="book-modal" class="book-modal">
                    <div class="modal-header">
                        <h2 id="book-modal-title" class="modal-title">Book Details</h2>
                        <button id="modal-close" class="modal-close" aria-label="Close modal">×</button>
                    </div>
                    <div class="modal-content" id="modal-content">
                        <div id="modal-loading" class="modal-loading">
                            <div class="loading-spinner">Loading...</div>
                        </div>
                        <div id="book-details" class="book-details" style="display: none;">
                            <div class="book-header">
                                <div class="book-cover-section">
                                    <div class="book-cover-container">
                                        <img id="book-cover" class="book-cover" src="" alt="Book cover">
                                        <div id="cover-placeholder" class="cover-placeholder">
                                            <i class="fas fa-book"></i>
                                        </div>
                                    </div>
                                    <div class="book-actions">
                                        <button id="add-to-library" class="btn-primary btn-add-library">
                                            Add to Library
                                        </button>
                                        <button id="remove-from-library" class="btn-secondary btn-remove-library" style="display: none;">
                                            In Library
                                        </button>
                                    </div>
                                </div>
                                <div class="book-info-section">
                                    <div class="book-title-group">
                                        <h1 id="book-title" class="book-title"></h1>
                                        <p id="book-author" class="book-author"></p>
                                    </div>
                                    <div class="book-metadata">
                                        <div class="metadata-row">
                                            <span class="metadata-label">Category:</span>
                                            <span id="book-category" class="metadata-value"></span>
                                        </div>
                                        <div class="metadata-row">
                                            <span class="metadata-label">Published:</span>
                                            <span id="book-publication" class="metadata-value"></span>
                                        </div>
                                        <div class="metadata-row">
                                            <span class="metadata-label">ISBN:</span>
                                            <span id="book-isbn" class="metadata-value"></span>
                                        </div>
                                        <div class="metadata-row">
                                            <span class="metadata-label">Pages:</span>
                                            <span id="book-pages" class="metadata-value"></span>
                                        </div>
                                    </div>
                                    <div class="rating-section">
                                        <div class="average-rating">
                                            <div id="average-rating-stars" class="rating-stars"></div>
                                            <span class="rating-text">
                                                <span id="average-rating-value">0.0</span>
                                                (<span id="rating-count">0</span> ratings)
                                            </span>
                                        </div>
                                        <div id="user-rating-section" class="user-rating">
                                            <label class="rating-label">Your Rating:</label>
                                            <div id="user-rating-input" class="rating-input">
                                                <button class="star-btn" data-rating="1"><i class="far fa-star"></i></button>
                                                <button class="star-btn" data-rating="2"><i class="far fa-star"></i></button>
                                                <button class="star-btn" data-rating="3"><i class="far fa-star"></i></button>
                                                <button class="star-btn" data-rating="4"><i class="far fa-star"></i></button>
                                                <button class="star-btn" data-rating="5"><i class="far fa-star"></i></button>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="book-description" id="book-description-section">
                                <h3 class="section-title">Description</h3>
                                <div class="description-content">
                                    <p id="book-description" class="book-description-text"></p>
                                    <button id="description-toggle" class="description-toggle" style="display: none;">
                                        Show more
                                    </button>
                                </div>
                            </div>
                            <div class="review-section" id="review-section">
                                <h3 class="section-title">Your Review</h3>
                                <form id="review-form" class="review-form">
                                    <div class="review-input-group">
                                        <textarea id="review-text" class="review-textarea"></textarea>
                                        <div class="review-actions">
                                            <span class="character-count">
                                                <span id="review-char-count">0</span>/2000
                                            </span>
                                            <div class="review-buttons">
                                                <button type="button" id="clear-review" class="btn-secondary">Clear</button>
                                                <button type="submit" id="save-review" class="btn-primary">Save Review</button>
                                            </div>
                                        </div>
                                    </div>
                                </form>
                            </div>
                            <div id="similar-books-section" class="similar-books-section">
                                <h3 class="section-title">Similar Books</h3>
                                <div id="similar-books-grid" class="similar-books-grid"></div>
                            </div>
                            <div id="recent-reviews-section" class="recent-reviews-section">
                                <h3 class="section-title">Recent Reviews</h3>
                                <div id="reviews-list" class="reviews-list"></div>
                                <button id="load-more-reviews" class="btn-load-more" style="display: none;">
                                    Load More Reviews
                                </button>
                            </div>
                        </div>
                        <div id="modal-error" class="modal-error" style="display: none;">
                            <div class="error-content">
                                <h3>Error Loading Book</h3>
                                <p id="error-message">Unable to load book details. Please try again.</p>
                                <button id="retry-load" class="btn-primary">Retry</button>
                            </div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button id="modal-cancel" class="btn-secondary">Close</button>
                    </div>
                </div>
            </div>
        `;

        // Mock book data
        mockBook = {
            id: 1,
            title: 'The Great Gatsby',
            author: 'F. Scott Fitzgerald',
            isbn: '9780743273565',
            description: 'A classic American novel set in the summer of 1922 on prosperous Long Island and in New York City.',
            publicationYear: 1925,
            publisher: 'Scribner',
            pageCount: 180,
            category: {
                id: 1,
                name: 'Fiction',
                slug: 'fiction'
            },
            coverImageUrl: 'https://example.com/cover.jpg',
            averageRating: 4.2,
            ratingCount: 1523
        };

        // Mock fetch globally
        global.fetch = jest.fn();

        // Initialize the component
        bookDetailModal = new BookDetailModal();
    });

    afterEach(() => {
        jest.clearAllMocks();
        global.fetch.mockRestore();
    });

    describe('Initialization', () => {
        test('should initialize correctly', () => {
            expect(bookDetailModal).toBeInstanceOf(BookDetailModal);
            expect(bookDetailModal.overlay).toBeTruthy();
            expect(bookDetailModal.modal).toBeTruthy();
            expect(bookDetailModal.isVisible).toBe(false);
        });

        test('should bind all required elements', () => {
            expect(bookDetailModal.elements.title).toBeTruthy();
            expect(bookDetailModal.elements.author).toBeTruthy();
            expect(bookDetailModal.elements.description).toBeTruthy();
            expect(bookDetailModal.elements.addToLibrary).toBeTruthy();
            expect(bookDetailModal.elements.removeFromLibrary).toBeTruthy();
        });
    });

    describe('Modal Visibility', () => {
        test('should show modal when show() is called', async () => {
            // Mock successful API responses for all calls made by show()
            global.fetch
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(mockBook) }) // book details
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(false) }) // library status
                .mockResolvedValueOnce({ ok: false, status: 404 }) // user rating
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve([]) }) // similar books
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve({ content: [] }) }); // reviews

            await bookDetailModal.show(1);

            expect(bookDetailModal.isVisible).toBe(true);
            expect(bookDetailModal.overlay.classList.contains('show')).toBe(true);
            expect(document.body.style.overflow).toBe('hidden');
        });

        test('should hide modal when hide() is called', () => {
            bookDetailModal.isVisible = true;
            bookDetailModal.overlay.classList.add('show');

            bookDetailModal.hide();

            expect(bookDetailModal.isVisible).toBe(false);
            expect(bookDetailModal.overlay.classList.contains('show')).toBe(false);
            expect(document.body.style.overflow).toBe('');
        });

        test('should close modal when escape key is pressed', () => {
            bookDetailModal.isVisible = true;
            const hideSpy = jest.spyOn(bookDetailModal, 'hide');

            const escapeEvent = new KeyboardEvent('keydown', { key: 'Escape' });
            document.dispatchEvent(escapeEvent);

            expect(hideSpy).toHaveBeenCalled();
        });

        test('should close modal when close button is clicked', () => {
            const hideSpy = jest.spyOn(bookDetailModal, 'hide');
            
            bookDetailModal.elements.closeButton.click();

            expect(hideSpy).toHaveBeenCalled();
        });

        test('should close modal when overlay is clicked', () => {
            const hideSpy = jest.spyOn(bookDetailModal, 'hide');
            
            // Create click event on overlay
            const clickEvent = new MouseEvent('click', { bubbles: true });
            Object.defineProperty(clickEvent, 'target', { value: bookDetailModal.overlay });
            
            bookDetailModal.overlay.dispatchEvent(clickEvent);

            expect(hideSpy).toHaveBeenCalled();
        });
    });

    describe('Book Details Loading', () => {
        test('should load and display book details successfully', async () => {
            // Mock successful API response
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve(mockBook)
            });

            // Mock additional API calls
            global.fetch
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(false) }) // library status
                .mockResolvedValueOnce({ ok: false, status: 404 }) // user rating
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve([]) }) // similar books
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve({ content: [] }) }); // reviews

            await bookDetailModal.show(1);

            expect(bookDetailModal.elements.title.textContent).toBe('The Great Gatsby');
            expect(bookDetailModal.elements.author.textContent).toBe('by F. Scott Fitzgerald');
            expect(bookDetailModal.elements.category.textContent).toBe('Fiction');
            expect(bookDetailModal.elements.isbn.textContent).toBe('9780743273565');
        });

        test('should handle API error gracefully', async () => {
            // Mock failed API response
            global.fetch.mockRejectedValueOnce(new Error('Network error'));

            await bookDetailModal.show(1);

            expect(bookDetailModal.elements.error.style.display).toBe('block');
            expect(bookDetailModal.elements.bookDetails.style.display).toBe('none');
        });

        test('should show loading state initially', async () => {
            // Mock delayed API response
            global.fetch.mockImplementationOnce(() => 
                new Promise(resolve => setTimeout(() => resolve({
                    ok: true,
                    json: () => Promise.resolve(mockBook)
                }), 100))
            );

            const showPromise = bookDetailModal.show(1);

            // Check loading state immediately
            expect(bookDetailModal.elements.loading.style.display).toBe('block');
            expect(bookDetailModal.elements.bookDetails.style.display).toBe('none');

            await showPromise;
        });
    });

    describe('Library Actions', () => {
        beforeEach(async () => {
            // Setup modal with book details
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve(mockBook)
            });
            await bookDetailModal.show(1);
        });

        test('should add book to library', async () => {
            bookDetailModal.libraryService.addBookToLibrary.mockResolvedValueOnce();
            
            await bookDetailModal.addToLibrary();

            expect(bookDetailModal.libraryService.addBookToLibrary).toHaveBeenCalledWith(mockBook);
            expect(bookDetailModal.notificationService.showSuccess).toHaveBeenCalledWith('Book added to your library!');
        });

        test('should remove book from library', async () => {
            bookDetailModal.libraryService.removeBookFromLibrary.mockResolvedValueOnce();
            
            await bookDetailModal.removeFromLibrary();

            expect(bookDetailModal.libraryService.removeBookFromLibrary).toHaveBeenCalledWith(1);
            expect(bookDetailModal.notificationService.showSuccess).toHaveBeenCalledWith('Book removed from your library.');
        });

        test('should handle library action errors', async () => {
            bookDetailModal.libraryService.addBookToLibrary.mockRejectedValueOnce(new Error('API Error'));
            
            await bookDetailModal.addToLibrary();

            expect(bookDetailModal.notificationService.showError).toHaveBeenCalledWith('Failed to add book to library. Please try again.');
        });
    });

    describe('Rating System', () => {
        beforeEach(async () => {
            // Setup complete modal with all necessary API calls
            global.fetch
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(mockBook) }) // book details
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(false) }) // library status
                .mockResolvedValueOnce({ ok: false, status: 404 }) // user rating
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve([]) }) // similar books
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve({ content: [] }) }); // reviews
            
            await bookDetailModal.show(1);
            
            // Reset call count for the test
            global.fetch.mockClear();
        });

        test('should submit rating when star is clicked', async () => {
            // Mock successful rating submission
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve({ success: true })
            });

            const starButton = bookDetailModal.elements.userRatingInput.querySelector('[data-rating="4"]');
            await bookDetailModal.handleRating({ currentTarget: starButton });

            expect(global.fetch).toHaveBeenCalledWith(
                '/api/books/1/ratings',
                expect.objectContaining({
                    method: 'POST',
                    body: JSON.stringify({ rating: 4 })
                })
            );
            expect(bookDetailModal.notificationService.showSuccess).toHaveBeenCalledWith('You rated this book 4 stars!');
        });

        test('should display average rating correctly', () => {
            bookDetailModal.populateAverageRating(4.2, 1523);

            expect(bookDetailModal.elements.averageRatingValue.textContent).toBe('4.2');
            expect(bookDetailModal.elements.ratingCount.textContent).toBe('1523');
        });
    });

    describe('Review System', () => {
        beforeEach(async () => {
            // Setup complete modal with all necessary API calls
            global.fetch
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(mockBook) }) // book details
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(false) }) // library status
                .mockResolvedValueOnce({ ok: false, status: 404 }) // user rating
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve([]) }) // similar books
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve({ content: [] }) }); // reviews
            
            await bookDetailModal.show(1);
            
            // Reset call count for the test
            global.fetch.mockClear();
        });

        test('should update character count as user types', () => {
            const testText = 'This is a test review';
            bookDetailModal.elements.reviewText.value = testText;
            
            bookDetailModal.updateCharCount();

            expect(bookDetailModal.elements.reviewCharCount.textContent).toBe(testText.length.toString());
        });

        test('should clear review when clear button is clicked', () => {
            bookDetailModal.elements.reviewText.value = 'Test review';
            
            bookDetailModal.clearReview();

            expect(bookDetailModal.elements.reviewText.value).toBe('');
        });

        test('should submit review on form submission', async () => {
            // Mock successful review submission
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: () => Promise.resolve({ success: true })
            });

            bookDetailModal.elements.reviewText.value = 'This is a great book!';
            
            const formEvent = new Event('submit');
            await bookDetailModal.saveReview(formEvent);

            expect(global.fetch).toHaveBeenCalledWith(
                '/api/books/1/reviews',
                expect.objectContaining({
                    method: 'POST',
                    body: JSON.stringify({ review: 'This is a great book!' })
                })
            );
            expect(bookDetailModal.notificationService.showSuccess).toHaveBeenCalledWith('Review saved successfully!');
        });

        test('should show warning for empty review submission', async () => {
            bookDetailModal.elements.reviewText.value = '';
            
            const formEvent = new Event('submit');
            await bookDetailModal.saveReview(formEvent);

            expect(bookDetailModal.notificationService.showWarning).toHaveBeenCalledWith('Please enter a review before saving.');
            expect(global.fetch).not.toHaveBeenCalled();
        });
    });

    describe('Description Handling', () => {
        test('should show expand button for long descriptions', () => {
            const longDescription = 'A'.repeat(500); // Long description
            
            bookDetailModal.populateDescription(longDescription);

            expect(bookDetailModal.elements.descriptionToggle.style.display).toBe('block');
            expect(bookDetailModal.elements.description.textContent).toContain('...');
        });

        test('should not show expand button for short descriptions', () => {
            const shortDescription = 'A short description';
            
            bookDetailModal.populateDescription(shortDescription);

            expect(bookDetailModal.elements.descriptionToggle.style.display).toBe('none');
            expect(bookDetailModal.elements.description.textContent).toBe(shortDescription);
        });

        test('should toggle description when button is clicked', () => {
            const longDescription = 'A'.repeat(500);
            bookDetailModal.populateDescription(longDescription);
            
            // Initially collapsed
            expect(bookDetailModal.elements.description.dataset.expanded).toBe('false');
            
            // Click to expand
            bookDetailModal.toggleDescription();
            expect(bookDetailModal.elements.description.dataset.expanded).toBe('true');
            expect(bookDetailModal.elements.descriptionToggle.textContent).toBe('Show less');
            
            // Click to collapse
            bookDetailModal.toggleDescription();
            expect(bookDetailModal.elements.description.dataset.expanded).toBe('false');
            expect(bookDetailModal.elements.descriptionToggle.textContent).toBe('Show more');
        });
    });

    describe('Utility Methods', () => {
        test('should escape HTML correctly', () => {
            const unsafe = '<script>alert("xss")</script>';
            const safe = bookDetailModal.escapeHtml(unsafe);
            
            expect(safe).toBe('&lt;script&gt;alert(&quot;xss&quot;)&lt;/script&gt;');
        });

        test('should render stars HTML correctly', () => {
            const starsHtml = bookDetailModal.renderStarsHtml(3.5);
            
            expect(starsHtml).toContain('fas fa-star'); // Full stars
            expect(starsHtml).toContain('fas fa-star-half-alt'); // Half star
            expect(starsHtml).toContain('far fa-star'); // Empty stars
        });

        test('should get auth headers when token exists', () => {
            localStorage.setItem('homeArchive_accessToken', 'test-token');
            
            const headers = bookDetailModal.getAuthHeaders();
            
            expect(headers.Authorization).toBe('Bearer test-token');
        });

        test('should return empty headers when no token exists', () => {
            localStorage.removeItem('homeArchive_accessToken');
            
            const headers = bookDetailModal.getAuthHeaders();
            
            expect(headers.Authorization).toBeUndefined();
        });
    });

    describe('Error Handling', () => {
        test('should handle missing book ID gracefully', async () => {
            const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
            
            await bookDetailModal.show(null);

            expect(consoleSpy).toHaveBeenCalledWith('BookDetailModal: Book ID is required');
            expect(bookDetailModal.isVisible).toBe(false);
            
            consoleSpy.mockRestore();
        });

        test('should show error message on API failure', async () => {
            global.fetch.mockRejectedValueOnce(new Error('Network error'));
            
            await bookDetailModal.show(1);

            expect(bookDetailModal.elements.error.style.display).toBe('block');
            expect(bookDetailModal.elements.errorMessage.textContent).toBe('Failed to load book details. Please try again.');
        });

        test('should retry loading when retry button is clicked', async () => {
            // First call fails
            global.fetch.mockRejectedValueOnce(new Error('Network error'));
            await bookDetailModal.show(1);
            
            // Reset mock to track only retry calls
            global.fetch.mockClear();
            
            // Second call succeeds - based on actual calls made
            global.fetch
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(mockBook) }) // book details
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve(false) }) // library status
                .mockResolvedValueOnce({ ok: false, status: 404 }) // user rating (returns 404)
                .mockResolvedValueOnce({ ok: true, json: () => Promise.resolve([]) }); // similar books
            
            await bookDetailModal.retryLoad();

            expect(global.fetch).toHaveBeenCalledTimes(4); // Actual number of successful calls
        });
    });
});