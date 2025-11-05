/**
 * SearchResults Component for The Home Archive
 * Handles display and interaction of book search results from the complete database
 * Supports grid/list view toggling, pagination, and integration with BookDetailModal
 * Part of User Story 3 - Book Discovery and Addition
 */

class SearchResults {
    constructor(containerId = 'search-results-container') {
        this.container = document.getElementById(containerId);
        this.bookSearchService = window.bookSearchService || null;
        this.libraryService = window.libraryService || null;
        this.notificationService = window.notificationService || null;
        this.bookDetailModal = window.bookDetailModal || null;
        
        // State management
        this.currentResults = null;
        this.currentQuery = null;
        this.currentFilters = {};
        this.currentPage = 0;
        this.pageSize = 20;
        this.totalPages = 0;
        this.totalElements = 0;
        this.isLoading = false;
        this.viewMode = 'grid'; // 'grid' or 'list'
        
        // User's library books for status checking
        this.userLibraryBooks = new Set();
        
        // Bind methods
        this.handleViewDetails = this.handleViewDetails.bind(this);
        this.handleAddToLibrary = this.handleAddToLibrary.bind(this);
        this.handlePageChange = this.handlePageChange.bind(this);
        this.handleViewModeChange = this.handleViewModeChange.bind(this);
        this.handleSort = this.handleSort.bind(this);
        
        // Initialize component
        this.init();
    }

    /**
     * Initialize the search results component
     */
    async init() {
        if (!this.container) {
            console.error('SearchResults: Container element not found');
            return;
        }

        // Load user's library for status checking
        await this.loadUserLibrary();
        
        // Initialize container structure
        this.createResultsStructure();
        
        // Set up event listeners
        this.setupEventListeners();
        
        console.log('SearchResults component initialized');
    }

    /**
     * Load user's library to check which books are already added
     */
    async loadUserLibrary() {
        if (!this.libraryService) return;
        
        try {
            const response = await this.libraryService.getUserLibrary();
            if (response.success && response.data) {
                this.userLibraryBooks = new Set(
                    response.data.content?.map(item => item.book.id) || []
                );
            }
        } catch (error) {
            console.warn('Failed to load user library for status checking:', error);
        }
    }

    /**
     * Create the basic structure for search results
     */
    createResultsStructure() {
        this.container.innerHTML = `
            <!-- Search Results Header -->
            <div class="search-results-header">
                <div class="results-info">
                    <h2 class="results-title">Search Results</h2>
                    <div class="results-count" id="results-count"></div>
                </div>
                
                <div class="results-controls">
                    <!-- View Mode Toggle -->
                    <div class="view-toggle" id="view-toggle">
                        <button class="view-btn active" data-view="grid" title="Grid View">
                            <i class="fas fa-th-large"></i>
                        </button>
                        <button class="view-btn" data-view="list" title="List View">
                            <i class="fas fa-list"></i>
                        </button>
                    </div>
                    
                    <!-- Sort Options -->
                    <div class="sort-controls">
                        <select id="sort-select" class="sort-select">
                            <option value="relevance">Most Relevant</option>
                            <option value="title">Title A-Z</option>
                            <option value="author">Author A-Z</option>
                            <option value="publicationYear">Publication Year</option>
                            <option value="rating">Highest Rated</option>
                        </select>
                        
                        <button id="sort-direction" class="sort-direction-btn" title="Sort Direction">
                            <i class="fas fa-sort-amount-down"></i>
                        </button>
                    </div>
                </div>
            </div>
            
            <!-- Search Results Content -->
            <div class="search-results-content">
                <!-- Loading State -->
                <div class="loading-state" id="loading-state" style="display: none;">
                    <div class="loading-spinner">
                        <i class="fas fa-book fa-spin"></i>
                    </div>
                    <p class="loading-text">Searching our vast collection...</p>
                </div>
                
                <!-- Error State -->
                <div class="error-state" id="error-state" style="display: none;">
                    <div class="error-icon">
                        <i class="fas fa-exclamation-triangle"></i>
                    </div>
                    <h3 class="error-title">Search Error</h3>
                    <p class="error-message" id="error-message"></p>
                    <button class="retry-btn" id="retry-btn">Try Again</button>
                </div>
                
                <!-- Empty State -->
                <div class="empty-state" id="empty-state" style="display: none;">
                    <div class="empty-icon">
                        <i class="fas fa-search"></i>
                    </div>
                    <h3 class="empty-title">No Books Found</h3>
                    <p class="empty-message" id="empty-message">
                        We couldn't find any books matching your search criteria.
                    </p>
                    <div class="empty-suggestions" id="empty-suggestions"></div>
                </div>
                
                <!-- Results Container -->
                <div class="results-container" id="results-container">
                    <div class="results-grid" id="results-grid"></div>
                </div>
            </div>
            
            <!-- Pagination -->
            <div class="pagination-container" id="pagination-container" style="display: none;">
                <div class="pagination">
                    <button class="page-btn prev-btn" id="prev-btn" disabled>
                        <i class="fas fa-chevron-left"></i>
                        Previous
                    </button>
                    
                    <div class="page-numbers" id="page-numbers"></div>
                    
                    <button class="page-btn next-btn" id="next-btn" disabled>
                        Next
                        <i class="fas fa-chevron-right"></i>
                    </button>
                </div>
                
                <div class="pagination-info" id="pagination-info"></div>
            </div>
        `;
    }

