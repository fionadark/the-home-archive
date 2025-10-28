package com.thehomearchive.library.repository;

import com.thehomearchive.library.entity.SearchHistory;
import com.thehomearchive.library.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for SearchHistory entity with comprehensive search analytics operations.
 * Provides CRUD operations and specialized queries for search tracking and analytics.
 */
@Repository
public interface SearchHistoryRepository extends BaseRepository<SearchHistory, Long> {

    /**
     * Find search history by user.
     *
     * @param user the user whose search history to find
     * @return List of search history entries for the user
     */
    List<SearchHistory> findByUser(User user);

    /**
     * Find search history by user with pagination.
     *
     * @param user the user whose search history to find
     * @param pageable pagination parameters
     * @return Page of search history entries for the user
     */
    Page<SearchHistory> findByUser(User user, Pageable pageable);

    /**
     * Find search history by user ID.
     *
     * @param userId ID of the user
     * @return List of search history entries for the user
     */
    List<SearchHistory> findByUserId(Long userId);

    /**
     * Find search history by user ID with pagination.
     *
     * @param userId ID of the user
     * @param pageable pagination parameters
     * @return Page of search history entries for the user
     */
    Page<SearchHistory> findByUserId(Long userId, Pageable pageable);

    /**
     * Find search history by session ID (for anonymous users).
     *
     * @param sessionId the session identifier
     * @return List of search history entries for the session
     */
    List<SearchHistory> findBySessionId(String sessionId);

    /**
     * Find search history by session ID with pagination.
     *
     * @param sessionId the session identifier
     * @param pageable pagination parameters
     * @return Page of search history entries for the session
     */
    Page<SearchHistory> findBySessionId(String sessionId, Pageable pageable);

    /**
     * Find search history by query string (case-insensitive).
     *
     * @param query the search query
     * @return List of search history entries with the query
     */
    List<SearchHistory> findByQueryIgnoreCase(String query);

    /**
     * Find search history containing query string (case-insensitive).
     *
     * @param query the search query fragment
     * @return List of search history entries containing the query
     */
    List<SearchHistory> findByQueryContainingIgnoreCase(String query);

    /**
     * Find search history containing query string with pagination.
     *
     * @param query the search query fragment
     * @param pageable pagination parameters
     * @return Page of search history entries containing the query
     */
    Page<SearchHistory> findByQueryContainingIgnoreCase(String query, Pageable pageable);

    /**
     * Find searches within a date range.
     *
     * @param startDate start of the date range (inclusive)
     * @param endDate end of the date range (inclusive)
     * @return List of search history entries within the date range
     */
    List<SearchHistory> findBySearchedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find searches within a date range with pagination.
     *
     * @param startDate start of the date range (inclusive)
     * @param endDate end of the date range (inclusive)
     * @param pageable pagination parameters
     * @return Page of search history entries within the date range
     */
    Page<SearchHistory> findBySearchedAtBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    /**
     * Find recent searches since a specific date.
     *
     * @param since the earliest date to include
     * @return List of search history entries since the date
     */
    List<SearchHistory> findBySearchedAtAfter(LocalDateTime since);

    /**
     * Find recent searches since a specific date with pagination.
     *
     * @param since the earliest date to include
     * @param pageable pagination parameters
     * @return Page of search history entries since the date
     */
    Page<SearchHistory> findBySearchedAtAfter(LocalDateTime since, Pageable pageable);

    /**
     * Find searches with specific result count.
     *
     * @param resultCount the number of results returned
     * @return List of search history entries with the result count
     */
    List<SearchHistory> findByResultCount(Integer resultCount);

