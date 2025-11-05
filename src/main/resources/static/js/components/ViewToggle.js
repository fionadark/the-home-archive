/**
 * ViewToggle Component for The Home Archive
 * Handles switching between grid and list views for the library page
 * Provides view preference persistence and smooth transitions
 */

class ViewToggle {
    constructor(options = {}) {
        this.options = {
            defaultView: 'grid', // 'grid' or 'list'
            persistPreference: true, // Save preference to localStorage
            storageKey: 'library-view-preference',
            animationDuration: 200, // Animation duration in milliseconds
            ...options
        };
        
        this.currentView = this.options.defaultView;
        this.onViewChange = null; // Callback for view changes
        this.isTransitioning = false;
        
        // DOM elements
        this.toggleButton = null;
        this.gridButton = null;
        this.listButton = null;
        this.viewIndicator = null;
        
        this.init();
    }

    /**
     * Initialize the view toggle component
     */
    init() {
        this.bindElements();
        this.loadPersistedView();
        this.setupEventListeners();
        this.updateUI();
        
        console.log('ViewToggle component initialized with view:', this.currentView);
    }

    /**
     * Bind DOM elements
     */
    bindElements() {
        // Try different possible element IDs/classes
        this.toggleButton = document.getElementById('view-toggle-btn');
        this.gridButton = document.getElementById('grid-view-btn');
        this.listButton = document.getElementById('list-view-btn');
        this.viewIndicator = document.getElementById('view-indicator');
        
        // Alternative selectors
        if (!this.toggleButton) {
            this.toggleButton = document.querySelector('.view-toggle-button');
        }
        if (!this.gridButton) {
            this.gridButton = document.querySelector('.grid-view-button');
        }
        if (!this.listButton) {
            this.listButton = document.querySelector('.list-view-button');
        }
        
        // Create default elements if not found
        if (!this.toggleButton && !this.gridButton && !this.listButton) {
            this.createDefaultToggle();
        }
    }

    /**
     * Create default toggle elements if not found in DOM
     */
    createDefaultToggle() {
        // Find a suitable container
        const container = document.querySelector('.library-controls') || 
                         document.querySelector('.library-header') ||
                         document.getElementById('library-page');
        
        if (!container) {
            console.warn('No suitable container found for view toggle');
            return;
        }
        
        // Create toggle container
        const toggleContainer = document.createElement('div');
        toggleContainer.className = 'view-toggle-container';
        toggleContainer.innerHTML = `
            <div class="view-toggle" role="radiogroup" aria-label="View mode">
                <button id="grid-view-btn" class="view-toggle-btn" data-view="grid" 
                        role="radio" aria-checked="true" aria-label="Grid view">
                    <i class="fas fa-th" aria-hidden="true"></i>
                    <span class="view-label">Grid</span>
                </button>
                <button id="list-view-btn" class="view-toggle-btn" data-view="list" 
                        role="radio" aria-checked="false" aria-label="List view">
                    <i class="fas fa-list" aria-hidden="true"></i>
                    <span class="view-label">List</span>
                </button>
            </div>
        `;
        
        // Insert at the beginning of container
        container.insertBefore(toggleContainer, container.firstChild);
        
        // Re-bind elements
        this.gridButton = document.getElementById('grid-view-btn');
        this.listButton = document.getElementById('list-view-btn');
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Single toggle button
        if (this.toggleButton) {
            this.toggleButton.addEventListener('click', () => {
                this.toggleView();
            });
        }
        
        // Separate grid/list buttons
        if (this.gridButton) {
            this.gridButton.addEventListener('click', () => {
                this.setView('grid');
            });
        }
        
        if (this.listButton) {
            this.listButton.addEventListener('click', () => {
                this.setView('list');
            });
        }
        
        // Keyboard shortcuts
        document.addEventListener('keydown', (e) => {
            // Only handle shortcuts when not typing in an input
            if (e.target.tagName !== 'INPUT' && e.target.tagName !== 'TEXTAREA') {
                if (e.key === 'g' && e.altKey) {
                    e.preventDefault();
                    this.setView('grid');
                } else if (e.key === 'l' && e.altKey) {
                    e.preventDefault();
                    this.setView('list');
                } else if (e.key === 'v' && e.altKey) {
                    e.preventDefault();
                    this.toggleView();
                }
            }
        });
    }

