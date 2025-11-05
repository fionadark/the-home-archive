# Data Model: Dark Academia Library Web Application

**Date**: 2025-01-27 | **Feature**: 002-web-application-this

## Entity Relationship Overview

```
User ||--o{ UserSession : has
User ||--o{ BookRating : creates
User ||--o{ SearchHistory : has
Book ||--o{ BookRating : receives
Book }o--|| Category : belongs_to
User ||--o{ EmailVerification : has
```

## Core Entities

### User Entity

**Purpose**: Represents registered users with authentication and profile information

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String email;
    
    @Column(nullable = false)
    private String passwordHash;
    
    @Column(nullable = false)
    private String firstName;
    
    @Column(nullable = false)
    private String lastName;
    
    @Enumerated(EnumType.STRING)
    private UserRole role; // USER, ADMIN
    
    @Column(nullable = false)
    private boolean emailVerified = false;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime lastLoginAt;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<UserSession> sessions;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<BookRating> ratings;
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<SearchHistory> searchHistory;
}
```

**Validation Rules**:
- Email must be unique and valid format
- Password must meet security requirements (handled via Spring Security)
- Names must not be empty
- Email verification required before full access

**State Transitions**:
- UNVERIFIED → VERIFIED (via email verification)
- VERIFIED → SUSPENDED (admin action)
- Any state → DELETED (soft delete)

### Book Entity

**Purpose**: Represents books in the library catalog with metadata and ratings

```java
@Entity
@Table(name = "books")
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false)
    private String author;
    
    @Column(unique = true)
    private String isbn;
    
    @Lob
    private String description;
    
    @Column
    private Integer publicationYear;
    
    @Column
    private String publisher;
    
    @Column
    private Integer pageCount;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    
    @Column
    private String coverImageUrl;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
    
    @OneToMany(mappedBy = "book", cascade = CascadeType.ALL)
    private List<BookRating> ratings;
    
    // Calculated field
    @Transient
    private Double averageRating;
    
    @Transient
    private Integer ratingCount;
}
```

**Validation Rules**:
- Title and author are required
- ISBN must be unique if provided
- Publication year must be reasonable (1000-current year+1)
- Page count must be positive if provided

### Category Entity

**Purpose**: Organizes books into browsable categories

```java
@Entity
@Table(name = "categories")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String name;
    
    @Column
    private String description;
    
    @Column(nullable = false)
    private String slug; // URL-friendly version
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @OneToMany(mappedBy = "category")
    private List<Book> books;
}
```

**Validation Rules**:
- Category name must be unique
- Slug must be URL-safe and unique
- Name cannot be empty

### BookRating Entity

**Purpose**: User ratings and reviews for books

```java
@Entity
@Table(name = "book_ratings")
public class BookRating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(nullable = false)
    @JoinColumn(name = "user_id")
    private User user;
    
    @ManyToOne(nullable = false)
    @JoinColumn(name = "book_id")
    private Book book;
    
    @Column(nullable = false)
    @Min(1) @Max(5)
    private Integer rating;
    
    @Lob
    private String review;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime updatedAt;
}
```

**Validation Rules**:
- Rating must be between 1-5
- One rating per user per book (unique constraint)
- Review is optional but limited to reasonable length

### UserSession Entity

**Purpose**: Manages JWT refresh tokens and "Remember Me" sessions

```java
@Entity
@Table(name = "user_sessions")
public class UserSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(nullable = false)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false, unique = true)
    private String refreshToken;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private boolean rememberMe = false;
    
    @Column
    private String userAgent;
    
    @Column
    private String ipAddress;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime lastUsedAt;
}
```

**Validation Rules**:
- Refresh token must be unique and secure
- Expiration date must be in the future
- User agent and IP are for security tracking

### EmailVerification Entity

**Purpose**: Manages email verification tokens for new users and email changes

```java
@Entity
@Table(name = "email_verifications")
public class EmailVerification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(nullable = false)
    @JoinColumn(name = "user_id")
    private User user;
    
    @Column(nullable = false, unique = true)
    private String token;
    
    @Column(nullable = false)
    private String email; // Email being verified
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column
    private LocalDateTime verifiedAt;
    
    @Enumerated(EnumType.STRING)
    private VerificationType type; // REGISTRATION, EMAIL_CHANGE
}
```

**Validation Rules**:
- Token must be unique and cryptographically secure
- Email must be valid format
- Expiration typically 24 hours from creation
- Token can only be used once

### SearchHistory Entity

**Purpose**: Tracks user search queries for analytics and personalization

```java
@Entity
@Table(name = "search_history")
public class SearchHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // nullable for anonymous searches
    
    @Column(nullable = false)
    private String query;
    
    @Column
    private Integer resultCount;
    
    @Column
    private String sessionId; // For anonymous tracking
    
    @Column(nullable = false)
    private LocalDateTime searchedAt;
}
```

**Validation Rules**:
- Query cannot be empty
- Either user or sessionId must be provided
- Result count must be non-negative

## Enumerations

### UserRole
```java
public enum UserRole {
    USER,     // Regular library user
    ADMIN     // Library administrator
}
```

### VerificationType
```java
public enum VerificationType {
    REGISTRATION,   // Initial email verification
    EMAIL_CHANGE    // Verifying new email address
}
```

## Database Indexes

**Performance-critical indexes**:
- `users(email)` - Unique index for login
- `books(title, author)` - Composite index for search
- `books(category_id)` - Foreign key index
- `book_ratings(user_id, book_id)` - Unique composite for ratings
- `user_sessions(refresh_token)` - Unique index for token lookup
- `email_verifications(token)` - Unique index for verification
- `search_history(user_id, searched_at)` - Index for user search history

## Data Migration Strategy

**Development → Production**:
1. Use Flyway for versioned schema migrations
2. Seed data script for initial categories
3. Sample books for development environment
4. Production deployment with empty user tables

**Schema Versioning**:
- V1: Core user and book entities
- V2: Add rating system
- V3: Add search history tracking
- V4: Add session management tables