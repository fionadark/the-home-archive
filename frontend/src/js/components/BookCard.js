/**
 * BookCard Component for The Home Archive
 * Handles rendering and interaction for individual books in the personal library
 * Supports both grid and list view modes with full CRUD operations
 */

class BookCard {
    constructor(bookData) {
        this.bookData = bookData;
        this.libraryService = window.libraryService || null;
        this.onAction = null; // Callback for parent component actions
        
        // Bind methods
        this.updateStatus = this.updateStatus.bind(this);
        this.removeBook = this.removeBook.bind(this);
        this.handleAction = this.handleAction.bind(this);
    }

    /**
     * Render book card in grid view format
     * @returns {HTMLElement} The rendered grid card element
     */
    renderGridView() {
        const cardElement = document.createElement('div');
        cardElement.className = 'book-card';
        cardElement.setAttribute('data-book-id', this.bookData.id);
        cardElement.setAttribute('data-reading-status', this.bookData.status);
        cardElement.setAttribute('data-category', this.bookData.book.category?.name || '');
        cardElement.setAttribute('role', 'article');
        cardElement.setAttribute('aria-label', `Book: ${this.bookData.book.title} by ${this.bookData.book.author}`);

        const book = this.bookData.book;
        const statusIcon = this.getStatusIcon(this.bookData.status);
        const readingStatusDisplay = this.formatReadingStatus(this.bookData.status);
        const showProgress = this.bookData.status === 'READING' && this.bookData.currentPage && book.pageCount;
        const progressPercentage = showProgress ? Math.round((this.bookData.currentPage / book.pageCount) * 100) : 0;
        const progressText = showProgress ? `${this.bookData.currentPage} / ${book.pageCount} pages` : '';

        cardElement.innerHTML = `
            <!-- Book Cover -->
            <div class="book-cover-container">
                <div class="book-cover">
                    <img 
                        src="${book.coverImageUrl || '/images/default-book-cover.jpg'}" 
                        alt="Cover of ${this.escapeHtml(book.title)} by ${this.escapeHtml(book.author || 'Unknown Author')}"
                        class="book-cover-image"
                        loading="lazy"
                        onerror="this.src='/images/default-book-cover.jpg'"
                    >
                    
                    <!-- Reading Status Badge -->
                    <div class="reading-status-badge status-${this.bookData.status}">
                        <i class="status-icon fas ${statusIcon}"></i>
                        <span class="status-text">${readingStatusDisplay}</span>
                    </div>
                    
                    <!-- Quick Actions Overlay -->
                    <div class="book-actions-overlay">
                        <button class="action-btn view-details-btn" title="View Details" data-action="view" data-book-id="${this.bookData.id}" tabindex="0">
                            <i class="fas fa-eye"></i>
                        </button>
                        <button class="action-btn edit-btn" title="Edit Book" data-action="edit" data-book-id="${this.bookData.id}" tabindex="0">
                            <i class="fas fa-edit"></i>
                        </button>
                        <button class="action-btn remove-btn" title="Remove from Library" data-action="remove" data-book-id="${this.bookData.id}" tabindex="0">
                            <i class="fas fa-trash"></i>
                        </button>
                    </div>
                </div>
                
                <!-- Progress Indicator (for currently reading books) -->
                <div class="reading-progress" style="display: ${showProgress ? 'block' : 'none'};">
                    <div class="progress-bar">
                        <div class="progress-fill" style="width: ${progressPercentage}%"></div>
                    </div>
                    <span class="progress-text">${progressText}</span>
                </div>
            </div>

            <!-- Book Information -->
            <div class="book-info">
                <h3 class="book-title" title="${this.escapeHtml(book.title)}">${this.escapeHtml(book.title)}</h3>
                <p class="book-author" title="${this.escapeHtml(book.author || 'Unknown Author')}">by ${this.escapeHtml(book.author || 'Unknown Author')}</p>
                
                <!-- Book Meta -->
                <div class="book-meta">
                    <span class="book-category">${this.escapeHtml(book.category?.name || 'Uncategorized')}</span>
                    <span class="book-year">${book.publicationYear || 'Unknown'}</span>
                </div>

                <!-- Personal Library Info -->
                <div class="library-info">
                    <div class="library-meta">
                        <span class="date-added" title="Added to library on ${this.formatDateForDisplay(this.bookData.dateAdded)}">
                            <i class="fas fa-calendar-plus"></i>
                            ${this.formatDateForDisplay(this.bookData.dateAdded)}
                        </span>
                        
                        <span class="physical-location" title="Physical location: ${this.escapeHtml(this.bookData.physicalLocation || '')}" style="display: ${this.bookData.physicalLocation ? 'inline' : 'none'};">
                            <i class="fas fa-map-marker-alt"></i>
                            ${this.escapeHtml(this.bookData.physicalLocation || '')}
                        </span>
                    </div>

                    <!-- Reading Dates -->
                    <div class="reading-dates">
                        <span class="date-started" style="display: ${this.bookData.dateStarted ? 'inline' : 'none'};" title="Started reading on ${this.formatDateForDisplay(this.bookData.dateStarted)}">
                            <i class="fas fa-play"></i>
                            Started: ${this.formatDateForDisplay(this.bookData.dateStarted)}
                        </span>
                        
                        <span class="date-completed" style="display: ${this.bookData.dateCompleted ? 'inline' : 'none'};" title="Completed on ${this.formatDateForDisplay(this.bookData.dateCompleted)}">
                            <i class="fas fa-check"></i>
                            Finished: ${this.formatDateForDisplay(this.bookData.dateCompleted)}
                        </span>
                    </div>
                </div>

                <!-- Personal Notes Preview -->
                <div class="personal-notes" style="display: ${this.bookData.personalNotes ? 'block' : 'none'};">
                    <p class="notes-preview" title="${this.escapeHtml(this.bookData.personalNotes || '')}">
                        <i class="fas fa-sticky-note"></i>
                        ${this.truncateText(this.bookData.personalNotes || '', 100)}
                    </p>
                </div>

                <!-- Book Actions -->
                <div class="book-actions">
                    <!-- Status Update -->
                    <div class="status-update">
                        <select class="status-select" data-book-id="${this.bookData.id}" title="Update reading status" aria-label="Update reading status for ${this.escapeHtml(book.title)}">
                            <option value="UNREAD" ${this.bookData.status === 'UNREAD' ? 'selected' : ''}>Unread</option>
                            <option value="READING" ${this.bookData.status === 'READING' ? 'selected' : ''}>Reading</option>
                            <option value="READ" ${this.bookData.status === 'READ' ? 'selected' : ''}>Read</option>
                            <option value="DNF" ${this.bookData.status === 'DNF' ? 'selected' : ''}>Did Not Finish</option>
                        </select>
                    </div>

                    <!-- Quick Actions -->
                    <div class="quick-actions">
                        <button class="quick-action-btn notes-btn" title="Add/Edit Notes" data-action="notes" data-book-id="${this.bookData.id}" tabindex="0">
                            <i class="fas fa-sticky-note"></i>
                        </button>
                        
                        <button class="quick-action-btn location-btn" title="Update Location" data-action="location" data-book-id="${this.bookData.id}" tabindex="0">
                            <i class="fas fa-map-marker-alt"></i>
                        </button>
                        
                        <button class="quick-action-btn share-btn" title="Share Book" data-action="share" data-book-id="${this.bookData.id}" tabindex="0">
                            <i class="fas fa-share-alt"></i>
                        </button>
                    </div>
                </div>
            </div>
        `;

        this.attachEventListeners(cardElement);
        return cardElement;
    }

