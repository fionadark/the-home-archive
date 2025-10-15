package com.homearchive.dto;

import java.util.List;

/**
 * DTO for book search responses.
 * Contains search results and metadata about the search operation.
 */
public class SearchResponse {
    
    private List<BookSearchDto> books;
    private int totalResults;
    private String query;
    private SortBy sortBy;
    private SortOrder sortOrder;
    private boolean hasMore;
    
    // Constructors
    public SearchResponse() {}
    
    public SearchResponse(List<BookSearchDto> books, int totalResults, String query) {
        this.books = books;
        this.totalResults = totalResults;
        this.query = query;
        this.hasMore = totalResults > (books != null ? books.size() : 0);
    }
    
    public SearchResponse(List<BookSearchDto> books, int totalResults, String query, 
                         SortBy sortBy, SortOrder sortOrder) {
        this.books = books;
        this.totalResults = totalResults;
        this.query = query;
        this.sortBy = sortBy;
        this.sortOrder = sortOrder;
        this.hasMore = totalResults > (books != null ? books.size() : 0);
    }
    
    // Getters and Setters
    public List<BookSearchDto> getBooks() {
        return books;
    }
    
    public void setBooks(List<BookSearchDto> books) {
        this.books = books;
        // Update hasMore when books list changes
        this.hasMore = totalResults > (books != null ? books.size() : 0);
    }
    
    public int getTotalResults() {
        return totalResults;
    }
    
    public void setTotalResults(int totalResults) {
        this.totalResults = totalResults;
        // Update hasMore when total changes
        this.hasMore = totalResults > (books != null ? books.size() : 0);
    }
    
    public String getQuery() {
        return query;
    }
    
    public void setQuery(String query) {
        this.query = query;
    }
    
    public SortBy getSortBy() {
        return sortBy;
    }
    
    public void setSortBy(SortBy sortBy) {
        this.sortBy = sortBy;
    }
    
    public SortOrder getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }
    
    public boolean isHasMore() {
        return hasMore;
    }
    
    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }
    
    // Utility methods
    public int getResultCount() {
        return books != null ? books.size() : 0;
    }
    
    public boolean isEmpty() {
        return books == null || books.isEmpty();
    }
    
    @Override
    public String toString() {
        return "SearchResponse{" +
                "books=" + (books != null ? books.size() : 0) + " items" +
                ", totalResults=" + totalResults +
                ", query='" + query + '\'' +
                ", sortBy=" + sortBy +
                ", sortOrder=" + sortOrder +
                ", hasMore=" + hasMore +
                '}';
    }
}