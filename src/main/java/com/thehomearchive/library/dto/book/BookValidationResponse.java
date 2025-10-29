package com.thehomearchive.library.dto.book;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for book validation by ISBN.
 * Contains validation result and enriched book data from external sources.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookValidationResponse {

    private boolean valid;
    
    private String isbn;
    
    private String title;
    
    private String author;
    
    private String publisher;
    
    private Integer publicationYear;
    
    private Integer pageCount;
    
    private String description;
    
    private String coverImageUrl;
    
    private boolean existsInDatabase;
    
    private Long bookId;
    
    private String errorMessage;
    
    private boolean enrichedFromExternalSource;
    
    private String externalSource;

    // Default constructor
    public BookValidationResponse() {}

    // Constructor for valid response
    public BookValidationResponse(boolean valid, String isbn) {
        this.valid = valid;
        this.isbn = isbn;
    }

    // Getters and setters
    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
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

    public Integer getPageCount() {
        return pageCount;
    }

    public void setPageCount(Integer pageCount) {
        this.pageCount = pageCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = coverImageUrl;
    }

    public boolean isExistsInDatabase() {
        return existsInDatabase;
    }

    public void setExistsInDatabase(boolean existsInDatabase) {
        this.existsInDatabase = existsInDatabase;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isEnrichedFromExternalSource() {
        return enrichedFromExternalSource;
    }

    public void setEnrichedFromExternalSource(boolean enrichedFromExternalSource) {
        this.enrichedFromExternalSource = enrichedFromExternalSource;
    }

    public String getExternalSource() {
        return externalSource;
    }

    public void setExternalSource(String externalSource) {
        this.externalSource = externalSource;
    }
}