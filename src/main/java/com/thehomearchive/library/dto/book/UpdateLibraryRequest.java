package com.thehomearchive.library.dto.book;

import com.thehomearchive.library.entity.ReadingStatus;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating personal library entries.
 * Used when users update their personal book metadata.
 */
public class UpdateLibraryRequest {

    @Size(max = 200, message = "Physical location must not exceed 200 characters")
    private String physicalLocation;

    private ReadingStatus readingStatus;

    @Size(max = 1000, message = "Personal notes must not exceed 1000 characters")
    private String personalNotes;

    // Constructors
    public UpdateLibraryRequest() {
    }

    public UpdateLibraryRequest(String physicalLocation, ReadingStatus readingStatus, String personalNotes) {
        this.physicalLocation = physicalLocation;
        this.readingStatus = readingStatus;
        this.personalNotes = personalNotes;
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
        return "UpdateLibraryRequest{" +
                "physicalLocation='" + physicalLocation + '\'' +
                ", readingStatus=" + readingStatus +
                ", personalNotes='" + personalNotes + '\'' +
                '}';
    }
}