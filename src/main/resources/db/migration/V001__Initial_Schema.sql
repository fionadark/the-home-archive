-- Dark Academia Library Web Application - Initial Database Schema
-- Migration: V001__Initial_Schema.sql
-- Date: 2025-10-22
-- Description: Creates all core tables for user management, authentication, and library functionality

-- ======================================================================
-- ENUMS (MySQL compatible)
-- ======================================================================

-- ======================================================================
-- CORE TABLES
-- ======================================================================

-- Users table - Core user authentication and profile information
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role ENUM('USER', 'ADMIN') NOT NULL DEFAULT 'USER',
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP NULL,
    
    INDEX idx_users_email (email),
    INDEX idx_users_role (role),
    INDEX idx_users_created_at (created_at)
);

-- User sessions table - JWT refresh token management
CREATE TABLE user_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    refresh_token VARCHAR(500) NOT NULL UNIQUE,
    expires_at TIMESTAMP NOT NULL,
    remember_me BOOLEAN NOT NULL DEFAULT FALSE,
    user_agent TEXT,
    ip_address VARCHAR(45),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_used_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_sessions_user_id (user_id),
    INDEX idx_user_sessions_token (refresh_token),
    INDEX idx_user_sessions_expires_at (expires_at)
);

-- Email verification table - Handles email verification tokens
CREATE TABLE email_verifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    token VARCHAR(255) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    verified_at TIMESTAMP NULL,
    type ENUM('REGISTRATION', 'EMAIL_CHANGE') NOT NULL DEFAULT 'REGISTRATION',
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_email_verifications_token (token),
    INDEX idx_email_verifications_user_id (user_id),
    INDEX idx_email_verifications_expires_at (expires_at)
);

-- Categories table - Book categorization
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    slug VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_categories_slug (slug),
    INDEX idx_categories_name (name)
);

-- Books table - Core book information
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    author VARCHAR(300) NOT NULL,
    isbn VARCHAR(20) UNIQUE,
    description TEXT,
    publication_year INT,
    publisher VARCHAR(200),
    page_count INT,
    category_id BIGINT,
    cover_image_url TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL,
    INDEX idx_books_title (title),
    INDEX idx_books_author (author),
    INDEX idx_books_isbn (isbn),
    INDEX idx_books_category_id (category_id),
    INDEX idx_books_publication_year (publication_year),
    FULLTEXT INDEX ft_books_search (title, author, description)
);

-- Personal library table - Links users to their books with additional metadata
CREATE TABLE personal_library (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    physical_location VARCHAR(200),
    reading_status ENUM('UNREAD', 'READING', 'READ', 'DNF') NOT NULL DEFAULT 'UNREAD',
    personal_notes TEXT,
    date_added TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    date_started TIMESTAMP NULL,
    date_completed TIMESTAMP NULL,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_book (user_id, book_id),
    INDEX idx_personal_library_user_id (user_id),
    INDEX idx_personal_library_book_id (book_id),
    INDEX idx_personal_library_status (reading_status),
    INDEX idx_personal_library_added (date_added)
);

-- Book ratings table - User ratings and reviews
CREATE TABLE book_ratings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    rating INT NOT NULL CHECK (rating >= 1 AND rating <= 5),
    review TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_book_rating (user_id, book_id),
    INDEX idx_book_ratings_user_id (user_id),
    INDEX idx_book_ratings_book_id (book_id),
    INDEX idx_book_ratings_rating (rating)
);

-- Search history table - Track user search queries for analytics
CREATE TABLE search_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL, -- NULL for anonymous searches
    query VARCHAR(500) NOT NULL,
    result_count INT NOT NULL DEFAULT 0,
    session_id VARCHAR(100), -- For anonymous tracking
    searched_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_search_history_user_id (user_id),
    INDEX idx_search_history_session_id (session_id),
    INDEX idx_search_history_searched_at (searched_at),
    INDEX idx_search_history_query (query)
);

-- ======================================================================
-- INITIAL DATA - Categories
-- ======================================================================

INSERT INTO categories (name, description, slug) VALUES
('Fiction', 'Literary fiction, novels, and fictional narratives', 'fiction'),
('Non-Fiction', 'Factual books including biographies, memoirs, and educational content', 'non-fiction'),
('Mystery & Thriller', 'Mystery novels, thrillers, and suspenseful fiction', 'mystery-thriller'),
('Science Fiction & Fantasy', 'Science fiction, fantasy, and speculative fiction', 'sci-fi-fantasy'),
('Romance', 'Romance novels and romantic fiction', 'romance'),
('Historical Fiction', 'Fiction set in historical periods', 'historical-fiction'),
('Biography & Autobiography', 'Life stories and memoirs', 'biography-autobiography'),
('History', 'Historical accounts and studies', 'history'),
('Philosophy', 'Philosophical works and theoretical discussions', 'philosophy'),
('Science & Nature', 'Scientific works, nature studies, and educational science books', 'science-nature'),
('Arts & Literature', 'Books about art, literary criticism, and cultural studies', 'arts-literature'),
('Self-Help & Development', 'Personal development and self-improvement books', 'self-help-development');

-- ======================================================================
-- DATABASE CONSTRAINTS & TRIGGERS
-- ======================================================================

-- Trigger to automatically update updated_at timestamp
DELIMITER $$
CREATE TRIGGER update_users_timestamp 
    BEFORE UPDATE ON users 
    FOR EACH ROW 
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$

CREATE TRIGGER update_books_timestamp 
    BEFORE UPDATE ON books 
    FOR EACH ROW 
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$

CREATE TRIGGER update_book_ratings_timestamp 
    BEFORE UPDATE ON book_ratings 
    FOR EACH ROW 
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END$$
DELIMITER ;

-- ======================================================================
-- PERFORMANCE INDEXES FOR COMMON QUERIES
-- ======================================================================

-- Composite indexes for common search patterns
CREATE INDEX idx_books_category_year ON books(category_id, publication_year);
CREATE INDEX idx_personal_library_user_status ON personal_library(user_id, reading_status);
CREATE INDEX idx_book_ratings_book_rating ON book_ratings(book_id, rating);

-- ======================================================================
-- CLEANUP PROCEDURES
-- ======================================================================

-- Cleanup expired sessions (to be run periodically)
DELIMITER $$
CREATE PROCEDURE CleanupExpiredSessions()
BEGIN
    DELETE FROM user_sessions WHERE expires_at < NOW();
    DELETE FROM email_verifications WHERE expires_at < NOW() AND verified_at IS NULL;
END$$
DELIMITER ;