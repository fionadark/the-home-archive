-- Create books table with full-text search support
CREATE TABLE books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    genre VARCHAR(100),
    isbn VARCHAR(20),
    publisher VARCHAR(255),
    publication_year INT,
    description TEXT,
    page_count INT,
    physical_location VARCHAR(100),
    reading_status VARCHAR(20),
    personal_rating INT,
    date_added DATETIME NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_title_author ON books(title, author);
CREATE INDEX idx_genre ON books(genre);
CREATE INDEX idx_publication_year ON books(publication_year);
CREATE INDEX idx_date_added ON books(date_added);
CREATE INDEX idx_reading_status ON books(reading_status);

-- Create full-text index for search functionality
CREATE FULLTEXT INDEX idx_fulltext_search ON books(title, author, description);
CREATE FULLTEXT INDEX idx_fulltext_metadata ON books(genre, publisher);

-- Add constraints
ALTER TABLE books ADD CONSTRAINT chk_personal_rating CHECK (personal_rating >= 1 AND personal_rating <= 5);
ALTER TABLE books ADD CONSTRAINT chk_publication_year CHECK (publication_year >= 1000 AND publication_year <= 2100);
ALTER TABLE books ADD CONSTRAINT chk_page_count CHECK (page_count > 0);
ALTER TABLE books ADD CONSTRAINT chk_reading_status CHECK (reading_status IN ('NOT_READ', 'READING', 'COMPLETED'));