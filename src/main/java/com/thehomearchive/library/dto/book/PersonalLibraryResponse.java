package com.thehomearchive.library.dto.book;

import com.thehomearchive.library.entity.ReadingStatus;
import java.time.LocalDateTime;

/**
 * DTO for personal library responses.
 * Contains book information along with personal metadata for a user's library.
 */
public class PersonalLibraryResponse {

    private Long id;
    private Long bookId;
    private String title;
    private String author;
    private String isbn;
    private String description;
    private Integer publicationYear;
    private String publisher;
    private Integer pageCount;
    private String categoryName;
    private String coverImageUrl;
    
    // Personal metadata
    private String physicalLocation;
    private ReadingStatus readingStatus;
    private String personalNotes;
    private LocalDateTime dateAdded;
    private LocalDateTime dateStarted;
    private LocalDateTime dateCompleted;

    // Constructors
    public PersonalLibraryResponse() {
    }

    public PersonalLibraryResponse(Long id, Long bookId, String title, String author) {
        this.id = id;
        this.bookId = bookId;
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

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
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

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
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

    public String getPersonalNotes() {
        return personalNotes;
    }

    public void setPersonalNotes(String personalNotes) {
        this.personalNotes = personalNotes;
    }

    public LocalDateTime getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(LocalDateTime dateAdded) {
        this.dateAdded = dateAdded;
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

    @Override
    public String toString() {
        return "PersonalLibraryResponse{" +
                "id=" + id +
                ", bookId=" + bookId +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", readingStatus=" + readingStatus +
                ", physicalLocation='" + physicalLocation + '\'' +
                '}';
    }
}