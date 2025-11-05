/**
 * Tests for BookSearchService
 * Comprehensive test suite covering all book search API functionality
 */

import { expect } from '@jest/globals';

// Mock the global authService
const mockAuthService = {
    getAccessToken: jest.fn(),
    refreshAccessToken: jest.fn(),
    logout: jest.fn()
};

// Mock global variables
global.authService = mockAuthService;

// Import the service after mocking
const BookSearchService = require('../../../frontend/src/js/services/bookSearchService.js');

describe('BookSearchService', () => {
    let bookSearchService;

    beforeEach(() => {
        bookSearchService = new BookSearchService();
        
        // Mock CSRF token
        document.querySelector = jest.fn().mockImplementation((selector) => {
            if (selector === 'meta[name="_csrf"]') {
                return { getAttribute: () => 'csrf-token-value' };
            }
            if (selector === 'meta[name="_csrf_header"]') {
                return { getAttribute: () => 'X-CSRF-TOKEN' };
            }
            return null;
        });

        // Reset mocks
        mockAuthService.getAccessToken.mockReturnValue('mock-jwt-token');
        mockAuthService.refreshAccessToken.mockResolvedValue(true);
        localStorage.getItem.mockReturnValue('[]');
    });

    describe('Constructor and Setup', () => {
        test('should initialize with correct default values', () => {
            expect(bookSearchService.baseURL).toBe('');
            expect(bookSearchService.apiBaseURL).toBe('/api/books');
            expect(bookSearchService.searchAPIURL).toBe('/api/search');
            expect(bookSearchService.categoriesAPIURL).toBe('/api/categories');
            expect(bookSearchService.authService).toBe(mockAuthService);
        });

        test('should get CSRF token from meta tags', () => {
            const csrf = bookSearchService.getCSRFToken();
            expect(csrf).toEqual({
                token: 'csrf-token-value',
                header: 'X-CSRF-TOKEN'
            });
        });

        test('should handle missing CSRF token', () => {
            document.querySelector.mockReturnValue(null);
            const newService = new BookSearchService();
            const csrf = newService.getCSRFToken();
            expect(csrf).toBeNull();
        });
    });

    describe('searchBooks', () => {
        const mockSearchResponse = {
            content: [
                {
                    id: 1,
                    title: 'Test Book',
                    author: 'Test Author',
                    isbn: '978-0123456789',
                    averageRating: 4.5,
                    ratingCount: 100
                }
            ],
            totalElements: 1,
            totalPages: 1,
            size: 20,
            number: 0
        };

        test('should search books with basic query', async () => {
            fetch.mockResponseOnce(JSON.stringify(mockSearchResponse));

            const result = await bookSearchService.searchBooks({ q: 'test' });

            expect(result.success).toBe(true);
            expect(result.data).toEqual(mockSearchResponse);
            expect(fetch).toHaveBeenCalledWith(
                '/api/books?q=test',
                expect.objectContaining({
                    method: 'GET',
                    headers: expect.objectContaining({
                        'Content-Type': 'application/json',
                        'Accept': 'application/json',
                        'Authorization': 'Bearer mock-jwt-token',
                        'X-CSRF-TOKEN': 'csrf-token-value'
                    })
                })
            );
        });

        test('should search books with advanced filters', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockSearchResponse
            });

            const searchOptions = {
                q: 'test',
                category: 1,
                minRating: 4,
                maxRating: 5,
                yearStart: 2000,
                yearEnd: 2023,
                page: 0,
                size: 10,
                sort: 'title',
                direction: 'asc'
            };

            const result = await bookSearchService.searchBooks(searchOptions);

            expect(result.success).toBe(true);
            expect(global.fetch).toHaveBeenCalledWith(
                expect.stringContaining('/api/books?'),
                expect.any(Object)
            );

            const fetchUrl = global.fetch.mock.calls[0][0];
            expect(fetchUrl).toContain('q=test');
            expect(fetchUrl).toContain('category=1');
            expect(fetchUrl).toContain('minRating=4');
            expect(fetchUrl).toContain('maxRating=5');
            expect(fetchUrl).toContain('yearStart=2000');
            expect(fetchUrl).toContain('yearEnd=2023');
            expect(fetchUrl).toContain('page=0');
            expect(fetchUrl).toContain('size=10');
            expect(fetchUrl).toContain('sort=title');
            expect(fetchUrl).toContain('direction=asc');
        });

        test('should handle search errors', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: false,
                status: 400,
                json: async () => ({ message: 'Bad request' })
            });

            const result = await bookSearchService.searchBooks({ q: 'test' });

            expect(result.success).toBe(false);
            expect(result.message).toBe('Bad request');
        });

        test('should handle network errors', async () => {
            global.fetch.mockRejectedValueOnce(new Error('Network error'));

            const result = await bookSearchService.searchBooks({ q: 'test' });

            expect(result.success).toBe(false);
            expect(result.message).toContain('Network error');
        });

        test('should save meaningful searches to history', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockSearchResponse
            });

            global.localStorage.getItem.mockReturnValue('[]');
            const setSpy = jest.spyOn(global.localStorage, 'setItem');

            await bookSearchService.searchBooks({ q: 'test book' });

            expect(setSpy).toHaveBeenCalledWith(
                'homeArchive_searchHistory',
                expect.stringContaining('test book')
            );
        });
    });

    describe('getBookDetails', () => {
        const mockBookDetails = {
            id: 1,
            title: 'Test Book',
            author: 'Test Author',
            description: 'A great test book',
            averageRating: 4.5,
            ratingCount: 100,
            reviews: []
        };

        test('should get book details successfully', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockBookDetails
            });

            const result = await bookSearchService.getBookDetails(1);

            expect(result.success).toBe(true);
            expect(result.data).toEqual(mockBookDetails);
            expect(global.fetch).toHaveBeenCalledWith(
                '/api/books/1?includeRatings=true',
                expect.any(Object)
            );
        });

        test('should handle book not found', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: false,
                status: 404
            });

            const result = await bookSearchService.getBookDetails(999);

            expect(result.success).toBe(false);
            expect(result.message).toBe('Book not found');
        });

        test('should require book ID', async () => {
            const result = await bookSearchService.getBookDetails();

            expect(result.success).toBe(false);
            expect(result.message).toBe('Book ID is required');
            expect(global.fetch).not.toHaveBeenCalled();
        });

        test('should include similar books when requested', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockBookDetails
            });

            await bookSearchService.getBookDetails(1, true, true);

            expect(global.fetch).toHaveBeenCalledWith(
                '/api/books/1?includeRatings=true&includeSimilar=true',
                expect.any(Object)
            );
        });
    });

    describe('getSearchSuggestions', () => {
        const mockSuggestions = [
            { type: 'title', value: 'Harry Potter', count: 10 },
            { type: 'author', value: 'J.K. Rowling', count: 7 }
        ];

        test('should get search suggestions', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockSuggestions
            });

            const result = await bookSearchService.getSearchSuggestions('harry');

            expect(result.success).toBe(true);
            expect(result.data).toEqual(mockSuggestions);
            expect(global.fetch).toHaveBeenCalledWith(
                '/api/search/suggestions?q=harry&limit=10&type=all',
                expect.any(Object)
            );
        });

        test('should handle short queries', async () => {
            const result = await bookSearchService.getSearchSuggestions('h');

            expect(result.success).toBe(true);
            expect(result.data).toEqual([]);
            expect(result.message).toBe('Query too short for suggestions');
            expect(global.fetch).not.toHaveBeenCalled();
        });

        test('should handle empty query', async () => {
            const result = await bookSearchService.getSearchSuggestions('');

            expect(result.success).toBe(true);
            expect(result.data).toEqual([]);
            expect(global.fetch).not.toHaveBeenCalled();
        });
    });

    describe('getPopularSearches', () => {
        const mockPopularSearches = [
            { query: 'fantasy', count: 100 },
            { query: 'science fiction', count: 85 }
        ];

        test('should get popular searches', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockPopularSearches
            });

            const result = await bookSearchService.getPopularSearches();

            expect(result.success).toBe(true);
            expect(result.data).toEqual(mockPopularSearches);
            expect(global.fetch).toHaveBeenCalledWith(
                '/api/search/popular?limit=20&timeframe=week',
                expect.any(Object)
            );
        });

        test('should use cache for popular searches', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockPopularSearches
            });

            // First call
            await bookSearchService.getPopularSearches();
            
            // Second call should use cache
            const result = await bookSearchService.getPopularSearches();

            expect(result.success).toBe(true);
            expect(result.message).toBe('Popular searches retrieved from cache');
            expect(global.fetch).toHaveBeenCalledTimes(1);
        });
    });

    describe('getCategories', () => {
        const mockCategories = [
            { id: 1, name: 'Fiction', bookCount: 500 },
            { id: 2, name: 'Non-Fiction', bookCount: 300 }
        ];

        test('should get categories with book counts', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockCategories
            });

            const result = await bookSearchService.getCategories();

            expect(result.success).toBe(true);
            expect(result.data).toEqual(mockCategories);
            expect(global.fetch).toHaveBeenCalledWith(
                '/api/categories?includeBookCount=true',
                expect.any(Object)
            );
        });

        test('should cache categories for longer time', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: true,
                json: async () => mockCategories
            });

            // First call
            await bookSearchService.getCategories();
            
            // Second call should use cache
            const result = await bookSearchService.getCategories();

            expect(result.success).toBe(true);
            expect(result.message).toBe('Categories retrieved from cache');
            expect(global.fetch).toHaveBeenCalledTimes(1);
        });
    });

    describe('Authentication Integration', () => {
        test('should handle token refresh on 401', async () => {
            global.fetch
                .mockResolvedValueOnce({
                    ok: false,
                    status: 401
                })
                .mockResolvedValueOnce({
                    ok: true,
                    json: async () => ({ success: true })
                });

            mockAuthService.refreshAccessToken.mockResolvedValueOnce(true);
            mockAuthService.getAccessToken.mockReturnValueOnce('new-token');

            const result = await bookSearchService.searchBooks({ q: 'test' });

            expect(mockAuthService.refreshAccessToken).toHaveBeenCalled();
            expect(global.fetch).toHaveBeenCalledTimes(2);
            expect(result.success).toBe(true);
        });

        test('should logout and redirect on failed token refresh', async () => {
            global.fetch.mockResolvedValueOnce({
                ok: false,
                status: 401
            });

            mockAuthService.refreshAccessToken.mockResolvedValueOnce(false);

            try {
                await bookSearchService.searchBooks({ q: 'test' });
            } catch (error) {
                expect(error.message).toBe('Session expired. Please login again.');
            }

            expect(mockAuthService.logout).toHaveBeenCalled();
            expect(global.window.location.href).toBe('/login.html');
        });
    });

    describe('Validation Methods', () => {
        test('should validate search options correctly', () => {
            const validOptions = {
                page: 0,
                size: 20,
                minRating: 1,
                maxRating: 5,
                yearStart: 2000,
                yearEnd: 2023,
                sort: 'title',
                direction: 'asc'
            };

            const result = bookSearchService.validateSearchOptions(validOptions);
            expect(result.isValid).toBe(true);
            expect(result.errors).toEqual([]);
        });

        test('should detect invalid search options', () => {
            const invalidOptions = {
                page: -1,
                size: 150,
                minRating: 6,
                maxRating: 0,
                yearStart: 3000,
                yearEnd: 1000,
                sort: 'invalid',
                direction: 'invalid'
            };

            const result = bookSearchService.validateSearchOptions(invalidOptions);
            expect(result.isValid).toBe(false);
            expect(result.errors.length).toBeGreaterThan(0);
        });
    });

    describe('Utility Methods', () => {
        test('should format search results correctly', () => {
            const rawResult = {
                title: 'Test Book',
                averageRating: 4.567,
                ratingCount: 123,
                publicationYear: 2020,
                description: 'A'.repeat(250)
            };

            const formatted = bookSearchService.formatSearchResult(rawResult);

            expect(formatted.formattedRating).toBe('4.6 (123 ratings)');
            expect(formatted.formattedYear).toBe(2020);
            expect(formatted.shortDescription).toHaveLength(203); // 200 + '...'
        });

        test('should handle missing data in format', () => {
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
                q: 'test',
                page: 0,
                size: 20,
                empty: '',
                null: null,
                undefined: undefined
            };

            const queryParams = bookSearchService.buildQueryParams(params);
            const paramString = queryParams.toString();

            expect(paramString).toContain('q=test');
            expect(paramString).toContain('page=0');
            expect(paramString).toContain('size=20');
            expect(paramString).not.toContain('empty');
            expect(paramString).not.toContain('null');
            expect(paramString).not.toContain('undefined');
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

    describe('Local Search History', () => {
        test('should save search to local history', () => {
            const searchData = {
                query: 'test query',
                filters: { category: 1 },
                resultCount: 10,
                timestamp: new Date().toISOString()
            };

            global.localStorage.getItem.mockReturnValue('[]');
            const setSpy = jest.spyOn(global.localStorage, 'setItem');

            bookSearchService.saveSearchToHistory(searchData);

            expect(setSpy).toHaveBeenCalledWith(
                'homeArchive_searchHistory',
                expect.stringContaining('test query')
            );
        });

        test('should avoid duplicate searches in history', () => {
            const existingHistory = [
                { query: 'test query', timestamp: new Date().toISOString() }
            ];

            global.localStorage.getItem.mockReturnValue(JSON.stringify(existingHistory));
            const setSpy = jest.spyOn(global.localStorage, 'setItem');

            bookSearchService.saveSearchToHistory({
                query: 'test query',
                resultCount: 5,
                timestamp: new Date().toISOString()
            });

            const savedData = JSON.parse(setSpy.mock.calls[0][1]);
            expect(savedData).toHaveLength(1); // No duplicate
        });

        test('should retrieve local search history', () => {
            const mockHistory = [
                { query: 'test', timestamp: new Date().toISOString() }
            ];

            global.localStorage.getItem.mockReturnValue(JSON.stringify(mockHistory));

            const history = bookSearchService.getLocalSearchHistory();
            expect(history).toEqual(mockHistory);
        });

        test('should handle corrupted local history', () => {
            global.localStorage.getItem.mockReturnValue('invalid json');
            console.warn = jest.fn();

            const history = bookSearchService.getLocalSearchHistory();
            expect(history).toEqual([]);
            expect(console.warn).toHaveBeenCalled();
        });
    });

    describe('Cache Management', () => {
        test('should set and get from cache', () => {
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
    });
});