    /**
     * Set up event listeners for the component
     */
    setupEventListeners() {
        // View mode toggle
        const viewToggle = this.container.querySelector('#view-toggle');
        viewToggle?.addEventListener('click', this.handleViewModeChange);
        
        // Sort controls
        const sortSelect = this.container.querySelector('#sort-select');
        sortSelect?.addEventListener('change', this.handleSort);
        
        const sortDirection = this.container.querySelector('#sort-direction');
        sortDirection?.addEventListener('click', this.handleSort);
        
        // Pagination
        const prevBtn = this.container.querySelector('#prev-btn');
        prevBtn?.addEventListener('click', () => this.handlePageChange(this.currentPage - 1));
        
        const nextBtn = this.container.querySelector('#next-btn');
        nextBtn?.addEventListener('click', () => this.handlePageChange(this.currentPage + 1));
        
        // Retry button
        const retryBtn = this.container.querySelector('#retry-btn');
        retryBtn?.addEventListener('click', () => this.performSearch());
        
        // Results container for delegated events
        const resultsContainer = this.container.querySelector('#results-container');
        resultsContainer?.addEventListener('click', this.handleResultsClick.bind(this));
    }

    /**
     * Handle clicks within the results container (delegated events)
     */
    handleResultsClick(event) {
        const button = event.target.closest('button');
        if (!button) return;
        
        const bookCard = button.closest('[data-book-id]');
        if (!bookCard) return;
        
        const bookId = parseInt(bookCard.dataset.bookId);
        const bookData = this.findBookById(bookId);
        
        if (button.classList.contains('btn-view-details')) {
            this.handleViewDetails(bookData);
        } else if (button.classList.contains('btn-add-to-library')) {
            this.handleAddToLibrary(bookData);
        }
    }

    /**
     * Find book data by ID in current results
     */
    findBookById(bookId) {
        if (!this.currentResults?.content) return null;
        return this.currentResults.content.find(book => book.id === bookId);
    }

    /**
     * Search for books with given query and filters
     */
    async searchBooks(query, filters = {}, page = 0) {
        try {
            this.currentQuery = query;
            this.currentFilters = filters;
            this.currentPage = page;
            
            await this.performSearch();
        } catch (error) {
            console.error('Error initiating search:', error);
            this.showError('Failed to start search. Please try again.');
        }
    }

    /**
     * Perform the actual search request
     */
    async performSearch() {
        if (!this.bookSearchService) {
            this.showError('Search service not available');
            return;
        }

        this.showLoading();
        
        try {
            const searchOptions = {
                q: this.currentQuery,
                ...this.currentFilters,
                page: this.currentPage,
                size: this.pageSize,
                sort: this.currentFilters.sort || 'relevance',
                direction: this.currentFilters.direction || 'asc'
            };
            
            const response = await this.bookSearchService.searchBooks(searchOptions);
            
            if (response.success) {
                this.currentResults = response.data;
                this.totalPages = response.data.totalPages || 0;
                this.totalElements = response.data.totalElements || 0;
                
                this.displayResults();
                this.updatePagination();
            } else {
                throw new Error(response.message || 'Search failed');
            }
        } catch (error) {
            console.error('Search error:', error);
            this.showError(error.message || 'Search failed. Please try again.');
        } finally {
            this.hideLoading();
        }
    }

