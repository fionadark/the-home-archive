/**
 * @jest-environment jsdom
 */

import BookAddition from '../../src/js/components/BookAddition.js';
import { BookService } from '../../src/js/services/BookService.js';
import { NotificationService } from '../../src/js/services/NotificationService.js';

// Mock the services
jest.mock('../../src/js/services/BookService.js');
jest.mock('../../src/js/services/NotificationService.js');

describe('BookAddition', () => {
    let bookAddition;
    let mockBookService;
    let mockNotificationService;
    let container;

    beforeEach(() => {
        // Setup DOM
        document.body.innerHTML = '';
        container = document.createElement('div');
        container.id = 'book-addition-container';
        document.body.appendChild(container);

        // Mock services
        mockBookService = new BookService();
        mockNotificationService = new NotificationService();
        
        BookService.mockImplementation(() => mockBookService);
        NotificationService.mockImplementation(() => mockNotificationService);

        // Initialize component
        bookAddition = new BookAddition();
    });

    afterEach(() => {
        jest.clearAllMocks();
        document.body.innerHTML = '';
    });

    describe('Initialization', () => {
        test('should initialize with default state', () => {
            expect(bookAddition.isbnValidationResults).toEqual({});
            expect(bookAddition.currentStep).toBe('search');
            expect(bookAddition.selectedBook).toBeNull();
            expect(bookAddition.manualBookData).toEqual({});
        });

        test('should render book addition interface', () => {
            bookAddition.render(container);
            
            expect(container.querySelector('.book-addition-container')).toBeTruthy();
            expect(container.querySelector('#isbn-search-section')).toBeTruthy();
            expect(container.querySelector('#manual-entry-section')).toBeTruthy();
            expect(container.querySelector('#book-confirmation-section')).toBeTruthy();
        });

        test('should initialize with Dark Academia styling', () => {
            bookAddition.render(container);
            
            expect(container.classList.contains('dark-academia')).toBe(true);
            expect(container.querySelector('.elegant-form')).toBeTruthy();
        });

        test('should start with ISBN search step active', () => {
            bookAddition.render(container);
            
            expect(container.querySelector('#isbn-search-section').classList.contains('active')).toBe(true);
            expect(container.querySelector('#manual-entry-section').classList.contains('active')).toBe(false);
            expect(container.querySelector('#book-confirmation-section').classList.contains('active')).toBe(false);
        });
    });

    describe('ISBN Search and Validation', () => {
        beforeEach(() => {
            bookAddition.render(container);
        });

        test('should validate valid ISBN-13', async () => {
            const mockValidationResponse = {
                valid: true,
                isbn: '978-0-7432-7356-5',
                title: 'The Great Gatsby',
                author: 'F. Scott Fitzgerald',
                publisher: 'Scribner',
                publicationYear: 1925,
                pageCount: 180,
                description: 'A classic American novel',
                coverImageUrl: 'https://example.com/gatsby.jpg',
                existsInDatabase: false,
                enrichedFromExternalSource: true,
                externalSource: 'Open Library'
            };

            mockBookService.validateBookByIsbn.mockResolvedValue(mockValidationResponse);

            const isbnInput = container.querySelector('#isbn-input');
            const validateButton = container.querySelector('#validate-isbn-btn');

            isbnInput.value = '978-0-7432-7356-5';
            validateButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockBookService.validateBookByIsbn).toHaveBeenCalledWith('978-0-7432-7356-5');
            expect(container.querySelector('.isbn-validation-success')).toBeTruthy();
            expect(container.querySelector('.book-preview')).toBeTruthy();
            expect(container.querySelector('.book-preview .title').textContent).toBe('The Great Gatsby');
        });

        test('should validate valid ISBN-10', async () => {
            const mockValidationResponse = {
                valid: true,
                isbn: '0-7432-7356-4',
                title: 'Test Book',
                author: 'Test Author',
                existsInDatabase: false
            };

            mockBookService.validateBookByIsbn.mockResolvedValue(mockValidationResponse);

            const isbnInput = container.querySelector('#isbn-input');
            const validateButton = container.querySelector('#validate-isbn-btn');

            isbnInput.value = '0-7432-7356-4';
            validateButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockBookService.validateBookByIsbn).toHaveBeenCalledWith('0-7432-7356-4');
            expect(container.querySelector('.isbn-validation-success')).toBeTruthy();
        });

        test('should handle invalid ISBN format', async () => {
            const mockValidationResponse = {
                valid: false,
                isbn: 'invalid-isbn',
                errorMessage: 'Invalid ISBN format'
            };

            mockBookService.validateBookByIsbn.mockResolvedValue(mockValidationResponse);

            const isbnInput = container.querySelector('#isbn-input');
            const validateButton = container.querySelector('#validate-isbn-btn');

            isbnInput.value = 'invalid-isbn';
            validateButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(container.querySelector('.isbn-validation-error')).toBeTruthy();
            expect(container.querySelector('.isbn-validation-error').textContent).toContain('Invalid ISBN format');
        });

        test('should handle book already in database', async () => {
            const mockValidationResponse = {
                valid: true,
                isbn: '978-0-7432-7356-5',
                title: 'The Great Gatsby',
                author: 'F. Scott Fitzgerald',
                existsInDatabase: true,
                bookId: 1
            };

            mockBookService.validateBookByIsbn.mockResolvedValue(mockValidationResponse);

            const isbnInput = container.querySelector('#isbn-input');
            const validateButton = container.querySelector('#validate-isbn-btn');

            isbnInput.value = '978-0-7432-7356-5';
            validateButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(container.querySelector('.book-exists-warning')).toBeTruthy();
            expect(container.querySelector('#add-existing-btn')).toBeTruthy();
            expect(container.querySelector('.book-exists-warning').textContent).toContain('already exists');
        });

        test('should clear validation results when ISBN input changes', () => {
            // Set up existing validation result
            bookAddition.isbnValidationResults = {
                valid: true,
                title: 'Test Book'
            };
            bookAddition.displayValidationResults();

            const isbnInput = container.querySelector('#isbn-input');
            isbnInput.value = 'new-isbn';
            
            const inputEvent = new Event('input');
            isbnInput.dispatchEvent(inputEvent);

            expect(container.querySelector('.isbn-validation-success')).toBeFalsy();
            expect(container.querySelector('.book-preview')).toBeFalsy();
        });

        test('should validate ISBN on Enter key', async () => {
            const mockValidationResponse = {
                valid: true,
                isbn: '978-0-7432-7356-5',
                title: 'Test Book'
            };

            mockBookService.validateBookByIsbn.mockResolvedValue(mockValidationResponse);

            const isbnInput = container.querySelector('#isbn-input');
            isbnInput.value = '978-0-7432-7356-5';

            const enterEvent = new KeyboardEvent('keypress', { key: 'Enter' });
            isbnInput.dispatchEvent(enterEvent);

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockBookService.validateBookByIsbn).toHaveBeenCalledWith('978-0-7432-7356-5');
        });

        test('should handle API validation errors', async () => {
            mockBookService.validateBookByIsbn.mockRejectedValue(new Error('API Error'));

            const isbnInput = container.querySelector('#isbn-input');
            const validateButton = container.querySelector('#validate-isbn-btn');

            isbnInput.value = '978-0-7432-7356-5';
            validateButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockNotificationService.showError).toHaveBeenCalledWith('Failed to validate ISBN. Please try again.');
        });
    });

    describe('Manual Book Entry', () => {
        beforeEach(() => {
            bookAddition.render(container);
        });

        test('should switch to manual entry mode', () => {
            const manualEntryButton = container.querySelector('#switch-to-manual-btn');
            manualEntryButton.click();

            expect(container.querySelector('#isbn-search-section').classList.contains('active')).toBe(false);
            expect(container.querySelector('#manual-entry-section').classList.contains('active')).toBe(true);
            expect(bookAddition.currentStep).toBe('manual');
        });

        test('should validate required fields in manual entry', () => {
            container.querySelector('#switch-to-manual-btn').click();

            const submitButton = container.querySelector('#submit-manual-book-btn');
            submitButton.click();

            expect(container.querySelector('.validation-errors')).toBeTruthy();
            expect(container.querySelector('.validation-errors').textContent).toContain('Title is required');
            expect(container.querySelector('.validation-errors').textContent).toContain('Author is required');
        });

        test('should validate ISBN format in manual entry', () => {
            container.querySelector('#switch-to-manual-btn').click();

            const titleInput = container.querySelector('#manual-title');
            const authorInput = container.querySelector('#manual-author');
            const isbnInput = container.querySelector('#manual-isbn');
            const submitButton = container.querySelector('#submit-manual-book-btn');

            titleInput.value = 'Test Book';
            authorInput.value = 'Test Author';
            isbnInput.value = 'invalid-isbn';

            submitButton.click();

            expect(container.querySelector('.validation-errors').textContent).toContain('Invalid ISBN format');
        });

        test('should validate year range in manual entry', () => {
            container.querySelector('#switch-to-manual-btn').click();

            const titleInput = container.querySelector('#manual-title');
            const authorInput = container.querySelector('#manual-author');
            const yearInput = container.querySelector('#manual-year');
            const submitButton = container.querySelector('#submit-manual-book-btn');

            titleInput.value = 'Test Book';
            authorInput.value = 'Test Author';
            yearInput.value = '2030'; // Future year

            submitButton.click();

            expect(container.querySelector('.validation-errors').textContent).toContain('Publication year cannot be in the future');
        });

        test('should validate page count in manual entry', () => {
            container.querySelector('#switch-to-manual-btn').click();

            const titleInput = container.querySelector('#manual-title');
            const authorInput = container.querySelector('#manual-author');
            const pageCountInput = container.querySelector('#manual-page-count');
            const submitButton = container.querySelector('#submit-manual-book-btn');

            titleInput.value = 'Test Book';
            authorInput.value = 'Test Author';
            pageCountInput.value = '-5'; // Negative pages

            submitButton.click();

            expect(container.querySelector('.validation-errors').textContent).toContain('Page count must be a positive number');
        });

        test('should populate manual form from ISBN validation', async () => {
            const mockValidationResponse = {
                valid: true,
                isbn: '978-0-7432-7356-5',
                title: 'The Great Gatsby',
                author: 'F. Scott Fitzgerald',
                publisher: 'Scribner',
                publicationYear: 1925,
                pageCount: 180,
                description: 'A classic American novel',
                existsInDatabase: false
            };

            mockBookService.validateBookByIsbn.mockResolvedValue(mockValidationResponse);

            const isbnInput = container.querySelector('#isbn-input');
            const validateButton = container.querySelector('#validate-isbn-btn');

            isbnInput.value = '978-0-7432-7356-5';
            validateButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            const editDataButton = container.querySelector('#edit-data-btn');
            editDataButton.click();

            expect(container.querySelector('#manual-entry-section').classList.contains('active')).toBe(true);
            expect(container.querySelector('#manual-title').value).toBe('The Great Gatsby');
            expect(container.querySelector('#manual-author').value).toBe('F. Scott Fitzgerald');
            expect(container.querySelector('#manual-isbn').value).toBe('978-0-7432-7356-5');
            expect(container.querySelector('#manual-year').value).toBe('1925');
            expect(container.querySelector('#manual-page-count').value).toBe('180');
        });

        test('should proceed to confirmation with valid manual data', async () => {
            // Mock categories first
            const mockCategories = [
                { id: 1, name: 'Fiction' },
                { id: 2, name: 'Non-Fiction' }
            ];
            mockBookService.getCategories.mockResolvedValue(mockCategories);
            
            // Load categories
            await bookAddition.loadCategories();
            
            container.querySelector('#switch-to-manual-btn').click();

            const titleInput = container.querySelector('#manual-title');
            const authorInput = container.querySelector('#manual-author');
            const categorySelect = container.querySelector('#manual-category');
            const submitButton = container.querySelector('#submit-manual-book-btn');

            titleInput.value = 'New Test Book';
            authorInput.value = 'New Test Author';
            categorySelect.value = '1'; // Select a category

            submitButton.click();

            expect(container.querySelector('#book-confirmation-section').classList.contains('active')).toBe(true);
            expect(bookAddition.currentStep).toBe('confirmation');
            expect(container.querySelector('.confirmation-title').textContent).toBe('New Test Book');
            expect(container.querySelector('.confirmation-author').textContent).toBe('by New Test Author');
        });
    });

    describe('Book Confirmation and Addition', () => {
        beforeEach(() => {
            bookAddition.render(container);
        });

        test('should show confirmation step with book data', () => {
            // Simulate moving to confirmation step with book data
            bookAddition.selectedBook = {
                title: 'The Great Gatsby',
                author: 'F. Scott Fitzgerald',
                isbn: '978-0-7432-7356-5',
                description: 'A classic American novel',
                publicationYear: 1925,
                publisher: 'Scribner',
                pageCount: 180,
                categoryId: 1,
                coverImageUrl: 'https://example.com/gatsby.jpg'
            };
            bookAddition.currentStep = 'confirmation';
            bookAddition.updateStepDisplay();
            bookAddition.displayConfirmationStep();

            expect(container.querySelector('#book-confirmation-section').classList.contains('active')).toBe(true);
            expect(container.querySelector('.confirmation-title').textContent).toBe('The Great Gatsby');
            expect(container.querySelector('.confirmation-author').textContent).toBe('by F. Scott Fitzgerald');
            expect(container.querySelector('.confirmation-isbn').textContent).toBe('ISBN: 978-0-7432-7356-5');
        });

        test('should add book to library successfully', async () => {
            const mockCreatedBook = {
                id: 1,
                title: 'The Great Gatsby',
                author: 'F. Scott Fitzgerald',
                isbn: '978-0-7432-7356-5'
            };

            mockBookService.createBook.mockResolvedValue(mockCreatedBook);

            bookAddition.selectedBook = {
                title: 'The Great Gatsby',
                author: 'F. Scott Fitzgerald',
                isbn: '978-0-7432-7356-5',
                categoryId: 1
            };
            bookAddition.currentStep = 'confirmation';
            bookAddition.updateStepDisplay();
            bookAddition.displayConfirmationStep();

            const confirmButton = container.querySelector('#confirm-add-book-btn');
            confirmButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockBookService.createBook).toHaveBeenCalledWith({
                title: 'The Great Gatsby',
                author: 'F. Scott Fitzgerald',
                isbn: '978-0-7432-7356-5',
                categoryId: 1
            });
            expect(mockNotificationService.showSuccess).toHaveBeenCalledWith('Book added to your library successfully!');
        });

        test('should handle book creation failure', async () => {
            mockBookService.createBook.mockRejectedValue(new Error('Book already exists'));

            bookAddition.selectedBook = {
                title: 'Test Book',
                author: 'Test Author'
            };
            bookAddition.currentStep = 'confirmation';
            bookAddition.displayConfirmationStep();

            const confirmButton = container.querySelector('#confirm-add-book-btn');
            confirmButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockNotificationService.showError).toHaveBeenCalledWith('Book already exists');
        });

        test('should allow editing book details before confirmation', () => {
            bookAddition.selectedBook = {
                title: 'The Great Gatsby',
                author: 'F. Scott Fitzgerald'
            };
            bookAddition.currentStep = 'confirmation';
            bookAddition.displayConfirmationStep();

            const editButton = container.querySelector('#edit-book-details-btn');
            editButton.click();

            expect(container.querySelector('#manual-entry-section').classList.contains('active')).toBe(true);
            expect(container.querySelector('#manual-title').value).toBe('The Great Gatsby');
            expect(container.querySelector('#manual-author').value).toBe('F. Scott Fitzgerald');
        });

        test('should reset form after successful addition', async () => {
            const mockCreatedBook = { id: 1 };
            mockBookService.createBook.mockResolvedValue(mockCreatedBook);

            bookAddition.selectedBook = { title: 'Test Book', author: 'Test Author' };
            bookAddition.currentStep = 'confirmation';
            bookAddition.displayConfirmationStep();

            const confirmButton = container.querySelector('#confirm-add-book-btn');
            confirmButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(bookAddition.currentStep).toBe('search');
            expect(bookAddition.selectedBook).toBeNull();
            expect(container.querySelector('#isbn-search-section').classList.contains('active')).toBe(true);
            expect(container.querySelector('#isbn-input').value).toBe('');
        });
    });

    describe('Adding Existing Books', () => {
        beforeEach(() => {
            bookAddition.render(container);
        });

        test('should add existing book to library', async () => {
            const mockValidationResponse = {
                valid: true,
                isbn: '978-0-7432-7356-5',
                title: 'The Great Gatsby',
                existsInDatabase: true,
                bookId: 1
            };

            mockBookService.validateBookByIsbn.mockResolvedValue(mockValidationResponse);
            mockBookService.addBookToLibrary.mockResolvedValue({ success: true });

            const isbnInput = container.querySelector('#isbn-input');
            const validateButton = container.querySelector('#validate-isbn-btn');

            isbnInput.value = '978-0-7432-7356-5';
            validateButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            const addExistingButton = container.querySelector('#add-existing-btn');
            addExistingButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockBookService.addBookToLibrary).toHaveBeenCalledWith(1);
            expect(mockNotificationService.showSuccess).toHaveBeenCalledWith('Book added to your library!');
        });

        test('should handle failure when adding existing book', async () => {
            const mockValidationResponse = {
                valid: true,
                existsInDatabase: true,
                bookId: 1
            };

            mockBookService.validateBookByIsbn.mockResolvedValue(mockValidationResponse);
            mockBookService.addBookToLibrary.mockRejectedValue(new Error('Book already in your library'));

            const isbnInput = container.querySelector('#isbn-input');
            const validateButton = container.querySelector('#validate-isbn-btn');

            isbnInput.value = '978-0-7432-7356-5';
            validateButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            const addExistingButton = container.querySelector('#add-existing-btn');
            addExistingButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockNotificationService.showError).toHaveBeenCalledWith('Book already in your library');
        });
    });

    describe('Category Selection', () => {
        beforeEach(() => {
            bookAddition.render(container);
        });

        test('should load and display categories', async () => {
            const mockCategories = [
                { id: 1, name: 'Fiction' },
                { id: 2, name: 'Non-Fiction' },
                { id: 3, name: 'Science' }
            ];

            mockBookService.getCategories.mockResolvedValue(mockCategories);

            await bookAddition.loadCategories();

            const categorySelect = container.querySelector('#manual-category');
            expect(categorySelect.children.length).toBe(4); // Including default option
            expect(categorySelect.children[1].textContent).toBe('Fiction');
            expect(categorySelect.children[1].value).toBe('1');
        });

        test('should handle category loading failure', async () => {
            mockBookService.getCategories.mockRejectedValue(new Error('Failed to load categories'));

            await bookAddition.loadCategories();

            expect(mockNotificationService.showError).toHaveBeenCalledWith('Failed to load categories');
        });

        test('should require category selection for manual entry', () => {
            container.querySelector('#switch-to-manual-btn').click();

            const titleInput = container.querySelector('#manual-title');
            const authorInput = container.querySelector('#manual-author');
            const submitButton = container.querySelector('#submit-manual-book-btn');

            titleInput.value = 'Test Book';
            authorInput.value = 'Test Author';
            // Don't select category

            submitButton.click();

            expect(container.querySelector('.validation-errors').textContent).toContain('Category is required');
        });
    });

    describe('Cover Image Handling', () => {
        beforeEach(() => {
            bookAddition.render(container);
        });

        test('should display cover image when available', async () => {
            const mockValidationResponse = {
                valid: true,
                isbn: '978-0-7432-7356-5',
                title: 'The Great Gatsby',
                coverImageUrl: 'https://example.com/gatsby.jpg',
                existsInDatabase: false
            };

            mockBookService.validateBookByIsbn.mockResolvedValue(mockValidationResponse);

            const isbnInput = container.querySelector('#isbn-input');
            const validateButton = container.querySelector('#validate-isbn-btn');

            isbnInput.value = '978-0-7432-7356-5';
            validateButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            const coverImage = container.querySelector('.book-preview img');
            expect(coverImage).toBeTruthy();
            expect(coverImage.src).toBe('https://example.com/gatsby.jpg');
        });

        test('should show placeholder when no cover image available', async () => {
            const mockValidationResponse = {
                valid: true,
                isbn: '978-0-7432-7356-5',
                title: 'The Great Gatsby',
                coverImageUrl: null,
                existsInDatabase: false
            };

            mockBookService.validateBookByIsbn.mockResolvedValue(mockValidationResponse);

            const isbnInput = container.querySelector('#isbn-input');
            const validateButton = container.querySelector('#validate-isbn-btn');

            isbnInput.value = '978-0-7432-7356-5';
            validateButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            const placeholder = container.querySelector('.book-preview .cover-placeholder');
            expect(placeholder).toBeTruthy();
            expect(placeholder.textContent).toContain('No Cover Available');
        });

        test('should allow manual cover URL entry', () => {
            container.querySelector('#switch-to-manual-btn').click();

            const coverUrlInput = container.querySelector('#manual-cover-url');
            coverUrlInput.value = 'https://example.com/custom-cover.jpg';

            const inputEvent = new Event('input');
            coverUrlInput.dispatchEvent(inputEvent);

            const preview = container.querySelector('.cover-preview img');
            expect(preview).toBeTruthy();
            expect(preview.src).toBe('https://example.com/custom-cover.jpg');
        });

        test('should handle invalid cover URLs gracefully', () => {
            container.querySelector('#switch-to-manual-btn').click();

            const coverUrlInput = container.querySelector('#manual-cover-url');
            coverUrlInput.value = 'invalid-url';

            const inputEvent = new Event('input');
            coverUrlInput.dispatchEvent(inputEvent);

            const placeholder = container.querySelector('.cover-preview .cover-placeholder');
            expect(placeholder).toBeTruthy();
        });
    });

    describe('Form Navigation', () => {
        beforeEach(() => {
            bookAddition.render(container);
        });

        test('should navigate between steps correctly', () => {
            // Start at ISBN search
            expect(bookAddition.currentStep).toBe('search');
            expect(container.querySelector('#isbn-search-section').classList.contains('active')).toBe(true);

            // Move to manual entry
            container.querySelector('#switch-to-manual-btn').click();
            expect(bookAddition.currentStep).toBe('manual');
            expect(container.querySelector('#manual-entry-section').classList.contains('active')).toBe(true);

            // Go back to ISBN search
            container.querySelector('#back-to-isbn-btn').click();
            expect(bookAddition.currentStep).toBe('search');
            expect(container.querySelector('#isbn-search-section').classList.contains('active')).toBe(true);
        });

        test('should show progress indicator', () => {
            const progressIndicator = container.querySelector('.step-progress');
            expect(progressIndicator).toBeTruthy();

            const steps = progressIndicator.querySelectorAll('.step');
            expect(steps).toHaveLength(3);
            expect(steps[0].classList.contains('active')).toBe(true);
        });

        test('should update progress indicator when changing steps', () => {
            container.querySelector('#switch-to-manual-btn').click();

            const steps = container.querySelectorAll('.step');
            expect(steps[0].classList.contains('completed')).toBe(true);
            expect(steps[1].classList.contains('active')).toBe(true);
        });
    });

    describe('Data Persistence', () => {
        beforeEach(() => {
            bookAddition.render(container);
        });

        test('should preserve manual entry data when navigating', () => {
            container.querySelector('#switch-to-manual-btn').click();

            const titleInput = container.querySelector('#manual-title');
            const authorInput = container.querySelector('#manual-author');

            titleInput.value = 'Preserved Title';
            authorInput.value = 'Preserved Author';

            // Navigate away and back
            container.querySelector('#back-to-isbn-btn').click();
            container.querySelector('#switch-to-manual-btn').click();

            expect(container.querySelector('#manual-title').value).toBe('Preserved Title');
            expect(container.querySelector('#manual-author').value).toBe('Preserved Author');
        });

        test('should clear form when reset is triggered', () => {
            container.querySelector('#switch-to-manual-btn').click();

            const titleInput = container.querySelector('#manual-title');
            const authorInput = container.querySelector('#manual-author');

            titleInput.value = 'Test Title';
            authorInput.value = 'Test Author';

            bookAddition.resetForm();

            expect(titleInput.value).toBe('');
            expect(authorInput.value).toBe('');
            expect(bookAddition.currentStep).toBe('search');
            expect(bookAddition.selectedBook).toBeNull();
        });
    });

    describe('Accessibility', () => {
        beforeEach(() => {
            bookAddition.render(container);
        });

        test('should have proper form labels', () => {
            const inputs = container.querySelectorAll('input, select, textarea');
            inputs.forEach(input => {
                if (input.id) {
                    const label = container.querySelector(`label[for="${input.id}"]`);
                    expect(label).toBeTruthy();
                }
            });
        });

        test('should have ARIA attributes for form validation', () => {
            container.querySelector('#switch-to-manual-btn').click();

            const titleInput = container.querySelector('#manual-title');
            const submitButton = container.querySelector('#submit-manual-book-btn');

            // Trigger validation
            submitButton.click();

            expect(titleInput.getAttribute('aria-invalid')).toBe('true');
            expect(titleInput.getAttribute('aria-describedby')).toBeTruthy();
        });

        test('should announce step changes to screen readers', () => {
            container.querySelector('#switch-to-manual-btn').click();

            const announcement = container.querySelector('.sr-only[aria-live="polite"]');
            expect(announcement).toBeTruthy();
            expect(announcement.textContent).toContain('Manual entry step active');
        });

        test('should have keyboard navigation support', () => {
            const focusableElements = container.querySelectorAll('input, button, select, textarea');
            focusableElements.forEach(element => {
                expect(element.getAttribute('tabindex')).not.toBe('-1');
            });
        });
    });

    describe('Error Handling', () => {
        beforeEach(() => {
            bookAddition.render(container);
        });

        test('should handle network errors gracefully', async () => {
            const networkError = new Error('Network error');
            networkError.name = 'NetworkError';
            mockBookService.validateBookByIsbn.mockRejectedValue(networkError);

            const isbnInput = container.querySelector('#isbn-input');
            const validateButton = container.querySelector('#validate-isbn-btn');

            isbnInput.value = '978-0-7432-7356-5';
            validateButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockNotificationService.showError).toHaveBeenCalledWith('Network error. Please check your connection and try again.');
        });

        test('should handle service unavailable errors', async () => {
            const serviceError = new Error('Service unavailable');
            serviceError.status = 503;
            mockBookService.createBook.mockRejectedValue(serviceError);

            bookAddition.selectedBook = { title: 'Test', author: 'Test' };
            bookAddition.currentStep = 'confirmation';
            bookAddition.displayConfirmationStep();

            const confirmButton = container.querySelector('#confirm-add-book-btn');
            confirmButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockNotificationService.showError).toHaveBeenCalledWith('Service is temporarily unavailable. Please try again later.');
        });

        test('should provide fallback for missing API responses', async () => {
            mockBookService.validateBookByIsbn.mockResolvedValue(null);

            const isbnInput = container.querySelector('#isbn-input');
            const validateButton = container.querySelector('#validate-isbn-btn');

            isbnInput.value = '978-0-7432-7356-5';
            validateButton.click();

            await new Promise(resolve => setTimeout(resolve, 0));

            expect(mockNotificationService.showError).toHaveBeenCalledWith('Unable to validate ISBN. Please try manual entry.');
        });
    });
});