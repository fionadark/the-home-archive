/**
 * BookDetailModal Component
 * Part of User Story 3 - Book Discovery and Addition
 * 
 * This component handles the book detail modal functionality including:
 * - Display book details fetched from the API
 * - Add/remove books from personal library
 * - Rate and review books
 * - Show similar books and recent reviews
 * - Handle modal show/hide with animations
 */

import { LibraryService } from '../services/libraryService.js';
import { BookService } from '../services/BookService.js';
import { NotificationService } from '../services/NotificationService.js';

export class BookDetailModal {
    constructor() {
        this.modal = null;
        this.overlay = null;
        this.currentBookId = null;
        this.currentBook = null;
        this.isVisible = false;
        
        // Services
        this.libraryService = new LibraryService();
        this.bookService = new BookService();
        this.notificationService = new NotificationService();
        
        this.init();
    }

    /**
     * Initialize the modal component
     */
    init() {
        this.bindElements();
        this.attachEventListeners();
        this.setupKeyboardNavigation();
    }

    /**
     * Bind DOM elements
     */
    bindElements() {
        this.overlay = document.getElementById('book-modal-overlay');
        this.modal = document.getElementById('book-modal');
        
        if (!this.overlay || !this.modal) {
            console.error('BookDetailModal: Required elements not found');
            return;
        }

        // Bind modal content elements
        this.elements = {
            // Loading and error states
            loading: document.getElementById('modal-loading'),
            error: document.getElementById('modal-error'),
            bookDetails: document.getElementById('book-details'),
            
            // Modal controls
            closeButton: document.getElementById('modal-close'),
            cancelButton: document.getElementById('modal-cancel'),
            retryButton: document.getElementById('retry-load'),
            
            // Book information
            title: document.getElementById('book-title'),
            author: document.getElementById('book-author'),
            category: document.getElementById('book-category'),
            publication: document.getElementById('book-publication'),
            isbn: document.getElementById('book-isbn'),
            pages: document.getElementById('book-pages'),
            description: document.getElementById('book-description'),
            descriptionToggle: document.getElementById('description-toggle'),
            
            // Book cover
            bookCover: document.getElementById('book-cover'),
            coverPlaceholder: document.getElementById('cover-placeholder'),
            
            // Library actions
            addToLibrary: document.getElementById('add-to-library'),
            removeFromLibrary: document.getElementById('remove-from-library'),
            
            // Rating system
            averageRatingStars: document.getElementById('average-rating-stars'),
            averageRatingValue: document.getElementById('average-rating-value'),
            ratingCount: document.getElementById('rating-count'),
            userRatingSection: document.getElementById('user-rating-section'),
            userRatingInput: document.getElementById('user-rating-input'),
            
            // Review system
            reviewForm: document.getElementById('review-form'),
            reviewText: document.getElementById('review-text'),
            reviewCharCount: document.getElementById('review-char-count'),
            clearReview: document.getElementById('clear-review'),
            saveReview: document.getElementById('save-review'),
            
            // Similar books and reviews
            similarBooksGrid: document.getElementById('similar-books-grid'),
            reviewsList: document.getElementById('reviews-list'),
            loadMoreReviews: document.getElementById('load-more-reviews'),
            
            // Error message
            errorMessage: document.getElementById('error-message')
        };
    }

