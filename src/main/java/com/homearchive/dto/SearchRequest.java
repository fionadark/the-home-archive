package com.homearchive.dto;

import com.homearchive.entity.PhysicalLocation;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * DTO for book search requests.
 * Handles validation and parameter binding for search operations.
 */
public class SearchRequest {
    
    @Size(max = 100, message = "Search query must not exceed 100 characters")
    private String query;
    
    private SortBy sortBy = SortBy.RELEVANCE;
    
    private SortOrder sortOrder = SortOrder.DESC;
    
    @Min(value = 1, message = "Limit must be at least 1")
    @Max(value = 50, message = "Limit must not exceed 50")
    private Integer limit = 50;
    
    private PhysicalLocation physicalLocation;
    
    // Constructors
    public SearchRequest() {}
    
    public SearchRequest(String query) {
        this.query = query;
    }
    
    public SearchRequest(String query, SortBy sortBy, SortOrder sortOrder, Integer limit) {
        this.query = query;
        this.sortBy = sortBy != null ? sortBy : SortBy.RELEVANCE;
        this.sortOrder = sortOrder != null ? sortOrder : SortOrder.DESC;
        this.limit = limit != null ? limit : 50;
    }
    
    // Getters and Setters
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
        this.sortBy = sortBy != null ? sortBy : SortBy.RELEVANCE;
    }
    
    public SortOrder getSortOrder() {
        return sortOrder;
    }
    
    public void setSortOrder(SortOrder sortOrder) {
        this.sortOrder = sortOrder != null ? sortOrder : SortOrder.DESC;
    }
    
    public Integer getLimit() {
        return limit;
    }
    
    public void setLimit(Integer limit) {
        this.limit = limit != null ? limit : 50;
    }
    
    public PhysicalLocation getPhysicalLocation() {
        return physicalLocation;
    }
    
    public void setPhysicalLocation(PhysicalLocation physicalLocation) {
        this.physicalLocation = physicalLocation;
    }
    
    // Utility methods
    public boolean hasQuery() {
        return query != null && !query.trim().isEmpty();
    }
    
    public String getTrimmedQuery() {
        return query != null ? query.trim() : "";
    }
    
    public boolean hasPhysicalLocationFilter() {
        return physicalLocation != null;
    }
    
    @Override
    public String toString() {
        return "SearchRequest{" +
                "query='" + query + '\'' +
                ", sortBy=" + sortBy +
                ", sortOrder=" + sortOrder +
                ", limit=" + limit +
                ", physicalLocation=" + physicalLocation +
                '}';
    }
}