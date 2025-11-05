/**
 * NotificationService - Handles user notifications and messages
 * Part of Phase 5 User Story 3 implementation
 */

export class NotificationService {
    constructor() {
        this.container = null;
        this.init();
    }

    /**
     * Initialize notification system
     */
    init() {
        // Create notification container if it doesn't exist
        this.container = document.getElementById('notification-container');
        if (!this.container) {
            this.container = document.createElement('div');
            this.container.id = 'notification-container';
            this.container.className = 'notification-container';
            document.body.appendChild(this.container);
        }
    }

    /**
     * Show success notification
     * @param {string} message - Success message
     * @param {number} duration - Display duration in ms
     */
    showSuccess(message, duration = 5000) {
        this.showNotification(message, 'success', duration);
    }

    /**
     * Show error notification
     * @param {string} message - Error message
     * @param {number} duration - Display duration in ms
     */
    showError(message, duration = 7000) {
        this.showNotification(message, 'error', duration);
    }

    /**
     * Show warning notification
     * @param {string} message - Warning message
     * @param {number} duration - Display duration in ms
     */
    showWarning(message, duration = 6000) {
        this.showNotification(message, 'warning', duration);
    }

    /**
     * Show info notification
     * @param {string} message - Info message
     * @param {number} duration - Display duration in ms
     */
    showInfo(message, duration = 5000) {
        this.showNotification(message, 'info', duration);
    }

    /**
     * Show notification with specified type
     * @param {string} message - Notification message
     * @param {string} type - Notification type (success, error, warning, info)
     * @param {number} duration - Display duration in ms
     */
    showNotification(message, type = 'info', duration = 5000) {
        const notification = document.createElement('div');
        notification.className = `notification notification-${type}`;
        notification.setAttribute('role', 'alert');
        notification.setAttribute('aria-live', 'polite');

        // Create notification content
        const content = document.createElement('div');
        content.className = 'notification-content';
        content.textContent = message;

        // Create close button
        const closeButton = document.createElement('button');
        closeButton.className = 'notification-close';
        closeButton.innerHTML = '&times;';
        closeButton.setAttribute('aria-label', 'Close notification');
        closeButton.addEventListener('click', () => {
            this.removeNotification(notification);
        });

        notification.appendChild(content);
        notification.appendChild(closeButton);

        // Add to container
        this.container.appendChild(notification);

        // Trigger animation
        setTimeout(() => {
            notification.classList.add('show');
        }, 10);

        // Auto-remove after duration
        if (duration > 0) {
            setTimeout(() => {
                this.removeNotification(notification);
            }, duration);
        }

        return notification;
    }

    /**
     * Remove notification
     * @param {HTMLElement} notification - Notification element to remove
     */
    removeNotification(notification) {
        if (notification && notification.parentNode) {
            notification.classList.add('hide');
            setTimeout(() => {
                if (notification.parentNode) {
                    notification.parentNode.removeChild(notification);
                }
            }, 300);
        }
    }

    /**
     * Clear all notifications
     */
    clearAll() {
        const notifications = this.container.querySelectorAll('.notification');
        notifications.forEach(notification => {
            this.removeNotification(notification);
        });
    }

    /**
     * Show loading notification
     * @param {string} message - Loading message
     * @returns {HTMLElement} Loading notification element
     */
    showLoading(message = 'Loading...') {
        const notification = document.createElement('div');
        notification.className = 'notification notification-loading';
        notification.setAttribute('role', 'status');
        notification.setAttribute('aria-live', 'polite');

        const content = document.createElement('div');
        content.className = 'notification-content';
        
        const spinner = document.createElement('div');
        spinner.className = 'loading-spinner';
        
        const text = document.createElement('span');
        text.textContent = message;

        content.appendChild(spinner);
        content.appendChild(text);
        notification.appendChild(content);

        this.container.appendChild(notification);

        setTimeout(() => {
            notification.classList.add('show');
        }, 10);

        return notification;
    }

    /**
     * Hide loading notification
     * @param {HTMLElement} loadingNotification - Loading notification element
     */
    hideLoading(loadingNotification) {
        if (loadingNotification) {
            this.removeNotification(loadingNotification);
        }
    }

    /**
     * Show confirmation dialog
     * @param {string} message - Confirmation message
     * @param {Function} onConfirm - Callback for confirm action
     * @param {Function} onCancel - Callback for cancel action
     * @returns {HTMLElement} Confirmation dialog element
     */
    showConfirmation(message, onConfirm, onCancel) {
        const overlay = document.createElement('div');
        overlay.className = 'notification-overlay';

        const dialog = document.createElement('div');
        dialog.className = 'notification-dialog';
        dialog.setAttribute('role', 'dialog');
        dialog.setAttribute('aria-modal', 'true');
        dialog.setAttribute('aria-labelledby', 'dialog-title');

        const title = document.createElement('h3');
        title.id = 'dialog-title';
        title.textContent = 'Confirmation';

        const content = document.createElement('p');
        content.textContent = message;

        const buttons = document.createElement('div');
        buttons.className = 'notification-dialog-buttons';

        const confirmBtn = document.createElement('button');
        confirmBtn.className = 'btn btn-primary';
        confirmBtn.textContent = 'Confirm';
        confirmBtn.addEventListener('click', () => {
            document.body.removeChild(overlay);
            if (onConfirm) onConfirm();
        });

        const cancelBtn = document.createElement('button');
        cancelBtn.className = 'btn btn-secondary';
        cancelBtn.textContent = 'Cancel';
        cancelBtn.addEventListener('click', () => {
            document.body.removeChild(overlay);
            if (onCancel) onCancel();
        });

        buttons.appendChild(cancelBtn);
        buttons.appendChild(confirmBtn);

        dialog.appendChild(title);
        dialog.appendChild(content);
        dialog.appendChild(buttons);

        overlay.appendChild(dialog);
        document.body.appendChild(overlay);

        // Focus first button
        confirmBtn.focus();

        return overlay;
    }
}

// Export as singleton instance
export const notificationService = new NotificationService();
export default notificationService;