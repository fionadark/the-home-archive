// Add Book Page JavaScript
class AddBookPage {
    constructor() {
        this.currentTab = 'isbn';
        this.tabButtons = document.querySelectorAll('.tab-btn');
        this.tabContents = document.querySelectorAll('.tab-content');
        
        // Forms
        this.isbnForm = document.getElementById('isbn-form');
        this.searchForm = document.getElementById('add-search-form');
        this.manualForm = document.getElementById('manual-form');
        
        // Results containers
        this.isbnResults = document.getElementById('isbn-results');
        this.searchResults = document.getElementById('add-search-results');
        this.recentBooks = document.getElementById('recent-books-container');
        
        this.init();
    }

    init() {
        this.bindEvents();
        this.loadRecentBooks();
        this.setupUserMenu();
    }

    bindEvents() {
        // Tab switching
        this.tabButtons.forEach(btn => {
            btn.addEventListener('click', (e) => {
                this.switchTab(e.target.getAttribute('data-tab'));
            });
        });

        // ISBN form
        if (this.isbnForm) {
            this.isbnForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.fetchByISBN();
            });
        }

        // Search form
        if (this.searchForm) {
            this.searchForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.searchBooks();
            });
        }

        // Manual form
        if (this.manualForm) {
            this.manualForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.addManualBook();
            });
        }

        // Clear manual form
        const clearBtn = document.getElementById('clear-manual-form');
        if (clearBtn) {
            clearBtn.addEventListener('click', () => {
                this.clearManualForm();
            });
        }
    }

    setupUserMenu() {
        // Use existing auth service to set up user menu
        if (window.authService) {
            window.authService.updateUserDisplay();
        }
    }

    switchTab(tabName) {
        // Update buttons
        this.tabButtons.forEach(btn => {
            btn.classList.remove('active');
            if (btn.getAttribute('data-tab') === tabName) {
                btn.classList.add('active');
            }
        });

        // Update content
        this.tabContents.forEach(content => {
            content.classList.remove('active');
            if (content.id === `content-${tabName}`) {
                content.classList.add('active');
            }
        });

        this.currentTab = tabName;
    }

    async fetchByISBN() {
        const isbnInput = document.getElementById('isbn-input');
        const isbn = isbnInput.value.trim().replace(/[^0-9X]/g, '');
        
        if (!isbn || (isbn.length !== 10 && isbn.length !== 13)) {
            this.showToast('Please enter a valid 10 or 13 digit ISBN', 'warning');
            return;
        }

        this.showLoading(true);

        try {
            const response = await fetch(`/api/v1/search/books?isbn=${isbn}`);
            
            if (response.ok) {
                const data = await response.json();
                if (data.data && data.data.length > 0) {
                    this.displayISBNResult(data.data[0]);
                } else {
                    this.showToast('No book found with that ISBN', 'warning');
                    this.isbnResults.style.display = 'none';
                }
            } else {
                throw new Error('ISBN search failed');
            }
        } catch (error) {
            console.error('ISBN fetch error:', error);
            this.showToast('Failed to fetch book details. Please try again.', 'error');
            this.isbnResults.style.display = 'none';
        } finally {
            this.showLoading(false);
        }
    }

    displayISBNResult(book) {
        this.isbnResults.style.display = 'block';
        this.isbnResults.innerHTML = `
            <div class="book-preview-content">
                <img src="${book.coverImageUrl || '/images/default-book-cover.jpg'}" 
                     alt="${book.title} cover" class="book-cover"
                     onerror="this.src='/images/default-book-cover.jpg'">
                <div class="book-details">
                    <h4>${this.escapeHtml(book.title)}</h4>
                    <p><strong>Author:</strong> ${this.escapeHtml(book.author || 'Unknown')}</p>
                    <p><strong>Published:</strong> ${book.publishedYear || 'Unknown'}</p>
                    ${book.genre ? `<p><strong>Genre:</strong> ${this.escapeHtml(book.genre)}</p>` : ''}
                    ${book.isbn ? `<p><strong>ISBN:</strong> ${book.isbn}</p>` : ''}
                    ${book.description ? `<p class="description">${this.escapeHtml(book.description)}</p>` : ''}
                    <div class="book-actions" style="margin-top: 1rem;">
                        <button class="btn btn-primary" onclick="addBookPage.addToLibrary(${JSON.stringify(book).replace(/"/g, '&quot;')})">
                            Add to My Library
                        </button>
                    </div>
                </div>
            </div>
        `;
    }

    async searchBooks() {
        const searchInput = document.getElementById('add-search-input');
        const query = searchInput.value.trim();
        
        if (!query) {
            this.showToast('Please enter a search term', 'warning');
            return;
        }

        this.showLoading(true);

        try {
            const response = await fetch(`/api/v1/search/books?q=${encodeURIComponent(query)}&limit=12`);
            
            if (response.ok) {
                const data = await response.json();
                this.displaySearchResults(data.data || []);
            } else {
                throw new Error('Search failed');
            }
        } catch (error) {
            console.error('Search error:', error);
            this.showToast('Search failed. Please try again.', 'error');
        } finally {
            this.showLoading(false);
        }
    }

    displaySearchResults(books) {
        this.searchResults.style.display = 'block';
        
        if (books.length === 0) {
            this.searchResults.innerHTML = `
                <div class="empty-state">
                    <p>No books found. Try different search terms.</p>
                </div>
            `;
            return;
        }

        this.searchResults.innerHTML = `
            <h4>Search Results (${books.length} found)</h4>
            <div class="search-results-grid">
                ${books.map(book => `
                    <div class="search-result-card">
                        <img src="${book.coverImageUrl || '/images/default-book-cover.jpg'}" 
                             alt="${book.title} cover" class="result-cover"
                             onerror="this.src='/images/default-book-cover.jpg'">
                        <div class="result-info">
                            <h5>${this.escapeHtml(book.title)}</h5>
                            <p>${this.escapeHtml(book.author || 'Unknown Author')}</p>
                            <button class="btn btn-primary btn-small" 
                                    onclick="addBookPage.addToLibrary(${JSON.stringify(book).replace(/"/g, '&quot;')})">
                                Add to Library
                            </button>
                        </div>
                    </div>
                `).join('')}
            </div>
        `;
    }

    async addManualBook() {
        const formData = new FormData(this.manualForm);
        const bookData = {
            title: formData.get('title') || document.getElementById('manual-title').value,
            author: formData.get('author') || document.getElementById('manual-author').value,
            isbn: formData.get('isbn') || document.getElementById('manual-isbn').value,
            genre: formData.get('genre') || document.getElementById('manual-genre').value,
            publishedYear: formData.get('publishedYear') || document.getElementById('manual-year').value,
            publisher: formData.get('publisher') || document.getElementById('manual-publisher').value,
            description: formData.get('description') || document.getElementById('manual-description').value,
            physicalLocation: formData.get('physicalLocation') || document.getElementById('manual-location').value,
            rating: formData.get('rating') || document.getElementById('manual-rating').value
        };

        // Validate required fields
        if (!bookData.title || !bookData.author) {
            this.showToast('Title and Author are required fields', 'warning');
            return;
        }

        this.showLoading(true);

        try {
            const response = await fetch('/api/v1/books', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                },
                body: JSON.stringify(bookData)
            });

            if (response.ok) {
                const result = await response.json();
                this.showToast(`"${bookData.title}" added to your library!`, 'success');
                this.clearManualForm();
                this.loadRecentBooks(); // Refresh recent books
            } else {
                const error = await response.json();
                this.showToast(error.message || 'Failed to add book', 'error');
            }
        } catch (error) {
            console.error('Error adding manual book:', error);
            this.showToast('Failed to add book. Please try again.', 'error');
        } finally {
            this.showLoading(false);
        }
    }

    async addToLibrary(book) {
        this.showLoading(true);

        try {
            // First add the book to the global database if it doesn't exist
            let bookId = book.id;
            
            if (!bookId) {
                const createResponse = await fetch('/api/v1/books', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                        'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                    },
                    body: JSON.stringify(book)
                });

                if (createResponse.ok) {
                    const createdBook = await createResponse.json();
                    bookId = createdBook.data?.id || createdBook.id;
                }
            }

            // Then add to user's library
            const response = await fetch(`/api/v1/library/books/${bookId}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            });

            if (response.ok) {
                this.showToast(`"${book.title}" added to your library!`, 'success');
                this.loadRecentBooks(); // Refresh recent books
            } else {
                const error = await response.json();
                this.showToast(error.message || 'Failed to add book to library', 'error');
            }
        } catch (error) {
            console.error('Error adding book to library:', error);
            this.showToast('Failed to add book to library', 'error');
        } finally {
            this.showLoading(false);
        }
    }

    clearManualForm() {
        this.manualForm.reset();
        // Clear any validation states
        const inputs = this.manualForm.querySelectorAll('.form-input, .form-select, .form-textarea');
        inputs.forEach(input => {
            input.classList.remove('error');
        });
    }

    async loadRecentBooks() {
        try {
            const response = await fetch('/api/v1/library/books/recent?limit=6', {
                headers: {
                    'Authorization': `Bearer ${localStorage.getItem('authToken')}`
                }
            });

            if (response.ok) {
                const data = await response.json();
                this.displayRecentBooks(data.data || []);
            }
        } catch (error) {
            console.error('Error loading recent books:', error);
        }
    }

    displayRecentBooks(books) {
        if (books.length === 0) {
            this.recentBooks.innerHTML = '<p>No books in your library yet.</p>';
            return;
        }

        this.recentBooks.innerHTML = books.map(book => `
            <div class="book-card">
                <img src="${book.coverImageUrl || '/images/default-book-cover.jpg'}" 
                     alt="${book.title} cover" class="book-cover"
                     onerror="this.src='/images/default-book-cover.jpg'">
                <div class="book-info">
                    <h5>${this.escapeHtml(book.title)}</h5>
                    <p>${this.escapeHtml(book.author || 'Unknown Author')}</p>
                    ${book.physicalLocation ? `<p class="location">📍 ${this.escapeHtml(book.physicalLocation)}</p>` : ''}
                </div>
            </div>
        `).join('');
    }

    showLoading(show) {
        const overlay = document.getElementById('loading-overlay');
        if (overlay) {
            overlay.style.display = show ? 'flex' : 'none';
        }
    }

    showToast(message, type = 'info') {
        const container = document.getElementById('toast-container');
        if (!container) return;

        const toast = document.createElement('div');
        toast.className = `toast ${type}`;
        toast.innerHTML = `
            <span>${message}</span>
            <button onclick="this.parentElement.remove()" style="margin-left: 1rem; background: none; border: none; color: inherit; cursor: pointer;">×</button>
        `;

        container.appendChild(toast);

        // Auto remove after 5 seconds
        setTimeout(() => {
            if (toast.parentNode) {
                container.removeChild(toast);
            }
        }, 5000);
    }

    escapeHtml(text) {
        if (!text) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    }
}

// Global instance for onclick handlers
let addBookPage;

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', () => {
    addBookPage = new AddBookPage();
});