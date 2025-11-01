/**
 * Book Search Service for The Home Archive
 * Handles book search and discovery API communication with backend
 * Compliant with Spring Boot 3.x REST API and authentication patterns
 * 
 * This service provides methods to:
 * - Search the complete book database with advanced filtering
 * - Get book details including ratings and metadata
 * - Retrieve search suggestions and popular searches
 * - Get books by category with pagination
 * - Handle external API integration (Google Books, OpenLibrary)
 * - Manage search history and suggestions
 */

class BookSearchService {
    constructor() {
        this.baseURL = ''; // Spring Boot serves from same origin
        this.apiBaseURL = '/api/books';
        this.searchAPIURL = '/api/search';
        this.categoriesAPIURL = '/api/categories';
        
        // Reference to auth service for token management
        this.authService = typeof authService !== 'undefined' ? authService : null;
        
        // Initialize CSRF token if available
        this.csrfToken = this.getCSRFToken();
        
        // Cache for frequently accessed data
        this.cache = new Map();
        this.cacheTimeout = 5 * 60 * 1000; // 5 minutes
    }

    /**
     * Get CSRF token from meta tag (Spring Security requirement)
     */
    getCSRFToken() {
        const csrfMeta = document.querySelector('meta[name="_csrf"]');
        const csrfHeaderMeta = document.querySelector('meta[name="_csrf_header"]');
        
        if (csrfMeta && csrfHeaderMeta) {
            return {
                token: csrfMeta.getAttribute('content'),
                header: csrfHeaderMeta.getAttribute('content')
            };
        }
        return null;
    }

    /**
     * Make authenticated API request with proper headers
     */
    async makeRequest(url, options = {}) {
        const token = this.authService ? this.authService.getAccessToken() : localStorage.getItem('homeArchive_accessToken');
        
        const headers = {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
            ...options.headers
        };

        // Add CSRF token if available
        if (this.csrfToken) {
            headers[this.csrfToken.header] = this.csrfToken.token;
        }

        // Add JWT token if available
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        const config = {
            method: options.method || 'GET',
            headers,
            credentials: 'same-origin', // Important for CORS
            ...options
        };

        if (config.method !== 'GET' && options.body) {
            config.body = typeof options.body === 'string' ? options.body : JSON.stringify(options.body);
        }

        try {
            const response = await fetch(url, config);
            
            // Handle token refresh if 401 Unauthorized
            if (response.status === 401 && token && this.authService) {
                const refreshed = await this.authService.refreshAccessToken();
                if (refreshed) {
                    // Retry original request with new token
                    headers['Authorization'] = `Bearer ${this.authService.getAccessToken()}`;
                    config.headers = headers;
                    return await fetch(url, config);
                } else {
                    // Refresh failed, redirect to login
                    this.authService.logout();
                    window.location.href = '/login.html';
                    throw new Error('Session expired. Please login again.');
                }
            }

            return response;
        } catch (error) {
            console.error('Book search API request failed:', error);
            throw error;
        }
    }

