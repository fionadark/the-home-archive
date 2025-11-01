/**
 * SearchFilter Component for The Home Archive
 * Handles search functionality and filter management for the library page
 * Provides debounced search, filter state management, and URL sync
 */

class SearchFilter {
    constructor(options = {}) {
        this.options = {
            searchDelay: 300, // Debounce delay in milliseconds
            minSearchLength: 1, // Minimum characters to trigger search
            autoSync: true, // Automatically sync with URL parameters
            ...options
        };
        
        this.searchTimeout = null;
        this.currentFilters = {};
        this.currentSort = { field: 'dateAdded', direction: 'desc' };
        this.onFilterChange = null; // Callback for filter changes
        this.onSearchChange = null; // Callback for search changes
        
        // Initialize DOM elements
        this.searchInput = null;
        this.statusFilter = null;
        this.categoryFilter = null;
        this.locationFilter = null;
        this.sortSelect = null;
        this.sortOrderBtn = null;
        this.clearSearchBtn = null;
        this.clearFiltersBtn = null;
        this.activeFiltersContainer = null;
        
        this.init();
    }

    /**
     * Initialize the search filter component
     */
    init() {
        this.bindElements();
        this.setupEventListeners();
        
        if (this.options.autoSync) {
            this.syncFromURL();
        }
        
        console.log('SearchFilter component initialized');
    }

    /**
     * Bind DOM elements
     */
    bindElements() {
        this.searchInput = document.getElementById('library-search');
        this.statusFilter = document.getElementById('status-filter');
        this.categoryFilter = document.getElementById('category-filter');
        this.locationFilter = document.getElementById('location-filter');
        this.sortSelect = document.getElementById('sort-select');
        this.sortOrderBtn = document.getElementById('sort-order-btn');
        this.clearSearchBtn = document.getElementById('clear-search');
        this.clearFiltersBtn = document.getElementById('clear-all-filters');
        this.activeFiltersContainer = document.getElementById('active-filters');
        
        // Verify required elements exist
        if (!this.searchInput) {
            console.error('Search input element not found');
        }
        if (!this.statusFilter) {
            console.error('Status filter element not found');
        }
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // Search input with debouncing
        if (this.searchInput) {
            this.searchInput.addEventListener('input', (e) => {
                this.handleSearchInput(e.target.value);
            });
            
            this.searchInput.addEventListener('keydown', (e) => {
                if (e.key === 'Escape') {
                    this.clearSearch();
                }
            });
        }

        // Clear search button
        if (this.clearSearchBtn) {
            this.clearSearchBtn.addEventListener('click', () => {
                this.clearSearch();
            });
        }

        // Filter dropdowns
        if (this.statusFilter) {
            this.statusFilter.addEventListener('change', (e) => {
                this.updateFilter('status', e.target.value);
            });
        }

        if (this.categoryFilter) {
            this.categoryFilter.addEventListener('change', (e) => {
                this.updateFilter('category', e.target.value);
            });
        }

        if (this.locationFilter) {
            this.locationFilter.addEventListener('change', (e) => {
                this.updateFilter('location', e.target.value);
            });
        }

        // Sort controls
        if (this.sortSelect) {
            this.sortSelect.addEventListener('change', (e) => {
                this.updateSort(e.target.value, this.currentSort.direction);
            });
        }

        if (this.sortOrderBtn) {
            this.sortOrderBtn.addEventListener('click', () => {
                const newDirection = this.currentSort.direction === 'asc' ? 'desc' : 'asc';
                this.updateSort(this.currentSort.field, newDirection);
            });
        }

        // Clear all filters button
        if (this.clearFiltersBtn) {
            this.clearFiltersBtn.addEventListener('click', () => {
                this.clearAllFilters();
            });
        }
    }

    /**
     * Handle search input with debouncing
     * @param {string} searchTerm - The search term
     */
    handleSearchInput(searchTerm) {
        // Clear previous timeout
        if (this.searchTimeout) {
            clearTimeout(this.searchTimeout);
        }

        // Update clear button visibility
        if (this.clearSearchBtn) {
            this.clearSearchBtn.style.display = searchTerm ? 'block' : 'none';
        }

        // Debounce search
        this.searchTimeout = setTimeout(() => {
            this.performSearch(searchTerm);
        }, this.options.searchDelay);
    }

