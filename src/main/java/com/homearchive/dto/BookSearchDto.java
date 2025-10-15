package com.homearchive.dto;

import com.homearchive.entity.ReadingStatus;

/**
 * DTO for book search results.
 * Contains only the fields needed for search result display.
 */
public class BookSearchDto {
    
    private Long id;
    private String title;
    private String author;
    private String genre;
    private Integer publicationYear;
    private String isbn;
    private String publisher;
    private String physicalLocation;
    private ReadingStatus readingStatus;
    private Integer personalRating;
    
    // Constructors
    public BookSearchDto() {}
    
    public BookSearchDto(Long id, String title, String author, String genre, Integer publicationYear) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.genre = genre;
        this.publicationYear = publicationYear;
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
    
    public Integer getPublicationYear() {
        return publicationYear;
    }
    
    public void setPublicationYear(Integer publicationYear) {
        this.publicationYear = publicationYear;
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
    
    @Override
    public String toString() {
        return "BookSearchDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", genre='" + genre + '\'' +
                ", publicationYear=" + publicationYear +
                '}';
    }
}