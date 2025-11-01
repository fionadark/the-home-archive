/**
 * Confirmation Dialog Utility for The Home Archive
 * Provides reusable confirmation dialogs for destructive actions
 * Supports promises, custom styling, and accessibility features
 */

(function() {
    'use strict';

    /**
     * Show a confirmation dialog
     * @param {Object} options - Dialog configuration
     * @param {string} options.title - Dialog title
     * @param {string} options.message - Dialog message/content
     * @param {string} options.confirmText - Confirm button text (default: 'Confirm')
     * @param {string} options.cancelText - Cancel button text (default: 'Cancel')
     * @param {string} options.type - Dialog type: 'info', 'warning', 'danger', 'success' (default: 'info')
     * @param {boolean} options.html - Whether message contains HTML (default: false)
     * @param {boolean} options.focusCancel - Focus cancel button by default (default: false)
     * @param {Function} options.onConfirm - Callback for confirm action
     * @param {Function} options.onCancel - Callback for cancel action
     * @returns {Promise<boolean>} Promise that resolves to true if confirmed, false if cancelled
     */
    function confirmDialog(options = {}) {
        return new Promise((resolve) => {
            const config = {
                title: 'Confirm Action',
                message: 'Are you sure you want to proceed?',
                confirmText: 'Confirm',
                cancelText: 'Cancel',
                type: 'info', // 'info', 'warning', 'danger', 'success'
                html: false,
                focusCancel: false,
                onConfirm: null,
                onCancel: null,
                ...options
            };

            // Create dialog instance
            const dialog = new ConfirmationDialog(config, resolve);
            dialog.show();
        });
    }

    /**
     * ConfirmationDialog class
     */
    class ConfirmationDialog {
        constructor(config, resolve) {
            this.config = config;
            this.resolve = resolve;
            this.dialog = null;
            this.overlay = null;
            this.confirmButton = null;
            this.cancelButton = null;
            this.isShown = false;
            
            this.handleKeyDown = this.handleKeyDown.bind(this);
            this.handleOverlayClick = this.handleOverlayClick.bind(this);
            this.handleConfirm = this.handleConfirm.bind(this);
            this.handleCancel = this.handleCancel.bind(this);
        }

        /**
         * Create and show the dialog
         */
        show() {
            if (this.isShown) return;
            
            this.createDialog();
            this.setupEventListeners();
            this.animateIn();
            this.isShown = true;
        }

        /**
         * Create dialog elements
         */
        createDialog() {
            // Create overlay
            this.overlay = document.createElement('div');
            this.overlay.className = 'confirm-dialog-overlay';
            this.overlay.setAttribute('role', 'presentation');
            
            // Create dialog
            this.dialog = document.createElement('div');
            this.dialog.className = `confirm-dialog confirm-dialog-${this.config.type}`;
            this.dialog.setAttribute('role', 'alertdialog');
            this.dialog.setAttribute('aria-modal', 'true');
            this.dialog.setAttribute('aria-labelledby', 'confirm-dialog-title');
            this.dialog.setAttribute('aria-describedby', 'confirm-dialog-message');
            
            // Create dialog content
            this.dialog.innerHTML = `
                <div class="confirm-dialog-header">
                    <h3 id="confirm-dialog-title" class="confirm-dialog-title">
                        ${this.escapeHtml(this.config.title)}
                    </h3>
                    <div class="confirm-dialog-icon">
                        ${this.getTypeIcon(this.config.type)}
                    </div>
                </div>
                <div class="confirm-dialog-body">
                    <div id="confirm-dialog-message" class="confirm-dialog-message">
                        ${this.config.html ? this.config.message : this.escapeHtml(this.config.message)}
                    </div>
                </div>
                <div class="confirm-dialog-footer">
                    <button type="button" class="confirm-dialog-btn confirm-dialog-cancel" data-action="cancel">
                        ${this.escapeHtml(this.config.cancelText)}
                    </button>
                    <button type="button" class="confirm-dialog-btn confirm-dialog-confirm confirm-dialog-${this.config.type}" data-action="confirm">
                        ${this.escapeHtml(this.config.confirmText)}
                    </button>
                </div>
            `;
            
            // Get button references
            this.confirmButton = this.dialog.querySelector('[data-action="confirm"]');
            this.cancelButton = this.dialog.querySelector('[data-action="cancel"]');
            
            // Append to overlay
            this.overlay.appendChild(this.dialog);
            
            // Add to document
            document.body.appendChild(this.overlay);
            
            // Add styles if they don't exist
            this.addStyles();
        }

        /**
         * Setup event listeners
         */
        setupEventListeners() {
            // Button clicks
            this.confirmButton.addEventListener('click', this.handleConfirm);
            this.cancelButton.addEventListener('click', this.handleCancel);
            
            // Overlay click to cancel
            this.overlay.addEventListener('click', this.handleOverlayClick);
            
            // Keyboard events
            document.addEventListener('keydown', this.handleKeyDown);
            
            // Focus management
            this.setupFocusManagement();
        }

        /**
         * Setup focus management for accessibility
         */
        setupFocusManagement() {
            // Store currently focused element
            this.previouslyFocused = document.activeElement;
            
            // Focus appropriate button
            setTimeout(() => {
                if (this.config.focusCancel) {
                    this.cancelButton.focus();
                } else {
                    this.confirmButton.focus();
                }
            }, 100);
            
            // Trap focus within dialog
            this.trapFocus();
        }

        /**
         * Trap focus within dialog
         */
        trapFocus() {
            const focusableElements = this.dialog.querySelectorAll(
                'button, [href], input, select, textarea, [tabindex]:not([tabindex="-1"])'
            );
            
            const firstFocusable = focusableElements[0];
            const lastFocusable = focusableElements[focusableElements.length - 1];
            
            this.dialog.addEventListener('keydown', (e) => {
                if (e.key === 'Tab') {
                    if (e.shiftKey) {
                        if (document.activeElement === firstFocusable) {
                            e.preventDefault();
                            lastFocusable.focus();
                        }
                    } else {
                        if (document.activeElement === lastFocusable) {
                            e.preventDefault();
                            firstFocusable.focus();
                        }
                    }
                }
            });
        }

        /**
         * Handle keyboard events
         */
        handleKeyDown(e) {
            switch (e.key) {
                case 'Escape':
                    e.preventDefault();
                    this.handleCancel();
                    break;
                case 'Enter':
                    if (e.target === this.cancelButton) {
                        e.preventDefault();
                        this.handleCancel();
                    } else if (e.target === this.confirmButton) {
                        e.preventDefault();
                        this.handleConfirm();
                    }
                    break;
            }
        }

        /**
         * Handle overlay click
         */
        handleOverlayClick(e) {
            if (e.target === this.overlay) {
                this.handleCancel();
            }
        }

        /**
         * Handle confirm action
         */
        async handleConfirm() {
            if (this.config.onConfirm) {
                try {
                    await this.config.onConfirm();
                } catch (error) {
                    console.error('Error in confirm callback:', error);
                }
            }
            
            this.close(true);
        }

        /**
         * Handle cancel action
         */
        async handleCancel() {
            if (this.config.onCancel) {
                try {
                    await this.config.onCancel();
                } catch (error) {
                    console.error('Error in cancel callback:', error);
                }
            }
            
            this.close(false);
        }

        /**
         * Close the dialog
         */
        close(confirmed) {
            if (!this.isShown) return;
            
            this.animateOut(() => {
                this.cleanup();
                this.resolve(confirmed);
            });
            
            this.isShown = false;
        }

        /**
         * Animate dialog in
         */
        animateIn() {
            // Set initial styles
            this.overlay.style.opacity = '0';
            this.dialog.style.transform = 'translate(-50%, -50%) scale(0.9)';
            this.dialog.style.opacity = '0';
            
            // Trigger animation
            requestAnimationFrame(() => {
                this.overlay.style.transition = 'opacity 0.2s ease';
                this.dialog.style.transition = 'transform 0.2s ease, opacity 0.2s ease';
                
                this.overlay.style.opacity = '1';
                this.dialog.style.transform = 'translate(-50%, -50%) scale(1)';
                this.dialog.style.opacity = '1';
            });
        }

        /**
         * Animate dialog out
         */
        animateOut(callback) {
            this.overlay.style.transition = 'opacity 0.15s ease';
            this.dialog.style.transition = 'transform 0.15s ease, opacity 0.15s ease';
            
            this.overlay.style.opacity = '0';
            this.dialog.style.transform = 'translate(-50%, -50%) scale(0.9)';
            this.dialog.style.opacity = '0';
            
            setTimeout(callback, 150);
        }

        /**
         * Clean up dialog and event listeners
         */
        cleanup() {
            // Remove event listeners
            document.removeEventListener('keydown', this.handleKeyDown);
            
            // Remove dialog from DOM
            if (this.overlay && this.overlay.parentNode) {
                this.overlay.parentNode.removeChild(this.overlay);
            }
            
            // Restore focus
            if (this.previouslyFocused && this.previouslyFocused.focus) {
                this.previouslyFocused.focus();
            }
        }

        /**
         * Get icon for dialog type
         */
        getTypeIcon(type) {
            const icons = {
                info: '<i class="fas fa-info-circle" aria-hidden="true"></i>',
                warning: '<i class="fas fa-exclamation-triangle" aria-hidden="true"></i>',
                danger: '<i class="fas fa-exclamation-circle" aria-hidden="true"></i>',
                success: '<i class="fas fa-check-circle" aria-hidden="true"></i>'
            };
            
            return icons[type] || icons.info;
        }

        /**
         * Escape HTML to prevent XSS
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
         * Add default styles
         */
        addStyles() {
            if (document.getElementById('confirm-dialog-styles')) return;
            
            const styles = document.createElement('style');
            styles.id = 'confirm-dialog-styles';
            styles.textContent = `
                .confirm-dialog-overlay {
                    position: fixed;
                    top: 0;
                    left: 0;
                    right: 0;
                    bottom: 0;
                    background: rgba(0, 0, 0, 0.5);
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    z-index: 9999;
                    padding: 1rem;
                }
                
                .confirm-dialog {
                    background: var(--bg-primary, #ffffff);
                    border-radius: 0.5rem;
                    box-shadow: 0 10px 25px rgba(0, 0, 0, 0.15);
                    max-width: 500px;
                    width: 100%;
                    max-height: 90vh;
                    overflow-y: auto;
                    position: relative;
                    border: 1px solid var(--border-color, #e9ecef);
                }
                
                .confirm-dialog-header {
                    display: flex;
                    align-items: flex-start;
                    gap: 1rem;
                    padding: 1.5rem 1.5rem 1rem;
                    border-bottom: 1px solid var(--border-color, #e9ecef);
                }
                
                .confirm-dialog-title {
                    margin: 0;
                    font-size: 1.25rem;
                    font-weight: 600;
                    color: var(--text-primary, #212529);
                    flex: 1;
                }
                
                .confirm-dialog-icon {
                    font-size: 1.5rem;
                    display: flex;
                    align-items: center;
                    justify-content: center;
                    width: 2rem;
                    height: 2rem;
                    border-radius: 50%;
                    flex-shrink: 0;
                }
                
                .confirm-dialog-info .confirm-dialog-icon {
                    color: var(--info-color, #0dcaf0);
                    background: var(--info-bg, #cff4fc);
                }
                
                .confirm-dialog-warning .confirm-dialog-icon {
                    color: var(--warning-color, #ffc107);
                    background: var(--warning-bg, #fff3cd);
                }
                
                .confirm-dialog-danger .confirm-dialog-icon {
                    color: var(--danger-color, #dc3545);
                    background: var(--danger-bg, #f8d7da);
                }
                
                .confirm-dialog-success .confirm-dialog-icon {
                    color: var(--success-color, #198754);
                    background: var(--success-bg, #d1e7dd);
                }
                
                .confirm-dialog-body {
                    padding: 1rem 1.5rem;
                }
                
                .confirm-dialog-message {
                    color: var(--text-secondary, #6c757d);
                    line-height: 1.5;
                    margin: 0;
                }
                
                .confirm-dialog-footer {
                    display: flex;
                    gap: 0.75rem;
                    justify-content: flex-end;
                    padding: 1rem 1.5rem 1.5rem;
                    border-top: 1px solid var(--border-color, #e9ecef);
                }
                
                .confirm-dialog-btn {
                    padding: 0.5rem 1rem;
                    border-radius: 0.375rem;
                    border: 1px solid transparent;
                    font-size: 0.875rem;
                    font-weight: 500;
                    cursor: pointer;
                    transition: all 0.15s ease;
                    min-width: 80px;
                }
                
                .confirm-dialog-cancel {
                    background: var(--bg-secondary, #f8f9fa);
                    color: var(--text-secondary, #6c757d);
                    border-color: var(--border-color, #dee2e6);
                }
                
                .confirm-dialog-cancel:hover {
                    background: var(--bg-hover, #e9ecef);
                    color: var(--text-primary, #212529);
                }
                
                .confirm-dialog-confirm {
                    color: white;
                    font-weight: 600;
                }
                
                .confirm-dialog-confirm.confirm-dialog-info {
                    background: var(--info-color, #0dcaf0);
                    border-color: var(--info-color, #0dcaf0);
                }
                
                .confirm-dialog-confirm.confirm-dialog-info:hover {
                    background: var(--info-hover, #31d2f2);
                }
                
                .confirm-dialog-confirm.confirm-dialog-warning {
                    background: var(--warning-color, #ffc107);
                    border-color: var(--warning-color, #ffc107);
                    color: var(--text-dark, #212529);
                }
                
                .confirm-dialog-confirm.confirm-dialog-warning:hover {
                    background: var(--warning-hover, #ffcd39);
                }
                
                .confirm-dialog-confirm.confirm-dialog-danger {
                    background: var(--danger-color, #dc3545);
                    border-color: var(--danger-color, #dc3545);
                }
                
                .confirm-dialog-confirm.confirm-dialog-danger:hover {
                    background: var(--danger-hover, #bb2d3b);
                }
                
                .confirm-dialog-confirm.confirm-dialog-success {
                    background: var(--success-color, #198754);
                    border-color: var(--success-color, #198754);
                }
                
                .confirm-dialog-confirm.confirm-dialog-success:hover {
                    background: var(--success-hover, #157347);
                }
                
                .confirm-dialog-btn:focus {
                    outline: 2px solid var(--focus-color, #0066cc);
                    outline-offset: 2px;
                }
                
                @media (max-width: 480px) {
                    .confirm-dialog {
                        margin: 0.5rem;
                        max-width: none;
                    }
                    
                    .confirm-dialog-header,
                    .confirm-dialog-body,
                    .confirm-dialog-footer {
                        padding-left: 1rem;
                        padding-right: 1rem;
                    }
                    
                    .confirm-dialog-footer {
                        flex-direction: column-reverse;
                    }
                    
                    .confirm-dialog-btn {
                        width: 100%;
                    }
                }
                
                /* Dark mode support */
                @media (prefers-color-scheme: dark) {
                    .confirm-dialog {
                        --bg-primary: #2d3748;
                        --bg-secondary: #4a5568;
                        --bg-hover: #718096;
                        --text-primary: #ffffff;
                        --text-secondary: #e2e8f0;
                        --border-color: #4a5568;
                        --focus-color: #63b3ed;
                    }
                }
            `;
            
            document.head.appendChild(styles);
        }
    }

    // Quick utility functions
    function confirmDanger(title, message, confirmText = 'Delete') {
        return confirmDialog({
            title,
            message,
            confirmText,
            type: 'danger',
            focusCancel: true
        });
    }

    function confirmWarning(title, message, confirmText = 'Proceed') {
        return confirmDialog({
            title,
            message,
            confirmText,
            type: 'warning',
            focusCancel: true
        });
    }

    function confirmInfo(title, message, confirmText = 'OK') {
        return confirmDialog({
            title,
            message,
            confirmText,
            type: 'info'
        });
    }

    // Export functions
    if (typeof module !== 'undefined' && module.exports) {
        module.exports = {
            confirmDialog,
            confirmDanger,
            confirmWarning,
            confirmInfo
        };
    } else {
        window.confirmDialog = confirmDialog;
        window.confirmDanger = confirmDanger;
        window.confirmWarning = confirmWarning;
        window.confirmInfo = confirmInfo;
    }

})();