    /**
     * Attach event listeners
     */
    attachEventListeners() {
        if (!this.overlay) return;

        // Close modal events
        this.elements.closeButton?.addEventListener('click', () => this.hide());
        this.elements.cancelButton?.addEventListener('click', () => this.hide());
        this.elements.retryButton?.addEventListener('click', () => this.retryLoad());
        
        // Close on overlay click
        this.overlay.addEventListener('click', (e) => {
            if (e.target === this.overlay) {
                this.hide();
            }
        });

        // Library actions
        this.elements.addToLibrary?.addEventListener('click', () => this.addToLibrary());
        this.elements.removeFromLibrary?.addEventListener('click', () => this.removeFromLibrary());

        // Description toggle
        this.elements.descriptionToggle?.addEventListener('click', () => this.toggleDescription());

        // Rating system
        if (this.elements.userRatingInput) {
            const starButtons = this.elements.userRatingInput.querySelectorAll('.star-btn');
            starButtons.forEach(button => {
                button.addEventListener('click', (e) => this.handleRating(e));
                button.addEventListener('mouseenter', (e) => this.previewRating(e));
                button.addEventListener('mouseleave', () => this.resetRatingPreview());
            });
        }

        // Review system
        this.elements.reviewForm?.addEventListener('submit', (e) => this.saveReview(e));
        this.elements.clearReview?.addEventListener('click', () => this.clearReview());
        this.elements.reviewText?.addEventListener('input', () => this.updateCharCount());

        // Load more reviews
        this.elements.loadMoreReviews?.addEventListener('click', () => this.loadMoreReviews());

        // Cover image error handling
        this.elements.bookCover?.addEventListener('error', () => this.showCoverPlaceholder());
        this.elements.bookCover?.addEventListener('load', () => this.hideCoverPlaceholder());
    }

    /**
     * Setup keyboard navigation
     */
    setupKeyboardNavigation() {
        document.addEventListener('keydown', (e) => {
            if (!this.isVisible) return;

            switch (e.key) {
                case 'Escape':
                    this.hide();
                    break;
                case 'Tab':
                    this.handleTabNavigation(e);
                    break;
            }
        });
    }

    /**
     * Handle tab navigation within modal
     */
    handleTabNavigation(e) {
        const focusableElements = this.modal.querySelectorAll(
            'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
        );
        
        const firstElement = focusableElements[0];
        const lastElement = focusableElements[focusableElements.length - 1];

        if (e.shiftKey) {
            if (document.activeElement === firstElement) {
                lastElement.focus();
                e.preventDefault();
            }
        } else {
            if (document.activeElement === lastElement) {
                firstElement.focus();
                e.preventDefault();
            }
        }
    }

    /**
     * Show the modal with book details
     * @param {number} bookId - The ID of the book to display
     */
    async show(bookId) {
        if (!bookId) {
            console.error('BookDetailModal: Book ID is required');
            return;
        }

        this.currentBookId = bookId;
        this.isVisible = true;

        // Show modal with loading state
        this.showLoading();
        this.overlay.classList.add('show');
        
        // Set focus to modal for accessibility
        this.modal.focus();
        
        // Prevent body scroll
        document.body.style.overflow = 'hidden';

        try {
            // Fetch book details
            await this.loadBookDetails(bookId);
        } catch (error) {
            console.error('Error loading book details:', error);
            this.showError('Failed to load book details. Please try again.');
        }
    }

    /**
     * Hide the modal
     */
    hide() {
        this.isVisible = false;
        this.overlay.classList.remove('show');
        
        // Reset body scroll
        document.body.style.overflow = '';
        
        // Clear current book data
        this.currentBookId = null;
        this.currentBook = null;
        
        // Reset modal state after animation
        setTimeout(() => {
            if (!this.isVisible) {
                this.showLoading();
            }
        }, 300);
    }

    /**
     * Show loading state
     */
    showLoading() {
        this.elements.loading.style.display = 'block';
        this.elements.bookDetails.style.display = 'none';
        this.elements.error.style.display = 'none';
    }

    /**
     * Show error state
     */
    showError(message) {
        this.elements.loading.style.display = 'none';
        this.elements.bookDetails.style.display = 'none';
        this.elements.error.style.display = 'block';
        
        if (this.elements.errorMessage) {
            this.elements.errorMessage.textContent = message;
        }
    }

    /**
     * Show book details
     */
    showBookDetails() {
        this.elements.loading.style.display = 'none';
        this.elements.error.style.display = 'none';
        this.elements.bookDetails.style.display = 'block';
    }

    /**
     * Retry loading book details
     */
    async retryLoad() {
        if (this.currentBookId) {
            this.showLoading();
            try {
                await this.loadBookDetails(this.currentBookId);
            } catch (error) {
                this.showError('Failed to load book details. Please try again.');
            }
        }
    }

