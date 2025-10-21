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
- **Web app**: `backend/src/`, `frontend/src/`
- Backend follows Spring Boot Maven structure: `backend/src/main/java/com/thehomearchive/library/`
- Frontend structure: `frontend/src/js/`, `frontend/src/css/`, `frontend/src/html/`

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Project initialization and basic structure

- [ ] T001 Create backend directory structure per implementation plan at backend/src/main/java/com/thehomearchive/library/
- [ ] T002 Initialize Spring Boot 3.2+ project with Maven dependencies in backend/pom.xml
- [ ] T003 [P] Create frontend directory structure at frontend/src/
- [ ] T004 [P] Configure application properties for H2 development database in backend/src/main/resources/application-dev.yml
- [ ] T005 [P] Configure application properties for PostgreSQL production in backend/src/main/resources/application-prod.yml
- [ ] T006 [P] Setup Jest testing framework configuration in frontend/package.json
- [ ] T007 [P] Create dark academia CSS variables and base theme in frontend/src/css/themes/dark-academia.css

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core infrastructure that MUST be complete before ANY user story can be implemented

**⚠️ CRITICAL**: No user story work can begin until this phase is complete

- [ ] T008 Create database schema with Flyway migrations in backend/src/main/resources/db/migration/V001__Initial_Schema.sql
- [ ] T009 [P] Implement JWT authentication configuration in backend/src/main/java/com/thehomearchive/library/config/JwtConfig.java
- [ ] T010 [P] Configure Spring Security with JWT in backend/src/main/java/com/thehomearchive/library/config/SecurityConfig.java
- [ ] T011 [P] Create global exception handler in backend/src/main/java/com/thehomearchive/library/config/GlobalExceptionHandler.java
- [ ] T012 [P] Setup CORS configuration in backend/src/main/java/com/thehomearchive/library/config/CorsConfig.java
- [ ] T013 Create base repository interface in backend/src/main/java/com/thehomearchive/library/repository/BaseRepository.java
- [ ] T014 [P] Implement email service configuration in backend/src/main/java/com/thehomearchive/library/config/EmailConfig.java
- [ ] T015 [P] Create API response wrapper DTOs in backend/src/main/java/com/thehomearchive/library/dto/ApiResponse.java
- [ ] T016 [P] Setup application main class and configuration in backend/src/main/java/com/thehomearchive/library/LibraryApplication.java

**Checkpoint**: Foundation ready - user story implementation can now begin in parallel

---

## Phase 3: User Story 1 - Secure Personal Library Access (Priority: P1) 🎯 MVP

**Goal**: Users can register, verify email, login securely, and access their personal library dashboard

**Independent Test**: Can create account, verify email, login, see personal dashboard, logout, and verify other users cannot access the library

### Tests for User Story 1 (TDD Required)

**NOTE: Write these tests FIRST, ensure they FAIL before implementation**

- [ ] T017 [P] [US1] Contract test for user registration endpoint in backend/src/test/java/com/thehomearchive/library/controller/AuthControllerTest.java
- [ ] T018 [P] [US1] Contract test for login endpoint in backend/src/test/java/com/thehomearchive/library/controller/AuthControllerTest.java
- [ ] T019 [P] [US1] Contract test for email verification endpoint in backend/src/test/java/com/thehomearchive/library/controller/AuthControllerTest.java
- [ ] T020 [P] [US1] Integration test for authentication flow in backend/src/test/java/com/thehomearchive/library/integration/AuthenticationIntegrationTest.java
- [ ] T021 [P] [US1] Security test for unauthorized access protection in backend/src/test/java/com/thehomearchive/library/security/SecurityTest.java
- [ ] T022 [P] [US1] Frontend test for login form in frontend/tests/pages/LoginPage.test.js

### Implementation for User Story 1

