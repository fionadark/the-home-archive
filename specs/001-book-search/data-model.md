# Data Model: Book Search

**Date**: 2025-10-15  
**Feature**: Book Search for Home Library Management

## Entities

### Book Entity

**Purpose**: Represents a physical book in the user's home library with all searchable metadata.

**Fields**:
- `id` (Long, Primary Key): Unique identifier for the book
- `title` (String, Required, Max 255): Book title - primary search field
- `author` (String, Required, Max 255): Book author - primary search field  
- `genre` (String, Optional, Max 100): Book genre/category
- `isbn` (String, Optional, Max 20): International Standard Book Number
- `publisher` (String, Optional, Max 255): Publishing company
- `publicationYear` (Integer, Optional): Year of publication
- `description` (Text, Optional): Book description/summary
- `pageCount` (Integer, Optional): Number of pages
- `physicalLocation` (String, Optional, Max 100): Location in home library
- `readingStatus` (Enum, Optional): NOT_READ, READING, COMPLETED
- `personalRating` (Integer, Optional, 1-5): User's rating of the book
- `dateAdded` (LocalDateTime, Required): When book was added to library
- `createdAt` (LocalDateTime, Required): Record creation timestamp
- `updatedAt` (LocalDateTime, Required): Record last update timestamp

**Validation Rules**:
- `title` and `author` are mandatory (constitution requirement)
- `personalRating` must be between 1-5 if provided
- `isbn` must follow ISBN format if provided
- `publicationYear` must be reasonable range (e.g., 1000-current year)

**Indexes**:
- Primary key index on `id`
- Composite index on (`title`, `author`) for exact match searches
- FULLTEXT index on (`title`, `author`, `description`) for relevance searching
- Individual indexes on `genre`, `publicationYear` for filtering
- Index on `dateAdded` for default sorting

### Search Query (DTO)

**Purpose**: Represents user's search input and parameters.

**Fields**:
- `query` (String, Optional, Max 100): Search terms
- `sortBy` (Enum, Optional): RELEVANCE, TITLE, AUTHOR, DATE_ADDED
- `sortOrder` (Enum, Optional): ASC, DESC
- `limit` (Integer, Optional, Max 50, Default 50): Result limit

### Search Result (DTO)

**Purpose**: Represents search response with results and metadata.

**Fields**:
- `books` (List<BookSearchDto>): List of matching books
- `totalCount` (Integer): Total number of matches
- `searchTime` (Long): Query execution time in milliseconds
- `hasMore` (Boolean): Whether more results exist beyond limit

### Book Search DTO

**Purpose**: Represents a book in search results with display information.

**Fields**:
- `id` (Long): Book identifier
- `title` (String): Book title
- `author` (String): Book author  
- `genre` (String): Book genre
- `publicationYear` (Integer): Publication year
- `relevanceScore` (Double, Internal): Used for sorting, not exposed to client

## Relationships

### Book Entity Relationships
- No direct relationships in initial implementation
- Future extensions may include:
  - Author entity (Many-to-Many)
  - Category/Genre entity (Many-to-One)
  - Reading lists (Many-to-Many)

## State Transitions

### Reading Status Lifecycle
```
NOT_READ → READING → COMPLETED
     ↓        ↓         ↓
   [Any state can transition to any other state]
```

**Business Rules**:
- Users can change reading status freely
- Status changes do not affect search functionality
- Default status is NOT_READ when not specified

## Database Schema

### Book Table (MySQL)
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

## Search Query Patterns

### Full-Text Search Query
```sql
SELECT b.*, 
       MATCH(title, author, description) AGAINST(? IN NATURAL LANGUAGE MODE) as relevance_score,
       CASE 
           WHEN LOWER(title) = LOWER(?) THEN 3.0
           WHEN LOWER(author) = LOWER(?) THEN 2.0  
           WHEN LOWER(genre) = LOWER(?) THEN 1.5
           ELSE MATCH(title, author, description) AGAINST(? IN NATURAL LANGUAGE MODE)
       END as weighted_relevance
FROM books b 
WHERE MATCH(title, author, description) AGAINST(? IN NATURAL LANGUAGE MODE)
   OR LOWER(title) LIKE LOWER(CONCAT('%', ?, '%'))
   OR LOWER(author) LIKE LOWER(CONCAT('%', ?, '%'))
   OR LOWER(genre) LIKE LOWER(CONCAT('%', ?, '%'))
ORDER BY weighted_relevance DESC, title ASC
LIMIT 50;
```

### Empty Query (Show All Books)
```sql
SELECT * FROM books 
ORDER BY title ASC;
```

## Performance Considerations

### Index Strategy
- Use composite indexes for multi-field searches
- FULLTEXT indexes for relevance-based searching  
- Individual indexes for filtering and sorting
- Avoid over-indexing to maintain write performance

### Query Optimization
- Use LIMIT to restrict result sets
- Apply WHERE clauses early in query execution
- Use covering indexes where possible
- Monitor query execution plans for optimization