    /**
     * Perform the actual search
     * @param {string} searchTerm - The search term
     */
    performSearch(searchTerm) {
        const trimmedTerm = searchTerm.trim();
        
        // Only search if term meets minimum length or is empty (clear search)
        if (trimmedTerm.length >= this.options.minSearchLength || trimmedTerm === '') {
            if (this.onSearchChange) {
                this.onSearchChange(trimmedTerm);
            }
            
            // Update URL if auto-sync is enabled
            if (this.options.autoSync) {
                this.syncToURL();
            }
            
            // Update active filters display
            this.updateActiveFiltersDisplay();
        }
    }

    /**
     * Update a filter value
     * @param {string} filterType - The type of filter (status, category, location)
     * @param {string} value - The filter value
     */
    updateFilter(filterType, value) {
        if (value) {
            this.currentFilters[filterType] = value;
        } else {
            delete this.currentFilters[filterType];
        }
        
        // Notify listeners
        if (this.onFilterChange) {
            this.onFilterChange(this.currentFilters);
        }
        
        // Update URL if auto-sync is enabled
        if (this.options.autoSync) {
            this.syncToURL();
        }
        
        // Update active filters display
        this.updateActiveFiltersDisplay();
    }

    /**
     * Update sort configuration
     * @param {string} field - Sort field
     * @param {string} direction - Sort direction (asc/desc)
     */
    updateSort(field, direction) {
        this.currentSort = { field, direction };
        
        // Update sort order button
        if (this.sortOrderBtn) {
            const icon = this.sortOrderBtn.querySelector('i');
            const text = this.sortOrderBtn.querySelector('.sort-order-text');
            
            if (icon) {
                icon.className = direction === 'asc' ? 'fas fa-sort-amount-up' : 'fas fa-sort-amount-down';
            }
            if (text) {
                text.textContent = direction === 'asc' ? 'Asc' : 'Desc';
            }
        }
        
        // Notify listeners
        if (this.onFilterChange) {
            this.onFilterChange(this.currentFilters, this.currentSort);
        }
        
        // Update URL if auto-sync is enabled
        if (this.options.autoSync) {
            this.syncToURL();
        }
    }

    /**
     * Clear search input
     */
    clearSearch() {
        if (this.searchInput) {
            this.searchInput.value = '';
            this.handleSearchInput('');
        }
    }

    /**
     * Clear all filters
     */
    clearAllFilters() {
        // Clear search
        this.clearSearch();
        
        // Reset filters
        this.currentFilters = {};
        
        // Reset form elements
        if (this.statusFilter) this.statusFilter.value = '';
        if (this.categoryFilter) this.categoryFilter.value = '';
        if (this.locationFilter) this.locationFilter.value = '';
        
        // Reset sort to default
        this.updateSort('dateAdded', 'desc');
        if (this.sortSelect) this.sortSelect.value = 'dateAdded';
        
        // Notify listeners
        if (this.onFilterChange) {
            this.onFilterChange(this.currentFilters, this.currentSort);
        }
        
        // Update URL if auto-sync is enabled
        if (this.options.autoSync) {
            this.syncToURL();
        }
        
        // Update active filters display
        this.updateActiveFiltersDisplay();
    }

    /**
     * Get current search term
     * @returns {string} Current search term
     */
    getSearchTerm() {
        return this.searchInput ? this.searchInput.value.trim() : '';
    }

    /**
     * Get current filters
     * @returns {Object} Current filter values
     */
    getFilters() {
        return { ...this.currentFilters };
    }

    /**
     * Get current sort configuration
     * @returns {Object} Current sort configuration
     */
    getSort() {
        return { ...this.currentSort };
    }

    /**
     * Get complete filter state including search and sort
     * @returns {Object} Complete filter state
     */
    getFilterState() {
        return {
            search: this.getSearchTerm(),
            filters: this.getFilters(),
            sort: this.getSort()
        };
    }

    /**
     * Set filters programmatically
     * @param {Object} filters - Filter values to set
     * @param {boolean} triggerEvent - Whether to trigger change events
     */
    setFilters(filters, triggerEvent = true) {
        this.currentFilters = { ...filters };
        
        // Update form elements
        if (this.statusFilter && filters.status !== undefined) {
            this.statusFilter.value = filters.status || '';
        }
        if (this.categoryFilter && filters.category !== undefined) {
            this.categoryFilter.value = filters.category || '';
        }
        if (this.locationFilter && filters.location !== undefined) {
            this.locationFilter.value = filters.location || '';
        }
        
        if (triggerEvent && this.onFilterChange) {
            this.onFilterChange(this.currentFilters, this.currentSort);
        }
        
        this.updateActiveFiltersDisplay();
    }

