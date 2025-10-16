# Tasks: Book Search

**Input**: Design documents from `/specs/001-book-search/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`
- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions
- **Single project**: `src/main/java/com/homearchive/`, `src/test/java/com/homearchive/` at repository root
- All paths follow Spring Boot Gradle structure as defined in plan.md

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic Spring Boot structure

- [x] T001 Create Spring Boot project structure with Java 17+ and Spring Boot 3.x dependencies
- [x] T002 Configure build.gradle with required dependencies: Spring Data JPA, Spring Web, Spring Security, MySQL Connector, H2, JUnit 5, Mockito, TestContainers
- [x] T003 [P] Setup application.yml configuration files for dev/test/prod environments in src/main/resources/
- [x] T004 [P] Create main Application.java class in src/main/java/com/homearchive/
- [x] T005 [P] Configure Flyway for database migrations in src/main/resources/db/migration/

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T006 Create V1__create_book_table.sql migration script with all book fields and indexes in src/main/resources/db/migration/
- [x] T007 Implement Book entity with JPA annotations and validation in src/main/java/com/homearchive/entity/Book.java
- [x] T008 Create ReadingStatus enum in src/main/java/com/homearchive/entity/ReadingStatus.java
- [x] T009 [P] Configure CORS settings in src/main/java/com/homearchive/config/CorsConfig.java
- [x] T010 [P] Setup basic security configuration in src/main/java/com/homearchive/config/SecurityConfig.java
- [x] T011 [P] Create base exception classes for error handling in src/main/java/com/homearchive/exception/
- [x] T012 [P] Configure Jackson for JSON serialization/deserialization
- [x] T013 Setup basic logging configuration in src/main/resources/logback-spring.xml

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Quick Book Search (Priority: P1) 🎯 MVP

**Goal**: Users can search for books by title/author with basic relevance ranking and see results instantly

**Independent Test**: Enter "harry" in search box, verify books with "Harry" in title/author appear first in scrollable list

### Implementation for User Story 1

- [x] T014 [P] [US1] Create SearchRequest DTO with query validation in src/main/java/com/homearchive/dto/SearchRequest.java
- [x] T015 [P] [US1] Create SearchResponse DTO in src/main/java/com/homearchive/dto/SearchResponse.java
- [x] T016 [P] [US1] Create BookSearchDto for result display in src/main/java/com/homearchive/dto/BookSearchDto.java
- [x] T017 [P] [US1] Create SortBy and SortOrder enums in src/main/java/com/homearchive/dto/
- [x] T018 [US1] Create BookRepository with basic search methods in src/main/java/com/homearchive/repository/BookRepository.java
- [x] T019 [US1] Implement BookSearchService with title/author search logic in src/main/java/com/homearchive/service/BookSearchService.java
- [x] T020 [US1] Create BookMapper for entity-to-DTO conversion in src/main/java/com/homearchive/mapper/BookMapper.java
- [x] T021 [US1] Implement BookSearchController with GET /api/books/search endpoint in src/main/java/com/homearchive/controller/BookSearchController.java
- [x] T022 [US1] Add input validation and 100 character limit enforcement
- [x] T023 [US1] Implement "No results found" message handling
- [x] T024 [US1] Add relevance scoring for exact title/author matches
- [x] T025 [US1] Implement alphabetical tiebreaker for equal relevance scores

**Checkpoint**: At this point, User Story 1 should be fully functional - basic search with title/author relevance ranking

---

## Phase 4: User Story 2 - Advanced Field Search (Priority: P2)

**Goal**: Extend search to cover all book metadata fields (genre, ISBN, publisher, description) with proper relevance weighting

**Independent Test**: Search for "mystery" genre, "978-" ISBN prefix, or publisher name and verify appropriate books appear with correct ranking

### Implementation for User Story 2

