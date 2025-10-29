/**
 * BookAddition - Component for adding books to the library
 * Part of Phase 5 User Story 3 implementation
 */

import { BookService } from '../services/BookService.js';
import { NotificationService } from '../services/NotificationService.js';

export default class BookAddition {
    constructor() {
        this.bookService = new BookService();
        this.notificationService = new NotificationService();
        
        this.isbnValidationResults = {};
        this.currentStep = 'search'; // search, manual, confirmation
        this.selectedBook = null;
        this.manualBookData = {};
        this.categories = [];
    }

    /**
     * Render the book addition component
     */
    render(container) {
        container.classList.add('dark-academia');
        
        container.innerHTML = `
            <div class="book-addition-container">
                <div class="step-progress">
                    <div class="step active">Search</div>
                    <div class="step">Manual Entry</div>
                    <div class="step">Confirmation</div>
                </div>
                
                <!-- ISBN Search Section -->
                <div id="isbn-search-section" class="section active">
                    <h3>Add Book by ISBN</h3>
                    <div class="elegant-form">
                        <div class="form-group">
                            <label for="isbn-input">ISBN</label>
                            <input 
                                type="text" 
                                id="isbn-input" 
                                placeholder="Enter ISBN-10 or ISBN-13..."
                                aria-describedby="isbn-help"
                            />
                            <small id="isbn-help">Enter the 10 or 13 digit ISBN number</small>
                        </div>
                        
                        <div class="form-actions">
                            <button id="validate-isbn-btn" class="btn btn-primary">
                                Validate ISBN
                            </button>
                            <button id="switch-to-manual-btn" class="btn btn-secondary">
                                Manual Entry Instead
                            </button>
                        </div>
                        
                        <div id="isbn-validation-results"></div>
                    </div>
                </div>

                <!-- Manual Entry Section -->
                <div id="manual-entry-section" class="section">
                    <h3>Add Book Manually</h3>
                    <div class="elegant-form">
                        <div class="form-row">
                            <div class="form-group">
                                <label for="manual-title">Title *</label>
                                <input type="text" id="manual-title" required />
                            </div>
                            <div class="form-group">
                                <label for="manual-author">Author *</label>
                                <input type="text" id="manual-author" required />
                            </div>
                        </div>
                        
                        <div class="form-row">
                            <div class="form-group">
                                <label for="manual-isbn">ISBN</label>
                                <input type="text" id="manual-isbn" />
                            </div>
                            <div class="form-group">
                                <label for="manual-year">Publication Year</label>
                                <input type="number" id="manual-year" min="1000" max="${new Date().getFullYear()}" />
                            </div>
                        </div>
                        
                        <div class="form-row">
                            <div class="form-group">
                                <label for="manual-publisher">Publisher</label>
                                <input type="text" id="manual-publisher" />
                            </div>
                            <div class="form-group">
                                <label for="manual-page-count">Page Count</label>
                                <input type="number" id="manual-page-count" min="1" />
                            </div>
                        </div>
                        
                        <div class="form-group">
                            <label for="manual-category">Category *</label>
                            <select id="manual-category" required>
                                <option value="">Select a category...</option>
                            </select>
                        </div>
                        
                        <div class="form-group">
                            <label for="manual-description">Description</label>
                            <textarea id="manual-description" rows="3"></textarea>
                        </div>
                        
                        <div class="form-group">
                            <label for="manual-cover-url">Cover Image URL</label>
                            <input type="url" id="manual-cover-url" />
                            <div class="cover-preview"></div>
                        </div>
                        
                        <div class="validation-errors"></div>
                        
                        <div class="form-actions">
                            <button id="back-to-isbn-btn" class="btn btn-secondary">
                                Back to ISBN Search
                            </button>
                            <button id="submit-manual-book-btn" class="btn btn-primary">
                                Continue
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Confirmation Section -->
                <div id="book-confirmation-section" class="section">
                    <h3>Confirm Book Details</h3>
                    <div class="book-confirmation-preview">
                        <div class="confirmation-cover"></div>
                        <div class="confirmation-details">
                            <h4 class="confirmation-title"></h4>
                            <p class="confirmation-author"></p>
                            <p class="confirmation-isbn"></p>
                            <p class="confirmation-publisher"></p>
                            <p class="confirmation-year"></p>
                            <p class="confirmation-description"></p>
                        </div>
                    </div>
                    
                    <div class="form-actions">
                        <button id="edit-book-details-btn" class="btn btn-secondary">
                            Edit Details
                        </button>
                        <button id="confirm-add-book-btn" class="btn btn-primary">
                            Add to Library
                        </button>
                    </div>
                </div>
            </div>
            
            <div class="sr-only" aria-live="polite" id="step-announcements"></div>
        `;
        
        this.bindEvents();
        this.loadCategories();
    }

