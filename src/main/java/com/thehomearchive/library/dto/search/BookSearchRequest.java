package com.thehomearchive.library.dto.search;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * DTO for book search requests.
 * Contains search parameters and pagination information.
 */
public class BookSearchRequest {
    
    @Size(max = 500, message = "Search query must not exceed 500 characters")
    private String q; // Search query (title, author, ISBN)
    
    private Long category; // Filter by category ID
    
    @Min(value = 0, message = "Page number must be non-negative")
    private Integer page = 0; // Page number (0-based)
    
    @Min(value = 1, message = "Page size must be at least 1")
    @Max(value = 100, message = "Page size must not exceed 100")
    private Integer size = 20; // Page size
    
    private SortCriteria sort = SortCriteria.TITLE; // Sort criteria
    
    private SortDirection direction = SortDirection.ASC; // Sort direction
    
    // Search scope flags
    private Boolean includeRatings = true; // Include rating information
    private Boolean includeCategory = true; // Include category information
    
    // Constructors
    public BookSearchRequest() {
    }
    
    public BookSearchRequest(String q) {
        this.q = q;
    }
    
    public BookSearchRequest(String q, Integer page, Integer size) {
        this.q = q;
        this.page = page;
        this.size = size;
    }
    
    // Getters and Setters
    public String getQ() {
        return q;
    }
    
    public void setQ(String q) {
        this.q = q;
    }
    
    public Long getCategory() {
        return category;
    }
    
    public void setCategory(Long category) {
        this.category = category;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    public SortCriteria getSort() {
        return sort;
    }
    
    public void setSort(SortCriteria sort) {
        this.sort = sort;
    }
    
    public SortDirection getDirection() {
        return direction;
    }
    
    public void setDirection(SortDirection direction) {
        this.direction = direction;
    }
    
    public Boolean getIncludeRatings() {
        return includeRatings;
    }
    
    public void setIncludeRatings(Boolean includeRatings) {
        this.includeRatings = includeRatings;
    }
    
    public Boolean getIncludeCategory() {
        return includeCategory;
    }
    
    public void setIncludeCategory(Boolean includeCategory) {
        this.includeCategory = includeCategory;
    }
    
    // Business methods
    
    /**
     * Check if this is a text-based search (has query string).
     *
     * @return true if query is present and not empty, false otherwise
     */
    public boolean hasQuery() {
        return q != null && !q.trim().isEmpty();
    }
    
    /**
     * Check if this search includes category filtering.
     *
     * @return true if category is specified, false otherwise
     */
    public boolean hasCategoryFilter() {
        return category != null;
    }
    
    /**
     * Get the normalized query string (trimmed).
     *
     * @return trimmed query or null if no query
     */
    public String getNormalizedQuery() {
        return q != null ? q.trim() : null;
    }
    
    /**
     * Check if ratings should be included in the response.
     *
     * @return true if ratings should be included, false otherwise
     */
    public boolean shouldIncludeRatings() {
        return Boolean.TRUE.equals(includeRatings);
    }
    
    /**
     * Check if category information should be included in the response.
     *
     * @return true if category should be included, false otherwise
     */
    public boolean shouldIncludeCategory() {
        return Boolean.TRUE.equals(includeCategory);
    }
    
    /**
     * Get the Spring Data Sort direction string.
     *
     * @return "asc" or "desc"
     */
    public String getDirectionString() {
        return direction != null ? direction.getValue() : SortDirection.ASC.getValue();
    }
    
    /**
     * Get the database column name for sorting.
     *
     * @return database column name
     */
    public String getSortColumn() {
        return sort != null ? sort.getColumn() : SortCriteria.TITLE.getColumn();
    }
    
    @Override
    public String toString() {
        return "BookSearchRequest{" +
                "q='" + q + '\'' +
                ", category=" + category +
                ", page=" + page +
                ", size=" + size +
                ", sort=" + sort +
                ", direction=" + direction +
                ", includeRatings=" + includeRatings +
                ", includeCategory=" + includeCategory +
                '}';
    }
    
    // Enums
    
    /**
     * Available sort criteria for book search.
     */
    public enum SortCriteria {
        TITLE("title"),
        AUTHOR("author"),
        PUBLICATION_YEAR("publicationYear"),
        RATING("averageRating"),
        CREATED_AT("createdAt");
        
        private final String column;
        
        SortCriteria(String column) {
            this.column = column;
        }
        
        public String getColumn() {
            return column;
        }
    }
    
    /**
     * Available sort directions.
     */
    public enum SortDirection {
        ASC("asc"),
        DESC("desc");
        
        private final String value;
        
        SortDirection(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
    }
}