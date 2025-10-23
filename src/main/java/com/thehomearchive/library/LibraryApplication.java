package com.thehomearchive.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Main application class for The Home Archive Library Web Application
 * 
 * This Spring Boot application provides a dark academia-themed library management system
 * with features for book search, user authentication, and personal library management.
 * 
 * Features enabled:
 * - JPA Repositories with auditing
 * - Method-level security
 * - Async processing
 * - Caching
 * - Transaction management
 * - Configuration properties binding
 */
@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.thehomearchive.library.repository")
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true)
@EnableAsync
@EnableCaching
@EnableTransactionManagement
@EnableConfigurationProperties
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }
}