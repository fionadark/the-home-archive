# Quickstart: Dark Academia Library Web Application

**Date**: 2025-01-27 | **Feature**: 002-web-application-this

## Overview

This quickstart guide provides step-by-step instructions for setting up the dark academia library web application development environment and running the application locally.

## Prerequisites

**Required Software**:
- Java 21+ LTS (OpenJDK recommended)
- Maven 3.8+ or wrapper included
- Node.js 18+ (for frontend tooling)
- PostgreSQL 15+ (production) or use H2 for development
- Git for version control

**Development Tools** (recommended):
- IntelliJ IDEA or VS Code
- Postman or curl for API testing
- pgAdmin or similar for database management

## Project Setup

### 1. Repository Structure

```bash
# Clone and navigate to project
git clone <repository-url>
cd the-home-archive

# Verify structure
tree -L 2
# Expected:
# ├── backend/
# ├── frontend/
# ├── specs/
# └── README.md
```

### 2. Backend Setup (Spring Boot)

```bash
# Navigate to backend
cd backend

# Install dependencies (if using Maven wrapper)
./mvnw clean install

# Or with system Maven
mvn clean install

# Run tests to verify setup
./mvnw test

# Start development server with H2 database
./mvnw spring-boot:run

# Backend should be running at http://localhost:8080
```

**Environment Configuration**:
```yaml
# backend/src/main/resources/application-dev.yml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:mem:testdb
    driverClassName: org.h2.Driver
    username: sa
    password: password
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
```

### 3. Database Setup (Optional - Production)

```bash
# Install PostgreSQL (macOS)
brew install postgresql
brew services start postgresql

# Create database
createdb library_dev
createuser library_user --pwprompt

# Grant permissions
psql -d library_dev -c "GRANT ALL PRIVILEGES ON DATABASE library_dev TO library_user;"
```

**Production Configuration**:
```yaml
# backend/src/main/resources/application-prod.yml
spring:
  profiles:
    active: prod
  datasource:
    url: jdbc:postgresql://localhost:5432/library_dev
    username: library_user
    password: ${DB_PASSWORD:library_password}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
```

### 4. Frontend Setup

```bash
# Navigate to frontend
cd ../frontend

# Install testing dependencies
npm init -y
npm install --save-dev jest @testing-library/dom @testing-library/jest-dom

# Create basic test setup
echo '{"scripts":{"test":"jest"}}' > package.json

# Run tests
npm test
```

## Development Workflow

### 1. Starting the Application

```bash
# Terminal 1: Start backend
cd backend
./mvnw spring-boot:run

# Terminal 2: Serve frontend (development)
cd frontend
python3 -m http.server 3000
# or
npx serve src/ -p 3000

# Access application:
# Frontend: http://localhost:3000
# Backend API: http://localhost:8080/api/v1
# H2 Console: http://localhost:8080/h2-console
```

### 2. API Testing

```bash
# Test health endpoint
curl http://localhost:8080/api/v1/health

# Register new user
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123",
    "firstName": "Test",
    "lastName": "User"
  }'

# Login
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }'

# Search books (with JWT token)
curl -X GET "http://localhost:8080/api/v1/books?q=science" \
  -H "Authorization: Bearer <your-jwt-token>"
```

### 3. Database Access

**H2 Console** (Development):
- URL: http://localhost:8080/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

**PostgreSQL** (Production):
```bash
# Connect to database
psql -d library_dev -U library_user

# Check tables
\dt

# Sample query
SELECT * FROM users;
SELECT * FROM books LIMIT 10;
```

## Testing Strategy

### Backend Testing

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=UserServiceTest

# Run with coverage
./mvnw test jacoco:report
```

**Test Categories**:
- **Unit Tests**: Service and repository layer testing
- **Integration Tests**: Full API endpoint testing
- **Security Tests**: Authentication and authorization
- **Database Tests**: JPA entity and relationship testing

### Frontend Testing

```bash
# Run JavaScript tests
npm test

# Run with coverage
npm test -- --coverage

