/**
 * BaseService - Common functionality for all API services
 * Handles authentication, CSRF tokens, and common API patterns
 * Compliant with Spring Boot 3.x REST API security patterns
 */

export class BaseService {
    constructor(baseURL = '') {
        this.baseURL = baseURL;
        
        // Reference to auth service for token management
        this.authService = typeof authService !== 'undefined' ? authService : null;
        
        // Initialize CSRF token if available
        this.csrfToken = this.getCSRFToken();
        
        // Cache for frequently accessed data
        this.cache = new Map();
        this.cacheTimeout = 5 * 60 * 1000; // 5 minutes
    }

    /**
     * Get CSRF token from meta tag or cookie
     * Spring Boot 3.x compatibility
     * @returns {Object|null} CSRF token object with token and header
     */
    getCSRFToken() {
        const metaToken = document.querySelector('meta[name="_csrf"]');
        const metaHeader = document.querySelector('meta[name="_csrf_header"]');
        
        if (metaToken && metaHeader) {
            return {
                token: metaToken.getAttribute('content'),
                header: metaHeader.getAttribute('content')
            };
        }
        
        // Fallback to cookie if meta tags not available
        const tokenFromCookie = this.getCookieValue('XSRF-TOKEN');
        if (tokenFromCookie) {
            return {
                token: tokenFromCookie,
                header: 'X-XSRF-TOKEN'
            };
        }
        
        return null;
    }

    /**
     * Get authentication token from auth service or localStorage
     * @returns {string|null} Auth token
     */
    getAuthToken() {
        return this.authService ? 
            this.authService.getAccessToken() : 
            localStorage.getItem('homeArchive_accessToken');
    }

    /**
     * Create headers with authentication and CSRF tokens
     * @param {Object} additionalHeaders - Additional headers to include
     * @returns {Object} Complete headers object
     */
    createHeaders(additionalHeaders = {}) {
        const headers = {
            'Content-Type': 'application/json',
            ...additionalHeaders
        };

        // Add CSRF token
        if (this.csrfToken) {
            headers[this.csrfToken.header] = this.csrfToken.token;
        }

        // Add authentication
        const token = this.getAuthToken();
        if (token) {
            headers['Authorization'] = `Bearer ${token}`;
        }

        return headers;
    }

    /**
     * Make authenticated API request with automatic retry on 401
     * @param {string} url - Request URL
     * @param {Object} options - Fetch options
     * @returns {Promise<Response>} Fetch response
     */
    async makeRequest(url, options = {}) {
        const headers = this.createHeaders(options.headers);
        
        let response = await fetch(url, {
            ...options,
            headers
        });

        // Handle token refresh on 401
        if (response.status === 401) {
            const token = this.getAuthToken();
            if (token && this.authService) {
                const refreshed = await this.authService.refreshAccessToken();
                if (refreshed) {
                    // Retry with new token
                    headers['Authorization'] = `Bearer ${this.authService.getAccessToken()}`;
                    response = await fetch(url, {
                        ...options,
                        headers
                    });
                } else {
                    // Refresh failed, logout user
                    this.authService.logout();
                    throw new Error('Authentication failed');
                }
            }
        }

        return response;
    }

    /**
     * Handle API response with standard error handling
     * @param {Response} response - Fetch response
     * @returns {Promise<any>} Parsed response data
     */
    async handleResponse(response) {
        if (!response.ok) {
            let errorMessage = `HTTP error! status: ${response.status}`;
            
            try {
                const errorData = await response.json();
                errorMessage = errorData.message || errorData.error || errorMessage;
            } catch (e) {
                // If JSON parsing fails, use default message
            }
            
            throw new Error(errorMessage);
        }

        // Handle different content types
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        }
        
        return await response.text();
    }

    /**
     * Get value from cookie
     * @param {string} name - Cookie name
     * @returns {string|null} Cookie value
     */
    getCookieValue(name) {
        const value = `; ${document.cookie}`;
        const parts = value.split(`; ${name}=`);
        if (parts.length === 2) {
            return parts.pop().split(';').shift();
        }
        return null;
    }

    /**
     * Cache data with expiration
     * @param {string} key - Cache key
     * @param {any} data - Data to cache
     */
    setCache(key, data) {
        this.cache.set(key, {
            data,
            timestamp: Date.now()
        });
    }

    /**
     * Get cached data if not expired
     * @param {string} key - Cache key
     * @returns {any|null} Cached data or null if expired/not found
     */
    getCache(key) {
        const cached = this.cache.get(key);
        if (!cached) return null;

        if (Date.now() - cached.timestamp > this.cacheTimeout) {
            this.cache.delete(key);
            return null;
        }

        return cached.data;
    }

    /**
     * Clear all cached data
     */
    clearCache() {
        this.cache.clear();
    }
}