- [ ] T023 [P] [US1] Create User entity in backend/src/main/java/com/thehomearchive/library/entity/User.java
- [ ] T024 [P] [US1] Create EmailVerification entity in backend/src/main/java/com/thehomearchive/library/entity/EmailVerification.java  
- [ ] T025 [P] [US1] Create UserSession entity in backend/src/main/java/com/thehomearchive/library/entity/UserSession.java
- [ ] T026 [P] [US1] Create UserRole enum in backend/src/main/java/com/thehomearchive/library/entity/UserRole.java
- [ ] T027 [P] [US1] Create VerificationType enum in backend/src/main/java/com/thehomearchive/library/entity/VerificationType.java
- [ ] T028 [P] [US1] Create authentication DTOs in backend/src/main/java/com/thehomearchive/library/dto/auth/
- [ ] T029 [US1] Create UserRepository in backend/src/main/java/com/thehomearchive/library/repository/UserRepository.java (depends on T023)
- [ ] T030 [US1] Create EmailVerificationRepository in backend/src/main/java/com/thehomearchive/library/repository/EmailVerificationRepository.java (depends on T024)
- [ ] T031 [US1] Create UserSessionRepository in backend/src/main/java/com/thehomearchive/library/repository/UserSessionRepository.java (depends on T025)
- [ ] T032 [US1] Implement UserService with registration logic in backend/src/main/java/com/thehomearchive/library/service/UserService.java
- [ ] T033 [US1] Implement AuthenticationService with login/logout in backend/src/main/java/com/thehomearchive/library/service/AuthenticationService.java
- [ ] T034 [US1] Implement EmailService for verification emails in backend/src/main/java/com/thehomearchive/library/service/EmailService.java
- [ ] T035 [US1] Implement JwtService for token management in backend/src/main/java/com/thehomearchive/library/service/JwtService.java
- [ ] T036 [US1] Create AuthController with registration/login endpoints in backend/src/main/java/com/thehomearchive/library/controller/AuthController.java
- [ ] T037 [P] [US1] Create dark academia login page HTML in frontend/src/html/login.html
- [ ] T038 [P] [US1] Create registration page HTML in frontend/src/html/register.html
- [ ] T039 [P] [US1] Create email verification page HTML in frontend/src/html/verify-email.html
- [ ] T040 [P] [US1] Create personal dashboard page HTML in frontend/src/html/dashboard.html
- [ ] T041 [P] [US1] Style login page with dark academia theme in frontend/src/css/pages/login.css
- [ ] T042 [P] [US1] Style registration page in frontend/src/css/pages/register.css
- [ ] T043 [P] [US1] Style dashboard page in frontend/src/css/pages/dashboard.css
- [ ] T044 [US1] Implement authentication JavaScript service in frontend/src/js/services/authService.js
- [ ] T045 [US1] Implement login page JavaScript in frontend/src/js/pages/loginPage.js
- [ ] T046 [US1] Implement registration page JavaScript in frontend/src/js/pages/registerPage.js
- [ ] T047 [US1] Implement navigation and security JavaScript in frontend/src/js/utils/security.js
- [ ] T048 [US1] Add authentication middleware and route protection in frontend/src/js/utils/router.js

**Checkpoint**: At this point, User Story 1 should be fully functional and testable independently

---

## Phase 4: User Story 2 - Personal Library Management (Priority: P2)

**Goal**: Users can view, search, sort, edit locations, and delete books in their personal library

**Independent Test**: Can login, view library with books, search by any field, sort by any column, edit physical locations, delete books with confirmation

### Tests for User Story 2 (TDD Required)

- [ ] T049 [P] [US2] Contract test for personal library endpoints in backend/src/test/java/com/thehomearchive/library/controller/LibraryControllerTest.java
- [ ] T050 [P] [US2] Contract test for book management endpoints in backend/src/test/java/com/thehomearchive/library/controller/BookControllerTest.java
- [ ] T051 [P] [US2] Integration test for library management flow in backend/src/test/java/com/thehomearchive/library/integration/LibraryManagementIntegrationTest.java
- [ ] T052 [P] [US2] Frontend test for library page functionality in frontend/tests/pages/LibraryPage.test.js
- [ ] T053 [P] [US2] Frontend test for book card component in frontend/tests/components/BookCard.test.js

