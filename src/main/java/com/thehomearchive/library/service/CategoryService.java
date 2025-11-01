package com.thehomearchive.library.service;

import com.thehomearchive.library.dto.category.CategoryCreateRequest;
import com.thehomearchive.library.dto.category.CategoryResponse;
import com.thehomearchive.library.entity.Category;
import com.thehomearchive.library.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for managing categories.
 */
@Service
@Transactional
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * Get all categories as response DTOs.
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get all categories as Map objects (for backward compatibility with tests).
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getAllCategoriesAsMap() {
        List<Category> categories = categoryRepository.findAll();
        return categories.stream()
                .map(this::convertToMap)
                .collect(Collectors.toList());
    }

    /**
     * Create a new category from DTO.
     */
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        // Check if category already exists
        if (categoryRepository.existsByNameIgnoreCase(request.getName())) {
            throw new IllegalArgumentException("Category with name '" + request.getName() + "' already exists");
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setSlug(generateSlug(request.getName()));

        Category saved = categoryRepository.save(category);
        return convertToResponse(saved);
    }

    /**
     * Create a new category from Map (for backward compatibility with tests).
     */
    public Map<String, Object> createCategory(Map<String, Object> request) {
        String name = (String) request.get("name");
        String description = (String) request.get("description");

        // Check if category already exists
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Category with name '" + name + "' already exists");
        }

        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setSlug(generateSlug(name));

        Category saved = categoryRepository.save(category);
        return convertToMap(saved);
    }

    /**
     * Get category by ID.
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with ID: " + id));
        return convertToResponse(category);
    }

    /**
     * Get category by slug.
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new IllegalArgumentException("Category not found with slug: " + slug));
        return convertToResponse(category);
    }

    /**
     * Check if category exists.
     */
    @Transactional(readOnly = true)
    public boolean categoryExists(Long id) {
        return categoryRepository.existsById(id);
    }

    /**
     * Generate slug from category name.
     */
    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Convert Category entity to response DTO.
     */
    private CategoryResponse convertToResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setSlug(category.getSlug());
        response.setCreatedAt(category.getCreatedAt());
        return response;
    }

    /**
     * Convert Category entity to Map (for backward compatibility).
     */
    private Map<String, Object> convertToMap(Category category) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", category.getId());
        map.put("name", category.getName());
        map.put("description", category.getDescription());
        map.put("slug", category.getSlug());
        map.put("createdAt", category.getCreatedAt());
        return map;
    }
}