    /**
     * Find searches with no results (result count = 0 or null).
     *
     * @return List of search history entries with no results
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.resultCount = 0 OR sh.resultCount IS NULL")
    List<SearchHistory> findSearchesWithNoResults();

    /**
     * Find searches with no results with pagination.
     *
     * @param pageable pagination parameters
     * @return Page of search history entries with no results
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.resultCount = 0 OR sh.resultCount IS NULL")
    Page<SearchHistory> findSearchesWithNoResults(Pageable pageable);

    /**
     * Find searches with results (result count > 0).
     *
     * @return List of search history entries with results
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.resultCount > 0")
    List<SearchHistory> findSearchesWithResults();

    /**
     * Find searches with results with pagination.
     *
     * @param pageable pagination parameters
     * @return Page of search history entries with results
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.resultCount > 0")
    Page<SearchHistory> findSearchesWithResults(Pageable pageable);

    /**
     * Find authenticated user searches.
     *
     * @return List of search history entries by authenticated users
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user IS NOT NULL")
    List<SearchHistory> findAuthenticatedSearches();

    /**
     * Find authenticated user searches with pagination.
     *
     * @param pageable pagination parameters
     * @return Page of search history entries by authenticated users
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user IS NOT NULL")
    Page<SearchHistory> findAuthenticatedSearches(Pageable pageable);

    /**
     * Find anonymous searches.
     *
     * @return List of search history entries by anonymous users
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user IS NULL")
    List<SearchHistory> findAnonymousSearches();

    /**
     * Find anonymous searches with pagination.
     *
     * @param pageable pagination parameters
     * @return Page of search history entries by anonymous users
     */
    @Query("SELECT sh FROM SearchHistory sh WHERE sh.user IS NULL")
    Page<SearchHistory> findAnonymousSearches(Pageable pageable);

    /**
     * Find user's recent search queries (distinct).
     *
     * @param userId ID of the user
     * @param limit maximum number of recent queries to return
     * @return List of distinct recent search queries
     */
    @Query("SELECT DISTINCT sh.query FROM SearchHistory sh " +
           "WHERE sh.user.id = :userId " +
           "ORDER BY sh.searchedAt DESC " +
           "LIMIT :limit")
    List<String> findRecentQueriesByUser(@Param("userId") Long userId, @Param("limit") Integer limit);

    /**
     * Find session's recent search queries (distinct).
     *
     * @param sessionId the session identifier
     * @param limit maximum number of recent queries to return
     * @return List of distinct recent search queries
     */
    @Query("SELECT DISTINCT sh.query FROM SearchHistory sh " +
           "WHERE sh.sessionId = :sessionId " +
           "ORDER BY sh.searchedAt DESC " +
           "LIMIT :limit")
    List<String> findRecentQueriesBySession(@Param("sessionId") String sessionId, @Param("limit") Integer limit);

    /**
     * Find most popular search terms.
     *
     * @param limit maximum number of popular terms to return
     * @return List of arrays containing [query, count] pairs
     */
    @Query("SELECT sh.query, COUNT(sh) as searchCount " +
           "FROM SearchHistory sh " +
           "GROUP BY sh.query " +
           "ORDER BY searchCount DESC " +
           "LIMIT :limit")
    List<Object[]> findMostPopularQueries(@Param("limit") Integer limit);

    /**
     * Find most popular search terms within a date range.
     *
     * @param startDate start of the date range (inclusive)
     * @param endDate end of the date range (inclusive)
     * @param limit maximum number of popular terms to return
     * @return List of arrays containing [query, count] pairs
     */
    @Query("SELECT sh.query, COUNT(sh) as searchCount " +
           "FROM SearchHistory sh " +
           "WHERE sh.searchedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY sh.query " +
           "ORDER BY searchCount DESC " +
           "LIMIT :limit")
    List<Object[]> findMostPopularQueriesInPeriod(@Param("startDate") LocalDateTime startDate,
                                                 @Param("endDate") LocalDateTime endDate,
                                                 @Param("limit") Integer limit);

    /**
     * Count total searches by user.
     *
     * @param userId ID of the user
     * @return Total number of searches by the user
     */
    Long countByUserId(Long userId);

    /**
     * Count total searches by session.
     *
     * @param sessionId the session identifier
     * @return Total number of searches by the session
     */
    Long countBySessionId(String sessionId);

