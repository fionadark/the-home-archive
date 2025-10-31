import { BaseService } from '../utils/baseService.js';

/**
 * Library Service for The Home Archive
 * Handles personal library management API communication with backend
 */
class LibraryService extends BaseService {
    constructor() {
        super('');
        this.apiBaseURL = '/api/library';
    }

    async getUserLibrary(options = {}) {
        try {
            const params = new URLSearchParams();
            
            if (options.page !== undefined) params.append('page', options.page);
            if (options.size !== undefined) params.append('size', options.size);
            if (options.sort) params.append('sort', options.sort);
            if (options.direction) params.append('direction', options.direction);
            if (options.status) params.append('status', options.status);
            if (options.category) params.append('category', options.category);
            if (options.rating !== undefined) params.append('rating', options.rating);
            if (options.search) params.append('search', options.search);

            const url = `${this.apiBaseURL}?${params.toString()}`;
            const response = await this.makeRequest(url);
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Failed to fetch user library:', error);
            throw error;
        }
    }

    async addBookToLibrary(bookData) {
        try {
            const response = await this.makeRequest(this.apiBaseURL, {
                method: 'POST',
                body: bookData
            });
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Failed to add book to library:', error);
            throw error;
        }
    }

    async updateLibraryEntry(entryId, updateData) {
        try {
            const response = await this.makeRequest(`${this.apiBaseURL}/${entryId}`, {
                method: 'PUT',
                body: updateData
            });
            return await this.handleResponse(response);
        } catch (error) {
            console.error('Failed to update library entry:', error);
            throw error;
        }
    }

    async removeBookFromLibrary(entryId) {
        try {
            const response = await this.makeRequest(`${this.apiBaseURL}/${entryId}`, {
                method: 'DELETE'
            });
            
            if (!response.ok) {
                throw new Error(`Failed to remove book from library: ${response.status}`);
            }
        } catch (error) {
            console.error('Failed to remove book from library:', error);
            throw error;
        }
    }
}

export const libraryService = new LibraryService();
export default libraryService;
