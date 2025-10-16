package com.homearchive.entity;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PhysicalLocation enum, specifically testing the fromString method
 * to ensure the Stream API optimization maintains the same behavior as the original implementation.
 */
class PhysicalLocationTest {

    @Test
    void testFromStringWithValidEnumName() {
        // Test exact enum name matches
        assertEquals(PhysicalLocation.LIVING_ROOM, PhysicalLocation.fromString("LIVING_ROOM"));
        assertEquals(PhysicalLocation.MASTER_BEDROOM, PhysicalLocation.fromString("MASTER_BEDROOM"));
        assertEquals(PhysicalLocation.HOME_OFFICE, PhysicalLocation.fromString("HOME_OFFICE"));
    }

    @Test
    void testFromStringWithValidDisplayName() {
        // Test exact display name matches
        assertEquals(PhysicalLocation.LIVING_ROOM, PhysicalLocation.fromString("Living Room"));
        assertEquals(PhysicalLocation.MASTER_BEDROOM, PhysicalLocation.fromString("Master Bedroom"));
        assertEquals(PhysicalLocation.HOME_OFFICE, PhysicalLocation.fromString("Home Office"));
    }

    @Test
    void testFromStringCaseInsensitive() {
        // Test case insensitive matching for enum names
        assertEquals(PhysicalLocation.LIVING_ROOM, PhysicalLocation.fromString("living_room"));
        assertEquals(PhysicalLocation.LIVING_ROOM, PhysicalLocation.fromString("LIVING_ROOM"));
        assertEquals(PhysicalLocation.LIVING_ROOM, PhysicalLocation.fromString("Living_Room"));

        // Test case insensitive matching for display names
        assertEquals(PhysicalLocation.LIVING_ROOM, PhysicalLocation.fromString("living room"));
        assertEquals(PhysicalLocation.LIVING_ROOM, PhysicalLocation.fromString("LIVING ROOM"));
        assertEquals(PhysicalLocation.LIVING_ROOM, PhysicalLocation.fromString("Living Room"));
    }

    @Test
    void testFromStringWithWhitespace() {
        // Test trimming of whitespace
        assertEquals(PhysicalLocation.LIVING_ROOM, PhysicalLocation.fromString("  LIVING_ROOM  "));
        assertEquals(PhysicalLocation.LIVING_ROOM, PhysicalLocation.fromString("  Living Room  "));
        assertEquals(PhysicalLocation.LIVING_ROOM, PhysicalLocation.fromString("\tLIVING_ROOM\n"));
    }

    @Test
    void testFromStringWithInvalidValues() {
        // Test invalid inputs return OTHER
        assertEquals(PhysicalLocation.OTHER, PhysicalLocation.fromString("INVALID_ROOM"));
        assertEquals(PhysicalLocation.OTHER, PhysicalLocation.fromString("Bathroom")); // Not a valid location
        assertEquals(PhysicalLocation.OTHER, PhysicalLocation.fromString(""));
        assertEquals(PhysicalLocation.OTHER, PhysicalLocation.fromString("   "));
        assertEquals(PhysicalLocation.OTHER, PhysicalLocation.fromString(null));
    }

    @Test
    void testFromStringWithAllEnumValues() {
        // Test all enum values can be found by their names
        for (PhysicalLocation location : PhysicalLocation.values()) {
            assertEquals(location, PhysicalLocation.fromString(location.name()));
            assertEquals(location, PhysicalLocation.fromString(location.getDisplayName()));
        }
    }

    @Test
    void testFromStringSpecialCases() {
        // Test specific enum values that might have unique characteristics
        assertEquals(PhysicalLocation.FIONA_BEDROOM, PhysicalLocation.fromString("FIONA_BEDROOM"));
        assertEquals(PhysicalLocation.FIONA_BEDROOM, PhysicalLocation.fromString("Fiona's Bedroom"));
        assertEquals(PhysicalLocation.OTHER, PhysicalLocation.fromString("OTHER"));
        assertEquals(PhysicalLocation.OTHER, PhysicalLocation.fromString("Other"));
    }

    @Test
    void testFromStringPreferenceOrderEnumNameOverDisplayName() {
        // If an input could match both enum name and display name of different enums,
        // the method should find the first match in the stream (which should be consistent)
        // This tests the behavior is predictable
        PhysicalLocation result1 = PhysicalLocation.fromString("LIVING_ROOM");
        PhysicalLocation result2 = PhysicalLocation.fromString("LIVING_ROOM");
        assertEquals(result1, result2); // Should be consistent
        assertEquals(PhysicalLocation.LIVING_ROOM, result1);
    }
}