    /**
     * Display search results
     */
    displayResults() {
        const resultsGrid = this.container.querySelector('#results-grid');
        const resultsCount = this.container.querySelector('#results-count');
        
        if (!this.currentResults?.content?.length) {
            this.showEmpty();
            return;
        }

        // Update results count
        resultsCount.textContent = this.formatResultsCount();
        
        // Clear previous results
        resultsGrid.innerHTML = '';
        
        // Set view mode class
        resultsGrid.className = this.viewMode === 'list' ? 'results-list' : 'results-grid';
        
        // Render each book
        this.currentResults.content.forEach(book => {
            const bookElement = this.renderBookCard(book);
            resultsGrid.appendChild(bookElement);
        });
        
        this.showResults();
    }

    /**
     * Render a single book card
     */
    renderBookCard(book) {
        const bookElement = document.createElement('div');
        bookElement.className = `book-card ${this.viewMode === 'list' ? 'list-view' : ''}`;
        bookElement.setAttribute('data-book-id', book.id);
        bookElement.setAttribute('role', 'article');
        bookElement.setAttribute('aria-label', `Book: ${book.title} by ${book.author || 'Unknown Author'}`);
        
        const isInLibrary = this.userLibraryBooks.has(book.id);
        const coverUrl = book.coverImageUrl || '/images/default-book-cover.jpg';
        const rating = book.averageRating || 0;
        const ratingCount = book.ratingCount || 0;
        
        // Format publication info
        const publicationInfo = [];
        if (book.publicationYear) publicationInfo.push(book.publicationYear);
        if (book.pageCount) publicationInfo.push(`${book.pageCount} pages`);
        
        bookElement.innerHTML = `
            <!-- Book Cover -->
            <div class="book-card-cover">
                ${book.coverImageUrl ? `
                    <img 
                        src="${coverUrl}" 
                        alt="Cover of ${this.escapeHtml(book.title)}"
                        class="book-cover-image"
                        loading="lazy"
                        onerror="this.parentElement.innerHTML='<div class=\\"book-cover-placeholder\\"><i class=\\"fas fa-book\\"></i></div>'"
                    >
                ` : `
                    <div class="book-cover-placeholder">
                        <i class="fas fa-book"></i>
                    </div>
                `}
                <div class="cover-overlay"></div>
            </div>
            
            <!-- Book Content -->
            <div class="book-card-content">
                <div class="book-info-section">
                    <h3 class="book-card-title">${this.escapeHtml(book.title)}</h3>
                    <p class="book-card-author">by ${this.escapeHtml(book.author || 'Unknown Author')}</p>
                    
                    <div class="book-card-meta">
                        ${book.category ? `
                            <span class="book-category">${this.escapeHtml(book.category.name)}</span>
                        ` : ''}
                        
                        <div class="book-rating">
                            <div class="rating-stars">
                                ${this.renderStars(rating)}
                            </div>
                            <span class="rating-text">
                                ${rating > 0 ? `${rating.toFixed(1)} (${ratingCount})` : 'No ratings'}
                            </span>
                        </div>
                        
                        ${publicationInfo.length > 0 ? `
                            <div class="publication-info">
                                <span class="publication-year">${publicationInfo[0]}</span>
                                ${publicationInfo[1] ? `<span class="page-count">${publicationInfo[1]}</span>` : ''}
                            </div>
                        ` : ''}
                    </div>
                    
                    ${book.description ? `
                        <p class="book-description-preview">${this.escapeHtml(book.description)}</p>
                    ` : ''}
                </div>
                
                <!-- Book Actions -->
                <div class="book-actions">
                    <button class="action-btn btn-view-details" aria-label="View details for ${this.escapeHtml(book.title)}">
                        <i class="fas fa-eye"></i>
                        View Details
                    </button>
                    
                    ${isInLibrary ? `
                        <button class="action-btn btn-in-library" disabled aria-label="Already in library">
                            <i class="fas fa-check"></i>
                            In Library
                        </button>
                    ` : `
                        <button class="action-btn btn-add-to-library" aria-label="Add ${this.escapeHtml(book.title)} to library">
                            <i class="fas fa-plus"></i>
                            Add to Library
                        </button>
                    `}
                </div>
            </div>
            
            <!-- Quick Actions Overlay (for grid view) -->
            ${this.viewMode === 'grid' ? `
                <div class="quick-actions">
                    <button class="quick-action-btn ${isInLibrary ? 'in-library' : ''}" 
                            title="${isInLibrary ? 'In Library' : 'Add to Library'}"
                            ${isInLibrary ? 'disabled' : ''}>
                        <i class="fas ${isInLibrary ? 'fa-check' : 'fa-plus'}"></i>
                    </button>
                </div>
            ` : ''}
        `;
        
        return bookElement;
    }

