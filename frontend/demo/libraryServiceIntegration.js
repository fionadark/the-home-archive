/**
 * Integration test demonstration for LibraryService
 * This file shows how the LibraryService would be used in a real application
 */

// Example usage scenarios for the LibraryService

// 1. Initialize the service (this happens automatically when the script loads)
console.log('LibraryService initialized:', typeof libraryService === 'object');

// 2. Example: Loading user's library on page load
async function loadUserLibrary() {
    try {
        // Get first page of user's library
        const result = await libraryService.getUserLibrary({
            page: 0,
            size: 20,
            sort: 'title',
            direction: 'asc'
        });
        
        if (result.success) {
            console.log('Library loaded successfully:', result.data);
            displayLibraryBooks(result.data.content);
        } else {
            console.error('Failed to load library:', result.message);
            showErrorMessage(result.message);
        }
    } catch (error) {
        console.error('Error loading library:', error);
        showErrorMessage('Failed to load library. Please try again.');
    }
}

// 3. Example: Adding a book to the library
async function addBookToLibrary(bookId) {
    try {
        const result = await libraryService.addBookToLibrary({
            bookId: bookId,
            status: 'UNREAD',
            personalNotes: 'Added from search results'
        });
        
        if (result.success) {
            console.log('Book added successfully:', result.data);
            showSuccessMessage('Book added to your library!');
            // Refresh the library display
            loadUserLibrary();
        } else {
            console.error('Failed to add book:', result.message);
            showErrorMessage(result.message);
        }
    } catch (error) {
        console.error('Error adding book:', error);
        showErrorMessage('Failed to add book. Please try again.');
    }
}

// 4. Example: Updating reading status
async function updateReadingStatus(entryId, newStatus) {
    try {
        const updateData = {
            status: newStatus
        };
        
        // Add completion date if marking as read
        if (newStatus === 'READ') {
            updateData.readingEndDate = new Date().toISOString().split('T')[0];
        }
        
        const result = await libraryService.updateLibraryEntry(entryId, updateData);
        
        if (result.success) {
            console.log('Reading status updated:', result.data);
            showSuccessMessage(`Status updated to ${libraryService.formatReadingStatus(newStatus)}`);
            // Refresh the library display
            loadUserLibrary();
        } else {
            console.error('Failed to update status:', result.message);
            showErrorMessage(result.message);
        }
    } catch (error) {
        console.error('Error updating status:', error);
        showErrorMessage('Failed to update status. Please try again.');
    }
}

// 5. Example: Searching user's library
async function searchLibrary(searchQuery) {
    try {
        const result = await libraryService.searchUserLibrary(searchQuery, {
            page: 0,
            size: 20
        });
        
        if (result.success) {
            console.log('Search completed:', result.data);
            displaySearchResults(result.data.content);
        } else {
            console.error('Search failed:', result.message);
            showErrorMessage(result.message);
        }
    } catch (error) {
        console.error('Error searching library:', error);
        showErrorMessage('Search failed. Please try again.');
    }
}

// 6. Example: Getting library statistics
async function loadLibraryStats() {
    try {
        const result = await libraryService.getLibraryStatistics();
        
        if (result.success) {
            console.log('Library stats loaded:', result.data);
            displayLibraryStats(result.data);
        } else {
            console.error('Failed to load stats:', result.message);
        }
    } catch (error) {
        console.error('Error loading stats:', error);
    }
}

// 7. Example: Checking if a book is already in library (before adding)
async function checkBookInLibrary(bookId) {
    try {
        const result = await libraryService.isBookInLibrary(bookId);
        
        if (result.success) {
            return result.data.inLibrary;
        }
        return false;
    } catch (error) {
        console.error('Error checking book in library:', error);
        return false;
    }
}

// Example UI helper functions (these would be implemented in the actual UI code)
function displayLibraryBooks(books) {
    console.log('Displaying library books:', books);
    // Implementation would update the DOM with book list
}

function displaySearchResults(books) {
    console.log('Displaying search results:', books);
    // Implementation would update the DOM with search results
}

function displayLibraryStats(stats) {
    console.log('Displaying library stats:', stats);
    // Implementation would update the DOM with statistics
}

function showSuccessMessage(message) {
    console.log('Success:', message);
    // Implementation would show success notification to user
}

function showErrorMessage(message) {
    console.log('Error:', message);
    // Implementation would show error notification to user
}

// Example validation before making API calls
function validateBookData(bookData) {
    const errors = [];
    
    if (!bookData.bookId) {
        errors.push('Book ID is required');
    }
    
    if (!bookData.status || !libraryService.isValidReadingStatus(bookData.status)) {
        errors.push('Valid reading status is required');
    }
    
    if (bookData.userRating && !libraryService.isValidRating(bookData.userRating)) {
        errors.push('Rating must be between 1 and 5');
    }
    
    return errors;
}

// Example: Complete workflow for adding a book with validation
async function addBookWithValidation(bookData) {
    // Client-side validation
    const validationErrors = validateBookData(bookData);
    if (validationErrors.length > 0) {
        showErrorMessage('Validation errors: ' + validationErrors.join(', '));
        return;
    }
    
    // Check if book is already in library
    const alreadyInLibrary = await checkBookInLibrary(bookData.bookId);
    if (alreadyInLibrary) {
        showErrorMessage('This book is already in your library');
        return;
    }
    
    // Add the book
    await addBookToLibrary(bookData.bookId);
}

// Export functions for use in other modules
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        loadUserLibrary,
        addBookToLibrary,
        updateReadingStatus,
        searchLibrary,
        loadLibraryStats,
        checkBookInLibrary,
        addBookWithValidation
    };
}

console.log('Library service integration examples loaded successfully');