- [x] T026 [US2] Extend BookRepository with full-text search across all fields in src/main/java/com/homearchive/repository/BookRepository.java
- [x] T027 [US2] Update BookSearchService to search genre, ISBN, publisher, description fields in src/main/java/com/homearchive/service/BookSearchService.java
- [x] T028 [US2] Implement field-weighted relevance scoring (title=3.0, author=2.0, genre=1.5, ISBN=exact, description=1.0)
- [x] T029 [US2] Add special character stripping/preprocessing for search queries
- [x] T030 [US2] Implement ISBN exact match priority handling
- [x] T031 [US2] Add comprehensive field validation for search parameters
- [x] T032 [US2] Optimize database queries with proper indexing strategy

**Checkpoint**: Advanced field search working across all book metadata with proper relevance weighting

---

## Phase 5: User Story 3 - Multi-word and Complex Search (Priority: P3)

**Goal**: Handle sophisticated search queries with multiple terms, Boolean logic, and advanced relevance calculation

**Independent Test**: Search "stephen king horror" and verify books matching multiple terms rank higher than single-term matches

### Implementation for User Story 3

- [x] T033 [US3] Implement multi-word query parsing in BookSearchService in src/main/java/com/homearchive/service/BookSearchService.java
- [x] T034 [US3] Add Boolean AND logic for multi-term queries
- [x] T035 [US3] Implement compound relevance scoring for multi-field matches
- [x] T036 [US3] Add query term highlighting and match indication
- [x] T037 [US3] Implement result limit of 50 books with proper truncation
- [x] T038 [US3] Add search performance optimization and caching
- [x] T039 [US3] Handle edge cases for very common words and empty results

**Checkpoint**: Advanced multi-word search with sophisticated relevance ranking complete

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Finalize integration, performance optimization, and production readiness

- [x] T040 [P] Add comprehensive error handling and logging across all components
- [x] T041 [P] Implement search result caching with appropriate TTL
- [x] T042 [P] Add database performance monitoring and query optimization
- [x] T043 [P] Configure production-ready security headers and CORS policies
- [x] T044 [P] Add API documentation with OpenAPI/Swagger integration
- [x] T045 [P] Implement health checks and metrics collection
- [x] T046 [P] Add request/response logging for debugging
- [x] T047 [P] Configure environment-specific database connections (H2 for testing, MySQL for dev/prod)
- [x] T048 [P] Add physical location dropdown support with predefined room options
- [ ] T049 Perform end-to-end integration testing across all user stories
- [ ] T050 Performance testing to ensure <2 second response times for 10,000 books

---

## Dependencies & Execution Strategy

### User Story Dependencies
- **User Story 1**: Independent (depends only on Phase 1 & 2)
- **User Story 2**: Extends User Story 1 (can build incrementally)
- **User Story 3**: Extends User Story 2 (can build incrementally)

### Parallel Execution Opportunities

**Phase 1 & 2**: Most setup tasks can run in parallel
**Phase 3 (US1)**: T014-T017 (DTOs), T020 (Mapper) can run in parallel
**Phase 4 (US2)**: Database and service enhancements can be done in parallel
**Phase 6**: All polish tasks can run in parallel

### MVP Strategy
**Minimum Viable Product**: Complete through Phase 3 (User Story 1)
- Provides core book search functionality
- Delivers immediate user value
- Independently testable and deployable
- Foundation for incremental enhancement

### Implementation Strategy
1. **Foundation First**: Complete Phases 1-2 before any user story work
2. **Incremental Delivery**: Each user story builds on previous ones
3. **Independent Testing**: Each user story can be tested in isolation
4. **Parallel Development**: Within each phase, leverage parallel task opportunities

## Summary

**Total Tasks**: 50
**User Story 1 (MVP)**: 12 tasks (T014-T025)
**User Story 2**: 7 tasks (T026-T032)  
**User Story 3**: 7 tasks (T033-T039)
**Setup & Foundation**: 13 tasks (T001-T013)
**Polish & Cross-cutting**: 11 tasks (T040-T050)

**Parallel Opportunities**: 19 tasks marked with [P] can run in parallel
**Independent Test Criteria**: Each user story has clear test scenarios for validation
**Suggested MVP Scope**: Phases 1-3 (User Story 1) - provides core search functionality