    /**
     * Set search term programmatically
     * @param {string} searchTerm - Search term to set
     * @param {boolean} triggerEvent - Whether to trigger change events
     */
    setSearchTerm(searchTerm, triggerEvent = true) {
        if (this.searchInput) {
            this.searchInput.value = searchTerm;
            
            // Update clear button visibility
            if (this.clearSearchBtn) {
                this.clearSearchBtn.style.display = searchTerm ? 'block' : 'none';
            }
            
            if (triggerEvent && this.onSearchChange) {
                this.onSearchChange(searchTerm);
            }
        }
    }

    /**
     * Set sort configuration programmatically
     * @param {string} field - Sort field
     * @param {string} direction - Sort direction
     * @param {boolean} triggerEvent - Whether to trigger change events
     */
    setSort(field, direction, triggerEvent = true) {
        this.currentSort = { field, direction };
        
        // Update form elements
        if (this.sortSelect) {
            this.sortSelect.value = field;
        }
        
        // Update sort order button
        if (this.sortOrderBtn) {
            const icon = this.sortOrderBtn.querySelector('i');
            const text = this.sortOrderBtn.querySelector('.sort-order-text');
            
            if (icon) {
                icon.className = direction === 'asc' ? 'fas fa-sort-amount-up' : 'fas fa-sort-amount-down';
            }
            if (text) {
                text.textContent = direction === 'asc' ? 'Asc' : 'Desc';
            }
        }
        
        if (triggerEvent && this.onFilterChange) {
            this.onFilterChange(this.currentFilters, this.currentSort);
        }
    }

    /**
     * Update the active filters display
     */
    updateActiveFiltersDisplay() {
        if (!this.activeFiltersContainer) return;
        
        const hasActiveFilters = Object.keys(this.currentFilters).length > 0 || this.getSearchTerm();
        
        if (hasActiveFilters) {
            this.activeFiltersContainer.style.display = 'block';
            this.renderActiveFilters();
        } else {
            this.activeFiltersContainer.style.display = 'none';
        }
    }

    /**
     * Render active filters
     */
    renderActiveFilters() {
        const activeFiltersList = this.activeFiltersContainer.querySelector('.active-filters-list');
        if (!activeFiltersList) return;
        
        activeFiltersList.innerHTML = '';
        
        // Add search term
        const searchTerm = this.getSearchTerm();
        if (searchTerm) {
            const searchChip = this.createFilterChip('Search', searchTerm, () => {
                this.clearSearch();
            });
            activeFiltersList.appendChild(searchChip);
        }
        
        // Add filter chips
        Object.entries(this.currentFilters).forEach(([key, value]) => {
            const label = this.getFilterLabel(key);
            const displayValue = this.getFilterDisplayValue(key, value);
            
            const chip = this.createFilterChip(label, displayValue, () => {
                this.removeFilter(key);
            });
            activeFiltersList.appendChild(chip);
        });
    }

    /**
     * Create a filter chip element
     * @param {string} label - Filter label
     * @param {string} value - Filter value
     * @param {Function} onRemove - Removal callback
     * @returns {HTMLElement} Filter chip element
     */
    createFilterChip(label, value, onRemove) {
        const chip = document.createElement('div');
        chip.className = 'filter-chip';
        chip.innerHTML = `
            <span class="filter-chip-label">${this.escapeHtml(label)}:</span>
            <span class="filter-chip-value">${this.escapeHtml(value)}</span>
            <button class="filter-chip-remove" title="Remove ${label} filter" aria-label="Remove ${label} filter">
                <i class="fas fa-times"></i>
            </button>
        `;
        
        const removeBtn = chip.querySelector('.filter-chip-remove');
        removeBtn.addEventListener('click', onRemove);
        
        return chip;
    }

    /**
     * Remove a specific filter
     * @param {string} filterType - Type of filter to remove
     */
    removeFilter(filterType) {
        delete this.currentFilters[filterType];
        
        // Update form element
        const element = this.getFilterElement(filterType);
        if (element) {
            element.value = '';
        }
        
        // Notify listeners
        if (this.onFilterChange) {
            this.onFilterChange(this.currentFilters, this.currentSort);
        }
        
        // Update URL if auto-sync is enabled
        if (this.options.autoSync) {
            this.syncToURL();
        }
        
        // Update display
        this.updateActiveFiltersDisplay();
    }

    /**
     * Get filter element by type
     * @param {string} filterType - Filter type
     * @returns {HTMLElement} Filter element
     */
    getFilterElement(filterType) {
        switch (filterType) {
            case 'status': return this.statusFilter;
            case 'category': return this.categoryFilter;
            case 'location': return this.locationFilter;
            default: return null;
        }
    }

