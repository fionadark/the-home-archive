package com.homearchive.entity;

import java.util.Arrays;

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
     * Finds a PhysicalLocation enum value by name or display name.
     * This method performs a case-insensitive search and returns the matching enum value.
     * If no match is found, it returns PhysicalLocation.OTHER.
     *
     * @param value The string value to search for (can be enum name or display name)
     * @return The matching PhysicalLocation enum value, or OTHER if no match is found
     */
    public static PhysicalLocation fromString(String value) {
        if (value == null || value.trim().isEmpty()) {
            return OTHER;
        }

        String normalizedValue = value.trim().toLowerCase();

        // Single-pass lookup using Stream API with optional chaining
        return Arrays.stream(PhysicalLocation.values())
                .filter(location -> location.name().toLowerCase().equals(normalizedValue) ||
                                  location.getDisplayName().toLowerCase().equals(normalizedValue))
                .findFirst()
                .orElse(OTHER);
    }    @Override
    public String toString() {
        return displayName;
    }
}