# Test specific component
npm test -- components/BookCard.test.js
```

## Data Seeding

### Development Data

```sql
-- Sample categories
INSERT INTO categories (name, description, slug, created_at) VALUES
('Fiction', 'Literary fiction and novels', 'fiction', CURRENT_TIMESTAMP),
('Science', 'Scientific texts and research', 'science', CURRENT_TIMESTAMP),
('History', 'Historical accounts and biographies', 'history', CURRENT_TIMESTAMP),
('Philosophy', 'Philosophical works and theory', 'philosophy', CURRENT_TIMESTAMP);

-- Sample books
INSERT INTO books (title, author, isbn, description, publication_year, category_id, created_at) VALUES
('The Dark Academia', 'Jane Scholar', '978-0123456789', 'A mysterious tale set in an ancient university', 2023, 1, CURRENT_TIMESTAMP),
('Principles of Science', 'Dr. Research', '978-0987654321', 'Fundamental scientific principles explained', 2022, 2, CURRENT_TIMESTAMP);
```

### Flyway Migrations

```sql
-- V001__Initial_Schema.sql
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) DEFAULT 'USER',
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP
);

-- Additional migration files...
```

## Configuration Reference

### Environment Variables

```bash
# Backend configuration
export DB_PASSWORD=your_secure_password
export JWT_SECRET=your_jwt_secret_key
export EMAIL_HOST=smtp.gmail.com
export EMAIL_PORT=587
export EMAIL_USERNAME=your_email@gmail.com
export EMAIL_PASSWORD=your_app_password

# Frontend configuration (if needed)
export API_BASE_URL=http://localhost:8080/api/v1
```

### Application Properties

```properties
# backend/src/main/resources/application.properties

# Server configuration
server.port=8080
server.servlet.context-path=/

# JWT configuration
jwt.secret=${JWT_SECRET:default_secret_key_change_in_production}
jwt.expiration=900000
jwt.refresh-expiration=604800000

# Email configuration
spring.mail.host=${EMAIL_HOST:localhost}
spring.mail.port=${EMAIL_PORT:587}
spring.mail.username=${EMAIL_USERNAME:}
spring.mail.password=${EMAIL_PASSWORD:}
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# File upload
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB
```

## Troubleshooting

### Common Issues

**Backend won't start**:
```bash
# Check Java version
java -version
# Should be 21+

# Check port availability
lsof -i :8080

# Clean and rebuild
./mvnw clean compile
```

**Database connection issues**:
```bash
# Verify H2 console access
curl http://localhost:8080/h2-console

# Check PostgreSQL service
brew services list | grep postgresql
```

**Authentication problems**:
```bash
# Verify JWT secret is set
echo $JWT_SECRET

# Check user creation
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@test.com","password":"test123","firstName":"Test","lastName":"User"}' \
  -v
```

### Performance Optimization

**Development Settings**:
```yaml
# Faster startup for development
spring:
  jpa:
    hibernate:
      ddl-auto: create-drop
  devtools:
    restart:
      enabled: true
logging:
  level:
    org.hibernate.SQL: DEBUG
```

**Production Settings**:
```yaml
# Production optimizations
spring:
  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
logging:
  level:
    org.hibernate.SQL: WARN
```

## Next Steps

1. **Complete Backend Implementation**:
   - Implement all controller endpoints per API specification
   - Add comprehensive validation and error handling
   - Set up email verification service
   - Implement JWT refresh token rotation

2. **Frontend Development**:
   - Create responsive HTML templates
   - Implement dark academia CSS theme
   - Add JavaScript for API communication
   - Implement search and filtering UI

3. **Testing Coverage**:
   - Write comprehensive unit tests
   - Add integration test suite
   - Implement E2E testing with browser automation
   - Set up performance testing

4. **Deployment Preparation**:
   - Configure production database
   - Set up CI/CD pipeline
   - Implement monitoring and logging
   - Security hardening and review

## Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Security Reference](https://spring.io/projects/spring-security)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [JWT Best Practices](https://tools.ietf.org/html/rfc7519)
- [Dark Academia CSS Inspiration](https://codepen.io/search/pens?q=dark+academia)