    /**
     * Get human-readable filter label
     * @param {string} filterType - Filter type
     * @returns {string} Filter label
     */
    getFilterLabel(filterType) {
        const labels = {
            status: 'Status',
            category: 'Category',
            location: 'Location'
        };
        return labels[filterType] || filterType;
    }

    /**
     * Get display value for filter
     * @param {string} filterType - Filter type
     * @param {string} value - Filter value
     * @returns {string} Display value
     */
    getFilterDisplayValue(filterType, value) {
        if (filterType === 'status') {
            const statusMap = {
                'UNREAD': 'Unread',
                'READING': 'Currently Reading',
                'read': 'Read',
                'READ': 'Read',
                'DNF': 'Did Not Finish'
            };
            return statusMap[value] || value;
        }
        return value;
    }

    /**
     * Populate category filter options
     * @param {Array} categories - Array of category objects
     */
    populateCategoryFilter(categories) {
        if (!this.categoryFilter) return;
        
        // Clear existing options (except "All Categories")
        const options = this.categoryFilter.querySelectorAll('option:not([value=""])');
        options.forEach(option => option.remove());
        
        // Add category options
        categories.forEach(category => {
            const option = document.createElement('option');
            option.value = category.name;
            option.textContent = category.name;
            this.categoryFilter.appendChild(option);
        });
    }

    /**
     * Populate location filter options
     * @param {Array} locations - Array of location strings
     */
    populateLocationFilter(locations) {
        if (!this.locationFilter) return;
        
        // Clear existing options (except "All Locations")
        const options = this.locationFilter.querySelectorAll('option:not([value=""])');
        options.forEach(option => option.remove());
        
        // Add location options
        locations.forEach(location => {
            const option = document.createElement('option');
            option.value = location;
            option.textContent = location;
            this.locationFilter.appendChild(option);
        });
    }

    /**
     * Sync current state to URL parameters
     */
    syncToURL() {
        try {
            const url = new URL(window.location);
            const params = url.searchParams;
            
            // Clear existing filter params
            params.delete('search');
            params.delete('status');
            params.delete('category');
            params.delete('location');
            params.delete('sort');
            params.delete('direction');
            
            // Add current params
            const searchTerm = this.getSearchTerm();
            if (searchTerm) {
                params.set('search', searchTerm);
            }
            
            Object.entries(this.currentFilters).forEach(([key, value]) => {
                params.set(key, value);
            });
            
            params.set('sort', this.currentSort.field);
            params.set('direction', this.currentSort.direction);
            
            // Update URL without triggering page reload
            window.history.replaceState({}, '', url.toString());
        } catch (error) {
            console.error('Error syncing to URL:', error);
        }
    }

    /**
     * Sync state from URL parameters
     */
    syncFromURL() {
        try {
            const url = new URL(window.location);
            const params = url.searchParams;
            
            // Set search term
            const searchTerm = params.get('search') || '';
            this.setSearchTerm(searchTerm, false);
            
            // Set filters
            const filters = {};
            ['status', 'category', 'location'].forEach(filterType => {
                const value = params.get(filterType);
                if (value) {
                    filters[filterType] = value;
                }
            });
            this.setFilters(filters, false);
            
            // Set sort
            const sortField = params.get('sort') || 'dateAdded';
            const sortDirection = params.get('direction') || 'desc';
            this.setSort(sortField, sortDirection, false);
            
            this.updateActiveFiltersDisplay();
        } catch (error) {
            console.error('Error syncing from URL:', error);
        }
    }

    /**
     * Escape HTML to prevent XSS
     * @param {string} unsafe - Unsafe text
     * @returns {string} Escaped text
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
     * Get current query parameters for API calls
     * @returns {Object} Query parameters object
     */
    getQueryParams() {
        const params = {};
        
        const searchTerm = this.getSearchTerm();
        if (searchTerm) {
            params.search = searchTerm;
        }
        
        Object.assign(params, this.currentFilters);
        
        params.sort = this.currentSort.field;
        params.direction = this.currentSort.direction;
        
        return params;
    }

    /**
     * Reset to default state
     */
    reset() {
        this.clearAllFilters();
        this.currentSort = { field: 'dateAdded', direction: 'desc' };
    }

    /**
     * Destroy the component and clean up event listeners
     */
    destroy() {
        if (this.searchTimeout) {
            clearTimeout(this.searchTimeout);
        }
        
        // Remove event listeners would go here if we stored references
        // For now, elements will be cleaned up when removed from DOM
        console.log('SearchFilter component destroyed');
    }
}

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { SearchFilter };
} else {
    window.SearchFilter = SearchFilter;
}