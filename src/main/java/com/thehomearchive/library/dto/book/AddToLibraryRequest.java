package com.thehomearchive.library.dto.book;

import com.thehomearchive.library.entity.ReadingStatus;
import jakarta.validation.constraints.Size;

/**
 * DTO for adding books to personal library.
 * Used when users add books to their personal collection.
 */
public class AddToLibraryRequest {

    @Size(max = 200, message = "Physical location must not exceed 200 characters")
    private String physicalLocation;

    private ReadingStatus readingStatus = ReadingStatus.UNREAD;

    @Size(max = 1000, message = "Personal notes must not exceed 1000 characters")
    private String personalNotes;

    // Constructors
    public AddToLibraryRequest() {
    }

    public AddToLibraryRequest(String physicalLocation, ReadingStatus readingStatus) {
        this.physicalLocation = physicalLocation;
        this.readingStatus = readingStatus != null ? readingStatus : ReadingStatus.UNREAD;
    }

    // Getters and Setters
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

    @Override
    public String toString() {
        return "AddToLibraryRequest{" +
                "physicalLocation='" + physicalLocation + '\'' +
                ", readingStatus=" + readingStatus +
                ", personalNotes='" + personalNotes + '\'' +
                '}';
    }
}