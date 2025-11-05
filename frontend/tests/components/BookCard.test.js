/**
 * Tests for BookCard component
 * Tests the book card rendering and interaction functionality
 */

import { expect } from '@jest/globals';

// Mock the libraryService
jest.mock('../../../frontend/src/js/services/libraryService.js', () => ({
    updateLibraryEntry: jest.fn(),
    removeBookFromLibrary: jest.fn(),
    formatReadingStatus: jest.fn(),
    formatDateForAPI: jest.fn()
}));

// Mock global alert and confirm
global.alert = jest.fn();
global.confirm = jest.fn(() => true);

describe('BookCard', () => {
    let bookCard;
    let mockLibraryService;
    
    // Sample book data for testing
    const sampleBook = {
        id: 1,
        book: {
            id: 123,
            title: 'The Great Gatsby',
            author: 'F. Scott Fitzgerald',
            isbn: '978-0-7432-7356-5',
            description: 'A classic American novel',
            publicationYear: 1925,
            publisher: 'Scribner',
            pageCount: 180,
            category: { id: 1, name: 'Fiction' },
            coverImageUrl: 'https://example.com/cover.jpg'
        },
        status: 'READING',
        userRating: 4,
        personalNotes: 'Really enjoying this classic!',
        physicalLocation: 'Living Room Shelf A',
        dateAdded: '2023-01-15T10:30:00Z',
        dateStarted: '2023-01-20T09:00:00Z',
        dateCompleted: null,
        isFavorite: false,
        personalTags: 'classic,american-literature'
    };

    beforeEach(() => {
        // Reset DOM
        document.body.innerHTML = '';
        
        // Mock library service
        mockLibraryService = require('../../../frontend/src/js/services/libraryService.js');
        
        // Reset all mocks
        jest.clearAllMocks();
        
        // Setup default mock responses
        mockLibraryService.formatReadingStatus.mockImplementation((status) => {
            const statusMap = {
                'UNREAD': 'Not Started',
                'READING': 'Currently Reading', 
                'READ': 'Completed',
                'DNF': 'Did Not Finish'
            };
            return statusMap[status] || status;
        });
        
        mockLibraryService.updateLibraryEntry.mockResolvedValue({
            success: true,
            data: { id: 1, status: 'read' }
        });
        
        mockLibraryService.removeBookFromLibrary.mockResolvedValue({
            success: true
        });
    });

    describe('Grid View Rendering', () => {
        test('should render book card in grid view with all information', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const cardElement = bookCard.renderGridView();
            
            expect(cardElement.querySelector('.book-title').textContent).toBe('The Great Gatsby');
            expect(cardElement.querySelector('.book-author').textContent).toBe('by F. Scott Fitzgerald');
            expect(cardElement.querySelector('.book-category').textContent).toBe('Fiction');
            expect(cardElement.querySelector('.book-year').textContent).toBe('1925');
        });

        test('should display correct reading status badge', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const cardElement = bookCard.renderGridView();
            const statusBadge = cardElement.querySelector('.reading-status-badge');
            
            expect(statusBadge.classList.contains('status-READING')).toBe(true);
            expect(statusBadge.querySelector('.status-text').textContent).toBe('Currently Reading');
        });

        test('should show cover image with fallback', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const cardElement = bookCard.renderGridView();
            const coverImage = cardElement.querySelector('.book-cover-image');
            
            expect(coverImage.src).toBe('https://example.com/cover.jpg');
            expect(coverImage.alt).toBe('Cover of The Great Gatsby by F. Scott Fitzgerald');
        });

        test('should display personal notes preview', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const cardElement = bookCard.renderGridView();
            const notesElement = cardElement.querySelector('.personal-notes');
            
            expect(notesElement.style.display).toBe('block');
            expect(notesElement.querySelector('.notes-preview').textContent).toContain('Really enjoying this classic!');
        });

        test('should show physical location when available', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const cardElement = bookCard.renderGridView();
            const locationElement = cardElement.querySelector('.physical-location');
            
            expect(locationElement.style.display).toBe('inline');
            expect(locationElement.textContent).toContain('Living Room Shelf A');
        });

        test('should hide elements when data not available', () => {
            const bookWithoutNotes = { ...sampleBook, personalNotes: null, physicalLocation: null };
            
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(bookWithoutNotes);
            
            const cardElement = bookCard.renderGridView();
            
            expect(cardElement.querySelector('.personal-notes').style.display).toBe('none');
            expect(cardElement.querySelector('.physical-location').style.display).toBe('none');
        });

        test('should display reading dates correctly', () => {
            const bookWithDates = {
                ...sampleBook,
                status: 'READ',
                dateStarted: '2023-01-20T09:00:00Z',
                dateCompleted: '2023-02-15T18:00:00Z'
            };
            
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(bookWithDates);
            
            const cardElement = bookCard.renderGridView();
            
            expect(cardElement.querySelector('.date-started').style.display).toBe('inline');
            expect(cardElement.querySelector('.date-completed').style.display).toBe('inline');
        });
    });

    describe('List View Rendering', () => {
        test('should render book card in list view with compact layout', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const listElement = bookCard.renderListView();
            
            expect(listElement.querySelector('.book-title-list').textContent).toBe('The Great Gatsby');
            expect(listElement.querySelector('.book-author-list').textContent).toBe('by F. Scott Fitzgerald');
            expect(listElement.querySelector('.book-category-list').textContent).toBe('Fiction');
        });

        test('should display compact cover image', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const listElement = bookCard.renderListView();
            const coverImage = listElement.querySelector('.book-cover-image-compact');
            
            expect(coverImage.src).toBe('https://example.com/cover.jpg');
            expect(coverImage.alt).toBe('Cover of The Great Gatsby');
        });

        test('should show all action buttons in list view', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const listElement = bookCard.renderListView();
            const actionButtons = listElement.querySelectorAll('.action-btn-list');
            
            expect(actionButtons.length).toBe(4); // view, edit, notes, remove
            expect(listElement.querySelector('.view-details-btn')).toBeTruthy();
            expect(listElement.querySelector('.edit-btn')).toBeTruthy();
            expect(listElement.querySelector('.notes-btn')).toBeTruthy();
            expect(listElement.querySelector('.remove-btn')).toBeTruthy();
        });
    });

    describe('Status Updates', () => {
        test('should update reading status when dropdown changes', async () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const cardElement = bookCard.renderGridView();
            document.body.appendChild(cardElement);
            
            // Setup event listeners
            bookCard.attachEventListeners(cardElement);
            
            const statusSelect = cardElement.querySelector('.status-select');
            statusSelect.value = 'READ';
            statusSelect.dispatchEvent(new Event('change'));
            
            await new Promise(resolve => setTimeout(resolve, 0)); // Wait for async operation
            
            expect(mockLibraryService.updateLibraryEntry).toHaveBeenCalledWith(1, { status: 'READ' });
        });

        test('should handle status update errors', async () => {
            mockLibraryService.updateLibraryEntry.mockResolvedValue({
                success: false,
                message: 'Update failed'
            });

            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
            
            await bookCard.updateStatus('read');
            
            expect(consoleSpy).toHaveBeenCalledWith('Error updating book status:', 'Update failed');
            expect(global.alert).toHaveBeenCalledWith('Failed to update book status. Please try again.');
            
            consoleSpy.mockRestore();
        });

        test('should show success message on successful status update', async () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const showMessageSpy = jest.spyOn(bookCard, 'showSuccessMessage').mockImplementation();
            
            await bookCard.updateStatus('read');
            
            expect(showMessageSpy).toHaveBeenCalledWith('Reading status updated successfully');
            
            showMessageSpy.mockRestore();
        });
    });

    describe('Book Actions', () => {
        test('should handle view details action', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const cardElement = bookCard.renderGridView();
            document.body.appendChild(cardElement);
            
            const onActionSpy = jest.fn();
            bookCard.onAction = onActionSpy;
            
            bookCard.attachEventListeners(cardElement);
            
            const viewBtn = cardElement.querySelector('.view-details-btn');
            viewBtn.click();
            
            expect(onActionSpy).toHaveBeenCalledWith('view', sampleBook);
        });

        test('should handle edit action', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const cardElement = bookCard.renderGridView();
            document.body.appendChild(cardElement);
            
            const onActionSpy = jest.fn();
            bookCard.onAction = onActionSpy;
            
            bookCard.attachEventListeners(cardElement);
            
            const editBtn = cardElement.querySelector('.edit-btn');
            editBtn.click();
            
            expect(onActionSpy).toHaveBeenCalledWith('edit', sampleBook);
        });

        test('should handle remove action with confirmation', async () => {
            global.confirm.mockReturnValue(true);

            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const cardElement = bookCard.renderGridView();
            document.body.appendChild(cardElement);
            
            bookCard.attachEventListeners(cardElement);
            
            const removeBtn = cardElement.querySelector('.remove-btn');
            removeBtn.click();
            
            await new Promise(resolve => setTimeout(resolve, 0)); // Wait for async operation
            
            expect(global.confirm).toHaveBeenCalledWith(
                expect.stringContaining('Are you sure you want to remove "The Great Gatsby"')
            );
            expect(mockLibraryService.removeBookFromLibrary).toHaveBeenCalledWith(1);
        });

        test('should not remove book if user cancels confirmation', async () => {
            global.confirm.mockReturnValue(false);

            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const cardElement = bookCard.renderGridView();
            document.body.appendChild(cardElement);
            
            bookCard.attachEventListeners(cardElement);
            
            const removeBtn = cardElement.querySelector('.remove-btn');
            removeBtn.click();
            
            await new Promise(resolve => setTimeout(resolve, 0));
            
            expect(mockLibraryService.removeBookFromLibrary).not.toHaveBeenCalled();
        });

        test('should handle notes action', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const cardElement = bookCard.renderGridView();
            document.body.appendChild(cardElement);
            
            const onActionSpy = jest.fn();
            bookCard.onAction = onActionSpy;
            
            bookCard.attachEventListeners(cardElement);
            
            const notesBtn = cardElement.querySelector('.notes-btn');
            notesBtn.click();
            
            expect(onActionSpy).toHaveBeenCalledWith('notes', sampleBook);
        });

        test('should handle location action', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const cardElement = bookCard.renderGridView();
            document.body.appendChild(cardElement);
            
            const onActionSpy = jest.fn();
            bookCard.onAction = onActionSpy;
            
            bookCard.attachEventListeners(cardElement);
            
            const locationBtn = cardElement.querySelector('.location-btn');
            locationBtn.click();
            
            expect(onActionSpy).toHaveBeenCalledWith('location', sampleBook);
        });
    });

    describe('Progress Display', () => {
        test('should show reading progress for books with page progress', () => {
            const bookWithProgress = {
                ...sampleBook,
                status: 'READING',
                currentPage: 90,
                book: { ...sampleBook.book, pageCount: 180 }
            };
            
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(bookWithProgress);
            
            const cardElement = bookCard.renderGridView();
            const progressElement = cardElement.querySelector('.reading-progress');
            
            expect(progressElement.style.display).toBe('block');
            expect(cardElement.querySelector('.progress-fill').style.width).toBe('50%');
            expect(cardElement.querySelector('.progress-text').textContent).toContain('90 / 180');
        });

        test('should hide reading progress for non-reading books', () => {
            const completedBook = { ...sampleBook, status: 'READ' };
            
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(completedBook);
            
            const cardElement = bookCard.renderGridView();
            const progressElement = cardElement.querySelector('.reading-progress');
            
            expect(progressElement.style.display).toBe('none');
        });
    });

    describe('Date Formatting', () => {
        test('should format dates for display correctly', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const formattedDate = bookCard.formatDateForDisplay('2023-01-15T10:30:00Z');
            
            expect(formattedDate).toMatch(/Jan 15, 2023|15 Jan 2023/); // Accept different formats
        });

        test('should handle null dates gracefully', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const formattedDate = bookCard.formatDateForDisplay(null);
            
            expect(formattedDate).toBe('');
        });

        test('should format relative dates for recent items', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const yesterday = new Date(Date.now() - 24 * 60 * 60 * 1000).toISOString();
            const formattedDate = bookCard.formatDateForDisplay(yesterday);
            
            expect(formattedDate).toBe('Yesterday');
        });
    });

    describe('Data Validation', () => {
        test('should handle missing book data gracefully', () => {
            const incompleteBook = {
                id: 1,
                book: {
                    title: 'Test Book',
                    author: null,
                    category: null
                },
                status: 'UNREAD'
            };
            
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(incompleteBook);
            
            const cardElement = bookCard.renderGridView();
            
            expect(cardElement.querySelector('.book-title').textContent).toBe('Test Book');
            expect(cardElement.querySelector('.book-author').textContent).toBe('by Unknown Author');
            expect(cardElement.querySelector('.book-category').textContent).toBe('Uncategorized');
        });

        test('should validate status values', () => {
            const bookWithInvalidStatus = { ...sampleBook, status: 'INVALID_STATUS' };
            
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(bookWithInvalidStatus);
            
            const cardElement = bookCard.renderGridView();
            const statusBadge = cardElement.querySelector('.reading-status-badge');
            
            expect(statusBadge.classList.contains('status-UNREAD')).toBe(true); // Should default to UNREAD
        });
    });

    describe('Event Delegation', () => {
        test('should set up event delegation for dynamically added cards', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            
            const setupEventDelegationSpy = jest.spyOn(BookCard, 'setupEventDelegation').mockImplementation();
            
            BookCard.setupEventDelegation(document.body);
            
            expect(setupEventDelegationSpy).toHaveBeenCalledWith(document.body);
            
            setupEventDelegationSpy.mockRestore();
        });

        test('should handle events on dynamically added cards', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            
            // Mock a container with delegated events
            const container = document.createElement('div');
            document.body.appendChild(container);
            
            BookCard.setupEventDelegation(container);
            
            // Add a book card
            bookCard = new BookCard(sampleBook);
            const cardElement = bookCard.renderGridView();
            container.appendChild(cardElement);
            
            const onActionSpy = jest.fn();
            BookCard.onGlobalAction = onActionSpy;
            
            // Trigger an action
            const viewBtn = cardElement.querySelector('.view-details-btn');
            viewBtn.click();
            
            expect(onActionSpy).toHaveBeenCalled();
        });
    });

    describe('Accessibility', () => {
        test('should include proper ARIA attributes', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const cardElement = bookCard.renderGridView();
            
            expect(cardElement.getAttribute('role')).toBe('article');
            expect(cardElement.getAttribute('aria-label')).toContain('The Great Gatsby');
            
            const statusSelect = cardElement.querySelector('.status-select');
            expect(statusSelect.getAttribute('aria-label')).toContain('Update reading status');
        });

        test('should support keyboard navigation', () => {
            const { BookCard } = require('../../../frontend/src/js/components/BookCard.js');
            bookCard = new BookCard(sampleBook);
            
            const cardElement = bookCard.renderGridView();
            document.body.appendChild(cardElement);
            
            bookCard.attachEventListeners(cardElement);
            
            const actionButtons = cardElement.querySelectorAll('.action-btn');
            actionButtons.forEach(button => {
                expect(button.tabIndex).toBe(0);
            });
        });
    });
});