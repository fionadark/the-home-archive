/**
 * UI Utilities for The Home Archive - Dark Academia Library
 * Provides reusable loading states, error handling, and UI helper functions
 * Maintains dark academia aesthetic and accessibility standards
 */

(function() {
    'use strict';

    /**
     * LoadingManager - Centralized loading state management
     */
    class LoadingManager {
        constructor() {
            this.activeLoaders = new Map();
            this.addStyles();
        }

        /**
         * Show loading state for a specific element or globally
         * @param {string|HTMLElement} target - Target element ID, element, or 'global' for page-wide loading
         * @param {Object} options - Loading configuration
         * @param {string} options.text - Loading text (default: 'Loading...')
         * @param {string} options.size - Size: 'small', 'medium', 'large' (default: 'medium')
         * @param {string} options.type - Type: 'spinner', 'dots', 'pulse', 'skeleton' (default: 'spinner')
         * @param {boolean} options.overlay - Whether to show overlay (default: false, true for global)
         * @param {boolean} options.disableInteraction - Disable user interaction (default: true)
         * @returns {Object} Loading instance with methods to update or hide
         */
        show(target, options = {}) {
            const config = {
                text: 'Loading...',
                size: 'medium',
                type: 'spinner',
                overlay: target === 'global',
                disableInteraction: true,
                ...options
            };

            const targetElement = this.resolveTarget(target);
            const loaderId = this.generateLoaderId(target);

            // Remove existing loader if present
            this.hide(target);

            // Create loading instance
            const loader = new LoadingInstance(targetElement, config, loaderId);
            this.activeLoaders.set(loaderId, loader);

            loader.show();
            return loader;
        }

        /**
         * Hide loading state
         * @param {string|HTMLElement} target - Target to hide loading from
         */
        hide(target) {
            const loaderId = this.generateLoaderId(target);
            const loader = this.activeLoaders.get(loaderId);
            
            if (loader) {
                loader.hide();
                this.activeLoaders.delete(loaderId);
            }
        }

        /**
         * Hide all active loaders
         */
        hideAll() {
            this.activeLoaders.forEach(loader => loader.hide());
            this.activeLoaders.clear();
        }

        /**
         * Check if target is currently loading
         * @param {string|HTMLElement} target - Target to check
         * @returns {boolean} Whether target is loading
         */
        isLoading(target) {
            const loaderId = this.generateLoaderId(target);
            return this.activeLoaders.has(loaderId);
        }

        /**
         * Resolve target to element
         */
        resolveTarget(target) {
            if (target === 'global') {
                return document.body;
            }
            if (typeof target === 'string') {
                return document.getElementById(target) || document.querySelector(target);
            }
            return target;
        }

        /**
         * Generate unique loader ID
         */
        generateLoaderId(target) {
            if (target === 'global') return 'global-loader';
            if (typeof target === 'string') return `loader-${target}`;
            if (target && target.id) return `loader-${target.id}`;
            return `loader-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`;
        }

        /**
         * Add loading component styles
         */
        addStyles() {
            if (document.getElementById('ui-utils-loading-styles')) return;

            const styles = document.createElement('style');
            styles.id = 'ui-utils-loading-styles';
            styles.textContent = `
                .da-loading-overlay {
                    position: absolute;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: var(--da-bg-overlay, rgba(33, 37, 41, 0.85));
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    z-index: var(--da-z-loading, 1000);
                    border-radius: inherit;
                }

                .da-loading-overlay.global {
                    position: fixed;
                    z-index: var(--da-z-modal, 9999);
                    background: var(--da-bg-overlay, rgba(33, 37, 41, 0.9));
                }

                .da-loading-content {
                    display: flex;
                    flex-direction: column;
                    align-items: center;
                    gap: var(--da-space-md, 1rem);
                    color: var(--da-text-light, #f8f9fa);
                    text-align: center;
                    padding: var(--da-space-lg, 1.5rem);
                    background: var(--da-bg-card, rgba(0, 0, 0, 0.8));
                    border-radius: var(--da-radius-lg, 0.5rem);
                    backdrop-filter: blur(8px);
                    border: 1px solid var(--da-border-accent, rgba(212, 175, 55, 0.3));
                }

                .da-loading-spinner {
                    border: 3px solid var(--da-border-muted, rgba(255, 255, 255, 0.2));
                    border-top: 3px solid var(--da-accent-gold, #d4af37);
                    border-radius: 50%;
                    animation: da-spin 1s linear infinite;
                }

                .da-loading-spinner.small {
                    width: 20px;
                    height: 20px;
                    border-width: 2px;
                }

                .da-loading-spinner.medium {
                    width: 40px;
                    height: 40px;
                }

                .da-loading-spinner.large {
                    width: 60px;
                    height: 60px;
                    border-width: 4px;
                }

                .da-loading-dots {
                    display: flex;
                    gap: 0.5rem;
                }

                .da-loading-dots .dot {
                    width: 8px;
                    height: 8px;
                    background: var(--da-accent-gold, #d4af37);
                    border-radius: 50%;
                    animation: da-dot-pulse 1.4s ease-in-out infinite both;
                }

                .da-loading-dots.small .dot {
                    width: 6px;
                    height: 6px;
                }

                .da-loading-dots.large .dot {
                    width: 12px;
                    height: 12px;
                }

                .da-loading-dots .dot:nth-child(1) { animation-delay: -0.32s; }
                .da-loading-dots .dot:nth-child(2) { animation-delay: -0.16s; }
                .da-loading-dots .dot:nth-child(3) { animation-delay: 0s; }

                .da-loading-pulse {
                    background: var(--da-accent-gold, #d4af37);
                    border-radius: 50%;
                    animation: da-pulse 1.5s ease-in-out infinite;
                }

                .da-loading-pulse.small {
                    width: 20px;
                    height: 20px;
                }

                .da-loading-pulse.medium {
                    width: 40px;
                    height: 40px;
                }

                .da-loading-pulse.large {
                    width: 60px;
                    height: 60px;
                }

                .da-loading-text {
                    font-family: var(--da-font-heading, 'Playfair Display', serif);
                    font-size: var(--da-font-size-sm, 0.875rem);
                    font-weight: 500;
                    color: var(--da-text-light, #f8f9fa);
                    margin: 0;
                }

                .da-loading-text.small {
                    font-size: var(--da-font-size-xs, 0.75rem);
                }

                .da-loading-text.large {
                    font-size: var(--da-font-size-base, 1rem);
                }

                .da-skeleton {
                    background: linear-gradient(90deg, 
                        var(--da-bg-muted, #495057) 25%, 
                        var(--da-bg-hover, #6c757d) 50%, 
                        var(--da-bg-muted, #495057) 75%
                    );
                    background-size: 200% 100%;
                    animation: da-skeleton-loading 1.5s infinite;
                    border-radius: var(--da-radius-sm, 0.25rem);
                }

                .da-skeleton-line {
                    height: 1rem;
                    margin-bottom: 0.75rem;
                }

                .da-skeleton-line:last-child {
                    margin-bottom: 0;
                    width: 60%;
                }

                .da-skeleton-avatar {
                    width: 40px;
                    height: 40px;
                    border-radius: 50%;
                }

                .da-skeleton-card {
                    height: 200px;
                    width: 100%;
                }

                /* Disable interaction overlay */
                .da-loading-disabled {
                    pointer-events: none;
                    user-select: none;
                    opacity: 0.6;
                }

                /* Animations */
                @keyframes da-spin {
                    0% { transform: rotate(0deg); }
                    100% { transform: rotate(360deg); }
                }

                @keyframes da-dot-pulse {
                    0%, 80%, 100% {
                        transform: scale(0);
                    }
                    40% {
                        transform: scale(1);
                    }
                }

                @keyframes da-pulse {
                    0% {
                        transform: scale(0);
                        opacity: 1;
                    }
                    100% {
                        transform: scale(1);
                        opacity: 0;
                    }
                }

                @keyframes da-skeleton-loading {
                    0% {
                        background-position: -200% 0;
                    }
                    100% {
                        background-position: 200% 0;
                    }
                }

                /* Responsive adjustments */
                @media (max-width: 768px) {
                    .da-loading-content {
                        padding: var(--da-space-md, 1rem);
                        gap: var(--da-space-sm, 0.75rem);
                    }
                    
                    .da-loading-text {
                        font-size: var(--da-font-size-xs, 0.75rem);
                    }
                }

                /* Dark mode support */
                @media (prefers-color-scheme: dark) {
                    .da-loading-overlay {
                        background: rgba(0, 0, 0, 0.9);
                    }
                    
                    .da-loading-content {
                        background: rgba(13, 17, 23, 0.95);
                        border-color: rgba(212, 175, 55, 0.5);
                    }
                }

                /* Reduced motion support */
                @media (prefers-reduced-motion: reduce) {
                    .da-loading-spinner,
                    .da-loading-dots .dot,
                    .da-loading-pulse,
                    .da-skeleton {
                        animation-duration: 0.01s !important;
                        animation-iteration-count: 1 !important;
                    }
                }
            `;

            document.head.appendChild(styles);
        }
    }

    /**
     * LoadingInstance - Individual loading state
     */
    class LoadingInstance {
        constructor(targetElement, config, loaderId) {
            this.target = targetElement;
            this.config = config;
            this.loaderId = loaderId;
            this.overlay = null;
            this.originalPosition = null;
            this.originalOverflow = null;
        }

        show() {
            if (!this.target) return;

            // Store original styles
            this.originalPosition = getComputedStyle(this.target).position;
            this.originalOverflow = getComputedStyle(this.target).overflow;

            // Make target relative if static for absolute positioning
            if (this.originalPosition === 'static') {
                this.target.style.position = 'relative';
            }

            // Create overlay
            this.overlay = document.createElement('div');
            this.overlay.className = `da-loading-overlay ${this.config.overlay ? 'global' : ''}`;
            this.overlay.setAttribute('role', 'status');
            this.overlay.setAttribute('aria-live', 'polite');
            this.overlay.setAttribute('aria-label', this.config.text);

            // Create loading content
            const content = document.createElement('div');
            content.className = 'da-loading-content';

            // Add loading indicator
            const indicator = this.createIndicator();
            content.appendChild(indicator);

            // Add loading text
            if (this.config.text) {
                const text = document.createElement('p');
                text.className = `da-loading-text ${this.config.size}`;
                text.textContent = this.config.text;
                content.appendChild(text);
            }

            this.overlay.appendChild(content);

            // Disable interaction if requested
            if (this.config.disableInteraction) {
                this.target.classList.add('da-loading-disabled');
                // Prevent scrolling for global loaders
                if (this.config.overlay && this.target === document.body) {
                    this.target.style.overflow = 'hidden';
                }
            }

            // Add to target
            this.target.appendChild(this.overlay);

            // Animate in
            requestAnimationFrame(() => {
                this.overlay.style.opacity = '0';
                this.overlay.style.transition = 'opacity 0.2s ease';
                requestAnimationFrame(() => {
                    this.overlay.style.opacity = '1';
                });
            });
        }

        hide() {
            if (!this.overlay || !this.target) return;

            // Animate out
            this.overlay.style.transition = 'opacity 0.15s ease';
            this.overlay.style.opacity = '0';

            setTimeout(() => {
                // Remove overlay
                if (this.overlay && this.overlay.parentNode) {
                    this.overlay.parentNode.removeChild(this.overlay);
                }

                // Restore original styles
                if (this.originalPosition === 'static') {
                    this.target.style.position = '';
                }

                if (this.config.disableInteraction) {
                    this.target.classList.remove('da-loading-disabled');
                    if (this.config.overlay && this.target === document.body) {
                        this.target.style.overflow = this.originalOverflow;
                    }
                }
            }, 150);
        }

        updateText(newText) {
            const textElement = this.overlay?.querySelector('.da-loading-text');
            if (textElement) {
                textElement.textContent = newText;
                this.overlay.setAttribute('aria-label', newText);
            }
        }

        createIndicator() {
            const indicator = document.createElement('div');

            switch (this.config.type) {
                case 'dots':
                    indicator.className = `da-loading-dots ${this.config.size}`;
                    indicator.innerHTML = '<div class="dot"></div><div class="dot"></div><div class="dot"></div>';
                    break;

                case 'pulse':
                    indicator.className = `da-loading-pulse ${this.config.size}`;
                    break;

                case 'skeleton':
                    indicator.className = 'da-skeleton da-skeleton-card';
                    break;

                case 'spinner':
                default:
                    indicator.className = `da-loading-spinner ${this.config.size}`;
                    break;
            }

            return indicator;
        }
    }

    /**
     * ErrorHandler - Centralized error handling and display
     */
    class ErrorHandler {
        constructor() {
            this.notificationService = null;
            this.defaultDuration = 7000;
            this.addStyles();
        }

        /**
         * Set notification service for error display
         * @param {Object} notificationService - Notification service instance
         */
        setNotificationService(notificationService) {
            this.notificationService = notificationService;
        }

        /**
         * Handle and display error
         * @param {Error|string|Object} error - Error to handle
         * @param {Object} options - Error handling options
         * @param {string} options.context - Context where error occurred
         * @param {boolean} options.silent - Don't show notification (default: false)
         * @param {boolean} options.log - Log to console (default: true)
         * @param {Function} options.fallback - Fallback function to execute
         * @param {HTMLElement} options.target - Target element for inline error display
         * @returns {Object} Normalized error object
         */
        handleError(error, options = {}) {
            const config = {
                context: 'Application',
                silent: false,
                log: true,
                fallback: null,
                target: null,
                ...options
            };

            // Normalize error
            const normalizedError = this.normalizeError(error, config.context);

            // Log error
            if (config.log) {
                console.error(`[${config.context}] Error:`, normalizedError.original);
            }

            // Show error notification or inline display
            if (!config.silent) {
                if (config.target) {
                    this.showInlineError(config.target, normalizedError.userMessage);
                } else if (this.notificationService) {
                    this.notificationService.showError(normalizedError.userMessage, this.defaultDuration);
                } else {
                    this.showFallbackError(normalizedError.userMessage);
                }
            }

            // Execute fallback
            if (config.fallback && typeof config.fallback === 'function') {
                try {
                    config.fallback(normalizedError);
                } catch (fallbackError) {
                    console.error('Error in fallback handler:', fallbackError);
                }
            }

            return normalizedError;
        }

        /**
         * Handle API errors specifically
         * @param {Response|Error} error - API error response or network error
         * @param {Object} options - Error handling options
         */
        async handleApiError(error, options = {}) {
            let apiError;

            if (error instanceof Response) {
                // Handle HTTP response errors
                try {
                    const errorData = await error.json();
                    apiError = {
                        status: error.status,
                        statusText: error.statusText,
                        message: errorData.message || errorData.error || 'An error occurred',
                        details: errorData.details || null,
                        original: error
                    };
                } catch (parseError) {
                    apiError = {
                        status: error.status,
                        statusText: error.statusText,
                        message: `Server error (${error.status})`,
                        details: null,
                        original: error
                    };
                }
            } else {
                // Handle network errors
                apiError = {
                    status: 0,
                    statusText: 'Network Error',
                    message: error.message || 'Unable to connect to server',
                    details: null,
                    original: error
                };
            }

            return this.handleError(apiError, {
                context: 'API',
                ...options
            });
        }

        /**
         * Handle form validation errors
         * @param {Object} validationErrors - Field validation errors
         * @param {HTMLElement} formElement - Form element to display errors on
         */
        handleValidationErrors(validationErrors, formElement) {
            // Clear existing errors
            this.clearValidationErrors(formElement);

            Object.keys(validationErrors).forEach(fieldName => {
                const field = formElement.querySelector(`[name="${fieldName}"]`);
                const errorMessage = validationErrors[fieldName];

                if (field) {
                    this.showFieldError(field, errorMessage);
                }
            });
        }

        /**
         * Clear validation errors from form
         * @param {HTMLElement} formElement - Form element
         */
        clearValidationErrors(formElement) {
            const errorElements = formElement.querySelectorAll('.da-field-error');
            errorElements.forEach(el => el.remove());

            const invalidFields = formElement.querySelectorAll('.da-field-invalid');
            invalidFields.forEach(field => {
                field.classList.remove('da-field-invalid');
                field.removeAttribute('aria-describedby');
            });
        }

        /**
         * Show error for specific form field
         * @param {HTMLElement} field - Form field element
         * @param {string} message - Error message
         */
        showFieldError(field, message) {
            // Mark field as invalid
            field.classList.add('da-field-invalid');

            // Create error element
            const errorId = `error-${field.name || Date.now()}`;
            const errorElement = document.createElement('div');
            errorElement.id = errorId;
            errorElement.className = 'da-field-error';
            errorElement.setAttribute('role', 'alert');
            errorElement.textContent = message;

            // Link field to error
            field.setAttribute('aria-describedby', errorId);

            // Insert error after field
            field.parentNode.insertBefore(errorElement, field.nextSibling);
        }

        /**
         * Show inline error in target element
         * @param {HTMLElement} target - Target element
         * @param {string} message - Error message
         */
        showInlineError(target, message) {
            // Remove existing inline errors
            const existingError = target.querySelector('.da-inline-error');
            if (existingError) {
                existingError.remove();
            }

            // Create error element
            const errorElement = document.createElement('div');
            errorElement.className = 'da-inline-error';
            errorElement.setAttribute('role', 'alert');
            errorElement.innerHTML = `
                <div class="da-inline-error-content">
                    <i class="fas fa-exclamation-circle" aria-hidden="true"></i>
                    <span class="da-inline-error-text">${this.escapeHtml(message)}</span>
                </div>
            `;

            // Add to target
            target.insertBefore(errorElement, target.firstChild);

            // Auto-remove after duration
            setTimeout(() => {
                if (errorElement && errorElement.parentNode) {
                    errorElement.style.transition = 'opacity 0.3s ease';
                    errorElement.style.opacity = '0';
                    setTimeout(() => {
                        if (errorElement && errorElement.parentNode) {
                            errorElement.parentNode.removeChild(errorElement);
                        }
                    }, 300);
                }
            }, this.defaultDuration);
        }

        /**
         * Show fallback error when no notification service is available
         * @param {string} message - Error message
         */
        showFallbackError(message) {
            // Use browser alert as last resort
            alert(`Error: ${message}`);
        }

        /**
         * Normalize error to consistent format
         * @param {*} error - Error to normalize
         * @param {string} context - Error context
         * @returns {Object} Normalized error
         */
        normalizeError(error, context) {
            let userMessage = 'An unexpected error occurred';
            let technicalMessage = error;

            if (error instanceof Error) {
                userMessage = this.getUserFriendlyMessage(error.message, context);
                technicalMessage = error.message;
            } else if (typeof error === 'string') {
                userMessage = this.getUserFriendlyMessage(error, context);
                technicalMessage = error;
            } else if (error && error.message) {
                userMessage = this.getUserFriendlyMessage(error.message, context);
                technicalMessage = error.message;
            } else if (error && error.status) {
                userMessage = this.getHttpErrorMessage(error.status);
                technicalMessage = `HTTP ${error.status}: ${error.statusText}`;
            }

            return {
                userMessage,
                technicalMessage,
                context,
                timestamp: new Date().toISOString(),
                original: error
            };
        }

        /**
         * Convert technical error message to user-friendly message
         * @param {string} message - Technical error message
         * @param {string} context - Error context
         * @returns {string} User-friendly message
         */
        getUserFriendlyMessage(message, context) {
            const messageLower = message.toLowerCase();

            // Network errors
            if (messageLower.includes('fetch') || messageLower.includes('network')) {
                return 'Unable to connect to the server. Please check your internet connection.';
            }

            // Authentication errors
            if (messageLower.includes('unauthorized') || messageLower.includes('401')) {
                return 'Your session has expired. Please log in again.';
            }

            // Permission errors
            if (messageLower.includes('forbidden') || messageLower.includes('403')) {
                return 'You do not have permission to perform this action.';
            }

            // Not found errors
            if (messageLower.includes('not found') || messageLower.includes('404')) {
                return 'The requested item was not found.';
            }

            // Server errors
            if (messageLower.includes('500') || messageLower.includes('server error')) {
                return 'A server error occurred. Please try again later.';
            }

            // Validation errors
            if (messageLower.includes('validation') || messageLower.includes('invalid')) {
                return 'Please check your input and try again.';
            }

            // Default context-specific messages
            switch (context.toLowerCase()) {
                case 'api':
                    return 'Unable to complete the request. Please try again.';
                case 'authentication':
                    return 'Authentication failed. Please check your credentials.';
                case 'library':
                    return 'Unable to load your library. Please refresh the page.';
                case 'search':
                    return 'Search failed. Please try again.';
                default:
                    return message || 'An unexpected error occurred. Please try again.';
            }
        }

        /**
         * Get user-friendly message for HTTP status codes
         * @param {number} status - HTTP status code
         * @returns {string} User-friendly message
         */
        getHttpErrorMessage(status) {
            switch (status) {
                case 400:
                    return 'Invalid request. Please check your input.';
                case 401:
                    return 'Your session has expired. Please log in again.';
                case 403:
                    return 'You do not have permission to perform this action.';
                case 404:
                    return 'The requested item was not found.';
                case 409:
                    return 'This item already exists or conflicts with existing data.';
                case 422:
                    return 'Please check your input and try again.';
                case 429:
                    return 'Too many requests. Please wait a moment and try again.';
                case 500:
                    return 'A server error occurred. Please try again later.';
                case 502:
                case 503:
                    return 'The service is temporarily unavailable. Please try again later.';
                case 504:
                    return 'The request timed out. Please try again.';
                default:
                    return `An error occurred (${status}). Please try again.`;
            }
        }

        /**
         * Escape HTML to prevent XSS
         * @param {string} unsafe - Unsafe string
         * @returns {string} Safe string
         */
        escapeHtml(unsafe) {
            if (typeof unsafe !== 'string') return '';
            return unsafe
                .replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;")
                .replace(/'/g, "&#039;");
        }

        /**
         * Add error handling styles
         */
        addStyles() {
            if (document.getElementById('ui-utils-error-styles')) return;

            const styles = document.createElement('style');
            styles.id = 'ui-utils-error-styles';
            styles.textContent = `
                .da-field-error {
                    color: var(--da-danger-color, #dc3545);
                    font-size: var(--da-font-size-xs, 0.75rem);
                    margin-top: var(--da-space-xs, 0.25rem);
                    margin-bottom: var(--da-space-sm, 0.5rem);
                    display: flex;
                    align-items: center;
                    gap: var(--da-space-xs, 0.25rem);
                }

                .da-field-error::before {
                    content: "⚠";
                    font-size: var(--da-font-size-sm, 0.875rem);
                }

                .da-field-invalid {
                    border-color: var(--da-danger-color, #dc3545) !important;
                    box-shadow: 0 0 0 0.125rem var(--da-danger-shadow, rgba(220, 53, 69, 0.25)) !important;
                }

                .da-inline-error {
                    background: var(--da-danger-bg, #f8d7da);
                    border: 1px solid var(--da-danger-border, #f5c6cb);
                    border-radius: var(--da-radius-md, 0.375rem);
                    padding: var(--da-space-md, 1rem);
                    margin-bottom: var(--da-space-md, 1rem);
                    color: var(--da-danger-text, #721c24);
                    animation: da-error-slide-in 0.3s ease;
                }

                .da-inline-error-content {
                    display: flex;
                    align-items: center;
                    gap: var(--da-space-sm, 0.5rem);
                }

                .da-inline-error-text {
                    flex: 1;
                    font-size: var(--da-font-size-sm, 0.875rem);
                    line-height: 1.4;
                }

                @keyframes da-error-slide-in {
                    from {
                        opacity: 0;
                        transform: translateY(-10px);
                    }
                    to {
                        opacity: 1;
                        transform: translateY(0);
                    }
                }

                /* Dark mode support */
                @media (prefers-color-scheme: dark) {
                    .da-inline-error {
                        background: rgba(220, 53, 69, 0.1);
                        border-color: rgba(220, 53, 69, 0.3);
                        color: #f8d7da;
                    }
                }
            `;

            document.head.appendChild(styles);
        }
    }

    /**
     * StateManager - Manages UI state transitions
     */
    class StateManager {
        constructor() {
            this.states = new Map();
            this.transitions = new Map();
        }

        /**
         * Register a state handler
         * @param {string} name - State name
         * @param {Function} handler - State handler function
         */
        registerState(name, handler) {
            this.states.set(name, handler);
        }

        /**
         * Set state for an element
         * @param {HTMLElement} element - Target element
         * @param {string} state - State name
         * @param {*} data - State data
         */
        setState(element, state, data = null) {
            const handler = this.states.get(state);
            if (handler && element) {
                handler(element, data);
                element.setAttribute('data-ui-state', state);
            }
        }

        /**
         * Get current state of element
         * @param {HTMLElement} element - Target element
         * @returns {string|null} Current state
         */
        getState(element) {
            return element ? element.getAttribute('data-ui-state') : null;
        }

        /**
         * Register predefined states
         */
        registerDefaultStates() {
            // Loading state
            this.registerState('loading', (element, data) => {
                element.classList.add('da-state-loading');
                element.setAttribute('aria-busy', 'true');
                if (data && data.disabled) {
                    element.disabled = true;
                }
            });

            // Error state
            this.registerState('error', (element, data) => {
                element.classList.add('da-state-error');
                element.setAttribute('aria-invalid', 'true');
                if (data && data.message) {
                    element.setAttribute('aria-describedby', 'error-message');
                }
            });

            // Success state
            this.registerState('success', (element) => {
                element.classList.add('da-state-success');
                element.setAttribute('aria-invalid', 'false');
            });

            // Disabled state
            this.registerState('disabled', (element) => {
                element.classList.add('da-state-disabled');
                element.disabled = true;
                element.setAttribute('aria-disabled', 'true');
            });

            // Normal state
            this.registerState('normal', (element) => {
                element.classList.remove('da-state-loading', 'da-state-error', 'da-state-success', 'da-state-disabled');
                element.disabled = false;
                element.removeAttribute('aria-busy');
                element.removeAttribute('aria-invalid');
                element.removeAttribute('aria-disabled');
            });
        }
    }

    // Initialize managers
    const loadingManager = new LoadingManager();
    const errorHandler = new ErrorHandler();
    const stateManager = new StateManager();
    stateManager.registerDefaultStates();

    // Export API
    const UIUtils = {
        // Loading management
        loading: {
            show: (target, options) => loadingManager.show(target, options),
            hide: (target) => loadingManager.hide(target),
            hideAll: () => loadingManager.hideAll(),
            isLoading: (target) => loadingManager.isLoading(target)
        },

        // Error handling
        error: {
            handle: (error, options) => errorHandler.handleError(error, options),
            handleApi: (error, options) => errorHandler.handleApiError(error, options),
            handleValidation: (errors, form) => errorHandler.handleValidationErrors(errors, form),
            clearValidation: (form) => errorHandler.clearValidationErrors(form),
            showInline: (target, message) => errorHandler.showInlineError(target, message),
            setNotificationService: (service) => errorHandler.setNotificationService(service)
        },

        // State management
        state: {
            set: (element, state, data) => stateManager.setState(element, state, data),
            get: (element) => stateManager.getState(element),
            register: (name, handler) => stateManager.registerState(name, handler)
        },

        // Utility functions
        utils: {
            debounce: function(func, wait, immediate) {
                let timeout;
                return function executedFunction(...args) {
                    const later = () => {
                        timeout = null;
                        if (!immediate) func.apply(this, args);
                    };
                    const callNow = immediate && !timeout;
                    clearTimeout(timeout);
                    timeout = setTimeout(later, wait);
                    if (callNow) func.apply(this, args);
                };
            },

            throttle: function(func, limit) {
                let inThrottle;
                return function(...args) {
                    if (!inThrottle) {
                        func.apply(this, args);
                        inThrottle = true;
                        setTimeout(() => inThrottle = false, limit);
                    }
                };
            },

            delay: (ms) => new Promise(resolve => setTimeout(resolve, ms)),

            escapeHtml: (unsafe) => errorHandler.escapeHtml(unsafe),

            isElementVisible: function(element) {
                const rect = element.getBoundingClientRect();
                return rect.top >= 0 && rect.left >= 0 && 
                       rect.bottom <= window.innerHeight && 
                       rect.right <= window.innerWidth;
            },

            scrollToElement: function(element, options = {}) {
                const config = { behavior: 'smooth', block: 'center', ...options };
                element.scrollIntoView(config);
            }
        }
    };

    // Export for different module systems
    if (typeof module !== 'undefined' && module.exports) {
        module.exports = UIUtils;
    } else {
        window.UIUtils = UIUtils;
    }

})();