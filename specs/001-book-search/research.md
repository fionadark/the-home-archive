# Research: Book Search Implementation

**Date**: 2025-10-15  
**Feature**: Book Search for Home Library Management

## Technology Decisions

### Decision: Full-Text Search Implementation
**Rationale**: MySQL 8.0+ provides native full-text search capabilities with FULLTEXT indexes, eliminating need for external search engines like Elasticsearch for this scale (up to 10,000 books). Native approach reduces complexity and operational overhead.

**Alternatives considered**:
- Elasticsearch: Overkill for personal library scale, adds infrastructure complexity
- Apache Lucene: Requires additional dependencies and index management
- Simple LIKE queries: Poor performance and limited relevance scoring capabilities

### Decision: JPA Criteria API for Dynamic Queries
**Rationale**: JPA Criteria API provides type-safe query construction for dynamic search parameters while preventing SQL injection. Supports complex relevance scoring and field-specific weighting required by specification.

**Alternatives considered**:
- Native SQL queries: Less type-safe, harder to maintain dynamic query building
- Spring Data JPA @Query: Limited flexibility for complex dynamic search logic
- QueryDSL: Additional dependency overhead for this feature scope

### Decision: Spring Security with JWT for API Authentication
**Rationale**: Stateless JWT tokens align with RESTful API design and support web/mobile clients as required by constitution. Spring Security provides comprehensive security features with minimal configuration.

**Alternatives considered**:
- Session-based authentication: Not suitable for RESTful APIs or mobile clients
- Basic authentication: Insecure for production use
- OAuth2: Overkill for personal home library application

## Best Practices Research

### Spring Boot Search Patterns
**Key findings**:
- Use @RestController with @RequestParam for search parameters
- Implement custom repository methods for complex search logic
- Use @Transactional(readOnly = true) for search operations
- Apply @Valid for input validation on search DTOs

### Database Indexing Strategy
**Key findings**:
- Composite indexes on (title, author) for exact match optimization
- FULLTEXT indexes on searchable text fields (title, author, description)
- Individual indexes on frequently filtered fields (genre, publication_year)
- B-tree indexes for range queries and sorting operations

### Relevance Scoring Implementation
**Key findings**:
- Use MySQL MATCH() AGAINST() for full-text relevance scores
- Implement field weighting: title (weight 3.0) > author (weight 2.0) > genre (weight 1.5) > description (weight 1.0)
- Apply CASE WHEN clauses for exact match bonuses
- Use ORDER BY relevance_score DESC, title ASC for consistent sorting

### Performance Optimization
**Key findings**:
- Implement result caching with @Cacheable for common searches
- Use database connection pooling (HikariCP) with appropriate pool sizing
- Limit result sets to 50 items maximum as specified
- Apply search query preprocessing (trim, lowercase, special character removal)

## Integration Patterns

### Controller Layer Pattern
```java
@RestController
@RequestMapping("/api/books")
public class BookSearchController {
    
    @GetMapping("/search")
    public ResponseEntity<SearchResponse> searchBooks(
        @Valid @ModelAttribute SearchRequest request) {
        // Input validation and service delegation
    }
}
```

### Service Layer Pattern
```java
@Service
@Transactional(readOnly = true)
public class BookSearchService {
    
    public SearchResponse searchBooks(SearchRequest request) {
        // Business logic for search processing
        // Relevance scoring and result ranking
        // Result limiting and formatting
    }
}
```

### Repository Layer Pattern
```java
@Repository
public interface BookRepository extends JpaRepository<Book, Long> {
    
    @Query("SELECT b, " +
           "CASE WHEN LOWER(b.title) = LOWER(:query) THEN 3.0 " +
           "     WHEN LOWER(b.author) = LOWER(:query) THEN 2.0 " +
           "     ELSE 1.0 END as relevance " +
           "FROM Book b WHERE ...")
    List<Object[]> findBooksWithRelevance(@Param("query") String query);
}
```

## Security Considerations

### Input Validation
- Maximum 100 character search input length validation
- Special character stripping to prevent injection attacks
- Parameter sanitization for all search fields

### SQL Injection Prevention
- Use JPA parameterized queries exclusively
- Avoid dynamic SQL string concatenation
- Apply input validation at controller level

### API Security
- JWT token validation for all search endpoints
- Rate limiting to prevent abuse
- CORS configuration for web/mobile client access

## Performance Targets

### Response Time Goals
- Search queries: <2 seconds for 10,000 books
- Database queries: <200ms for indexed searches
- API endpoints: <200ms response time

### Scalability Considerations
- Database connection pooling for concurrent requests
- Query result caching for frequently accessed searches
- Efficient indexing strategy for search performance
- Result pagination to limit memory usage