### Implementation for User Story 2

- [ ] T054 [P] [US2] Create Book entity in backend/src/main/java/com/thehomearchive/library/entity/Book.java
- [ ] T055 [P] [US2] Create Category entity in backend/src/main/java/com/thehomearchive/library/entity/Category.java
- [ ] T056 [P] [US2] Create PersonalLibrary entity (user-book relationship) in backend/src/main/java/com/thehomearchive/library/entity/PersonalLibrary.java
- [ ] T057 [P] [US2] Create book management DTOs in backend/src/main/java/com/thehomearchive/library/dto/book/
- [ ] T058 [US2] Create BookRepository with search capabilities in backend/src/main/java/com/thehomearchive/library/repository/BookRepository.java (depends on T054)
- [ ] T059 [US2] Create CategoryRepository in backend/src/main/java/com/thehomearchive/library/repository/CategoryRepository.java (depends on T055)
- [ ] T060 [US2] Create PersonalLibraryRepository in backend/src/main/java/com/thehomearchive/library/repository/PersonalLibraryRepository.java (depends on T056)
- [ ] T061 [US2] Implement LibraryService with personal library management in backend/src/main/java/com/thehomearchive/library/service/LibraryService.java
- [ ] T062 [US2] Implement BookService with CRUD operations in backend/src/main/java/com/thehomearchive/library/service/BookService.java
- [ ] T063 [US2] Create LibraryController for personal library operations in backend/src/main/java/com/thehomearchive/library/controller/LibraryController.java
- [ ] T064 [US2] Create BookController for book management in backend/src/main/java/com/thehomearchive/library/controller/BookController.java
- [ ] T065 [P] [US2] Create library management page HTML with card grid layout in frontend/src/html/library.html
- [ ] T066 [P] [US2] Create book card component HTML template in frontend/src/html/components/book-card.html
- [ ] T067 [P] [US2] Style library page with dark academia theme in frontend/src/css/pages/library.css
- [ ] T068 [P] [US2] Style book card component in frontend/src/css/components/book-card.css
- [ ] T069 [P] [US2] Style search and filter components in frontend/src/css/components/search-filter.css
- [ ] T070 [US2] Implement library API service in frontend/src/js/services/libraryService.js
- [ ] T071 [US2] Implement book card component JavaScript in frontend/src/js/components/BookCard.js
- [ ] T072 [US2] Implement search and filter JavaScript in frontend/src/js/components/SearchFilter.js
- [ ] T073 [US2] Implement library page JavaScript in frontend/src/js/pages/libraryPage.js
- [ ] T074 [US2] Implement view toggle (card/list) functionality in frontend/src/js/components/ViewToggle.js
- [ ] T075 [US2] Add confirmation dialogs for delete operations in frontend/src/js/utils/confirmDialog.js

**Checkpoint**: At this point, User Stories 1 AND 2 should both work independently

---

## Phase 5: User Story 3 - Book Discovery and Addition (Priority: P3)

**Goal**: Users can search the complete book database, view book details, and add books to their personal library

**Independent Test**: Can access book search page, search database by various fields, view book details, add books to personal library, prevent duplicate additions

### Tests for User Story 3 (TDD Required)

- [ ] T076 [P] [US3] Contract test for book search endpoints in backend/src/test/java/com/thehomearchive/library/controller/BookSearchControllerTest.java
- [ ] T077 [P] [US3] Contract test for book addition endpoints in backend/src/test/java/com/thehomearchive/library/controller/BookAdditionControllerTest.java  
- [ ] T078 [P] [US3] Integration test for book discovery flow in backend/src/test/java/com/thehomearchive/library/integration/BookDiscoveryIntegrationTest.java
- [ ] T079 [P] [US3] Frontend test for book search functionality in frontend/tests/pages/BookSearchPage.test.js
- [ ] T080 [P] [US3] Frontend test for book addition flow in frontend/tests/components/BookAddition.test.js

### Implementation for User Story 3

