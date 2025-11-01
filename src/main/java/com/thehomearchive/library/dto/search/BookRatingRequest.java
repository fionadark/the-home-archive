package com.thehomearchive.library.dto.search;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO for creating or updating book ratings.
 * Contains rating value and optional review text.
 */
public class BookRatingRequest {
    
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
    
    @Size(max = 5000, message = "Review must not exceed 5000 characters")
    private String review;
    
    // Constructors
    public BookRatingRequest() {
    }
    
    public BookRatingRequest(Integer rating) {
        this.rating = rating;
    }
    
    public BookRatingRequest(Integer rating, String review) {
        this.rating = rating;
        this.review = review;
    }
    
    // Getters and Setters
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
     * Get the trimmed review text.
     *
     * @return trimmed review or null if no review
     */
    public String getTrimmedReview() {
        return review != null ? review.trim() : null;
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
    
    @Override
    public String toString() {
        return "BookRatingRequest{" +
                "rating=" + rating +
                ", hasReview=" + hasReview() +
                '}';
    }
}