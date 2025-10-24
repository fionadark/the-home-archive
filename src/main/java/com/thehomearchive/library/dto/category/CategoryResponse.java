package com.thehomearchive.library.dto.category;

import java.time.LocalDateTime;

/**
 * Response DTO for category information.
 */
public class CategoryResponse {

    private Long id;
    private String name;
    private String description;
    private String slug;
    private LocalDateTime createdAt;

    public CategoryResponse() {}

    public CategoryResponse(Long id, String name, String description, String slug, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.slug = slug;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}