    /**
     * Render book card in list view format
     * @returns {HTMLElement} The rendered list item element
     */
    renderListView() {
        const listElement = document.createElement('div');
        listElement.className = 'book-list-item';
        listElement.setAttribute('data-book-id', this.bookData.id);
        listElement.setAttribute('data-reading-status', this.bookData.status);
        listElement.setAttribute('data-category', this.bookData.book.category?.name || '');
        listElement.setAttribute('role', 'article');
        listElement.setAttribute('aria-label', `Book: ${this.bookData.book.title} by ${this.bookData.book.author}`);

        const book = this.bookData.book;
        const statusIcon = this.getStatusIcon(this.bookData.status);
        const readingStatusDisplay = this.formatReadingStatus(this.bookData.status);
        const showProgress = this.bookData.status === 'READING' && this.bookData.currentPage && book.pageCount;
        const progressPercentage = showProgress ? Math.round((this.bookData.currentPage / book.pageCount) * 100) : 0;
        const progressText = showProgress ? `${this.bookData.currentPage} / ${book.pageCount} pages` : '';

        listElement.innerHTML = `
            <!-- Compact Book Cover -->
            <div class="book-cover-compact">
                <img 
                    src="${book.coverImageUrl || '/images/default-book-cover.jpg'}" 
                    alt="Cover of ${this.escapeHtml(book.title)}"
                    class="book-cover-image-compact"
                    loading="lazy"
                    onerror="this.src='/images/default-book-cover.jpg'"
                >
            </div>

            <!-- Book Details -->
            <div class="book-details-list">
                <div class="book-main-info">
                    <h3 class="book-title-list">${this.escapeHtml(book.title)}</h3>
                    <p class="book-author-list">by ${this.escapeHtml(book.author || 'Unknown Author')}</p>
                    <div class="book-meta-list">
                        <span class="book-category-list">${this.escapeHtml(book.category?.name || 'Uncategorized')}</span>
                        <span class="book-year-list">${book.publicationYear || 'Unknown'}</span>
                        <span class="book-pages-list" style="display: ${book.pageCount ? 'inline' : 'none'};">${book.pageCount || 0} pages</span>
                    </div>
                </div>

                <div class="book-library-info-list">
                    <!-- Reading Status -->
                    <div class="status-section-list">
                        <div class="reading-status-badge-list status-${this.bookData.status}">
                            <i class="status-icon fas ${statusIcon}"></i>
                            <span class="status-text">${readingStatusDisplay}</span>
                        </div>
                        
                        <!-- Progress Bar for Reading Books -->
                        <div class="reading-progress-list" style="display: ${showProgress ? 'flex' : 'none'};">
                            <div class="progress-bar-list">
                                <div class="progress-fill-list" style="width: ${progressPercentage}%"></div>
                            </div>
                            <span class="progress-text-list">${progressText}</span>
                        </div>
                    </div>

                    <!-- Dates and Location -->
                    <div class="dates-location-list">
                        <span class="date-added-list" title="Added ${this.formatDateForDisplay(this.bookData.dateAdded)}">
                            <i class="fas fa-calendar-plus"></i>
                            Added ${this.formatDateForDisplay(this.bookData.dateAdded)}
                        </span>
                        
                        <span class="date-started-list" style="display: ${this.bookData.dateStarted ? 'inline' : 'none'};" title="Started ${this.formatDateForDisplay(this.bookData.dateStarted)}">
                            <i class="fas fa-play"></i>
                            Started ${this.formatDateForDisplay(this.bookData.dateStarted)}
                        </span>
                        
                        <span class="date-completed-list" style="display: ${this.bookData.dateCompleted ? 'inline' : 'none'};" title="Finished ${this.formatDateForDisplay(this.bookData.dateCompleted)}">
                            <i class="fas fa-check"></i>
                            Finished ${this.formatDateForDisplay(this.bookData.dateCompleted)}
                        </span>
                        
                        <span class="physical-location-list" style="display: ${this.bookData.physicalLocation ? 'inline' : 'none'};" title="Location: ${this.escapeHtml(this.bookData.physicalLocation || '')}">
                            <i class="fas fa-map-marker-alt"></i>
                            ${this.escapeHtml(this.bookData.physicalLocation || '')}
                        </span>
                    </div>

                    <!-- Personal Notes in List View -->
                    <div class="personal-notes-list" style="display: ${this.bookData.personalNotes ? 'block' : 'none'};">
                        <p class="notes-preview-list">
                            <i class="fas fa-sticky-note"></i>
                            ${this.truncateText(this.bookData.personalNotes || '', 150)}
                        </p>
                    </div>
                </div>
            </div>

            <!-- Actions Section -->
            <div class="book-actions-list">
                <!-- Status Update Dropdown -->
                <div class="status-update-list">
                    <select class="status-select-list" data-book-id="${this.bookData.id}" title="Update reading status" aria-label="Update reading status for ${this.escapeHtml(book.title)}">
                        <option value="UNREAD" ${this.bookData.status === 'UNREAD' ? 'selected' : ''}>Unread</option>
                        <option value="READING" ${this.bookData.status === 'READING' ? 'selected' : ''}>Reading</option>
                        <option value="read" ${this.bookData.status === 'read' ? 'selected' : ''}>Read</option>
                        <option value="DNF" ${this.bookData.status === 'DNF' ? 'selected' : ''}>Did Not Finish</option>
                    </select>
                </div>

                <!-- Action Buttons -->
                <div class="action-buttons-list">
                    <button class="action-btn-list view-details-btn" title="View Details" data-action="view" data-book-id="${this.bookData.id}" tabindex="0">
                        <i class="fas fa-eye"></i>
                        <span class="action-text">View</span>
                    </button>
                    
                    <button class="action-btn-list edit-btn" title="Edit Book" data-action="edit" data-book-id="${this.bookData.id}" tabindex="0">
                        <i class="fas fa-edit"></i>
                        <span class="action-text">Edit</span>
                    </button>
                    
                    <button class="action-btn-list notes-btn" title="Add/Edit Notes" data-action="notes" data-book-id="${this.bookData.id}" tabindex="0">
                        <i class="fas fa-sticky-note"></i>
                        <span class="action-text">Notes</span>
                    </button>
                    
                    <button class="action-btn-list remove-btn" title="Remove from Library" data-action="remove" data-book-id="${this.bookData.id}" tabindex="0">
                        <i class="fas fa-trash"></i>
                        <span class="action-text">Remove</span>
                    </button>
                </div>
            </div>
        `;

        this.attachEventListeners(listElement);
        return listElement;
    }

