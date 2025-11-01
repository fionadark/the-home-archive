# API Documentation

Complete documentation for the Dark Academia Library API.

## Quick Start

### Authentication

All API endpoints except registration and login require authentication via JWT Bearer tokens.

#### Register
```bash
curl -X POST "http://localhost:8080/api/v1/auth/register" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePassword123!",
    "firstName": "John",
    "lastName": "Doe"
  }'
```

#### Login
```bash
curl -X POST "http://localhost:8080/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "user@example.com",
    "password": "SecurePassword123!"
  }'
```

**Response:**
```json
{
  "success": true,
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "expiresIn": 86400
  }
}
```

#### Using the Token
```bash
# Store token and use in subsequent requests
TOKEN="your-access-token"
curl -H "Authorization: Bearer $TOKEN" "http://localhost:8080/api/v1/users/profile"
```

### Book Search

#### Basic Search
```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/search/books?q=javascript&page=0&size=10"
```

#### Enhanced Search (with External APIs)
```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/search/books/enhanced?q=machine%20learning&includeExternal=true"
```

## Core Endpoints

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/register` | Register new user |
| POST | `/api/v1/auth/login` | User login |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| POST | `/api/v1/auth/logout` | User logout |
| POST | `/api/v1/auth/verify-email` | Verify email address |
| POST | `/api/v1/auth/forgot-password` | Request password reset |
| POST | `/api/v1/auth/reset-password` | Reset password |

### User Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users/profile` | Get user profile |
| PUT | `/api/v1/users/profile` | Update user profile |
| POST | `/api/v1/users/change-password` | Change password |
| DELETE | `/api/v1/users/account` | Delete account |

### Book Search & Discovery

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/search/books` | Search local book database |
| GET | `/api/v1/search/books/enhanced` | Enhanced search with external APIs |
| GET | `/api/v1/search/books/suggestions` | Get search suggestions |
| GET | `/api/v1/search/books/popular` | Get popular searches |
| GET | `/api/v1/search/history` | Get personal search history |
| DELETE | `/api/v1/search/history` | Clear search history |

### Book Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/books/{id}` | Get book details |
| POST | `/api/v1/books/{id}/rate` | Rate a book |
| GET | `/api/v1/books/{id}/ratings` | Get book ratings |
| GET | `/api/v1/categories` | Get all categories |

### Personal Library

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users/library` | Get personal library |
| POST | `/api/v1/users/library/books` | Add books to library |
| DELETE | `/api/v1/users/library/books/{id}` | Remove book from library |
| GET | `/api/v1/users/favorites` | Get favorite books |
| POST | `/api/v1/users/favorites/{id}` | Add to favorites |
| DELETE | `/api/v1/users/favorites/{id}` | Remove from favorites |

### Reading Lists

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/users/reading-lists` | Get reading lists |
| POST | `/api/v1/users/reading-lists` | Create reading list |
| PUT | `/api/v1/users/reading-lists/{id}` | Update reading list |
| DELETE | `/api/v1/users/reading-lists/{id}` | Delete reading list |
| POST | `/api/v1/users/reading-lists/{id}/books` | Add books to list |
| DELETE | `/api/v1/users/reading-lists/{id}/books` | Remove books from list |

## Search Features

### Basic Search Parameters

- `q` (string): Search query (title, author, ISBN)
- `category` (number): Category ID filter
- `page` (number, default: 0): Page number (0-based)
- `size` (number, default: 20): Page size (1-100)
- `sort` (string, default: "title"): Sort field
- `direction` (string, default: "asc"): Sort direction

### Enhanced Search with External APIs

The enhanced search automatically queries external APIs (OpenLibrary, Google Books) when local results are insufficient:

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/search/books/enhanced?q=programming&minLocalResults=5"
```

**Response includes:**
- `localResults`: Books from local database
- `externalResults`: Books from external APIs
- `externalSearchPerformed`: Whether external search was triggered
- `externalApiHealthStatus`: Health status of external APIs

### Search Suggestions

Get real-time search suggestions:

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/search/books/suggestions?q=java&limit=5"
```

## Error Handling

### Standard Error Response

```json
{
  "success": false,
  "message": "Error description",
  "details": "Additional error context",
  "timestamp": "2025-10-30T10:30:00Z",
  "path": "/api/v1/endpoint"
}
```

### HTTP Status Codes

