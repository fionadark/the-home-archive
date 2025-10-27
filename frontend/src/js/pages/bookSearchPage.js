/**
 * BookSearchPage - Main book search and discovery page
 * Part of Phase 5 User Story 3 implementation
 */

import { BookService } from '../services/BookService.js';
import { NotificationService } from '../services/NotificationService.js';

export default class BookSearchPage {
    constructor() {
        this.bookService = new BookService();
        this.notificationService = new NotificationService();
        
        this.currentPage = 0;
        this.totalPages = 0;
        this.searchResults = [];
        this.currentSearchQuery = '';
        this.searchFilters = {
            searchType: 'title',
            category: '',
            yearFrom: '',
            yearTo: '',
            minRating: '',
            author: '',
            includeExternal: false
        };
        
        this.suggestionTimeout = null;
        this.currentSuggestionIndex = -1;
    }

    /**
     * Render the book search page
     */
    render() {
        const container = document.getElementById('book-search-container') || document.body;
        container.classList.add('dark-academia');
        
        container.innerHTML = `
            <div class="search-container elegant-form">
                <div class="step-progress">
                    <div class="step active">Search</div>
                    <div class="step">Results</div>
                    <div class="step">Details</div>
                </div>
                
                <div class="search-form">
                    <div class="form-group">
                        <label for="search-input">Search Books</label>
                        <div class="search-input-group">
                            <input 
                                type="text" 
                                id="search-input" 
                                placeholder="Enter title, author, or ISBN..."
                                aria-label="Search for books"
                            />
                            <div class="search-suggestions" style="display: none;"></div>
                        </div>
                    </div>
                    
                    <div class="form-group">
                        <label for="search-type-select">Search Type</label>
                        <select id="search-type-select" aria-label="Select search type">
                            <option value="title">Title</option>
                            <option value="author">Author</option>
                            <option value="isbn">ISBN</option>
                            <option value="keyword">Keyword</option>
                        </select>
                    </div>
                    
                    <button id="search-button" class="btn btn-primary" aria-label="Search for books">
                        Search
                    </button>
                    
                    <button id="toggle-advanced-filters" class="btn btn-secondary">
                        Advanced Filters
                    </button>
                </div>
                
                <div id="advanced-filters" class="advanced-filters">
                    <div id="advanced-filters-content" style="display: none;">
                        <div class="form-row">
                            <div class="form-group">
                                <label for="filter-author">Author</label>
                                <input type="text" id="filter-author" placeholder="Filter by author" />
                            </div>
                            
                            <div class="form-group">
                                <label for="filter-category">Category</label>
                                <select id="filter-category">
                                    <option value="">All Categories</option>
                                </select>
                            </div>
                        </div>
                        
                        <div class="form-row">
                            <div class="form-group">
                                <label for="filter-year-from">Year From</label>
                                <input type="number" id="filter-year-from" placeholder="e.g., 1950" />
                            </div>
                            
                            <div class="form-group">
                                <label for="filter-year-to">Year To</label>
                                <input type="number" id="filter-year-to" placeholder="e.g., 2023" />
                            </div>
                        </div>
                        
                        <div class="form-row">
                            <div class="form-group">
                                <label for="filter-min-rating">Minimum Rating</label>
                                <input type="number" id="filter-min-rating" min="0" max="5" step="0.1" placeholder="e.g., 4.0" />
                            </div>
                            
                            <div class="form-group">
                                <label>
                                    <input type="checkbox" id="filter-include-external" />
                                    Include External Sources
                                </label>
                            </div>
                        </div>
                        
                        <button id="reset-filters-button" class="btn btn-secondary">
                            Reset Filters
                        </button>
                    </div>
                </div>
            </div>
            
            <div id="search-results" class="search-results">
                <div class="loading-spinner" style="display: none;"></div>
                <div class="search-results-content"></div>
                <div class="pagination" style="display: none;">
                    <button class="prev-page-btn btn btn-secondary" disabled>Previous</button>
                    <span class="page-info">Page 1 of 1</span>
                    <button class="next-page-btn btn btn-secondary">Next</button>
                </div>
            </div>
            
            <div class="sr-only" aria-live="polite" id="search-announcements"></div>
        `;
        
        this.bindEvents();
    }

