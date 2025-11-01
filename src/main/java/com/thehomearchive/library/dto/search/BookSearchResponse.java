package com.thehomearchive.library.dto.search;

import com.thehomearchive.library.dto.book.BookResponse;
import com.thehomearchive.library.dto.category.CategoryResponse;

/**
 * DTO for book search results with enhanced information.
 * Extends basic BookResponse with search-specific data like ratings.
 */
public class BookSearchResponse extends BookResponse {
    
    private Double averageRating; // Average rating from all users
    private Integer ratingCount; // Total number of ratings
    private Integer userRating; // Current user's rating (if authenticated)
    private CategoryResponse category; // Full category information
    
    // Search relevance score (for ranking results)
    private Double relevanceScore;
    
    // Constructors
    public BookSearchResponse() {
        super();
    }
    
    public BookSearchResponse(Long id, String title, String author) {
        super(id, title, author);
    }
    
    // Getters and Setters for new fields
    public Double getAverageRating() {
        return averageRating;
    }
    
    public void setAverageRating(Double averageRating) {
        this.averageRating = averageRating;
    }
    
    public Integer getRatingCount() {
        return ratingCount;
    }
    
    public void setRatingCount(Integer ratingCount) {
        this.ratingCount = ratingCount;
    }
    
    public Integer getUserRating() {
        return userRating;
    }
    
    public void setUserRating(Integer userRating) {
        this.userRating = userRating;
    }
    
    public CategoryResponse getCategory() {
        return category;
    }
    
    public void setCategory(CategoryResponse category) {
        this.category = category;
    }
    
    public Double getRelevanceScore() {
        return relevanceScore;
    }
    
    public void setRelevanceScore(Double relevanceScore) {
        this.relevanceScore = relevanceScore;
    }
    
    // Business methods
    
    /**
     * Check if this book has been rated by users.
     *
     * @return true if rating count is greater than 0, false otherwise
     */
    public boolean hasRatings() {
        return ratingCount != null && ratingCount > 0;
    }
    
    /**
     * Check if the current user has rated this book.
     *
     * @return true if user rating is present, false otherwise
     */
    public boolean isRatedByUser() {
        return userRating != null;
    }
    
    /**
     * Get a formatted average rating (e.g., "4.5" or "No ratings").
     *
     * @return formatted rating string
     */
    public String getFormattedAverageRating() {
        if (averageRating == null || !hasRatings()) {
            return "No ratings";
        }
        return String.format("%.1f", averageRating);
    }
    
    /**
     * Get a star rating representation (e.g., "★★★★☆").
     *
     * @return star rating string
     */
    public String getStarRating() {
        if (averageRating == null || !hasRatings()) {
            return "☆☆☆☆☆";
        }
        
        StringBuilder stars = new StringBuilder();
        int fullStars = averageRating.intValue();
        double remainder = averageRating - fullStars;
        
        // Add full stars
        for (int i = 0; i < fullStars && i < 5; i++) {
            stars.append("★");
        }
        
        // Add half star if remainder >= 0.5
        if (remainder >= 0.5 && fullStars < 5) {
            stars.append("½");
            fullStars++;
        }
        
        // Add empty stars to make 5 total
        for (int i = fullStars; i < 5; i++) {
            stars.append("☆");
        }
        
        return stars.toString();
    }
    
    /**
     * Check if this book is highly rated (4+ stars).
     *
     * @return true if average rating is 4 or higher, false otherwise
     */
    public boolean isHighlyRated() {
        return averageRating != null && averageRating >= 4.0;
    }
    
    /**
     * Get rating category (Excellent, Good, Average, Poor, Terrible).
     *
     * @return rating category string
     */
    public String getRatingCategory() {
        if (averageRating == null || !hasRatings()) {
            return "Unrated";
        }
        
        if (averageRating >= 4.5) return "Excellent";
        if (averageRating >= 3.5) return "Good";
        if (averageRating >= 2.5) return "Average";
        if (averageRating >= 1.5) return "Poor";
        return "Terrible";
    }
    
    /**
     * Check if this search result has a relevance score.
     *
     * @return true if relevance score is present, false otherwise
     */
    public boolean hasRelevanceScore() {
        return relevanceScore != null;
    }
    
    @Override
    public String toString() {
        return "BookSearchResponse{" +
                "id=" + getId() +
                ", title='" + getTitle() + '\'' +
                ", author='" + getAuthor() + '\'' +
                ", averageRating=" + averageRating +
                ", ratingCount=" + ratingCount +
                ", userRating=" + userRating +
                ", relevanceScore=" + relevanceScore +
                '}';
    }
}