    /**
     * Render star rating display
     */
    renderStars(rating) {
        const fullStars = Math.floor(rating);
        const hasHalfStar = rating % 1 >= 0.5;
        const totalStars = 5;
        let starsHTML = '';
        
        for (let i = 0; i < totalStars; i++) {
            if (i < fullStars) {
                starsHTML += '<i class="fas fa-star star"></i>';
            } else if (i === fullStars && hasHalfStar) {
                starsHTML += '<i class="fas fa-star-half-alt star"></i>';
            } else {
                starsHTML += '<i class="far fa-star star empty"></i>';
            }
        }
        
        return starsHTML;
    }

    /**
     * Handle view details button click
     */
    async handleViewDetails(bookData) {
        if (!bookData) return;
        
        try {
            if (this.bookDetailModal) {
                await this.bookDetailModal.show(bookData.id);
            } else {
                // Fallback: redirect to book details page
                window.location.href = `/book-details.html?id=${bookData.id}`;
            }
        } catch (error) {
            console.error('Error showing book details:', error);
            this.showNotification('Failed to load book details', 'error');
        }
    }

    /**
     * Handle add to library button click
     */
    async handleAddToLibrary(bookData) {
        if (!bookData || !this.libraryService) {
            this.showNotification('Library service not available', 'error');
            return;
        }
        
        try {
            const response = await this.libraryService.addBook(bookData.id);
            
            if (response.success) {
                // Update local state
                this.userLibraryBooks.add(bookData.id);
                
                // Update UI for this book
                this.updateBookCardLibraryStatus(bookData.id, true);
                
                this.showNotification(`"${bookData.title}" added to your library!`, 'success');
            } else {
                throw new Error(response.message || 'Failed to add book to library');
            }
        } catch (error) {
            console.error('Error adding book to library:', error);
            this.showNotification(error.message || 'Failed to add book to library', 'error');
        }
    }

    /**
     * Update book card library status in UI
     */
    updateBookCardLibraryStatus(bookId, isInLibrary) {
        const bookCard = this.container.querySelector(`[data-book-id="${bookId}"]`);
        if (!bookCard) return;
        
        const addButton = bookCard.querySelector('.btn-add-to-library');
        const quickActionBtn = bookCard.querySelector('.quick-action-btn');
        
        if (isInLibrary && addButton) {
            // Replace add button with in-library button
            addButton.outerHTML = `
                <button class="action-btn btn-in-library" disabled aria-label="Already in library">
                    <i class="fas fa-check"></i>
                    In Library
                </button>
            `;
        }
        
        if (quickActionBtn) {
            quickActionBtn.disabled = isInLibrary;
            quickActionBtn.className = `quick-action-btn ${isInLibrary ? 'in-library' : ''}`;
            quickActionBtn.title = isInLibrary ? 'In Library' : 'Add to Library';
            quickActionBtn.innerHTML = `<i class="fas ${isInLibrary ? 'fa-check' : 'fa-plus'}"></i>`;
        }
    }

    /**
     * Handle view mode change (grid/list)
     */
    handleViewModeChange(event) {
        const button = event.target.closest('.view-btn');
        if (!button) return;
        
        const newViewMode = button.dataset.view;
        if (newViewMode === this.viewMode) return;
        
        this.viewMode = newViewMode;
        
        // Update button states
        this.container.querySelectorAll('.view-btn').forEach(btn => {
            btn.classList.toggle('active', btn.dataset.view === this.viewMode);
        });
        
        // Re-render results with new view mode
        if (this.currentResults?.content?.length) {
            this.displayResults();
        }
    }

    /**
     * Handle sort option change
     */
    handleSort(event) {
        const sortSelect = this.container.querySelector('#sort-select');
        const sortDirection = this.container.querySelector('#sort-direction');
        
        if (event.target === sortDirection) {
            // Toggle sort direction
            const currentDirection = this.currentFilters.direction || 'asc';
            this.currentFilters.direction = currentDirection === 'asc' ? 'desc' : 'asc';
            
            // Update button icon
            const icon = sortDirection.querySelector('i');
            icon.className = this.currentFilters.direction === 'asc' ? 
                'fas fa-sort-amount-down' : 'fas fa-sort-amount-up';
        } else if (event.target === sortSelect) {
            // Update sort field
            this.currentFilters.sort = sortSelect.value;
        }
        
        // Perform new search with updated sort
        this.currentPage = 0;
        this.performSearch();
    }

