package com.thehomearchive.library.repository;

import com.thehomearchive.library.entity.BookRating;
import com.thehomearchive.library.entity.Book;
import com.thehomearchive.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for BookRating entity with comprehensive rating and review operations.
 * Provides CRUD operations and specialized queries for book ratings and analytics.
 */
@Repository
public interface BookRatingRepository extends BaseRepository<BookRating, Long> {

    /**
     * Find a user's rating for a specific book.
     *
     * @param user the user who rated the book
     * @param book the book that was rated
     * @return Optional containing the rating if found
     */
    Optional<BookRating> findByUserAndBook(User user, Book book);

    /**
     * Find a user's rating for a specific book by IDs.
     *
     * @param userId ID of the user who rated the book
     * @param bookId ID of the book that was rated
     * @return Optional containing the rating if found
     */
    Optional<BookRating> findByUserIdAndBookId(Long userId, Long bookId);

    /**
     * Check if a user has already rated a specific book.
     *
     * @param userId ID of the user
     * @param bookId ID of the book
     * @return true if rating exists, false otherwise
     */
    boolean existsByUserIdAndBookId(Long userId, Long bookId);

    /**
     * Find all ratings by a specific user.
     *
     * @param user the user whose ratings to find
     * @return List of ratings by the user
     */
    List<BookRating> findByUser(User user);

    /**
     * Find all ratings by a specific user with pagination.
     *
     * @param user the user whose ratings to find
     * @param pageable pagination parameters
     * @return Page of ratings by the user
     */
    Page<BookRating> findByUser(User user, Pageable pageable);

    /**
     * Find all ratings by a specific user ID.
     *
     * @param userId ID of the user
     * @return List of ratings by the user
     */
    List<BookRating> findByUserId(Long userId);

    /**
     * Find all ratings by a specific user ID with pagination.
     *
     * @param userId ID of the user
     * @param pageable pagination parameters
     * @return Page of ratings by the user
     */
    Page<BookRating> findByUserId(Long userId, Pageable pageable);

    /**
     * Find all ratings for a specific book.
     *
     * @param book the book whose ratings to find
     * @return List of ratings for the book
     */
    List<BookRating> findByBook(Book book);

    /**
     * Find all ratings for a specific book with pagination.
     *
     * @param book the book whose ratings to find
     * @param pageable pagination parameters
     * @return Page of ratings for the book
     */
    Page<BookRating> findByBook(Book book, Pageable pageable);

    /**
     * Find all ratings for a specific book by ID.
     *
     * @param bookId ID of the book
     * @return List of ratings for the book
     */
    List<BookRating> findByBookId(Long bookId);

    /**
     * Find all ratings for a specific book by ID with pagination.
     *
     * @param bookId ID of the book
     * @param pageable pagination parameters
     * @return Page of ratings for the book
     */
    Page<BookRating> findByBookId(Long bookId, Pageable pageable);

    /**
     * Find ratings by specific rating value.
     *
     * @param rating the rating value (1-5)
     * @return List of ratings with the specified value
     */
    List<BookRating> findByRating(Integer rating);

    /**
     * Find ratings by specific rating value with pagination.
     *
     * @param rating the rating value (1-5)
     * @param pageable pagination parameters
     * @return Page of ratings with the specified value
     */
    Page<BookRating> findByRating(Integer rating, Pageable pageable);

    /**
     * Find ratings by rating range.
     *
     * @param minRating minimum rating (inclusive)
     * @param maxRating maximum rating (inclusive)
     * @return List of ratings within the range
     */
    List<BookRating> findByRatingBetween(Integer minRating, Integer maxRating);

    /**
     * Find ratings with written reviews.
     *
     * @return List of ratings that include review text
     */
    @Query("SELECT br FROM BookRating br WHERE br.review IS NOT NULL AND br.review != ''")
    List<BookRating> findRatingsWithReviews();

    /**
     * Find ratings with written reviews with pagination.
     *
     * @param pageable pagination parameters
     * @return Page of ratings that include review text
     */
    @Query("SELECT br FROM BookRating br WHERE br.review IS NOT NULL AND br.review != ''")
    Page<BookRating> findRatingsWithReviews(Pageable pageable);

    /**
     * Find ratings without written reviews.
     *
     * @return List of ratings without review text
     */
    @Query("SELECT br FROM BookRating br WHERE br.review IS NULL OR br.review = ''")
    List<BookRating> findRatingsWithoutReviews();

    /**
     * Find recent ratings within a time period.
     *
     * @param since the earliest date to include
     * @return List of ratings created since the specified date
     */
    List<BookRating> findByCreatedAtAfter(LocalDateTime since);

