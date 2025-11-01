package com.thehomearchive.library.dto.search;

import java.time.LocalDateTime;

/**
 * DTO for book rating responses.
 * Contains rating information and metadata.
 */
public class BookRatingResponse {
    
    private Long id;
    private Long bookId;
    private String bookTitle; // For convenience
    private Long userId;
    private String userFullName; // For convenience (without exposing email)
    private Integer rating;
    private String review;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Constructors
    public BookRatingResponse() {
    }
    
    public BookRatingResponse(Long id, Long bookId, Long userId, Integer rating) {
        this.id = id;
        this.bookId = bookId;
        this.userId = userId;
        this.rating = rating;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public Long getBookId() {
        return bookId;
    }
    
    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }
    
    public String getBookTitle() {
        return bookTitle;
    }
    
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getUserFullName() {
        return userFullName;
    }
    
    public void setUserFullName(String userFullName) {
        this.userFullName = userFullName;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getReview() {
        return review;
    }
    
    public void setReview(String review) {
        this.review = review;
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
     * Check if this rating includes a review.
     *
     * @return true if review is present and not empty, false otherwise
     */
    public boolean hasReview() {
        return review != null && !review.trim().isEmpty();
    }
    
    /**
     * Get a truncated version of the review for display purposes.
     *
     * @param maxLength maximum length of the truncated review
     * @return truncated review or full review if shorter than maxLength
     */
    public String getTruncatedReview(int maxLength) {
        if (review == null || review.length() <= maxLength) {
            return review;
        }
        return review.substring(0, maxLength) + "...";
    }
    
    /**
     * Check if this is a positive rating (4 or 5 stars).
     *
     * @return true if rating is 4 or 5, false otherwise
     */
    public boolean isPositiveRating() {
        return rating != null && rating >= 4;
    }
    
    /**
     * Check if this is a negative rating (1 or 2 stars).
     *
     * @return true if rating is 1 or 2, false otherwise
     */
    public boolean isNegativeRating() {
        return rating != null && rating <= 2;
    }
    
    /**
     * Check if this is a neutral rating (3 stars).
     *
     * @return true if rating is 3, false otherwise
     */
    public boolean isNeutralRating() {
        return rating != null && rating == 3;
    }
    
    /**
     * Get a star rating representation (e.g., "★★★★☆").
     *
     * @return star rating string
     */
    public String getStarRating() {
        if (rating == null) {
            return "☆☆☆☆☆";
        }
        
        StringBuilder stars = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (i <= rating) {
                stars.append("★");
            } else {
                stars.append("☆");
            }
        }
        return stars.toString();
    }
    
    /**
     * Get rating category (Excellent, Good, Average, Poor, Terrible).
     *
     * @return rating category string
     */
    public String getRatingCategory() {
        if (rating == null) {
            return "Unrated";
        }
        
        switch (rating) {
            case 5: return "Excellent";
            case 4: return "Good";
            case 3: return "Average";
            case 2: return "Poor";
            case 1: return "Terrible";
            default: return "Invalid";
        }
    }
    
    /**
     * Check if this rating was recently created (within last 7 days).
     *
     * @return true if created within last 7 days, false otherwise
     */
    public boolean isRecentlyCreated() {
        return createdAt != null && createdAt.isAfter(LocalDateTime.now().minusDays(7));
    }
    
    /**
     * Check if this rating was recently updated (within last 24 hours).
     *
     * @return true if updated within last 24 hours, false otherwise
     */
    public boolean isRecentlyUpdated() {
        return updatedAt != null && 
               createdAt != null && 
               !updatedAt.equals(createdAt) && 
               updatedAt.isAfter(LocalDateTime.now().minusHours(24));
    }
    
    @Override
    public String toString() {
        return "BookRatingResponse{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", bookTitle='" + bookTitle + '\'' +
                ", userId=" + userId +
                ", userFullName='" + userFullName + '\'' +
                ", rating=" + rating +
                ", hasReview=" + hasReview() +
                ", createdAt=" + createdAt +
                '}';
    }
}