    /**
     * Handle page change
     */
    handlePageChange(newPage) {
        if (newPage < 0 || newPage >= this.totalPages || newPage === this.currentPage) {
            return;
        }
        
        this.currentPage = newPage;
        this.performSearch();
        
        // Scroll to top of results
        if (this.container && typeof this.container.scrollIntoView === 'function') {
            this.container.scrollIntoView({ behavior: 'smooth', block: 'start' });
        }
    }

    /**
     * Update pagination UI
     */
    updatePagination() {
        const paginationContainer = this.container.querySelector('#pagination-container');
        const prevBtn = this.container.querySelector('#prev-btn');
        const nextBtn = this.container.querySelector('#next-btn');
        const pageNumbers = this.container.querySelector('#page-numbers');
        const paginationInfo = this.container.querySelector('#pagination-info');
        
        if (this.totalPages <= 1) {
            paginationContainer.style.display = 'none';
            return;
        }
        
        paginationContainer.style.display = 'block';
        
        // Update prev/next buttons
        prevBtn.disabled = this.currentPage === 0;
        nextBtn.disabled = this.currentPage >= this.totalPages - 1;
        
        // Update page numbers
        pageNumbers.innerHTML = this.generatePageNumbers();
        
        // Update pagination info
        const startIndex = this.currentPage * this.pageSize + 1;
        const endIndex = Math.min((this.currentPage + 1) * this.pageSize, this.totalElements);
        paginationInfo.textContent = `Showing ${startIndex}-${endIndex} of ${this.totalElements} books`;
    }

