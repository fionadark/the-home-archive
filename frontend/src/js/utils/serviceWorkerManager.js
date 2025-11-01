/**
 * Service Worker Registration
 * Registers and manages the service worker for offline capabilities
 */

class ServiceWorkerManager {
    constructor() {
        this.swRegistration = null;
        this.isUpdateAvailable = false;
        
        this.init();
    }
    
    /**
     * Initialize service worker registration
     */
    async init() {
        if (!('serviceWorker' in navigator)) {
            console.log('Service workers not supported');
            return;
        }
        
        try {
            await this.registerServiceWorker();
            this.setupUpdateHandler();
            this.setupMessageHandler();
        } catch (error) {
            console.error('Service worker initialization failed:', error);
        }
    }
    
    /**
     * Register the service worker
     */
    async registerServiceWorker() {
        try {
            this.swRegistration = await navigator.serviceWorker.register('/sw.js', {
                scope: '/'
            });
            
            console.log('Service worker registered successfully');
            
            // Check for updates immediately
            await this.checkForUpdates();
            
            return this.swRegistration;
        } catch (error) {
            console.error('Service worker registration failed:', error);
            throw error;
        }
    }
    
    /**
     * Check for service worker updates
     */
    async checkForUpdates() {
        if (!this.swRegistration) return;
        
        try {
            await this.swRegistration.update();
            console.log('Service worker update check completed');
        } catch (error) {
            console.error('Service worker update check failed:', error);
        }
    }
    
    /**
     * Setup update handler for new service worker versions
     */
    setupUpdateHandler() {
        if (!this.swRegistration) return;
        
        this.swRegistration.addEventListener('updatefound', () => {
            const newWorker = this.swRegistration.installing;
            
            newWorker.addEventListener('statechange', () => {
                if (newWorker.state === 'installed' && navigator.serviceWorker.controller) {
                    this.isUpdateAvailable = true;
                    this.showUpdateNotification();
                }
            });
        });
    }
    
    /**
     * Setup message handler for service worker communication
     */
    setupMessageHandler() {
        navigator.serviceWorker.addEventListener('message', (event) => {
            console.log('Message from service worker:', event.data);
            
            if (event.data.type === 'BACKGROUND_SYNC_COMPLETE') {
                this.handleBackgroundSyncComplete();
            }
        });
    }
    
    /**
     * Show update notification to user
     */
    showUpdateNotification() {
        if (window.UIUtils) {
            UIUtils.showMessage('A new version is available. Refresh to update.', 'info', {
                actions: [
                    {
                        text: 'Refresh',
                        action: () => this.applyUpdate()
                    },
                    {
                        text: 'Later',
                        action: () => {}
                    }
                ]
            });
        } else {
            if (confirm('A new version is available. Refresh to update?')) {
                this.applyUpdate();
            }
        }
    }
    
    /**
     * Apply service worker update
     */
    async applyUpdate() {
        if (!this.swRegistration || !this.swRegistration.waiting) return;
        
        // Tell the waiting service worker to take over
        this.swRegistration.waiting.postMessage({ type: 'SKIP_WAITING' });
        
        // Refresh the page
        window.location.reload();
    }
    
    /**
     * Handle background sync completion
     */
    handleBackgroundSyncComplete() {
        console.log('Background sync completed');
        
        if (window.UIUtils) {
            UIUtils.showMessage('Offline changes have been synced', 'success');
        }
        
        // Notify offline handler if available
        if (window.offlineHandler) {
            window.offlineHandler.handleBackgroundSyncComplete();
        }
    }
    
    /**
     * Cache specific URLs
     */
    async cacheUrls(urls) {
        if (!this.swRegistration || !this.swRegistration.active) return;
        
        this.swRegistration.active.postMessage({
            type: 'CACHE_URLS',
            urls: urls
        });
    }
    
    /**
     * Get service worker registration status
     */
    getStatus() {
        return {
            supported: 'serviceWorker' in navigator,
            registered: !!this.swRegistration,
            active: !!(this.swRegistration && this.swRegistration.active),
            updateAvailable: this.isUpdateAvailable
        };
    }
    
    /**
     * Unregister service worker (for testing/development)
     */
    async unregister() {
        if (!this.swRegistration) return;
        
        try {
            await this.swRegistration.unregister();
            console.log('Service worker unregistered');
            this.swRegistration = null;
        } catch (error) {
            console.error('Service worker unregistration failed:', error);
        }
    }
}

// Auto-initialize when DOM is ready
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        window.serviceWorkerManager = new ServiceWorkerManager();
    });
} else {
    window.serviceWorkerManager = new ServiceWorkerManager();
}

// Export for manual usage
window.ServiceWorkerManager = ServiceWorkerManager;