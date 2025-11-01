package com.thehomearchive.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing a book in the library catalog.
 * This is the core book information shared across all users.
 * Personal user-specific data (reading status, personal notes, etc.) 
 * is stored in the PersonalLibrary entity.
 */
@Entity
@Table(name = "books", indexes = {
    @Index(name = "idx_books_title", columnList = "title"),
    @Index(name = "idx_books_author", columnList = "author"),
    @Index(name = "idx_books_isbn", columnList = "isbn"),
    @Index(name = "idx_books_category_id", columnList = "category_id"),
    @Index(name = "idx_books_publication_year", columnList = "publication_year")
})
public class Book {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 500, message = "Title must not exceed 500 characters")
    @Column(nullable = false, length = 500)
    private String title;
    
    @NotBlank(message = "Author is required")
    @Size(max = 300, message = "Author must not exceed 300 characters")
    @Column(nullable = false, length = 300)
    private String author;
    
    @Size(max = 20, message = "ISBN must not exceed 20 characters")
    @Column(length = 20, unique = true)
    private String isbn;
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    @Min(value = 1000, message = "Publication year must be after 1000")
    @Max(value = 2100, message = "Publication year must be before 2100")
    @Column(name = "publication_year")
    private Integer publicationYear;
    
    @Size(max = 200, message = "Publisher must not exceed 200 characters")
    @Column(length = 200)
    private String publisher;
    
    @Min(value = 1, message = "Page count must be positive")
    @Column(name = "page_count")
    private Integer pageCount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column(name = "cover_image_url", columnDefinition = "TEXT")
    private String coverImageUrl;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Book() {
    }
    
    public Book(String title, String author) {
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
    
    public String getIsbn() {
        return isbn;
    }
    
    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getPublicationYear() {
        return publicationYear;
    }
    
    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
    }
    
    public String getPublisher() {
        return publisher;
    }
    
    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }
    
    public Integer getPageCount() {
        return pageCount;
    }
    
    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }
    
    public Category getCategory() {
        return category;
    }
    
    public void setCategory(Category category) {
        this.category = category;
    }
    
    public String getCoverImageUrl() {
        return coverImageUrl;
    }
    
    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
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
                ", isbn='" + isbn + '\'' +
                ", publicationYear=" + publicationYear +
                '}';
    }
}