package com.thehomearchive.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a user's rating and review for a book.
 * Each user can only rate a book once, enforced by unique constraint.
 */
@Entity
@Table(name = "book_ratings", 
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_book_ratings_user_book", 
                           columnNames = {"user_id", "book_id"})
       },
       indexes = {
           @Index(name = "idx_book_ratings_user_id", columnList = "user_id"),
           @Index(name = "idx_book_ratings_book_id", columnList = "book_id"),
           @Index(name = "idx_book_ratings_rating", columnList = "rating"),
           @Index(name = "idx_book_ratings_created_at", columnList = "created_at")
       })
public class BookRating {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, foreignKey = @ForeignKey(name = "fk_book_ratings_user"))
    @NotNull(message = "User is required")
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false, foreignKey = @ForeignKey(name = "fk_book_ratings_book"))
    @NotNull(message = "Book is required")
    private Book book;
    
    @Column(nullable = false)
    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private Integer rating;
    
    @Lob
    @Column(columnDefinition = "TEXT")
    @Size(max = 5000, message = "Review must not exceed 5000 characters")
    private String review;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public BookRating() {
    }
    
    public BookRating(User user, Book book, Integer rating) {
        this.user = user;
        this.book = book;
        this.rating = rating;
    }
    
    public BookRating(User user, Book book, Integer rating, String review) {
        this.user = user;
        this.book = book;
        this.rating = rating;
        this.review = review;
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
    
    public Book getBook() {
        return book;
    }
    
    public void setBook(Book book) {
        this.book = book;
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
     * Check if this rating includes a written review.
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
    
    // Lifecycle callbacks
    
    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    // equals and hashCode based on natural key (user + book)
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookRating that = (BookRating) o;
        return Objects.equals(user, that.user) && Objects.equals(book, that.book);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(user, book);
    }
    
    @Override
    public String toString() {
        return "BookRating{" +
                "id=" + id +
                ", user=" + (user != null ? user.getId() : null) +
                ", book=" + (book != null ? book.getId() : null) +
                ", rating=" + rating +
                ", hasReview=" + hasReview() +
                ", createdAt=" + createdAt +
                '}';
    }
}