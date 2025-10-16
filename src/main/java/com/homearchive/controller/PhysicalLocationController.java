package com.homearchive.controller;

import com.homearchive.entity.PhysicalLocation;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST controller for physical location management.
 * Provides endpoints for retrieving available physical locations for books.
 */
@RestController
@RequestMapping("/api/v1/locations")
@Tag(name = "Physical Locations", description = "API for managing physical locations where books are stored")
public class PhysicalLocationController {

    /**
     * Get all available physical locations for dropdown selection.
     * 
     * @return List of all available physical locations with display names
     */
    @GetMapping
    @Operation(
        summary = "Get all physical locations",
        description = "Retrieves all available physical locations that can be assigned to books. " +
                     "Returns both enum values and display names for frontend dropdown usage."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved physical locations"
        )
    })
    public ResponseEntity<List<Map<String, String>>> getAllPhysicalLocations() {
        List<Map<String, String>> locations = Arrays.stream(PhysicalLocation.values())
            .map(location -> {
                Map<String, String> locationMap = new LinkedHashMap<>();
                locationMap.put("value", location.name());
                locationMap.put("label", location.getDisplayName());
                return locationMap;
            })
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(locations);
    }

    /**
     * Get physical locations formatted for simple dropdown (value/label pairs).
     * 
     * @return Map of enum values to display names
     */
    @GetMapping("/simple")
    @Operation(
        summary = "Get physical locations as simple key-value pairs",
        description = "Retrieves all available physical locations as a simple map of enum values to display names. " +
                     "Useful for basic dropdown implementations."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Successfully retrieved physical locations map"
        )
    })
    public ResponseEntity<Map<String, String>> getPhysicalLocationsMap() {
        Map<String, String> locations = Arrays.stream(PhysicalLocation.values())
            .collect(Collectors.toMap(
                PhysicalLocation::name,
                PhysicalLocation::getDisplayName,
                (existing, replacement) -> existing, // Handle duplicates (shouldn't happen)
                LinkedHashMap::new // Preserve order
            ));
        
        return ResponseEntity.ok(locations);
    }
}