    /**
     * Search books in the complete database with advanced filtering
     * 
     * @param {Object} searchOptions - Search and filter options
     * @param {string} searchOptions.q - Main search query (title, author, ISBN)
     * @param {string} searchOptions.title - Search by title specifically
     * @param {string} searchOptions.author - Search by author specifically
     * @param {string} searchOptions.isbn - Search by ISBN specifically
     * @param {number} searchOptions.category - Filter by category ID
     * @param {string} searchOptions.categoryName - Filter by category name
     * @param {number} searchOptions.minRating - Minimum average rating (1-5)
     * @param {number} searchOptions.maxRating - Maximum average rating (1-5)
     * @param {number} searchOptions.yearStart - Publication year range start
     * @param {number} searchOptions.yearEnd - Publication year range end
     * @param {string} searchOptions.language - Book language filter
     * @param {number} searchOptions.page - Page number (0-based)
     * @param {number} searchOptions.size - Page size (default: 20, max: 100)
     * @param {string} searchOptions.sort - Sort field ('title', 'author', 'publicationYear', 'rating', 'relevance')
     * @param {string} searchOptions.direction - Sort direction ('asc' or 'desc')
     * @param {boolean} searchOptions.includeExternal - Include external API results (Google Books, OpenLibrary)
     * @returns {Promise<Object>} Paginated search results
     */
    async searchBooks(searchOptions = {}) {
        try {
            const params = new URLSearchParams();
            
            // Main search parameters
            if (searchOptions.q) params.append('q', searchOptions.q);
            if (searchOptions.title) params.append('title', searchOptions.title);
            if (searchOptions.author) params.append('author', searchOptions.author);
            if (searchOptions.isbn) params.append('isbn', searchOptions.isbn);
            
            // Filter parameters
            if (searchOptions.category !== undefined) params.append('category', searchOptions.category);
            if (searchOptions.categoryName) params.append('categoryName', searchOptions.categoryName);
            if (searchOptions.minRating !== undefined) params.append('minRating', searchOptions.minRating);
            if (searchOptions.maxRating !== undefined) params.append('maxRating', searchOptions.maxRating);
            if (searchOptions.yearStart !== undefined) params.append('yearStart', searchOptions.yearStart);
            if (searchOptions.yearEnd !== undefined) params.append('yearEnd', searchOptions.yearEnd);
            if (searchOptions.language) params.append('language', searchOptions.language);
            
            // Pagination parameters
            if (searchOptions.page !== undefined) params.append('page', searchOptions.page);
            if (searchOptions.size !== undefined) params.append('size', searchOptions.size);
            if (searchOptions.sort) params.append('sort', searchOptions.sort);
            if (searchOptions.direction) params.append('direction', searchOptions.direction);
            
            // External API integration
            if (searchOptions.includeExternal !== undefined) params.append('includeExternal', searchOptions.includeExternal);

            const url = `${this.apiBaseURL}${params.toString() ? '?' + params.toString() : ''}`;
            const response = await this.makeRequest(url);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Search failed: ${response.status}`);
            }

            const data = await response.json();
            
            // Save search to history if it's a meaningful query
            if (searchOptions.q || searchOptions.title || searchOptions.author) {
                this.saveSearchToHistory({
                    query: searchOptions.q || `${searchOptions.title || ''} ${searchOptions.author || ''}`.trim(),
                    filters: searchOptions,
                    resultCount: data.totalElements || 0,
                    timestamp: new Date().toISOString()
                });
            }
            
            return {
                success: true,
                data: data,
                message: 'Search completed successfully'
            };
        } catch (error) {
            console.error('Error searching books:', error);
            return {
                success: false,
                message: error.message || 'Search failed. Please try again.',
                error: error
            };
        }
    }

    /**
     * Get detailed information about a specific book
     * 
     * @param {number} bookId - ID of the book to retrieve
     * @param {boolean} includeRatings - Include rating statistics
     * @param {boolean} includeSimilar - Include similar books
     * @returns {Promise<Object>} Book details
     */
    async getBookDetails(bookId, includeRatings = true, includeSimilar = false) {
        try {
            if (!bookId) {
                throw new Error('Book ID is required');
            }

            const params = new URLSearchParams();
            if (includeRatings) params.append('includeRatings', 'true');
            if (includeSimilar) params.append('includeSimilar', 'true');

            const url = `${this.apiBaseURL}/${bookId}${params.toString() ? '?' + params.toString() : ''}`;
            const response = await this.makeRequest(url);

            if (!response.ok) {
                if (response.status === 404) {
                    throw new Error('Book not found');
                }
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to fetch book details: ${response.status}`);
            }

            const data = await response.json();
            
