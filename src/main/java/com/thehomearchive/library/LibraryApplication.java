package com.thehomearchive.library;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for The Home Archive Library Web Application
 * 
 * This Spring Boot application provides a dark academia-themed library management system
 * with features for book search, user authentication, and personal library management.
 */
@SpringBootApplication
public class LibraryApplication {

    public static void main(String[] args) {
        SpringApplication.run(LibraryApplication.class, args);
    }
}