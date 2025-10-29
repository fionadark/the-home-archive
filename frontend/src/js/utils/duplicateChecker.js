/**
 * DuplicateChecker - Utility for preventing duplicate book additions
 * Part of Phase 5 User Story 3 implementation - T100
 * 
 * Provides client-side duplicate checking before attempting to add books
 * to the user's personal library, reducing unnecessary API calls and 
 * improving user experience with immediate feedback.
 */

import { LibraryService } from '../services/libraryService.js';

export default class DuplicateChecker {
    constructor() {
        this.libraryService = new LibraryService();
        
        // Cache for user's library books to avoid repeated API calls
        this.userLibraryCache = new Map();
        this.cacheTimestamp = null;
        this.cacheExpiryMs = 5 * 60 * 1000; // 5 minutes cache
        
        // Set of book IDs currently being checked/added to prevent race conditions
        this.pendingOperations = new Set();
    }

    /**
     * Check if a book is already in the user's personal library
     * @param {number|string} bookId - The book ID to check
     * @param {Object} options - Options for the check
     * @param {boolean} options.useCache - Whether to use cached data (default: true)
     * @param {boolean} options.showUI - Whether to show UI feedback (default: true)
     * @returns {Promise<boolean>} True if book is already in library, false otherwise
     */
    async isBookInLibrary(bookId, options = {}) {
        const {
            useCache = true,
            showUI = true
        } = options;

        // Convert to number for consistency
        const numericBookId = parseInt(bookId, 10);
        
        if (isNaN(numericBookId) || numericBookId <= 0) {
            throw new Error('Invalid book ID provided for duplicate check');
        }

        // Check if operation is already pending
        if (this.pendingOperations.has(numericBookId)) {
            if (showUI) {
                this.showTemporaryMessage('Checking for duplicates...', 'info');
            }
            return true; // Assume duplicate to prevent race condition
        }

        try {
            // Add to pending operations
            this.pendingOperations.add(numericBookId);

            // Try cache first if enabled and valid
            if (useCache && this.isCacheValid()) {
                const cachedResult = this.userLibraryCache.has(numericBookId);
                if (showUI && cachedResult) {
                    this.showTemporaryMessage('This book is already in your library', 'warning');
                }
                return cachedResult;
            }

            // Make API call to check if book exists in library
            const response = await this.libraryService.isBookInLibrary(numericBookId);
            
            // Handle the response format (LibraryService returns {success, data, message})
            const exists = response.success && response.data === true;
            
            // Update cache
            if (exists) {
                this.userLibraryCache.set(numericBookId, true);
                if (showUI) {
                    this.showTemporaryMessage('This book is already in your library', 'warning');
                }
            }

            return exists;

        } catch (error) {
            console.error('Error checking for duplicate book:', error);
            
            if (showUI) {
                this.showTemporaryMessage('Unable to verify if book is already in library. Proceeding with caution.', 'warning');
            }
            
            // On error, assume no duplicate to allow user to proceed
            return false;
            
        } finally {
            // Remove from pending operations
            this.pendingOperations.delete(numericBookId);
        }
    }

    /**
     * Check multiple books for duplicates efficiently
     * @param {Array<number|string>} bookIds - Array of book IDs to check
     * @param {Object} options - Options for the check
     * @returns {Promise<Object>} Object mapping bookId to boolean (true if duplicate)
     */
    async checkMultipleBooks(bookIds, options = {}) {
        const results = {};
        
        // Process books in parallel but limit concurrent requests
        const batchSize = 5;
        for (let i = 0; i < bookIds.length; i += batchSize) {
            const batch = bookIds.slice(i, i + batchSize);
            const batchPromises = batch.map(async bookId => {
                const isDuplicate = await this.isBookInLibrary(bookId, {
                    ...options,
                    showUI: false // Don't show UI for batch operations
                });
                return { bookId: parseInt(bookId, 10), isDuplicate };
            });
            
            const batchResults = await Promise.all(batchPromises);
            batchResults.forEach(({ bookId, isDuplicate }) => {
                results[bookId] = isDuplicate;
            });
        }
        
        return results;
    }

    /**
     * Refresh the user's library cache
     * @returns {Promise<void>}
     */
    async refreshCache() {
        try {
            // Get user's library with pagination to avoid large responses
            const libraryData = await this.libraryService.getUserLibrary({
                page: 0,
                size: 1000, // Should cover most personal libraries
                sortBy: 'dateAdded',
                sortDir: 'desc'
            });

            // Clear and rebuild cache
            this.userLibraryCache.clear();
            
            if (libraryData && libraryData.content) {
                libraryData.content.forEach(book => {
                    if (book.bookId) {
                        this.userLibraryCache.set(book.bookId, true);
                    }
                });
            }
            
            this.cacheTimestamp = Date.now();
            
        } catch (error) {
            console.error('Error refreshing library cache:', error);
            // Clear cache on error to force fresh checks
            this.clearCache();
        }
    }

    /**
     * Clear the library cache
     */
    clearCache() {
        this.userLibraryCache.clear();
        this.cacheTimestamp = null;
    }

    /**
     * Check if the current cache is still valid
     * @returns {boolean} True if cache is valid, false otherwise
     */
    isCacheValid() {
        if (!this.cacheTimestamp) {
            return false;
        }
        
        return (Date.now() - this.cacheTimestamp) < this.cacheExpiryMs;
    }