    /**
     * Count searches within a date range.
     *
     * @param startDate start of the date range (inclusive)
     * @param endDate end of the date range (inclusive)
     * @return Number of searches within the date range
     */
    Long countBySearchedAtBetween(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count searches with no results.
     *
     * @return Number of searches that returned no results
     */
    @Query("SELECT COUNT(sh) FROM SearchHistory sh WHERE sh.resultCount = 0 OR sh.resultCount IS NULL")
    Long countSearchesWithNoResults();

    /**
     * Count searches with results.
     *
     * @return Number of searches that returned results
     */
    @Query("SELECT COUNT(sh) FROM SearchHistory sh WHERE sh.resultCount > 0")
    Long countSearchesWithResults();

    /**
     * Get search statistics for a specific period.
     *
     * @param startDate start of the period (inclusive)
     * @param endDate end of the period (inclusive)
     * @return Array containing [totalSearches, uniqueQueries, averageResultCount]
     */
    @Query("SELECT COUNT(sh), COUNT(DISTINCT sh.query), AVG(sh.resultCount) " +
           "FROM SearchHistory sh " +
           "WHERE sh.searchedAt BETWEEN :startDate AND :endDate")
    Object[] getSearchStatisticsForPeriod(@Param("startDate") LocalDateTime startDate,
                                         @Param("endDate") LocalDateTime endDate);

    /**
     * Get daily search count for a period.
     *
     * @param startDate start of the period (inclusive)
     * @param endDate end of the period (inclusive)
     * @return List of arrays containing [date, count] pairs
     */
    @Query("SELECT DATE(sh.searchedAt), COUNT(sh) " +
           "FROM SearchHistory sh " +
           "WHERE sh.searchedAt BETWEEN :startDate AND :endDate " +
           "GROUP BY DATE(sh.searchedAt) " +
           "ORDER BY DATE(sh.searchedAt)")
    List<Object[]> getDailySearchCounts(@Param("startDate") LocalDateTime startDate,
                                       @Param("endDate") LocalDateTime endDate);

    /**
     * Find similar queries to the given query.
     *
     * @param query the reference query
     * @param similarityThreshold minimum similarity percentage (0-100)
     * @return List of similar queries
     */
    @Query("SELECT DISTINCT sh.query " +
           "FROM SearchHistory sh " +
           "WHERE sh.query != :query " +
           "AND LOWER(sh.query) LIKE CONCAT('%', LOWER(:query), '%')")
    List<String> findSimilarQueries(@Param("query") String query);

    /**
     * Delete search history older than specified date.
     *
     * @param cutoffDate the cutoff date (entries older than this will be deleted)
     * @return Number of deleted entries
     */
    Long deleteBySearchedAtBefore(LocalDateTime cutoffDate);

    /**
     * Delete search history by user.
     *
     * @param userId ID of the user
     * @return Number of deleted entries
     */
    Long deleteByUserId(Long userId);

    /**
     * Delete search history by session.
     *
     * @param sessionId the session identifier
     * @return Number of deleted entries
     */
    Long deleteBySessionId(String sessionId);

    /**
     * Find popular queries with pagination.
     *
     * @param pageable pagination parameters
     * @return Page of popular queries as strings
     */
    @Query("SELECT sh.query " +
           "FROM SearchHistory sh " +
           "GROUP BY sh.query " +
           "ORDER BY COUNT(sh) DESC")
    Page<String> findPopularQueries(Pageable pageable);

    /**
     * Find queries by partial match with pagination.
     *
     * @param partialQuery the partial query to match
     * @param pageable pagination parameters
     * @return Page of matching queries as strings
     */
    @Query("SELECT DISTINCT sh.query " +
           "FROM SearchHistory sh " +
           "WHERE LOWER(sh.query) LIKE LOWER(CONCAT('%', :partialQuery, '%'))")
    Page<String> findQueriesByPartialMatch(@Param("partialQuery") String partialQuery, Pageable pageable);
}