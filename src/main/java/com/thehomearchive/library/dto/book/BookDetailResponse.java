package com.thehomearchive.library.dto.book;

import java.time.LocalDateTime;

/**
 * DTO for detailed book responses including user-specific data.
 * Extends BookResponse with additional fields for user ratings and metadata.
 */
public class BookDetailResponse extends BookResponse {
    
    private Integer userRating; // Current user's rating (if authenticated and rated)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public BookDetailResponse() {
        super();
    }
    
    public BookDetailResponse(BookResponse bookResponse) {
        super();
        this.setId(bookResponse.getId());
        this.setTitle(bookResponse.getTitle());
        this.setAuthor(bookResponse.getAuthor());
        this.setIsbn(bookResponse.getIsbn());
        this.setDescription(bookResponse.getDescription());
        this.setPublicationYear(bookResponse.getPublicationYear());
        this.setPublisher(bookResponse.getPublisher());
        this.setPageCount(bookResponse.getPageCount());
        this.setCategoryId(bookResponse.getCategoryId());
        this.setCategoryName(bookResponse.getCategoryName());
        this.setCoverImageUrl(bookResponse.getCoverImageUrl());
        this.setAverageRating(bookResponse.getAverageRating());
        this.setRatingCount(bookResponse.getRatingCount());
        this.setCreatedAt(bookResponse.getCreatedAt());
        this.setUpdatedAt(bookResponse.getUpdatedAt());
    }
    
    // Getters and Setters
    public Integer getUserRating() {
        return userRating;
    }
    
    public void setUserRating(Integer userRating) {
        this.userRating = userRating;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    // Business methods
    
    /**
     * Check if the current user has rated this book.
     *
     * @return true if user has rated the book, false otherwise
     */
    public boolean hasUserRating() {
        return userRating != null;
    }
    
    /**
     * Check if this book was recently added (within last 30 days).
     *
     * @return true if added within last 30 days, false otherwise
     */
    public boolean isRecentlyAdded() {
        return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(30));
    }
    
    /**
     * Check if this book was recently updated (within last 7 days).
     *
     * @return true if updated within last 7 days, false otherwise
     */
    public boolean isRecentlyUpdated() {
        return updatedAt != null && 
               createdAt != null && 
               !updatedAt.equals(createdAt) && 
               updatedAt.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    @Override
    public String toString() {
        return "BookDetailResponse{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", author='" + getAuthor() + '\'' +
                ", userRating=" + userRating +
                ", averageRating=" + getAverageRating() +
                ", ratingCount=" + getRatingCount() +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}