            return {
                success: true,
                data: data,
                message: 'Book details retrieved successfully'
            };
        } catch (error) {
            console.error('Error fetching book details:', error);
            return {
                success: false,
                message: error.message || 'Failed to fetch book details. Please try again.',
                error: error
            };
        }
    }

    /**
     * Get search suggestions based on input
     * 
     * @param {string} query - Partial query for suggestions
     * @param {number} limit - Maximum number of suggestions (default: 10)
     * @param {string} type - Type of suggestions ('all', 'titles', 'authors', 'keywords')
     * @returns {Promise<Object>} Search suggestions
     */
    async getSearchSuggestions(query, limit = 10, type = 'all') {
        try {
            if (!query || query.trim().length < 2) {
                return {
                    success: true,
                    data: [],
                    message: 'Query too short for suggestions'
                };
            }

            const params = new URLSearchParams();
            params.append('q', query.trim());
            params.append('limit', limit.toString());
            params.append('type', type);

            const url = `${this.searchAPIURL}/suggestions?${params.toString()}`;
            const response = await this.makeRequest(url);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to fetch suggestions: ${response.status}`);
            }

            const data = await response.json();
            
            return {
                success: true,
                data: data,
                message: 'Suggestions retrieved successfully'
            };
        } catch (error) {
            console.error('Error fetching search suggestions:', error);
            return {
                success: false,
                message: error.message || 'Failed to fetch suggestions.',
                error: error,
                data: []
            };
        }
    }

    /**
     * Get popular searches and trending topics
     * 
     * @param {number} limit - Maximum number of popular searches (default: 20)
     * @param {string} timeframe - Timeframe for popularity ('day', 'week', 'month', 'all')
     * @returns {Promise<Object>} Popular searches
     */
    async getPopularSearches(limit = 20, timeframe = 'week') {
        try {
            const cacheKey = `popular_searches_${timeframe}_${limit}`;
            const cached = this.getFromCache(cacheKey);
            if (cached) {
                return {
                    success: true,
                    data: cached,
                    message: 'Popular searches retrieved from cache'
                };
            }

            const params = new URLSearchParams();
            params.append('limit', limit.toString());
            params.append('timeframe', timeframe);

            const url = `${this.searchAPIURL}/popular?${params.toString()}`;
            const response = await this.makeRequest(url);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to fetch popular searches: ${response.status}`);
            }

            const data = await response.json();
            
            // Cache the results
            this.setCache(cacheKey, data);
            
            return {
                success: true,
                data: data,
                message: 'Popular searches retrieved successfully'
            };
        } catch (error) {
            console.error('Error fetching popular searches:', error);
            return {
                success: false,
                message: error.message || 'Failed to fetch popular searches.',
                error: error,
                data: []
            };
        }
    }

    /**
     * Get all available categories
     * 
     * @param {boolean} includeBookCount - Include book count for each category
     * @returns {Promise<Object>} Categories list
     */
    async getCategories(includeBookCount = true) {
        try {
            const cacheKey = `categories_${includeBookCount}`;
            const cached = this.getFromCache(cacheKey);
            if (cached) {
                return {
                    success: true,
                    data: cached,
                    message: 'Categories retrieved from cache'
                };
            }

            const params = new URLSearchParams();
            if (includeBookCount) params.append('includeBookCount', 'true');

            const url = `${this.categoriesAPIURL}${params.toString() ? '?' + params.toString() : ''}`;
            const response = await this.makeRequest(url);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to fetch categories: ${response.status}`);
            }

            const data = await response.json();
            
            // Cache the results for longer since categories don't change often
            this.setCache(cacheKey, data, 30 * 60 * 1000); // 30 minutes
            
            return {
                success: true,
                data: data,
                message: 'Categories retrieved successfully'
            };
        } catch (error) {
            console.error('Error fetching categories:', error);
            return {
                success: false,
                message: error.message || 'Failed to fetch categories.',
                error: error,
                data: []
            };
        }
    }

    /**
     * Get books in a specific category
     * 
     * @param {number} categoryId - Category ID
     * @param {Object} options - Additional options
     * @param {number} options.page - Page number (0-based)
     * @param {number} options.size - Page size (default: 20)
     * @param {string} options.sort - Sort field
     * @param {string} options.direction - Sort direction
     * @returns {Promise<Object>} Books in category
     */
    async getBooksByCategory(categoryId, options = {}) {
        try {
            if (!categoryId) {
                throw new Error('Category ID is required');
            }

            const params = new URLSearchParams();
            if (options.page !== undefined) params.append('page', options.page);
            if (options.size !== undefined) params.append('size', options.size);
            if (options.sort) params.append('sort', options.sort);
            if (options.direction) params.append('direction', options.direction);

            const url = `${this.categoriesAPIURL}/${categoryId}/books${params.toString() ? '?' + params.toString() : ''}`;
            const response = await this.makeRequest(url);

            if (!response.ok) {
                if (response.status === 404) {
                    throw new Error('Category not found');
                }
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to fetch books in category: ${response.status}`);
            }

            const data = await response.json();
            
            return {
                success: true,
                data: data,
                message: 'Category books retrieved successfully'
            };
        } catch (error) {
            console.error('Error fetching books by category:', error);
            return {
                success: false,
                message: error.message || 'Failed to fetch books in category.',
                error: error
            };
        }
    }

    /**
     * Get similar books to a given book
     * 
     * @param {number} bookId - Book ID to find similar books for
     * @param {number} limit - Maximum number of similar books (default: 10)
     * @param {string} algorithm - Similarity algorithm ('category', 'author', 'keywords', 'collaborative')
     * @returns {Promise<Object>} Similar books
     */
    async getSimilarBooks(bookId, limit = 10, algorithm = 'category') {
        try {
            if (!bookId) {
                throw new Error('Book ID is required');
            }

            const params = new URLSearchParams();
            params.append('limit', limit.toString());
            params.append('algorithm', algorithm);

            const url = `${this.apiBaseURL}/${bookId}/similar?${params.toString()}`;
            const response = await this.makeRequest(url);

            if (!response.ok) {
                if (response.status === 404) {
                    throw new Error('Book not found');
                }
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to fetch similar books: ${response.status}`);
            }

            const data = await response.json();
            
            return {
                success: true,
                data: data,
                message: 'Similar books retrieved successfully'
            };
        } catch (error) {
            console.error('Error fetching similar books:', error);
            return {
                success: false,
                message: error.message || 'Failed to fetch similar books.',
                error: error,
                data: []
            };
        }
    }

    /**
     * Get user's search history
     * 
     * @param {number} limit - Maximum number of history entries (default: 20)
     * @returns {Promise<Object>} Search history
     */
    async getSearchHistory(limit = 20) {
        try {
            const params = new URLSearchParams();
            params.append('limit', limit.toString());

            const url = `${this.searchAPIURL}/history?${params.toString()}`;
            const response = await this.makeRequest(url);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to fetch search history: ${response.status}`);
            }

            const data = await response.json();
            
            return {
                success: true,
                data: data,
                message: 'Search history retrieved successfully'
            };
        } catch (error) {
            console.error('Error fetching search history:', error);
            return {
                success: false,
                message: error.message || 'Failed to fetch search history.',
                error: error,
                data: []
            };
        }
    }

    /**
     * Clear user's search history
     * 
     * @returns {Promise<Object>} Success/failure response
     */
    async clearSearchHistory() {
        try {
            const response = await this.makeRequest(`${this.searchAPIURL}/history`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to clear search history: ${response.status}`);
            }

            return {
                success: true,
                message: 'Search history cleared successfully'
            };
        } catch (error) {
            console.error('Error clearing search history:', error);
            return {
                success: false,
                message: error.message || 'Failed to clear search history.',
                error: error
            };
        }
    }

    /**
     * Save search query to local storage for quick access
     * (This is a client-side utility function, server-side history is handled by backend)
     */
    saveSearchToHistory(searchData) {
        try {
            const history = this.getLocalSearchHistory();
            
            // Avoid duplicates - remove existing entry with same query
            const filteredHistory = history.filter(item => 
                item.query.toLowerCase() !== searchData.query.toLowerCase()
            );
            
            // Add new search to beginning
            filteredHistory.unshift(searchData);
            
            // Keep only last 50 searches
            const trimmedHistory = filteredHistory.slice(0, 50);
            
            localStorage.setItem('homeArchive_searchHistory', JSON.stringify(trimmedHistory));
        } catch (error) {
            console.warn('Failed to save search to local history:', error);
        }
    }

    /**
     * Get search history from local storage
     */
    getLocalSearchHistory() {
        try {
            const history = localStorage.getItem('homeArchive_searchHistory');
            return history ? JSON.parse(history) : [];
        } catch (error) {
            console.warn('Failed to retrieve local search history:', error);
            return [];
        }
    }

    /**
     * Cache management utilities
     */
    setCache(key, data, timeout = this.cacheTimeout) {
        this.cache.set(key, {
            data,
            timestamp: Date.now(),
            timeout
        });
    }

    getFromCache(key) {
        const cached = this.cache.get(key);
        if (!cached) return null;
        
        if (Date.now() - cached.timestamp > cached.timeout) {
            this.cache.delete(key);
            return null;
        }
        
        return cached.data;
    }

    clearCache() {
        this.cache.clear();
    }

    /**
     * Utility method to build search URL with query parameters
     * 
     * @param {Object} params - Parameters object
     * @returns {URLSearchParams} URL search parameters
     */
    buildQueryParams(params) {
        const searchParams = new URLSearchParams();
        
        Object.entries(params).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                searchParams.append(key, value);
            }
        });
        
        return searchParams;
    }

    /**
     * Utility method to validate search parameters
     * 
     * @param {Object} searchOptions - Search options to validate
     * @returns {Object} Validation result with errors if any
     */
    validateSearchOptions(searchOptions) {
        const errors = [];
        
        // Validate page and size
        if (searchOptions.page !== undefined && (searchOptions.page < 0 || !Number.isInteger(searchOptions.page))) {
            errors.push('Page must be a non-negative integer');
        }
        
        if (searchOptions.size !== undefined) {
            if (searchOptions.size < 1 || searchOptions.size > 100 || !Number.isInteger(searchOptions.size)) {
                errors.push('Size must be an integer between 1 and 100');
            }
        }
        
        // Validate rating range
        if (searchOptions.minRating !== undefined && (searchOptions.minRating < 1 || searchOptions.minRating > 5)) {
            errors.push('Minimum rating must be between 1 and 5');
        }
        
        if (searchOptions.maxRating !== undefined && (searchOptions.maxRating < 1 || searchOptions.maxRating > 5)) {
            errors.push('Maximum rating must be between 1 and 5');
        }
        
        if (searchOptions.minRating !== undefined && searchOptions.maxRating !== undefined && 
            searchOptions.minRating > searchOptions.maxRating) {
            errors.push('Minimum rating cannot be greater than maximum rating');
        }
        
        // Validate year range
        const currentYear = new Date().getFullYear();
        if (searchOptions.yearStart !== undefined && 
            (searchOptions.yearStart < 1000 || searchOptions.yearStart > currentYear)) {
            errors.push(`Start year must be between 1000 and ${currentYear}`);
        }
        
        if (searchOptions.yearEnd !== undefined && 
            (searchOptions.yearEnd < 1000 || searchOptions.yearEnd > currentYear)) {
            errors.push(`End year must be between 1000 and ${currentYear}`);
        }
        
        if (searchOptions.yearStart !== undefined && searchOptions.yearEnd !== undefined && 
            searchOptions.yearStart > searchOptions.yearEnd) {
            errors.push('Start year cannot be greater than end year');
        }
        
        // Validate sort options
        const validSortFields = ['title', 'author', 'publicationYear', 'rating', 'relevance'];
        if (searchOptions.sort && !validSortFields.includes(searchOptions.sort)) {
            errors.push(`Sort field must be one of: ${validSortFields.join(', ')}`);
        }
        
        const validDirections = ['asc', 'desc'];
        if (searchOptions.direction && !validDirections.includes(searchOptions.direction)) {
            errors.push(`Sort direction must be one of: ${validDirections.join(', ')}`);
        }
        
        return {
            isValid: errors.length === 0,
            errors
        };
    }

    /**
     * Utility method to format search results for display
     * 
     * @param {Object} searchResult - Raw search result from API
     * @returns {Object} Formatted search result
     */
    formatSearchResult(searchResult) {
        return {
            ...searchResult,
            formattedRating: searchResult.averageRating ? 
                `${searchResult.averageRating.toFixed(1)} (${searchResult.ratingCount} ratings)` : 
                'No ratings',
            formattedYear: searchResult.publicationYear || 'Unknown',
            shortDescription: searchResult.description ? 
                (searchResult.description.length > 200 ? 
                    searchResult.description.substring(0, 200) + '...' : 
                    searchResult.description) : 
                'No description available'
        };
    }

    /**
     * Utility method to debounce search requests
     * 
     * @param {Function} func - Function to debounce
     * @param {number} delay - Delay in milliseconds
     * @returns {Function} Debounced function
     */
    debounce(func, delay) {
        let timeoutId;
        return function (...args) {
            clearTimeout(timeoutId);
            timeoutId = setTimeout(() => func.apply(this, args), delay);
        };
    }
}

// Create global instance
const bookSearchService = new BookSearchService();

// Export for module use if needed
if (typeof module !== 'undefined' && module.exports) {
    module.exports = BookSearchService;
}