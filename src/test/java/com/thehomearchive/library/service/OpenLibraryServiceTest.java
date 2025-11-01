package com.thehomearchive.library.service;

import com.thehomearchive.library.config.OpenLibraryConfig;
import com.thehomearchive.library.dto.book.BookResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OpenLibraryServiceTest {

    @Mock
    private OpenLibraryConfig config;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OpenLibraryService openLibraryService;

    private Map<String, Object> mockOpenLibraryResponse;

    @BeforeEach
    void setUp() {
        // Use lenient stubbing to avoid UnnecessaryStubbingException
        lenient().when(config.getSearchUrl()).thenReturn("https://openlibrary.org/search.json");
        lenient().when(config.getDefaultFields()).thenReturn("title,author_name,first_publish_year,isbn,publisher,cover_i");
        lenient().when(config.getCoversUrl()).thenReturn("https://covers.openlibrary.org/b");

        // Create mock OpenLibrary API response
        mockOpenLibraryResponse = Map.of(
            "numFound", 1,
            "start", 0,
            "docs", List.of(
                Map.of(
                    "title", "The Great Gatsby",
                    "author_name", List.of("F. Scott Fitzgerald"),
                    "first_publish_year", 1925,
                    "isbn", List.of("9780743273565", "0743273567"),
                    "publisher", List.of("Scribner"),
                    "cover_i", 123456
                )
            )
        );
    }

    @Test
    void searchBooks_withValidQuery_returnsBookList() {
        // Given
        String query = "gatsby";
        int limit = 10;
        when(restTemplate.getForObject(any(URI.class), eq(Map.class)))
            .thenReturn(mockOpenLibraryResponse);

        // When
        List<BookResponse> result = openLibraryService.searchBooks(query, limit);

        // Then
        assertThat(result).hasSize(1);
        BookResponse book = result.get(0);
        assertThat(book.getTitle()).isEqualTo("The Great Gatsby");
        assertThat(book.getAuthor()).isEqualTo("F. Scott Fitzgerald");
        assertThat(book.getPublicationYear()).isEqualTo(1925);
        assertThat(book.getIsbn()).isEqualTo("9780743273565"); // Should prefer ISBN-13
        assertThat(book.getPublisher()).isEqualTo("Scribner");
        assertThat(book.getCoverImageUrl()).isEqualTo("https://covers.openlibrary.org/b/id/123456-M.jpg");

        verify(restTemplate).getForObject(any(URI.class), eq(Map.class));
    }

    @Test
    void searchBooks_withEmptyQuery_returnsEmptyList() {
        // Given
        String emptyQuery = "";

        // When
        List<BookResponse> result = openLibraryService.searchBooks(emptyQuery, 10);

        // Then
        assertThat(result).isEmpty();
        verify(restTemplate, never()).getForObject(any(URI.class), eq(Map.class));
    }

    @Test
    void searchBooks_withNullQuery_returnsEmptyList() {
        // Given
        String nullQuery = null;

        // When
        List<BookResponse> result = openLibraryService.searchBooks(nullQuery, 10);

        // Then
        assertThat(result).isEmpty();
        verify(restTemplate, never()).getForObject(any(URI.class), eq(Map.class));
    }

    @Test
    void searchByIsbn_withValidIsbn_returnsBookList() {
        // Given
        String isbn = "978-0-7432-7356-5"; // ISBN with hyphens
        when(restTemplate.getForObject(any(URI.class), eq(Map.class)))
            .thenReturn(mockOpenLibraryResponse);

        // When
        List<BookResponse> result = openLibraryService.searchByIsbn(isbn);

        // Then
        assertThat(result).hasSize(1);
        BookResponse book = result.get(0);
        assertThat(book.getTitle()).isEqualTo("The Great Gatsby");
        assertThat(book.getIsbn()).isEqualTo("9780743273565");

        verify(restTemplate).getForObject(any(URI.class), eq(Map.class));
    }

    @Test
    void searchByTitle_withValidTitle_returnsBookList() {
        // Given
        String title = "The Great Gatsby";
        int limit = 5;
        when(restTemplate.getForObject(any(URI.class), eq(Map.class)))
            .thenReturn(mockOpenLibraryResponse);

        // When
        List<BookResponse> result = openLibraryService.searchByTitle(title, limit);

        // Then
        assertThat(result).hasSize(1);
        BookResponse book = result.get(0);
        assertThat(book.getTitle()).isEqualTo("The Great Gatsby");

        verify(restTemplate).getForObject(any(URI.class), eq(Map.class));
    }

    @Test
    void searchByAuthor_withValidAuthor_returnsBookList() {
        // Given
        String author = "F. Scott Fitzgerald";
        int limit = 5;
        when(restTemplate.getForObject(any(URI.class), eq(Map.class)))
            .thenReturn(mockOpenLibraryResponse);

        // When
        List<BookResponse> result = openLibraryService.searchByAuthor(author, limit);

        // Then
        assertThat(result).hasSize(1);
        BookResponse book = result.get(0);
        assertThat(book.getAuthor()).isEqualTo("F. Scott Fitzgerald");

        verify(restTemplate).getForObject(any(URI.class), eq(Map.class));
    }

    @Test
    void searchBooks_withMultipleAuthors_joinsAuthorsCorrectly() {
        // Given
        Map<String, Object> multiAuthorResponse = Map.of(
            "numFound", 1,
            "start", 0,
            "docs", List.of(
                Map.of(
                    "title", "Good Omens",
                    "author_name", List.of("Terry Pratchett", "Neil Gaiman"),
                    "first_publish_year", 1990,
                    "isbn", List.of("9780060853976")
                )
            )
        );

        when(restTemplate.getForObject(any(URI.class), eq(Map.class)))
            .thenReturn(multiAuthorResponse);

        // When
        List<BookResponse> result = openLibraryService.searchBooks("good omens", 10);

        // Then
        assertThat(result).hasSize(1);
        BookResponse book = result.get(0);
        assertThat(book.getAuthor()).isEqualTo("Terry Pratchett, Neil Gaiman");
    }

    @Test
    void searchBooks_withNoTitleInResponse_skipsBook() {
        // Given
        Map<String, Object> noTitleResponse = Map.of(
            "numFound", 1,
            "start", 0,
            "docs", List.of(
                Map.of(
                    "author_name", List.of("Anonymous"),
                    "isbn", List.of("1234567890")
                    // No title field
                )
            )
        );

        when(restTemplate.getForObject(any(URI.class), eq(Map.class)))
            .thenReturn(noTitleResponse);

        // When
        List<BookResponse> result = openLibraryService.searchBooks("query", 10);

        // Then
        assertThat(result).isEmpty(); // Should skip books without titles
    }

    @Test
    void searchBooks_withEmptyResponse_returnsEmptyList() {
        // Given
        Map<String, Object> emptyResponse = Map.of(
            "numFound", 0,
            "start", 0,
            "docs", List.of()
        );

        when(restTemplate.getForObject(any(URI.class), eq(Map.class)))
            .thenReturn(emptyResponse);

        // When
        List<BookResponse> result = openLibraryService.searchBooks("nonexistent", 10);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void searchBooks_withNullResponse_returnsEmptyList() {
        // Given
        when(restTemplate.getForObject(any(URI.class), eq(Map.class)))
            .thenReturn(null);

        // When
        List<BookResponse> result = openLibraryService.searchBooks("query", 10);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    void searchBooks_withRestTemplateException_throwsRuntimeException() {
        // Given
        when(restTemplate.getForObject(any(URI.class), eq(Map.class)))
            .thenThrow(new RuntimeException("Network error"));

        // When & Then
        assertThatThrownBy(() -> openLibraryService.searchBooks("query", 10))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to search OpenLibrary");
    }

    @Test
    void searchBooks_withLargeLimit_capsAt100() {
        // Given
        String query = "test";
        int largeLimit = 500;
        when(restTemplate.getForObject(any(URI.class), eq(Map.class)))
            .thenReturn(mockOpenLibraryResponse);

        // When
        openLibraryService.searchBooks(query, largeLimit);

        // Then
        // Verify that the URI contains limit=100 (OpenLibrary max)
        verify(restTemplate).getForObject(argThat((URI uri) -> 
            uri.toString().contains("limit=100")), eq(Map.class));
    }

    @Test
    void mapDocumentToBook_withMissingFields_handlesGracefully() {
        // Given
        Map<String, Object> incompleteDoc = Map.of(
            "title", "Incomplete Book",
            "author_name", List.of("Some Author")
            // Missing other fields like ISBN, year, etc.
        );

        Map<String, Object> incompleteResponse = Map.of(
            "numFound", 1,
            "start", 0,
            "docs", List.of(incompleteDoc)
        );

        when(restTemplate.getForObject(any(URI.class), eq(Map.class)))
            .thenReturn(incompleteResponse);

        // When
        List<BookResponse> result = openLibraryService.searchBooks("query", 10);

        // Then
        assertThat(result).hasSize(1);
        BookResponse book = result.get(0);
        assertThat(book.getTitle()).isEqualTo("Incomplete Book");
        assertThat(book.getAuthor()).isEqualTo("Some Author");
        assertThat(book.getIsbn()).isNull();
        assertThat(book.getPublicationYear()).isNull();
        assertThat(book.getPublisher()).isNull();
        assertThat(book.getCoverImageUrl()).isNull();
    }
}