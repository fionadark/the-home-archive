<!--
Sync Impact Report:
- Version change: 1.0.0 → 1.0.1
- Modified sections: Technology Stack (Maven → Gradle)
- Added sections: None
- Removed sections: None
- Templates requiring updates: ✅ plan-template.md, ✅ tasks-template.md
- Follow-up TODOs: None
-->

# The Home Archive Constitution

## Core Principles

### I. Data Integrity (NON-NEGOTIABLE)
Book data MUST be preserved accurately and consistently. No book information shall be lost due to system failures, user errors, or data migrations. All book modifications MUST be validated before persistence. Data backup and recovery procedures are mandatory for any production deployment.

**Rationale**: Personal book collections represent significant time investment and emotional value. Data loss is unacceptable for a personal archive system.

### II. REST API Design
All data operations MUST follow RESTful conventions with clear resource endpoints. HTTP methods shall map correctly to operations (GET for retrieval, POST for creation, PUT for updates, DELETE for removal). API responses MUST include appropriate HTTP status codes and consistent JSON structure.

**Rationale**: Standard REST patterns ensure predictable behavior and enable future integrations with mobile apps, web interfaces, or third-party tools.

### III. Test-First Development (NON-NEGOTIABLE)
Unit tests MUST be written before implementation for all business logic. Integration tests are required for database operations and API endpoints. Test coverage MUST exceed 80% for service layer and repository layer code. All tests must pass before any feature is considered complete.

**Rationale**: Personal data systems require high reliability. Comprehensive testing prevents data corruption and ensures consistent behavior across all features.

### IV. Database Consistency
All database operations MUST use appropriate transaction boundaries. Foreign key relationships shall be enforced at the database level. Database migrations MUST be versioned and reversible. No direct SQL manipulation outside of defined repository patterns.

**Rationale**: Referential integrity ensures data remains coherent as the collection grows and prevents orphaned records or inconsistent states.

### V. Simplicity & Performance
Features MUST solve real user needs without unnecessary complexity. Database queries shall be optimized for collections up to 10,000 books. UI responses MUST complete within 500ms for typical operations. New dependencies require explicit justification.

**Rationale**: Personal tools should be fast and reliable. Over-engineering reduces maintainability and introduces potential failure points.

## Technology Stack

**Core Framework**: Java 17+ with Spring Boot 3.x
**Database**: PostgreSQL with Spring Data JPA
**Testing**: JUnit 5, Mockito, TestContainers for integration tests
**Build Tool**: Gradle with standard Spring Boot plugin and dependencies
**Documentation**: OpenAPI 3.0 specification for REST endpoints

Technology changes require constitutional amendment and migration plan.

## Development Standards

**Code Organization**: Standard Spring Boot structure with separate layers for controllers, services, repositories, and entities. Package by feature, not by layer.

**Error Handling**: Global exception handling with user-friendly error messages. All exceptions MUST be logged with appropriate context. API errors return standardized JSON error responses.

**Validation**: Input validation at controller level using Bean Validation annotations. Business rule validation in service layer. Database constraints for data integrity.

**Documentation**: README with setup instructions, API documentation via OpenAPI, inline code comments for complex business logic only.

## Governance

This constitution supersedes all other development practices and coding standards. All feature specifications, implementation plans, and code reviews MUST verify compliance with these principles.

Amendments require:
1. Documentation of rationale and impact assessment
2. Review of affected templates and existing code
3. Migration plan for breaking changes
4. Version increment following semantic versioning

Complexity must be explicitly justified against the Simplicity principle. Use `.specify/templates/agent-file-template.md` for runtime development guidance.

**Version**: 1.0.1 | **Ratified**: 2025-10-14 | **Last Amended**: 2025-10-14