# Deployment Guide

Complete deployment guide for the Dark Academia Library API.

## Quick Start (Development)

### Prerequisites
- Java 21+ JDK
- Git

### Run Locally
```bash
git clone <repository-url>
cd the-home-archive
./gradlew bootRun
```

The application will start at `http://localhost:8080` with:
- H2 in-memory database
- Sample data loaded automatically
- H2 console available at `/h2-console`

## Environment Configuration

### Development (application-dev.yml)
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: 
  h2:
    console:
      enabled: true

jwt:
  secret: dev-secret-key-change-in-production
  expiration: 86400000 # 24 hours

external:
  apis:
    open-library:
      base-url: https://openlibrary.org
      enabled: true
    google-books:
      api-key: ${GOOGLE_BOOKS_API_KEY:}  # Optional - Google Books disabled if not provided
      enabled: ${GOOGLE_BOOKS_ENABLED:false}
```

### Production (application-prod.yml)
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
  jpa:
    hibernate:
      ddl-auto: validate

jwt:
  secret: ${JWT_SECRET}
  expiration: 3600000 # 1 hour in production

external:
  apis:
    open-library:
      base-url: https://openlibrary.org
      enabled: true
    google-books:
      api-key: ${GOOGLE_BOOKS_API_KEY}  # Required for Google Books integration
      enabled: ${GOOGLE_BOOKS_ENABLED:true}

management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics
```

## Docker Deployment

### Dockerfile
```dockerfile
FROM openjdk:21-jre-slim

WORKDIR /app
COPY build/libs/the-home-archive-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Docker Compose with PostgreSQL
```yaml
version: '3.8'

services:
  database:
    image: postgres:15-alpine
    environment:
      POSTGRES_DB: library
      POSTGRES_USER: library_user
      POSTGRES_PASSWORD: library_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DATABASE_URL: jdbc:postgresql://database:5432/library
      DATABASE_USERNAME: library_user
      DATABASE_PASSWORD: library_password
      JWT_SECRET: your-super-secret-jwt-key
    depends_on:
      - database

volumes:
  postgres_data:
```

### Build and Run
```bash
# Build the application
./gradlew build

# Build Docker image
docker build -t library-api .

# Run with Docker Compose
docker-compose up -d
```

## Local Development Setup

### 1. Clone the Repository
```bash
git clone https://github.com/username/the-home-archive.git
cd the-home-archive
```

### 2. Environment Configuration

Create `application-dev.yml` in `src/main/resources/`:

```yaml
spring:
  profiles:
    active: dev
  datasource:
    url: jdbc:h2:mem:testdb
    driverClassName: org.h2.Driver
    username: sa
    password: 
  h2:
    console:
      enabled: true
      path: /h2-console
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
    hibernate:
      ddl-auto: create-drop
    show-sql: true

# JWT Configuration
jwt:
  secret: dev-secret-key-change-in-production
  expiration: 86400000 # 24 hours
  refresh-expiration: 604800000 # 7 days

# External API Configuration
external:
  apis:
    google-books:
      api-key: your-google-books-api-key
      base-url: https://www.googleapis.com/books/v1/volumes
      enabled: true
    open-library:
      base-url: https://openlibrary.org
      enabled: true

# Logging
logging:
  level:
    com.darktower.library: DEBUG
    org.springframework.security: DEBUG
```

### 3. Run the Application

#### Using Maven
```bash
# Clean and compile
mvn clean compile

# Run with dev profile
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Or run with environment variable
SPRING_PROFILES_ACTIVE=dev mvn spring-boot:run
```

#### Using Java
```bash
# Build the JAR
mvn clean package -DskipTests

# Run the JAR
java -jar target/the-home-archive-1.0.0.jar --spring.profiles.active=dev
```

### 4. Verify Installation

#### Health Check
```bash
curl http://localhost:8080/actuator/health
```

#### Expected Response
```json
{
  "status": "UP",
  "components": {
    "db": {"status": "UP"},
    "diskSpace": {"status": "UP"}
  }
}
```

#### H2 Database Console
- URL: http://localhost:8080/api/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: (empty)

## Docker Deployment

### 1. Dockerfile

Create `Dockerfile` in the project root:

```dockerfile
# Build stage
FROM maven:3.9-openjdk-21-slim AS builder