    /**
     * Generate page numbers for pagination
     */
    generatePageNumbers() {
        const maxVisiblePages = 7;
        const currentPage = this.currentPage;
        const totalPages = this.totalPages;
        
        let startPage = Math.max(0, currentPage - Math.floor(maxVisiblePages / 2));
        let endPage = Math.min(totalPages - 1, startPage + maxVisiblePages - 1);
        
        // Adjust start page if we're near the end
        if (endPage - startPage < maxVisiblePages - 1) {
            startPage = Math.max(0, endPage - maxVisiblePages + 1);
        }
        
        let html = '';
        
        // First page
        if (startPage > 0) {
            html += `<button class="page-number" data-page="0">1</button>`;
            if (startPage > 1) {
                html += '<span class="page-ellipsis">...</span>';
            }
        }
        
        // Page numbers
        for (let i = startPage; i <= endPage; i++) {
            html += `
                <button class="page-number ${i === currentPage ? 'active' : ''}" data-page="${i}">
                    ${i + 1}
                </button>
            `;
        }
        
        // Last page
        if (endPage < totalPages - 1) {
            if (endPage < totalPages - 2) {
                html += '<span class="page-ellipsis">...</span>';
            }
            html += `<button class="page-number" data-page="${totalPages - 1}">${totalPages}</button>`;
        }
        
        // Add click handlers
        setTimeout(() => {
            if (this.container) {
                this.container.querySelectorAll('.page-number').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const page = parseInt(btn.dataset.page);
                        this.handlePageChange(page);
                    });
                });
            }
        }, 0);
        
        return html;
    }

    /**
     * Format results count text
     */
    formatResultsCount() {
        if (this.totalElements === 0) {
            return 'No results found';
        } else if (this.totalElements === 1) {
            return '1 book found';
        } else {
            return `${this.totalElements.toLocaleString()} books found`;
        }
    }

    /**
     * Show loading state
     */
    showLoading() {
        this.isLoading = true;
        this.hideAllStates();
        this.container.querySelector('#loading-state').style.display = 'block';
    }

    /**
     * Hide loading state
     */
    hideLoading() {
        this.isLoading = false;
        this.container.querySelector('#loading-state').style.display = 'none';
    }

    /**
     * Show results
     */
    showResults() {
        this.hideAllStates();
        this.container.querySelector('.search-results-content .results-container').style.display = 'block';
    }

    /**
     * Show empty state
     */
    showEmpty() {
        this.hideAllStates();
        const emptyState = this.container.querySelector('#empty-state');
        const emptyMessage = this.container.querySelector('#empty-message');
        const emptySuggestions = this.container.querySelector('#empty-suggestions');
        
        emptyState.style.display = 'block';
        
        // Update message based on search type
        if (this.currentQuery) {
            emptyMessage.textContent = `No books found for "${this.currentQuery}". Try different keywords or filters.`;
        } else {
            emptyMessage.textContent = 'No books found matching your search criteria.';
        }
        
        // Add search suggestions
        this.addSearchSuggestions(emptySuggestions);
    }

    /**
     * Show error state
     */
    showError(message) {
        this.hideAllStates();
        const errorState = this.container.querySelector('#error-state');
        const errorMessage = this.container.querySelector('#error-message');
        
        errorState.style.display = 'block';
        errorMessage.textContent = message;
    }

    /**
     * Hide all states
     */
    hideAllStates() {
        const states = [
            '#loading-state',
            '#error-state', 
            '#empty-state',
            '.search-results-content .results-container'
        ];
        
        states.forEach(selector => {
            const element = this.container.querySelector(selector);
            if (element) element.style.display = 'none';
        });
        
        this.container.querySelector('#pagination-container').style.display = 'none';
    }

    /**
     * Add search suggestions to empty state
     */
    async addSearchSuggestions(container) {
        if (!this.bookSearchService) return;
        
        try {
            // Get popular searches or categories as suggestions
            const response = await this.bookSearchService.getPopularSearches(8);
            
            if (response.success && response.data?.length) {
                container.innerHTML = response.data.map(search => 
                    `<button class="suggestion-tag" data-suggestion="${this.escapeHtml(search.query || search.name)}">${this.escapeHtml(search.query || search.name)}</button>`
                ).join('');
                
                // Add click handlers for suggestions
                container.querySelectorAll('.suggestion-tag').forEach(tag => {
                    tag.addEventListener('click', () => {
                        const suggestion = tag.dataset.suggestion;
                        // Emit event for parent component to handle
                        this.container.dispatchEvent(new CustomEvent('suggestion-click', {
                            detail: { suggestion }
                        }));
                    });
                });
            }
        } catch (error) {
            console.warn('Failed to load search suggestions:', error);
        }
    }

    /**
     * Show notification
     */
    showNotification(message, type = 'info') {
        if (this.notificationService) {
            if (type === 'error') {
                this.notificationService.showError(message);
            } else if (type === 'success') {
                this.notificationService.showSuccess(message);
            } else {
                this.notificationService.showInfo?.(message) || this.notificationService.showSuccess(message);
            }
        } else {
            // Fallback to console
            console.log(`${type.toUpperCase()}: ${message}`);
        }
    }

    /**
     * Update search filters without re-searching
     */
    updateFilters(filters) {
        this.currentFilters = { ...this.currentFilters, ...filters };
    }

    /**
     * Get current search state
     */
    getCurrentState() {
        return {
            query: this.currentQuery,
            filters: this.currentFilters,
            page: this.currentPage,
            viewMode: this.viewMode,
            results: this.currentResults
        };
    }

    /**
     * Clear all results and reset state
     */
    clear() {
        this.currentResults = null;
        this.currentQuery = null;
        this.currentFilters = {};
        this.currentPage = 0;
        this.totalPages = 0;
        this.totalElements = 0;
        
        this.hideAllStates();
        this.container.querySelector('#results-count').textContent = '';
    }

    /**
     * Utility method to escape HTML
     */
    escapeHtml(text) {
        if (typeof text !== 'string') return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    /**
     * Utility method to highlight search terms
     */
    highlightSearchTerms(text, terms) {
        if (!terms || !text) return text;
        
        const termArray = Array.isArray(terms) ? terms : [terms];
        let highlightedText = text;
        
        termArray.forEach(term => {
            if (term.length > 2) {
                const regex = new RegExp(`(${term})`, 'gi');
                highlightedText = highlightedText.replace(regex, '<span class="search-highlight">$1</span>');
            }
        });
        
        return highlightedText;
    }

    /**
     * Destroy the component
     */
    destroy() {
        // Remove all event listeners
        this.container?.removeEventListener('click', this.handleResultsClick);
        
        // Clear the container
        if (this.container) {
            this.container.innerHTML = '';
        }
        
        // Clear references
        this.container = null;
        this.currentResults = null;
        this.bookSearchService = null;
        this.libraryService = null;
        this.notificationService = null;
        this.bookDetailModal = null;
    }
}

// Create global instance if container exists
if (typeof window !== 'undefined') {
    document.addEventListener('DOMContentLoaded', () => {
        if (document.getElementById('search-results-container')) {
            window.searchResults = new SearchResults();
        }
    });
}

// Export for module use
if (typeof module !== 'undefined' && module.exports) {
    module.exports = SearchResults;
}