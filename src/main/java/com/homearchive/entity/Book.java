package com.homearchive.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a book in the home library.
 * Designed for full-text searching across all fields.
 */
@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_title_author", columnList = "title, author"),
    @Index(name = "idx_genre", columnList = "genre"),
    @Index(name = "idx_publication_year", columnList = "publication_year"),
    @Index(name = "idx_date_added", columnList = "date_added"),
    @Index(name = "idx_reading_status", columnList = "reading_status")
})
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String title;
    
    @NotBlank(message = "Author is required")
    @Size(max = 255, message = "Author must not exceed 255 characters")
    @Column(nullable = false, length = 255)
    private String author;
    
    @Size(max = 100, message = "Genre must not exceed 100 characters")
    @Column(length = 100)
    private String genre;
    
    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    @Column(length = 20)
    private String isbn;
    
    @Size(max = 255, message = "Publisher must not exceed 255 characters")
    @Column(length = 255)
    private String publisher;
    
    @Min(value = 1000, message = "Publication year must be after 1000")
    @Max(value = 2100, message = "Publication year must be before 2100")
    @Column(name = "publication_year")
    private Integer publicationYear;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Min(value = 1, message = "Page count must be positive")
    @Column(name = "page_count")
    private Integer pageCount;
    
    @Size(max = 100, message = "Physical location must not exceed 100 characters")
    @Column(name = "physical_location", length = 100)
    private String physicalLocation;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reading_status", length = 20)
    private ReadingStatus readingStatus;
    
    @Min(value = 1, message = "Personal rating must be between 1 and 5")
    @Max(value = 5, message = "Personal rating must be between 1 and 5")
    @Column(name = "personal_rating")
    private Integer personalRating;
    
    @NotNull
    @Column(name = "date_added", nullable = false)
    private LocalDateTime dateAdded;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Book() {
        this.dateAdded = LocalDateTime.now();
    }
    
    public Book(String title, String author) {
        this();
        this.title = title;
        this.author = author;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getAuthor() {
        return author;
    }
    
    public void setAuthor(String author) {
        this.author = author;
    }
    
    public String getGenre() {
        return genre;
    }
    
    public void setGenre(String genre) {
        this.genre = genre;
    }
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public Integer getPublicationYear() {
        return publicationYear;
    }
    
    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getPageCount() {
        return pageCount;
    }
    
    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }
    
    public String getPhysicalLocation() {
        return physicalLocation;
    }
    
    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }
    
    public ReadingStatus getReadingStatus() {
        return readingStatus;
    }
    
    public void setReadingStatus(ReadingStatus readingStatus) {
        this.readingStatus = readingStatus;
    }
    
    public Integer getPersonalRating() {
        return personalRating;
    }
    
    public void setPersonalRating(Integer personalRating) {
        this.personalRating = personalRating;
    }
    
    public LocalDateTime getDateAdded() {
        return dateAdded;
    }
    
    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
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
    
    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(id, book.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return "Book{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", genre='" + genre + '\'' +
                ", publicationYear=" + publicationYear +
                '}';
    }
}