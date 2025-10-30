# Tasks: Dark Academia Library Web Application

**Input**: Design documents from `/specs/002-web-application-this/`
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: TDD approach required by constitution - tests included for all user stories

**Organization**: Tasks are grouped by user story to enable independent implementation and testing of each story.

## Format: `[ID] [P?] [Story] Description`
- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3)
- Include exact file paths in descriptions

## Path Conventions
- **Web app**: `src/`, `frontend/src/`
- Backend follows Spring Boot Gradle structure: `src/main/java/com/thehomearchive/library/`
- Frontend structure: `frontend/src/js/`, `frontend/src/css/`, `frontend/src/html/`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [x] T001 Create backend directory structure per implementation plan at backend/src/main/java/com/thehomearchive/library/
- [x] T002 Initialize Spring Boot 3.2+ project with Gradle dependencies in backend/build.gradle
- [x] T003 [P] Create frontend directory structure at frontend/src/
- [x] T004 [P] Configure application properties for H2 development database in backend/src/main/resources/application-dev.yml
- [x] T005 [P] Configure application properties for MySQL production in backend/src/main/resources/application-prod.yml
- [x] T006 [P] Setup Jest testing framework configuration in frontend/package.json
- [x] T007 [P] Create dark academia CSS variables and base theme in frontend/src/css/themes/dark-academia.css

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [x] T008 Create database schema with Flyway migrations in src/main/resources/db/migration/V001__Initial_Schema.sql
- [x] T009 [P] Implement JWT authentication configuration in src/main/java/com/thehomearchive/library/config/JwtConfig.java
- [x] T010 [P] Configure Spring Security with JWT in src/main/java/com/thehomearchive/library/config/SecurityConfig.java
- [x] T011 [P] Create global exception handler in src/main/java/com/thehomearchive/library/config/GlobalExceptionHandler.java
- [x] T012 [P] Setup CORS configuration (implemented in SecurityConfig.java with comprehensive CORS support)
- [x] T013 Create base repository interface in src/main/java/com/thehomearchive/library/repository/BaseRepository.java
- [x] T014 [P] Implement email service configuration (configured in application-prod.yml with SMTP settings)
- [x] T015 [P] Create API response wrapper DTOs in src/main/java/com/thehomearchive/library/dto/response/ (ApiResponse, PagedResponse, AuthResponse)
- [x] T016 [P] Setup application main class and configuration in src/main/java/com/thehomearchive/library/LibraryApplication.java
- [x] T017 [P] Configure external book API clients (Google Books, OpenLibrary) in GoogleBooksConfig.java and OpenLibraryConfig.java
- [x] T018 [P] Setup Resilience4j circuit breaker for external APIs in src/main/java/com/thehomearchive/library/config/ResilienceConfig.java
- [x] T019 [P] Configure Amazon RDS MySQL connection pooling in src/main/resources/application-prod.yml

**✅ CHECKPOINT COMPLETE**: Foundation ready - user story implementation can now begin in parallel

**Verification Status**: All Phase 2 tasks verified working ✅
- Database schema with Flyway migrations ✅
- JWT authentication & Spring Security ✅  
- Global exception handling ✅
- CORS configuration ✅
- Base repository interface ✅
- Email service configuration ✅
- API response DTOs ✅
- Enhanced application configuration ✅
- External API integrations (Google Books, OpenLibrary) ✅
- Resilience4j circuit breakers ✅
- MySQL connection pooling ✅

**Build Status**: ✅ Successful compilation and Spring context loading
**Constitution Compliance**: ✅ All requirements met
**Ready for**: Phase 3 - User Story implementation can proceed

---

## Phase 3: User Story 1 - Secure Personal Library Access (Priority: P1) 🎯 MVP

**Goal**: Users can register, verify email, login securely, and access their personal library dashboard

**Independent Test**: Can create account, verify email, login, see personal dashboard, logout, and verify other users cannot access the library

### Tests for User Story 1 (TDD Required)

**NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [x] T020 [P] [US1] Contract test for user registration endpoint in backend/src/test/java/com/thehomearchive/library/controller/AuthControllerTest.java
- [x] T021 [P] [US1] Contract test for login endpoint in backend/src/test/java/com/thehomearchive/library/controller/AuthControllerTest.java
- [x] T022 [P] [US1] Contract test for email verification endpoint in backend/src/test/java/com/thehomearchive/library/controller/AuthControllerTest.java
- [x] T023 [P] [US1] Integration test for authentication flow in backend/src/test/java/com/thehomearchive/library/integration/AuthenticationIntegrationTest.java
- [x] T024 [P] [US1] Security test for unauthorized access protection in backend/src/test/java/com/thehomearchive/library/security/SecurityTest.java
- [ ] T025 [P] [US1] Frontend test for login form in frontend/tests/pages/LoginPage.test.js

### Implementation for User Story 1

- [x] T026 [P] [US1] Create User entity in src/main/java/com/thehomearchive/library/entity/User.java
- [x] T027 [P] [US1] Create EmailVerification entity in src/main/java/com/thehomearchive/library/entity/EmailVerification.java  
- [x] T028: Create UserSession entity for JWT session management and tracking
- [x] T029 [P] [US1] Create UserRole enum in src/main/java/com/thehomearchive/library/entity/UserRole.java
- [x] T030 [P] [US1] Create VerificationType enum in src/main/java/com/thehomearchive/library/entity/VerificationType.java
- [x] T031 [P] [US1] Create authentication DTOs in src/main/java/com/thehomearchive/library/dto/auth/
- [x] T029 [US1] Create UserRepository in backend/src/main/java/com/thehomearchive/library/repository/UserRepository.java (depends on T023)
- [x] T030 [US1] Create EmailVerificationRepository in backend/src/main/java/com/thehomearchive/library/repository/EmailVerificationRepository.java (depends on T024)
- [x] T031 [US1] Create UserSessionRepository in backend/src/main/java/com/thehomearchive/library/repository/UserSessionRepository.java (depends on T025)
- [x] T032 [US1] Implement UserService with registration logic in backend/src/main/java/com/thehomearchive/library/service/UserService.java
- [x] T033 [US1] Implement AuthenticationService with login/logout in backend/src/main/java/com/thehomearchive/library/service/AuthenticationService.java
- [x] T034 [US1] Implement EmailService for verification emails in backend/src/main/java/com/thehomearchive/library/service/EmailService.java
- [x] T035 [US1] Implement JwtService for token management in backend/src/main/java/com/thehomearchive/library/service/JwtService.java
- [x] T036 [US1] Create AuthController with registration/login endpoints in backend/src/main/java/com/thehomearchive/library/controller/AuthController.java
- [x] T037 [P] [US1] Create dark academia login page HTML in frontend/src/html/login.html
- [x] T038 [P] [US1] Create registration page HTML in frontend/src/html/register.html
- [x] T039 [P] [US1] Create email verification page HTML in frontend/src/html/verify-email.html
- [x] T040 [P] [US1] Create personal dashboard page HTML in frontend/src/html/dashboard.html
- [x] T041 [P] [US1] Style login page with dark academia theme in frontend/src/css/pages/login.css
- [x] T042 [P] [US1] Style registration page in frontend/src/css/pages/register.css
- [x] T043 [P] [US1] Style dashboard page in frontend/src/css/pages/dashboard.css
- [x] T044 [US1] Implement authentication JavaScript service in frontend/src/js/services/authService.js
- [x] T045 [US1] Implement login page JavaScript in frontend/src/js/pages/loginPage.js
- [x] T046 [US1] Implement registration page JavaScript in frontend/src/js/pages/registerPage.js
- [x] T047 [US1] Implement navigation and security JavaScript in frontend/src/js/utils/security.js ✅ Complete authentication system validated
- [x] T048 [US1] Add authentication middleware and route protection in frontend/src/js/utils/router.js ✅ End-to-end authentication flow working

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Personal Library Management (Priority: P2)

**Goal**: Users can view, search, sort, edit locations, and delete books in their personal library

**Independent Test**: Can login, view library with books, search by any field, sort by any column, edit physical locations, delete books with confirmation

### Tests for User Story 2 (TDD Required)

