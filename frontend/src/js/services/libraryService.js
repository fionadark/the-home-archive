/**
 * Library Service for The Home Archive
 * Handles personal library management API communication with backend
 * Compliant with Spring Boot 3.x REST API and authentication patterns
 * 
 * This service provides methods to:
 * - Retrieve user's personal library with pagination and filtering
 * - Search within user's library
 * - Add books to personal library with reading status and notes
 * - Update library entries (status, notes, rating, etc.)
 * - Remove books from personal library
 * - Get library statistics and insights
 */

class LibraryService {
    constructor() {
        this.baseURL = ''; // Spring Boot serves from same origin
        this.apiBaseURL = '/api/library';
        
        // Reference to auth service for token management
        this.authService = typeof authService !== 'undefined' ? authService : null;
        
        // Initialize CSRF token if available
        this.csrfToken = this.getCSRFToken();
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
            console.error('Library API request failed:', error);
            throw error;
        }
    }

    /**
     * Get user's personal library with pagination and filtering
     * 
     * @param {Object} options - Query options
     * @param {number} options.page - Page number (0-based)
     * @param {number} options.size - Page size (default: 20)
     * @param {string} options.sort - Sort field (e.g., 'title', 'author', 'dateAdded', 'lastRead')
     * @param {string} options.direction - Sort direction ('asc' or 'desc')
     * @param {string} options.status - Filter by reading status ('UNREAD', 'READING', 'READ', 'DNF')
     * @param {string} options.category - Filter by category name
     * @param {number} options.rating - Filter by user rating (1-5)
     * @param {string} options.search - Search query for title/author
     * @returns {Promise<Object>} Paginated library response
     */
    async getUserLibrary(options = {}) {
        try {
            const params = new URLSearchParams();
            
            // Add pagination parameters
            if (options.page !== undefined) params.append('page', options.page);
            if (options.size !== undefined) params.append('size', options.size);
            if (options.sort) params.append('sort', options.sort);
            if (options.direction) params.append('direction', options.direction);
            
            // Add filter parameters
            if (options.status) params.append('status', options.status);
            if (options.category) params.append('category', options.category);
            if (options.rating !== undefined) params.append('rating', options.rating);
            if (options.search) params.append('search', options.search);

            const url = `${this.apiBaseURL}${params.toString() ? '?' + params.toString() : ''}`;
            const response = await this.makeRequest(url);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to fetch library: ${response.status}`);
            }

            const data = await response.json();
            
            return {
                success: true,
                data: data,
                message: 'Library retrieved successfully'
            };
        } catch (error) {
            console.error('Error fetching user library:', error);
            return {
                success: false,
                message: error.message || 'Failed to fetch library. Please try again.',
                error: error
            };
        }
    }

    /**
     * Search within user's personal library
     * 
     * @param {string} query - Search query
     * @param {Object} options - Additional search options
     * @param {number} options.page - Page number (0-based)
     * @param {number} options.size - Page size (default: 20)
     * @param {string} options.sort - Sort field
     * @param {string} options.direction - Sort direction
     * @returns {Promise<Object>} Search results
     */
    async searchUserLibrary(query, options = {}) {
        try {
            const params = new URLSearchParams();
            params.append('query', query);
            
            // Add pagination and sort parameters
            if (options.page !== undefined) params.append('page', options.page);
            if (options.size !== undefined) params.append('size', options.size);
            if (options.sort) params.append('sort', options.sort);
            if (options.direction) params.append('direction', options.direction);

            const url = `${this.apiBaseURL}/search?${params.toString()}`;
            const response = await this.makeRequest(url);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Search failed: ${response.status}`);
            }

            const data = await response.json();
            
            return {
                success: true,
                data: data,
                message: 'Search completed successfully'
            };
        } catch (error) {
            console.error('Error searching library:', error);
            return {
                success: false,
                message: error.message || 'Search failed. Please try again.',
                error: error
            };
        }
    }

    /**
     * Add a book to user's personal library
     * 
     * @param {Object} bookData - Book data to add
     * @param {number} bookData.bookId - ID of the book to add
     * @param {string} bookData.status - Reading status ('UNREAD', 'READING', 'READ', 'DNF')
     * @param {number} bookData.userRating - User's rating (1-5, optional)
     * @param {string} bookData.personalNotes - Personal notes (optional)
     * @param {string} bookData.readingStartDate - When started reading (ISO date, optional)
     * @param {string} bookData.readingEndDate - When finished reading (ISO date, optional)
     * @param {boolean} bookData.isFavorite - Whether book is marked as favorite (optional)
     * @param {string} bookData.personalTags - Comma-separated personal tags (optional)
     * @returns {Promise<Object>} Response with added library entry
     */
    async addBookToLibrary(bookData) {
        try {
            // Validate required fields
            if (!bookData.bookId) {
                throw new Error('Book ID is required');
            }
            if (!bookData.status || !['UNREAD', 'READING', 'READ', 'DNF'].includes(bookData.status)) {
                throw new Error('Valid reading status is required (UNREAD, READING, READ, DNF)');
            }

            const response = await this.makeRequest(`${this.apiBaseURL}/add`, {
                method: 'POST',
                body: bookData
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to add book to library: ${response.status}`);
            }

            const data = await response.json();
            
            return {
                success: true,
                data: data,
                message: 'Book added to library successfully'
            };
        } catch (error) {
            console.error('Error adding book to library:', error);
            return {
                success: false,
                message: error.message || 'Failed to add book to library. Please try again.',
                error: error
            };
        }
    }

    /**
     * Update a library entry
     * 
     * @param {number} entryId - ID of the library entry to update
     * @param {Object} updateData - Data to update
     * @param {string} updateData.status - Reading status (optional)
     * @param {number} updateData.userRating - User's rating (1-5, optional)
     * @param {string} updateData.personalNotes - Personal notes (optional)
     * @param {string} updateData.readingStartDate - When started reading (ISO date, optional)
     * @param {string} updateData.readingEndDate - When finished reading (ISO date, optional)
     * @param {boolean} updateData.isFavorite - Whether book is marked as favorite (optional)
     * @param {string} updateData.personalTags - Comma-separated personal tags (optional)
     * @returns {Promise<Object>} Response with updated library entry
     */
    async updateLibraryEntry(entryId, updateData) {
        try {
            if (!entryId) {
                throw new Error('Entry ID is required');
            }

            // Validate status if provided
            if (updateData.status && !['UNREAD', 'READING', 'READ', 'DNF'].includes(updateData.status)) {
                throw new Error('Valid reading status is required (UNREAD, READING, READ, DNF)');
            }

            const response = await this.makeRequest(`${this.apiBaseURL}/${entryId}`, {
                method: 'PUT',
                body: updateData
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to update library entry: ${response.status}`);
            }

            const data = await response.json();
            
            return {
                success: true,
                data: data,
                message: 'Library entry updated successfully'
            };
        } catch (error) {
            console.error('Error updating library entry:', error);
            return {
                success: false,
                message: error.message || 'Failed to update library entry. Please try again.',
                error: error
            };
        }
    }

    /**
     * Remove a book from user's personal library
     * 
     * @param {number} entryId - ID of the library entry to remove
     * @returns {Promise<Object>} Response indicating success/failure
     */
    async removeBookFromLibrary(entryId) {
        try {
            if (!entryId) {
                throw new Error('Entry ID is required');
            }

            const response = await this.makeRequest(`${this.apiBaseURL}/${entryId}`, {
                method: 'DELETE'
            });

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to remove book from library: ${response.status}`);
            }

            return {
                success: true,
                message: 'Book removed from library successfully'
            };
        } catch (error) {
            console.error('Error removing book from library:', error);
            return {
                success: false,
                message: error.message || 'Failed to remove book from library. Please try again.',
                error: error
            };
        }
    }

    /**
     * Get library statistics for the current user
     * 
     * @returns {Promise<Object>} Library statistics
     */
    async getLibraryStatistics() {
        try {
            const response = await this.makeRequest(`${this.apiBaseURL}/stats`);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to fetch library statistics: ${response.status}`);
            }

            const data = await response.json();
            
            return {
                success: true,
                data: data,
                message: 'Library statistics retrieved successfully'
            };
        } catch (error) {
            console.error('Error fetching library statistics:', error);
            return {
                success: false,
                message: error.message || 'Failed to fetch library statistics. Please try again.',
                error: error
            };
        }
    }

    /**
     * Get a specific library entry by ID
     * 
     * @param {number} entryId - ID of the library entry
     * @returns {Promise<Object>} Library entry details
     */
    async getLibraryEntry(entryId) {
        try {
            if (!entryId) {
                throw new Error('Entry ID is required');
            }

            const response = await this.makeRequest(`${this.apiBaseURL}/${entryId}`);

            if (!response.ok) {
                if (response.status === 404) {
                    throw new Error('Library entry not found');
                }
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to fetch library entry: ${response.status}`);
            }

            const data = await response.json();
            
            return {
                success: true,
                data: data,
                message: 'Library entry retrieved successfully'
            };
        } catch (error) {
            console.error('Error fetching library entry:', error);
            return {
                success: false,
                message: error.message || 'Failed to fetch library entry. Please try again.',
                error: error
            };
        }
    }

    /**
     * Check if a book is in user's library
     * 
     * @param {number} bookId - ID of the book to check
     * @returns {Promise<Object>} Response indicating if book is in library
     */
    async isBookInLibrary(bookId) {
        try {
            if (!bookId) {
                throw new Error('Book ID is required');
            }

            const response = await this.makeRequest(`${this.apiBaseURL}/contains/${bookId}`);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to check book in library: ${response.status}`);
            }

            const data = await response.json();
            
            return {
                success: true,
                data: data,
                message: 'Book library status checked successfully'
            };
        } catch (error) {
            console.error('Error checking book in library:', error);
            return {
                success: false,
                message: error.message || 'Failed to check book in library. Please try again.',
                error: error
            };
        }
    }

    /**
     * Get reading progress for currently reading books
     * 
     * @returns {Promise<Object>} Reading progress data
     */
    async getReadingProgress() {
        try {
            const response = await this.makeRequest(`${this.apiBaseURL}/progress`);

            if (!response.ok) {
                const errorData = await response.json().catch(() => ({}));
                throw new Error(errorData.message || `Failed to fetch reading progress: ${response.status}`);
            }

            const data = await response.json();
            
            return {
                success: true,
                data: data,
                message: 'Reading progress retrieved successfully'
            };
        } catch (error) {
            console.error('Error fetching reading progress:', error);
            return {
                success: false,
                message: error.message || 'Failed to fetch reading progress. Please try again.',
                error: error
            };
        }
    }

    /**
     * Utility method to validate reading status
     * 
     * @param {string} status - Status to validate
     * @returns {boolean} Whether status is valid
     */
    isValidReadingStatus(status) {
        return ['UNREAD', 'READING', 'READ', 'DNF'].includes(status);
    }

    /**
     * Utility method to validate rating
     * 
     * @param {number} rating - Rating to validate
     * @returns {boolean} Whether rating is valid
     */
    isValidRating(rating) {
        return rating >= 1 && rating <= 5 && Number.isInteger(rating);
    }

    /**
     * Utility method to format reading status for display
     * 
     * @param {string} status - Reading status
     * @returns {string} Formatted status
     */
    formatReadingStatus(status) {
        const statusMap = {
            'UNREAD': 'Not Started',
            'READING': 'Currently Reading',
            'READ': 'Completed',
            'DNF': 'Did Not Finish'
        };
        return statusMap[status] || status;
    }

    /**
     * Utility method to format date for API
     * 
     * @param {Date|string} date - Date to format
     * @returns {string} ISO date string
     */
    formatDateForAPI(date) {
        if (!date) return null;
        if (typeof date === 'string') {
            return new Date(date).toISOString().split('T')[0];
        }
        if (date instanceof Date) {
            return date.toISOString().split('T')[0];
        }
        return null;
    }

    /**
     * Utility method to build query parameters
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
}

// Create global instance
const libraryService = new LibraryService();

// Export for module use if needed
if (typeof module !== 'undefined' && module.exports) {
    module.exports = LibraryService;
}