    /**
     * Bind event listeners
     */
    bindEvents() {
        const searchInput = document.getElementById('search-input');
        const searchButton = document.getElementById('search-button');
        const searchTypeSelect = document.getElementById('search-type-select');
        const toggleAdvanced = document.getElementById('toggle-advanced-filters');
        const resetFilters = document.getElementById('reset-filters-button');

        // Search events
        searchButton.addEventListener('click', () => this.handleSearch());
        searchInput.addEventListener('keypress', (e) => {
            if (e.key === 'Enter') {
                this.handleSearch();
            }
        });

        // Advanced filters toggle
        toggleAdvanced.addEventListener('click', () => {
            const content = document.getElementById('advanced-filters-content');
            content.style.display = content.style.display === 'none' ? 'block' : 'none';
        });

        // Reset filters
        resetFilters.addEventListener('click', () => this.resetFilters());

        // Search suggestions
        searchInput.addEventListener('input', (e) => this.handleSearchInput(e));
        searchInput.addEventListener('keydown', (e) => this.handleSuggestionNavigation(e));

        // Pagination
        document.querySelector('.prev-page-btn').addEventListener('click', () => this.previousPage());
        document.querySelector('.next-page-btn').addEventListener('click', () => this.nextPage());
    }

    /**
     * Handle search button click
     */
    handleSearch() {
        const query = document.getElementById('search-input').value.trim();
        const searchType = document.getElementById('search-type-select').value;
        
        if (!query) {
            this.notificationService.showError('Please enter a search term');
            return;
        }

        // Validate filters
        if (!this.validateFilters()) {
            return;
        }

        this.performSearch(query, searchType);
    }

    /**
     * Validate search filters
     */
    validateFilters() {
        const yearFrom = document.getElementById('filter-year-from').value;
        const yearTo = document.getElementById('filter-year-to').value;
        const minRating = document.getElementById('filter-min-rating').value;

        if (yearFrom && yearTo && parseInt(yearFrom) > parseInt(yearTo)) {
            this.notificationService.showError('Year range is invalid: "From" year cannot be greater than "To" year');
            return false;
        }

        if (minRating && (parseFloat(minRating) < 0 || parseFloat(minRating) > 5)) {
            this.notificationService.showError('Rating must be between 0 and 5');
            return false;
        }

        return true;
    }

    /**
     * Perform search with current parameters
     */
    async performSearch(query, searchType) {
        this.showLoading();
        this.currentSearchQuery = query;

        try {
            const searchParams = {
                q: query,
                searchType: searchType,
                page: this.currentPage,
                size: 20
            };

            // Add advanced filters
            const author = document.getElementById('filter-author').value;
            const category = document.getElementById('filter-category').value;
            const yearFrom = document.getElementById('filter-year-from').value;
            const yearTo = document.getElementById('filter-year-to').value;
            const minRating = document.getElementById('filter-min-rating').value;
            const includeExternal = document.getElementById('filter-include-external').checked;

            if (author) searchParams.author = author;
            if (category) searchParams.category = category;
            if (yearFrom) searchParams.yearFrom = yearFrom;
            if (yearTo) searchParams.yearTo = yearTo;
            if (minRating) searchParams.minRating = minRating;
            if (includeExternal) searchParams.includeExternal = includeExternal;

            const results = await this.bookService.searchBooks(searchParams);
            this.displaySearchResults(results);

        } catch (error) {
            this.handleSearchError(error);
        } finally {
            this.hideLoading();
        }
    }