- [x] T049 [P] [US2] Contract test for personal library endpoints in backend/src/test/java/com/thehomearchive/library/controller/LibraryControllerTest.java
- [x] T050 [P] [US2] Contract test for book management endpoints in backend/src/test/java/com/thehomearchive/library/controller/BookControllerTest.java
- [x] T051 [P] [US2] Integration test for library management flow in backend/src/test/java/com/thehomearchive/library/integration/LibraryManagementIntegrationTest.java
- [x] T052 [P] [US2] Frontend test for library page functionality in frontend/tests/pages/LibraryPage.test.js
- [x] T053 [P] [US2] Frontend test for book card component in frontend/tests/components/BookCard.test.js

### Implementation for User Story 2

- [x] T054 [P] [US2] Create Book entity in backend/src/main/java/com/thehomearchive/library/entity/Book.java
- [x] T055 [P] [US2] Create Category entity in backend/src/main/java/com/thehomearchive/library/entity/Category.java
- [x] T056 [P] [US2] Create PersonalLibrary entity (user-book relationship) in backend/src/main/java/com/thehomearchive/library/entity/PersonalLibrary.java
- [x] T057 [P] [US2] Create book management DTOs in backend/src/main/java/com/thehomearchive/library/dto/book/
- [x] T058 [US2] Create BookRepository with search capabilities in backend/src/main/java/com/thehomearchive/library/repository/BookRepository.java (depends on T054)
- [x] T059 [US2] Create CategoryRepository in backend/src/main/java/com/thehomearchive/library/repository/CategoryRepository.java (depends on T055)
- [x] T060 [US2] Create PersonalLibraryRepository in backend/src/main/java/com/thehomearchive/library/repository/PersonalLibraryRepository.java (depends on T056)
- [x] T061 [US2] Implement LibraryService with personal library management in backend/src/main/java/com/thehomearchive/library/service/LibraryService.java
- [x] T062 [US2] Implement BookService with CRUD operations in backend/src/main/java/com/thehomearchive/library/service/BookService.java
- [x] T063 [US2] Create LibraryController for personal library operations in backend/src/main/java/com/thehomearchive/library/controller/LibraryController.java
- [x] T064 [US2] Create BookController for book management in backend/src/main/java/com/thehomearchive/library/controller/BookController.java
- [x] T065 [P] [US2] Create library management page HTML with card grid layout in frontend/src/html/library.html
- [x] T066 [P] [US2] Create book card component HTML template in frontend/src/html/components/book-card.html
- [x] T067 [P] [US2] Style library page with dark academia theme in frontend/src/css/pages/library.css
- [x] T068 [P] [US2] Style book card component in frontend/src/css/components/book-card.css
- [x] T069 [P] [US2] Style search and filter components in frontend/src/css/components/search-filter.css
- [x] T070 [US2] Implement library API service in frontend/src/js/services/libraryService.js
- [x] T071 [US2] Implement book card component JavaScript in frontend/src/js/components/BookCard.js
- [x] T072 [US2] Implement search and filter JavaScript in frontend/src/js/components/SearchFilter.js
- [x] T073 [US2] Implement library page JavaScript in frontend/src/js/pages/libraryPage.js
- [x] T074 [US2] Implement view toggle (card/list) functionality in frontend/src/js/components/ViewToggle.js
- [x] T075 [US2] Add confirmation dialogs for delete operations in frontend/src/js/utils/confirmDialog.js

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Book Discovery and Addition (Priority: P3)

**Goal**: Users can search the complete book database, view book details, and add books to their personal library

**Independent Test**: Can access book search page, search database by various fields, view book details, add books to personal library, prevent duplicate additions

### Tests for User Story 3 (TDD Required)

- [x] T076 [P] [US3] Contract test for book search endpoints in backend/src/test/java/com/thehomearchive/library/controller/BookSearchControllerTest.java
- [x] T077 [P] [US3] Contract test for book addition endpoints in backend/src/test/java/com/thehomearchive/library/controller/BookAdditionControllerTest.java  
- [x] T078 [P] [US3] Integration test for book discovery flow in backend/src/test/java/com/thehomearchive/library/integration/BookDiscoveryIntegrationTest.java
- [x] T079 [P] [US3] Frontend test for book search functionality in frontend/tests/pages/BookSearchPage.test.js
- [x] T080 [P] [US3] Frontend test for book addition flow in frontend/tests/components/BookAddition.test.js

### Implementation for User Story 3