    /**
     * Load book details from API
     * @param {number} bookId - The book ID to load
     */
    async loadBookDetails(bookId) {
        try {
            // Fetch book details
            const response = await fetch(`/api/books/${bookId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                    ...this.getAuthHeaders()
                },
                credentials: 'same-origin'
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const bookData = await response.json();
            this.currentBook = bookData;
            
            // Update UI with book data
            this.populateBookDetails(bookData);
            
            // Load additional data
            await Promise.all([
                this.checkLibraryStatus(bookId),
                this.loadUserRating(bookId),
                this.loadSimilarBooks(bookId),
                this.loadRecentReviews(bookId)
            ]);
            
            this.showBookDetails();
            
        } catch (error) {
            console.error('Error loading book details:', error);
            throw error;
        }
    }

    /**
     * Populate book details in the UI
     * @param {Object} book - The book data
     */
    populateBookDetails(book) {
        // Basic book information
        this.elements.title.textContent = book.title || 'Unknown Title';
        this.elements.author.textContent = `by ${book.author || 'Unknown Author'}`;
        
        // Metadata
        this.elements.category.textContent = book.category?.name || 'Uncategorized';
        
        // Publication info
        let publicationInfo = '';
        if (book.publisher && book.publicationYear) {
            publicationInfo = `${book.publisher}, ${book.publicationYear}`;
        } else if (book.publicationYear) {
            publicationInfo = book.publicationYear.toString();
        } else if (book.publisher) {
            publicationInfo = book.publisher;
        } else {
            publicationInfo = 'Unknown';
        }
        this.elements.publication.textContent = publicationInfo;
        
        this.elements.isbn.textContent = book.isbn || 'Not available';
        this.elements.pages.textContent = book.pageCount ? `${book.pageCount} pages` : 'Unknown';
        
        // Description
        this.populateDescription(book.description);
        
        // Cover image
        this.populateCoverImage(book.coverImageUrl, book.title);
        
        // Average rating
        this.populateAverageRating(book.averageRating, book.ratingCount);
    }

    /**
     * Populate book description with expand/collapse functionality
     * @param {string} description - The book description
     */
    populateDescription(description) {
        const descriptionElement = this.elements.description;
        const toggleElement = this.elements.descriptionToggle;
        
        if (!description) {
            descriptionElement.textContent = 'No description available.';
            toggleElement.style.display = 'none';
            return;
        }
        
        const maxLength = 300;
        const fullDescription = description;
        const isLong = description.length > maxLength;
        
        if (isLong) {
            const shortDescription = description.substring(0, maxLength) + '...';
            descriptionElement.textContent = shortDescription;
            descriptionElement.dataset.fullDescription = fullDescription;
            descriptionElement.dataset.shortDescription = shortDescription;
            descriptionElement.dataset.expanded = 'false';
            toggleElement.style.display = 'block';
            toggleElement.textContent = 'Show more';
        } else {
            descriptionElement.textContent = fullDescription;
            toggleElement.style.display = 'none';
        }
    }

    /**
     * Toggle description expand/collapse
     */
    toggleDescription() {
        const descriptionElement = this.elements.description;
        const toggleElement = this.elements.descriptionToggle;
        const isExpanded = descriptionElement.dataset.expanded === 'true';
        
        if (isExpanded) {
            descriptionElement.textContent = descriptionElement.dataset.shortDescription;
            toggleElement.textContent = 'Show more';
            descriptionElement.dataset.expanded = 'false';
        } else {
            descriptionElement.textContent = descriptionElement.dataset.fullDescription;
            toggleElement.textContent = 'Show less';
            descriptionElement.dataset.expanded = 'true';
        }
    }

    /**
     * Populate cover image
     * @param {string} coverUrl - The cover image URL
     * @param {string} title - The book title for alt text
     */
    populateCoverImage(coverUrl, title) {
        const coverElement = this.elements.bookCover;
        const placeholderElement = this.elements.coverPlaceholder;
        
        if (coverUrl) {
            coverElement.src = coverUrl;
            coverElement.alt = `Cover of ${title}`;
            coverElement.style.display = 'block';
            placeholderElement.style.display = 'none';
        } else {
            this.showCoverPlaceholder();
        }
    }

    /**
     * Show cover placeholder
     */
    showCoverPlaceholder() {
        this.elements.bookCover.style.display = 'none';
        this.elements.coverPlaceholder.style.display = 'flex';
    }

    /**
     * Hide cover placeholder
     */
    hideCoverPlaceholder() {
        this.elements.bookCover.style.display = 'block';
        this.elements.coverPlaceholder.style.display = 'none';
    }

    /**
     * Populate average rating display
     * @param {number} averageRating - The average rating
     * @param {number} ratingCount - The total number of ratings
     */
    populateAverageRating(averageRating, ratingCount) {
        const starsElement = this.elements.averageRatingStars;
        const valueElement = this.elements.averageRatingValue;
        const countElement = this.elements.ratingCount;
        
        const rating = averageRating || 0;
        const count = ratingCount || 0;
        
        // Update rating display
        this.renderStars(starsElement, rating);
        valueElement.textContent = rating.toFixed(1);
        countElement.textContent = count;
    }

    /**
     * Render star rating display
     * @param {HTMLElement} container - The container element
     * @param {number} rating - The rating value (0-5)
     * @param {boolean} interactive - Whether stars are interactive
     */
    renderStars(container, rating, interactive = false) {
        container.innerHTML = '';
        
        for (let i = 1; i <= 5; i++) {
            const star = document.createElement('i');
            
            if (i <= Math.floor(rating)) {
                star.className = 'fas fa-star';
            } else if (i - 0.5 <= rating) {
                star.className = 'fas fa-star-half-alt';
            } else {
                star.className = 'far fa-star';
            }
            
            if (interactive) {
                star.className += ' star-interactive';
                star.setAttribute('data-rating', i);
            }
            
            container.appendChild(star);
        }
    }

    /**
     * Check if book is in user's library
     * @param {number} bookId - The book ID
     */
    async checkLibraryStatus(bookId) {
        try {
            const isInLibrary = await this.libraryService.isBookInLibrary(bookId);
            this.updateLibraryButtons(isInLibrary);
        } catch (error) {
            console.error('Error checking library status:', error);
            // Don't show error to user for this non-critical operation
        }
    }

    /**
     * Update library action buttons
     * @param {boolean} isInLibrary - Whether book is in library
     */
    updateLibraryButtons(isInLibrary) {
        if (isInLibrary) {
            this.elements.addToLibrary.style.display = 'none';
            this.elements.removeFromLibrary.style.display = 'block';
        } else {
            this.elements.addToLibrary.style.display = 'block';
            this.elements.removeFromLibrary.style.display = 'none';
        }
    }

    /**
     * Add book to user's library
     */
    async addToLibrary() {
        if (!this.currentBook) return;
        
        try {
            await this.libraryService.addBookToLibrary(this.currentBook);
            this.updateLibraryButtons(true);
            this.notificationService.showSuccess('Book added to your library!');
        } catch (error) {
            console.error('Error adding book to library:', error);
            this.notificationService.showError('Failed to add book to library. Please try again.');
        }
    }

    /**
     * Remove book from user's library
     */
    async removeFromLibrary() {
        if (!this.currentBookId) return;
        
        try {
            await this.libraryService.removeBookFromLibrary(this.currentBookId);
            this.updateLibraryButtons(false);
            this.notificationService.showSuccess('Book removed from your library.');
        } catch (error) {
            console.error('Error removing book from library:', error);
            this.notificationService.showError('Failed to remove book from library. Please try again.');
        }
    }

    /**
     * Load user's rating for this book
     * @param {number} bookId - The book ID
     */
    async loadUserRating(bookId) {
        try {
            const userRating = await this.getUserRating(bookId);
            if (userRating) {
                this.displayUserRating(userRating.rating);
                if (userRating.review) {
                    this.elements.reviewText.value = userRating.review;
                    this.updateCharCount();
                }
            }
        } catch (error) {
            console.error('Error loading user rating:', error);
            // Don't show error to user for this non-critical operation
        }
    }

    /**
     * Get user's rating from API
     * @param {number} bookId - The book ID
     * @returns {Promise<Object|null>} The user's rating data
     */
    async getUserRating(bookId) {
        const response = await fetch(`/api/books/${bookId}/user-rating`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                ...this.getAuthHeaders()
            },
            credentials: 'same-origin'
        });

        if (response.status === 404) {
            return null; // User hasn't rated this book
        }

        if (!response.ok) {
            throw new Error(`Failed to get user rating: ${response.statusText}`);
        }

        return response.json();
    }

    /**
     * Display user's rating in the UI
     * @param {number} rating - The user's rating (1-5)
     */
    displayUserRating(rating) {
        const starButtons = this.elements.userRatingInput.querySelectorAll('.star-btn');
        starButtons.forEach((button, index) => {
            const starIcon = button.querySelector('i');
            if (index < rating) {
                starIcon.className = 'fas fa-star';
                button.classList.add('selected');
            } else {
                starIcon.className = 'far fa-star';
                button.classList.remove('selected');
            }
        });
    }

    /**
     * Handle rating click
     * @param {Event} e - The click event
     */
    async handleRating(e) {
        const rating = parseInt(e.currentTarget.dataset.rating);
        
        try {
            await this.submitRating(rating);
            this.displayUserRating(rating);
            this.notificationService.showSuccess(`You rated this book ${rating} star${rating !== 1 ? 's' : ''}!`);
            
            // Refresh average rating
            if (this.currentBookId) {
                await this.refreshAverageRating(this.currentBookId);
            }
        } catch (error) {
            console.error('Error submitting rating:', error);
            this.notificationService.showError('Failed to submit rating. Please try again.');
        }
    }

    /**
     * Preview rating on hover
     * @param {Event} e - The mouseenter event
     */
    previewRating(e) {
        const rating = parseInt(e.currentTarget.dataset.rating);
        const starButtons = this.elements.userRatingInput.querySelectorAll('.star-btn');
        
        starButtons.forEach((button, index) => {
            const starIcon = button.querySelector('i');
            if (index < rating) {
                starIcon.className = 'fas fa-star';
            } else {
                starIcon.className = 'far fa-star';
            }
        });
    }

    /**
     * Reset rating preview
     */
    resetRatingPreview() {
        // Get current user rating and restore it
        const selectedButtons = this.elements.userRatingInput.querySelectorAll('.star-btn.selected');
        const currentRating = selectedButtons.length;
        
        const starButtons = this.elements.userRatingInput.querySelectorAll('.star-btn');
        starButtons.forEach((button, index) => {
            const starIcon = button.querySelector('i');
            if (index < currentRating) {
                starIcon.className = 'fas fa-star';
            } else {
                starIcon.className = 'far fa-star';
            }
        });
    }

    /**
     * Submit rating to API
     * @param {number} rating - The rating value (1-5)
     */
    async submitRating(rating) {
        const response = await fetch(`/api/books/${this.currentBookId}/ratings`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...this.getAuthHeaders()
            },
            credentials: 'same-origin',
            body: JSON.stringify({ rating })
        });

        if (!response.ok) {
            throw new Error(`Failed to submit rating: ${response.statusText}`);
        }

        return response.json();
    }

    /**
     * Refresh average rating display
     * @param {number} bookId - The book ID
     */
    async refreshAverageRating(bookId) {
        try {
            const response = await fetch(`/api/books/${bookId}`, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                },
                credentials: 'same-origin'
            });

            if (response.ok) {
                const bookData = await response.json();
                this.populateAverageRating(bookData.averageRating, bookData.ratingCount);
            }
        } catch (error) {
            console.error('Error refreshing average rating:', error);
        }
    }

    /**
     * Save user review
     * @param {Event} e - The form submit event
     */
    async saveReview(e) {
        e.preventDefault();
        
        const reviewText = this.elements.reviewText.value.trim();
        if (!reviewText) {
            this.notificationService.showWarning('Please enter a review before saving.');
            return;
        }

        try {
            await this.submitReview(reviewText);
            this.notificationService.showSuccess('Review saved successfully!');
        } catch (error) {
            console.error('Error saving review:', error);
            this.notificationService.showError('Failed to save review. Please try again.');
        }
    }

    /**
     * Submit review to API
     * @param {string} reviewText - The review text
     */
    async submitReview(reviewText) {
        const response = await fetch(`/api/books/${this.currentBookId}/reviews`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...this.getAuthHeaders()
            },
            credentials: 'same-origin',
            body: JSON.stringify({ review: reviewText })
        });

        if (!response.ok) {
            throw new Error(`Failed to submit review: ${response.statusText}`);
        }

        return response.json();
    }

    /**
     * Clear review text
     */
    clearReview() {
        this.elements.reviewText.value = '';
        this.updateCharCount();
    }

    /**
     * Update character count display
     */
    updateCharCount() {
        const currentLength = this.elements.reviewText.value.length;
        this.elements.reviewCharCount.textContent = currentLength;
        
        // Update styling based on character count
        if (currentLength > 1800) {
            this.elements.reviewCharCount.style.color = 'var(--warning-color)';
        } else if (currentLength > 1900) {
            this.elements.reviewCharCount.style.color = 'var(--error-color)';
        } else {
            this.elements.reviewCharCount.style.color = '';
        }
    }

    /**
     * Load similar books
     * @param {number} bookId - The book ID
     */
    async loadSimilarBooks(bookId) {
        try {
            const similarBooks = await this.getSimilarBooks(bookId);
            this.displaySimilarBooks(similarBooks);
        } catch (error) {
            console.error('Error loading similar books:', error);
            // Hide similar books section on error
            document.getElementById('similar-books-section').style.display = 'none';
        }
    }

    /**
     * Get similar books from API
     * @param {number} bookId - The book ID
     * @returns {Promise<Array>} Array of similar books
     */
    async getSimilarBooks(bookId) {
        const response = await fetch(`/api/books/${bookId}/similar?limit=6`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            credentials: 'same-origin'
        });

        if (!response.ok) {
            throw new Error(`Failed to get similar books: ${response.statusText}`);
        }

        return response.json();
    }

    /**
     * Display similar books
     * @param {Array} books - Array of similar books
     */
    displaySimilarBooks(books) {
        const container = this.elements.similarBooksGrid;
        
        if (!books || books.length === 0) {
            document.getElementById('similar-books-section').style.display = 'none';
            return;
        }

        container.innerHTML = '';
        
        books.forEach(book => {
            const bookCard = this.createSimilarBookCard(book);
            container.appendChild(bookCard);
        });
    }

    /**
     * Create similar book card element
     * @param {Object} book - The book data
     * @returns {HTMLElement} The book card element
     */
    createSimilarBookCard(book) {
        const card = document.createElement('div');
        card.className = 'similar-book-card';
        card.innerHTML = `
            <div class="similar-book-cover">
                ${book.coverImageUrl ? 
                    `<img src="${book.coverImageUrl}" alt="Cover of ${book.title}" loading="lazy">` :
                    `<div class="cover-placeholder"><i class="fas fa-book"></i></div>`
                }
            </div>
            <div class="similar-book-info">
                <h4 class="similar-book-title">${this.escapeHtml(book.title)}</h4>
                <p class="similar-book-author">${this.escapeHtml(book.author)}</p>
                <div class="similar-book-rating">
                    <span class="rating-stars">${this.renderStarsHtml(book.averageRating || 0)}</span>
                    <span class="rating-value">(${(book.averageRating || 0).toFixed(1)})</span>
                </div>
            </div>
        `;
        
        // Add click handler to show book details
        card.addEventListener('click', () => {
            this.show(book.id);
        });
        
        return card;
    }

    /**
     * Load recent reviews
     * @param {number} bookId - The book ID
     */
    async loadRecentReviews(bookId) {
        try {
            const reviews = await this.getRecentReviews(bookId);
            this.displayRecentReviews(reviews);
        } catch (error) {
            console.error('Error loading recent reviews:', error);
            // Hide reviews section on error
            document.getElementById('recent-reviews-section').style.display = 'none';
        }
    }

    /**
     * Get recent reviews from API
     * @param {number} bookId - The book ID
     * @param {number} page - The page number (default: 0)
     * @returns {Promise<Object>} Reviews response with pagination
     */
    async getRecentReviews(bookId, page = 0) {
        const response = await fetch(`/api/books/${bookId}/reviews?page=${page}&size=5`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Accept': 'application/json'
            },
            credentials: 'same-origin'
        });

        if (!response.ok) {
            throw new Error(`Failed to get reviews: ${response.statusText}`);
        }

        return response.json();
    }

    /**
     * Display recent reviews
     * @param {Object} reviewsResponse - The reviews response with pagination
     */
    displayRecentReviews(reviewsResponse) {
        const container = this.elements.reviewsList;
        const loadMoreButton = this.elements.loadMoreReviews;
        
        if (!reviewsResponse.content || reviewsResponse.content.length === 0) {
            document.getElementById('recent-reviews-section').style.display = 'none';
            return;
        }

        // Clear container if this is the first page
        if (reviewsResponse.first) {
            container.innerHTML = '';
        }
        
        reviewsResponse.content.forEach(review => {
            const reviewElement = this.createReviewElement(review);
            container.appendChild(reviewElement);
        });
        
        // Show/hide load more button
        if (reviewsResponse.last) {
            loadMoreButton.style.display = 'none';
        } else {
            loadMoreButton.style.display = 'block';
            loadMoreButton.dataset.nextPage = (reviewsResponse.number + 1).toString();
        }
    }

    /**
     * Create review element
     * @param {Object} review - The review data
     * @returns {HTMLElement} The review element
     */
    createReviewElement(review) {
        const reviewElement = document.createElement('div');
        reviewElement.className = 'review-item';
        
        const reviewDate = new Date(review.createdAt).toLocaleDateString();
        
        reviewElement.innerHTML = `
            <div class="review-header">
                <div class="review-user">
                    <span class="user-name">${this.escapeHtml(review.user.firstName)} ${this.escapeHtml(review.user.lastName)}</span>
                    <span class="review-rating">${this.renderStarsHtml(review.rating)}</span>
                </div>
                <span class="review-date">${reviewDate}</span>
            </div>
            <div class="review-content">
                <p>${this.escapeHtml(review.review)}</p>
            </div>
        `;
        
        return reviewElement;
    }

    /**
     * Load more reviews
     */
    async loadMoreReviews() {
        if (!this.currentBookId) return;
        
        const loadMoreButton = this.elements.loadMoreReviews;
        const nextPage = parseInt(loadMoreButton.dataset.nextPage || '1');
        
        try {
            loadMoreButton.disabled = true;
            loadMoreButton.textContent = 'Loading...';
            
            const reviews = await this.getRecentReviews(this.currentBookId, nextPage);
            this.displayRecentReviews(reviews);
            
        } catch (error) {
            console.error('Error loading more reviews:', error);
            this.notificationService.showError('Failed to load more reviews.');
        } finally {
            loadMoreButton.disabled = false;
            loadMoreButton.textContent = 'Load More Reviews';
        }
    }

    /**
     * Get authentication headers
     * @returns {Object} The auth headers
     */
    getAuthHeaders() {
        const token = localStorage.getItem('homeArchive_accessToken');
        return token ? { 'Authorization': `Bearer ${token}` } : {};
    }

    /**
     * Render stars as HTML string
     * @param {number} rating - The rating value (0-5)
     * @returns {string} HTML string with star icons
     */
    renderStarsHtml(rating) {
        let starsHtml = '';
        for (let i = 1; i <= 5; i++) {
            if (i <= Math.floor(rating)) {
                starsHtml += '<i class="fas fa-star"></i>';
            } else if (i - 0.5 <= rating) {
                starsHtml += '<i class="fas fa-star-half-alt"></i>';
            } else {
                starsHtml += '<i class="far fa-star"></i>';
            }
        }
        return starsHtml;
    }

    /**
     * Escape HTML to prevent XSS
     * @param {string} unsafe - The unsafe string
     * @returns {string} The escaped string
     */
    escapeHtml(unsafe) {
        if (typeof unsafe !== 'string') return '';
        
        return unsafe
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }
}

// Export for use in other modules
export default BookDetailModal;