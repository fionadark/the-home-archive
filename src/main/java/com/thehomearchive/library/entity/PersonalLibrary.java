package com.thehomearchive.library.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Entity representing the relationship between a user and a book in their personal library.
 * This is the bridge entity that connects users to books with personal metadata.
 * Based on the database schema in V001__Initial_Schema.sql.
 */
@Entity
@Table(name = "personal_library", 
       uniqueConstraints = @UniqueConstraint(name = "unique_user_book", columnNames = {"user_id", "book_id"}),
       indexes = {
           @Index(name = "idx_personal_library_user_id", columnList = "user_id"),
           @Index(name = "idx_personal_library_book_id", columnList = "book_id"),
           @Index(name = "idx_personal_library_status", columnList = "reading_status")
       })
public class PersonalLibrary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Size(max = 200, message = "Physical location must not exceed 200 characters")
    @Column(name = "physical_location", length = 200)
    private String physicalLocation;

    @Min(value = 1, message = "Personal rating must be between 1 and 5")
    @Max(value = 5, message = "Personal rating must be between 1 and 5")
    @Column(name = "personal_rating")
    private Integer personalRating;

    @Lob
    @Column(name = "personal_notes", columnDefinition = "TEXT")
    private String personalNotes;

    @Enumerated(EnumType.STRING)
    @Column(name = "reading_status", length = 20)
    private ReadingStatus readingStatus;

    @Column(name = "date_started")
    private LocalDateTime dateStarted;

    @Column(name = "date_completed")
    private LocalDateTime dateCompleted;

    @CreationTimestamp
    @Column(name = "date_added", nullable = false, updatable = false)
    private LocalDateTime dateAdded;

    // Constructors
    public PersonalLibrary() {
        this.readingStatus = ReadingStatus.UNREAD;
    }

    public PersonalLibrary(User user, Book book) {
        this();
        this.user = user;
        this.book = book;
    }

    public PersonalLibrary(User user, Book book, String physicalLocation) {
        this(user, book);
        this.physicalLocation = physicalLocation;
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

    public String getPhysicalLocation() {
        return physicalLocation;
    }

    public void setPhysicalLocation(String physicalLocation) {
        this.physicalLocation = physicalLocation;
    }

    public Integer getPersonalRating() {
        return personalRating;
    }

    public void setPersonalRating(Integer personalRating) {
        this.personalRating = personalRating;
    }

    public String getPersonalNotes() {
        return personalNotes;
    }

    public void setPersonalNotes(String personalNotes) {
        this.personalNotes = personalNotes;
    }

    public ReadingStatus getReadingStatus() {
        return readingStatus;
    }

    public void setReadingStatus(ReadingStatus readingStatus) {
        this.readingStatus = readingStatus;
    }

    public LocalDateTime getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(LocalDateTime dateStarted) {
        this.dateStarted = dateStarted;
    }

    public LocalDateTime getDateCompleted() {
        return dateCompleted;
    }

    public void setDateCompleted(LocalDateTime dateCompleted) {
        this.dateCompleted = dateCompleted;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
    }

    // Utility methods
    /**
     * Mark book as started reading.
     */
    public void startReading() {
        this.readingStatus = ReadingStatus.READING;
        this.dateStarted = LocalDateTime.now();
    }

    /**
     * Mark book as finished reading.
     */
    public void finishReading() {
        this.readingStatus = ReadingStatus.READ;
        this.dateCompleted = LocalDateTime.now();
    }

    /**
     * Check if this book has been read.
     */
    public boolean isRead() {
        return this.readingStatus == ReadingStatus.READ;
    }

    /**
     * Check if this book is currently being read.
     */
    public boolean isCurrentlyReading() {
        return this.readingStatus == ReadingStatus.READING;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersonalLibrary that = (PersonalLibrary) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PersonalLibrary{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", bookId=" + (book != null ? book.getId() : null) +
                ", physicalLocation='" + physicalLocation + '\'' +
                ", personalRating=" + personalRating +
                ", readingStatus=" + readingStatus +
                '}';
    }
}