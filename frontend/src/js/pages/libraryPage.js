/**
 * Library Page Controller for The Home Archive
 * Main controller that orchestrates all library page components
 * Manages state, data loading, pagination, and component interactions
 */

(function() {
    'use strict';

    // Import components and services
    const { BookCard } = window.BookCard || {};
    const { SearchFilter } = window.SearchFilter || {};
    const { ViewToggle } = window.ViewToggle || {};
    const { confirmDialog } = window.confirmDialog || {};
    
    class LibraryPageController {
        constructor() {
            this.initialized = false;
            this.isLoading = false;
            this.error = null;
            
            // Data state
            this.books = [];
            this.filteredBooks = [];
            this.totalBooks = 0;
            this.currentPage = 1;
            this.booksPerPage = 20;
            this.totalPages = 0;
            
            // View state
            this.currentView = 'grid'; // 'grid' or 'list'
            
            // Component instances
            this.searchFilter = null;
            this.viewToggle = null;
            this.bookCard = null;
            
            // DOM elements
            this.elements = {
                container: null,
                booksGrid: null,
                loadingSpinner: null,
                errorContainer: null,
                emptyState: null,
                statsContainer: null,
                paginationContainer: null,
                addBookButton: null
            };
            
            // Pagination elements
            this.paginationElements = {
                prevButton: null,
                nextButton: null,
                pageInfo: null,
                pageButtons: null,
                jumpToPage: null
            };
            
            // Bind methods
            this.handleFilterChange = this.handleFilterChange.bind(this);
            this.handleSearchChange = this.handleSearchChange.bind(this);
            this.handleViewChange = this.handleViewChange.bind(this);
            this.handleBookUpdate = this.handleBookUpdate.bind(this);
            this.handleBookDelete = this.handleBookDelete.bind(this);
            this.handleAddBook = this.handleAddBook.bind(this);
            this.handlePageChange = this.handlePageChange.bind(this);
        }

        /**
         * Initialize the library page
         */
        async init() {
            try {
                console.log('Initializing Library Page Controller...');
                
                // Bind DOM elements
                this.bindElements();
                
                // Initialize components
                await this.initializeComponents();
                
                // Setup event listeners
                this.setupEventListeners();
                
                // Load initial data
                await this.loadData();
                
                this.initialized = true;
                console.log('Library Page Controller initialized successfully');
                
                // Trigger initial render
                this.render();
                
            } catch (error) {
                console.error('Failed to initialize library page:', error);
                this.showError('Failed to initialize library page. Please refresh and try again.');
            }
        }

        /**
         * Bind DOM elements
         */
        bindElements() {
            this.elements.container = document.getElementById('library-page');
            this.elements.booksGrid = document.getElementById('books-grid');
            this.elements.loadingSpinner = document.getElementById('loading-spinner');
            this.elements.errorContainer = document.getElementById('error-container');
            this.elements.emptyState = document.getElementById('empty-state');
            this.elements.statsContainer = document.getElementById('library-stats');
            this.elements.paginationContainer = document.getElementById('pagination-container');
            this.elements.addBookButton = document.getElementById('add-book-btn');
            
            // Pagination elements
            if (this.elements.paginationContainer) {
                this.paginationElements.prevButton = this.elements.paginationContainer.querySelector('#prev-page');
                this.paginationElements.nextButton = this.elements.paginationContainer.querySelector('#next-page');
                this.paginationElements.pageInfo = this.elements.paginationContainer.querySelector('#page-info');
                this.paginationElements.pageButtons = this.elements.paginationContainer.querySelector('#page-buttons');
                this.paginationElements.jumpToPage = this.elements.paginationContainer.querySelector('#jump-to-page');
            }
            
            // Verify required elements
            if (!this.elements.container) {
                throw new Error('Library page container not found');
            }
            if (!this.elements.booksGrid) {
                throw new Error('Books grid container not found');
            }
        }

        /**
         * Initialize component instances
         */
        async initializeComponents() {
            // Initialize search filter
            if (typeof SearchFilter !== 'undefined') {
                this.searchFilter = new SearchFilter({
                    searchDelay: 300,
                    autoSync: true
                });
                
                // Set up callbacks
                this.searchFilter.onFilterChange = this.handleFilterChange;
                this.searchFilter.onSearchChange = this.handleSearchChange;
            } else {
                console.warn('SearchFilter component not available');
            }
            
            // Initialize view toggle
            if (typeof ViewToggle !== 'undefined') {
                this.viewToggle = new ViewToggle({
                    defaultView: 'grid',
                    persistPreference: true
                });
                
                // Set up callback
                this.viewToggle.onViewChange = this.handleViewChange;
                this.currentView = this.viewToggle.getCurrentView();
            } else {
                console.warn('ViewToggle component not available');
            }
            
            // Initialize book card component
            if (typeof BookCard !== 'undefined') {
                this.bookCard = new BookCard({
                    onStatusUpdate: this.handleBookUpdate,
                    onDelete: this.handleBookDelete,
                    onEdit: this.handleBookUpdate
                });
            } else {
                console.warn('BookCard component not available');
            }
            
            // Load categories and locations for filters
            await this.loadFilterOptions();
        }

        /**
         * Setup event listeners
         */
        setupEventListeners() {
            // Add book button
            if (this.elements.addBookButton) {
                this.elements.addBookButton.addEventListener('click', this.handleAddBook);
            }
            
            // Pagination event listeners
            if (this.paginationElements.prevButton) {
                this.paginationElements.prevButton.addEventListener('click', () => {
                    this.handlePageChange(this.currentPage - 1);
                });
            }
            
            if (this.paginationElements.nextButton) {
                this.paginationElements.nextButton.addEventListener('click', () => {
                    this.handlePageChange(this.currentPage + 1);
                });
            }
            
            if (this.paginationElements.jumpToPage) {
                this.paginationElements.jumpToPage.addEventListener('keypress', (e) => {
                    if (e.key === 'Enter') {
                        const page = parseInt(e.target.value);
                        if (page >= 1 && page <= this.totalPages) {
                            this.handlePageChange(page);
                        }
                    }
                });
            }
            
            // Window events
            window.addEventListener('popstate', () => {
                if (this.searchFilter) {
                    this.searchFilter.syncFromURL();
                    this.loadData();
                }
            });
            
            // Refresh data on focus (in case data changed in another tab)
            window.addEventListener('focus', () => {
                if (this.initialized) {
                    this.refreshData();
                }
            });
        }

        /**
         * Load filter options (categories and locations)
         */
        async loadFilterOptions() {
            try {
                // Load categories
                const categories = await window.libraryService.getCategories();
                if (this.searchFilter) {
                    this.searchFilter.populateCategoryFilter(categories);
                }
                
                // Load locations
                const locations = await window.libraryService.getLocations();
                if (this.searchFilter) {
                    this.searchFilter.populateLocationFilter(locations);
                }
            } catch (error) {
                console.error('Failed to load filter options:', error);
                // Don't show error to user as this is not critical
            }
        }

        /**
         * Load library data
         */
        async loadData() {
            if (this.isLoading) return;
            
            try {
                this.setLoading(true);
                this.clearError();
                
                // Get filter parameters
                const params = this.searchFilter ? this.searchFilter.getQueryParams() : {};
                
                // Add pagination parameters
                params.page = this.currentPage;
                params.limit = this.booksPerPage;
                
                console.log('Loading library data with params:', params);
                
                // Load books
                const response = await window.libraryService.getBooks(params);
                
                this.books = response.books || [];
                this.totalBooks = response.total || 0;
                this.totalPages = Math.ceil(this.totalBooks / this.booksPerPage);
                
                // Apply client-side filtering if needed
                this.applyFilters();
                
                // Update UI
                this.render();
                this.updateStats();
                this.updatePagination();
                
            } catch (error) {
                console.error('Failed to load library data:', error);
                this.showError('Failed to load your library. Please check your connection and try again.');
            } finally {
                this.setLoading(false);
            }
        }

        /**
         * Refresh data (reload from server)
         */
        async refreshData() {
            await this.loadData();
        }

        /**
         * Apply client-side filters
         */
        applyFilters() {
            this.filteredBooks = [...this.books];
            
            // Additional client-side filtering can be added here if needed
            // For now, server-side filtering is sufficient
        }

        /**
         * Handle filter changes
         */
        async handleFilterChange(filters, sort) {
            console.log('Filter change:', filters, sort);
            this.currentPage = 1; // Reset to first page
            await this.loadData();
        }

        /**
         * Handle search changes
         */
        async handleSearchChange(searchTerm) {
            console.log('Search change:', searchTerm);
            this.currentPage = 1; // Reset to first page
            await this.loadData();
        }

        /**
         * Handle view changes
         */
        handleViewChange(viewType) {
            console.log('View change:', viewType);
            this.currentView = viewType;
            this.render();
        }

        /**
         * Handle book updates
         */
        async handleBookUpdate(bookId, updateData) {
            try {
                console.log('Updating book:', bookId, updateData);
                
                // Update book on server
                const updatedBook = await window.libraryService.updateBook(bookId, updateData);
                
                // Update local data
                const index = this.books.findIndex(book => book.id === bookId);
                if (index !== -1) {
                    this.books[index] = updatedBook;
                    this.applyFilters();
                    this.render();
                    this.updateStats();
                }
                
                // Show success message
                this.showMessage('Book updated successfully', 'success');
                
            } catch (error) {
                console.error('Failed to update book:', error);
                this.showMessage('Failed to update book. Please try again.', 'error');
            }
        }

        /**
         * Handle book deletion
         */
        async handleBookDelete(bookId) {
            try {
                // Get book for confirmation dialog
                const book = this.books.find(b => b.id === bookId);
                if (!book) return;
                
                // Show confirmation dialog
                if (typeof confirmDialog !== 'undefined') {
                    const confirmed = await confirmDialog({
                        title: 'Delete Book',
                        message: `Are you sure you want to delete "${book.title}" from your library?`,
                        confirmText: 'Delete',
                        cancelText: 'Cancel',
                        type: 'danger'
                    });
                    
                    if (!confirmed) return;
                }
                
                console.log('Deleting book:', bookId);
                
                // Delete book on server
                await window.libraryService.deleteBook(bookId);
                
                // Remove from local data
                this.books = this.books.filter(book => book.id !== bookId);
                this.totalBooks--;
                this.applyFilters();
                
                // Update UI
                this.render();
                this.updateStats();
                this.updatePagination();
                
                // Show success message
                this.showMessage('Book deleted successfully', 'success');
                
                // If current page is empty, go to previous page
                if (this.filteredBooks.length === 0 && this.currentPage > 1) {
                    this.handlePageChange(this.currentPage - 1);
                }
                
            } catch (error) {
                console.error('Failed to delete book:', error);
                this.showMessage('Failed to delete book. Please try again.', 'error');
            }
        }

        /**
         * Handle add book button click
         */
        handleAddBook() {
            // Redirect to add book page
            window.location.href = '/add-book';
        }

        /**
         * Handle page changes
         */
        async handlePageChange(newPage) {
            if (newPage < 1 || newPage > this.totalPages || newPage === this.currentPage) {
                return;
            }
            
            this.currentPage = newPage;
            await this.loadData();
            
            // Scroll to top of page
            this.elements.container.scrollIntoView({ behavior: 'smooth' });
        }

        /**
         * Render the books grid
         */
        render() {
            if (!this.elements.booksGrid) return;
            
            // Clear previous content
            this.elements.booksGrid.innerHTML = '';
            
            // Set grid class based on view type
            this.elements.booksGrid.className = `books-grid view-${this.currentView}`;
            
            // Check if we have books to display
            if (this.filteredBooks.length === 0) {
                this.showEmptyState();
                return;
            }
            
            // Hide empty state
            this.hideEmptyState();
            
            // Render books
            this.filteredBooks.forEach(book => {
                if (this.bookCard) {
                    const bookElement = this.bookCard.render(book, this.currentView);
                    this.elements.booksGrid.appendChild(bookElement);
                }
            });
        }

        /**
         * Show empty state
         */
        showEmptyState() {
            if (this.elements.emptyState) {
                this.elements.emptyState.style.display = 'block';
                
                // Update empty state message based on whether filters are active
                const hasFilters = this.searchFilter && 
                    (this.searchFilter.getSearchTerm() || Object.keys(this.searchFilter.getFilters()).length > 0);
                
                const emptyMessage = this.elements.emptyState.querySelector('.empty-message');
                if (emptyMessage) {
                    if (hasFilters) {
                        emptyMessage.textContent = 'No books match your current filters.';
                    } else {
                        emptyMessage.textContent = 'Your library is empty. Add some books to get started!';
                    }
                }
            }
        }

        /**
         * Hide empty state
         */
        hideEmptyState() {
            if (this.elements.emptyState) {
                this.elements.emptyState.style.display = 'none';
            }
        }

        /**
         * Update library statistics
         */
        updateStats() {
            if (!this.elements.statsContainer) return;
            
            const stats = {
                total: this.totalBooks,
                read: this.books.filter(book => book.status === 'READ').length,
                reading: this.books.filter(book => book.status === 'READING').length,
                unread: this.books.filter(book => book.status === 'UNREAD').length
            };
            
            // Update stats display
            const totalStat = this.elements.statsContainer.querySelector('.stat-total');
            const readStat = this.elements.statsContainer.querySelector('.stat-read');
            const readingStat = this.elements.statsContainer.querySelector('.stat-reading');
            const unreadStat = this.elements.statsContainer.querySelector('.stat-unread');
            
            if (totalStat) totalStat.textContent = stats.total;
            if (readStat) readStat.textContent = stats.read;
            if (readingStat) readingStat.textContent = stats.reading;
            if (unreadStat) unreadStat.textContent = stats.unread;
        }

        /**
         * Update pagination controls
         */
        updatePagination() {
            if (!this.elements.paginationContainer) return;
            
            // Show/hide pagination based on whether we have multiple pages
            if (this.totalPages <= 1) {
                this.elements.paginationContainer.style.display = 'none';
                return;
            }
            
            this.elements.paginationContainer.style.display = 'flex';
            
            // Update previous button
            if (this.paginationElements.prevButton) {
                this.paginationElements.prevButton.disabled = this.currentPage <= 1;
            }
            
            // Update next button
            if (this.paginationElements.nextButton) {
                this.paginationElements.nextButton.disabled = this.currentPage >= this.totalPages;
            }
            
            // Update page info
            if (this.paginationElements.pageInfo) {
                const startItem = ((this.currentPage - 1) * this.booksPerPage) + 1;
                const endItem = Math.min(this.currentPage * this.booksPerPage, this.totalBooks);
                this.paginationElements.pageInfo.textContent = 
                    `${startItem}-${endItem} of ${this.totalBooks} books`;
            }
            
            // Update page buttons
            this.updatePageButtons();
            
            // Update jump to page input
            if (this.paginationElements.jumpToPage) {
                this.paginationElements.jumpToPage.value = this.currentPage;
                this.paginationElements.jumpToPage.max = this.totalPages;
            }
        }

        /**
         * Update page buttons
         */
        updatePageButtons() {
            if (!this.paginationElements.pageButtons) return;
            
            this.paginationElements.pageButtons.innerHTML = '';
            
            // Calculate visible page range
            const maxVisible = 5;
            let startPage = Math.max(1, this.currentPage - Math.floor(maxVisible / 2));
            let endPage = Math.min(this.totalPages, startPage + maxVisible - 1);
            
            // Adjust start if we're near the end
            if (endPage - startPage < maxVisible - 1) {
                startPage = Math.max(1, endPage - maxVisible + 1);
            }
            
            // First page button
            if (startPage > 1) {
                this.createPageButton(1, '1');
                if (startPage > 2) {
                    this.createPageButton(null, '...');
                }
            }
            
            // Visible page buttons
            for (let page = startPage; page <= endPage; page++) {
                this.createPageButton(page, page.toString());
            }
            
            // Last page button
            if (endPage < this.totalPages) {
                if (endPage < this.totalPages - 1) {
                    this.createPageButton(null, '...');
                }
                this.createPageButton(this.totalPages, this.totalPages.toString());
            }
        }

        /**
         * Create a page button
         */
        createPageButton(page, text) {
            const button = document.createElement('button');
            button.className = 'page-button';
            button.textContent = text;
            
            if (page === null) {
                button.disabled = true;
                button.className += ' page-ellipsis';
            } else if (page === this.currentPage) {
                button.className += ' active';
                button.setAttribute('aria-current', 'page');
            } else {
                button.addEventListener('click', () => {
                    this.handlePageChange(page);
                });
            }
            
            this.paginationElements.pageButtons.appendChild(button);
        }

        /**
         * Set loading state
         */
        setLoading(loading) {
            this.isLoading = loading;
            
            if (this.elements.loadingSpinner) {
                this.elements.loadingSpinner.style.display = loading ? 'flex' : 'none';
            }
            
            // Disable controls during loading
            if (this.elements.addBookButton) {
                this.elements.addBookButton.disabled = loading;
            }
        }

        /**
         * Show error message
         */
        showError(message) {
            this.error = message;
            
            if (this.elements.errorContainer) {
                this.elements.errorContainer.style.display = 'block';
                const errorMessage = this.elements.errorContainer.querySelector('.error-message');
                if (errorMessage) {
                    errorMessage.textContent = message;
                }
            }
        }

        /**
         * Clear error message
         */
        clearError() {
            this.error = null;
            
            if (this.elements.errorContainer) {
                this.elements.errorContainer.style.display = 'none';
            }
        }

        /**
         * Show temporary message
         */
        showMessage(message, type = 'info') {
            // Create message element if it doesn't exist
            let messageElement = document.getElementById('temp-message');
            if (!messageElement) {
                messageElement = document.createElement('div');
                messageElement.id = 'temp-message';
                messageElement.className = 'temp-message';
                document.body.appendChild(messageElement);
            }
            
            messageElement.className = `temp-message temp-message-${type}`;
            messageElement.textContent = message;
            messageElement.style.display = 'block';
            
            // Auto-hide after 3 seconds
            setTimeout(() => {
                messageElement.style.display = 'none';
            }, 3000);
        }

        /**
         * Get current state for debugging
         */
        getState() {
            return {
                initialized: this.initialized,
                isLoading: this.isLoading,
                error: this.error,
                booksCount: this.books.length,
                filteredBooksCount: this.filteredBooks.length,
                totalBooks: this.totalBooks,
                currentPage: this.currentPage,
                totalPages: this.totalPages,
                currentView: this.currentView
            };
        }
    }

    // Initialize when DOM is ready
    document.addEventListener('DOMContentLoaded', async () => {
        // Check if we're on the library page
        if (document.getElementById('library-page')) {
            try {
                window.libraryPageController = new LibraryPageController();
                await window.libraryPageController.init();
            } catch (error) {
                console.error('Failed to initialize library page:', error);
            }
        }
    });

    // Export for testing and external access
    window.LibraryPageController = LibraryPageController;

})();