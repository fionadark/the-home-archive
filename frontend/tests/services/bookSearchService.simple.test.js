/**
 * Simple tests for BookSearchService
 * Basic test suite to verify core functionality works
 */

// Import the service directly
const BookSearchService = require('../../src/js/services/bookSearchService.js');

describe('BookSearchService - Basic Functionality', () => {
    let bookSearchService;
    let mockAuthService;

    beforeEach(() => {
        // Mock localStorage as Jest spy functions
        const localStorageMock = {
            getItem: jest.fn(),
            setItem: jest.fn(),
            removeItem: jest.fn(),
            clear: jest.fn()
        };
        Object.defineProperty(window, 'localStorage', {
            value: localStorageMock,
            writable: true
        });
        
        // Make localStorage available globally for the service
        global.localStorage = localStorageMock;

        // Mock document with CSRF token meta tags
        const mockCSRFMeta = {
            getAttribute: jest.fn((attr) => {
                if (attr === 'content') return 'test-csrf-token';
                return null;
            })
        };
        
        const mockCSRFHeaderMeta = {
            getAttribute: jest.fn((attr) => {
                if (attr === 'content') return 'X-CSRF-TOKEN';
                return null;
            })
        };
        
        const documentMock = {
            querySelector: jest.fn((selector) => {
                if (selector === 'meta[name="_csrf"]') {
                    return mockCSRFMeta;
                }
                if (selector === 'meta[name="_csrf_header"]') {
                    return mockCSRFHeaderMeta;
                }
                return null;
            }),
            addEventListener: jest.fn(),
            removeEventListener: jest.fn()
        };
        
        // Set both global.document and window.document
        global.document = documentMock;
        Object.defineProperty(window, 'document', {
            value: documentMock,
            writable: true
        });

        global.fetch = jest.fn();

        // Mock authService with token
        const mockAuthService = {
            getAccessToken: jest.fn().mockReturnValue('test-token'),
            refreshAccessToken: jest.fn().mockResolvedValue(true),
            logout: jest.fn()
        };

        // Create service instance with mocked authService
        bookSearchService = new BookSearchService();
        bookSearchService.authService = mockAuthService;
    });

    afterEach(() => {
        jest.clearAllMocks();
    });

    test('should initialize correctly', () => {
        expect(bookSearchService).toBeDefined();
        expect(bookSearchService.apiBaseURL).toBe('/api/books');
        expect(bookSearchService.searchAPIURL).toBe('/api/search');
        expect(bookSearchService.categoriesAPIURL).toBe('/api/categories');
    });

    test('should get CSRF token correctly', () => {
        const token = bookSearchService.getCSRFToken();
        expect(token).toEqual({
            token: 'test-csrf-token',
            header: 'X-CSRF-TOKEN'
        });
    });

    test('should validate search options correctly', () => {
        const validOptions = {
            page: 0,
            size: 20,
            sort: 'title',
            direction: 'asc'
        };

        const result = bookSearchService.validateSearchOptions(validOptions);
        expect(result.isValid).toBe(true);
        expect(result.errors).toHaveLength(0);
    });

    test('should detect invalid search options', () => {
        const invalidOptions = {
            page: -1,
            size: 200,
            minRating: 10,
            sort: 'invalid-field'
        };

        const result = bookSearchService.validateSearchOptions(invalidOptions);
        expect(result.isValid).toBe(false);
        expect(result.errors.length).toBeGreaterThan(0);
    });

    test('should format search results correctly', () => {
        const rawResult = {
            title: 'Test Book',
            averageRating: 4.567,
            ratingCount: 123,
            publicationYear: 2020,
            description: 'A short description'
        };

        const formatted = bookSearchService.formatSearchResult(rawResult);
        expect(formatted.formattedRating).toBe('4.6 (123 ratings)');
        expect(formatted.formattedYear).toBe(2020);
        expect(formatted.shortDescription).toBe('A short description');
    });

    test('should handle missing data in formatting', () => {
        const rawResult = {
            title: 'Test Book'
        };

        const formatted = bookSearchService.formatSearchResult(rawResult);
        expect(formatted.formattedRating).toBe('No ratings');
        expect(formatted.formattedYear).toBe('Unknown');
        expect(formatted.shortDescription).toBe('No description available');
    });

    test('should build query parameters correctly', () => {
        const params = {
            q: 'test query',
            page: 0,
            size: 20,
            empty: '',
            null: null,
            undefined: undefined
        };

        const queryParams = bookSearchService.buildQueryParams(params);
        const paramString = queryParams.toString();

        expect(paramString).toContain('q=test+query');
        expect(paramString).toContain('page=0');
        expect(paramString).toContain('size=20');
        expect(paramString).not.toContain('empty');
        expect(paramString).not.toContain('null');
        expect(paramString).not.toContain('undefined');
    });

    test('should handle cache operations', () => {
        const testData = { test: 'data' };
        
        bookSearchService.setCache('test-key', testData);
        const cached = bookSearchService.getFromCache('test-key');

        expect(cached).toEqual(testData);
    });

    test('should expire cached data', (done) => {
        const testData = { test: 'data' };
        
        bookSearchService.setCache('test-key', testData, 50); // 50ms timeout

        setTimeout(() => {
            const cached = bookSearchService.getFromCache('test-key');
            expect(cached).toBeNull();
            done();
        }, 100);
    });

    test('should clear all cache', () => {
        bookSearchService.setCache('key1', 'data1');
        bookSearchService.setCache('key2', 'data2');

        bookSearchService.clearCache();

        expect(bookSearchService.getFromCache('key1')).toBeNull();
        expect(bookSearchService.getFromCache('key2')).toBeNull();
    });

    test('should handle local search history', () => {
        const searchData = {
            query: 'test query',
            timestamp: Date.now()
        };

        bookSearchService.saveSearchToHistory(searchData);

        expect(window.localStorage.setItem).toHaveBeenCalledWith(
            'homeArchive_searchHistory',
            expect.stringContaining('test query')
        );
    });

    test('should retrieve local search history', () => {
        const mockHistory = [
            { query: 'test 1', timestamp: Date.now() },
            { query: 'test 2', timestamp: Date.now() }
        ];

        window.localStorage.getItem.mockReturnValue(JSON.stringify(mockHistory));

        const history = bookSearchService.getLocalSearchHistory();
        expect(history).toEqual(mockHistory);
    });

    test('should handle API request structure', async () => {
        const mockResponse = {
            content: [],
            totalElements: 0
        };

        global.fetch.mockResolvedValue({
            ok: true,
            json: () => Promise.resolve(mockResponse)
        });

        const result = await bookSearchService.searchBooks({ q: 'test' });

        expect(result.success).toBe(true);
        expect(result.data).toEqual(mockResponse);
        expect(global.fetch).toHaveBeenCalledWith(
            '/api/books?q=test',
            expect.objectContaining({
                method: 'GET',
                headers: expect.objectContaining({
                    'Authorization': 'Bearer test-token',
                    'Content-Type': 'application/json',
                    'Accept': 'application/json'
                })
            })
        );
    });

    test('should handle API errors gracefully', async () => {
        global.fetch.mockResolvedValue({
            ok: false,
            status: 400,
            json: () => Promise.resolve({ message: 'Bad request' })
        });

        const result = await bookSearchService.searchBooks({ q: 'test' });

        expect(result.success).toBe(false);
        expect(result.message).toBe('Bad request');
    });

    test('should handle network errors', async () => {
        global.fetch.mockRejectedValue(new Error('Network error'));

        const result = await bookSearchService.searchBooks({ q: 'test' });

        expect(result.success).toBe(false);
        expect(result.message).toContain('Network error');
    });

    test('should handle missing book ID in getBookDetails', async () => {
        const result = await bookSearchService.getBookDetails();

        expect(result.success).toBe(false);
        expect(result.message).toBe('Book ID is required');
        expect(global.fetch).not.toHaveBeenCalled();
    });

    test('should handle short queries in suggestions', async () => {
        const result = await bookSearchService.getSearchSuggestions('h');

        expect(result.success).toBe(true);
        expect(result.data).toEqual([]);
        expect(result.message).toBe('Query too short for suggestions');
        expect(global.fetch).not.toHaveBeenCalled();
    });

    test('should create debounced function', (done) => {
        const mockFn = jest.fn();
        const debouncedFn = bookSearchService.debounce(mockFn, 100);

        debouncedFn('test1');
        debouncedFn('test2');
        debouncedFn('test3');

        setTimeout(() => {
            expect(mockFn).toHaveBeenCalledTimes(1);
            expect(mockFn).toHaveBeenCalledWith('test3');
            done();
        }, 150);
    });
});