    /**
     * Bind event listeners
     */
    bindEvents() {
        // ISBN search events
        const isbnInput = document.getElementById('isbn-input');
        const validateBtn = document.getElementById('validate-isbn-btn');
        const switchToManualBtn = document.getElementById('switch-to-manual-btn');

        validateBtn.addEventListener('click', () => this.validateIsbn());
        isbnInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.validateIsbn();
            }
        });
        isbnInput.addEventListener('input', () => this.clearValidationResults());
        switchToManualBtn.addEventListener('click', () => this.switchToManualEntry());

        // Manual entry events
        const backToIsbnBtn = document.getElementById('back-to-isbn-btn');
        const submitManualBtn = document.getElementById('submit-manual-book-btn');
        const coverUrlInput = document.getElementById('manual-cover-url');

        backToIsbnBtn.addEventListener('click', () => this.switchToIsbnSearch());
        submitManualBtn.addEventListener('click', () => this.submitManualBook());
        coverUrlInput.addEventListener('input', (e) => this.previewCoverImage(e.target.value));

        // Confirmation events
        const editDetailsBtn = document.getElementById('edit-book-details-btn');
        const confirmAddBtn = document.getElementById('confirm-add-book-btn');

        editDetailsBtn.addEventListener('click', () => this.editBookDetails());
        confirmAddBtn.addEventListener('click', () => this.confirmAddBook());
    }

    /**
     * Validate ISBN
     */
    async validateIsbn() {
        const isbn = document.getElementById('isbn-input').value.trim();
        
        if (!isbn) {
            this.notificationService.showError('Please enter an ISBN');
            return;
        }

        try {
            this.showIsbnLoading();
            const results = await this.bookService.validateBookByIsbn(isbn);
            
            if (!results) {
                this.notificationService.showError('Unable to validate ISBN. Please try manual entry.');
                return;
            }
            
            this.isbnValidationResults = results;
            this.displayValidationResults();
            
        } catch (error) {
            // Enhanced error handling
            if (error.name === 'NetworkError' || error.message.includes('Network')) {
                this.notificationService.showError('Network error. Please check your connection and try again.');
            } else {
                this.notificationService.showError('Failed to validate ISBN. Please try again.');
            }
        } finally {
            this.hideIsbnLoading();
        }
    }

    /**
     * Display ISBN validation results
     */
    displayValidationResults() {
        const container = document.getElementById('isbn-validation-results');
        const results = this.isbnValidationResults;

        if (!results.valid) {
            container.innerHTML = `
                <div class="isbn-validation-error">
                    <p>❌ ${results.errorMessage || 'Invalid ISBN'}</p>
                </div>
            `;
            return;
        }

        if (results.existsInDatabase) {
            container.innerHTML = `
                <div class="book-exists-warning">
                    <h4>Book Found in Database</h4>
                    <p>This book already exists in our database.</p>
                    <div class="book-preview">
                        ${this.createBookPreview(results)}
                    </div>
                    <button id="add-existing-btn" class="btn btn-primary">
                        Add to My Library
                    </button>
                </div>
            `;
            
            document.getElementById('add-existing-btn').addEventListener('click', () => {
                this.addExistingBookToLibrary(results.bookId);
            });
        } else {
            container.innerHTML = `
                <div class="isbn-validation-success">
                    <h4>✅ Valid ISBN - Book Information Found</h4>
                    <div class="book-preview">
                        ${this.createBookPreview(results)}
                    </div>
                    <div class="validation-actions">
                        <button id="use-isbn-data-btn" class="btn btn-primary">
                            Use This Data
                        </button>
                        <button id="edit-data-btn" class="btn btn-secondary">
                            Edit Before Adding
                        </button>
                    </div>
                </div>
            `;
            
            document.getElementById('use-isbn-data-btn').addEventListener('click', () => {
                this.useIsbnData();
            });
            
            document.getElementById('edit-data-btn').addEventListener('click', () => {
                this.populateManualFormFromIsbn();
            });
        }
    }

    /**
     * Create book preview HTML
     */
    createBookPreview(bookData) {
        return `
            <div class="book-info">
                <div class="book-cover">
                    ${bookData.coverImageUrl ? 
                        `<img src="${bookData.coverImageUrl}" alt="Cover of ${bookData.title}" />` :
                        `<div class="cover-placeholder">No Cover Available</div>`
                    }
                </div>
                <div class="book-details">
                    <h5 class="title">${bookData.title || 'Title not found'}</h5>
                    <p class="author">by ${bookData.author || 'Unknown Author'}</p>
                    <p class="isbn">ISBN: ${bookData.isbn}</p>
                    ${bookData.publisher ? `<p class="publisher">${bookData.publisher}</p>` : ''}
                    ${bookData.publicationYear ? `<p class="year">${bookData.publicationYear}</p>` : ''}
                    ${bookData.pageCount ? `<p class="pages">${bookData.pageCount} pages</p>` : ''}
                    ${bookData.description ? `<p class="description">${bookData.description}</p>` : ''}
                    ${bookData.enrichedFromExternalSource ? 
                        `<small class="external-source">Data from ${bookData.externalSource}</small>` : ''
                    }
                </div>
            </div>
        `;
    }

    /**
     * Clear validation results
     */
    clearValidationResults() {
        document.getElementById('isbn-validation-results').innerHTML = '';
        this.isbnValidationResults = {};
    }

    /**
     * Switch to manual entry step
     */
    switchToManualEntry() {
        this.currentStep = 'manual';
        this.updateStepDisplay();
        this.announceStepChange('Manual entry step active');
    }

    /**
     * Switch back to ISBN search
     */
    switchToIsbnSearch() {
        this.currentStep = 'search';
        this.updateStepDisplay();
        this.announceStepChange('ISBN search step active');
    }

    /**
     * Populate manual form from ISBN data
     */
    populateManualFormFromIsbn() {
        const data = this.isbnValidationResults;
        
        document.getElementById('manual-title').value = data.title || '';
        document.getElementById('manual-author').value = data.author || '';
        document.getElementById('manual-isbn').value = data.isbn || '';
        document.getElementById('manual-year').value = data.publicationYear || '';
        document.getElementById('manual-publisher').value = data.publisher || '';
        document.getElementById('manual-page-count').value = data.pageCount || '';
        document.getElementById('manual-description').value = data.description || '';
        document.getElementById('manual-cover-url').value = data.coverImageUrl || '';
        
        if (data.coverImageUrl) {
            this.previewCoverImage(data.coverImageUrl);
        }
        
        this.switchToManualEntry();
    }

    /**
     * Use ISBN data directly
     */
    useIsbnData() {
        this.selectedBook = {
            title: this.isbnValidationResults.title,
            author: this.isbnValidationResults.author,
            isbn: this.isbnValidationResults.isbn,
            publicationYear: this.isbnValidationResults.publicationYear,
            publisher: this.isbnValidationResults.publisher,
            pageCount: this.isbnValidationResults.pageCount,
            description: this.isbnValidationResults.description,
            coverImageUrl: this.isbnValidationResults.coverImageUrl,
            categoryId: 1 // Default category, user can change in confirmation
        };
        
        this.currentStep = 'confirmation';
        this.updateStepDisplay();
        this.displayConfirmationStep();
    }

    /**
     * Submit manual book form
     */
    submitManualBook() {
        const errors = this.validateManualForm();
        
        if (errors.length > 0) {
            this.displayValidationErrors(errors);
            return;
        }

        this.selectedBook = this.collectManualFormData();
        this.currentStep = 'confirmation';
        this.updateStepDisplay();
        this.displayConfirmationStep();
    }

    /**
     * Validate manual form
     */
    validateManualForm() {
        const errors = [];
        
        const title = document.getElementById('manual-title').value.trim();
        const author = document.getElementById('manual-author').value.trim();
        const isbn = document.getElementById('manual-isbn').value.trim();
        const year = document.getElementById('manual-year').value;
        const pageCount = document.getElementById('manual-page-count').value;
        const category = document.getElementById('manual-category').value;

        if (!title) errors.push('Title is required');
        if (!author) errors.push('Author is required');
        if (!category) errors.push('Category is required');
        
        if (isbn && !this.isValidIsbn(isbn)) {
            errors.push('Invalid ISBN format');
        }
        
        if (year && (year < 1000 || year > new Date().getFullYear())) {
            errors.push('Publication year cannot be in the future');
        }
        
        if (pageCount && (pageCount < 1 || pageCount > 10000)) {
            errors.push('Page count must be a positive number');
        }

        return errors;
    }

    /**
     * Validate ISBN format
     */
    isValidIsbn(isbn) {
        // Basic ISBN validation - remove hyphens and check length
        const cleanIsbn = isbn.replace(/[-\s]/g, '');
        return cleanIsbn.length === 10 || cleanIsbn.length === 13;
    }

    /**
     * Display validation errors
     */
    displayValidationErrors(errors) {
        const container = document.querySelector('.validation-errors');
        container.innerHTML = `
            <div class="error-list">
                <h5>Please fix the following errors:</h5>
                <ul>
                    ${errors.map(error => `<li>${error}</li>`).join('')}
                </ul>
            </div>
        `;
        
        // Set ARIA attributes
        const inputs = document.querySelectorAll('#manual-entry-section input, #manual-entry-section select');
        inputs.forEach(input => {
            input.setAttribute('aria-invalid', 'true');
            input.setAttribute('aria-describedby', 'validation-errors');
        });
    }

    /**
     * Collect manual form data
     */
    collectManualFormData() {
        return {
            title: document.getElementById('manual-title').value.trim(),
            author: document.getElementById('manual-author').value.trim(),
            isbn: document.getElementById('manual-isbn').value.trim(),
            publicationYear: parseInt(document.getElementById('manual-year').value) || null,
            publisher: document.getElementById('manual-publisher').value.trim(),
            pageCount: parseInt(document.getElementById('manual-page-count').value) || null,
            categoryId: parseInt(document.getElementById('manual-category').value),
            description: document.getElementById('manual-description').value.trim(),
            coverImageUrl: document.getElementById('manual-cover-url').value.trim()
        };
    }

    /**
     * Display confirmation step
     */
    displayConfirmationStep() {
        const book = this.selectedBook;
        
        document.querySelector('.confirmation-title').textContent = book.title;
        document.querySelector('.confirmation-author').textContent = `by ${book.author}`;
        document.querySelector('.confirmation-isbn').textContent = book.isbn ? `ISBN: ${book.isbn}` : '';
        document.querySelector('.confirmation-publisher').textContent = book.publisher || '';
        document.querySelector('.confirmation-year').textContent = book.publicationYear || '';
        document.querySelector('.confirmation-description').textContent = book.description || '';
        
        const coverContainer = document.querySelector('.confirmation-cover');
        if (book.coverImageUrl) {
            coverContainer.innerHTML = `<img src="${book.coverImageUrl}" alt="Cover of ${book.title}" />`;
        } else {
            coverContainer.innerHTML = '<div class="cover-placeholder">No Cover</div>';
        }
        
        this.announceStepChange('Confirmation step active. Review book details before adding to library.');
    }

    /**
     * Edit book details
     */
    editBookDetails() {
        // Populate manual form with current selection
        const book = this.selectedBook;
        
        document.getElementById('manual-title').value = book.title || '';
        document.getElementById('manual-author').value = book.author || '';
        document.getElementById('manual-isbn').value = book.isbn || '';
        document.getElementById('manual-year').value = book.publicationYear || '';
        document.getElementById('manual-publisher').value = book.publisher || '';
        document.getElementById('manual-page-count').value = book.pageCount || '';
        document.getElementById('manual-category').value = book.categoryId || '';
        document.getElementById('manual-description').value = book.description || '';
        document.getElementById('manual-cover-url').value = book.coverImageUrl || '';
        
        this.switchToManualEntry();
    }

    /**
     * Confirm and add book
     */
    async confirmAddBook() {
        try {
            const createdBook = await this.bookService.createBook(this.selectedBook);
            this.notificationService.showSuccess('Book added to your library successfully!');
            this.resetForm();
            
        } catch (error) {
            // Enhanced error handling
            if (error.status === 503) {
                this.notificationService.showError('Service is temporarily unavailable. Please try again later.');
            } else if (error.name === 'NetworkError' || error.message.includes('Network')) {
                this.notificationService.showError('Network error. Please check your connection and try again.');
            } else {
                this.notificationService.showError(error.message);
            }
        }
    }

    /**
     * Add existing book to library
     */
    async addExistingBookToLibrary(bookId) {
        try {
            await this.bookService.addBookToLibrary(bookId);
            this.notificationService.showSuccess('Book added to your library!');
            this.resetForm();
            
        } catch (error) {
            this.notificationService.showError(error.message);
        }
    }

    /**
     * Load categories
     */
    async loadCategories() {
        try {
            this.categories = await this.bookService.getCategories();
            this.populateCategorySelect();
            
        } catch (error) {
            this.notificationService.showError('Failed to load categories');
        }
    }

    /**
     * Populate category select
     */
    populateCategorySelect() {
        const select = document.getElementById('manual-category');
        
        // Clear existing options except the first one
        while (select.children.length > 1) {
            select.removeChild(select.lastChild);
        }
        
        this.categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.id;
            option.textContent = category.name;
            select.appendChild(option);
        });
    }

    /**
     * Preview cover image
     */
    previewCoverImage(url) {
        const preview = document.querySelector('.cover-preview');
        
        if (!url) {
            preview.innerHTML = '';
            return;
        }
        
        const isValidUrl = this.isValidUrl(url);
        
        if (isValidUrl) {
            preview.innerHTML = `<img src="${url}" alt="Cover preview" style="max-width: 150px; max-height: 200px;" />`;
        } else {
            preview.innerHTML = '<div class="cover-placeholder">Invalid URL</div>';
        }
    }

    /**
     * Validate URL format
     */
    isValidUrl(string) {
        try {
            new URL(string);
            return true;
        } catch (_) {
            return false;
        }
    }

    /**
     * Update step display
     */
    updateStepDisplay() {
        // Update section visibility
        document.querySelectorAll('.section').forEach(section => {
            section.classList.remove('active');
        });
        
        document.querySelectorAll('.step').forEach(step => {
            step.classList.remove('active', 'completed');
        });
        
        const steps = document.querySelectorAll('.step');
        
        switch (this.currentStep) {
            case 'search':
                document.getElementById('isbn-search-section').classList.add('active');
                steps[0].classList.add('active');
                break;
            case 'manual':
                document.getElementById('manual-entry-section').classList.add('active');
                steps[0].classList.add('completed');
                steps[1].classList.add('active');
                break;
            case 'confirmation':
                document.getElementById('book-confirmation-section').classList.add('active');
                steps[0].classList.add('completed');
                steps[1].classList.add('completed');
                steps[2].classList.add('active');
                break;
        }
    }

    /**
     * Show ISBN loading state
     */
    showIsbnLoading() {
        const button = document.getElementById('validate-isbn-btn');
        button.disabled = true;
        button.textContent = 'Validating...';
    }

    /**
     * Hide ISBN loading state
     */
    hideIsbnLoading() {
        const button = document.getElementById('validate-isbn-btn');
        button.disabled = false;
        button.textContent = 'Validate ISBN';
    }

    /**
     * Reset form to initial state
     */
    resetForm() {
        this.currentStep = 'search';
        this.selectedBook = null;
        this.isbnValidationResults = {};
        
        // Clear form inputs
        document.getElementById('isbn-input').value = '';
        document.querySelectorAll('#manual-entry-section input, #manual-entry-section select, #manual-entry-section textarea').forEach(input => {
            input.value = '';
        });
        
        this.clearValidationResults();
        document.querySelector('.validation-errors').innerHTML = '';
        document.querySelector('.cover-preview').innerHTML = '';
        
        this.updateStepDisplay();
    }

    /**
     * Announce step change to screen readers
     */
    announceStepChange(message) {
        const announcement = document.getElementById('step-announcements');
        announcement.textContent = message;
    }
}