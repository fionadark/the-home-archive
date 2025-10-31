import { BaseService } from '../utils/baseService.js';

/**
 * BookService - Handles all book-related API operations
 * Part of Phase 5 User Story 3 implementation
 */
export class BookService extends BaseService {
    constructor() {
        super('');
        this.baseUrl = '/api/books';
    }

    /**
     * Search for books with various filters
     * @param {Object} searchParams - Search parameters
     * @returns {Promise} Search results
     */
    async searchBooks(searchParams) {
        try {
            const params = new URLSearchParams();
            
            Object.keys(searchParams).forEach(key => {
                if (searchParams[key] !== null && searchParams[key] !== undefined && searchParams[key] !== '') {
                    params.append(key, searchParams[key]);
                }
            });

            const url = `${this.baseUrl}/search?${params.toString()}`;
            const response = await this.makeRequest(url);
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Failed to search books:', error);
            throw error;
        }
    }

    /**
     * Validate book by ISBN
     * @param {string} isbn - ISBN to validate
     * @returns {Promise} Validation results
     */
    async validateBookByIsbn(isbn) {
        try {
            const url = `/api/v1/books/validate?isbn=${encodeURIComponent(isbn)}`;
            const response = await this.makeRequest(url);
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Failed to validate ISBN:', error);
            throw error;
        }
    }

    /**
     * Create a new book
     * @param {Object} bookData - Book data
     * @returns {Promise} Created book
     */
    async createBook(bookData) {
        try {
            const response = await this.makeRequest('/api/v1/books', {
                method: 'POST',
                body: bookData
            });
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Failed to create book:', error);
            throw error;
        }
    }

    /**
     * Add existing book to user library
     * @param {number} bookId - Book ID
     * @returns {Promise} Result
     */
    async addBookToLibrary(bookId) {
        try {
            const bookData = {
                readingStatus: 'WANT_TO_READ',
                physicalLocation: '',
                personalNotes: ''
            };

            const response = await this.makeRequest(`/api/library/books/${bookId}`, {
                method: 'POST',
                body: bookData
            });
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Failed to add book to library:', error);
            throw error;
        }
    }

    /**
     * Get search suggestions
     * @param {string} query - Search query
     * @param {number} limit - Maximum suggestions
     * @returns {Promise} Suggestions
     */
    async getSearchSuggestions(query, limit = 5) {
        try {
            const url = `${this.baseUrl}/search/suggestions?q=${encodeURIComponent(query)}&limit=${limit}`;
            const response = await this.makeRequest(url);
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Failed to get suggestions:', error);
            return []; // Return empty array on error
        }
    }

    /**
     * Get categories
     * @returns {Promise} Categories list
     */
    async getCategories() {
        try {
            const response = await this.makeRequest('/api/v1/books/categories');
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Failed to get categories:', error);
            return [];
        }
    }
}

export const bookService = new BookService();
export default bookService;