    /**
     * Find recent ratings within a time period with pagination.
     *
     * @param since the earliest date to include
     * @param pageable pagination parameters
     * @return Page of ratings created since the specified date
     */
    Page<BookRating> findByCreatedAtAfter(LocalDateTime since, Pageable pageable);

    /**
     * Find ratings updated within a time period.
     *
     * @param since the earliest update date to include
     * @return List of ratings updated since the specified date
     */
    List<BookRating> findByUpdatedAtAfter(LocalDateTime since);

    /**
     * Calculate average rating for a specific book.
     *
     * @param bookId ID of the book
     * @return Average rating as Double, null if no ratings exist
     */
    @Query("SELECT AVG(br.rating) FROM BookRating br WHERE br.book.id = :bookId")
    Double findAverageRatingByBookId(@Param("bookId") Long bookId);

    /**
     * Count total ratings for a specific book.
     *
     * @param bookId ID of the book
     * @return Total number of ratings for the book
     */
    Long countByBookId(Long bookId);

    /**
     * Count ratings by rating value for a specific book.
     *
     * @param bookId ID of the book
     * @param rating rating value to count
     * @return Number of ratings with the specified value
     */
    Long countByBookIdAndRating(Long bookId, Integer rating);

    /**
     * Count total ratings by a specific user.
     *
     * @param userId ID of the user
     * @return Total number of ratings by the user
     */
    Long countByUserId(Long userId);

    /**
     * Find top-rated books with minimum rating count.
     *
     * @param minRatingCount minimum number of ratings required
     * @param pageable pagination parameters
     * @return Page of book IDs ordered by average rating
     */
    @Query("SELECT br.book.id, AVG(br.rating) as avgRating, COUNT(br) as ratingCount " +
           "FROM BookRating br " +
           "GROUP BY br.book.id " +
           "HAVING COUNT(br) >= :minRatingCount " +
           "ORDER BY avgRating DESC")
    Page<Object[]> findTopRatedBooks(@Param("minRatingCount") Long minRatingCount, Pageable pageable);

    /**
     * Find rating distribution for a specific book.
     *
     * @param bookId ID of the book
     * @return List of arrays containing [rating, count] pairs
     */
    @Query("SELECT br.rating, COUNT(br) " +
           "FROM BookRating br " +
           "WHERE br.book.id = :bookId " +
           "GROUP BY br.rating " +
           "ORDER BY br.rating")
    List<Object[]> findRatingDistributionByBookId(@Param("bookId") Long bookId);

    /**
     * Find user's rating statistics.
     *
     * @param userId ID of the user
     * @return Array containing [totalRatings, averageRating, highestRating, lowestRating]
     */
    @Query("SELECT COUNT(br), AVG(br.rating), MAX(br.rating), MIN(br.rating) " +
           "FROM BookRating br " +
           "WHERE br.user.id = :userId")
    Object[] findUserRatingStatistics(@Param("userId") Long userId);

    /**
     * Find books rated by both users (for recommendation purposes).
     *
     * @param userId1 ID of the first user
     * @param userId2 ID of the second user
     * @return List of book IDs rated by both users
     */
    @Query("SELECT br1.book.id " +
           "FROM BookRating br1, BookRating br2 " +
           "WHERE br1.user.id = :userId1 " +
           "AND br2.user.id = :userId2 " +
           "AND br1.book.id = br2.book.id")
    List<Long> findBooksRatedByBothUsers(@Param("userId1") Long userId1, @Param("userId2") Long userId2);

    /**
     * Find users who rated a book similarly (within rating tolerance).
     *
     * @param userId ID of the reference user
     * @param bookId ID of the book
     * @param tolerance maximum difference in rating
     * @return List of user IDs with similar ratings
     */
    @Query("SELECT br2.user.id " +
           "FROM BookRating br1, BookRating br2 " +
           "WHERE br1.user.id = :userId " +
           "AND br1.book.id = :bookId " +
           "AND br2.book.id = :bookId " +
           "AND br2.user.id != :userId " +
           "AND ((br1.rating - br2.rating) <= :tolerance AND (br1.rating - br2.rating) >= -:tolerance)")
    List<Long> findUsersWithSimilarRating(@Param("userId") Long userId, 
                                         @Param("bookId") Long bookId, 
                                         @Param("tolerance") Integer tolerance);

    /**
     * Delete all ratings for a specific book.
     *
     * @param bookId ID of the book
     * @return Number of deleted ratings
     */
    Long deleteByBookId(Long bookId);

    /**
     * Delete all ratings by a specific user.
     *
     * @param userId ID of the user
     * @return Number of deleted ratings
     */
    Long deleteByUserId(Long userId);
}