WORKDIR /app
COPY pom.xml .
COPY src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:21-jre-slim

# Install useful tools
RUN apt-get update && apt-get install -y \
    curl \
    netcat-openbsd \
    && rm -rf /var/lib/apt/lists/*

# Create application user
RUN groupadd -r spring && useradd -r -g spring spring

WORKDIR /app

# Copy JAR from build stage
COPY --from=builder /app/target/the-home-archive-*.jar app.jar

# Create logs directory
RUN mkdir -p /app/logs && chown -R spring:spring /app

USER spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

EXPOSE 8080

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

### 2. Docker Compose Configuration

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  database:
    image: postgres:15-alpine
    container_name: library-db
    environment:
      POSTGRES_DB: library
      POSTGRES_USER: library_user
      POSTGRES_PASSWORD: library_password
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data
      - ./init-scripts:/docker-entrypoint-initdb.d
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U library_user -d library"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: library-redis
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

  app:
    build: .
    container_name: library-api
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: docker
      SPRING_DATASOURCE_URL: jdbc:postgresql://database:5432/library
      SPRING_DATASOURCE_USERNAME: library_user
      SPRING_DATASOURCE_PASSWORD: library_password
      SPRING_REDIS_HOST: redis
      SPRING_REDIS_PORT: 6379
      JWT_SECRET: ${JWT_SECRET}
      GOOGLE_BOOKS_API_KEY: ${GOOGLE_BOOKS_API_KEY}
    depends_on:
      database:
        condition: service_healthy
      redis:
        condition: service_healthy
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8080/actuator/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 60s
    restart: unless-stopped

volumes:
  postgres_data:
  redis_data:

networks:
  default:
    name: library-network
```

### 3. Environment Variables

Create `.env` file:

```env
# Database Configuration
POSTGRES_DB=library
POSTGRES_USER=library_user
POSTGRES_PASSWORD=library_password

# JWT Configuration
JWT_SECRET=your-super-secret-jwt-key-at-least-32-characters

# External API Keys
GOOGLE_BOOKS_API_KEY=your-google-books-api-key

# Application Configuration
SPRING_PROFILES_ACTIVE=docker
```

### 4. Docker Profile Configuration

Create `application-docker.yml`:

```yaml
spring:
  profiles:
    active: docker
  datasource:
    url: jdbc:postgresql://database:5432/library
    username: ${SPRING_DATASOURCE_USERNAME:library_user}
    password: ${SPRING_DATASOURCE_PASSWORD:library_password}
    driver-class-name: org.postgresql.Driver
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
    show-sql: false
  redis:
    host: ${SPRING_REDIS_HOST:redis}
    port: ${SPRING_REDIS_PORT:6379}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0

# Production-ready settings
server:
  port: 8080
  compression:
    enabled: true
  http2:
    enabled: true

# Actuator endpoints
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: when-authorized

# JWT Configuration
jwt:
  secret: ${JWT_SECRET}
  expiration: 86400000 # 24 hours
  refresh-expiration: 604800000 # 7 days

# External APIs
external:
  apis:
    google-books:
      api-key: ${GOOGLE_BOOKS_API_KEY}
      base-url: https://www.googleapis.com/books/v1/volumes
      enabled: true
    open-library:
      base-url: https://openlibrary.org
      enabled: true

# Logging
logging:
  level:
    com.darktower.library: INFO
    org.springframework.security: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: /app/logs/application.log
```

### 5. Deploy with Docker Compose

```bash
# Build and start services
docker-compose up -d --build

# Check service status
docker-compose ps

# View logs
docker-compose logs -f app

# Stop services
docker-compose down

# Stop and remove volumes (data loss!)
docker-compose down -v
```

### 6. Database Initialization

Create `init-scripts/01-init.sql`:

```sql
-- Create additional databases if needed
CREATE DATABASE library_test;

-- Create read-only user for analytics
CREATE USER library_readonly WITH PASSWORD 'readonly_password';
GRANT CONNECT ON DATABASE library TO library_readonly;
GRANT USAGE ON SCHEMA public TO library_readonly;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO library_readonly;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO library_readonly;

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_books_title ON books(title);
CREATE INDEX IF NOT EXISTS idx_books_author ON books(author);
CREATE INDEX IF NOT EXISTS idx_books_category_id ON books(category_id);
CREATE INDEX IF NOT EXISTS idx_books_publication_year ON books(publication_year);
CREATE INDEX IF NOT EXISTS idx_user_ratings_user_id ON user_ratings(user_id);
CREATE INDEX IF NOT EXISTS idx_user_ratings_book_id ON user_ratings(book_id);
```

## Production Deployment

### 1. Production Profile Configuration

Create `application-prod.yml`:

```yaml
spring:
  profiles:
    active: prod
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      validation-timeout: 5000
  jpa:
    database-platform: org.hibernate.dialect.PostgreSQLDialect
    hibernate:
      ddl-auto: validate
    show-sql: false
    properties:
      hibernate:
        jdbc:
          batch_size: 20
        order_inserts: true
        order_updates: true
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD}
    ssl: ${REDIS_SSL:false}
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 10
        max-idle: 10
        min-idle: 2

# Server configuration
server:
  port: ${PORT:8080}
  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain,application/javascript,text/css
  http2:
    enabled: true
  forward-headers-strategy: framework

# Security settings
security:
  require-ssl: true

# Actuator configuration
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  endpoint:
    health:
      show-details: never
  metrics:
    export:
      prometheus:
        enabled: true

# JWT configuration
jwt:
  secret: ${JWT_SECRET}
  expiration: ${JWT_EXPIRATION:3600000} # 1 hour in production
  refresh-expiration: ${JWT_REFRESH_EXPIRATION:604800000} # 7 days

# External APIs
external:
  apis:
    google-books:
      api-key: ${GOOGLE_BOOKS_API_KEY}
      base-url: https://www.googleapis.com/books/v1/volumes
      enabled: ${GOOGLE_BOOKS_ENABLED:true}
    open-library:
      base-url: https://openlibrary.org
      enabled: ${OPEN_LIBRARY_ENABLED:true}

# Logging
logging:
  level:
    com.darktower.library: INFO
    org.springframework.security: WARN
    org.hibernate.SQL: WARN
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
  file:
    name: ${LOG_FILE_PATH:/var/log/library/application.log}
```

### 2. Cloud Platform Deployment

#### Heroku Deployment

1. **Create `Procfile`:**
```
web: java -Dserver.port=$PORT $JAVA_OPTS -jar target/the-home-archive-*.jar --spring.profiles.active=prod
```

2. **Configure Environment Variables:**
```bash
heroku config:set SPRING_PROFILES_ACTIVE=prod
heroku config:set DATABASE_URL="your-postgres-url"
heroku config:set JWT_SECRET="your-jwt-secret"
heroku config:set GOOGLE_BOOKS_API_KEY="your-api-key"
```

3. **Deploy:**
```bash
git push heroku main
```

#### AWS Elastic Beanstalk

1. **Create `Dockerrun.aws.json`:**
```json
{
  "AWSEBDockerrunVersion": "1",
  "Image": {
    "Name": "your-dockerhub-username/library-api:latest",
    "Update": "true"
  },
  "Ports": [
    {
      "ContainerPort": "8080"
    }
  ]
}
```

2. **Deploy using EB CLI:**
```bash
eb init
eb create production
eb deploy
```

#### Google Cloud Platform (Cloud Run)

1. **Build and push image:**
```bash
gcloud builds submit --tag gcr.io/your-project-id/library-api
```

2. **Deploy to Cloud Run:**
```bash
gcloud run deploy library-api \
  --image gcr.io/your-project-id/library-api \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --port 8080 \
  --memory 1Gi \
  --cpu 1 \
  --set-env-vars SPRING_PROFILES_ACTIVE=prod
```

### 3. Database Migration Strategy

#### Flyway Configuration

Add to `pom.xml`:
```xml
<plugin>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-maven-plugin</artifactId>
    <version>9.8.1</version>
</plugin>
```

Create migration files in `src/main/resources/db/migration/`:

**`V1__Initial_schema.sql`:**
```sql
-- Initial database schema
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    slug VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE books (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    author VARCHAR(300) NOT NULL,
    isbn VARCHAR(13) UNIQUE,
    description TEXT,
    publication_year INTEGER,
    publisher VARCHAR(200),
    page_count INTEGER,
    category_id BIGINT REFERENCES categories(id),
    cover_image_url VARCHAR(1000),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Continue with other tables...
```

**`V2__Add_indexes.sql`:**
```sql
-- Performance indexes
CREATE INDEX idx_books_title ON books(title);
CREATE INDEX idx_books_author ON books(author);
CREATE INDEX idx_books_category_id ON books(category_id);
CREATE INDEX idx_books_publication_year ON books(publication_year);
CREATE INDEX idx_books_isbn ON books(isbn);
```

#### Migration Commands
```bash
# Run migrations
mvn flyway:migrate

# Check migration status
mvn flyway:info

# Validate migrations
mvn flyway:validate
```

## Monitoring and Observability

### 1. Health Checks

The application provides comprehensive health checks:

```bash
# Overall health
curl http://localhost:8080/actuator/health

# Detailed health (requires authentication)
curl -H "Authorization: Bearer token" http://localhost:8080/actuator/health

# Database health
curl http://localhost:8080/actuator/health/db

# External API health
curl http://localhost:8080/actuator/health/external-apis
```

### 2. Metrics Collection

#### Prometheus Configuration

Add to `application.yml`:
```yaml
management:
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
        jvm.gc.pause: 0.5, 0.95, 0.99
```

#### Custom Metrics

```java
@Component
public class SearchMetrics {
    
    private final Counter searchCounter;
    private final Timer searchTimer;
    
    public SearchMetrics(MeterRegistry meterRegistry) {
        this.searchCounter = Counter.builder("search.requests.total")
            .description("Total search requests")
            .tag("endpoint", "books")
            .register(meterRegistry);
            
        this.searchTimer = Timer.builder("search.duration")
            .description("Search request duration")
            .register(meterRegistry);
    }
    
    public void recordSearch(Duration duration) {
        searchCounter.increment();
        searchTimer.record(duration);
    }
}
```

### 3. Logging Configuration

#### Structured Logging with Logback

Create `logback-spring.xml`:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProfile name="dev">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <springProfile name="prod">
        <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
            <file>/var/log/library/application.log</file>
            <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
                <providers>
                    <timestamp/>
                    <logLevel/>
                    <loggerName/>
                    <message/>
                    <mdc/>
                    <stackTrace/>
                </providers>
            </encoder>
            <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
                <fileNamePattern>/var/log/library/application.%d{yyyy-MM-dd}.log</fileNamePattern>
                <maxHistory>30</maxHistory>
                <totalSizeCap>1GB</totalSizeCap>
            </rollingPolicy>
        </appender>
        <root level="INFO">
            <appender-ref ref="FILE"/>
        </root>
    </springProfile>
</configuration>
```

## Security Configuration

### 1. SSL/TLS Configuration

#### Self-Signed Certificate (Development)
```bash
# Generate keystore
keytool -genkeypair -alias library -keyalg RSA -keysize 2048 -storetype PKCS12 -keystore library.p12 -validity 3650
```

Add to `application-prod.yml`:
```yaml
server:
  ssl:
    key-store: classpath:library.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: library
  port: 8443
```

#### Let's Encrypt (Production)
```bash
# Using Certbot
certbot certonly --standalone -d api.yourdomain.com

# Convert to PKCS12
openssl pkcs12 -export -in /etc/letsencrypt/live/api.yourdomain.com/fullchain.pem \
  -inkey /etc/letsencrypt/live/api.yourdomain.com/privkey.pem \
  -out library.p12 -name library
```

### 2. Reverse Proxy Configuration

#### Nginx Configuration
```nginx
upstream library_backend {
    server localhost:8080;
}

server {
    listen 80;
    server_name api.yourdomain.com;
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name api.yourdomain.com;

    ssl_certificate /etc/letsencrypt/live/api.yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/api.yourdomain.com/privkey.pem;

    location / {
        proxy_pass http://library_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 30s;
        proxy_read_timeout 30s;
    }

    location /actuator/health {
        proxy_pass http://library_backend;
        access_log off;
    }
}
```

## Performance Optimization

### 1. JVM Tuning

#### Production JVM Options
```bash
java -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+UseContainerSupport \
     -XX:MaxRAMPercentage=75.0 \
     -XX:+HeapDumpOnOutOfMemoryError \
     -XX:HeapDumpPath=/tmp/heapdump.hprof \
     -Djava.security.egd=file:/dev/./urandom \
     -jar app.jar
```

#### Memory Analysis
```bash
# Monitor memory usage
jstat -gc -t $(pgrep java) 5s

# Generate heap dump
jmap -dump:format=b,file=heap.hprof $(pgrep java)

# Analyze with Eclipse MAT or VisualVM
```

### 2. Database Optimization

#### Connection Pool Tuning
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      leak-detection-threshold: 60000
```

#### Query Optimization
```sql
-- Analyze slow queries
SELECT query, mean_time, calls, total_time 
FROM pg_stat_statements 
ORDER BY mean_time DESC 
LIMIT 10;

-- Create missing indexes
CREATE INDEX CONCURRENTLY idx_books_search 
ON books USING gin(to_tsvector('english', title || ' ' || author));
```

## Troubleshooting

### Common Issues

#### 1. Application Won't Start
```bash
# Check Java version
java -version

# Check port availability
netstat -tulpn | grep :8080

# Check logs
tail -f /var/log/library/application.log
```

#### 2. Database Connection Issues
```bash
# Test database connectivity
pg_isready -h localhost -p 5432 -U library_user

# Check connection pool
curl http://localhost:8080/actuator/metrics/hikaricp.connections.active
```

#### 3. Memory Issues
```bash
# Monitor memory usage
ps aux | grep java

# Check heap usage
jstat -gc $(pgrep java)

# Generate heap dump for analysis
jmap -dump:live,format=b,file=heap.hprof $(pgrep java)
```

### Performance Troubleshooting

#### 1. Slow Response Times
```bash
# Check application metrics
curl http://localhost:8080/actuator/metrics/http.server.requests

# Monitor garbage collection
jstat -gc -t $(pgrep java) 1s

# Profile with async-profiler
java -jar async-profiler.jar -d 30 -f profile.html $(pgrep java)
```

#### 2. High CPU Usage
```bash
# Top threads by CPU
top -H -p $(pgrep java)

# Generate thread dump
jstack $(pgrep java) > thread_dump.txt

# Analyze thread dump
# Look for blocked threads or tight loops
```

### Recovery Procedures

#### 1. Database Recovery
```bash
# Backup before recovery
pg_dump -h localhost -U library_user library > backup.sql

# Restore from backup
psql -h localhost -U library_user library < backup.sql

# Run consistency checks
SELECT pg_stat_database.*, pg_database_size(pg_database.datname) 
FROM pg_stat_database 
JOIN pg_database ON pg_stat_database.datname = pg_database.datname;
```

#### 2. Application Recovery
```bash
# Graceful shutdown
kill -TERM $(pgrep java)

# Force restart if needed
kill -KILL $(pgrep java)

# Start application
nohup java -jar app.jar > /dev/null 2>&1 &

# Verify health
curl http://localhost:8080/actuator/health
```

---

This deployment guide provides comprehensive instructions for deploying the Dark Academia Library API across different environments with proper monitoring, security, and troubleshooting procedures.