| Status | Description |
|--------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request (validation errors) |
| 401 | Unauthorized (invalid/missing token) |
| 403 | Forbidden (insufficient permissions) |
| 404 | Not Found |
| 409 | Conflict (duplicate resource) |
| 429 | Too Many Requests (rate limited) |
| 500 | Internal Server Error |

### Common Error Examples

#### Validation Error
```json
{
  "success": false,
  "message": "Validation failed",
  "details": "Email is required",
  "timestamp": "2025-10-30T10:30:00Z",
  "path": "/api/v1/auth/register"
}
```

#### Authentication Error
```json
{
  "success": false,
  "message": "Invalid or expired token",
  "timestamp": "2025-10-30T10:30:00Z",
  "path": "/api/v1/users/profile"
}
```

## Rate Limiting

### Default Limits

- **Authenticated users**: 1000 requests per hour
- **Unauthenticated users**: 100 requests per hour
- **Search endpoints**: 500 requests per hour per user
- **External API calls**: 100 requests per hour per user

### Rate Limit Headers

```
X-RateLimit-Limit: 1000
X-RateLimit-Remaining: 999
X-RateLimit-Reset: 1698667200
```

### Rate Limit Exceeded

```json
{
  "success": false,
  "message": "Rate limit exceeded",
  "details": "Try again in 60 seconds",
  "timestamp": "2025-10-30T10:30:00Z"
}
```

## External API Integration

### Supported APIs

1. **OpenLibrary** - Primary external source (enabled by default)
   - Public API, no authentication required
   - Comprehensive open book database
   - Cover images and metadata
   - Works out of the box

2. **Google Books** - Optional secondary source (requires configuration)
   - Requires API key (GOOGLE_BOOKS_API_KEY environment variable)
   - Rich metadata and reviews when configured
   - High-quality cover images
   - Disabled by default if no API key provided

### Health Check

Check external API status:

```bash
curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/api/v1/search/external-apis/health"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "openLibraryHealthy": true,
    "openLibraryMessage": "Service operational",
    "googleBooksHealthy": false,
    "googleBooksMessage": "API key not configured"
  }
}
```

## Code Examples

### JavaScript/Node.js

```javascript
class LibraryApiClient {
  constructor(baseUrl) {
    this.baseUrl = baseUrl;
    this.accessToken = null;
  }

  async login(email, password) {
    const response = await fetch(`${this.baseUrl}/api/v1/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ email, password })
    });
    
    const result = await response.json();
    if (result.success) {
      this.accessToken = result.data.accessToken;
    }
    return result;
  }

  async searchBooks(query, options = {}) {
    const params = new URLSearchParams();
    if (query) params.append('q', query);
    Object.entries(options).forEach(([key, value]) => {
      if (value !== undefined) params.append(key, value);
    });

    const response = await fetch(
      `${this.baseUrl}/api/v1/search/books?${params}`,
      {
        headers: { 'Authorization': `Bearer ${this.accessToken}` }
      }
    );
    
    return response.json();
  }
}

// Usage
const client = new LibraryApiClient('http://localhost:8080');
await client.login('user@example.com', 'password');
const books = await client.searchBooks('javascript', { size: 10 });
```

### Python

```python
import requests

class LibraryApiClient:
    def __init__(self, base_url):
        self.base_url = base_url.rstrip('/')
        self.access_token = None

    def login(self, email, password):
        response = requests.post(f"{self.base_url}/api/v1/auth/login", 
                               json={"email": email, "password": password})
        result = response.json()
        if result.get('success'):
            self.access_token = result['data']['accessToken']
        return result

    def search_books(self, query=None, **options):
        params = {}
        if query:
            params['q'] = query
        params.update(options)
        
        headers = {'Authorization': f'Bearer {self.access_token}'}
        response = requests.get(f"{self.base_url}/api/v1/search/books", 
                              params=params, headers=headers)
        return response.json()

# Usage
client = LibraryApiClient('http://localhost:8080')
client.login('user@example.com', 'password')
books = client.search_books('python programming', size=10)
```

## Testing

### Unit Tests
```bash
./gradlew test
```

### Integration Tests (with External APIs)
```bash
./gradlew test -Dtest.integration.external=true
```

### Specific External API Tests
```bash
./gradlew test -Dtest.integration.external=true --tests OpenLibraryServiceIntegrationTest
```

The integration tests verify:
- Real API connectivity to OpenLibrary
- Search functionality across different query types
- Data validation and field mapping
- Error handling and timeout management
- Edge cases and unicode content

---

For deployment instructions, see [DEPLOYMENT.md](DEPLOYMENT.md).