    /**
     * Attach event listeners to the card element
     * @param {HTMLElement} element - The card element to attach listeners to
     */
    attachEventListeners(element) {
        // Status update listeners
        const statusSelects = element.querySelectorAll('.status-select, .status-select-list');
        statusSelects.forEach(select => {
            select.addEventListener('change', async (e) => {
                const newStatus = e.target.value;
                await this.updateStatus(newStatus);
            });
        });

        // Action button listeners
        const actionButtons = element.querySelectorAll('[data-action]');
        actionButtons.forEach(button => {
            button.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                const action = button.getAttribute('data-action');
                this.handleAction(action);
            });
        });

        // Card click listener for view details
        element.addEventListener('click', (e) => {
            // Only trigger if not clicking on interactive elements
            if (!e.target.closest('button, select, input, a')) {
                this.handleAction('view');
            }
        });

        // Keyboard navigation
        element.addEventListener('keydown', (e) => {
            if (e.key === 'Enter' || e.key === ' ') {
                if (!e.target.closest('button, select, input')) {
                    e.preventDefault();
                    this.handleAction('view');
                }
            }
        });
    }

    /**
     * Update the reading status of the book
     * @param {string} newStatus - The new reading status
     */
    async updateStatus(newStatus) {
        if (!this.libraryService) {
            console.error('Library service not available');
            alert('Unable to update book status. Please try again.');
            return;
        }

        try {
            const updateData = { status: newStatus };
            
            // Add completion date if marking as read
            if (newStatus === 'read') {
                updateData.dateCompleted = new Date().toISOString().split('T')[0];
            }
            
            // Add start date if marking as currently reading and no start date exists
            if (newStatus === 'READING' && !this.bookData.dateStarted) {
                updateData.dateStarted = new Date().toISOString().split('T')[0];
            }

            const result = await this.libraryService.updateLibraryEntry(this.bookData.id, updateData);
            
            if (result.success) {
                // Update local data
                this.bookData.status = newStatus;
                if (updateData.dateCompleted) {
                    this.bookData.dateCompleted = updateData.dateCompleted;
                }
                if (updateData.dateStarted) {
                    this.bookData.dateStarted = updateData.dateStarted;
                }
                
                this.showSuccessMessage('Reading status updated successfully');
                
                // Notify parent component
                if (this.onAction) {
                    this.onAction('statusUpdated', this.bookData);
                }
            } else {
                console.error('Error updating book status:', result.message);
                alert('Failed to update book status. Please try again.');
            }
        } catch (error) {
            console.error('Error updating book status:', error);
            alert('Failed to update book status. Please try again.');
        }
    }

    /**
     * Remove the book from the library
     */
    async removeBook() {
        if (!this.libraryService) {
            console.error('Library service not available');
            alert('Unable to remove book. Please try again.');
            return;
        }

        const confirmMessage = `Are you sure you want to remove "${this.bookData.book.title}" from your library? This action cannot be undone.`;
        
        if (!confirm(confirmMessage)) {
            return;
        }

        try {
            const result = await this.libraryService.removeBookFromLibrary(this.bookData.id);
            
            if (result.success) {
                this.showSuccessMessage('Book removed from library successfully');
                
                // Notify parent component
                if (this.onAction) {
                    this.onAction('removed', this.bookData);
                }
            } else {
                console.error('Error removing book:', result.message);
                alert('Failed to remove book. Please try again.');
            }
        } catch (error) {
            console.error('Error removing book:', error);
            alert('Failed to remove book. Please try again.');
        }
    }

    /**
     * Handle various actions on the book card
     * @param {string} action - The action to handle
     */
    handleAction(action) {
        switch (action) {
            case 'view':
                if (this.onAction) {
                    this.onAction('view', this.bookData);
                }
                break;
            case 'edit':
                if (this.onAction) {
                    this.onAction('edit', this.bookData);
                }
                break;
            case 'notes':
                if (this.onAction) {
                    this.onAction('notes', this.bookData);
                }
                break;
            case 'location':
                if (this.onAction) {
                    this.onAction('location', this.bookData);
                }
                break;
            case 'share':
                if (this.onAction) {
                    this.onAction('share', this.bookData);
                }
                break;
            case 'remove':
                this.removeBook();
                break;
            default:
                console.warn('Unknown action:', action);
        }
    }

    /**
     * Get the appropriate icon for a reading status
     * @param {string} status - The reading status
     * @returns {string} Font Awesome icon class
     */
    getStatusIcon(status) {
        const iconMap = {
            'UNREAD': 'fa-bookmark',
            'READING': 'fa-book-open',
            'read': 'fa-check-circle',
            'READ': 'fa-check-circle',
            'DNF': 'fa-times-circle'
        };
        return iconMap[status] || 'fa-bookmark';
    }

    /**
     * Format reading status for display
     * @param {string} status - The reading status
     * @returns {string} Formatted status
     */
    formatReadingStatus(status) {
        if (this.libraryService && typeof this.libraryService.formatReadingStatus === 'function') {
            return this.libraryService.formatReadingStatus(status);
        }
        
        const statusMap = {
            'UNREAD': 'Not Started',
            'READING': 'Currently Reading',
            'read': 'Completed',
            'READ': 'Completed',
            'DNF': 'Did Not Finish'
        };
        return statusMap[status] || status;
    }

    /**
     * Format date for display
     * @param {string} dateString - ISO date string
     * @returns {string} Formatted date
     */
    formatDateForDisplay(dateString) {
        if (!dateString) return '';
        
        try {
            const date = new Date(dateString);
            const now = new Date();
            const diffTime = Math.abs(now - date);
            const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));
            
            if (diffDays === 1) {
                return 'Yesterday';
            } else if (diffDays === 0) {
                return 'Today';
            } else if (diffDays <= 7) {
                return `${diffDays} days ago`;
            } else {
                return date.toLocaleDateString('en-US', { 
                    year: 'numeric', 
                    month: 'short', 
                    day: 'numeric' 
                });
            }
        } catch (error) {
            console.error('Error formatting date:', error);
            return '';
        }
    }

    /**
     * Truncate text to specified length
     * @param {string} text - Text to truncate
     * @param {number} maxLength - Maximum length
     * @returns {string} Truncated text
     */
    truncateText(text, maxLength) {
        if (!text || text.length <= maxLength) return text;
        return text.substring(0, maxLength) + '...';
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
     * Show a success message (can be overridden by parent)
     * @param {string} message - Success message
     */
    showSuccessMessage(message) {
        // This can be overridden by the parent component
        // For now, we'll use a simple console log
        console.log('Success:', message);
    }

    /**
     * Static method to set up event delegation for a container
     * @param {HTMLElement} container - Container element
     */
    static setupEventDelegation(container) {
        if (!container) return;

        container.addEventListener('click', (e) => {
            const bookCard = e.target.closest('.book-card, .book-list-item');
            if (!bookCard) return;

            const bookId = bookCard.getAttribute('data-book-id');
            const action = e.target.closest('[data-action]')?.getAttribute('data-action');
            
            if (action && BookCard.onGlobalAction) {
                e.preventDefault();
                e.stopPropagation();
                BookCard.onGlobalAction(action, bookId, bookCard);
            }
        });

        container.addEventListener('change', (e) => {
            if (e.target.matches('.status-select, .status-select-list')) {
                const bookCard = e.target.closest('.book-card, .book-list-item');
                const bookId = bookCard?.getAttribute('data-book-id');
                const newStatus = e.target.value;
                
                if (BookCard.onGlobalStatusUpdate) {
                    BookCard.onGlobalStatusUpdate(bookId, newStatus);
                }
            }
        });
    }

    /**
     * Update book data and re-render if needed
     * @param {Object} newData - Updated book data
     */
    updateData(newData) {
        this.bookData = { ...this.bookData, ...newData };
    }

    /**
     * Get current book data
     * @returns {Object} Current book data
     */
    getData() {
        return this.bookData;
    }
}

// Static properties for global event handling
BookCard.onGlobalAction = null;
BookCard.onGlobalStatusUpdate = null;

// Export for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { BookCard };
} else {
    window.BookCard = BookCard;
}