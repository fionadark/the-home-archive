package com.homearchive.mapper;

import com.homearchive.dto.BookSearchDto;
import com.homearchive.entity.Book;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper class for converting between Book entities and DTOs.
 */
@Component
public class BookMapper {
    
    /**
     * Convert a Book entity to BookSearchDto.
     */
    public BookSearchDto toSearchDto(Book book) {
        return toSearchDto(book, null);
    }
    
    /**
     * Convert a Book entity to BookSearchDto with match indication.
     */
    public BookSearchDto toSearchDto(Book book, String searchQuery) {
        if (book == null) {
            return null;
        }
        
        BookSearchDto dto = new BookSearchDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setGenre(book.getGenre());
        dto.setPublicationYear(book.getPublicationYear());
        dto.setIsbn(book.getIsbn());
        dto.setPublisher(book.getPublisher());
        dto.setPhysicalLocation(book.getPhysicalLocation());
        dto.setReadingStatus(book.getReadingStatus());
        dto.setPersonalRating(book.getPersonalRating());
        
        // Add match indication if search query is provided
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            addMatchIndication(dto, book, searchQuery);
        }
        
        return dto;
    }
    
    /**
     * Convert a list of Book entities to a list of BookSearchDto.
     */
    public List<BookSearchDto> toSearchDtoList(List<Book> books) {
        return toSearchDtoList(books, null);
    }
    
    /**
     * Convert a list of Book entities to a list of BookSearchDto with match indication.
     */
    public List<BookSearchDto> toSearchDtoList(List<Book> books, String searchQuery) {
        if (books == null) {
            return null;
        }
        
        return books.stream()
                .map(book -> toSearchDto(book, searchQuery))
                .collect(Collectors.toList());
    }
    
    /**
     * Add match indication to the DTO based on search query.
     */
    private void addMatchIndication(BookSearchDto dto, Book book, String searchQuery) {
        List<String> matchedFields = new ArrayList<>();
        List<String> matchedTerms = new ArrayList<>();
        
        String[] searchTerms = searchQuery.toLowerCase().split("\\s+");
        
        for (String term : searchTerms) {
            if (term.length() < 2) continue; // Skip very short terms
            
            if (book.getTitle() != null && book.getTitle().toLowerCase().contains(term)) {
                if (!matchedFields.contains("title")) matchedFields.add("title");
                if (!matchedTerms.contains(term)) matchedTerms.add(term);
            }
            if (book.getAuthor() != null && book.getAuthor().toLowerCase().contains(term)) {
                if (!matchedFields.contains("author")) matchedFields.add("author");
                if (!matchedTerms.contains(term)) matchedTerms.add(term);
            }
            if (book.getGenre() != null && book.getGenre().toLowerCase().contains(term)) {
                if (!matchedFields.contains("genre")) matchedFields.add("genre");
                if (!matchedTerms.contains(term)) matchedTerms.add(term);
            }
            if (book.getIsbn() != null && book.getIsbn().toLowerCase().contains(term)) {
                if (!matchedFields.contains("isbn")) matchedFields.add("isbn");
                if (!matchedTerms.contains(term)) matchedTerms.add(term);
            }
            if (book.getPublisher() != null && book.getPublisher().toLowerCase().contains(term)) {
                if (!matchedFields.contains("publisher")) matchedFields.add("publisher");
                if (!matchedTerms.contains(term)) matchedTerms.add(term);
            }
            if (book.getDescription() != null && book.getDescription().toLowerCase().contains(term)) {
                if (!matchedFields.contains("description")) matchedFields.add("description");
                if (!matchedTerms.contains(term)) matchedTerms.add(term);
            }
        }
        
        dto.setMatchedFields(matchedFields);
        dto.setMatchedTerms(matchedTerms);
    }
}