    /**
     * Load persisted view preference
     */
    loadPersistedView() {
        if (!this.options.persistPreference) return;
        
        try {
            const savedView = localStorage.getItem(this.options.storageKey);
            if (savedView && (savedView === 'grid' || savedView === 'list')) {
                this.currentView = savedView;
            }
        } catch (error) {
            console.warn('Failed to load view preference:', error);
        }
    }

    /**
     * Save view preference
     */
    saveViewPreference() {
        if (!this.options.persistPreference) return;
        
        try {
            localStorage.setItem(this.options.storageKey, this.currentView);
        } catch (error) {
            console.warn('Failed to save view preference:', error);
        }
    }

    /**
     * Set the current view
     * @param {string} view - View type ('grid' or 'list')
     * @param {boolean} skipAnimation - Whether to skip transition animation
     */
    setView(view, skipAnimation = false) {
        if (view !== 'grid' && view !== 'list') {
            console.error('Invalid view type:', view);
            return;
        }
        
        if (view === this.currentView || this.isTransitioning) {
            return;
        }
        
        const previousView = this.currentView;
        this.currentView = view;
        
        // Save preference
        this.saveViewPreference();
        
        // Update UI
        this.updateUI();
        
        // Handle transition
        if (!skipAnimation) {
            this.handleTransition(previousView, view);
        }
        
        // Notify listeners
        if (this.onViewChange) {
            this.onViewChange(view, previousView);
        }
        
        console.log('View changed to:', view);
    }

    /**
     * Toggle between grid and list views
     */
    toggleView() {
        const newView = this.currentView === 'grid' ? 'list' : 'grid';
        this.setView(newView);
    }

    /**
     * Get current view
     * @returns {string} Current view type
     */
    getCurrentView() {
        return this.currentView;
    }

    /**
     * Update UI elements to reflect current view
     */
    updateUI() {
        // Update single toggle button
        if (this.toggleButton) {
            this.updateToggleButton();
        }
        
        // Update separate buttons
        if (this.gridButton && this.listButton) {
            this.updateSeparateButtons();
        }
        
        // Update view indicator
        if (this.viewIndicator) {
            this.updateViewIndicator();
        }
        
        // Update body class for global styling
        this.updateBodyClass();
    }

    /**
     * Update single toggle button
     */
    updateToggleButton() {
        const icon = this.toggleButton.querySelector('i');
        const text = this.toggleButton.querySelector('.toggle-text');
        
        if (icon) {
            if (this.currentView === 'grid') {
                icon.className = 'fas fa-list';
                this.toggleButton.title = 'Switch to list view';
                this.toggleButton.setAttribute('aria-label', 'Switch to list view');
            } else {
                icon.className = 'fas fa-th';
                this.toggleButton.title = 'Switch to grid view';
                this.toggleButton.setAttribute('aria-label', 'Switch to grid view');
            }
        }
        
        if (text) {
            text.textContent = this.currentView === 'grid' ? 'List' : 'Grid';
        }
        
        // Update data attribute
        this.toggleButton.setAttribute('data-current-view', this.currentView);
    }

    /**
     * Update separate grid/list buttons
     */
    updateSeparateButtons() {
        // Update grid button
        if (this.currentView === 'grid') {
            this.gridButton.classList.add('active');
            this.gridButton.setAttribute('aria-checked', 'true');
            this.gridButton.setAttribute('aria-pressed', 'true');
        } else {
            this.gridButton.classList.remove('active');
            this.gridButton.setAttribute('aria-checked', 'false');
            this.gridButton.setAttribute('aria-pressed', 'false');
        }
        
        // Update list button
        if (this.currentView === 'list') {
            this.listButton.classList.add('active');
            this.listButton.setAttribute('aria-checked', 'true');
            this.listButton.setAttribute('aria-pressed', 'true');
        } else {
            this.listButton.classList.remove('active');
            this.listButton.setAttribute('aria-checked', 'false');
            this.listButton.setAttribute('aria-pressed', 'false');
        }
    }

    /**
     * Update view indicator
     */
    updateViewIndicator() {
        this.viewIndicator.textContent = this.currentView === 'grid' ? 'Grid View' : 'List View';
        this.viewIndicator.setAttribute('data-view', this.currentView);
    }

