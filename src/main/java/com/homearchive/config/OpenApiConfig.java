package com.homearchive.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI/Swagger configuration for the Home Archive application.
 * Provides comprehensive API documentation with detailed endpoints, schemas, and examples.
 * Required by constitution for API documentation.
 */
@Configuration
public class OpenApiConfig {
    
    @Value("${server.port:8080}")
    private int serverPort;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;
    
    @Bean
    public OpenAPI homeArchiveOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Home Archive API")
                        .description("""
                                The Home Archive API provides comprehensive book search and management capabilities 
                                for personal book collections. This RESTful API supports multi-word search, 
                                advanced filtering, and physical location tracking for book organization.
                                
                                ## Features
                                - **Multi-word Search**: Find books using multiple keywords across titles, authors, and genres
                                - **Advanced Filtering**: Filter by publication year, genre, and physical location
                                - **Fuzzy Matching**: Intelligent search with typo tolerance and partial matching
                                - **Performance Optimized**: Cached results and database indexing for fast responses
                                - **Production Ready**: Comprehensive monitoring, security headers, and error handling
                                
                                ## Search Capabilities
                                - Title search with intelligent matching
                                - Author search with partial name matching
                                - Genre-based filtering
                                - Publication year filtering
                                - Physical location search (room/shelf)
                                - Combined multi-criteria search
                                
                                ## Performance
                                - Response time target: <200ms for typical searches
                                - Support for 10,000+ book collections
                                - Intelligent caching with TTL management
                                - Database query optimization
                                
                                ## Authentication
                                Currently using basic authentication for administrative endpoints.
                                Public search endpoints are available without authentication.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Home Archive Team")
                                .email("support@homearchive.com")
                                .url("https://github.com/homearchive/home-archive"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://choosealicense.com/licenses/mit/")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort + contextPath)
                                .description("Development server"),
                        new Server()
                                .url("https://api.homearchive.com")
                                .description("Production server (when deployed)")
                ))
                .tags(List.of(
                        new Tag()
                                .name("Book Search")
                                .description("Search and discovery operations for books in the collection. " +
                                           "Supports multi-word queries, fuzzy matching, and relevance-based ranking."),
                        new Tag()
                                .name("Book Management")
                                .description("CRUD operations for managing books in the collection. " +
                                           "Includes adding, updating, and organizing books by location."),
                        new Tag()
                                .name("Health & Monitoring")
                                .description("Application health checks, metrics, and system status monitoring. " +
                                           "Includes database connectivity and performance metrics."),
                        new Tag()
                                .name("Database Performance")
                                .description("Database query performance metrics, slow query detection, " +
                                           "and connection pool monitoring for production optimization.")
                ));
    }
}