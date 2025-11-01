package com.thehomearchive.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a user's search query history.
 * Tracks search queries for analytics and personalization.
 * Supports both authenticated users and anonymous sessions.
 */
@Entity
@Table(name = "search_history",
       indexes = {
           @Index(name = "idx_search_history_user_id", columnList = "user_id"),
           @Index(name = "idx_search_history_session_id", columnList = "session_id"),
           @Index(name = "idx_search_history_searched_at", columnList = "searched_at"),
           @Index(name = "idx_search_history_user_searched_at", columnList = "user_id, searched_at"),
           @Index(name = "idx_search_history_query", columnList = "query")
       })
public class SearchHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_search_history_user"))
    private User user; // nullable for anonymous searches
    
    @Column(nullable = false, length = 500)
    @NotBlank(message = "Search query cannot be empty")
    @Size(max = 500, message = "Search query must not exceed 500 characters")
    private String query;
    
    @Column(name = "result_count")
    @Min(value = 0, message = "Result count must be non-negative")
    private Integer resultCount;
    
    @Column(name = "session_id", length = 100)
    @Size(max = 100, message = "Session ID must not exceed 100 characters")
    private String sessionId; // For anonymous tracking
    
    @Column(name = "searched_at", nullable = false)
    @NotNull(message = "Search timestamp is required")
    private LocalDateTime searchedAt;
    
    // Additional metadata fields for analytics
    @Column(name = "user_agent", length = 500)
    @Size(max = 500, message = "User agent must not exceed 500 characters")
    private String userAgent;
    
    @Column(name = "ip_address", length = 45) // IPv6 compatible
    @Size(max = 45, message = "IP address must not exceed 45 characters")
    private String ipAddress;
    
    // Constructors
    public SearchHistory() {
    }
    
    public SearchHistory(String query) {
        this.query = query;
        this.searchedAt = LocalDateTime.now();
    }
    
    public SearchHistory(User user, String query) {
        this.user = user;
        this.query = query;
        this.searchedAt = LocalDateTime.now();
    }
    
    public SearchHistory(String sessionId, String query) {
        this.sessionId = sessionId;
        this.query = query;
        this.searchedAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        this.user = user;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public Integer getResultCount() {
        return resultCount;
    }
    
    public void setResultCount(Integer resultCount) {
        this.resultCount = resultCount;
    }
    
    public String getSessionId() {
        return sessionId;
    }
    
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }
    
    public LocalDateTime getSearchedAt() {
        return searchedAt;
    }
    
    public void setSearchedAt(LocalDateTime searchedAt) {
        this.searchedAt = searchedAt;
    }
    
    public String getUserAgent() {
        return userAgent;
    }
    
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
    
    public String getIpAddress() {
        return ipAddress;
    }
    
    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
    
    // Business methods
    
    /**
     * Check if this search was performed by an authenticated user.
     *
     * @return true if user is present, false otherwise
     */
    public boolean isAuthenticated() {
        return user != null;
    }
    
    /**
     * Check if this search was performed anonymously.
     *
     * @return true if user is null, false otherwise
     */
    public boolean isAnonymous() {
        return user == null;
    }
    
    /**
     * Check if search returned any results.
     *
     * @return true if result count is greater than 0, false otherwise
     */
    public boolean hasResults() {
        return resultCount != null && resultCount > 0;
    }
    
    /**
     * Get a normalized version of the query (trimmed and lowercase).
     *
     * @return normalized query string
     */
    public String getNormalizedQuery() {
        return query != null ? query.trim().toLowerCase() : null;
    }
    
    /**
     * Check if this search has tracking information (either user or session).
     *
     * @return true if user or sessionId is present, false otherwise
     */
    public boolean hasTrackingInfo() {
        return user != null || (sessionId != null && !sessionId.trim().isEmpty());
    }
    
    /**
     * Get the identifier for tracking purposes (user ID or session ID).
     *
     * @return user ID if authenticated, session ID if anonymous, null otherwise
     */
    public String getTrackingIdentifier() {
        if (user != null) {
            return "user:" + user.getId();
        }
        if (sessionId != null && !sessionId.trim().isEmpty()) {
            return "session:" + sessionId;
        }
        return null;
    }
    
    // Validation method to ensure either user or sessionId is provided
    @PrePersist
    @PreUpdate
    protected void onSave() {
        // Set timestamp if not already set
        if (searchedAt == null) {
            searchedAt = LocalDateTime.now();
        }
        
        // Validate tracking info
        if (user == null && (sessionId == null || sessionId.trim().isEmpty())) {
            throw new IllegalStateException("Either user or sessionId must be provided for search tracking");
        }
    }
    
    // equals and hashCode based on id
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchHistory that = (SearchHistory) o;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "SearchHistory{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : null) +
                ", query='" + query + '\'' +
                ", resultCount=" + resultCount +
                ", sessionId='" + sessionId + '\'' +
                ", searchedAt=" + searchedAt +
                ", isAuthenticated=" + isAuthenticated() +
                '}';
    }
}