    /**
     * Mark a book as added to library (update cache)
     * @param {number|string} bookId - The book ID that was added
     */
    markBookAsAdded(bookId) {
        const numericBookId = parseInt(bookId, 10);
        if (!isNaN(numericBookId)) {
            this.userLibraryCache.set(numericBookId, true);
        }
    }

    /**
     * Mark a book as removed from library (update cache)
     * @param {number|string} bookId - The book ID that was removed
     */
    markBookAsRemoved(bookId) {
        const numericBookId = parseInt(bookId, 10);
        if (!isNaN(numericBookId)) {
            this.userLibraryCache.delete(numericBookId);
        }
    }

    /**
     * Show temporary message to user
     * @param {string} message - Message to display
     * @param {string} type - Message type (info, warning, error, success)
     */
    showTemporaryMessage(message, type = 'info') {
        // Check if we have a notification service available
        if (typeof window !== 'undefined' && window.notificationService) {
            window.notificationService.show(message, type);
            return;
        }

        // Fallback to console and simple UI indication
        console.log(`[${type.toUpperCase()}] ${message}`);
        
        // Try to show a simple browser notification if available
        if ('Notification' in window && Notification.permission === 'granted') {
            new Notification('Library Duplicate Check', {
                body: message,
                icon: '/favicon.ico',
                tag: 'duplicate-check'
            });
        }
        
        // Create a temporary toast message
        this.showToast(message, type);
    }

    /**
     * Show a simple toast notification
     * @param {string} message - Message to display
     * @param {string} type - Message type for styling
     */
    showToast(message, type) {
        // Remove any existing toast
        const existingToast = document.querySelector('.duplicate-checker-toast');
        if (existingToast) {
            existingToast.remove();
        }

        // Create toast element
        const toast = document.createElement('div');
        toast.className = `duplicate-checker-toast toast-${type}`;
        toast.style.cssText = `
            position: fixed;
            top: 20px;
            right: 20px;
            padding: 12px 20px;
            background: var(--color-surface-elevated, #1a1a1a);
            color: var(--color-text-primary, #ffffff);
            border: 1px solid var(--color-border, #333);
            border-radius: 8px;
            font-family: var(--font-family-body, 'Inter', sans-serif);
            font-size: 14px;
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
            z-index: 10000;
            max-width: 300px;
            animation: slideInFromRight 0.3s ease-out;
        `;

        // Add type-specific styling
        if (type === 'warning') {
            toast.style.borderColor = '#f59e0b';
            toast.style.background = '#451a03';
        } else if (type === 'error') {
            toast.style.borderColor = '#ef4444';
            toast.style.background = '#450a0a';
        } else if (type === 'success') {
            toast.style.borderColor = '#10b981';
            toast.style.background = '#064e3b';
        }

        toast.textContent = message;
        document.body.appendChild(toast);

        // Add animation styles if not already added
        if (!document.querySelector('#duplicate-checker-styles')) {
            const styles = document.createElement('style');
            styles.id = 'duplicate-checker-styles';
            styles.textContent = `
                @keyframes slideInFromRight {
                    from {
                        transform: translateX(100%);
                        opacity: 0;
                    }
                    to {
                        transform: translateX(0);
                        opacity: 1;
                    }
                }
            `;
            document.head.appendChild(styles);
        }

        // Auto-remove after 4 seconds
        setTimeout(() => {
            if (toast.parentNode) {
                toast.style.animation = 'slideInFromRight 0.3s ease-out reverse';
                setTimeout(() => toast.remove(), 300);
            }
        }, 4000);
    }

    /**
     * Validate that a book addition should proceed
     * @param {number|string} bookId - The book ID to validate
     * @param {Object} options - Validation options
     * @param {boolean} options.force - Force addition even if duplicate
     * @param {boolean} options.interactive - Show confirmation dialogs
     * @returns {Promise<boolean>} True if addition should proceed, false otherwise
     */
    async validateBookAddition(bookId, options = {}) {
        const {
            force = false,
            interactive = true
        } = options;

        if (force) {
            return true;
        }

        const isDuplicate = await this.isBookInLibrary(bookId, {
            useCache: true,
            showUI: interactive
        });

        if (!isDuplicate) {
            return true;
        }

        // Book is duplicate - handle based on interactive mode
        if (!interactive) {
            return false;
        }

        // Show confirmation dialog for interactive mode
        return this.showDuplicateConfirmation();
    }

    /**
     * Show confirmation dialog for duplicate book
     * @returns {Promise<boolean>} True if user wants to proceed, false otherwise
     */
    async showDuplicateConfirmation() {
        return new Promise((resolve) => {
            const message = 'This book is already in your library. Do you want to add it again?';
            
            // Use browser confirm as fallback
            if (typeof window !== 'undefined' && window.confirm) {
                resolve(window.confirm(message));
                return;
            }

            // If no confirm available, default to false (don't add duplicate)
            resolve(false);
        });
    }

    /**
     * Get cache statistics for debugging
     * @returns {Object} Cache statistics
     */
    getCacheStats() {
        return {
            cacheSize: this.userLibraryCache.size,
            cacheAge: this.cacheTimestamp ? Date.now() - this.cacheTimestamp : null,
            isValid: this.isCacheValid(),
            pendingOperations: this.pendingOperations.size,
            cachedBookIds: Array.from(this.userLibraryCache.keys())
        };
    }
}

// Create and export a singleton instance for convenience
export const duplicateChecker = new DuplicateChecker();

// Also export the class for custom instantiation
export { DuplicateChecker };