    /**
     * Display search results
     */
    displaySearchResults(results) {
        this.searchResults = results.books.content;
        this.totalPages = results.totalPages;
        this.currentPage = results.currentPage;

        const resultsContainer = document.querySelector('.search-results-content');
        
        if (this.searchResults.length === 0) {
            resultsContainer.innerHTML = `
                <div class="empty-results">
                    <h3>No books found</h3>
                    <p>Try adjusting your search criteria or filters.</p>
                </div>
            `;
        } else {
            resultsContainer.innerHTML = this.searchResults.map(book => this.createBookCard(book)).join('');
            this.bindBookCardEvents();
        }

        this.updatePagination();
        this.announceResults(results.totalElements);

        // Show external API notice if applicable
        if (results.externalApiUsed) {
            const notice = document.createElement('div');
            notice.className = 'external-api-notice';
            notice.textContent = `Results include books from ${results.externalApiSource}`;
            resultsContainer.insertBefore(notice, resultsContainer.firstChild);
        }
    }

    /**
     * Create book card HTML
     */
    createBookCard(book) {
        return `
            <div class="book-card" data-book-id="${book.id}">
                <div class="book-cover">
                    ${book.coverImageUrl ? 
                        `<img src="${book.coverImageUrl}" alt="Cover of ${book.title}" />` :
                        `<div class="cover-placeholder">No Cover</div>`
                    }
                </div>
                <div class="book-details">
                    <h4 class="book-title">${book.title}</h4>
                    <p class="book-author">by ${book.author}</p>
                    <p class="book-year">${book.publicationYear || ''}</p>
                    <p class="book-category">${book.categoryName || ''}</p>
                    <div class="book-rating">
                        ${book.averageRating ? `★ ${book.averageRating}` : 'Not rated'}
                    </div>
                    <div class="book-actions">
                        ${book.inUserLibrary ? 
                            `<span class="in-library-indicator">✓ In Your Library</span>` :
                            `<button class="add-to-library-btn btn btn-primary" data-book-id="${book.id}">
                                Add to Library
                            </button>`
                        }
                    </div>
                </div>
            </div>
        `;
    }