    /**
     * Update body class for global styling
     */
    updateBodyClass() {
        // Remove previous view classes
        document.body.classList.remove('view-grid', 'view-list');
        
        // Add current view class
        document.body.classList.add(`view-${this.currentView}`);
    }

    /**
     * Handle transition animation
     * @param {string} fromView - Previous view
     * @param {string} toView - New view
     */
    handleTransition(fromView, toView) {
        if (this.options.animationDuration <= 0) return;
        
        this.isTransitioning = true;
        
        // Find the content container
        const contentContainer = document.getElementById('books-grid') || 
                                document.querySelector('.books-container');
        
        if (!contentContainer) {
            this.isTransitioning = false;
            return;
        }
        
        // Add transition class
        contentContainer.classList.add('view-transitioning');
        
        // Set transition styles
        contentContainer.style.transition = `opacity ${this.options.animationDuration}ms ease-in-out`;
        
        // Fade out
        contentContainer.style.opacity = '0.7';
        
        // After transition duration, complete the transition
        setTimeout(() => {
            // Fade back in
            contentContainer.style.opacity = '1';
            
            // Clean up after another short delay
            setTimeout(() => {
                contentContainer.classList.remove('view-transitioning');
                contentContainer.style.transition = '';
                this.isTransitioning = false;
            }, this.options.animationDuration);
            
        }, this.options.animationDuration);
    }

    /**
     * Check if view toggle is supported/available
     * @returns {boolean} Whether view toggle is available
     */
    isAvailable() {
        return !!(this.toggleButton || (this.gridButton && this.listButton));
    }

    /**
     * Enable/disable the view toggle
     * @param {boolean} enabled - Whether to enable the toggle
     */
    setEnabled(enabled) {
        const buttons = [this.toggleButton, this.gridButton, this.listButton].filter(Boolean);
        
        buttons.forEach(button => {
            button.disabled = !enabled;
            if (enabled) {
                button.classList.remove('disabled');
            } else {
                button.classList.add('disabled');
            }
        });
    }

    /**
     * Add CSS styles for the view toggle if they don't exist
     */
    addDefaultStyles() {
        // Check if styles already exist
        if (document.getElementById('view-toggle-styles')) return;
        
        const styles = document.createElement('style');
        styles.id = 'view-toggle-styles';
        styles.textContent = `
            .view-toggle-container {
                display: flex;
                align-items: center;
                gap: 0.5rem;
                margin: 1rem 0;
            }
            
            .view-toggle {
                display: flex;
                border: 1px solid var(--border-color, #ddd);
                border-radius: 0.375rem;
                overflow: hidden;
                background: var(--bg-secondary, #f8f9fa);
            }
            
            .view-toggle-btn {
                display: flex;
                align-items: center;
                gap: 0.25rem;
                padding: 0.5rem 0.75rem;
                border: none;
                background: transparent;
                color: var(--text-secondary, #6c757d);
                font-size: 0.875rem;
                cursor: pointer;
                transition: all 0.2s ease;
                position: relative;
            }
            
            .view-toggle-btn:hover {
                background: var(--bg-hover, #e9ecef);
                color: var(--text-primary, #212529);
            }
            
            .view-toggle-btn.active {
                background: var(--primary-color, #007bff);
                color: white;
            }
            
            .view-toggle-btn:disabled {
                opacity: 0.5;
                cursor: not-allowed;
            }
            
            .view-toggle-btn i {
                font-size: 1rem;
            }
            
            .view-label {
                font-weight: 500;
            }
            
            @media (max-width: 768px) {
                .view-label {
                    display: none;
                }
                
                .view-toggle-btn {
                    padding: 0.5rem;
                }
            }
            
            .view-transitioning {
                pointer-events: none;
            }
        `;
        
        document.head.appendChild(styles);
    }

    /**
     * Get current state for debugging
     * @returns {Object} Current state
     */
    getState() {
        return {
            currentView: this.currentView,
            isTransitioning: this.isTransitioning,
            isAvailable: this.isAvailable(),
            options: this.options
        };
    }

    /**
     * Destroy the component and clean up
     */
    destroy() {
        // Remove event listeners would go here if we stored references
        // For now, elements will be cleaned up when removed from DOM
        
        // Remove body classes
        document.body.classList.remove('view-grid', 'view-list');
        
        console.log('ViewToggle component destroyed');
    }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { ViewToggle };
} else {
    window.ViewToggle = ViewToggle;
}