import { BaseService } from '../utils/baseService.js';

/**
 * Book Search Service for The Home Archive
 * Handles book search and discovery API communication with backend
 * Compliant with Spring Boot 3.x REST API and authentication patterns
 */
class BookSearchService extends BaseService {
    constructor() {
        super('');
        this.apiBaseURL = '/api/books';
        this.searchAPIURL = '/api/search';
        this.categoriesAPIURL = '/api/categories';
    }

    /**
     * Search the complete book database with advanced filtering
     * @param {Object} searchParams - Search parameters
     * @returns {Promise<Object>} Search results with pagination
     */
    async searchBooks(searchParams = {}) {
        try {
            const params = new URLSearchParams();
            
            // Add search parameters
            Object.keys(searchParams).forEach(key => {
                if (searchParams[key] !== null && searchParams[key] !== undefined && searchParams[key] !== '') {
                    params.append(key, searchParams[key]);
                }
            });

            const url = `${this.searchAPIURL}?${params.toString()}`;
            const response = await this.makeRequest(url);
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Failed to search books:', error);
            throw error;
        }
    }

    /**
     * Get book details by ID
     * @param {string} bookId - Book ID
     * @returns {Promise<Object>} Book details
     */
    async getBookDetails(bookId) {
        try {
            const response = await this.makeRequest(`${this.apiBaseURL}/${bookId}`);
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Failed to get book details:', error);
            throw error;
        }
    }

    /**
     * Get books by category with pagination
     * @param {string} category - Category name
     * @param {Object} options - Pagination options
     * @returns {Promise<Object>} Books in category
     */
    async getBooksByCategory(category, options = {}) {
        try {
            const params = new URLSearchParams();
            params.append('category', category);
            
            if (options.page !== undefined) params.append('page', options.page);
            if (options.size !== undefined) params.append('size', options.size);
            if (options.sort) params.append('sort', options.sort);
            if (options.direction) params.append('direction', options.direction);

            const url = `${this.categoriesAPIURL}?${params.toString()}`;
            const response = await this.makeRequest(url);
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Failed to get books by category:', error);
            throw error;
        }
    }

    /**
     * Get search suggestions
     * @param {string} query - Partial query
     * @returns {Promise<Array>} Search suggestions
     */
    async getSearchSuggestions(query) {
        try {
            const cacheKey = `suggestions_${query}`;
            const cached = this.getCache(cacheKey);
            if (cached) return cached;

            const params = new URLSearchParams();
            params.append('q', query);
            params.append('limit', '10');

            const url = `${this.searchAPIURL}/suggestions?${params.toString()}`;
            const response = await this.makeRequest(url);
            const suggestions = await this.handleResponse(response);
            
            this.setCache(cacheKey, suggestions);
            return suggestions;
        } catch (error) {
            console.error('Failed to get search suggestions:', error);
            return []; // Return empty array on error
        }
    }

    /**
     * Get popular searches
     * @returns {Promise<Array>} Popular search terms
     */
    async getPopularSearches() {
        try {
            const cacheKey = 'popular_searches';
            const cached = this.getCache(cacheKey);
            if (cached) return cached;

            const response = await this.makeRequest(`${this.searchAPIURL}/popular`);
            const popular = await this.handleResponse(response);
            
            this.setCache(cacheKey, popular);
            return popular;
        } catch (error) {
            console.error('Failed to get popular searches:', error);
            return [];
        }
    }

    /**
     * Get all available categories
     * @returns {Promise<Array>} Category list
     */
    async getCategories() {
        try {
            const cacheKey = 'categories';
            const cached = this.getCache(cacheKey);
            if (cached) return cached;

            const response = await this.makeRequest(`${this.categoriesAPIURL}/list`);
            const categories = await this.handleResponse(response);
            
            this.setCache(cacheKey, categories);
            return categories;
        } catch (error) {
            console.error('Failed to get categories:', error);
            return [];
        }
    }
}

export const bookSearchService = new BookSearchService();
export default bookSearchService;
