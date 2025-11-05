package com.thehomearchive.library.dto.search;

import java.util.List;

/**
 * DTO for paginated book search results.
 * Follows Spring Data Page contract for consistent pagination.
 */
public class BookSearchPageResponse {
    
    private List<BookSearchResponse> content; // Books on current page
    private Integer page; // Current page number (0-based)
    private Integer size; // Page size
    private Long totalElements; // Total number of books matching criteria
    private Integer totalPages; // Total number of pages
    private Boolean first; // Is this the first page?
    private Boolean last; // Is this the last page?
    private Integer numberOfElements; // Number of elements on current page
    
    // Search metadata
    private String query; // Original search query
    private Long searchTimeMs; // Search execution time in milliseconds
    private Boolean hasNext; // Are there more pages?
    private Boolean hasPrevious; // Are there previous pages?
    
    // Constructors
    public BookSearchPageResponse() {
    }
    
    public BookSearchPageResponse(List<BookSearchResponse> content, Integer page, Integer size, 
                                  Long totalElements, Integer totalPages) {
        this.content = content;
        this.page = page;
        this.size = size;
        this.totalElements = totalElements;
        this.totalPages = totalPages;
        this.numberOfElements = content != null ? content.size() : 0;
        this.first = page != null && page == 0;
        this.last = page != null && totalPages != null && page >= (totalPages - 1);
        this.hasNext = !Boolean.TRUE.equals(last);
        this.hasPrevious = !Boolean.TRUE.equals(first);
    }
    
    // Getters and Setters
    public List<BookSearchResponse> getContent() {
        return content;
    }
    
    public void setContent(List<BookSearchResponse> content) {
        this.content = content;
        this.numberOfElements = content != null ? content.size() : 0;
    }
    
    public Integer getPage() {
        return page;
    }
    
    public void setPage(Integer page) {
        this.page = page;
        updateNavigationFlags();
    }
    
    public Integer getSize() {
        return size;
    }
    
    public void setSize(Integer size) {
        this.size = size;
    }
    
    public Long getTotalElements() {
        return totalElements;
    }
    
    public void setTotalElements(Long totalElements) {
        this.totalElements = totalElements;
    }
    
    public Integer getTotalPages() {
        return totalPages;
    }
    
    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
        updateNavigationFlags();
    }
    
    public Boolean getFirst() {
        return first;
    }
    
    public void setFirst(Boolean first) {
        this.first = first;
    }
    
    public Boolean getLast() {
        return last;
    }
    
    public void setLast(Boolean last) {
        this.last = last;
    }
    
    public Integer getNumberOfElements() {
        return numberOfElements;
    }
    
    public void setNumberOfElements(Integer numberOfElements) {
        this.numberOfElements = numberOfElements;
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public Long getSearchTimeMs() {
        return searchTimeMs;
    }
    
    public void setSearchTimeMs(Long searchTimeMs) {
        this.searchTimeMs = searchTimeMs;
    }
    
    public Boolean getHasNext() {
        return hasNext;
    }
    
    public void setHasNext(Boolean hasNext) {
        this.hasNext = hasNext;
    }
    
    public Boolean getHasPrevious() {
        return hasPrevious;
    }
    
    public void setHasPrevious(Boolean hasPrevious) {
        this.hasPrevious = hasPrevious;
    }
    
    // Business methods
    
    /**
     * Check if the search returned any results.
     *
     * @return true if content is not empty, false otherwise
     */
    public boolean hasContent() {
        return content != null && !content.isEmpty();
    }
    
    /**
     * Check if this is an empty result set.
     *
     * @return true if no results found, false otherwise
     */
    public boolean isEmpty() {
        return !hasContent();
    }
    
    /**
     * Get the number of results on the current page.
     *
     * @return number of elements on current page
     */
    public int getContentSize() {
        return content != null ? content.size() : 0;
    }
    
    /**
     * Check if there are more pages after the current one.
     *
     * @return true if there are more pages, false otherwise
     */
    public boolean hasNextPage() {
        return Boolean.TRUE.equals(hasNext);
    }
    
    /**
     * Check if there are pages before the current one.
     *
     * @return true if there are previous pages, false otherwise
     */
    public boolean hasPreviousPage() {
        return Boolean.TRUE.equals(hasPrevious);
    }
    
    /**
     * Get the next page number (if available).
     *
     * @return next page number or null if no next page
     */
    public Integer getNextPage() {
        return hasNextPage() ? page + 1 : null;
    }
    
    /**
     * Get the previous page number (if available).
     *
     * @return previous page number or null if no previous page
     */
    public Integer getPreviousPage() {
        return hasPreviousPage() ? page - 1 : null;
    }
    
    /**
     * Get formatted search time for display.
     *
     * @return formatted search time string
     */
    public String getFormattedSearchTime() {
        if (searchTimeMs == null) {
            return "N/A";
        }
        if (searchTimeMs < 1000) {
            return searchTimeMs + "ms";
        } else {
            return String.format("%.2fs", searchTimeMs / 1000.0);
        }
    }
    
    /**
     * Get result range description (e.g., "1-20 of 100 results").
     *
     * @return result range string
     */
    public String getResultRange() {
        if (!hasContent()) {
            return "No results";
        }
        
        int start = (page * size) + 1;
        int end = start + getContentSize() - 1;
        
        return String.format("%d-%d of %d results", start, end, totalElements);
    }
    
    /**
     * Update navigation flags based on current page and total pages.
     */
    private void updateNavigationFlags() {
        if (page != null && totalPages != null) {
            this.first = page == 0;
            this.last = page >= (totalPages - 1);
            this.hasNext = !last;
            this.hasPrevious = !first;
        }
    }
    
    @Override
    public String toString() {
        return "BookSearchPageResponse{" +
                "page=" + page +
                ", size=" + size +
                ", totalElements=" + totalElements +
                ", totalPages=" + totalPages +
                ", numberOfElements=" + numberOfElements +
                ", query='" + query + '\'' +
                ", searchTimeMs=" + searchTimeMs +
                ", hasContent=" + hasContent() +
                '}';
    }
}