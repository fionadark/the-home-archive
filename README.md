# The Home Archive 📚

A home library management application built as an experiment in **AI-driven software development** using [GitHub Spec Kit](https://github.com/github/spec-kit) and **Claude Sonnet 4**.

## About This Project

This application demonstrates the power of AI-assisted development by creating a complete book search and management system entirely through collaborative development between a human developer and AI implementation. 

### Development Approach
- **AI-Driven Development**: Built collaboratively with Claude Sonnet 4
- **Specification-First**: Designed using GitHub Spec Kit methodology
- **Iterative Refinement**: Features evolved through natural language conversations
- **Quality-Focused**: Comprehensive testing and code optimization throughout

## Features

### 🔍 Intelligent Book Search
- **Multi-source search** across local database and external APIs (OpenLibrary, Google Books)
- **Enhanced search** with automatic external API fallback when local results are insufficient
- **Smart suggestions** with real-time search recommendations
- **Fuzzy matching** and partial word support (e.g., "harr" finds "Harry Potter")
- **Advanced filtering** by category, publication year, rating, and metadata
- **Circuit breaker patterns** for reliable external API integration

### 📚 Library Management
- **Personal library** with secure user authentication and JWT tokens
- **Complete book metadata** tracking (title, author, genre, ISBN, publication year, etc.)
- **Reading lists** and favorite books organization
- **Book ratings and reviews** with personal and community ratings
- **Physical location tracking** with room and shelf management

### 🔗 External API Integration
- **OpenLibrary integration** as primary external book source (no API key required)
- **Google Books API support** (optional enhancement, requires API key)
- **Intelligent fallback** between local database and external sources
- **Real-time health monitoring** of external APIs
- **Rate limiting and timeout protection** for reliable service

## Tech Stack

The technology stack was selected through AI analysis of requirements, considering factors like:
- **Java 21 LTS**: Modern language features with long-term support
- **Spring Boot 3.x**: Mature ecosystem with Spring Data JPA, Spring Web, Spring Security, and Resilience4j
- **External APIs**: OpenLibrary (primary, public) and Google Books (optional, requires API key)
- **Circuit Breakers**: Resilience4j for fault-tolerant external API calls
- **Gradle**: Flexible build system with dependency management
- **H2/PostgreSQL**: Development flexibility with production scalability
- **OpenAPI**: Self-documenting API with interactive exploration

## Quick Start

### Prerequisites
- Java 21+ JDK
- Git

### Run Locally
```bash
git clone <repository-url>
cd the-home-archive
./gradlew bootRun
```

The application starts at `http://localhost:8080` with:
- H2 in-memory database with sample data
- OpenLibrary API integration enabled (works out of the box)
- Google Books API disabled by default (requires API key)
- Interactive API documentation at `/swagger-ui.html`
- H2 console at `/h2-console` (dev mode)

### API Examples

```bash
# Register a new user
curl -X POST "http://localhost:8080/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"SecurePass123!","firstName":"John","lastName":"Doe"}'

# Login and get JWT token
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"SecurePass123!"}'

# Search books (enhanced search with external APIs)
curl -H "Authorization: Bearer YOUR_TOKEN" \
  "http://localhost:8080/api/v1/search/books/enhanced?q=machine%20learning&includeExternal=true"
```

## Documentation

- **[API Documentation](docs/API.md)** - Complete REST API reference with examples
- **[Deployment Guide](docs/deployment.md)** - Development and production deployment instructions

## External API Testing
```bash
# Run all tests (unit tests only)
./gradlew test

# Run integration tests with real external APIs
./gradlew test -Dtest.integration.external=true

# Test specific external API functionality
./gradlew test -Dtest.integration.external=true --tests OpenLibraryServiceIntegrationTest
```


---

*Built with ❤️ using GitHub Spec Kit and Claude Sonnet 4*
