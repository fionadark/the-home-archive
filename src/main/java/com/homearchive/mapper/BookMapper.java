package com.homearchive.mapper;

import com.homearchive.dto.BookSearchDto;
import com.homearchive.entity.Book;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Book entity and BookSearchDto.
 */
@Component
public class BookMapper {
    
    /**
     * Convert a Book entity to BookSearchDto.
     */
    public BookSearchDto toSearchDto(Book book) {
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
        
        return dto;
    }
    
    /**
     * Convert a list of Book entities to a list of BookSearchDto.
     */
    public List<BookSearchDto> toSearchDtoList(List<Book> books) {
        if (books == null) {
            return null;
        }
        
        return books.stream()
                .map(this::toSearchDto)
                .collect(Collectors.toList());
    }
}