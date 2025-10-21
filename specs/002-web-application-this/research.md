# Research: Dark Academia Library Web Application

**Date**: 2025-01-27 | **Feature**: 002-web-application-this

## Research Tasks

Based on the Technical Context unknowns identified in plan.md:

### Task 1: Production Database Choice
**Unknown**: Production database to replace H2 development database
**Research Focus**: Enterprise-grade database selection for Spring Boot library application

### Task 2: Frontend Testing Framework
**Unknown**: Testing framework for HTML/CSS/JavaScript frontend  
**Research Focus**: Modern JavaScript testing solutions compatible with our tech stack

### Task 3: Performance Requirements
**Unknown**: Performance goals for concurrent users, response times, throughput
**Research Focus**: Typical performance benchmarks for library/catalog web applications

### Task 4: Scale and Scope Clarification
**Unknown**: Expected user base, book catalog size, concurrent sessions
**Research Focus**: Sizing requirements for library management systems

## Research Findings

### Production Database Decision

**Decision**: PostgreSQL 15+

**Rationale**: 
- Excellent Spring Boot integration with Spring Data JPA
- Robust ACID compliance for user authentication and book catalog integrity
- Advanced full-text search capabilities for book search functionality
- Mature ecosystem with extensive documentation
- Cost-effective for small to medium deployments
- JSON support for flexible book metadata storage

**Alternatives Considered**:
- **MySQL**: Good performance but less advanced search capabilities
- **MongoDB**: NoSQL flexibility but loses ACID guarantees for user data
- **H2 (production)**: Embedded option but limited scalability and features

### Frontend Testing Framework Decision

**Decision**: Jest + Testing Library

**Rationale**:
- Industry standard for JavaScript testing with extensive documentation
- DOM Testing Library provides excellent integration for HTML/CSS testing
- Snapshot testing capabilities for UI regression testing
- Mocking capabilities for API service testing
- Compatible with our vanilla JavaScript approach (no framework lock-in)

**Alternatives Considered**:
- **Cypress**: Excellent for E2E but overkill for unit/integration testing
- **Mocha/Chai**: Flexible but requires more configuration setup
- **Playwright**: Modern E2E solution but focused on browser automation

### Performance Requirements Decision

**Decision**: Target 500 concurrent users, <300ms API response time, 10k books catalog

**Rationale**:
- Realistic scope for library management system
- Achievable with Spring Boot + PostgreSQL on modest hardware
- Allows for growth while maintaining responsive user experience
- Aligns with typical library patron usage patterns
- Reasonable for initial MVP deployment

**Alternatives Considered**:
- **1000+ users**: Would require more complex caching and scaling architecture
- **<100ms responses**: Aggressive target requiring extensive optimization
- **100k+ books**: Would necessitate advanced search indexing and pagination

### Scale and Scope Decision

**Decision**: 100-1000 registered users, 10,000 book records, 50 concurrent sessions

**Rationale**:
- Appropriate for community library or small academic institution
- Manageable data size for PostgreSQL without partitioning
- Realistic concurrent usage (5-10% of registered users active)
- Allows for organic growth without architectural changes
- Sufficient complexity to demonstrate full feature set

**Alternatives Considered**:
- **Enterprise scale (10k+ users)**: Requires microservices architecture
- **Personal library (10-50 users)**: Too simple for demonstration purposes
- **Public library system (50k+ books)**: Needs advanced catalog management features

## Technology Integration Research

### Spring Boot 3.x Best Practices

**Decision**: Spring Boot 3.2+ with Spring Security 6.x, Spring Data JPA 3.x

**Rationale**:
- Latest stable release with Java 21 optimizations
- Enhanced security features with OAuth2/JWT improvements
- Better performance with native compilation support
- Comprehensive auto-configuration for rapid development

### Email Verification Best Practices  

**Decision**: Spring Boot Mail with JavaMail, token-based verification

**Rationale**:
- Built-in Spring Boot integration
- Secure token generation with expiration
- Template-based email formatting
- SMTP configuration flexibility for different providers

### JWT Authentication Patterns

**Decision**: Access token (15min) + Refresh token (7 days) with "Remember Me" option (30 days)

**Rationale**:
- Balances security with user experience
- Follows OAuth2/JWT security best practices
- Remember Me extends refresh token lifetime
- Shorter access tokens limit exposure window

### Dark Academia Theme Implementation

**Decision**: CSS Custom Properties (variables) with component-based architecture

**Rationale**:
- Modern CSS approach with excellent browser support
- Easy theme customization and maintenance
- Supports both card grid and list view layouts
- Enables future theme variations

## Updated Technical Context

**Language/Version**: Java 21+ LTS (backend), JavaScript ES6+ (frontend), HTML5, CSS3  
**Primary Dependencies**: Spring Boot 3.2+, Spring Data JPA, Spring Web, Spring Security 6.x, PostgreSQL 15+  
**Storage**: PostgreSQL 15+ (production), H2 Database (development/testing)  
**Testing**: JUnit 5, Spring Boot Test, Jest + Testing Library (frontend)  
**Target Platform**: Web browsers (modern), JVM-compatible server environment  
**Project Type**: Web application (backend + frontend)  
**Performance Goals**: 500 concurrent users, <300ms API response, 10k book catalog  
**Constraints**: Email verification required, JWT authentication, responsive design, dark academia theme  
**Scale/Scope**: 100-1000 registered users, 10,000 book records, 50 concurrent sessions