- [x] T081 [P] [US3] Create BookRating entity in backend/src/main/java/com/thehomearchive/library/entity/BookRating.java
- [x] T082 [P] [US3] Create SearchHistory entity in backend/src/main/java/com/thehomearchive/library/entity/SearchHistory.java
- [x] T083 [P] [US3] Create book search DTOs in backend/src/main/java/com/thehomearchive/library/dto/search/
- [x] T084 [US3] Create BookRatingRepository in backend/src/main/java/com/thehomearchive/library/repository/BookRatingRepository.java (depends on T081)
- [x] T085 [US3] Create SearchHistoryRepository in backend/src/main/java/com/thehomearchive/library/repository/SearchHistoryRepository.java (depends on T082)
- [x] T086 [US3] Implement BookSearchService with full database search in backend/src/main/java/com/thehomearchive/library/service/BookSearchService.java
- [x] T087 [US3] Implement RatingService for book ratings in backend/src/main/java/com/thehomearchive/library/service/RatingService.java
- [x] T088 [US3] Create BookSearchController for database search in backend/src/main/java/com/thehomearchive/library/controller/BookSearchController.java
- [x] T089 [US3] Create RatingController for book ratings in backend/src/main/java/com/thehomearchive/library/controller/RatingController.java
- [x] T090 [P] [US3] Create book search page HTML in frontend/src/html/book-search.html
- [x] T091 [P] [US3] Create book detail modal HTML in frontend/src/html/components/book-detail-modal.html
- [x] T092 [P] [US3] Style book search page with dark academia theme in frontend/src/css/pages/book-search.css
- [x] T093 [P] [US3] Style book detail modal in frontend/src/css/components/book-detail-modal.css
- [x] T094 [P] [US3] Style search results layout in frontend/src/css/components/search-results.css
- [x] T095 [US3] Implement book search API service in frontend/src/js/services/bookSearchService.js
- [x] T096 [US3] Implement book detail modal JavaScript in frontend/src/js/components/BookDetailModal.js
- [x] T097 [US3] Implement search results component in frontend/src/js/components/SearchResults.js
- [x] T098 [US3] Implement book search page JavaScript in frontend/src/js/pages/bookSearchPage.js
- [x] T099 [US3] Implement book addition functionality in frontend/src/js/components/BookAddition.js
- [x] T100 [US3] Add duplicate prevention logic in frontend/src/js/utils/duplicateChecker.js
- ~~[ ] T101 [P] [US3] Implement Google Books API integration service~~ **REMOVED** - OpenLibrary API (T102) provides sufficient coverage
- [x] T102 [P] [US3] Implement OpenLibrary API integration service in backend/src/main/java/com/thehomearchive/library/service/OpenLibraryService.java
- [ ] T103 [US3] Create book metadata enrichment service using OpenLibrary API in backend/src/main/java/com/thehomearchive/library/service/BookMetadataService.java
- [ ] T104 [US3] Add external API fallback and error handling in book search flow

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T105 [P] Create comprehensive API documentation in backend/src/main/resources/static/docs/
- [ ] T106 [P] Add responsive design for mobile devices in frontend/src/css/responsive/mobile.css
- [ ] T107 [P] Implement loading states and error handling in frontend/src/js/utils/uiUtils.js
- [ ] T108 [P] Add performance monitoring and logging in backend/src/main/java/com/thehomearchive/library/config/MonitoringConfig.java
- [ ] T109 [P] Create sample data seeding script in backend/src/main/resources/data.sql
- [ ] T110 [P] Add security headers and CSRF protection in backend/src/main/java/com/thehomearchive/library/config/SecurityHeadersConfig.java
- [ ] T111 [P] Implement graceful degradation for offline scenarios in frontend/src/js/utils/offlineHandler.js
- [ ] T112 [P] Add accessibility (ARIA) labels and keyboard navigation in frontend/src/js/utils/accessibility.js
- [ ] T113 Code cleanup and refactoring across all components
- [ ] T114 Run quickstart.md validation and end-to-end testing

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies - can start immediately
- **Foundational (Phase 2)**: Depends on Setup completion - BLOCKS all user stories
- **User Stories (Phase 3+)**: All depend on Foundational phase completion
  - User stories can then proceed in parallel (if staffed)
  - Or sequentially in priority order (P1 → P2 → P3)
