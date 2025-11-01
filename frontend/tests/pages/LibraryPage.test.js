/**
 * Tests for LibraryPage functionality
 * Tests the main library page JavaScript that coordinates all components
 */

import { expect } from '@jest/globals';

// Mock the libraryService
jest.mock('../../../frontend/src/js/services/libraryService.js', () => ({
    getUserLibrary: jest.fn(),
    searchUserLibrary: jest.fn(),
    updateLibraryEntry: jest.fn(),
    removeBookFromLibrary: jest.fn(),
    getLibraryStatistics: jest.fn()
}));

// Mock DOM elements and methods
Object.defineProperty(window, 'location', {
    value: {
        href: 'http://localhost:3000/library',
        pathname: '/library'
    },
    writable: true
});

// Mock global alert and confirm
global.alert = jest.fn();
global.confirm = jest.fn(() => true);

describe('LibraryPage', () => {
    let libraryPage;
    let mockLibraryService;
    
    // Mock DOM elements
    let mockElements;

    beforeEach(() => {
        // Reset DOM
        document.body.innerHTML = `
            <div id="loading-state" class="loading-state"></div>
            <div id="empty-state" class="empty-state" style="display: none;"></div>
            <div id="no-results-state" class="no-results-state" style="display: none;"></div>
            <div id="books-grid" class="books-grid" style="display: none;"></div>
            <div id="books-list" class="books-list" style="display: none;"></div>
            <div id="pagination-container" class="pagination-container" style="display: none;"></div>
            
            <!-- Statistics -->
            <div id="total-books" class="stat-number">0</div>
            <div id="books-read" class="stat-number">0</div>
            <div id="currently-reading" class="stat-number">0</div>
            <div id="want-to-read" class="stat-number">0</div>
            
            <!-- Search and Filters -->
            <input id="library-search" class="search-input" type="search" />
            <button id="clear-search" class="clear-search-btn"></button>
            <select id="status-filter" class="filter-select">
                <option value="">All Books</option>
                <option value="UNREAD">Unread</option>
                <option value="READING">Reading</option>
                <option value="READ">Read</option>
                <option value="DNF">Did Not Finish</option>
            </select>
            <select id="category-filter" class="filter-select">
                <option value="">All Categories</option>
            </select>
            <select id="sort-select" class="filter-select">
                <option value="dateAdded">Date Added</option>
                <option value="title">Title</option>
                <option value="author">Author</option>
            </select>
            <button id="sort-order-btn" class="sort-order-btn">
                <i class="fas fa-sort-amount-down"></i>
                <span class="sort-order-text">Desc</span>
            </button>
            
            <!-- View Controls -->
            <button id="grid-view-btn" class="view-btn active"></button>
            <button id="list-view-btn" class="view-btn"></button>
            <button id="add-book-btn" class="btn-primary"></button>
            
            <!-- Pagination -->
            <button id="prev-page-btn" class="pagination-btn"></button>
            <button id="next-page-btn" class="pagination-btn"></button>
            <div id="pagination-numbers" class="pagination-numbers"></div>
            <span id="results-info">Showing 0-0 of 0 books</span>
            
            <!-- Filter management -->
            <div id="active-filters" class="active-filters" style="display: none;">
                <div class="active-filters-list"></div>
            </div>
            <button id="clear-all-filters" class="clear-filters-btn"></button>
        `;

        // Mock library service
        mockLibraryService = require('../../../frontend/src/js/services/libraryService.js');
        
        // Reset all mocks
        jest.clearAllMocks();
        
        // Mock successful responses by default
        mockLibraryService.getUserLibrary.mockResolvedValue({
            success: true,
            data: {
                content: [],
                page: { number: 0, size: 20, totalElements: 0, totalPages: 0 }
            }
        });
        
        mockLibraryService.getLibraryStatistics.mockResolvedValue({
            success: true,
            data: {
                totalBooks: 0,
                booksRead: 0,
                currentlyReading: 0,
                wantToRead: 0
            }
        });
    });

    describe('Initialization', () => {
        test('should initialize library page with default state', async () => {
            // Import the library page module (this would normally be done via script tag)
            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.init();
            
            expect(mockLibraryService.getLibraryStatistics).toHaveBeenCalled();
            expect(mockLibraryService.getUserLibrary).toHaveBeenCalledWith({
                page: 0,
                size: 20,
                sort: 'dateAdded',
                direction: 'desc'
            });
        });

        test('should set up event listeners on initialization', async () => {
            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            const addEventListenerSpy = jest.spyOn(document, 'addEventListener');
            
            await libraryPage.init();
            
            // Verify that event listeners are set up
            expect(addEventListenerSpy).toHaveBeenCalled();
        });

        test('should display loading state initially', async () => {
            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            // Before initialization, loading should be visible
            const loadingElement = document.getElementById('loading-state');
            expect(loadingElement.style.display).not.toBe('none');
        });
    });

    describe('Library Data Loading', () => {
        test('should load library statistics successfully', async () => {
            const mockStats = {
                totalBooks: 25,
                booksRead: 15,
                currentlyReading: 3,
                wantToRead: 7
            };
            
            mockLibraryService.getLibraryStatistics.mockResolvedValue({
                success: true,
                data: mockStats
            });

            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.loadStatistics();
            
            expect(document.getElementById('total-books').textContent).toBe('25');
            expect(document.getElementById('books-read').textContent).toBe('15');
            expect(document.getElementById('currently-reading').textContent).toBe('3');
            expect(document.getElementById('want-to-read').textContent).toBe('7');
        });

        test('should load books with pagination', async () => {
            const mockBooks = [
                {
                    id: 1,
                    book: { title: 'Test Book 1', author: 'Author 1' },
                    status: 'READING',
                    dateAdded: '2023-01-01T00:00:00Z'
                },
                {
                    id: 2,
                    book: { title: 'Test Book 2', author: 'Author 2' },
                    status: 'READ',
                    dateAdded: '2023-01-02T00:00:00Z'
                }
            ];

            mockLibraryService.getUserLibrary.mockResolvedValue({
                success: true,
                data: {
                    content: mockBooks,
                    page: { number: 0, size: 20, totalElements: 2, totalPages: 1 }
                }
            });

            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.loadBooks();
            
            expect(mockLibraryService.getUserLibrary).toHaveBeenCalled();
            expect(document.getElementById('loading-state').style.display).toBe('none');
        });

        test('should display empty state when no books', async () => {
            mockLibraryService.getUserLibrary.mockResolvedValue({
                success: true,
                data: {
                    content: [],
                    page: { number: 0, size: 20, totalElements: 0, totalPages: 0 }
                }
            });

            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.loadBooks();
            
            expect(document.getElementById('empty-state').style.display).toBe('block');
            expect(document.getElementById('books-grid').style.display).toBe('none');
        });

        test('should handle API errors gracefully', async () => {
            mockLibraryService.getUserLibrary.mockResolvedValue({
                success: false,
                message: 'Failed to fetch library'
            });

            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
            
            await libraryPage.loadBooks();
            
            expect(consoleSpy).toHaveBeenCalledWith('Error loading books:', 'Failed to fetch library');
            
            consoleSpy.mockRestore();
        });
    });

    describe('Search and Filtering', () => {
        test('should filter books by search query', async () => {
            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.init();
            
            const searchInput = document.getElementById('library-search');
            searchInput.value = 'test query';
            
            // Trigger search
            searchInput.dispatchEvent(new Event('input'));
            
            // Wait for debounce
            await new Promise(resolve => setTimeout(resolve, 350));
            
            expect(mockLibraryService.searchUserLibrary).toHaveBeenCalledWith('test query', expect.any(Object));
        });

        test('should filter books by reading status', async () => {
            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.init();
            
            const statusFilter = document.getElementById('status-filter');
            statusFilter.value = 'READING';
            
            statusFilter.dispatchEvent(new Event('change'));
            
            expect(mockLibraryService.getUserLibrary).toHaveBeenCalledWith(
                expect.objectContaining({ status: 'READING' })
            );
        });

        test('should update sort order', async () => {
            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.init();
            
            const sortOrderBtn = document.getElementById('sort-order-btn');
            sortOrderBtn.click();
            
            expect(mockLibraryService.getUserLibrary).toHaveBeenCalledWith(
                expect.objectContaining({ direction: 'asc' })
            );
        });

        test('should clear all filters', async () => {
            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.init();
            
            // Set some filters
            document.getElementById('status-filter').value = 'READING';
            document.getElementById('library-search').value = 'test';
            
            // Clear filters
            const clearFiltersBtn = document.getElementById('clear-all-filters');
            clearFiltersBtn.click();
            
            expect(document.getElementById('status-filter').value).toBe('');
            expect(document.getElementById('library-search').value).toBe('');
        });
    });

    describe('View Toggle', () => {
        test('should switch to list view', async () => {
            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.init();
            
            const listViewBtn = document.getElementById('list-view-btn');
            const gridViewBtn = document.getElementById('grid-view-btn');
            
            listViewBtn.click();
            
            expect(listViewBtn.classList.contains('active')).toBe(true);
            expect(gridViewBtn.classList.contains('active')).toBe(false);
        });

        test('should switch to grid view', async () => {
            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.init();
            
            const listViewBtn = document.getElementById('list-view-btn');
            const gridViewBtn = document.getElementById('grid-view-btn');
            
            // First switch to list view
            listViewBtn.click();
            // Then switch back to grid view
            gridViewBtn.click();
            
            expect(gridViewBtn.classList.contains('active')).toBe(true);
            expect(listViewBtn.classList.contains('active')).toBe(false);
        });
    });

    describe('Pagination', () => {
        test('should handle next page navigation', async () => {
            mockLibraryService.getUserLibrary.mockResolvedValue({
                success: true,
                data: {
                    content: [{ id: 1 }],
                    page: { number: 0, size: 20, totalElements: 25, totalPages: 2 }
                }
            });

            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.init();
            
            const nextPageBtn = document.getElementById('next-page-btn');
            nextPageBtn.click();
            
            expect(mockLibraryService.getUserLibrary).toHaveBeenLastCalledWith(
                expect.objectContaining({ page: 1 })
            );
        });

        test('should handle previous page navigation', async () => {
            mockLibraryService.getUserLibrary.mockResolvedValue({
                success: true,
                data: {
                    content: [{ id: 1 }],
                    page: { number: 1, size: 20, totalElements: 25, totalPages: 2 }
                }
            });

            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            libraryPage.currentPage = 1; // Start on page 2
            
            await libraryPage.init();
            
            const prevPageBtn = document.getElementById('prev-page-btn');
            prevPageBtn.click();
            
            expect(mockLibraryService.getUserLibrary).toHaveBeenLastCalledWith(
                expect.objectContaining({ page: 0 })
            );
        });

        test('should disable pagination buttons appropriately', async () => {
            // Test first page
            mockLibraryService.getUserLibrary.mockResolvedValue({
                success: true,
                data: {
                    content: [{ id: 1 }],
                    page: { number: 0, size: 20, totalElements: 25, totalPages: 2 }
                }
            });

            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.loadBooks();
            
            const prevPageBtn = document.getElementById('prev-page-btn');
            const nextPageBtn = document.getElementById('next-page-btn');
            
            expect(prevPageBtn.disabled).toBe(true);
            expect(nextPageBtn.disabled).toBe(false);
        });
    });

    describe('Book Status Updates', () => {
        test('should update book status successfully', async () => {
            mockLibraryService.updateLibraryEntry.mockResolvedValue({
                success: true,
                data: { id: 1, status: 'READ' }
            });

            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.updateBookStatus(1, 'read');
            
            expect(mockLibraryService.updateLibraryEntry).toHaveBeenCalledWith(1, { status: 'read' });
        });

        test('should handle book status update errors', async () => {
            mockLibraryService.updateLibraryEntry.mockResolvedValue({
                success: false,
                message: 'Update failed'
            });

            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
            
            await libraryPage.updateBookStatus(1, 'read');
            
            expect(consoleSpy).toHaveBeenCalledWith('Error updating book status:', 'Update failed');
            
            consoleSpy.mockRestore();
        });
    });

    describe('Book Removal', () => {
        test('should remove book with confirmation', async () => {
            global.confirm.mockReturnValue(true);
            mockLibraryService.removeBookFromLibrary.mockResolvedValue({
                success: true
            });

            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.removeBook(1);
            
            expect(global.confirm).toHaveBeenCalledWith(
                expect.stringContaining('Are you sure you want to remove this book')
            );
            expect(mockLibraryService.removeBookFromLibrary).toHaveBeenCalledWith(1);
        });

        test('should not remove book if user cancels', async () => {
            global.confirm.mockReturnValue(false);

            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.removeBook(1);
            
            expect(mockLibraryService.removeBookFromLibrary).not.toHaveBeenCalled();
        });

        test('should handle book removal errors', async () => {
            global.confirm.mockReturnValue(true);
            mockLibraryService.removeBookFromLibrary.mockResolvedValue({
                success: false,
                message: 'Removal failed'
            });

            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
            
            await libraryPage.removeBook(1);
            
            expect(consoleSpy).toHaveBeenCalledWith('Error removing book:', 'Removal failed');
            
            consoleSpy.mockRestore();
        });
    });

    describe('State Management', () => {
        test('should maintain filter state during page navigation', async () => {
            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            await libraryPage.init();
            
            // Set filters
            document.getElementById('status-filter').value = 'READING';
            document.getElementById('sort-select').value = 'title';
            
            // Navigate to next page
            const nextPageBtn = document.getElementById('next-page-btn');
            nextPageBtn.click();
            
            expect(mockLibraryService.getUserLibrary).toHaveBeenLastCalledWith(
                expect.objectContaining({
                    status: 'READING',
                    sort: 'title',
                    page: 1
                })
            );
        });

        test('should reset page when filters change', async () => {
            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            libraryPage.currentPage = 2; // Start on page 3
            
            await libraryPage.init();
            
            const statusFilter = document.getElementById('status-filter');
            statusFilter.value = 'READ';
            statusFilter.dispatchEvent(new Event('change'));
            
            expect(mockLibraryService.getUserLibrary).toHaveBeenLastCalledWith(
                expect.objectContaining({ page: 0 })
            );
        });
    });

    describe('Error Handling', () => {
        test('should display error message for network failures', async () => {
            mockLibraryService.getUserLibrary.mockRejectedValue(new Error('Network error'));

            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
            
            await libraryPage.loadBooks();
            
            expect(consoleSpy).toHaveBeenCalledWith('Error loading books:', expect.any(Error));
            
            consoleSpy.mockRestore();
        });

        test('should handle malformed API responses', async () => {
            mockLibraryService.getUserLibrary.mockResolvedValue(null);

            const { LibraryPage } = require('../../../frontend/src/js/pages/libraryPage.js');
            libraryPage = new LibraryPage();
            
            const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
            
            await libraryPage.loadBooks();
            
            expect(consoleSpy).toHaveBeenCalled();
            
            consoleSpy.mockRestore();
        });
    });
});