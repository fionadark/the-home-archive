/**
 * BookService - Handles all book-related API operations
 * Part of Phase 5 User Story 3 implementation
 */

export class BookService {
    constructor() {
        this.baseUrl = '/api/books';
    }

    /**
     * Search for books with various filters
     * @param {Object} searchParams - Search parameters
     * @returns {Promise} Search results
     */
    async searchBooks(searchParams) {
        const params = new URLSearchParams();
        
        Object.keys(searchParams).forEach(key => {
            if (searchParams[key] !== null && searchParams[key] !== undefined && searchParams[key] !== '') {
                params.append(key, searchParams[key]);
            }
        });

        const response = await fetch(`${this.baseUrl}/search?${params.toString()}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${this.getAuthToken()}`
            }
        });

        if (!response.ok) {
            throw new Error(`Search failed: ${response.statusText}`);
        }

        return response.json();
    }

    /**
     * Validate book by ISBN
     * @param {string} isbn - ISBN to validate
     * @returns {Promise} Validation results
     */
    async validateBookByIsbn(isbn) {
        const response = await fetch(`/api/v1/books/validate?isbn=${encodeURIComponent(isbn)}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${this.getAuthToken()}`
            }
        });

        if (!response.ok) {
            throw new Error(`ISBN validation failed: ${response.statusText}`);
        }

        return response.json();
    }

    /**
     * Create a new book
     * @param {Object} bookData - Book data
     * @returns {Promise} Created book
     */
    async createBook(bookData) {
        const response = await fetch('/api/v1/books', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${this.getAuthToken()}`
            },
            body: JSON.stringify(bookData)
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to create book');
        }

        return response.json();
    }

    /**
     * Add existing book to user library
     * @param {number} bookId - Book ID
     * @returns {Promise} Result
     */
    async addBookToLibrary(bookId) {
        const response = await fetch(`/api/library/books/${bookId}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${this.getAuthToken()}`
            },
            body: JSON.stringify({
                readingStatus: 'WANT_TO_READ',
                physicalLocation: '',
                personalNotes: ''
            })
        });

        if (!response.ok) {
            const error = await response.json();
            throw new Error(error.message || 'Failed to add book to library');
        }

        return response.json();
    }

    /**
     * Get search suggestions
     * @param {string} query - Search query
     * @param {number} limit - Maximum suggestions
     * @returns {Promise} Suggestions
     */
    async getSearchSuggestions(query, limit = 5) {
        const response = await fetch(`${this.baseUrl}/search/suggestions?q=${encodeURIComponent(query)}&limit=${limit}`, {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${this.getAuthToken()}`
            }
        });

        if (!response.ok) {
            throw new Error(`Failed to get suggestions: ${response.statusText}`);
        }

        return response.json();
    }

    /**
     * Get categories
     * @returns {Promise} Categories list
     */
    async getCategories() {
        const response = await fetch('/api/v1/books/categories', {
            method: 'GET',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${this.getAuthToken()}`
            }
        });

        if (!response.ok) {
            throw new Error(`Failed to get categories: ${response.statusText}`);
        }

        return response.json();
    }

    /**
     * Get authentication token
     * @returns {string} Auth token
     */
    getAuthToken() {
        return localStorage.getItem('authToken') || '';
    }
}