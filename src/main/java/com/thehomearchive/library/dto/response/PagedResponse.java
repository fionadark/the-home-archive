package com.thehomearchive.library.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;

import java.util.List;

/**
 * Paginated response wrapper for list-based API endpoints.
 * Provides pagination metadata along with the data.
 *
 * @param <T> Type of the list items
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PagedResponse<T> {
    
    private List<T> content;
    private PaginationMetadata pagination;
    
    // Private constructor to enforce static factory methods
    private PagedResponse() {}
    
    // Getters
    public List<T> getContent() {
        return content;
    }
    
    public PaginationMetadata getPagination() {
        return pagination;
    }
    
    /**
     * Create a paginated response from Spring Data Page.
     *
     * @param page Spring Data Page object
     * @param <T> Type of page content
     * @return PagedResponse with pagination metadata
     */
    public static <T> PagedResponse<T> of(@NonNull Page<T> page) {
        PagedResponse<T> response = new PagedResponse<>();
        response.content = page.getContent();
        response.pagination = PaginationMetadata.of(page);
        return response;
    }
    
    /**
     * Create a paginated response from list and pagination metadata.
     *
     * @param content List of items
     * @param pagination Pagination metadata
     * @param <T> Type of list items
     * @return PagedResponse with provided data
     */
    public static <T> PagedResponse<T> of(@NonNull List<T> content, @NonNull PaginationMetadata pagination) {
        PagedResponse<T> response = new PagedResponse<>();
        response.content = content;
        response.pagination = pagination;
        return response;
    }
    
    /**
     * Pagination metadata for paginated responses.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PaginationMetadata {
        
        private int page;
        private int size;
        private int totalPages;
        private long totalElements;
        private boolean first;
        private boolean last;
        private boolean hasNext;
        private boolean hasPrevious;
        
        // Private constructor
        private PaginationMetadata() {}
        
        // Getters
        public int getPage() {
            return page;
        }
        
        public int getSize() {
            return size;
        }
        
        public int getTotalPages() {
            return totalPages;
        }
        
        public long getTotalElements() {
            return totalElements;
        }
        
        public boolean isFirst() {
            return first;
        }
        
        public boolean isLast() {
            return last;
        }
        
        public boolean isHasNext() {
            return hasNext;
        }
        
        public boolean isHasPrevious() {
            return hasPrevious;
        }
        
        /**
         * Create pagination metadata from Spring Data Page.
         *
         * @param page Spring Data Page object
         * @return PaginationMetadata with page information
         */
        public static PaginationMetadata of(@NonNull Page<?> page) {
            PaginationMetadata metadata = new PaginationMetadata();
            metadata.page = page.getNumber();
            metadata.size = page.getSize();
            metadata.totalPages = page.getTotalPages();
            metadata.totalElements = page.getTotalElements();
            metadata.first = page.isFirst();
            metadata.last = page.isLast();
            metadata.hasNext = page.hasNext();
            metadata.hasPrevious = page.hasPrevious();
            return metadata;
        }
        
        /**
         * Create pagination metadata manually.
         *
         * @param page Current page number (0-based)
         * @param size Page size
         * @param totalPages Total number of pages
         * @param totalElements Total number of elements
         * @return PaginationMetadata with provided information
         */
        public static PaginationMetadata of(int page, int size, int totalPages, long totalElements) {
            PaginationMetadata metadata = new PaginationMetadata();
            metadata.page = page;
            metadata.size = size;
            metadata.totalPages = totalPages;
            metadata.totalElements = totalElements;
            metadata.first = page == 0;
            metadata.last = page >= totalPages - 1;
            metadata.hasNext = page < totalPages - 1;
            metadata.hasPrevious = page > 0;
            return metadata;
        }
    }
}