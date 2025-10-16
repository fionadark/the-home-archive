package com.homearchive.entity;

/**
 * Enum representing predefined physical locations for books in the home.
 * Provides standardized room options for better organization and searching.
 * This currently only represents rooms within my actual home (Fiona D).
 */
public enum PhysicalLocation {
    MASTER_BEDROOM("Master Bedroom"),
    FIONA_BEDROOM("Fiona's Bedroom"),
    GUEST_BEDROOM("Guest Bedroom"),
    DINING_ROOM("Dining Room"),
    LIVING_ROOM("Living Room"),
    HOME_OFFICE("Home Office"),
    KITCHEN("Kitchen"),
    BASEMENT("Basement Bedroom"),
    OTHER("Other");

    private final String displayName;

    PhysicalLocation(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get PhysicalLocation from display name or enum name.
     * Case-insensitive lookup.
     * 
     * @param value The display name or enum name to lookup
     * @return The corresponding PhysicalLocation, or OTHER if not found
     */
    public static PhysicalLocation fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return OTHER;
        }

        String normalized = value.trim().toUpperCase();

        // First try exact enum name match
        for (PhysicalLocation location : values()) {
            if (location.name().equals(normalized)) {
                return location;
            }
        }

        // Then try display name match
        for (PhysicalLocation location : values()) {
            if (location.displayName.toUpperCase().equals(normalized)) {
                return location;
            }
        }

        // Return OTHER for unrecognized values
        return OTHER;
    }

    @Override
    public String toString() {
        return displayName;
    }
}