- [ ] T081 [P] [US3] Create BookRating entity in backend/src/main/java/com/thehomearchive/library/entity/BookRating.java
- [ ] T082 [P] [US3] Create SearchHistory entity in backend/src/main/java/com/thehomearchive/library/entity/SearchHistory.java
- [ ] T083 [P] [US3] Create book search DTOs in backend/src/main/java/com/thehomearchive/library/dto/search/
- [ ] T084 [US3] Create BookRatingRepository in backend/src/main/java/com/thehomearchive/library/repository/BookRatingRepository.java (depends on T081)
- [ ] T085 [US3] Create SearchHistoryRepository in backend/src/main/java/com/thehomearchive/library/repository/SearchHistoryRepository.java (depends on T082)
- [ ] T086 [US3] Implement BookSearchService with full database search in backend/src/main/java/com/thehomearchive/library/service/BookSearchService.java
- [ ] T087 [US3] Implement RatingService for book ratings in backend/src/main/java/com/thehomearchive/library/service/RatingService.java
- [ ] T088 [US3] Create BookSearchController for database search in backend/src/main/java/com/thehomearchive/library/controller/BookSearchController.java
- [ ] T089 [US3] Create RatingController for book ratings in backend/src/main/java/com/thehomearchive/library/controller/RatingController.java
- [ ] T090 [P] [US3] Create book search page HTML in frontend/src/html/book-search.html
- [ ] T091 [P] [US3] Create book detail modal HTML in frontend/src/html/components/book-detail-modal.html
- [ ] T092 [P] [US3] Style book search page with dark academia theme in frontend/src/css/pages/book-search.css
- [ ] T093 [P] [US3] Style book detail modal in frontend/src/css/components/book-detail-modal.css
- [ ] T094 [P] [US3] Style search results layout in frontend/src/css/components/search-results.css
- [ ] T095 [US3] Implement book search API service in frontend/src/js/services/bookSearchService.js
- [ ] T096 [US3] Implement book detail modal JavaScript in frontend/src/js/components/BookDetailModal.js
- [ ] T097 [US3] Implement search results component in frontend/src/js/components/SearchResults.js
- [ ] T098 [US3] Implement book search page JavaScript in frontend/src/js/pages/bookSearchPage.js
- [ ] T099 [US3] Implement book addition functionality in frontend/src/js/components/BookAddition.js
- [ ] T100 [US3] Add duplicate prevention logic in frontend/src/js/utils/duplicateChecker.js

**Checkpoint**: All user stories should now be independently functional

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Improvements that affect multiple user stories

- [ ] T101 [P] Create comprehensive API documentation in backend/src/main/resources/static/docs/
- [ ] T102 [P] Add responsive design for mobile devices in frontend/src/css/responsive/mobile.css
- [ ] T103 [P] Implement loading states and error handling in frontend/src/js/utils/uiUtils.js
- [ ] T104 [P] Add performance monitoring and logging in backend/src/main/java/com/thehomearchive/library/config/MonitoringConfig.java
- [ ] T105 [P] Create sample data seeding script in backend/src/main/resources/data.sql
- [ ] T106 [P] Add security headers and CSRF protection in backend/src/main/java/com/thehomearchive/library/config/SecurityHeadersConfig.java
- [ ] T107 [P] Implement graceful degradation for offline scenarios in frontend/src/js/utils/offlineHandler.js
- [ ] T108 [P] Add accessibility (ARIA) labels and keyboard navigation in frontend/src/js/utils/accessibility.js
- [ ] T109 Code cleanup and refactoring across all components
- [ ] T110 Run quickstart.md validation and end-to-end testing

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

- **Total Tasks**: 110 tasks
- **User Story 1 Tasks**: 32 tasks (T017-T048) - Authentication & Security
- **User Story 2 Tasks**: 27 tasks (T049-T075) - Library Management  
- **User Story 3 Tasks**: 25 tasks (T076-T100) - Book Discovery
- **Parallel Opportunities**: 45 tasks marked [P] can run in parallel
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