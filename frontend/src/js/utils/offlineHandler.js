/**
 * Offline Handler for The Home Archive - Dark Academia Library
 * Provides graceful degradation for offline scenarios and network connectivity management
 * Maintains dark academia aesthetic and provides seamless user experience during network issues
 * 
 * Features:
 * - Network status detection and monitoring
 * - Local storage cache management for critical data
 * - Operation queuing for offline actions
 * - Graceful UI degradation with appropriate messaging
 * - Background sync when connection is restored
 * - Service worker integration for advanced offline capabilities
 */

(function() {
    'use strict';

    /**
     * OfflineHandler - Main offline management system
     */
    class OfflineHandler {
        constructor() {
            this.isOnline = navigator.onLine;
            this.cache = new Map();
            this.operationQueue = [];
            this.subscribers = new Set();
            this.retryAttempts = new Map();
            this.maxRetryAttempts = 3;
            this.retryDelay = 1000;
            this.cachePrefix = 'homeArchive_offline_';
            this.cacheExpiry = 24 * 60 * 60 * 1000; // 24 hours
            
            // UI components
            this.notificationService = null;
            this.loadingManager = window.UIUtils?.loading || null;
            this.errorHandler = window.UIUtils?.error || null;
            
            // Connection monitoring
            this.connectionCheckInterval = null;
            this.connectionCheckDelay = 30000; // 30 seconds
            
            this.init();
        }

        /**
         * Initialize offline handler
         */
        init() {
            this.setupEventListeners();
            this.loadCachedData();
            this.addStyles();
            this.startConnectionMonitoring();
            
            // Register service worker if available
            if ('serviceWorker' in navigator) {
                this.registerServiceWorker();
            }
            
            // Show initial status if offline
            if (!this.isOnline) {
                this.handleOfflineMode();
            }
        }

        /**
         * Set notification service for status messages
         * @param {Object} notificationService - Notification service instance
         */
        setNotificationService(notificationService) {
            this.notificationService = notificationService;
        }

        /**
         * Set up network event listeners
         */
        setupEventListeners() {
            window.addEventListener('online', () => this.handleOnlineMode());
            window.addEventListener('offline', () => this.handleOfflineMode());
            
            // Listen for page visibility changes to check connection
            document.addEventListener('visibilitychange', () => {
                if (document.visibilityState === 'visible') {
                    this.checkConnection();
                }
            });
            
            // Intercept fetch requests for caching
            if ('serviceWorker' in navigator && window.fetch) {
                this.interceptFetch();
            }
        }

        /**
         * Handle transition to online mode
         */
        async handleOnlineMode() {
            const wasOffline = !this.isOnline;
            this.isOnline = true;
            
            console.log('[OfflineHandler] Connection restored');
            
            if (wasOffline) {
                this.showStatus('Connection restored', 'success');
                
                // Process queued operations
                await this.processQueuedOperations();
                
                // Refresh cached data
                this.refreshCriticalData();
                
                // Notify subscribers
                this.notifySubscribers('online');
            }
        }

        /**
         * Handle transition to offline mode
         */
        handleOfflineMode() {
            this.isOnline = false;
            
            console.log('[OfflineHandler] Connection lost - switching to offline mode');
            
            this.showStatus('You are offline. Some features may be limited.', 'warning', true);
            
            // Notify subscribers
            this.notifySubscribers('offline');
        }

        /**
         * Check network connection with a lightweight request
         */
        async checkConnection() {
            try {
                // Use a lightweight endpoint to check connectivity
                const response = await fetch('/api/health', {
                    method: 'HEAD',
                    cache: 'no-cache',
                    signal: AbortSignal.timeout(5000) // 5 second timeout
                });
                
                const isCurrentlyOnline = response.ok;
                
                if (isCurrentlyOnline !== this.isOnline) {
                    if (isCurrentlyOnline) {
                        this.handleOnlineMode();
                    } else {
                        this.handleOfflineMode();
                    }
                }
            } catch (error) {
                // If health check fails, assume offline
                if (this.isOnline) {
                    this.handleOfflineMode();
                }
            }
        }

        /**
         * Start periodic connection monitoring
         */
        startConnectionMonitoring() {
            this.connectionCheckInterval = setInterval(() => {
                this.checkConnection();
            }, this.connectionCheckDelay);
        }

        /**
         * Stop connection monitoring
         */
        stopConnectionMonitoring() {
            if (this.connectionCheckInterval) {
                clearInterval(this.connectionCheckInterval);
                this.connectionCheckInterval = null;
            }
        }

        /**
         * Cache data for offline access
         * @param {string} key - Cache key
         * @param {*} data - Data to cache
         * @param {number} expiry - Cache expiry time in milliseconds (optional)
         */
        cacheData(key, data, expiry = this.cacheExpiry) {
            try {
                const cacheItem = {
                    data: data,
                    timestamp: Date.now(),
                    expiry: expiry
                };
                
                this.cache.set(key, cacheItem);
                localStorage.setItem(this.cachePrefix + key, JSON.stringify(cacheItem));
                
                console.log(`[OfflineHandler] Cached data for key: ${key}`);
            } catch (error) {
                console.warn('[OfflineHandler] Failed to cache data:', error);
                // If localStorage is full, try to clear old cache
                this.clearExpiredCache();
            }
        }

        /**
         * Get cached data
         * @param {string} key - Cache key
         * @returns {*} Cached data or null if not found/expired
         */
        getCachedData(key) {
            try {
                let cacheItem = this.cache.get(key);
                
                // If not in memory, try localStorage
                if (!cacheItem) {
                    const stored = localStorage.getItem(this.cachePrefix + key);
                    if (stored) {
                        cacheItem = JSON.parse(stored);
                        this.cache.set(key, cacheItem);
                    }
                }
                
                if (cacheItem) {
                    // Check if expired
                    if (Date.now() - cacheItem.timestamp > cacheItem.expiry) {
                        this.removeCachedData(key);
                        return null;
                    }
                    
                    console.log(`[OfflineHandler] Retrieved cached data for key: ${key}`);
                    return cacheItem.data;
                }
                
                return null;
            } catch (error) {
                console.warn('[OfflineHandler] Failed to retrieve cached data:', error);
                return null;
            }
        }

        /**
         * Remove cached data
         * @param {string} key - Cache key
         */
        removeCachedData(key) {
            this.cache.delete(key);
            localStorage.removeItem(this.cachePrefix + key);
        }

        /**
         * Clear expired cache items
         */
        clearExpiredCache() {
            const now = Date.now();
            
            // Clear from memory cache
            for (const [key, item] of this.cache.entries()) {
                if (now - item.timestamp > item.expiry) {
                    this.cache.delete(key);
                }
            }
            
            // Clear from localStorage
            for (let i = localStorage.length - 1; i >= 0; i--) {
                const key = localStorage.key(i);
                if (key && key.startsWith(this.cachePrefix)) {
                    try {
                        const item = JSON.parse(localStorage.getItem(key));
                        if (now - item.timestamp > item.expiry) {
                            localStorage.removeItem(key);
                        }
                    } catch (error) {
                        // Remove corrupted cache items
                        localStorage.removeItem(key);
                    }
                }
            }
        }

        /**
         * Load cached data from localStorage into memory
         */
        loadCachedData() {
            try {
                for (let i = 0; i < localStorage.length; i++) {
                    const key = localStorage.key(i);
                    if (key && key.startsWith(this.cachePrefix)) {
                        const cacheKey = key.replace(this.cachePrefix, '');
                        const item = JSON.parse(localStorage.getItem(key));
                        this.cache.set(cacheKey, item);
                    }
                }
                
                // Clean expired items
                this.clearExpiredCache();
                
                console.log('[OfflineHandler] Loaded cached data from storage');
            } catch (error) {
                console.warn('[OfflineHandler] Failed to load cached data:', error);
            }
        }

        /**
         * Queue operation for when connection is restored
         * @param {Object} operation - Operation to queue
         * @param {string} operation.type - Operation type
         * @param {string} operation.url - Request URL
         * @param {Object} operation.options - Request options
         * @param {Function} operation.callback - Callback function
         * @param {number} operation.priority - Operation priority (1-10, 10 = highest)
         */
        queueOperation(operation) {
            const queueItem = {
                id: this.generateOperationId(),
                timestamp: Date.now(),
                retries: 0,
                priority: operation.priority || 5,
                ...operation
            };
            
            this.operationQueue.push(queueItem);
            
            // Sort by priority (highest first)
            this.operationQueue.sort((a, b) => b.priority - a.priority);
            
            console.log(`[OfflineHandler] Queued operation: ${operation.type}`);
            
            this.showStatus('Changes saved locally. They will sync when connection is restored.', 'info');
        }

        /**
         * Process queued operations when connection is restored
         */
        async processQueuedOperations() {
            if (this.operationQueue.length === 0) {
                return;
            }
            
            console.log(`[OfflineHandler] Processing ${this.operationQueue.length} queued operations`);
            
            const successfulOperations = [];
            const failedOperations = [];
            
            for (const operation of this.operationQueue) {
                try {
                    await this.retryOperation(operation);
                    successfulOperations.push(operation);
                } catch (error) {
                    console.error(`[OfflineHandler] Failed to process operation:`, error);
                    failedOperations.push(operation);
                }
            }
            
            // Remove successful operations from queue
            this.operationQueue = this.operationQueue.filter(op => 
                !successfulOperations.includes(op)
            );
            
            if (successfulOperations.length > 0) {
                this.showStatus(`${successfulOperations.length} changes synced successfully`, 'success');
            }
            
            if (failedOperations.length > 0) {
                this.showStatus(`${failedOperations.length} changes failed to sync`, 'error');
            }
        }

        /**
         * Retry a queued operation
         * @param {Object} operation - Operation to retry
         */
        async retryOperation(operation) {
            const maxRetries = this.retryAttempts.get(operation.id) || this.maxRetryAttempts;
            
            if (operation.retries >= maxRetries) {
                throw new Error(`Operation failed after ${maxRetries} retries`);
            }
            
            try {
                // Attempt the operation
                const response = await fetch(operation.url, {
                    ...operation.options,
                    signal: AbortSignal.timeout(10000) // 10 second timeout
                });
                
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}: ${response.statusText}`);
                }
                
                // Execute callback if provided
                if (operation.callback && typeof operation.callback === 'function') {
                    const data = await response.json();
                    operation.callback(null, data);
                }
                
                return response;
            } catch (error) {
                operation.retries++;
                
                if (operation.retries < maxRetries) {
                    // Exponential backoff
                    const delay = this.retryDelay * Math.pow(2, operation.retries - 1);
                    await new Promise(resolve => setTimeout(resolve, delay));
                    return this.retryOperation(operation);
                } else {
                    // Execute error callback if provided
                    if (operation.callback && typeof operation.callback === 'function') {
                        operation.callback(error, null);
                    }
                    throw error;
                }
            }
        }

        /**
         * Generate unique operation ID
         */
        generateOperationId() {
            return `op_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`;
        }

        /**
         * Subscribe to network status changes
         * @param {Function} callback - Callback function (status) => {}
         * @returns {Function} Unsubscribe function
         */
        subscribe(callback) {
            this.subscribers.add(callback);
            
            // Return unsubscribe function
            return () => {
                this.subscribers.delete(callback);
            };
        }

        /**
         * Notify subscribers of status changes
         * @param {string} status - Status ('online' or 'offline')
         */
        notifySubscribers(status) {
            this.subscribers.forEach(callback => {
                try {
                    callback(status);
                } catch (error) {
                    console.error('[OfflineHandler] Error in subscriber callback:', error);
                }
            });
        }

        /**
         * Enhanced fetch with offline support
         * @param {string} url - Request URL
         * @param {Object} options - Fetch options
         * @returns {Promise} Response or cached data
         */
        async fetch(url, options = {}) {
            const cacheKey = this.generateCacheKey(url, options);
            
            if (this.isOnline) {
                try {
                    const response = await fetch(url, options);
                    
                    // Cache successful GET requests
                    if (response.ok && (options.method || 'GET') === 'GET') {
                        const data = await response.clone().json();
                        this.cacheData(cacheKey, {
                            status: response.status,
                            statusText: response.statusText,
                            headers: Object.fromEntries(response.headers.entries()),
                            data: data
                        });
                    }
                    
                    return response;
                } catch (error) {
                    // Network error - fall back to cache for GET requests
                    if ((options.method || 'GET') === 'GET') {
                        const cachedData = this.getCachedData(cacheKey);
                        if (cachedData) {
                            console.log(`[OfflineHandler] Using cached data for: ${url}`);
                            return this.createMockResponse(cachedData);
                        }
                    }
                    throw error;
                }
            } else {
                // Offline mode
                if ((options.method || 'GET') === 'GET') {
                    // Try to return cached data
                    const cachedData = this.getCachedData(cacheKey);
                    if (cachedData) {
                        console.log(`[OfflineHandler] Offline: Using cached data for: ${url}`);
                        return this.createMockResponse(cachedData);
                    } else {
                        throw new Error('No cached data available for this request');
                    }
                } else {
                    // Queue non-GET operations
                    this.queueOperation({
                        type: 'HTTP_REQUEST',
                        url: url,
                        options: options,
                        priority: this.getOperationPriority(url, options)
                    });
                    
                    // Return a mock response for offline operations
                    return this.createMockResponse({
                        status: 202,
                        statusText: 'Accepted (Queued for sync)',
                        data: { message: 'Operation queued for sync when connection is restored' }
                    });
                }
            }
        }

        /**
         * Create mock response from cached data
         * @param {Object} cachedData - Cached response data
         * @returns {Response} Mock response object
         */
        createMockResponse(cachedData) {
            return new Response(JSON.stringify(cachedData.data), {
                status: cachedData.status,
                statusText: cachedData.statusText,
                headers: new Headers(cachedData.headers || {})
            });
        }

        /**
         * Generate cache key from URL and options
         * @param {string} url - Request URL
         * @param {Object} options - Request options
         * @returns {string} Cache key
         */
        generateCacheKey(url, options) {
            const method = options.method || 'GET';
            const params = new URL(url, window.location.origin).search;
            return `${method}_${url.split('?')[0]}${params}`;
        }

        /**
         * Get operation priority based on URL and method
         * @param {string} url - Request URL
         * @param {Object} options - Request options
         * @returns {number} Priority (1-10)
         */
        getOperationPriority(url, options) {
            const method = options.method || 'GET';
            
            // High priority for user data operations
            if (url.includes('/api/library') || url.includes('/api/auth')) {
                return method === 'DELETE' ? 9 : 8;
            }
            
            // Medium priority for book operations
            if (url.includes('/api/books')) {
                return 6;
            }
            
            // Lower priority for search and metadata
            if (url.includes('/api/search') || url.includes('/api/categories')) {
                return 4;
            }
            
            return 5; // Default priority
        }

        /**
         * Refresh critical data when connection is restored
         */
        async refreshCriticalData() {
            const criticalEndpoints = [
                '/api/library?size=20&page=0',
                '/api/categories',
                '/api/auth/user'
            ];
            
            for (const endpoint of criticalEndpoints) {
                try {
                    const response = await fetch(endpoint);
                    if (response.ok) {
                        const data = await response.json();
                        this.cacheData(this.generateCacheKey(endpoint, {}), {
                            status: response.status,
                            statusText: response.statusText,
                            headers: Object.fromEntries(response.headers.entries()),
                            data: data
                        });
                    }
                } catch (error) {
                    console.warn(`[OfflineHandler] Failed to refresh data for ${endpoint}:`, error);
                }
            }
        }

        /**
         * Register service worker for advanced offline capabilities
         */
        async registerServiceWorker() {
            try {
                const registration = await navigator.serviceWorker.register('/sw.js');
                console.log('[OfflineHandler] Service Worker registered:', registration);
                
                // Handle service worker updates
                registration.addEventListener('updatefound', () => {
                    const newWorker = registration.installing;
                    newWorker.addEventListener('statechange', () => {
                        if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
                            this.showStatus('App updated. Refresh to use the latest version.', 'info', true);
                        }
                    });
                });
            } catch (error) {
                console.log('[OfflineHandler] Service Worker registration failed:', error);
            }
        }

        /**
         * Intercept fetch requests for automatic caching
         */
        interceptFetch() {
            // This would typically be handled by service worker
            // But we can provide a fallback for environments without SW
            if (!navigator.serviceWorker.controller) {
                const originalFetch = window.fetch;
                window.fetch = async (url, options) => {
                    return this.fetch(url, options);
                };
            }
        }

        /**
         * Show status message to user
         * @param {string} message - Status message
         * @param {string} type - Message type ('success', 'error', 'warning', 'info')
         * @param {boolean} persistent - Whether message should persist
         */
        showStatus(message, type = 'info', persistent = false) {
            if (this.notificationService) {
                const duration = persistent ? 0 : 5000; // 0 = persistent
                
                switch (type) {
                    case 'success':
                        this.notificationService.showSuccess(message, duration);
                        break;
                    case 'error':
                        this.notificationService.showError(message, duration);
                        break;
                    case 'warning':
                        this.notificationService.showWarning(message, duration);
                        break;
                    default:
                        this.notificationService.showInfo(message, duration);
                }
            } else {
                // Fallback to console
                console.log(`[OfflineHandler] ${type.toUpperCase()}: ${message}`);
            }
        }

        /**
         * Get offline status information
         * @returns {Object} Status information
         */
        getStatus() {
            return {
                isOnline: this.isOnline,
                cacheSize: this.cache.size,
                queuedOperations: this.operationQueue.length,
                lastCheck: new Date().toISOString()
            };
        }

        /**
         * Clear all cached data
         */
        clearCache() {
            this.cache.clear();
            
            // Clear from localStorage
            for (let i = localStorage.length - 1; i >= 0; i--) {
                const key = localStorage.key(i);
                if (key && key.startsWith(this.cachePrefix)) {
                    localStorage.removeItem(key);
                }
            }
            
            console.log('[OfflineHandler] Cache cleared');
        }

        /**
         * Force connection check
         */
        forceConnectionCheck() {
            this.checkConnection();
        }

        /**
         * Destroy offline handler (cleanup)
         */
        destroy() {
            this.stopConnectionMonitoring();
            window.removeEventListener('online', this.handleOnlineMode);
            window.removeEventListener('offline', this.handleOfflineMode);
            this.subscribers.clear();
            this.cache.clear();
            this.operationQueue = [];
        }

        /**
         * Add offline handler styles
         */
        addStyles() {
            if (document.getElementById('offline-handler-styles')) return;

            const styles = document.createElement('style');
            styles.id = 'offline-handler-styles';
            styles.textContent = `
                .da-offline-indicator {
                    position: fixed;
                    top: 0;
                    left: 0;
                    right: 0;
                    background: var(--da-warning-bg, #fff3cd);
                    border-bottom: 2px solid var(--da-warning-border, #ffeaa7);
                    color: var(--da-warning-text, #856404);
                    padding: var(--da-space-sm, 0.5rem) var(--da-space-md, 1rem);
                    font-size: var(--da-font-size-sm, 0.875rem);
                    font-weight: 500;
                    text-align: center;
                    z-index: var(--da-z-banner, 9998);
                    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
                    backdrop-filter: blur(8px);
                    animation: da-offline-slide-down 0.3s ease;
                }

                .da-offline-indicator.success {
                    background: var(--da-success-bg, #d1edff);
                    border-color: var(--da-success-border, #0066cc);
                    color: var(--da-success-text, #004085);
                }

                .da-offline-indicator.error {
                    background: var(--da-danger-bg, #f8d7da);
                    border-color: var(--da-danger-border, #dc3545);
                    color: var(--da-danger-text, #721c24);
                }

                .da-offline-indicator .da-close-btn {
                    position: absolute;
                    right: var(--da-space-md, 1rem);
                    top: 50%;
                    transform: translateY(-50%);
                    background: none;
                    border: none;
                    color: inherit;
                    font-size: 1.2rem;
                    cursor: pointer;
                    opacity: 0.7;
                    transition: opacity 0.2s ease;
                }

                .da-offline-indicator .da-close-btn:hover {
                    opacity: 1;
                }

                .da-cached-indicator {
                    display: inline-flex;
                    align-items: center;
                    gap: var(--da-space-xs, 0.25rem);
                    font-size: var(--da-font-size-xs, 0.75rem);
                    color: var(--da-text-muted, #6c757d);
                    margin-left: var(--da-space-xs, 0.25rem);
                }

                .da-cached-indicator::before {
                    content: "📱";
                    font-size: 1em;
                }

                .da-syncing-indicator {
                    display: inline-flex;
                    align-items: center;
                    gap: var(--da-space-xs, 0.25rem);
                    font-size: var(--da-font-size-xs, 0.75rem);
                    color: var(--da-accent-blue, #0066cc);
                    animation: da-pulse-subtle 2s infinite;
                }

                .da-syncing-indicator::before {
                    content: "🔄";
                    font-size: 1em;
                    animation: da-spin 1s linear infinite;
                }

                @keyframes da-offline-slide-down {
                    from {
                        transform: translateY(-100%);
                        opacity: 0;
                    }
                    to {
                        transform: translateY(0);
                        opacity: 1;
                    }
                }

                @keyframes da-pulse-subtle {
                    0%, 100% {
                        opacity: 1;
                    }
                    50% {
                        opacity: 0.7;
                    }
                }

                @keyframes da-spin {
                    from {
                        transform: rotate(0deg);
                    }
                    to {
                        transform: rotate(360deg);
                    }
                }

                /* Dark mode support */
                @media (prefers-color-scheme: dark) {
                    .da-offline-indicator {
                        background: rgba(255, 243, 205, 0.1);
                        border-color: rgba(255, 234, 167, 0.3);
                        color: #ffeaa7;
                    }
                    
                    .da-offline-indicator.success {
                        background: rgba(209, 237, 255, 0.1);
                        border-color: rgba(0, 102, 204, 0.3);
                        color: #74b9ff;
                    }
                    
                    .da-offline-indicator.error {
                        background: rgba(248, 215, 218, 0.1);
                        border-color: rgba(220, 53, 69, 0.3);
                        color: #ff7675;
                    }
                }

                /* Reduced motion support */
                @media (prefers-reduced-motion: reduce) {
                    .da-offline-indicator,
                    .da-syncing-indicator::before,
                    .da-pulse-subtle {
                        animation: none !important;
                    }
                }
            `;

            document.head.appendChild(styles);
        }
    }

    /**
     * Utility function to enhance existing services with offline support
     */
    function enhanceServiceWithOffline(service, offlineHandler) {
        if (!service || !offlineHandler) return service;

        const originalMethods = {};

        // Enhance common API methods
        ['getUserLibrary', 'searchBooks', 'getBookDetails', 'addBookToLibrary'].forEach(methodName => {
            if (typeof service[methodName] === 'function') {
                originalMethods[methodName] = service[methodName].bind(service);
                
                service[methodName] = async function(...args) {
                    try {
                        // Try original method first
                        return await originalMethods[methodName](...args);
                    } catch (error) {
                        // If offline, try to provide cached fallback
                        if (!offlineHandler.isOnline && methodName.startsWith('get')) {
                            const cacheKey = `${methodName}_${JSON.stringify(args)}`;
                            const cachedData = offlineHandler.getCachedData(cacheKey);
                            
                            if (cachedData) {
                                console.log(`[OfflineHandler] Using cached data for ${methodName}`);
                                return {
                                    success: true,
                                    data: cachedData,
                                    message: 'Data loaded from cache (offline)',
                                    offline: true
                                };
                            }
                        }
                        
                        throw error;
                    }
                };
            }
        });

        return service;
    }

    // Initialize global offline handler
    const offlineHandler = new OfflineHandler();

    // Enhance existing services if they exist
    if (window.libraryService) {
        window.libraryService = enhanceServiceWithOffline(window.libraryService, offlineHandler);
    }

    if (window.bookSearchService) {
        window.bookSearchService = enhanceServiceWithOffline(window.bookSearchService, offlineHandler);
    }

    // Export API
    const OfflineUtils = {
        handler: offlineHandler,
        enhanceService: (service) => enhanceServiceWithOffline(service, offlineHandler),
        
        // Convenience methods
        isOnline: () => offlineHandler.isOnline,
        getStatus: () => offlineHandler.getStatus(),
        subscribe: (callback) => offlineHandler.subscribe(callback),
        cache: (key, data, expiry) => offlineHandler.cacheData(key, data, expiry),
        getCached: (key) => offlineHandler.getCachedData(key),
        queue: (operation) => offlineHandler.queueOperation(operation),
        clearCache: () => offlineHandler.clearCache(),
        checkConnection: () => offlineHandler.forceConnectionCheck()
    };

    // Export for different module systems
    if (typeof module !== 'undefined' && module.exports) {
        module.exports = OfflineUtils;
    } else {
        window.OfflineUtils = OfflineUtils;
    }

    // Auto-enhance UIUtils error handler if available
    if (window.UIUtils && window.UIUtils.error) {
        window.UIUtils.error.setNotificationService = function(notificationService) {
            offlineHandler.setNotificationService(notificationService);
        };
    }

})();