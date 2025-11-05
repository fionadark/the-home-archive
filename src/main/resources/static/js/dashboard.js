/**
 * Dashboard Page JavaScript
 * Handles user dashboard, library management, and authentication state
 */

document.addEventListener('DOMContentLoaded', function() {
    // Check authentication on page load
    if (!authService.isAuthenticated()) {
        window.location.href = '/login.html';
        return;
    }

    // Get DOM elements
    const userNameElement = document.getElementById('user-name');
    const userMenuButton = document.getElementById('user-menu-button');
    const userDropdown = document.getElementById('user-dropdown');
    const logoutBtn = document.getElementById('logout-btn');

    // Library elements
    const librarySearch = document.getElementById('library-search');
    const searchBtn = document.querySelector('.search-btn');
    const viewGridBtn = document.getElementById('view-grid');
    const viewListBtn = document.getElementById('view-list');
    const genreFilter = document.getElementById('genre-filter');
    const locationFilter = document.getElementById('location-filter');
    const clearFiltersBtn = document.getElementById('clear-filters');
    const sortBySelect = document.getElementById('sort-by');
    const sortOrderBtn = document.getElementById('sort-order');

    // Content elements
    const loadingState = document.getElementById('loading-state');
    const emptyState = document.getElementById('empty-state');
    const noResultsState = document.getElementById('no-results-state');
    const booksContainer = document.getElementById('books-container');
    const clearSearchBtn = document.getElementById('clear-search');

    // Statistics elements
    const totalBooksElement = document.getElementById('total-books');
    const uniqueAuthorsElement = document.getElementById('unique-authors');
    const uniqueGenresElement = document.getElementById('unique-genres');
    const avgRatingElement = document.getElementById('avg-rating');

    // Pagination elements
    const paginationSection = document.getElementById('pagination-section');
    const prevPageBtn = document.getElementById('prev-page');
    const nextPageBtn = document.getElementById('next-page');
    const pageInfo = document.getElementById('page-info');

    // Modal elements
    const bookModal = document.getElementById('book-modal');
    const modalOverlay = document.getElementById('modal-overlay');
    const modalClose = document.getElementById('modal-close');
    const modalBody = document.getElementById('modal-body');

    // State management
    let currentUser = null;
    let currentBooks = [];
    let filteredBooks = [];
    let currentView = 'grid';
    let currentPage = 1;
    const booksPerPage = 12;
    let currentSort = 'title';
    let currentSortOrder = 'asc';

    /**
     * Initialize user interface
     */
    async function initializeUser() {
        try {
            // Get current user from storage or fetch from API
            currentUser = authService.getCurrentUser();
            
            if (!currentUser) {
                const result = await authService.getUserProfile();
                if (result.success) {
                    currentUser = result.user;
                } else {
                    console.error('Failed to fetch user profile:', result.message);
                    // Fallback to basic user info from token
                    currentUser = { firstName: 'User', lastName: '' };
                }
            }

            // Update UI with user information
            if (currentUser) {
                const displayName = `${currentUser.firstName} ${currentUser.lastName}`.trim();
                userNameElement.textContent = displayName || 'User';
            }

        } catch (error) {
            console.error('User initialization error:', error);
            userNameElement.textContent = 'User';
        }
    }

    /**
     * Setup user menu interactions
     */
    function setupUserMenu() {
        userMenuButton.addEventListener('click', function() {
            const isExpanded = this.getAttribute('aria-expanded') === 'true';
            
            this.setAttribute('aria-expanded', !isExpanded);
            userDropdown.style.display = isExpanded ? 'none' : 'block';
        });

        // Close dropdown when clicking outside
        document.addEventListener('click', function(event) {
            if (!userMenuButton.contains(event.target) && !userDropdown.contains(event.target)) {
                userMenuButton.setAttribute('aria-expanded', 'false');
                userDropdown.style.display = 'none';
            }
        });

        // Handle logout
        logoutBtn.addEventListener('click', async function() {
            try {
                await authService.logout();
                window.location.href = '/login.html';
            } catch (error) {
                console.error('Logout error:', error);
                // Force logout locally even if server call fails
                authService.clearTokens();
                authService.clearUser();
                window.location.href = '/login.html';
            }
        });
    }

    /**
     * Load user's book library (mock data for now)
     */
    async function loadLibrary() {
        showLoadingState();

        try {
            // TODO: Replace with actual API call when book endpoints are implemented
            // For now, using mock data to demonstrate functionality
            await new Promise(resolve => setTimeout(resolve, 1000)); // Simulate API delay
            
            currentBooks = generateMockBooks();
            filteredBooks = [...currentBooks];
            
            updateStatistics();
            updateFilters();
            renderBooks();
            
        } catch (error) {
            console.error('Failed to load library:', error);
            showErrorState('Failed to load your library. Please try again.');
        }
    }

    /**
     * Generate mock book data for demonstration
     * TODO: Remove when backend book API is implemented
     */
    function generateMockBooks() {
        return [
            {
                id: 1,
                title: "The Secret History",
                author: "Donna Tartt",
                genre: "Literary Fiction",
                physicalLocation: "Living Room Shelf A",
                publicationYear: 1992,
                rating: 4.5,
                dateAdded: "2025-01-15",
                description: "A dark academic thriller about a group of classics students."
            },
            {
                id: 2,
                title: "If We Were Villains",
                author: "M.L. Rio",
                genre: "Mystery",
                physicalLocation: "Bedroom Shelf B",
                publicationYear: 2017,
                rating: 4.2,
                dateAdded: "2025-02-01",
                description: "A dark academia novel set in an elite arts college."
            },
            {
                id: 3,
                title: "The Name of the Rose",
                author: "Umberto Eco",
                genre: "Historical Fiction",
                physicalLocation: "Study Shelf A",
                publicationYear: 1980,
                rating: 4.8,
                dateAdded: "2024-12-10",
                description: "A medieval mystery set in an Italian monastery."
            },
            {
                id: 4,
                title: "The Goldfinch",
                author: "Donna Tartt",
                genre: "Literary Fiction",
                physicalLocation: "Living Room Shelf B",
                publicationYear: 2013,
                rating: 4.1,
                dateAdded: "2025-01-20",
                description: "A young boy's journey after a museum bombing."
            },
            {
                id: 5,
                title: "Ninth House",
                author: "Leigh Bardugo",
                genre: "Dark Fantasy",
                physicalLocation: "Bedroom Shelf A",
                publicationYear: 2019,
                rating: 4.3,
                dateAdded: "2025-02-05",
                description: "Occult activities at Yale University."
            }
        ];
    }

    /**
     * Update library statistics
     */
    function updateStatistics() {
        const books = filteredBooks;
        const authors = new Set(books.map(book => book.author));
        const genres = new Set(books.map(book => book.genre));
        const ratings = books.map(book => book.rating).filter(r => r > 0);
        const avgRating = ratings.length > 0 ? 
            (ratings.reduce((sum, rating) => sum + rating, 0) / ratings.length).toFixed(1) : 
            '0.0';

        totalBooksElement.textContent = books.length;
        uniqueAuthorsElement.textContent = authors.size;
        uniqueGenresElement.textContent = genres.size;
        avgRatingElement.textContent = avgRating;
    }

    /**
     * Update filter dropdowns with available options
     */
    function updateFilters() {
        const genres = [...new Set(currentBooks.map(book => book.genre))].sort();
        const locations = [...new Set(currentBooks.map(book => book.physicalLocation))].sort();

        // Update genre filter
        genreFilter.innerHTML = '<option value="">All Genres</option>';
        genres.forEach(genre => {
            const option = document.createElement('option');
            option.value = genre;
            option.textContent = genre;
            genreFilter.appendChild(option);
        });

        // Update location filter
        locationFilter.innerHTML = '<option value="">All Locations</option>';
        locations.forEach(location => {
            const option = document.createElement('option');
            option.value = location;
            option.textContent = location;
            locationFilter.appendChild(option);
        });
    }

    /**
     * Apply filters and search to books
     */
    function applyFilters() {
        const searchTerm = librarySearch.value.toLowerCase().trim();
        const selectedGenre = genreFilter.value;
        const selectedLocation = locationFilter.value;

        filteredBooks = currentBooks.filter(book => {
            const matchesSearch = !searchTerm || 
                book.title.toLowerCase().includes(searchTerm) ||
                book.author.toLowerCase().includes(searchTerm) ||
                book.physicalLocation.toLowerCase().includes(searchTerm) ||
                book.genre.toLowerCase().includes(searchTerm);

            const matchesGenre = !selectedGenre || book.genre === selectedGenre;
            const matchesLocation = !selectedLocation || book.physicalLocation === selectedLocation;

            return matchesSearch && matchesGenre && matchesLocation;
        });

        applySorting();
        updateStatistics();
        currentPage = 1; // Reset to first page
        renderBooks();
    }

    /**
     * Apply sorting to filtered books
     */
    function applySorting() {
        filteredBooks.sort((a, b) => {
            let valueA = a[currentSort];
            let valueB = b[currentSort];

            // Handle different data types
            if (typeof valueA === 'string') {
                valueA = valueA.toLowerCase();
                valueB = valueB.toLowerCase();
            } else if (valueA instanceof Date) {
                valueA = valueA.getTime();
                valueB = valueB.getTime();
            }

            let comparison = 0;
            if (valueA < valueB) comparison = -1;
            if (valueA > valueB) comparison = 1;

            return currentSortOrder === 'asc' ? comparison : -comparison;
        });
    }

    /**
     * Render books in current view
     */
    function renderBooks() {
        hideAllStates();

        if (filteredBooks.length === 0) {
            if (librarySearch.value.trim() || genreFilter.value || locationFilter.value) {
                showNoResultsState();
            } else {
                showEmptyState();
            }
            return;
        }

        // Calculate pagination
        const totalPages = Math.ceil(filteredBooks.length / booksPerPage);
        const startIndex = (currentPage - 1) * booksPerPage;
        const endIndex = startIndex + booksPerPage;
        const booksToShow = filteredBooks.slice(startIndex, endIndex);

        // Update container class based on view
        booksContainer.className = currentView === 'grid' ? 'books-grid' : 'books-list';
        booksContainer.style.display = 'block';

        // Render books
        booksContainer.innerHTML = booksToShow.map(book => createBookCard(book)).join('');

        // Setup book interactions
        setupBookInteractions();

        // Update pagination
        updatePagination(totalPages);
    }

    /**
     * Create HTML for book card
     */
    function createBookCard(book) {
        const listViewClass = currentView === 'list' ? 'list-view' : '';
        
        return `
            <div class="book-card ${listViewClass}" data-book-id="${book.id}">
                <h3 class="book-title">${escapeHtml(book.title)}</h3>
                <div class="book-author">by ${escapeHtml(book.author)}</div>
                <div class="book-details">
                    <div class="book-detail">
                        <span class="book-detail-label">Genre:</span>
                        <span class="book-detail-value">${escapeHtml(book.genre)}</span>
                    </div>
                    <div class="book-detail">
                        <span class="book-detail-label">Location:</span>
                        <span class="book-detail-value">${escapeHtml(book.physicalLocation)}</span>
                    </div>
                    <div class="book-detail">
                        <span class="book-detail-label">Published:</span>
                        <span class="book-detail-value">${book.publicationYear}</span>
                    </div>
                    <div class="book-detail">
                        <span class="book-detail-label">Rating:</span>
                        <span class="book-detail-value">${book.rating}/5</span>
                    </div>
                </div>
                <div class="book-actions">
                    <button class="book-action-btn edit-location" data-book-id="${book.id}">
                        Edit Location
                    </button>
                    <button class="book-action-btn danger delete-book" data-book-id="${book.id}">
                        Delete
                    </button>
                </div>
            </div>
        `;
    }

    /**
     * Setup book card interactions
     */
    function setupBookInteractions() {
        // Edit location buttons
        document.querySelectorAll('.edit-location').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const bookId = parseInt(btn.dataset.bookId);
                openEditLocationModal(bookId);
            });
        });

        // Delete buttons
        document.querySelectorAll('.delete-book').forEach(btn => {
            btn.addEventListener('click', (e) => {
                e.stopPropagation();
                const bookId = parseInt(btn.dataset.bookId);
                openDeleteConfirmModal(bookId);
            });
        });

        // Book card clicks (for future book details)
        document.querySelectorAll('.book-card').forEach(card => {
            card.addEventListener('click', () => {
                const bookId = parseInt(card.dataset.bookId);
                openBookDetailsModal(bookId);
            });
        });
    }

    /**
     * Open edit location modal
     */
    function openEditLocationModal(bookId) {
        const book = currentBooks.find(b => b.id === bookId);
        if (!book) return;

        modalBody.innerHTML = `
            <h4>Edit Location for "${escapeHtml(book.title)}"</h4>
            <form id="edit-location-form">
                <div class="form-group">
                    <label for="new-location" class="form-label">Physical Location</label>
                    <input type="text" id="new-location" class="form-input" value="${escapeHtml(book.physicalLocation)}" required>
                </div>
                <div class="form-actions" style="display: flex; gap: 1rem; margin-top: 1rem;">
                    <button type="submit" class="btn btn-primary">Update Location</button>
                    <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>
                </div>
            </form>
        `;

        showModal();

        // Handle form submission
        document.getElementById('edit-location-form').addEventListener('submit', (e) => {
            e.preventDefault();
            const newLocation = document.getElementById('new-location').value.trim();
            updateBookLocation(bookId, newLocation);
        });
    }

    /**
     * Open delete confirmation modal
     */
    function openDeleteConfirmModal(bookId) {
        const book = currentBooks.find(b => b.id === bookId);
        if (!book) return;

        modalBody.innerHTML = `
            <h4>Delete Book</h4>
            <p>Are you sure you want to delete "<strong>${escapeHtml(book.title)}</strong>" by ${escapeHtml(book.author)} from your library?</p>
            <p><small>This action cannot be undone.</small></p>
            <div class="form-actions" style="display: flex; gap: 1rem; margin-top: 1rem;">
                <button type="button" class="btn btn-primary danger" onclick="deleteBook(${bookId})">Delete Book</button>
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Cancel</button>
            </div>
        `;

        showModal();
    }

    /**
     * Open book details modal
     */
    function openBookDetailsModal(bookId) {
        const book = currentBooks.find(b => b.id === bookId);
        if (!book) return;

        modalBody.innerHTML = `
            <h4>${escapeHtml(book.title)}</h4>
            <p><strong>Author:</strong> ${escapeHtml(book.author)}</p>
            <p><strong>Genre:</strong> ${escapeHtml(book.genre)}</p>
            <p><strong>Published:</strong> ${book.publicationYear}</p>
            <p><strong>Location:</strong> ${escapeHtml(book.physicalLocation)}</p>
            <p><strong>Rating:</strong> ${book.rating}/5</p>
            <p><strong>Added:</strong> ${book.dateAdded}</p>
            ${book.description ? `<p><strong>Description:</strong> ${escapeHtml(book.description)}</p>` : ''}
            <div class="form-actions" style="display: flex; gap: 1rem; margin-top: 1rem;">
                <button type="button" class="btn btn-secondary" onclick="closeModal()">Close</button>
            </div>
        `;

        showModal();
    }

    /**
     * Update book location
     */
    function updateBookLocation(bookId, newLocation) {
        if (!newLocation) return;

        // TODO: Replace with actual API call
        const book = currentBooks.find(b => b.id === bookId);
        if (book) {
            book.physicalLocation = newLocation;
            updateFilters(); // Refresh filter options
            applyFilters(); // Re-render with new location
            closeModal();
            
            // Show success message
            announceToScreenReader(`Location updated to ${newLocation}`);
        }
    }

    /**
     * Delete book from library
     */
    window.deleteBook = function(bookId) {
        // TODO: Replace with actual API call
        const bookIndex = currentBooks.findIndex(b => b.id === bookId);
        if (bookIndex !== -1) {
            const deletedBook = currentBooks[bookIndex];
            currentBooks.splice(bookIndex, 1);
            updateFilters(); // Refresh filter options
            applyFilters(); // Re-render without deleted book
            closeModal();
            
            // Show success message
            announceToScreenReader(`"${deletedBook.title}" has been deleted from your library`);
        }
    };

    /**
     * Modal functions
     */
    function showModal() {
        bookModal.style.display = 'flex';
        bookModal.setAttribute('aria-hidden', 'false');
        document.body.style.overflow = 'hidden';
        
        // Focus on first focusable element in modal
        const focusableElement = bookModal.querySelector('input, button, select, textarea');
        if (focusableElement) {
            focusableElement.focus();
        }
    }

    window.closeModal = function() {
        bookModal.style.display = 'none';
        bookModal.setAttribute('aria-hidden', 'true');
        document.body.style.overflow = '';
    };

    /**
     * State management functions
     */
    function showLoadingState() {
        hideAllStates();
        loadingState.style.display = 'block';
    }

    function showEmptyState() {
        hideAllStates();
        emptyState.style.display = 'block';
    }

    function showNoResultsState() {
        hideAllStates();
        noResultsState.style.display = 'block';
    }

    function showErrorState(message) {
        hideAllStates();
        // Create error state element if it doesn't exist
        let errorState = document.getElementById('error-state');
        if (!errorState) {
            errorState = document.createElement('div');
            errorState.id = 'error-state';
            errorState.className = 'empty-state';
            errorState.innerHTML = `
                <div class="empty-icon">⚠️</div>
                <h3>Something went wrong</h3>
                <p id="error-message">${message}</p>
                <button onclick="location.reload()" class="btn btn-primary">Try Again</button>
            `;
            document.querySelector('.book-collection').appendChild(errorState);
        } else {
            document.getElementById('error-message').textContent = message;
        }
        errorState.style.display = 'block';
    }

    function hideAllStates() {
        loadingState.style.display = 'none';
        emptyState.style.display = 'none';
        noResultsState.style.display = 'none';
        booksContainer.style.display = 'none';
        paginationSection.style.display = 'none';
        
        const errorState = document.getElementById('error-state');
        if (errorState) errorState.style.display = 'none';
    }

    /**
     * Update pagination
     */
    function updatePagination(totalPages) {
        if (totalPages <= 1) {
            paginationSection.style.display = 'none';
            return;
        }

        paginationSection.style.display = 'block';
        pageInfo.textContent = `Page ${currentPage} of ${totalPages}`;
        
        prevPageBtn.disabled = currentPage === 1;
        nextPageBtn.disabled = currentPage === totalPages;
    }

    /**
     * Setup event listeners
     */
    function setupEventListeners() {
        // Search functionality
        librarySearch.addEventListener('input', debounce(applyFilters, 300));
        searchBtn.addEventListener('click', applyFilters);

        // View toggle
        viewGridBtn.addEventListener('click', () => switchView('grid'));
        viewListBtn.addEventListener('click', () => switchView('list'));

        // Filters
        genreFilter.addEventListener('change', applyFilters);
        locationFilter.addEventListener('change', applyFilters);
        clearFiltersBtn.addEventListener('click', clearAllFilters);

        // Sorting
        sortBySelect.addEventListener('change', (e) => {
            currentSort = e.target.value;
            applyFilters();
        });

        sortOrderBtn.addEventListener('click', () => {
            currentSortOrder = currentSortOrder === 'asc' ? 'desc' : 'asc';
            sortOrderBtn.classList.toggle('desc', currentSortOrder === 'desc');
            sortOrderBtn.title = `Sort ${currentSortOrder === 'asc' ? 'ascending' : 'descending'}`;
            applyFilters();
        });

        // Pagination
        prevPageBtn.addEventListener('click', () => {
            if (currentPage > 1) {
                currentPage--;
                renderBooks();
            }
        });

        nextPageBtn.addEventListener('click', () => {
            const totalPages = Math.ceil(filteredBooks.length / booksPerPage);
            if (currentPage < totalPages) {
                currentPage++;
                renderBooks();
            }
        });

        // Clear search
        clearSearchBtn.addEventListener('click', () => {
            librarySearch.value = '';
            applyFilters();
        });

        // Modal close events
        modalClose.addEventListener('click', closeModal);
        modalOverlay.addEventListener('click', closeModal);

        // Keyboard accessibility for modal
        document.addEventListener('keydown', (e) => {
            if (e.key === 'Escape' && bookModal.style.display === 'flex') {
                closeModal();
            }
        });
    }

    /**
     * Switch between grid and list view
     */
    function switchView(view) {
        currentView = view;
        
        viewGridBtn.classList.toggle('active', view === 'grid');
        viewListBtn.classList.toggle('active', view === 'list');
        
        viewGridBtn.setAttribute('aria-pressed', view === 'grid');
        viewListBtn.setAttribute('aria-pressed', view === 'list');
        
        renderBooks();
    }

    /**
     * Clear all filters
     */
    function clearAllFilters() {
        librarySearch.value = '';
        genreFilter.value = '';
        locationFilter.value = '';
        applyFilters();
    }

    /**
     * Utility functions
     */
    function escapeHtml(text) {
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }

    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    function announceToScreenReader(message) {
        const announcement = document.createElement('div');
        announcement.setAttribute('aria-live', 'polite');
        announcement.setAttribute('aria-atomic', 'true');
        announcement.className = 'sr-only';
        announcement.textContent = message;
        
        document.body.appendChild(announcement);
        
        setTimeout(() => {
            if (announcement.parentNode) {
                announcement.parentNode.removeChild(announcement);
            }
        }, 1000);
    }

    /**
     * Initialize the dashboard
     */
    async function init() {
        await initializeUser();
        setupUserMenu();
        setupEventListeners();
        await loadLibrary();

        console.log('Dashboard initialized');
    }

    // Initialize when DOM is ready
    init();
});