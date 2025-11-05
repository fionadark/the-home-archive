package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.search.BookRatingRequest;
import com.thehomearchive.library.dto.search.BookRatingResponse;
import com.thehomearchive.library.entity.Book;
import com.thehomearchive.library.entity.BookRating;
import com.thehomearchive.library.entity.User;
import com.thehomearchive.library.entity.UserRole;
import com.thehomearchive.library.exception.DuplicateRatingException;
import com.thehomearchive.library.exception.ResourceNotFoundException;
import com.thehomearchive.library.repository.BookRatingRepository;
import com.thehomearchive.library.repository.BookRepository;
import com.thehomearchive.library.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private BookRatingRepository bookRatingRepository;

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private RatingService ratingService;

    private User testUser;
    private Book testBook;
    private BookRating testRating;
    private BookRatingRequest testRequest;

    @BeforeEach
    void setUp() {
        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(UserRole.USER);
        testUser.setEmailVerified(true);

        // Setup test book
        testBook = new Book();
        testBook.setId(1L);
        testBook.setTitle("Test Book");
        testBook.setAuthor("Test Author");
        testBook.setIsbn("1234567890");

        // Setup test rating
        testRating = new BookRating();
        testRating.setId(1L);
        testRating.setUser(testUser);
        testRating.setBook(testBook);
        testRating.setRating(5);
        testRating.setReview("Excellent book!");
        testRating.setCreatedAt(LocalDateTime.now());
        testRating.setUpdatedAt(LocalDateTime.now());

        // Setup test request
        testRequest = new BookRatingRequest();
        testRequest.setRating(5);
        testRequest.setReview("Excellent book!");
    }

    // ========== CREATE RATING TESTS ==========

    @Test
    void createRating_Success() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRatingRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(false);
        when(bookRatingRepository.save(any(BookRating.class))).thenReturn(testRating);

        // When
        BookRatingResponse response = ratingService.createRating(1L, 1L, testRequest);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getReview()).isEqualTo("Excellent book!");
        assertThat(response.getBookId()).isEqualTo(1L);
        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getBookTitle()).isEqualTo("Test Book");
        assertThat(response.getUserFullName()).isEqualTo("John Doe");

        verify(bookRatingRepository).save(any(BookRating.class));
    }

    @Test
    void createRating_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ratingService.createRating(1L, 1L, testRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("User not found with id: '1'");
    }

    @Test
    void createRating_BookNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ratingService.createRating(1L, 1L, testRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found with id: '1'");
    }

    @Test
    void createRating_DuplicateRating() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRatingRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> ratingService.createRating(1L, 1L, testRequest))
                .isInstanceOf(DuplicateRatingException.class)
                .hasMessageContaining("User has already rated this book");
    }

    @Test
    void createRating_WithoutReview() {
        // Given
        BookRatingRequest requestWithoutReview = new BookRatingRequest();
        requestWithoutReview.setRating(4);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRatingRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(false);
        when(bookRatingRepository.save(any(BookRating.class))).thenReturn(testRating);

        // When
        BookRatingResponse response = ratingService.createRating(1L, 1L, requestWithoutReview);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(5); // From saved rating
        verify(bookRatingRepository).save(any(BookRating.class));
    }

    // ========== UPDATE RATING TESTS ==========

    @Test
    void updateRating_Success() {
        // Given
        BookRatingRequest updateRequest = new BookRatingRequest();
        updateRequest.setRating(4);
        updateRequest.setReview("Updated review");

        when(bookRatingRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(testRating));
        when(bookRatingRepository.save(any(BookRating.class))).thenReturn(testRating);

        // When
        BookRatingResponse response = ratingService.updateRating(1L, 1L, updateRequest);

        // Then
        assertThat(response).isNotNull();
        verify(bookRatingRepository).save(testRating);
    }

    @Test
    void updateRating_NotFound() {
        // Given
        when(bookRatingRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ratingService.updateRating(1L, 1L, testRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rating not found for user 1 and book 1");
    }

    // ========== DELETE RATING TESTS ==========

    @Test
    void deleteRating_Success() {
        // Given
        when(bookRatingRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(testRating));

        // When
        ratingService.deleteRating(1L, 1L);

        // Then
        verify(bookRatingRepository).delete(testRating);
    }

    @Test
    void deleteRating_NotFound() {
        // Given
        when(bookRatingRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> ratingService.deleteRating(1L, 1L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rating not found for user 1 and book 1");
    }

    // ========== QUERY TESTS ==========

    @Test
    void getRating_Success() {
        // Given
        when(bookRatingRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(testRating));

        // When
        BookRatingResponse response = ratingService.getRating(1L, 1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(5);
        assertThat(response.getReview()).isEqualTo("Excellent book!");
    }

    @Test
    void getUserRatingForBook_Found() {
        // Given
        when(bookRatingRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(testRating));

        // When
        BookRatingResponse response = ratingService.getUserRatingForBook(1L, 1L);

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getRating()).isEqualTo(5);
    }

    @Test
    void getUserRatingForBook_NotFound() {
        // Given
        when(bookRatingRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.empty());

        // When
        BookRatingResponse response = ratingService.getUserRatingForBook(1L, 1L);

        // Then
        assertThat(response).isNull();
    }

    @Test
    void getBookRatings_Success() {
        // Given
        List<BookRating> ratings = List.of(testRating);
        Page<BookRating> ratingsPage = new PageImpl<>(ratings, PageRequest.of(0, 10), 1);
        
        when(bookRepository.existsById(1L)).thenReturn(true);
        when(bookRatingRepository.findByBookId(eq(1L), any(Pageable.class))).thenReturn(ratingsPage);

        // When
        Page<BookRatingResponse> response = ratingService.getBookRatings(1L, 0, 10, "createdAt", "DESC");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getRating()).isEqualTo(5);
    }

    @Test
    void getBookRatings_BookNotFound() {
        // Given
        when(bookRepository.existsById(1L)).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> ratingService.getBookRatings(1L, 0, 10, "createdAt", "DESC"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Book not found with id: '1'");
    }

    @Test
    void getUserRatings_Success() {
        // Given
        List<BookRating> ratings = List.of(testRating);
        Page<BookRating> ratingsPage = new PageImpl<>(ratings, PageRequest.of(0, 10), 1);
        
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookRatingRepository.findByUserId(eq(1L), any(Pageable.class))).thenReturn(ratingsPage);

        // When
        Page<BookRatingResponse> response = ratingService.getUserRatings(1L, 0, 10, "createdAt", "DESC");

        // Then
        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getRating()).isEqualTo(5);
    }

    // ========== STATISTICS TESTS ==========

    @Test
    void getBookRatingStatistics_Success() {
        // Given
        when(bookRepository.existsById(1L)).thenReturn(true);
        when(bookRatingRepository.findAverageRatingByBookId(1L)).thenReturn(4.5);
        when(bookRatingRepository.countByBookId(1L)).thenReturn(10L);
        when(bookRatingRepository.findRatingDistributionByBookId(1L))
                .thenReturn(List.of(new Object[]{5, 5L}, new Object[]{4, 3L}, new Object[]{3, 2L}));

        // When
        RatingService.BookRatingStatistics stats = ratingService.getBookRatingStatistics(1L);

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getBookId()).isEqualTo(1L);
        assertThat(stats.getAverageRating()).isEqualTo(4.5);
        assertThat(stats.getTotalRatings()).isEqualTo(10L);
        assertThat(stats.getDistribution()).hasSize(3);
        assertThat(stats.getDistribution().get(5)).isEqualTo(5L);
    }

    @Test
    void getUserRatingStatistics_Success() {
        // Given
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookRatingRepository.findUserRatingStatistics(1L))
                .thenReturn(new Object[]{10L, 4.2, 5, 3});

        // When
        RatingService.UserRatingStatistics stats = ratingService.getUserRatingStatistics(1L);

        // Then
        assertThat(stats).isNotNull();
        assertThat(stats.getUserId()).isEqualTo(1L);
        assertThat(stats.getTotalRatings()).isEqualTo(10L);
        assertThat(stats.getAverageRating()).isEqualTo(4.2);
        assertThat(stats.getHighestRating()).isEqualTo(5);
        assertThat(stats.getLowestRating()).isEqualTo(3);
    }

    // ========== BUSINESS LOGIC TESTS ==========

    @Test
    void hasUserRatedBook_True() {
        // Given
        when(bookRatingRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(true);

        // When
        boolean result = ratingService.hasUserRatedBook(1L, 1L);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void hasUserRatedBook_False() {
        // Given
        when(bookRatingRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(false);

        // When
        boolean result = ratingService.hasUserRatedBook(1L, 1L);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void getRatingDistributionPercentages_Success() {
        // Given
        when(bookRatingRepository.findRatingDistributionByBookId(1L))
                .thenReturn(List.of(
                    new Object[]{5, 5L},
                    new Object[]{4, 3L},
                    new Object[]{3, 2L}
                )); // Total: 10 ratings

        // When
        Map<Integer, Double> percentages = ratingService.getRatingDistributionPercentages(1L);

        // Then
        assertThat(percentages).hasSize(3);
        assertThat(percentages.get(5)).isEqualTo(50.0); // 5/10 * 100
        assertThat(percentages.get(4)).isEqualTo(30.0); // 3/10 * 100
        assertThat(percentages.get(3)).isEqualTo(20.0); // 2/10 * 100
    }

    @Test
    void getRatingDistributionPercentages_NoRatings() {
        // Given
        when(bookRatingRepository.findRatingDistributionByBookId(1L)).thenReturn(List.of());

        // When
        Map<Integer, Double> percentages = ratingService.getRatingDistributionPercentages(1L);

        // Then
        assertThat(percentages).isEmpty();
    }

    @Test
    void createOrUpdateRating_CreateNew() {
        // Given
        when(bookRatingRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.empty());
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookRepository.findById(1L)).thenReturn(Optional.of(testBook));
        when(bookRatingRepository.existsByUserIdAndBookId(1L, 1L)).thenReturn(false);
        when(bookRatingRepository.save(any(BookRating.class))).thenReturn(testRating);

        // When
        BookRatingResponse response = ratingService.createOrUpdateRating(1L, 1L, testRequest);

        // Then
        assertThat(response).isNotNull();
        verify(bookRatingRepository).save(any(BookRating.class));
    }

    @Test
    void createOrUpdateRating_UpdateExisting() {
        // Given
        when(bookRatingRepository.findByUserIdAndBookId(1L, 1L)).thenReturn(Optional.of(testRating));
        when(bookRatingRepository.save(any(BookRating.class))).thenReturn(testRating);

        // When
        BookRatingResponse response = ratingService.createOrUpdateRating(1L, 1L, testRequest);

        // Then
        assertThat(response).isNotNull();
        verify(bookRatingRepository).save(testRating);
    }

    // ========== BATCH OPERATIONS TESTS ==========

    @Test
    void getBatchAverageRatings_Success() {
        // Given
        List<Long> bookIds = List.of(1L, 2L);
        Map<Long, Double> expectedResult = Map.of(1L, 4.5, 2L, 3.8);
        when(bookRatingRepository.findAverageRatingsByBookIds(bookIds)).thenReturn(expectedResult);

        // When
        Map<Long, Double> result = ratingService.getBatchAverageRatings(bookIds);

        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getBatchAverageRatings_EmptyList() {
        // When
        Map<Long, Double> result = ratingService.getBatchAverageRatings(List.of());

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(bookRatingRepository);
    }

    @Test
    void getBatchAverageRatings_NullList() {
        // When
        Map<Long, Double> result = ratingService.getBatchAverageRatings(null);

        // Then
        assertThat(result).isEmpty();
        verifyNoInteractions(bookRatingRepository);
    }

    @Test
    void getBatchRatingCounts_Success() {
        // Given
        List<Long> bookIds = List.of(1L, 2L);
        Map<Long, Long> expectedResult = Map.of(1L, 10L, 2L, 5L);
        when(bookRatingRepository.findRatingCountsByBookIds(bookIds)).thenReturn(expectedResult);

        // When
        Map<Long, Long> result = ratingService.getBatchRatingCounts(bookIds);

        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    @Test
    void getBatchUserRatings_Success() {
        // Given
        List<Long> bookIds = List.of(1L, 2L);
        Map<Long, Integer> expectedResult = Map.of(1L, 5, 2L, 4);
        when(bookRatingRepository.findUserRatingsByBookIds(1L, bookIds)).thenReturn(expectedResult);

        // When
        Map<Long, Integer> result = ratingService.getBatchUserRatings(1L, bookIds);

        // Then
        assertThat(result).isEqualTo(expectedResult);
    }

    // ========== RECOMMENDATION TESTS ==========

    @Test
    void findSimilarUsers_Success() {
        // Given
        List<Long> expectedUserIds = List.of(2L, 3L, 4L);
        when(bookRatingRepository.findUsersWithSimilarRating(1L, 1L, 1)).thenReturn(expectedUserIds);

        // When
        List<Long> result = ratingService.findSimilarUsers(1L, 1L, 1);

        // Then
        assertThat(result).isEqualTo(expectedUserIds);
    }

    @Test
    void findCommonlyRatedBooks_Success() {
        // Given
        List<Long> expectedBookIds = List.of(1L, 2L, 3L);
        when(bookRatingRepository.findBooksRatedByBothUsers(1L, 2L)).thenReturn(expectedBookIds);

        // When
        List<Long> result = ratingService.findCommonlyRatedBooks(1L, 2L);

        // Then
        assertThat(result).isEqualTo(expectedBookIds);
    }
}