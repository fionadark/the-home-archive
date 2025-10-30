# OpenLibrary API Integration Testing

This document explains how to test the OpenLibraryService with the real OpenLibrary API.

## Test Files

### 1. `OpenLibraryServiceTest.java` (Unit Tests)
- **Purpose**: Fast unit tests with mocked API responses
- **Runs**: Automatically with `./gradlew test`
- **Coverage**: Service logic, error handling, field mapping

### 2. `OpenLibraryServiceIntegrationTest.java` (Conditional Integration Tests)
- **Purpose**: Full integration tests with real API calls
- **Runs**: Only when system property is set
- **Command**: `./gradlew test -Dtest.integration.external=true --tests OpenLibraryServiceIntegrationTest`
- **Coverage**: Real API responses, network handling, live data validation

### 3. `OpenLibraryServiceManualTest.java` (Manual Tests)
- **Purpose**: Manual verification and debugging
- **Runs**: Only when @Disabled annotation is removed
- **Coverage**: Step-by-step API verification with console output

## Running Integration Tests

### Option 1: Conditional Integration Tests (Recommended)

```bash
# Run all integration tests
./gradlew test -Dtest.integration.external=true --tests OpenLibraryServiceIntegrationTest

# Run specific integration test
./gradlew test -Dtest.integration.external=true --tests OpenLibraryServiceIntegrationTest.searchBooks_withValidQuery_shouldReturnRealResults
```

### Option 2: Manual Tests (For Debugging)

1. Edit `OpenLibraryServiceManualTest.java`
2. Remove `@Disabled` annotation from the test method you want to run
3. Run the test:
```bash
./gradlew test --tests OpenLibraryServiceManualTest.manualTest_searchBooks_shouldContactRealAPI
```

### Option 3: All Tests (Including Integration)

```bash
# Run all tests including integration tests
./gradlew test -Dtest.integration.external=true
```

## Test Coverage

### Integration Tests Verify:

1. **Real API Connectivity**
   - Successful HTTP requests to openlibrary.org
   - Proper timeout handling
   - Network error resilience

2. **Search Functionality**
   - General search: `searchBooks("The Great Gatsby", 5)`
   - ISBN search: `searchByIsbn("9780743273565")`
   - Title search: `searchByTitle("Pride and Prejudice", 3)`
   - Author search: `searchByAuthor("Jane Austen", 5)`

3. **Data Validation**
   - Real book data parsing
   - Field mapping accuracy (title, author, ISBN, year, etc.)
   - Cover image URL construction
   - Publication year validation (1400-2030)

4. **Edge Cases**
   - Empty/null queries
   - Obscure queries with no results
   - Large limits (respects 100 max)
   - Unicode/international content
   - Hyphenated ISBNs

5. **API Response Handling**
   - Diverse field population
   - Missing field graceful handling
   - Multiple authors joining
   - ISBN format preferences (ISBN-13 over ISBN-10)

## Expected Results

### Successful Tests Should Show:
- ✅ Books found for popular titles
- ✅ Proper field mapping from OpenLibrary response
- ✅ Valid cover image URLs when available
- ✅ Reasonable publication years
- ✅ Clean ISBN formatting (no hyphens)
- ✅ Multiple author handling

### Common Issues:
- **Network timeouts**: Increase timeout in test setup
- **Rate limiting**: Add delays between tests if needed
- **No results**: Some ISBNs/titles might not be in OpenLibrary
- **Field variations**: OpenLibrary data quality varies by book

## Configuration

Integration tests use these settings:
- **Base URL**: https://openlibrary.org
- **Timeout**: 10 seconds (longer for integration)
- **Max Results**: Respects OpenLibrary's 100 limit
- **Fields**: Uses complete field set from OpenLibrary schema

## CI/CD Considerations

- Unit tests run automatically (fast, no external dependencies)
- Integration tests are conditional (require explicit activation)
- Manual tests are disabled by default
- No API keys required (OpenLibrary is public)

## Troubleshooting

### Test Failures:
1. Check internet connectivity
2. Verify OpenLibrary API is accessible
3. Review timeout settings
4. Check for rate limiting

### No Results:
- Try different search terms
- Verify query formatting
- Check OpenLibrary.org directly

### Performance:
- Integration tests are slower (real network calls)
- Consider running subset of tests during development
- Full integration tests for CI/CD pipelines

## Example Output

When running manual tests, you'll see output like:
```
Testing OpenLibrary API integration...
General search results: 3
First book: The Great Gatsby by F. Scott Fitzgerald
ISBN search results: 1
ISBN book: The Great Gatsby (ISBN: 9780743273565)
Title search results: 2
Author search results: 3
  - Pride and Prejudice by Jane Austen
  - Sense and Sensibility by Jane Austen
  - Emma by Jane Austen
All tests completed successfully!
```