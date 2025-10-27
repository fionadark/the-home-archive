/**
 * @jest-environment jsdom
 */

import BookSearchPage from '../../src/js/pages/bookSearchPage.js';
import { BookService } from '../../src/js/services/BookService.js';
import { NotificationService } from '../../src/js/services/NotificationService.js';

// Mock the services
jest.mock('../../src/js/services/BookService.js');
jest.mock('../../src/js/services/NotificationService.js');

describe('BookSearchPage', () => {
    let bookSearchPage;
    let mockBookService;
    let mockNotificationService;
    let container;

    beforeEach(() => {
        // Setup DOM
        document.body.innerHTML = '';
        container = document.createElement('div');
        container.id = 'book-search-container';
        document.body.appendChild(container);

        // Mock services
        mockBookService = new BookService();
        mockNotificationService = new NotificationService();
        
        BookService.mockImplementation(() => mockBookService);
        NotificationService.mockImplementation(() => mockNotificationService);

        // Initialize page
        bookSearchPage = new BookSearchPage();
    });

    afterEach(() => {
        jest.clearAllMocks();
        document.body.innerHTML = '';
    });

    describe('Initialization', () => {
        test('should initialize with default state', () => {
            expect(bookSearchPage.currentPage).toBe(0);
            expect(bookSearchPage.totalPages).toBe(0);
            expect(bookSearchPage.searchResults).toEqual([]);
            expect(bookSearchPage.currentSearchQuery).toBe('');
            expect(bookSearchPage.searchFilters).toEqual({
                searchType: 'title',
                category: '',
                yearFrom: '',
                yearTo: '',
                minRating: '',
                author: '',
                includeExternal: false
            });
        });

        test('should render search interface on initialization', () => {
            bookSearchPage.render();
            
            expect(container.querySelector('.search-container')).toBeTruthy();
            expect(container.querySelector('#search-input')).toBeTruthy();
            expect(container.querySelector('#search-type-select')).toBeTruthy();
            expect(container.querySelector('#search-button')).toBeTruthy();
            expect(container.querySelector('#advanced-filters')).toBeTruthy();
            expect(container.querySelector('#search-results')).toBeTruthy();
        });

        test('should initialize with Dark Academia styling', () => {
            bookSearchPage.render();
            
            expect(container.classList.contains('dark-academia')).toBe(true);
            expect(container.querySelector('.search-container').classList.contains('elegant-form')).toBe(true);
        });
    });

    describe('Search Functionality', () => {
        beforeEach(() => {
            bookSearchPage.render();
        });

        test('should perform basic title search', async () => {
            const mockSearchResults = {
                books: {
                    content: [
                        {
                            id: 1,
                            title: 'The Great Gatsby',
                            author: 'F. Scott Fitzgerald',
                            isbn: '978-0-7432-7356-5',
                            description: 'A classic American novel',
                            publicationYear: 1925,
                            categoryName: 'Fiction',
                            averageRating: 4.5,
                            coverImageUrl: 'https://example.com/gatsby.jpg',
                            inUserLibrary: false
                        }
                    ]
                },
                totalElements: 1,
                totalPages: 1,
                currentPage: 0
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);

            const searchInput = container.querySelector('#search-input');
            const searchButton = container.querySelector('#search-button');

            searchInput.value = 'Gatsby';
            searchButton.click();

            await new Promise(resolve => setTimeout(resolve, 0)); // Wait for async

            expect(mockBookService.searchBooks).toHaveBeenCalledWith({
                q: 'Gatsby',
                searchType: 'title',
                page: 0,
                size: 20
            });

            expect(container.querySelector('.search-results')).toBeTruthy();
            expect(container.querySelector('.book-card')).toBeTruthy();
            expect(container.querySelector('.book-title').textContent).toBe('The Great Gatsby');
        });

        test('should perform search by author', async () => {
            const mockSearchResults = {
                books: { content: [] },
                totalElements: 0,
                totalPages: 0,
                currentPage: 0
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);

            const searchInput = container.querySelector('#search-input');
            const searchTypeSelect = container.querySelector('#search-type-select');
            const searchButton = container.querySelector('#search-button');

            searchInput.value = 'Harper Lee';
            searchTypeSelect.value = 'author';
            searchButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockBookService.searchBooks).toHaveBeenCalledWith({
                q: 'Harper Lee',
                searchType: 'author',
                page: 0,
                size: 20
            });
        });

        test('should perform ISBN search', async () => {
            const mockSearchResults = {
                books: { content: [] },
                totalElements: 0,
                totalPages: 0,
                currentPage: 0
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);

            const searchInput = container.querySelector('#search-input');
            const searchTypeSelect = container.querySelector('#search-type-select');
            const searchButton = container.querySelector('#search-button');

            searchInput.value = '978-0-7432-7356-5';
            searchTypeSelect.value = 'isbn';
            searchButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockBookService.searchBooks).toHaveBeenCalledWith({
                q: '978-0-7432-7356-5',
                searchType: 'isbn',
                page: 0,
                size: 20
            });
        });

        test('should handle search with Enter key', async () => {
            const mockSearchResults = {
                books: { content: [] },
                totalElements: 0,
                totalPages: 0,
                currentPage: 0
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);

            const searchInput = container.querySelector('#search-input');
            searchInput.value = 'test query';

            const enterEvent = new KeyboardEvent('keypress', { key: 'Enter' });
            searchInput.dispatchEvent(enterEvent);

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockBookService.searchBooks).toHaveBeenCalledWith({
                q: 'test query',
                searchType: 'title',
                page: 0,
                size: 20
            });
        });

        test('should handle empty search query', async () => {
            const searchInput = container.querySelector('#search-input');
            const searchButton = container.querySelector('#search-button');

            searchInput.value = '';
            searchButton.click();

            expect(mockNotificationService.showError).toHaveBeenCalledWith('Please enter a search term');
            expect(mockBookService.searchBooks).not.toHaveBeenCalled();
        });
    });

    describe('Advanced Search Functionality', () => {
        beforeEach(() => {
            bookSearchPage.render();
        });

        test('should toggle advanced filters visibility', () => {
            const toggleButton = container.querySelector('#toggle-advanced-filters');
            const advancedFilters = container.querySelector('#advanced-filters-content');

            expect(advancedFilters.style.display).toBe('none');

            toggleButton.click();
            expect(advancedFilters.style.display).toBe('block');

            toggleButton.click();
            expect(advancedFilters.style.display).toBe('none');
        });

        test('should perform advanced search with multiple filters', async () => {
            const mockSearchResults = {
                books: { content: [] },
                totalElements: 0,
                totalPages: 0,
                currentPage: 0
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);

            // Show advanced filters
            container.querySelector('#toggle-advanced-filters').click();

            // Set advanced filter values
            container.querySelector('#search-input').value = 'novel';
            container.querySelector('#filter-author').value = 'Lee';
            container.querySelector('#filter-category').value = '1';
            container.querySelector('#filter-year-from').value = '1950';
            container.querySelector('#filter-year-to').value = '1970';
            container.querySelector('#filter-min-rating').value = '4.0';
            container.querySelector('#filter-include-external').checked = true;

            container.querySelector('#search-button').click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockBookService.searchBooks).toHaveBeenCalledWith({
                q: 'novel',
                searchType: 'title',
                author: 'Lee',
                category: '1',
                yearFrom: '1950',
                yearTo: '1970',
                minRating: '4.0',
                includeExternal: true,
                page: 0,
                size: 20
            });
        });

        test('should reset advanced filters', () => {
            // Show advanced filters
            container.querySelector('#toggle-advanced-filters').click();

            // Set some filter values
            container.querySelector('#filter-author').value = 'Test Author';
            container.querySelector('#filter-year-from').value = '2000';
            container.querySelector('#filter-min-rating').value = '3.0';

            // Reset filters
            container.querySelector('#reset-filters-button').click();

            expect(container.querySelector('#filter-author').value).toBe('');
            expect(container.querySelector('#filter-year-from').value).toBe('');
            expect(container.querySelector('#filter-min-rating').value).toBe('');
            expect(container.querySelector('#filter-include-external').checked).toBe(false);
        });

        test('should validate year range filters', async () => {
            container.querySelector('#toggle-advanced-filters').click();

            container.querySelector('#search-input').value = 'test';
            container.querySelector('#filter-year-from').value = '2020';
            container.querySelector('#filter-year-to').value = '2010'; // Invalid: to < from

            container.querySelector('#search-button').click();

            expect(mockNotificationService.showError).toHaveBeenCalledWith('Year range is invalid: "From" year cannot be greater than "To" year');
            expect(mockBookService.searchBooks).not.toHaveBeenCalled();
        });

        test('should validate rating filter', async () => {
            container.querySelector('#toggle-advanced-filters').click();

            container.querySelector('#search-input').value = 'test';
            container.querySelector('#filter-min-rating').value = '6.0'; // Invalid: > 5.0

            container.querySelector('#search-button').click();

            expect(mockNotificationService.showError).toHaveBeenCalledWith('Rating must be between 0 and 5');
            expect(mockBookService.searchBooks).not.toHaveBeenCalled();
        });
    });

    describe('Search Results Display', () => {
        beforeEach(() => {
            bookSearchPage.render();
        });

        test('should display search results correctly', async () => {
            const mockSearchResults = {
                books: {
                    content: [
                        {
                            id: 1,
                            title: 'The Great Gatsby',
                            author: 'F. Scott Fitzgerald',
                            isbn: '978-0-7432-7356-5',
                            description: 'A classic American novel set in the Jazz Age',
                            publicationYear: 1925,
                            categoryName: 'Fiction',
                            averageRating: 4.5,
                            ratingCount: 150,
                            coverImageUrl: 'https://example.com/gatsby.jpg',
                            inUserLibrary: false
                        },
                        {
                            id: 2,
                            title: 'To Kill a Mockingbird',
                            author: 'Harper Lee',
                            isbn: '978-0-06-112008-4',
                            description: 'A gripping tale of racial injustice',
                            publicationYear: 1960,
                            categoryName: 'Fiction',
                            averageRating: 4.8,
                            ratingCount: 200,
                            coverImageUrl: 'https://example.com/mockingbird.jpg',
                            inUserLibrary: true
                        }
                    ]
                },
                totalElements: 2,
                totalPages: 1,
                currentPage: 0
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);

            bookSearchPage.performSearch('fiction', 'title');

            await new Promise(resolve => setTimeout(resolve, 0));

            const bookCards = container.querySelectorAll('.book-card');
            expect(bookCards).toHaveLength(2);

            // Check first book
            expect(bookCards[0].querySelector('.book-title').textContent).toBe('The Great Gatsby');
            expect(bookCards[0].querySelector('.book-author').textContent).toBe('by F. Scott Fitzgerald');
            expect(bookCards[0].querySelector('.book-year').textContent).toBe('1925');
            expect(bookCards[0].querySelector('.book-category').textContent).toBe('Fiction');

            // Check second book
            expect(bookCards[1].querySelector('.book-title').textContent).toBe('To Kill a Mockingbird');
            expect(bookCards[1].querySelector('.book-author').textContent).toBe('by Harper Lee');
            
            // Check library status indicators
            expect(bookCards[0].querySelector('.add-to-library-btn')).toBeTruthy();
            expect(bookCards[1].querySelector('.in-library-indicator')).toBeTruthy();
        });

        test('should display empty results message', async () => {
            const mockSearchResults = {
                books: { content: [] },
                totalElements: 0,
                totalPages: 0,
                currentPage: 0
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);

            bookSearchPage.performSearch('nonexistent', 'title');

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(container.querySelector('.empty-results')).toBeTruthy();
            expect(container.querySelector('.empty-results').textContent).toContain('No books found');
        });

        test('should display loading state during search', () => {
            mockBookService.searchBooks.mockReturnValue(new Promise(() => {})); // Never resolves

            bookSearchPage.performSearch('test', 'title');

            expect(container.querySelector('.loading-spinner')).toBeTruthy();
            expect(container.querySelector('.search-button').disabled).toBe(true);
        });

        test('should display external API results indicator', async () => {
            const mockSearchResults = {
                books: {
                    content: [{
                        id: 1,
                        title: 'External Book',
                        author: 'External Author',
                        isbn: '978-1-111-11111-1',
                        inUserLibrary: false
                    }]
                },
                totalElements: 1,
                totalPages: 1,
                currentPage: 0,
                externalApiUsed: true,
                externalApiSource: 'Google Books'
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);

            bookSearchPage.performSearch('external', 'title');

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(container.querySelector('.external-api-notice')).toBeTruthy();
            expect(container.querySelector('.external-api-notice').textContent).toContain('Google Books');
        });
    });

    describe('Pagination', () => {
        beforeEach(() => {
            bookSearchPage.render();
        });

        test('should display pagination controls', async () => {
            const mockSearchResults = {
                books: { content: Array(20).fill().map((_, i) => ({ id: i, title: `Book ${i}` })) },
                totalElements: 50,
                totalPages: 3,
                currentPage: 0
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);

            bookSearchPage.performSearch('test', 'title');

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(container.querySelector('.pagination')).toBeTruthy();
            expect(container.querySelector('.page-info').textContent).toContain('Page 1 of 3');
            expect(container.querySelector('.next-page-btn')).toBeTruthy();
            expect(container.querySelector('.prev-page-btn').disabled).toBe(true);
        });

        test('should navigate to next page', async () => {
            const mockSearchResults = {
                books: { content: [] },
                totalElements: 50,
                totalPages: 3,
                currentPage: 0
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);

            bookSearchPage.performSearch('test', 'title');
            await new Promise(resolve => setTimeout(resolve, 0));

            // Mock second page results
            const secondPageResults = {
                books: { content: [] },
                totalElements: 50,
                totalPages: 3,
                currentPage: 1
            };
            mockBookService.searchBooks.mockResolvedValue(secondPageResults);

            container.querySelector('.next-page-btn').click();
            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockBookService.searchBooks).toHaveBeenLastCalledWith({
                q: 'test',
                searchType: 'title',
                page: 1,
                size: 20
            });
        });

        test('should navigate to previous page', async () => {
            // Start on page 2
            bookSearchPage.currentPage = 1;
            const mockSearchResults = {
                books: { content: [] },
                totalElements: 50,
                totalPages: 3,
                currentPage: 1
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);

            bookSearchPage.performSearch('test', 'title');
            await new Promise(resolve => setTimeout(resolve, 0));

            // Mock first page results
            const firstPageResults = {
                books: { content: [] },
                totalElements: 50,
                totalPages: 3,
                currentPage: 0
            };
            mockBookService.searchBooks.mockResolvedValue(firstPageResults);

            container.querySelector('.prev-page-btn').click();
            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockBookService.searchBooks).toHaveBeenLastCalledWith({
                q: 'test',
                searchType: 'title',
                page: 0,
                size: 20
            });
        });
    });

    describe('Book Addition to Library', () => {
        beforeEach(() => {
            bookSearchPage.render();
        });

        test('should add book to library successfully', async () => {
            const mockSearchResults = {
                books: {
                    content: [{
                        id: 1,
                        title: 'Test Book',
                        author: 'Test Author',
                        inUserLibrary: false
                    }]
                },
                totalElements: 1,
                totalPages: 1,
                currentPage: 0
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);
            mockBookService.addBookToLibrary.mockResolvedValue({ success: true });

            bookSearchPage.performSearch('test', 'title');
            await new Promise(resolve => setTimeout(resolve, 0));

            const addButton = container.querySelector('.add-to-library-btn');
            addButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockBookService.addBookToLibrary).toHaveBeenCalledWith(1);
            expect(mockNotificationService.showSuccess).toHaveBeenCalledWith('Book added to your library!');
        });

        test('should handle add to library failure', async () => {
            const mockSearchResults = {
                books: {
                    content: [{
                        id: 1,
                        title: 'Test Book',
                        author: 'Test Author',
                        inUserLibrary: false
                    }]
                },
                totalElements: 1,
                totalPages: 1,
                currentPage: 0
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);
            mockBookService.addBookToLibrary.mockRejectedValue(new Error('Book already in library'));

            bookSearchPage.performSearch('test', 'title');
            await new Promise(resolve => setTimeout(resolve, 0));

            const addButton = container.querySelector('.add-to-library-btn');
            addButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockNotificationService.showError).toHaveBeenCalledWith('Book already in library');
        });

        test('should update UI after successful addition', async () => {
            const mockSearchResults = {
                books: {
                    content: [{
                        id: 1,
                        title: 'Test Book',
                        author: 'Test Author',
                        inUserLibrary: false
                    }]
                },
                totalElements: 1,
                totalPages: 1,
                currentPage: 0
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);
            mockBookService.addBookToLibrary.mockResolvedValue({ success: true });

            bookSearchPage.performSearch('test', 'title');
            await new Promise(resolve => setTimeout(resolve, 0));

            const bookCard = container.querySelector('.book-card');
            const addButton = bookCard.querySelector('.add-to-library-btn');
            
            addButton.click();
            await new Promise(resolve => setTimeout(resolve, 0));

            // Button should be replaced with "In Library" indicator
            expect(bookCard.querySelector('.add-to-library-btn')).toBeFalsy();
            expect(bookCard.querySelector('.in-library-indicator')).toBeTruthy();
        });
    });

    describe('Search Suggestions', () => {
        beforeEach(() => {
            bookSearchPage.render();
        });

        test('should show search suggestions on input', async () => {
            const mockSuggestions = [
                'The Great Gatsby',
                'The Great Expectations',
                'The Great Depression'
            ];

            mockBookService.getSearchSuggestions.mockResolvedValue(mockSuggestions);

            const searchInput = container.querySelector('#search-input');
            searchInput.value = 'The Great';
            
            // Trigger input event
            const inputEvent = new Event('input');
            searchInput.dispatchEvent(inputEvent);

            await new Promise(resolve => setTimeout(resolve, 300)); // Wait for debounce

            expect(mockBookService.getSearchSuggestions).toHaveBeenCalledWith('The Great', 5);
            
            const suggestionsList = container.querySelector('.search-suggestions');
            expect(suggestionsList).toBeTruthy();
            expect(suggestionsList.children).toHaveLength(3);
        });

        test('should select suggestion on click', async () => {
            const mockSuggestions = ['The Great Gatsby'];
            mockBookService.getSearchSuggestions.mockResolvedValue(mockSuggestions);

            const searchInput = container.querySelector('#search-input');
            searchInput.value = 'The Great';
            
            const inputEvent = new Event('input');
            searchInput.dispatchEvent(inputEvent);

            await new Promise(resolve => setTimeout(resolve, 300));

            const suggestion = container.querySelector('.suggestion-item');
            suggestion.click();

            expect(searchInput.value).toBe('The Great Gatsby');
            expect(container.querySelector('.search-suggestions')).toBeFalsy();
        });

        test('should hide suggestions when input is cleared', async () => {
            const mockSuggestions = ['The Great Gatsby'];
            mockBookService.getSearchSuggestions.mockResolvedValue(mockSuggestions);

            const searchInput = container.querySelector('#search-input');
            searchInput.value = 'The Great';
            
            let inputEvent = new Event('input');
            searchInput.dispatchEvent(inputEvent);
            await new Promise(resolve => setTimeout(resolve, 300));

            // Clear input
            searchInput.value = '';
            inputEvent = new Event('input');
            searchInput.dispatchEvent(inputEvent);

            expect(container.querySelector('.search-suggestions')).toBeFalsy();
        });
    });

    describe('Error Handling', () => {
        beforeEach(() => {
            bookSearchPage.render();
        });

        test('should handle search API errors', async () => {
            mockBookService.searchBooks.mockRejectedValue(new Error('Network error'));

            bookSearchPage.performSearch('test', 'title');

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockNotificationService.showError).toHaveBeenCalledWith('Search failed. Please try again.');
            expect(container.querySelector('.loading-spinner')).toBeFalsy();
            expect(container.querySelector('.search-button').disabled).toBe(false);
        });

        test('should handle rate limiting errors', async () => {
            const rateLimitError = new Error('Rate limit exceeded');
            rateLimitError.status = 429;
            mockBookService.searchBooks.mockRejectedValue(rateLimitError);

            bookSearchPage.performSearch('test', 'title');

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockNotificationService.showError).toHaveBeenCalledWith('Too many requests. Please wait a moment and try again.');
        });

        test('should handle network connectivity issues', async () => {
            const networkError = new Error('Network error');
            networkError.name = 'NetworkError';
            mockBookService.searchBooks.mockRejectedValue(networkError);

            bookSearchPage.performSearch('test', 'title');

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockNotificationService.showError).toHaveBeenCalledWith('Network error. Please check your connection and try again.');
        });
    });

    describe('Keyboard Navigation', () => {
        beforeEach(() => {
            bookSearchPage.render();
        });

        test('should navigate suggestions with arrow keys', async () => {
            const mockSuggestions = ['Suggestion 1', 'Suggestion 2', 'Suggestion 3'];
            mockBookService.getSearchSuggestions.mockResolvedValue(mockSuggestions);

            const searchInput = container.querySelector('#search-input');
            searchInput.value = 'test';
            
            const inputEvent = new Event('input');
            searchInput.dispatchEvent(inputEvent);
            await new Promise(resolve => setTimeout(resolve, 300));

            // Arrow down
            let keyEvent = new KeyboardEvent('keydown', { key: 'ArrowDown' });
            searchInput.dispatchEvent(keyEvent);

            expect(container.querySelector('.suggestion-item.highlighted')).toBeTruthy();

            // Arrow down again
            keyEvent = new KeyboardEvent('keydown', { key: 'ArrowDown' });
            searchInput.dispatchEvent(keyEvent);

            const highlightedItems = container.querySelectorAll('.suggestion-item.highlighted');
            expect(highlightedItems).toHaveLength(1);
        });

        test('should select highlighted suggestion with Enter', async () => {
            const mockSuggestions = ['Selected Suggestion'];
            mockBookService.getSearchSuggestions.mockResolvedValue(mockSuggestions);

            const searchInput = container.querySelector('#search-input');
            searchInput.value = 'test';
            
            const inputEvent = new Event('input');
            searchInput.dispatchEvent(inputEvent);
            await new Promise(resolve => setTimeout(resolve, 300));

            // Arrow down to highlight
            let keyEvent = new KeyboardEvent('keydown', { key: 'ArrowDown' });
            searchInput.dispatchEvent(keyEvent);

            // Enter to select
            keyEvent = new KeyboardEvent('keydown', { key: 'Enter' });
            searchInput.dispatchEvent(keyEvent);

            expect(searchInput.value).toBe('Selected Suggestion');
        });

        test('should close suggestions with Escape', async () => {
            const mockSuggestions = ['Test Suggestion'];
            mockBookService.getSearchSuggestions.mockResolvedValue(mockSuggestions);

            const searchInput = container.querySelector('#search-input');
            searchInput.value = 'test';
            
            const inputEvent = new Event('input');
            searchInput.dispatchEvent(inputEvent);
            await new Promise(resolve => setTimeout(resolve, 300));

            // Escape to close
            const keyEvent = new KeyboardEvent('keydown', { key: 'Escape' });
            searchInput.dispatchEvent(keyEvent);

            expect(container.querySelector('.search-suggestions')).toBeFalsy();
        });
    });

    describe('Accessibility', () => {
        beforeEach(() => {
            bookSearchPage.render();
        });

        test('should have proper ARIA labels', () => {
            expect(container.querySelector('#search-input').getAttribute('aria-label')).toBeTruthy();
            expect(container.querySelector('#search-button').getAttribute('aria-label')).toBeTruthy();
            expect(container.querySelector('#search-type-select').getAttribute('aria-label')).toBeTruthy();
        });

        test('should announce search results to screen readers', async () => {
            const mockSearchResults = {
                books: { content: [{ id: 1, title: 'Test Book' }] },
                totalElements: 1,
                totalPages: 1,
                currentPage: 0
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);

            bookSearchPage.performSearch('test', 'title');
            await new Promise(resolve => setTimeout(resolve, 0));

            const announcement = container.querySelector('.sr-only[aria-live="polite"]');
            expect(announcement).toBeTruthy();
            expect(announcement.textContent).toContain('1 search result found');
        });

        test('should have keyboard accessible pagination', async () => {
            const mockSearchResults = {
                books: { content: [] },
                totalElements: 50,
                totalPages: 3,
                currentPage: 0
            };

            mockBookService.searchBooks.mockResolvedValue(mockSearchResults);

            bookSearchPage.performSearch('test', 'title');
            await new Promise(resolve => setTimeout(resolve, 0));

            const nextButton = container.querySelector('.next-page-btn');
            expect(nextButton.getAttribute('tabindex')).toBe('0');
            expect(nextButton.getAttribute('role')).toBe('button');
        });
    });
});