/**
 * Test Suite for SearchResults Component
 * Tests all functionality including search, pagination, view modes, and integrations
 */

// Import the SearchResults component
const SearchResults = require('../../src/js/components/SearchResults');

// Mock dependencies
const mockBookSearchService = {
    searchBooks: jest.fn(),
    getBookById: jest.fn(),
    getPopularSearches: jest.fn()
};

const mockLibraryService = {
    getUserLibrary: jest.fn(),
    addBook: jest.fn(),
    removeFromLibrary: jest.fn()
};

const mockNotificationService = {
    showSuccess: jest.fn(),
    showError: jest.fn(),
    showInfo: jest.fn()
};

const mockBookDetailModal = {
    show: jest.fn()
};

// Setup global mocks for window object
Object.defineProperty(window, 'bookSearchService', {
    value: mockBookSearchService,
    writable: true
});

Object.defineProperty(window, 'libraryService', {
    value: mockLibraryService,
    writable: true
});

Object.defineProperty(window, 'notificationService', {
    value: mockNotificationService,
    writable: true
});

Object.defineProperty(window, 'bookDetailModal', {
    value: mockBookDetailModal,
    writable: true
});

describe('SearchResults Component', () => {
    let searchResults;
    let container;

    // Mock search results data
    const mockSearchResponse = {
        success: true,
        data: {
            content: [
                {
                    id: 1,
                    title: 'The Great Gatsby',
                    author: 'F. Scott Fitzgerald',
                    description: 'A classic American novel',
                    coverImageUrl: 'https://example.com/gatsby.jpg',
                    averageRating: 4.2,
                    ratingCount: 1523,
                    publicationYear: 1925,
                    pageCount: 180,
                    category: {
                        id: 1,
                        name: 'Fiction'
                    }
                },
                {
                    id: 2,
                    title: '1984',
                    author: 'George Orwell',
                    description: 'Dystopian social science fiction novel',
                    coverImageUrl: null,
                    averageRating: 4.5,
                    ratingCount: 2103,
                    publicationYear: 1949,
                    pageCount: 328,
                    category: {
                        id: 2,
                        name: 'Science Fiction'
                    }
                }
            ],
            totalElements: 25,
            totalPages: 3,
            page: 0,
            size: 20
        }
    };

    const mockLibraryResponse = {
        success: true,
        data: {
            content: [
                { book: { id: 1 } } // Book with id 1 is in library
            ]
        }
    };

    beforeEach(() => {
        // Create test container
        document.body.innerHTML = `
            <div id="search-results-container"></div>
        `;
        
        container = document.getElementById('search-results-container');
        
        // Setup global mocks
        global.bookSearchService = mockBookSearchService;
        global.libraryService = mockLibraryService;
        global.notificationService = mockNotificationService;
        global.bookDetailModal = mockBookDetailModal;
        
        // Reset all mocks
        jest.clearAllMocks();
        
        // Setup default mock responses
        mockLibraryService.getUserLibrary.mockResolvedValue(mockLibraryResponse);
        mockBookSearchService.searchBooks.mockResolvedValue(mockSearchResponse);
        mockBookSearchService.getPopularSearches.mockResolvedValue({
            success: true,
            data: [
                { query: 'javascript programming' },
                { query: 'science fiction' },
                { query: 'mystery novels' }
            ]
        });
    });

    afterEach(() => {
        if (searchResults) {
            searchResults.destroy();
        }
        jest.clearAllMocks();
        document.body.innerHTML = '';
    });

    describe('Initialization', () => {
        test('should initialize correctly with container', async () => {
            searchResults = new SearchResults('search-results-container');
            
            // Wait for async initialization
            await new Promise(resolve => setTimeout(resolve, 100));
            
            expect(searchResults.container).toBe(container);
            expect(searchResults.viewMode).toBe('grid');
            expect(searchResults.currentPage).toBe(0);
            expect(searchResults.pageSize).toBe(20);
            expect(mockLibraryService.getUserLibrary).toHaveBeenCalled();
        });

        test('should handle missing container gracefully', () => {
            const consoleSpy = jest.spyOn(console, 'error').mockImplementation();
            
            searchResults = new SearchResults('non-existent-container');
            
            expect(consoleSpy).toHaveBeenCalledWith('SearchResults: Container element not found');
            
            consoleSpy.mockRestore();
        });

        test('should create proper HTML structure', async () => {
            searchResults = new SearchResults('search-results-container');
            await new Promise(resolve => setTimeout(resolve, 100));
            
            expect(container.querySelector('.search-results-header')).toBeTruthy();
            expect(container.querySelector('#results-count')).toBeTruthy();
            expect(container.querySelector('#view-toggle')).toBeTruthy();
            expect(container.querySelector('#sort-select')).toBeTruthy();
            expect(container.querySelector('#results-container')).toBeTruthy();
            expect(container.querySelector('#pagination-container')).toBeTruthy();
        });
    });

    describe('Search Functionality', () => {
        beforeEach(async () => {
            searchResults = new SearchResults('search-results-container');
            await new Promise(resolve => setTimeout(resolve, 100));
        });

        test('should perform search with query and filters', async () => {
            const query = 'javascript';
            const filters = { category: 1, minRating: 4 };
            
            await searchResults.searchBooks(query, filters, 0);
            
            expect(mockBookSearchService.searchBooks).toHaveBeenCalledWith({
                q: query,
                category: 1,
                minRating: 4,
                page: 0,
                size: 20,
                sort: 'relevance',
                direction: 'asc'
            });
            
            expect(searchResults.currentQuery).toBe(query);
            expect(searchResults.currentFilters).toEqual(expect.objectContaining(filters));
        });

        test('should display search results correctly', async () => {
            await searchResults.searchBooks('test query');
            
            const resultsGrid = container.querySelector('#results-grid');
            const bookCards = resultsGrid.querySelectorAll('.book-card');
            
            expect(bookCards.length).toBe(2);
            expect(bookCards[0].querySelector('.book-card-title').textContent).toBe('The Great Gatsby');
            expect(bookCards[1].querySelector('.book-card-title').textContent).toBe('1984');
        });

        test('should show loading state during search', async () => {
            // Make search take longer
            mockBookSearchService.searchBooks.mockImplementation(() => 
                new Promise(resolve => setTimeout(() => resolve(mockSearchResponse), 100))
            );
            
            const searchPromise = searchResults.searchBooks('test');
            
            expect(container.querySelector('#loading-state').style.display).toBe('block');
            
            await searchPromise;
            
            expect(container.querySelector('#loading-state').style.display).toBe('none');
        });

        test('should handle search errors gracefully', async () => {
            const errorResponse = {
                success: false,
                message: 'Search service unavailable'
            };
            
            mockBookSearchService.searchBooks.mockResolvedValue(errorResponse);
            
            await searchResults.searchBooks('test query');
            
            expect(container.querySelector('#error-state').style.display).toBe('block');
            expect(container.querySelector('#error-message').textContent).toBe('Search service unavailable');
        });

        test('should show empty state when no results found', async () => {
            const emptyResponse = {
                success: true,
                data: {
                    content: [],
                    totalElements: 0,
                    totalPages: 0
                }
            };
            
            mockBookSearchService.searchBooks.mockResolvedValue(emptyResponse);
            
            await searchResults.searchBooks('nonexistent book');
            
            expect(container.querySelector('#empty-state').style.display).toBe('block');
            expect(container.querySelector('#empty-message').textContent).toContain('No books found for "nonexistent book"');
        });
    });

    describe('View Mode Toggle', () => {
        beforeEach(async () => {
            searchResults = new SearchResults('search-results-container');
            await new Promise(resolve => setTimeout(resolve, 100));
            await searchResults.searchBooks('test');
        });

        test('should switch from grid to list view', () => {
            const listViewBtn = container.querySelector('[data-view="list"]');
            listViewBtn.click();
            
            expect(searchResults.viewMode).toBe('list');
            expect(listViewBtn.classList.contains('active')).toBe(true);
            expect(container.querySelector('[data-view="grid"]').classList.contains('active')).toBe(false);
            
            const resultsContainer = container.querySelector('#results-grid');
            expect(resultsContainer.classList.contains('results-list')).toBe(true);
        });

        test('should switch from list to grid view', () => {
            // First switch to list
            container.querySelector('[data-view="list"]').click();
            
            // Then switch back to grid
            const gridViewBtn = container.querySelector('[data-view="grid"]');
            gridViewBtn.click();
            
            expect(searchResults.viewMode).toBe('grid');
            expect(gridViewBtn.classList.contains('active')).toBe(true);
            
            const resultsContainer = container.querySelector('#results-grid');
            expect(resultsContainer.classList.contains('results-grid')).toBe(true);
        });

        test('should render book cards differently in list vs grid view', () => {
            // Check grid view
            let bookCard = container.querySelector('.book-card');
            expect(bookCard.classList.contains('list-view')).toBe(false);
            
            // Switch to list view
            container.querySelector('[data-view="list"]').click();
            
            bookCard = container.querySelector('.book-card');
            expect(bookCard.classList.contains('list-view')).toBe(true);
        });
    });

    describe('Pagination', () => {
        beforeEach(async () => {
            searchResults = new SearchResults('search-results-container');
            await new Promise(resolve => setTimeout(resolve, 100));
            await searchResults.searchBooks('test');
        });

        test('should show pagination when multiple pages exist', () => {
            const paginationContainer = container.querySelector('#pagination-container');
            expect(paginationContainer.style.display).toBe('block');
            
            const paginationInfo = container.querySelector('#pagination-info');
            expect(paginationInfo.textContent).toBe('Showing 1-20 of 25 books');
        });

        test('should handle next page click', async () => {
            const nextBtn = container.querySelector('#next-btn');
            expect(nextBtn.disabled).toBe(false);
            
            nextBtn.click();
            
            // Wait for the search to complete
            await new Promise(resolve => setTimeout(resolve, 50));
            
            expect(mockBookSearchService.searchBooks).toHaveBeenCalledWith(
                expect.objectContaining({ page: 1 })
            );
        });

        test('should handle previous page click', async () => {
            // Go to page 1 first
            searchResults.currentPage = 1;
            searchResults.updatePagination();
            
            const prevBtn = container.querySelector('#prev-btn');
            expect(prevBtn.disabled).toBe(false);
            
            prevBtn.click();
            
            await new Promise(resolve => setTimeout(resolve, 50));
            
            expect(mockBookSearchService.searchBooks).toHaveBeenCalledWith(
                expect.objectContaining({ page: 0 })
            );
        });

        test('should disable buttons at boundaries', () => {
            // At first page
            expect(container.querySelector('#prev-btn').disabled).toBe(true);
            
            // Simulate being at last page
            searchResults.currentPage = 2;
            searchResults.totalPages = 3;
            searchResults.updatePagination();
            
            expect(container.querySelector('#next-btn').disabled).toBe(true);
        });

        test('should hide pagination for single page results', async () => {
            const singlePageResponse = {
                success: true,
                data: {
                    content: [mockSearchResponse.data.content[0]],
                    totalElements: 1,
                    totalPages: 1
                }
            };
            
            mockBookSearchService.searchBooks.mockResolvedValue(singlePageResponse);
            await searchResults.searchBooks('single result');
            
            const paginationContainer = container.querySelector('#pagination-container');
            expect(paginationContainer.style.display).toBe('none');
        });
    });

    describe('Sorting', () => {
        beforeEach(async () => {
            searchResults = new SearchResults('search-results-container');
            await new Promise(resolve => setTimeout(resolve, 100));
            await searchResults.searchBooks('test');
        });

        test('should change sort field', async () => {
            const sortSelect = container.querySelector('#sort-select');
            sortSelect.value = 'title';
            sortSelect.dispatchEvent(new Event('change'));
            
            await new Promise(resolve => setTimeout(resolve, 50));
            
            expect(mockBookSearchService.searchBooks).toHaveBeenCalledWith(
                expect.objectContaining({ sort: 'title' })
            );
        });

        test('should toggle sort direction', async () => {
            const sortDirectionBtn = container.querySelector('#sort-direction');
            sortDirectionBtn.click();
            
            await new Promise(resolve => setTimeout(resolve, 50));
            
            expect(mockBookSearchService.searchBooks).toHaveBeenCalledWith(
                expect.objectContaining({ direction: 'desc' })
            );
            
            // Check icon changed
            const icon = sortDirectionBtn.querySelector('i');
            expect(icon.classList.contains('fa-sort-amount-up')).toBe(true);
        });

        test('should reset to first page when sorting changes', async () => {
            searchResults.currentPage = 2;
            
            const sortSelect = container.querySelector('#sort-select');
            sortSelect.value = 'rating';
            sortSelect.dispatchEvent(new Event('change'));
            
            await new Promise(resolve => setTimeout(resolve, 50));
            
            expect(mockBookSearchService.searchBooks).toHaveBeenCalledWith(
                expect.objectContaining({ page: 0, sort: 'rating' })
            );
        });
    });

    describe('Book Card Interactions', () => {
        beforeEach(async () => {
            searchResults = new SearchResults('search-results-container');
            await new Promise(resolve => setTimeout(resolve, 100));
            await searchResults.searchBooks('test');
        });

        test('should handle view details button click', async () => {
            const viewDetailsBtn = container.querySelector('.btn-view-details');
            viewDetailsBtn.click();
            
            expect(mockBookDetailModal.show).toHaveBeenCalledWith(1);
        });

        test('should handle add to library button click', async () => {
            mockLibraryService.addBook.mockResolvedValue({ success: true });
            
            // Find a book not in library (book id 2)
            const bookCards = container.querySelectorAll('.book-card');
            const book2Card = Array.from(bookCards).find(card => card.dataset.bookId === '2');
            const addButton = book2Card.querySelector('.btn-add-to-library');
            
            addButton.click();
            
            await new Promise(resolve => setTimeout(resolve, 50));
            
            expect(mockLibraryService.addBook).toHaveBeenCalledWith(2);
            expect(mockNotificationService.showSuccess).toHaveBeenCalledWith('"1984" added to your library!');
        });

        test('should show error when add to library fails', async () => {
            mockLibraryService.addBook.mockResolvedValue({
                success: false,
                message: 'Book already in library'
            });
            
            const bookCards = container.querySelectorAll('.book-card');
            const book2Card = Array.from(bookCards).find(card => card.dataset.bookId === '2');
            const addButton = book2Card.querySelector('.btn-add-to-library');
            
            addButton.click();
            
            await new Promise(resolve => setTimeout(resolve, 50));
            
            expect(mockNotificationService.showError).toHaveBeenCalledWith('Book already in library');
        });

        test('should display correct library status for books', () => {
            const bookCards = container.querySelectorAll('.book-card');
            
            // Book 1 should show "In Library" (it's in the mock library response)
            const book1Card = Array.from(bookCards).find(card => card.dataset.bookId === '1');
            expect(book1Card.querySelector('.btn-in-library')).toBeTruthy();
            expect(book1Card.querySelector('.btn-add-to-library')).toBeFalsy();
            
            // Book 2 should show "Add to Library"
            const book2Card = Array.from(bookCards).find(card => card.dataset.bookId === '2');
            expect(book2Card.querySelector('.btn-add-to-library')).toBeTruthy();
            expect(book2Card.querySelector('.btn-in-library')).toBeFalsy();
        });
    });

    describe('Book Card Rendering', () => {
        beforeEach(async () => {
            searchResults = new SearchResults('search-results-container');
            await new Promise(resolve => setTimeout(resolve, 100));
            await searchResults.searchBooks('test');
        });

        test('should render book information correctly', () => {
            const firstBookCard = container.querySelector('.book-card');
            
            expect(firstBookCard.querySelector('.book-card-title').textContent).toBe('The Great Gatsby');
            expect(firstBookCard.querySelector('.book-card-author').textContent).toBe('by F. Scott Fitzgerald');
            expect(firstBookCard.querySelector('.book-category').textContent).toBe('Fiction');
            expect(firstBookCard.querySelector('.publication-year').textContent).toBe('1925');
            expect(firstBookCard.querySelector('.page-count').textContent).toBe('180 pages');
        });

        test('should render rating stars correctly', () => {
            const firstBookCard = container.querySelector('.book-card');
            const stars = firstBookCard.querySelectorAll('.star');
            
            expect(stars.length).toBe(5);
            
            // Check rating display (4.2 should show 4 full stars, 1 half star, 0 empty)
            const fullStars = firstBookCard.querySelectorAll('.star:not(.empty)');
            expect(fullStars.length).toBeGreaterThan(3); // At least 4 stars should be filled
        });

        test('should handle books without cover images', () => {
            const secondBookCard = container.querySelectorAll('.book-card')[1];
            expect(secondBookCard.querySelector('.book-cover-placeholder')).toBeTruthy();
        });

        test('should render books without ratings', () => {
            const bookWithoutRating = {
                ...mockSearchResponse.data.content[0],
                averageRating: 0,
                ratingCount: 0
            };
            
            const bookElement = searchResults.renderBookCard(bookWithoutRating);
            expect(bookElement.querySelector('.rating-text').textContent.trim()).toBe('No ratings');
        });
    });

    describe('Utility Methods', () => {
        beforeEach(async () => {
            searchResults = new SearchResults('search-results-container');
            await new Promise(resolve => setTimeout(resolve, 100));
        });

        test('should escape HTML correctly', () => {
            const maliciousText = '<script>alert("xss")</script>';
            const escaped = searchResults.escapeHtml(maliciousText);
            expect(escaped).toBe('&lt;script&gt;alert("xss")&lt;/script&gt;');
        });

        test('should handle non-string input in escapeHtml', () => {
            expect(searchResults.escapeHtml(null)).toBe('');
            expect(searchResults.escapeHtml(undefined)).toBe('');
            expect(searchResults.escapeHtml(123)).toBe('');
        });

        test('should highlight search terms correctly', () => {
            const text = 'This is a test string';
            const highlighted = searchResults.highlightSearchTerms(text, ['test']);
            expect(highlighted).toContain('<span class="search-highlight">test</span>');
        });

        test('should format results count correctly', () => {
            searchResults.totalElements = 0;
            expect(searchResults.formatResultsCount()).toBe('No results found');
            
            searchResults.totalElements = 1;
            expect(searchResults.formatResultsCount()).toBe('1 book found');
            
            searchResults.totalElements = 1500;
            expect(searchResults.formatResultsCount()).toBe('1,500 books found');
        });
    });

    describe('State Management', () => {
        beforeEach(async () => {
            searchResults = new SearchResults('search-results-container');
            await new Promise(resolve => setTimeout(resolve, 100));
        });

        test('should return current state correctly', async () => {
            await searchResults.searchBooks('test query', { category: 1 });
            
            const state = searchResults.getCurrentState();
            expect(state.query).toBe('test query');
            expect(state.filters).toEqual(expect.objectContaining({ category: 1 }));
            expect(state.page).toBe(0);
            expect(state.viewMode).toBe('grid');
            expect(state.results).toBe(searchResults.currentResults);
        });

        test('should update filters correctly', () => {
            searchResults.updateFilters({ minRating: 4, category: 2 });
            expect(searchResults.currentFilters).toEqual(
                expect.objectContaining({ minRating: 4, category: 2 })
            );
        });

        test('should clear state correctly', () => {
            searchResults.currentQuery = 'test';
            searchResults.currentFilters = { category: 1 };
            searchResults.currentPage = 2;
            
            searchResults.clear();
            
            expect(searchResults.currentQuery).toBeNull();
            expect(searchResults.currentFilters).toEqual({});
            expect(searchResults.currentPage).toBe(0);
            expect(searchResults.totalElements).toBe(0);
        });
    });

    describe('Error Handling', () => {
        beforeEach(async () => {
            searchResults = new SearchResults('search-results-container');
            await new Promise(resolve => setTimeout(resolve, 100));
        });

        test('should handle missing bookSearchService', async () => {
            searchResults.bookSearchService = null;
            
            await searchResults.searchBooks('test');
            
            expect(container.querySelector('#error-state').style.display).toBe('block');
            expect(container.querySelector('#error-message').textContent).toBe('Search service not available');
        });

        test('should handle network errors gracefully', async () => {
            mockBookSearchService.searchBooks.mockRejectedValue(new Error('Network error'));
            
            await searchResults.searchBooks('test');
            
            expect(container.querySelector('#error-state').style.display).toBe('block');
            expect(container.querySelector('#error-message').textContent).toBe('Network error');
        });

        test('should retry search when retry button is clicked', async () => {
            // First search fails
            mockBookSearchService.searchBooks.mockRejectedValueOnce(new Error('Network error'));
            await searchResults.searchBooks('test');
            
            // Setup successful retry
            mockBookSearchService.searchBooks.mockResolvedValue(mockSearchResponse);
            
            const retryBtn = container.querySelector('#retry-btn');
            retryBtn.click();
            
            await new Promise(resolve => setTimeout(resolve, 50));
            
            expect(mockBookSearchService.searchBooks).toHaveBeenCalledTimes(2);
        });
    });

    describe('Integration Events', () => {
        beforeEach(async () => {
            searchResults = new SearchResults('search-results-container');
            await new Promise(resolve => setTimeout(resolve, 100));
        });

        test('should emit suggestion-click event', async () => {
            const emptyResponse = {
                success: true,
                data: { content: [], totalElements: 0, totalPages: 0 }
            };
            mockBookSearchService.searchBooks.mockResolvedValue(emptyResponse);
            
            await searchResults.searchBooks('nonexistent');
            
            // Wait for suggestions to load
            await new Promise(resolve => setTimeout(resolve, 100));
            
            let eventFired = false;
            container.addEventListener('suggestion-click', (event) => {
                eventFired = true;
                expect(event.detail.suggestion).toBe('javascript programming');
            });
            
            const suggestionBtn = container.querySelector('.suggestion-tag');
            if (suggestionBtn) {
                suggestionBtn.click();
                expect(eventFired).toBe(true);
            }
        });
    });

    describe('Component Lifecycle', () => {
        test('should destroy component cleanly', async () => {
            searchResults = new SearchResults('search-results-container');
            await new Promise(resolve => setTimeout(resolve, 100));
            
            searchResults.destroy();
            
            expect(searchResults.container).toBeNull();
            expect(searchResults.currentResults).toBeNull();
            expect(container.innerHTML).toBe('');
        });
    });
});