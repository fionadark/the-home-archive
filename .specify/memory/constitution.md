# The Home Archive Constitution

## Core Principles

### I. Service-Oriented Architecture
Every feature must be implemented as a service layer with clear boundaries. Services must be loosely coupled, independently testable, and follow single responsibility principle. Database operations, external API calls, and business logic must be separated into distinct service layers.

### II. RESTful API Design
All endpoints must follow REST conventions with proper HTTP methods (GET, POST, PUT, DELETE). Response formats must be consistent JSON with standardized error handling. API versioning required for breaking changes. OpenAPI/Swagger documentation mandatory for all endpoints.

### III. Test-First Development (NON-NEGOTIABLE)
TDD mandatory: Unit tests → Integration tests → User acceptance → Implementation. Test coverage minimum 80% for service layers, 90% for critical business logic. Mock external dependencies in unit tests, use TestContainers for integration testing.

### IV. Security by Design
Authentication and authorization required for all endpoints except health checks. Input validation mandatory at controller level. SQL injection prevention through JPA/prepared statements only. Sensitive data encryption at rest and in transit. Security headers and CORS properly configured.

### V. Book Archive Domain Standards
Book objects must contain minimum required fields: title (String, required) and author (String, required). Optional standard fields include: ISBN, publication year, genre, publisher, page count, description, reading status, date added, personal rating, and physical location in home. All book operations must validate required fields and provide meaningful error messages for invalid data.

## Technology Stack Requirements

### Backend Framework
- Spring Boot 3.x with Java 17+ LTS
- Spring Data JPA for database operations
- Spring Security for authentication/authorization
- Spring Web for REST API development
- Gradle for dependency management

### Database Standards
- Amazon RDS MySQL 8.0+ as target production database
- Local development supports both MySQL and in-memory H2 for testing
- Data mockable through local data structures (ArrayList/HashMap) during development
- Flyway for database migrations with version control
- JPA entities with proper validation annotations for book domain
- Database connection pooling (HikariCP) with environment-specific configurations
- Book repository pattern with CRUD operations and custom queries (search by title, author, genre)

### External Integration
- Book information enrichment APIs (Google Books API, OpenLibrary API) for metadata lookup
- OpenFeign or WebClient for REST API consumption with proper error handling
- Jackson for JSON serialization/deserialization
- Resilience4j for circuit breaker and retry patterns on external book APIs
- Configuration properties for API endpoints and credentials
- Graceful degradation when external APIs are unavailable

### Quality & Monitoring
- SLF4J with Logback for structured logging
- Micrometer for metrics collection
- Spring Boot Actuator for health checks
- JUnit 5 + Mockito for testing
- TestContainers for integration testing

## Development Workflow & Quality Gates

### Code Organization
- Package structure: controller → service → repository → entity (focused on book domain)
- Book DTOs for API requests/responses, separate from JPA entities
- Configuration classes for database profiles (local/RDS) and external book APIs
- Utility classes for book validation, ISBN formatting, and search operations
- Mock data service for development when database is unavailable

### Development Process
1. Feature branch from main with descriptive name
2. Write failing tests for new functionality
3. Implement minimal code to pass tests
4. Refactor while maintaining green tests
5. Integration tests for external service interactions
6. Code review with security and performance focus
7. Automated testing pipeline before merge

### Quality Standards
- No direct database queries in controllers - all data access through service layer
- Book validation must occur at service layer (title and author required)
- All external book API calls must have timeout and error handling
- Logging at appropriate levels (INFO for book operations, DEBUG for troubleshooting)
- Environment-specific configurations (local mock data vs RDS connection)
- Proper exception handling with user-friendly error messages for book operations
- API documentation updated with book schema changes

## Governance

Constitution supersedes all coding practices and architectural decisions. All pull requests must demonstrate compliance with these principles. Any deviation requires user acceptance.

### Compliance Requirements
- Code reviews must verify adherence to service layer separation
- Security review required for authentication/authorization changes
- Performance testing required for database and external API changes
- Integration tests must pass before merge to main branch
- API changes require documentation updates

### Amendment Process
- Constitution changes require user acceptance
- Breaking changes require migration plan and backward compatibility strategy
- Use architecture decision records (ADRs) for significant technical choices

**Version**: 1.0.0 | **Ratified**: 2025-10-15 | **Last Amended**: 2025-10-15