/**
 * Tests for LibraryService
 * Comprehensive test suite covering all library API functionality
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
global.localStorage = {
    getItem: jest.fn(),
    setItem: jest.fn(),
    removeItem: jest.fn()
};

// Import the service after mocking
const LibraryService = require('../../../frontend/src/js/services/libraryService.js');

describe('LibraryService', () => {
    let libraryService;
    let originalFetch;

    beforeEach(() => {
        libraryService = new LibraryService();
        
        // Mock fetch
        originalFetch = global.fetch;
        global.fetch = jest.fn();
        
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
        jest.clearAllMocks();
        mockAuthService.getAccessToken.mockReturnValue('mock-jwt-token');
        mockAuthService.refreshAccessToken.mockResolvedValue(true);
    });

    afterEach(() => {
        global.fetch = originalFetch;
        jest.clearAllMocks();
    });

    describe('Constructor and setup', () => {
        test('should initialize with correct configuration', () => {
            expect(libraryService.apiBaseURL).toBe('/api/library');
            expect(libraryService.authService).toBe(mockAuthService);
            expect(libraryService.csrfToken).toEqual({
                token: 'csrf-token-value',
                header: 'X-CSRF-TOKEN'
            });
        });

        test('should work without authService', () => {
            global.authService = undefined;
            const service = new LibraryService();
            expect(service.authService).toBeNull();
        });
    });

    describe('makeRequest', () => {
        test('should make request with proper headers', async () => {
            const mockResponse = { ok: true, json: () => Promise.resolve({ success: true }) };
            global.fetch.mockResolvedValue(mockResponse);

            await libraryService.makeRequest('/test');

            expect(global.fetch).toHaveBeenCalledWith('/test', {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                    'X-CSRF-TOKEN': 'csrf-token-value',
                    'Authorization': 'Bearer mock-jwt-token'
                },
                credentials: 'same-origin'
            });
        });

        test('should handle POST requests with body', async () => {
            const mockResponse = { ok: true, json: () => Promise.resolve({ success: true }) };
            global.fetch.mockResolvedValue(mockResponse);

            const testData = { test: 'data' };
            await libraryService.makeRequest('/test', {
                method: 'POST',
                body: testData
            });

            expect(global.fetch).toHaveBeenCalledWith('/test', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    'Accept': 'application/json',
                    'X-CSRF-TOKEN': 'csrf-token-value',
                    'Authorization': 'Bearer mock-jwt-token'
                },
                credentials: 'same-origin',
                body: JSON.stringify(testData)
            });
        });

        test('should handle token refresh on 401', async () => {
            const unauthorizedResponse = { ok: false, status: 401 };
            const successResponse = { ok: true, json: () => Promise.resolve({ success: true }) };
            
            global.fetch
                .mockResolvedValueOnce(unauthorizedResponse)
                .mockResolvedValueOnce(successResponse);

            mockAuthService.refreshAccessToken.mockResolvedValue(true);
            mockAuthService.getAccessToken
                .mockReturnValueOnce('expired-token')
                .mockReturnValueOnce('new-token');

            const result = await libraryService.makeRequest('/test');

            expect(mockAuthService.refreshAccessToken).toHaveBeenCalled();
            expect(global.fetch).toHaveBeenCalledTimes(2);
            expect(result).toBe(successResponse);
        });

        test('should logout and redirect on failed token refresh', async () => {
            const unauthorizedResponse = { ok: false, status: 401 };
            global.fetch.mockResolvedValue(unauthorizedResponse);
            mockAuthService.refreshAccessToken.mockResolvedValue(false);

            // Mock window.location
            Object.defineProperty(window, 'location', {
                value: { href: '' },
                writable: true
            });

            await expect(libraryService.makeRequest('/test')).rejects.toThrow('Session expired. Please login again.');

            expect(mockAuthService.logout).toHaveBeenCalled();
            expect(window.location.href).toBe('/login.html');
        });
    });

    describe('getUserLibrary', () => {
        test('should fetch user library with default parameters', async () => {
            const mockLibraryData = {
                content: [
                    { id: 1, book: { title: 'Test Book 1' }, status: 'READING' },
                    { id: 2, book: { title: 'Test Book 2' }, status: 'READ' }
                ],
                page: { number: 0, size: 20, totalElements: 2 }
            };

            const mockResponse = {
                ok: true,
                json: () => Promise.resolve(mockLibraryData)
            };
            global.fetch.mockResolvedValue(mockResponse);

            const result = await libraryService.getUserLibrary();

            expect(result.success).toBe(true);
            expect(result.data).toEqual(mockLibraryData);
            expect(global.fetch).toHaveBeenCalledWith('/api/library', expect.any(Object));
        });

        test('should handle pagination and filtering parameters', async () => {
            const mockResponse = {
                ok: true,
                json: () => Promise.resolve({ content: [] })
            };
            global.fetch.mockResolvedValue(mockResponse);

            const options = {
                page: 1,
                size: 10,
                sort: 'title',
                direction: 'asc',
                status: 'READING',
                category: 'Fiction',
                rating: 5,
                search: 'test'
            };

            await libraryService.getUserLibrary(options);

            expect(global.fetch).toHaveBeenCalledWith(
                '/api/library?page=1&size=10&sort=title&direction=asc&status=READING&category=Fiction&rating=5&search=test',
                expect.any(Object)
            );
        });

        test('should handle API errors gracefully', async () => {
            const errorResponse = {
                ok: false,
                status: 500,
                json: () => Promise.resolve({ message: 'Server error' })
            };
            global.fetch.mockResolvedValue(errorResponse);

            const result = await libraryService.getUserLibrary();

            expect(result.success).toBe(false);
            expect(result.message).toBe('Server error');
        });
    });

    describe('searchUserLibrary', () => {
        test('should search user library with query', async () => {
            const mockSearchResults = {
                content: [
                    { id: 1, book: { title: 'Matching Book' }, status: 'READ' }
                ],
                page: { number: 0, size: 20, totalElements: 1 }
            };

            const mockResponse = {
                ok: true,
                json: () => Promise.resolve(mockSearchResults)
            };
            global.fetch.mockResolvedValue(mockResponse);

            const result = await libraryService.searchUserLibrary('test query');

            expect(result.success).toBe(true);
            expect(result.data).toEqual(mockSearchResults);
            expect(global.fetch).toHaveBeenCalledWith(
                '/api/library/search?query=test+query',
                expect.any(Object)
            );
        });

        test('should include pagination options in search', async () => {
            const mockResponse = {
                ok: true,
                json: () => Promise.resolve({ content: [] })
            };
            global.fetch.mockResolvedValue(mockResponse);

            const options = { page: 2, size: 5, sort: 'author', direction: 'desc' };
            await libraryService.searchUserLibrary('query', options);

            expect(global.fetch).toHaveBeenCalledWith(
                '/api/library/search?query=query&page=2&size=5&sort=author&direction=desc',
                expect.any(Object)
            );
        });
    });

    describe('addBookToLibrary', () => {
        test('should add book to library successfully', async () => {
            const bookData = {
                bookId: 123,
                status: 'READING',
                userRating: 4,
                personalNotes: 'Great book!'
            };

            const mockResponse = {
                ok: true,
                json: () => Promise.resolve({ id: 1, ...bookData })
            };
            global.fetch.mockResolvedValue(mockResponse);

            const result = await libraryService.addBookToLibrary(bookData);

            expect(result.success).toBe(true);
            expect(global.fetch).toHaveBeenCalledWith('/api/library/add', {
                method: 'POST',
                headers: expect.any(Object),
                credentials: 'same-origin',
                body: JSON.stringify(bookData)
            });
        });

        test('should validate required fields', async () => {
            const result = await libraryService.addBookToLibrary({});

            expect(result.success).toBe(false);
            expect(result.message).toBe('Book ID is required');
        });

        test('should validate reading status', async () => {
            const result = await libraryService.addBookToLibrary({
                bookId: 123,
                status: 'INVALID_STATUS'
            });

            expect(result.success).toBe(false);
            expect(result.message).toBe('Valid reading status is required (UNREAD, READING, READ, DNF)');
        });
    });

    describe('updateLibraryEntry', () => {
        test('should update library entry successfully', async () => {
            const updateData = {
                status: 'READ',
                userRating: 5,
                personalNotes: 'Finished reading!'
            };

            const mockResponse = {
                ok: true,
                json: () => Promise.resolve({ id: 1, ...updateData })
            };
            global.fetch.mockResolvedValue(mockResponse);

            const result = await libraryService.updateLibraryEntry(1, updateData);

            expect(result.success).toBe(true);
            expect(global.fetch).toHaveBeenCalledWith('/api/library/1', {
                method: 'PUT',
                headers: expect.any(Object),
                credentials: 'same-origin',
                body: JSON.stringify(updateData)
            });
        });

        test('should validate entry ID', async () => {
            const result = await libraryService.updateLibraryEntry(null, {});

            expect(result.success).toBe(false);
            expect(result.message).toBe('Entry ID is required');
        });

        test('should validate status if provided', async () => {
            const result = await libraryService.updateLibraryEntry(1, { status: 'INVALID' });

            expect(result.success).toBe(false);
            expect(result.message).toBe('Valid reading status is required (UNREAD, READING, READ, DNF)');
        });
    });

    describe('removeBookFromLibrary', () => {
        test('should remove book from library successfully', async () => {
            const mockResponse = { ok: true };
            global.fetch.mockResolvedValue(mockResponse);

            const result = await libraryService.removeBookFromLibrary(1);

            expect(result.success).toBe(true);
            expect(result.message).toBe('Book removed from library successfully');
            expect(global.fetch).toHaveBeenCalledWith('/api/library/1', {
                method: 'DELETE',
                headers: expect.any(Object),
                credentials: 'same-origin'
            });
        });

        test('should validate entry ID', async () => {
            const result = await libraryService.removeBookFromLibrary(null);

            expect(result.success).toBe(false);
            expect(result.message).toBe('Entry ID is required');
        });
    });

    describe('getLibraryStatistics', () => {
        test('should fetch library statistics successfully', async () => {
            const mockStats = {
                totalBooks: 25,
                readBooks: 15,
                currentlyReading: 3,
                averageRating: 4.2
            };

            const mockResponse = {
                ok: true,
                json: () => Promise.resolve(mockStats)
            };
            global.fetch.mockResolvedValue(mockResponse);

            const result = await libraryService.getLibraryStatistics();

            expect(result.success).toBe(true);
            expect(result.data).toEqual(mockStats);
            expect(global.fetch).toHaveBeenCalledWith('/api/library/stats', expect.any(Object));
        });
    });

    describe('getLibraryEntry', () => {
        test('should fetch specific library entry', async () => {
            const mockEntry = {
                id: 1,
                book: { title: 'Test Book' },
                status: 'reading',
                userRating: 4
            };

            const mockResponse = {
                ok: true,
                json: () => Promise.resolve(mockEntry)
            };
            global.fetch.mockResolvedValue(mockResponse);

            const result = await libraryService.getLibraryEntry(1);

            expect(result.success).toBe(true);
            expect(result.data).toEqual(mockEntry);
            expect(global.fetch).toHaveBeenCalledWith('/api/library/1', expect.any(Object));
        });

        test('should handle not found error', async () => {
            const mockResponse = { ok: false, status: 404 };
            global.fetch.mockResolvedValue(mockResponse);

            const result = await libraryService.getLibraryEntry(999);

            expect(result.success).toBe(false);
            expect(result.message).toBe('Library entry not found');
        });
    });

    describe('isBookInLibrary', () => {
        test('should check if book is in library', async () => {
            const mockResponse = {
                ok: true,
                json: () => Promise.resolve({ inLibrary: true, entryId: 1 })
            };
            global.fetch.mockResolvedValue(mockResponse);

            const result = await libraryService.isBookInLibrary(123);

            expect(result.success).toBe(true);
            expect(global.fetch).toHaveBeenCalledWith('/api/library/contains/123', expect.any(Object));
        });

        test('should validate book ID', async () => {
            const result = await libraryService.isBookInLibrary(null);

            expect(result.success).toBe(false);
            expect(result.message).toBe('Book ID is required');
        });
    });

    describe('Utility methods', () => {
        test('isValidReadingStatus should validate statuses correctly', () => {
            expect(libraryService.isValidReadingStatus('UNREAD')).toBe(true);
            expect(libraryService.isValidReadingStatus('READING')).toBe(true);
            expect(libraryService.isValidReadingStatus('read')).toBe(true);
            expect(libraryService.isValidReadingStatus('DNF')).toBe(true);
            expect(libraryService.isValidReadingStatus('INVALID')).toBe(false);
            expect(libraryService.isValidReadingStatus('')).toBe(false);
        });

        test('isValidRating should validate ratings correctly', () => {
            expect(libraryService.isValidRating(1)).toBe(true);
            expect(libraryService.isValidRating(3)).toBe(true);
            expect(libraryService.isValidRating(5)).toBe(true);
            expect(libraryService.isValidRating(0)).toBe(false);
            expect(libraryService.isValidRating(6)).toBe(false);
            expect(libraryService.isValidRating(3.5)).toBe(false);
        });

        test('formatReadingStatus should format statuses correctly', () => {
            expect(libraryService.formatReadingStatus('UNREAD')).toBe('Not Started');
            expect(libraryService.formatReadingStatus('READING')).toBe('Currently Reading');
            expect(libraryService.formatReadingStatus('read')).toBe('Completed');
            expect(libraryService.formatReadingStatus('DNF')).toBe('Did Not Finish');
            expect(libraryService.formatReadingStatus('UNKNOWN')).toBe('UNKNOWN');
        });

        test('formatDateForAPI should format dates correctly', () => {
            const date = new Date('2023-12-25T10:30:00Z');
            expect(libraryService.formatDateForAPI(date)).toBe('2023-12-25');
            expect(libraryService.formatDateForAPI('2023-12-25')).toBe('2023-12-25');
            expect(libraryService.formatDateForAPI(null)).toBeNull();
            expect(libraryService.formatDateForAPI(undefined)).toBeNull();
        });

        test('buildQueryParams should build parameters correctly', () => {
            const params = {
                page: 1,
                size: 10,
                status: 'READING',
                empty: '',
                nullValue: null,
                undefinedValue: undefined
            };

            const result = libraryService.buildQueryParams(params);
            
            expect(result.toString()).toBe('page=1&size=10&status=READING');
        });
    });

    describe('Error handling', () => {
        test('should handle network errors', async () => {
            global.fetch.mockRejectedValue(new Error('Network error'));

            const result = await libraryService.getUserLibrary();

            expect(result.success).toBe(false);
            expect(result.message).toContain('Network error');
        });

        test('should handle invalid JSON responses', async () => {
            const mockResponse = {
                ok: false,
                status: 500,
                json: () => Promise.reject(new Error('Invalid JSON'))
            };
            global.fetch.mockResolvedValue(mockResponse);

            const result = await libraryService.getUserLibrary();

            expect(result.success).toBe(false);
            expect(result.message).toBe('Failed to fetch library: 500');
        });
    });
});