- **Polish (Final Phase)**: Depends on all desired user stories being complete

### User Story Dependencies

- **User Story 1 (P1)**: Can start after Foundational (Phase 2) - No dependencies on other stories
- **User Story 2 (P2)**: Can start after Foundational (Phase 2) - Integrates with US1 authentication but independently testable
- **User Story 3 (P3)**: Can start after Foundational (Phase 2) - Integrates with US1 auth and US2 library but independently testable

### Within Each User Story

- Tests MUST be written and FAIL before implementation (TDD requirement)
- Entities before repositories
- Repositories before services  
- Services before controllers
- Backend before frontend integration
- Core implementation before UI polish
- Story complete before moving to next priority

### Parallel Opportunities

- All Setup tasks marked [P] can run in parallel
- All Foundational tasks marked [P] can run in parallel (within Phase 2)
- Once Foundational phase completes, all user stories can start in parallel (if team capacity allows)
- All tests for a user story marked [P] can run in parallel
- Entities within a story marked [P] can run in parallel
- Frontend styling tasks marked [P] can run in parallel with backend development
- Different user stories can be worked on in parallel by different team members

---

## Parallel Example: User Story 1

```bash
# Launch all tests for User Story 1 together (TDD requirement):
Task: "Contract test for user registration endpoint"
Task: "Contract test for login endpoint"  
Task: "Contract test for email verification endpoint"
Task: "Integration test for authentication flow"
Task: "Security test for unauthorized access protection"
Task: "Frontend test for login form"

# Launch all entities for User Story 1 together:
Task: "Create User entity"
Task: "Create EmailVerification entity"
Task: "Create UserSession entity"
Task: "Create UserRole enum"
Task: "Create VerificationType enum"

# Launch parallel frontend styling:
Task: "Style login page with dark academia theme"
Task: "Style registration page"
Task: "Style dashboard page"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (Tasks T001-T007)
2. Complete Phase 2: Foundational (Tasks T008-T016) - CRITICAL foundation
3. Complete Phase 3: User Story 1 (Tasks T017-T048)
4. **STOP and VALIDATE**: Test User Story 1 independently - complete auth flow
5. Deploy/demo MVP - users can securely register, verify email, login, access dashboard

### Incremental Delivery

1. Complete Setup + Foundational → Foundation ready
2. Add User Story 1 → Test independently → Deploy/Demo (MVP - secure access!)
3. Add User Story 2 → Test independently → Deploy/Demo (MVP + library management!)
4. Add User Story 3 → Test independently → Deploy/Demo (Full feature set!)
5. Each story adds value without breaking previous stories

### Parallel Team Strategy

With multiple developers:

1. Team completes Setup + Foundational together (Tasks T001-T016)
2. Once Foundational is done:
   - Developer A: User Story 1 (Authentication & Security)
   - Developer B: User Story 2 (Library Management)  
   - Developer C: User Story 3 (Book Discovery)
3. Stories complete and integrate independently

---

## Task Summary

- **Total Tasks**: 114 tasks
- **User Story 1 Tasks**: 32 tasks (T020-T051) - Authentication & Security
- **User Story 2 Tasks**: 27 tasks (T052-T078) - Library Management  
- **User Story 3 Tasks**: 29 tasks (T079-T104) - Book Discovery & External APIs
- **Parallel Opportunities**: 49 tasks marked [P] can run in parallel
- **Independent Test Criteria**: Each user story has comprehensive test coverage and can be validated independently
- **Suggested MVP Scope**: User Story 1 only (secure authentication and dashboard access)

---

## Constitution Compliance

✅ **TDD Approach**: All user stories include comprehensive tests written first
✅ **Spring Boot 3.x**: All backend tasks use Spring Boot 3.2+ with Java 21+ LTS
✅ **Service-Oriented Architecture**: Clear separation of controller → service → repository layers
✅ **Spring Security**: JWT authentication with proper security configuration
✅ **JPA/Hibernate**: All entities use proper JPA annotations and relationships
✅ **RESTful APIs**: All endpoints follow REST conventions per OpenAPI specification
✅ **Performance**: Tasks include monitoring, caching, and performance considerations