    /**
     * Bind events for book cards
     */
    bindBookCardEvents() {
        const addButtons = document.querySelectorAll('.add-to-library-btn');
        addButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                const bookId = parseInt(e.target.dataset.bookId);
                this.addBookToLibrary(bookId, e.target);
            });
        });
    }

    /**
     * Add book to user library
     */
    async addBookToLibrary(bookId, buttonElement) {
        try {
            await this.bookService.addBookToLibrary(bookId);
            this.notificationService.showSuccess('Book added to your library!');
            
            // Update UI
            const bookCard = buttonElement.closest('.book-card');
            const actionsDiv = bookCard.querySelector('.book-actions');
            actionsDiv.innerHTML = '<span class="in-library-indicator">✓ In Your Library</span>';
            
        } catch (error) {
            this.notificationService.showError(error.message);
        }
    }

    /**
     * Handle search input for suggestions
     */
    async handleSearchInput(event) {
        const query = event.target.value.trim();
        
        if (this.suggestionTimeout) {
            clearTimeout(this.suggestionTimeout);
        }

        if (query.length < 2) {
            this.hideSuggestions();
            return;
        }

        this.suggestionTimeout = setTimeout(async () => {
            try {
                const suggestions = await this.bookService.getSearchSuggestions(query, 5);
                this.displaySuggestions(suggestions);
            } catch (error) {
                console.error('Failed to load suggestions:', error);
            }
        }, 300);
    }

    /**
     * Display search suggestions
     */
    displaySuggestions(suggestions) {
        const container = document.querySelector('.search-suggestions');
        
        if (suggestions.length === 0) {
            this.hideSuggestions();
            return;
        }

        container.innerHTML = suggestions.map((suggestion, index) => 
            `<div class="suggestion-item" data-index="${index}">${suggestion}</div>`
        ).join('');
        
        container.style.display = 'block';
        this.bindSuggestionEvents();
    }

    /**
     * Bind suggestion events
     */
    bindSuggestionEvents() {
        const suggestions = document.querySelectorAll('.suggestion-item');
        suggestions.forEach(item => {
            item.addEventListener('click', () => {
                document.getElementById('search-input').value = item.textContent;
                this.hideSuggestions();
            });
        });
    }

    /**
     * Handle keyboard navigation for suggestions
     */
    handleSuggestionNavigation(event) {
        const suggestions = document.querySelectorAll('.suggestion-item');
        
        if (suggestions.length === 0) return;

        switch (event.key) {
            case 'ArrowDown':
                event.preventDefault();
                this.currentSuggestionIndex = Math.min(this.currentSuggestionIndex + 1, suggestions.length - 1);
                this.highlightSuggestion();
                break;
            case 'ArrowUp':
                event.preventDefault();
                this.currentSuggestionIndex = Math.max(this.currentSuggestionIndex - 1, -1);
                this.highlightSuggestion();
                break;
            case 'Enter':
                if (this.currentSuggestionIndex >= 0) {
                    event.preventDefault();
                    const selected = suggestions[this.currentSuggestionIndex];
                    document.getElementById('search-input').value = selected.textContent;
                    this.hideSuggestions();
                }
                break;
            case 'Escape':
                this.hideSuggestions();
                break;
        }
    }

    /**
     * Highlight current suggestion
     */
    highlightSuggestion() {
        const suggestions = document.querySelectorAll('.suggestion-item');
        suggestions.forEach((item, index) => {
            item.classList.toggle('highlighted', index === this.currentSuggestionIndex);
        });
    }

    /**
     * Hide suggestions
     */
    hideSuggestions() {
        const container = document.querySelector('.search-suggestions');
        container.style.display = 'none';
        this.currentSuggestionIndex = -1;
    }

    /**
     * Show loading state
     */
    showLoading() {
        const spinner = document.querySelector('.loading-spinner');
        const button = document.querySelector('#search-button');
        
        spinner.style.display = 'block';
        button.disabled = true;
    }

    /**
     * Hide loading state
     */
    hideLoading() {
        const spinner = document.querySelector('.loading-spinner');
        const button = document.querySelector('#search-button');
        
        spinner.style.display = 'none';
        button.disabled = false;
    }

    /**
     * Handle search errors
     */
    handleSearchError(error) {
        let message = 'Search failed. Please try again.';
        
        if (error.status === 429) {
            message = 'Too many requests. Please wait a moment and try again.';
        } else if (error.name === 'NetworkError') {
            message = 'Network error. Please check your connection and try again.';
        }
        
        this.notificationService.showError(message);
    }

    /**
     * Update pagination controls
     */
    updatePagination() {
        const pagination = document.querySelector('.pagination');
        const prevBtn = document.querySelector('.prev-page-btn');
        const nextBtn = document.querySelector('.next-page-btn');
        const pageInfo = document.querySelector('.page-info');

        if (this.totalPages <= 1) {
            pagination.style.display = 'none';
            return;
        }

        pagination.style.display = 'flex';
        prevBtn.disabled = this.currentPage === 0;
        nextBtn.disabled = this.currentPage >= this.totalPages - 1;
        pageInfo.textContent = `Page ${this.currentPage + 1} of ${this.totalPages}`;
    }

    /**
     * Go to previous page
     */
    async previousPage() {
        if (this.currentPage > 0) {
            this.currentPage--;
            await this.performSearch(this.currentSearchQuery, document.getElementById('search-type-select').value);
        }
    }

    /**
     * Go to next page
     */
    async nextPage() {
        if (this.currentPage < this.totalPages - 1) {
            this.currentPage++;
            await this.performSearch(this.currentSearchQuery, document.getElementById('search-type-select').value);
        }
    }

    /**
     * Reset all filters
     */
    resetFilters() {
        document.getElementById('filter-author').value = '';
        document.getElementById('filter-category').value = '';
        document.getElementById('filter-year-from').value = '';
        document.getElementById('filter-year-to').value = '';
        document.getElementById('filter-min-rating').value = '';
        document.getElementById('filter-include-external').checked = false;
    }

    /**
     * Announce search results to screen readers
     */
    announceResults(totalElements) {
        const announcement = document.getElementById('search-announcements');
        announcement.textContent = `${totalElements} search result${totalElements !== 1 ? 's' : ''} found`;
    }
}