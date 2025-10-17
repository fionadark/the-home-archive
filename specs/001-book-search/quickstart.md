# Quickstart Guide: Book Search Implementation

**Date**: 2025-10-15  
**Feature**: Book Search for Home Library Management  
**Branch**: `001-book-search`

## Overview

This guide provides step-by-step instructions for implementing the book search functionality in the Home Archive Spring Boot application.

## Prerequisites

- Java 17+ LTS
- Spring Boot 3.x
- MySQL 8.0+ (or H2 for testing)
- Gradle
- IDE (IntelliJ IDEA, Eclipse, VS Code)

## Implementation Steps

### 1. Database Setup

#### Create Migration Script
Create `src/main/resources/db/migration/V1__create_book_table.sql`:

```sql
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    isbn VARCHAR(20),
    publisher VARCHAR(255),
    publication_year INT,
    description TEXT,
    page_count INT,
    physical_location VARCHAR(100),
    reading_status ENUM('NOT_READ', 'READING', 'COMPLETED') DEFAULT 'NOT_READ',
    personal_rating TINYINT CHECK (personal_rating >= 1 AND personal_rating <= 5),
    date_added DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_title_author (title, author),
    FULLTEXT idx_search_fields (title, author, description),
    INDEX idx_genre (genre),
    INDEX idx_publication_year (publication_year),
    INDEX idx_date_added (date_added)
);
```

### 2. Entity Implementation

#### Book Entity
Create `src/main/java/com/homearchive/entity/Book.java`:

```java
@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @NotBlank(message = "Title is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String title;
    
    @NotBlank(message = "Author is required")
    @Size(max = 255)
    @Column(nullable = false)
    private String author;
    
    @Size(max = 100)
    private String genre;
    
    @Size(max = 20)
    private String isbn;
    
    @Size(max = 255)
    private String publisher;
    
    @Column(name = "publication_year")
    private Integer publicationYear;
    
    @Lob
    private String description;
    
    @Column(name = "page_count")
    private Integer pageCount;
    
    @Column(name = "physical_location")
    @Size(max = 100)
    private String physicalLocation;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reading_status")
    private ReadingStatus readingStatus = ReadingStatus.NOT_READ;
    
    @Column(name = "personal_rating")
    @Min(1) @Max(5)
    private Integer personalRating;
    
    @Column(name = "date_added", nullable = false)
    private LocalDateTime dateAdded = LocalDateTime.now();
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
```

### 3. DTOs Implementation

#### Search Request DTO
Create `src/main/java/com/homearchive/dto/SearchRequest.java`:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchRequest {
    @Size(max = 100, message = "Search query cannot exceed 100 characters")
    private String q;
    
    private SortBy sortBy = SortBy.RELEVANCE;
    
    private SortOrder sortOrder = SortOrder.DESC;
    
    @Min(1) @Max(50)
    private Integer limit = 50;
}
```

#### Search Response DTO
Create `src/main/java/com/homearchive/dto/SearchResponse.java`:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResponse {
    private List<BookSearchDto> books;
    private Integer totalCount;
    private Long searchTime;
    private Boolean hasMore;
}
```

#### Book Search DTO
Create `src/main/java/com/homearchive/dto/BookSearchDto.java`:

```java
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookSearchDto {
    private Long id;
    private String title;
    private String author;
    private String genre;
    private Integer publicationYear;
}
```

### 4. Repository Implementation

#### Book Repository
Create `src/main/java/com/homearchive/repository/BookRepository.java`:

```java
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    @Query(value = """
        SELECT b.*, 
               CASE 
                   WHEN LOWER(b.title) = LOWER(:query) THEN 3.0
                   WHEN LOWER(b.author) = LOWER(:query) THEN 2.0  
                   WHEN LOWER(b.genre) = LOWER(:query) THEN 1.5
                   ELSE MATCH(b.title, b.author, b.description) AGAINST(:query IN NATURAL LANGUAGE MODE)
               END as relevance_score
        FROM books b 
        WHERE MATCH(b.title, b.author, b.description) AGAINST(:query IN NATURAL LANGUAGE MODE)
           OR LOWER(b.title) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(b.author) LIKE LOWER(CONCAT('%', :query, '%'))
           OR LOWER(b.genre) LIKE LOWER(CONCAT('%', :query, '%'))
        ORDER BY relevance_score DESC, b.title ASC
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findBooksWithRelevance(@Param("query") String query, @Param("limit") int limit);
    
    @Query("SELECT b FROM Book b ORDER BY b.title ASC")
    List<Book> findAllOrderByTitle(Pageable pageable);
    
    long countByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrGenreContainingIgnoreCase(
        String title, String author, String genre);
}
```

### 5. Service Implementation

#### Book Search Service
Create `src/main/java/com/homearchive/service/BookSearchService.java`:

```java
@Service
@Transactional(readOnly = true)
@Slf4j
public class BookSearchService {
    
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    
    public BookSearchService(BookRepository bookRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.bookMapper = bookMapper;
    }
    
    public SearchResponse searchBooks(SearchRequest request) {
        long startTime = System.currentTimeMillis();
        
        try {
            List<Book> books = executeSearch(request);
            long totalCount = countTotalMatches(request);
            
            List<BookSearchDto> bookDtos = books.stream()
                .map(bookMapper::toSearchDto)
                .toList();
            
            long searchTime = System.currentTimeMillis() - startTime;
            boolean hasMore = totalCount > request.getLimit();
            
            return new SearchResponse(bookDtos, (int) totalCount, searchTime, hasMore);
            
        } catch (Exception e) {
            log.error("Error executing search: {}", e.getMessage(), e);
            throw new SearchException("Failed to execute search", e);
        }
    }
    
    private List<Book> executeSearch(SearchRequest request) {
        String query = preprocessQuery(request.getQ());
        
        if (StringUtils.isBlank(query)) {
            return bookRepository.findAllOrderByTitle(
                PageRequest.of(0, request.getLimit())
            ).getContent();
        }
        
        List<Object[]> results = bookRepository.findBooksWithRelevance(query, request.getLimit());
        return results.stream()
            .map(row -> (Book) row[0])
            .toList();
    }
    
    private String preprocessQuery(String query) {
        if (query == null) return "";
        return query.trim()
            .replaceAll("[^a-zA-Z0-9\\s]", "") // Remove special characters
            .toLowerCase();
    }
    
    private long countTotalMatches(SearchRequest request) {
        String query = preprocessQuery(request.getQ());
        if (StringUtils.isBlank(query)) {
            return bookRepository.count();
        }
        return bookRepository.countByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrGenreContainingIgnoreCase(
            query, query, query);
    }
}
```

### 6. Controller Implementation

#### Book Search Controller
Create `src/main/java/com/homearchive/controller/BookSearchController.java`:

```java
@RestController
@RequestMapping("/api/books")
@CrossOrigin(origins = {"http://localhost:3000", "https://app.homearchive.com"})
@Validated
@Slf4j
public class BookSearchController {
    
    private final BookSearchService bookSearchService;
    
    public BookSearchController(BookSearchService bookSearchService) {
        this.bookSearchService = bookSearchService;
    }
    
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> searchBooks(@Valid @ModelAttribute SearchRequest request) {
        log.info("Search request received: query='{}', sortBy={}, limit={}", 
            request.getQ(), request.getSortBy(), request.getLimit());
        
        SearchResponse response = bookSearchService.searchBooks(request);
        
        log.info("Search completed: {} results in {}ms", 
            response.getTotalCount(), response.getSearchTime());
        
        return ResponseEntity.ok(response);
    }
}
```

### 7. Configuration

#### Application Configuration
Update `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: home-archive
  
  datasource:
    url: jdbc:mysql://localhost:3306/home_archive?createDatabaseIfNotExist=true
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:password}
    driver-class-name: com.mysql.cj.jdbc.Driver
  
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: true
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true

logging:
  level:
    com.homearchive: DEBUG
    org.springframework.security: DEBUG

server:
  port: 8080
```

### 8. Testing Implementation

#### Unit Test Example
Create `src/test/java/com/homearchive/service/BookSearchServiceTest.java`:

```java
@ExtendWith(MockitoExtension.class)
class BookSearchServiceTest {
    
    @Mock
    private BookRepository bookRepository;
    
    @Mock
    private BookMapper bookMapper;
    
    @InjectMocks
    private BookSearchService bookSearchService;
    
    @Test
    void searchBooks_WithValidQuery_ReturnsResults() {
        // Given
        SearchRequest request = new SearchRequest("harry potter", SortBy.RELEVANCE, SortOrder.DESC, 10);
        Book book = createTestBook();
        when(bookRepository.findBooksWithRelevance(any(), anyInt()))
            .thenReturn(List.of(new Object[]{book, 3.0}));
        when(bookMapper.toSearchDto(book)).thenReturn(createTestBookDto());
        
        // When
        SearchResponse response = bookSearchService.searchBooks(request);
        
        // Then
        assertThat(response.getBooks()).hasSize(1);
        assertThat(response.getTotalCount()).isEqualTo(1);
        assertThat(response.getSearchTime()).isGreaterThan(0);
    }
}
```

## Testing the Implementation

### 1. Start the Application
```bash
./gradlew bootRun
```

### 2. Test Search Endpoint
```bash
# Search for books
curl "http://localhost:8080/api/books/search?q=harry&limit=10"

# Get all books (empty query)
curl "http://localhost:8080/api/books/search"

# Search with sorting
curl "http://localhost:8080/api/books/search?q=fiction&sortBy=title&sortOrder=asc"
```

### 3. Expected Response Format
```json
{
  "books": [
    {
      "id": 1,
      "title": "Harry Potter and the Philosopher's Stone",
      "author": "J.K. Rowling",
      "genre": "Fantasy",
      "publicationYear": 1997
    }
  ],
  "totalCount": 1,
  "searchTime": 45,
  "hasMore": false
}
```

## Performance Optimization

### Database Indexes
Ensure the following indexes are created:
- Composite index on (title, author)
- FULLTEXT index on (title, author, description)
- Individual indexes on genre and publication_year

### Caching (Optional)
Add caching for frequently accessed searches:
```java
@Cacheable(value = "book-search", key = "#request.q + '_' + #request.limit")
public SearchResponse searchBooks(SearchRequest request) {
    // Implementation
}
```

## Troubleshooting

### Common Issues
1. **FULLTEXT indexes not working**: Ensure MySQL version 8.0+
2. **Search too slow**: Check index usage with EXPLAIN
3. **Special characters causing issues**: Verify preprocessing logic
4. **Authentication failures**: Check JWT token configuration

### Debug Logging
Enable debug logging for search operations:
```yaml
logging:
  level:
    com.homearchive.service.BookSearchService: DEBUG
```

## Next Steps

1. Implement book CRUD operations
2. Add user authentication and authorization  
3. Create frontend search interface
4. Add search analytics and monitoring
5. Implement search result caching
6. Add more advanced search filters