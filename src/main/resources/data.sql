-- Dark Academia Library Web Application - Sample Data Seeding Script
-- File: data.sql
-- Date: 2025-10-30
-- Description: Provides sample data for development and testing environments
-- Note: This data is loaded automatically by Spring Boot in development environments

-- ======================================================================
-- SAMPLE DATA CONFIGURATION
-- ======================================================================
-- This script provides realistic sample data for:
-- 1. Users (with different roles and verification status)
-- 2. Books (diverse collection across categories)
-- 3. Personal libraries (reading history and status)
-- 4. Book ratings and reviews
-- 5. Search history (user behavior simulation)
-- 6. User sessions (authentication state)

-- ======================================================================
-- SAMPLE USERS
-- ======================================================================
-- Note: All passwords are hashed version of 'password123' using BCrypt
-- In production, these would be different and more secure

INSERT INTO users (email, password_hash, first_name, last_name, role, email_verified, created_at, updated_at, last_login_at) VALUES
-- Admin user
('admin@thehomearchive.com', '$2a$10$XCpXul8t4A.XocjOxg3C5.4m/8zcPp/ua4oukVOXpe10P3ZCQYOp.', 'Library', 'Administrator', 'ADMIN', TRUE, '2024-01-15 10:00:00', '2024-01-15 10:00:00', '2024-12-20 09:30:00'),

-- Regular users with various reading preferences
('alice.scholar@university.edu', '$2a$10$XCpXul8t4A.XocjOxg3C5.4m/8zcPp/ua4oukVOXpe10P3ZCQYOp.', 'Alice', 'Scholar', 'USER', TRUE, '2024-02-01 14:30:00', '2024-02-01 14:30:00', '2024-12-19 18:45:00'),
('benjamin.reader@literature.org', '$2a$10$XCpXul8t4A.XocjOxg3C5.4m/8zcPp/ua4oukVOXpe10P3ZCQYOp.', 'Benjamin', 'Reader', 'USER', TRUE, '2024-02-15 09:15:00', '2024-02-15 09:15:00', '2024-12-18 20:15:00'),
('charlotte.novelist@writers.com', '$2a$10$XCpXul8t4A.XocjOxg3C5.4m/8zcPp/ua4oukVOXpe10P3ZCQYOp.', 'Charlotte', 'Novelist', 'USER', TRUE, '2024-03-01 16:20:00', '2024-03-01 16:20:00', '2024-12-17 15:30:00'),
('david.historian@academy.edu', '$2a$10$XCpXul8t4A.XocjOxg3C5.4m/8zcPp/ua4oukVOXpe10P3ZCQYOp.', 'David', 'Historian', 'USER', TRUE, '2024-03-10 11:45:00', '2024-03-10 11:45:00', '2024-12-16 12:00:00'),
('emily.philosopher@classics.org', '$2a$10$XCpXul8t4A.XocjOxg3C5.4m/8zcPp/ua4oukVOXpe10P3ZCQYOp.', 'Emily', 'Philosopher', 'USER', TRUE, '2024-04-01 08:30:00', '2024-04-01 08:30:00', '2024-12-15 19:20:00'),
('frank.scientist@research.edu', '$2a$10$XCpXul8t4A.XocjOxg3C5.4m/8zcPp/ua4oukVOXpe10P3ZCQYOp.', 'Frank', 'Scientist', 'USER', TRUE, '2024-04-15 13:00:00', '2024-04-15 13:00:00', '2024-12-14 10:45:00'),

-- New user (not yet verified)
('grace.newuser@student.edu', '$2a$10$XCpXul8t4A.XocjOxg3C5.4m/8zcPp/ua4oukVOXpe10P3ZCQYOp.', 'Grace', 'Newuser', 'USER', FALSE, '2024-12-20 08:00:00', '2024-12-20 08:00:00', NULL);

-- ======================================================================
-- SAMPLE CATEGORIES (Required before books)
-- ======================================================================

INSERT INTO categories (name, description, slug, created_at) VALUES
('Fiction', 'Literary fiction, novels, and fictional narratives', 'fiction', '2024-01-15 09:00:00'),
('Non-Fiction', 'Factual books including biographies, memoirs, and educational content', 'non-fiction', '2024-01-15 09:05:00'),
('Mystery & Thriller', 'Mystery novels, thrillers, and suspenseful fiction', 'mystery-thriller', '2024-01-15 09:10:00'),
('Science Fiction & Fantasy', 'Science fiction, fantasy, and speculative fiction', 'sci-fi-fantasy', '2024-01-15 09:15:00'),
('Romance', 'Romance novels and romantic fiction', 'romance', '2024-01-15 09:20:00'),
('Historical Fiction', 'Fiction set in historical periods', 'historical-fiction', '2024-01-15 09:25:00'),
('Biography & Autobiography', 'Life stories and memoirs', 'biography-autobiography', '2024-01-15 09:30:00'),
('History', 'Historical accounts and studies', 'history', '2024-01-15 09:35:00'),
('Philosophy', 'Philosophical works and theoretical discussions', 'philosophy', '2024-01-15 09:40:00'),
('Science & Nature', 'Scientific works, nature studies, and educational science books', 'science-nature', '2024-01-15 09:45:00'),
('Arts & Literature', 'Books about art, literary criticism, and cultural studies', 'arts-literature', '2024-01-15 09:50:00'),
('Self-Help & Development', 'Personal development and self-improvement books', 'self-help-development', '2024-01-15 09:55:00');

-- ======================================================================
-- SAMPLE BOOKS - CURATED DARK ACADEMIA COLLECTION  
-- ======================================================================
-- Note: Using categories: Fiction=1, Mystery & Thriller=3, Science Fiction & Fantasy=4, 
--       Historical Fiction=6, Biography & Autobiography=7, History=8, Philosophy=9, 
--       Science & Nature=10, Arts & Literature=11

INSERT INTO books (title, author, isbn, description, publication_year, publisher, page_count, category_id, cover_image_url, created_at, updated_at) VALUES

('If We Were Villains', 'M.L. Rio', '9781250095282', 'At an elite arts college, seven young actors studying Shakespeare become entangled in a web of obsession, desire, and murder. A gripping dark academia thriller about friendship, ambition, and the price of art.', 2017, 'Flatiron Books', 368, 1, 'https://images.example.com/if-we-were-villains.jpg', '2024-01-15 10:30:00', '2024-01-15 10:30:00'),

('The Name of the Rose', 'Umberto Eco', '9780156001311', 'Set in a medieval monastery, this intellectual mystery follows Brother William of Baskerville as he investigates a series of murders. A complex work blending philosophy, theology, and detective fiction.', 1980, 'Harcourt', 536, 1, 'https://images.example.com/name-of-rose.jpg', '2024-01-15 10:45:00', '2024-01-15 10:45:00'),

('Babel', 'R.F. Kuang', '9780062976888', 'A dark academic fantasy set in 1830s Oxford, exploring colonialism and the power of language through the story of Robin Swift, a Chinese boy studying at the Royal Institute of Translation.', 2022, 'Harper Voyager', 560, 4, 'https://images.example.com/babel.jpg', '2024-01-15 11:00:00', '2024-01-15 11:00:00'),

  ('The Picture of Dorian Gray', 'Oscar Wilde', '978-0486278070', 'Wilde''s only novel is the portrait of a youth whose features, year after year, retain the same appearance of innocence while the shame of his hideous vices becomes mirrored on the features of his portrait.', 1890, 'Dover Publications', 272, 1, 'https://example.com/covers/dorian-gray.jpg', '2024-01-15 10:00:00', '2024-01-15 10:00:00'),

-- Mystery & Thriller
('The Thursday Murder Club', 'Richard Osman', '9780525559474', 'Four unlikely friends meet weekly to investigate cold cases. When a real murder occurs in their retirement community, they find themselves in the middle of their first live case.', 2020, 'Pamela Dorman Books', 352, 3, 'https://images.example.com/thursday-murder-club.jpg', '2024-01-15 11:15:00', '2024-01-15 11:15:00'),

('The Seven Husbands of Evelyn Hugo', 'Taylor Jenkins Reid', '9781501161933', 'Reclusive Hollywood icon Evelyn Hugo finally decides to tell her life story, but only to unknown journalist Monique Grant. A captivating novel about ambition, love, and the price of fame.', 2017, 'Atria Books', 400, 1, 'https://images.example.com/evelyn-hugo.jpg', '2024-01-15 11:30:00', '2024-01-15 11:30:00'),

-- Historical Fiction
('The Invisible Bridge', 'Julie Orringer', '9780375414596', 'An epic novel following Andras Lévi, a Hungarian-Jewish architecture student who falls in love in 1930s Paris before being swept into the horrors of World War II.', 2010, 'Knopf', 623, 6, 'https://images.example.com/invisible-bridge.jpg', '2024-01-15 11:45:00', '2024-01-15 11:45:00'),

('All the Light We Cannot See', 'Anthony Doerr', '9781476746586', 'The story of a blind French girl and a German boy whose paths collide in occupied France during World War II. A beautifully written tale of human resilience and connection.', 2014, 'Scribner', 531, 6, 'https://images.example.com/all-light.jpg', '2024-01-15 12:00:00', '2024-01-15 12:00:00'),

-- Philosophy & Non-Fiction
('Meditations', 'Marcus Aurelius', '9780140449334', 'The personal writings of the Roman Emperor Marcus Aurelius, offering insights into Stoic philosophy and practical wisdom for daily life. A cornerstone of philosophical literature.', 1900, 'Penguin Classics', 254, 9, 'https://images.example.com/meditations.jpg', '2024-01-15 12:15:00', '2024-01-15 12:15:00'),

('The Myth of Sisyphus', 'Albert Camus', '9780525564454', 'Camus'' philosophical essay exploring the concept of the absurd and arguing that the struggle itself toward the heights is enough to fill a man''s heart.', 1942, 'Vintage', 212, 9, 'https://images.example.com/myth-sisyphus.jpg', '2024-01-15 12:30:00', '2024-01-15 12:30:00'),

('Sapiens: A Brief History of Humankind', 'Yuval Noah Harari', '9780062316097', 'A compelling account of how Homo sapiens conquered the world through cognitive, agricultural, and scientific revolutions. An accessible exploration of human history and future.', 2014, 'Harper', 464, 8, 'https://images.example.com/sapiens.jpg', '2024-01-15 12:45:00', '2024-01-15 12:45:00'),

-- Science & Nature
('The Origin of Species', 'Charles Darwin', '9780140436310', 'Darwin''s groundbreaking work introducing the theory of evolution by natural selection. A foundational text in biology and one of the most influential books in science.', 1859, 'Penguin Classics', 432, 10, 'https://images.example.com/origin-species.jpg', '2024-01-15 13:00:00', '2024-01-15 13:00:00'),

('Silent Spring', 'Rachel Carson', '9780547628462', 'Carson''s influential environmental science book that launched the modern environmental movement by documenting the harmful effects of pesticides on ecosystems.', 1962, 'Mariner Books', 378, 10, 'https://images.example.com/silent-spring.jpg', '2024-01-15 13:15:00', '2024-01-15 13:15:00'),

-- Arts & Literature
('Ways of Seeing', 'John Berger', '9780140135152', 'A influential work of art criticism that changed the way we understand visual culture. Berger challenges traditional Western cultural perspectives on art and imagery.', 1972, 'Penguin Books', 176, 11, 'https://images.example.com/ways-seeing.jpg', '2024-01-15 13:30:00', '2024-01-15 13:30:00'),

('The Death of Ivan Ilyich', 'Leo Tolstoy', '9780486406022', 'Tolstoy''s masterful novella about a judge who confronts the meaning of life while facing death. A profound meditation on mortality and authentic living.', 1886, 'Dover Publications', 96, 1, 'https://images.example.com/ivan-ilyich.jpg', '2024-01-15 13:45:00', '2024-01-15 13:45:00'),

-- Biography & Autobiography
('The Diary of a Young Girl', 'Anne Frank', '9780553577129', 'The poignant diary of Anne Frank, a Jewish girl hiding from the Nazis in Amsterdam. A powerful testament to the human spirit and the horrors of the Holocaust.', 1947, 'Bantam', 283, 7, 'https://images.example.com/anne-frank.jpg', '2024-01-15 14:00:00', '2024-01-15 14:00:00'),

('Long Walk to Freedom', 'Nelson Mandela', '9780316548182', 'Mandela''s autobiography chronicling his journey from rural childhood to becoming South Africa''s first Black president. An inspiring story of resilience and social justice.', 1994, 'Little, Brown and Company', 656, 7, 'https://images.example.com/long-walk.jpg', '2024-01-15 14:15:00', '2024-01-15 14:15:00'),

-- Science Fiction & Fantasy
('Dune', 'Frank Herbert', '9780441172719', 'An epic science fiction novel set on the desert planet Arrakis, following Paul Atreides as he becomes embroiled in a struggle for control of the universe''s most valuable resource.', 1965, 'Ace Books', 688, 4, 'https://images.example.com/dune.jpg', '2024-01-15 14:30:00', '2024-01-15 14:30:00'),

('The Left Hand of Darkness', 'Ursula K. Le Guin', '9780441478125', 'A groundbreaking science fiction novel exploring gender, politics, and human nature through the story of an envoy on a planet where people are ambisexual.', 1969, 'Ace Books', 304, 4, 'https://images.example.com/left-hand-darkness.jpg', '2024-01-15 14:45:00', '2024-01-15 14:45:00');

-- ======================================================================
-- PERSONAL LIBRARY ENTRIES
-- ======================================================================
-- Simulate realistic reading behaviors and library management

-- Alice Scholar's Library (Literature focus)
INSERT INTO personal_library (user_id, book_id, physical_location, reading_status, personal_notes, date_added, date_started, date_completed) VALUES
(2, 1, 'Bedroom Bookshelf A1', 'READ', 'Absolutely mesmerizing. The way Tartt builds tension throughout the story is masterful. One of my all-time favorites.', '2024-02-05 10:30:00', '2024-02-05 19:00:00', '2024-02-18 22:30:00'),
(2, 3, 'Living Room Coffee Table', 'READING', 'Dense but fascinating. Taking notes on the philosophical discussions. About halfway through.', '2024-11-01 14:20:00', '2024-11-15 09:00:00', NULL),
(2, 5, 'Study Desk', 'READ', 'Wilde''s wit and observations about society are timeless. The ending still gives me chills.', '2024-03-10 11:00:00', '2024-03-12 20:00:00', '2024-03-20 16:45:00'),
(2, 10, 'Philosophy Section', 'READ', 'Essential Stoic text. I return to certain passages regularly for guidance and reflection.', '2024-04-01 09:30:00', '2024-04-05 07:00:00', '2024-04-25 18:00:00');

-- Benjamin Reader's Library (Mystery and Philosophy mix)
INSERT INTO personal_library (user_id, book_id, physical_location, reading_status, personal_notes, date_added, date_started, date_completed) VALUES
(3, 2, 'Mystery Section', 'READ', 'Rio captures the intensity of drama school perfectly. The Shakespeare references add wonderful depth.', '2024-03-01 16:00:00', '2024-03-01 20:00:00', '2024-03-08 21:15:00'),
(3, 6, 'Nightstand', 'READ', 'Clever premise and engaging characters. Looking forward to the next book in the series.', '2024-06-15 12:30:00', '2024-06-20 19:30:00', '2024-06-28 14:20:00'),
(3, 11, 'Philosophy Corner', 'READING', 'Powerful and thought-provoking. Camus'' writing style is both accessible and profound.', '2024-12-01 10:00:00', '2024-12-10 08:30:00', NULL),
(3, 13, 'Science Section', 'UNREAD', 'Heard great things about this classic. Planning to read during winter break.', '2024-11-20 15:45:00', NULL, NULL);

-- Charlotte Novelist's Library (Contemporary fiction focus)
INSERT INTO personal_library (user_id, book_id, physical_location, reading_status, personal_notes, date_added, date_started, date_completed) VALUES
(4, 4, 'Writing Desk', 'READ', 'Kuang''s exploration of language and power is brilliant. The academic setting feels so authentic.', '2024-08-01 11:30:00', '2024-08-05 19:00:00', '2024-08-22 20:45:00'),
(4, 7, 'Reading Nook', 'READ', 'Beautifully written and emotionally complex. Jenkins Reid is a master storyteller.', '2024-09-10 14:00:00', '2024-09-12 21:00:00', '2024-09-18 23:30:00'),
(4, 9, 'Historical Fiction Shelf', 'READ', 'Heartbreaking and beautiful. Doerr''s prose is absolutely stunning.', '2024-05-15 09:45:00', '2024-05-20 18:00:00', '2024-06-05 22:00:00'),
(4, 16, 'Inspiration Shelf', 'READ', 'Anne''s voice is so vivid and hopeful despite the circumstances. A truly important work.', '2024-07-01 13:20:00', '2024-07-03 16:00:00', '2024-07-10 19:30:00');

-- David Historian's Library (Historical focus)
INSERT INTO personal_library (user_id, book_id, physical_location, reading_status, personal_notes, date_added, date_started, date_completed) VALUES
(5, 8, 'Historical Fiction Collection', 'READ', 'Epic and emotionally devastating. Orringer''s research and character development are exceptional.', '2024-06-01 10:15:00', '2024-06-10 07:30:00', '2024-07-15 21:45:00'),
(5, 12, 'History Desk', 'READ', 'Harari makes complex historical concepts accessible. Changed how I think about human development.', '2024-04-20 15:30:00', '2024-05-01 19:00:00', '2024-05-28 16:20:00'),
(5, 17, 'Biography Section', 'READ', 'Mandela''s journey is truly inspiring. A testament to the power of perseverance and forgiveness.', '2024-08-15 11:00:00', '2024-08-20 20:00:00', '2024-09-10 18:30:00'),
(5, 9, 'WWII Collection', 'READ', 'The parallel stories are woven together beautifully. One of the best WWII novels I''ve read.', '2024-10-01 12:30:00', '2024-10-05 19:30:00', '2024-10-20 22:00:00');

-- Emily Philosopher's Library (Philosophy and classics)
INSERT INTO personal_library (user_id, book_id, physical_location, reading_status, personal_notes, date_added, date_started, date_completed) VALUES
(6, 10, 'Philosophy Primary Shelf', 'READ', 'A cornerstone of Stoic philosophy. Marcus Aurelius'' insights remain relevant after nearly 2000 years.', '2024-03-15 08:00:00', '2024-03-20 06:30:00', '2024-04-10 17:45:00'),
(6, 11, 'Existentialism Section', 'READ', 'Camus'' exploration of absurdism is both challenging and liberating. A must-read for philosophy students.', '2024-05-01 14:30:00', '2024-05-10 09:00:00', '2024-05-25 20:15:00'),
(6, 15, 'Art Theory Shelf', 'READ', 'Revolutionary perspective on visual culture. Berger challenges conventional art criticism effectively.', '2024-07-20 16:45:00', '2024-07-25 10:30:00', '2024-08-05 14:20:00'),
(6, 16, 'Russian Literature', 'READ', 'Tolstoy''s meditation on death and meaning is profound. Short but incredibly impactful.', '2024-09-01 11:15:00', '2024-09-05 18:00:00', '2024-09-08 21:30:00');

-- Frank Scientist's Library (Science and biography)
INSERT INTO personal_library (user_id, book_id, physical_location, reading_status, personal_notes, date_added, date_started, date_completed) VALUES
(7, 13, 'Science Classics', 'READ', 'Foundational work in biology. Darwin''s clear reasoning and evidence presentation is exemplary.', '2024-05-10 09:30:00', '2024-05-15 07:00:00', '2024-06-01 19:45:00'),
(7, 14, 'Environmental Science', 'READ', 'Carson''s work sparked the environmental movement. Her writing combines scientific rigor with poetic beauty.', '2024-06-20 13:45:00', '2024-06-25 17:30:00', '2024-07-08 16:00:00'),
(7, 12, 'Popular Science', 'READ', 'Excellent overview of human history from a scientific perspective. Harari makes complex ideas accessible.', '2024-08-01 10:20:00', '2024-08-10 19:15:00', '2024-08-30 21:00:00'),
(7, 18, 'Science Fiction', 'READING', 'Classic sci-fi that explores ecology and politics. Herbert''s world-building is incredibly detailed.', '2024-11-15 14:30:00', '2024-11-20 20:00:00', NULL);

-- ======================================================================
-- BOOK RATINGS AND REVIEWS
-- ======================================================================
-- Realistic ratings and detailed reviews from different perspectives

INSERT INTO book_ratings (user_id, book_id, rating, review, created_at, updated_at) VALUES
-- Alice Scholar's ratings
(2, 1, 5, 'A masterpiece of contemporary literature. Tartt''s prose is rich and immersive, and the characters are unforgettably complex. The way she explores themes of beauty, morality, and the corruption of innocence is brilliant. This book stayed with me long after I finished it.', '2024-02-20 10:30:00', '2024-02-20 10:30:00'),
(2, 5, 5, 'Wilde''s only novel is a perfect blend of wit, philosophy, and gothic horror. The dialogue sparkles with his characteristic humor, while the underlying themes about aestheticism and moral decay are deeply thought-provoking. A timeless classic.', '2024-03-22 16:45:00', '2024-03-22 16:45:00'),
(2, 10, 4, 'Essential reading for anyone interested in Stoic philosophy. Marcus Aurelius writes with honesty and humility about the human condition. Some passages require multiple readings to fully appreciate, but the wisdom is invaluable.', '2024-04-27 18:00:00', '2024-04-27 18:00:00'),

-- Benjamin Reader's ratings
(3, 2, 4, 'Rio captures the intensity and competitiveness of drama school perfectly. The Shakespeare references add wonderful depth to the story, and the mystery keeps you guessing until the end. Well-crafted characters and atmospheric setting.', '2024-03-10 21:15:00', '2024-03-10 21:15:00'),
(3, 6, 4, 'Clever premise and engaging characters. Osman writes with humor and warmth while still delivering a solid mystery. The elderly protagonists are refreshing and their friendship is heartwarming. Looking forward to the sequel.', '2024-06-30 14:20:00', '2024-06-30 14:20:00'),

-- Charlotte Novelist's ratings
(4, 4, 5, 'Kuang''s exploration of colonialism through the lens of translation and language is brilliant. The academic setting feels authentic, and the magical realism elements enhance rather than overshadow the serious themes. Beautifully written and important.', '2024-08-25 20:45:00', '2024-08-25 20:45:00'),
(4, 7, 5, 'Jenkins Reid has crafted a compelling story about ambition, love, and the price of fame. Evelyn Hugo is a complex, fascinating character, and the mystery of why she chose Monique unfolds perfectly. Couldn''t put it down.', '2024-09-20 23:30:00', '2024-09-20 23:30:00'),
(4, 9, 5, 'Absolutely stunning prose. Doerr weaves together two parallel stories with incredible skill, creating a narrative that is both heartbreaking and beautiful. The way he writes about light and perception is particularly moving.', '2024-06-07 22:00:00', '2024-06-07 22:00:00'),

-- David Historian's ratings
(5, 8, 5, 'An epic novel that does justice to its historical setting. Orringer''s research is meticulous, and her character development is exceptional. The way she portrays the slide from normalcy into horror is both subtle and devastating.', '2024-07-17 21:45:00', '2024-07-17 21:45:00'),
(5, 12, 4, 'Harari makes complex historical and anthropological concepts accessible to general readers. His perspective on human development is thought-provoking, though some conclusions could be better supported. Still, an engaging and educational read.', '2024-05-30 16:20:00', '2024-05-30 16:20:00'),
(5, 17, 5, 'Mandela''s autobiography is both inspiring and educational. His journey from rural childhood to presidency is remarkable, and his reflections on forgiveness and reconciliation are particularly powerful. Essential reading for understanding 20th-century history.', '2024-09-12 18:30:00', '2024-09-12 18:30:00'),

-- Emily Philosopher's ratings
(6, 10, 5, 'A cornerstone of Stoic philosophy that remains remarkably relevant. Marcus Aurelius writes with humility and practical wisdom about duty, virtue, and accepting what we cannot control. I return to this book regularly for guidance.', '2024-04-12 17:45:00', '2024-04-12 17:45:00'),
(6, 11, 4, 'Camus'' exploration of the absurd is both challenging and liberating. His argument about finding meaning through struggle rather than hope is thought-provoking. The writing is clear and accessible despite the complex philosophical concepts.', '2024-05-27 20:15:00', '2024-05-27 20:15:00'),
(6, 15, 4, 'Revolutionary perspective on visual culture and art criticism. Berger effectively challenges the traditional Western approach to viewing art. Some arguments could be stronger, but overall a important and influential work.', '2024-08-07 14:20:00', '2024-08-07 14:20:00'),

-- Frank Scientist's ratings
(7, 13, 5, 'Foundational work in biology that changed our understanding of life itself. Darwin''s clear reasoning and methodical presentation of evidence is exemplary scientific writing. Still relevant and readable today.', '2024-06-03 19:45:00', '2024-06-03 19:45:00'),
(7, 14, 5, 'Carson''s work is both scientifically rigorous and beautifully written. Her ability to explain complex ecological concepts while maintaining poetic beauty is remarkable. This book truly launched the modern environmental movement.', '2024-07-10 16:00:00', '2024-07-10 16:00:00'),
(7, 12, 4, 'Excellent synthesis of human history from scientific and anthropological perspectives. Harari''s writing is engaging and accessible. Some generalizations are broad, but overall provides valuable insights into human development.', '2024-09-02 21:00:00', '2024-09-02 21:00:00');

-- ======================================================================
-- SEARCH HISTORY
-- ======================================================================
-- Simulate realistic user search patterns and interests

INSERT INTO search_history (user_id, query, result_count, searched_at) VALUES
-- Alice Scholar's searches (literature focus)
(2, 'Donna Tartt', 1, '2024-02-01 09:15:00'),
(2, 'dark academia novels', 3, '2024-02-01 09:30:00'),
(2, 'Oscar Wilde philosophy', 2, '2024-03-05 14:20:00'),
(2, 'Victorian literature', 5, '2024-03-05 14:25:00'),
(2, 'Marcus Aurelius Stoicism', 1, '2024-03-28 11:00:00'),
(2, 'philosophical fiction', 8, '2024-10-15 16:30:00'),
(2, 'Umberto Eco', 1, '2024-11-25 10:45:00'),

-- Benjamin Reader's searches (mystery and philosophy)
(3, 'mystery novels college setting', 2, '2024-02-28 19:45:00'),
(3, 'Shakespeare references fiction', 3, '2024-02-28 20:00:00'),
(3, 'cozy mystery series', 4, '2024-06-10 13:15:00'),
(3, 'existentialism literature', 6, '2024-11-28 09:30:00'),
(3, 'Albert Camus philosophy', 1, '2024-11-28 09:45:00'),
(3, 'Charles Darwin evolution', 1, '2024-12-01 15:20:00'),

-- Charlotte Novelist's searches (contemporary fiction)
(4, 'R.F. Kuang Babel', 1, '2024-07-25 12:30:00'),
(4, 'language power fiction', 4, '2024-07-25 12:45:00'),
(4, 'Taylor Jenkins Reid', 1, '2024-09-05 10:15:00'),
(4, 'Hollywood fiction novels', 7, '2024-09-05 10:30:00'),
(4, 'Anthony Doerr writing style', 2, '2024-05-10 20:15:00'),
(4, 'World War II fiction', 12, '2024-05-10 20:30:00'),
(4, 'Anne Frank diary', 1, '2024-06-28 11:00:00'),

-- David Historian's searches (historical interests)
(5, 'Julie Orringer historical fiction', 1, '2024-05-25 14:30:00'),
(5, 'World War II Hungary', 3, '2024-05-25 14:45:00'),
(5, 'Yuval Noah Harari', 1, '2024-04-15 16:00:00'),
(5, 'human evolution history', 8, '2024-04-15 16:15:00'),
(5, 'Nelson Mandela biography', 1, '2024-08-10 13:20:00'),
(5, 'South African history', 6, '2024-08-10 13:35:00'),
(5, 'Holocaust literature', 9, '2024-10-20 17:45:00'),

-- Emily Philosopher's searches (philosophy focus)
(6, 'Stoic philosophy modern', 5, '2024-03-10 07:30:00'),
(6, 'Marcus Aurelius Meditations', 1, '2024-03-10 07:45:00'),
(6, 'existentialism absurdism', 7, '2024-04-28 16:20:00'),
(6, 'John Berger art criticism', 1, '2024-07-15 12:15:00'),
(6, 'visual culture theory', 4, '2024-07-15 12:30:00'),
(6, 'Russian literature philosophy', 11, '2024-08-25 18:45:00'),
(6, 'Leo Tolstoy short works', 3, '2024-08-25 19:00:00'),

-- Frank Scientist's searches (science focus)
(7, 'Darwin Origin of Species', 1, '2024-05-05 08:45:00'),
(7, 'evolution theory history', 6, '2024-05-05 09:00:00'),
(7, 'Rachel Carson environmental', 1, '2024-06-15 14:30:00'),
(7, 'Silent Spring impact', 2, '2024-06-15 14:45:00'),
(7, 'scientific writing classics', 8, '2024-07-30 11:15:00'),
(7, 'Frank Herbert Dune ecology', 1, '2024-11-10 19:30:00'),
(7, 'science fiction environmental themes', 5, '2024-11-10 19:45:00'),

-- Anonymous searches (session-based)
(NULL, 'best books 2024', 15, '2024-12-15 14:20:00'),
(NULL, 'dark academia aesthetic', 8, '2024-12-16 16:45:00'),
(NULL, 'philosophy for beginners', 12, '2024-12-17 10:30:00'),
(NULL, 'mystery novels recommendations', 18, '2024-12-18 20:15:00'),
(NULL, 'classic literature must read', 25, '2024-12-19 13:45:00');

-- ======================================================================
-- USER SESSIONS
-- ======================================================================
-- Simulate active and recent user sessions for authentication state

INSERT INTO user_sessions (user_id, session_token, refresh_token, expiry_date, refresh_expiry_date, active, user_agent, ip_address, created_at, updated_at, last_accessed_at) VALUES
-- Admin active session
(1, 'session_token_admin_001_secure_random_string_here', 'refresh_token_admin_001_secure_random_string_here', '2025-01-30 09:30:00', '2025-01-30 09:30:00', TRUE, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', '192.168.1.100', '2024-12-20 09:30:00', '2024-12-20 09:30:00', '2024-12-20 15:45:00'),

-- Alice Scholar active sessions (multiple devices)
(2, 'session_token_alice_001_secure_random_string_here', 'refresh_token_alice_001_secure_random_string_here', '2025-01-19 18:45:00', '2025-01-19 18:45:00', TRUE, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', '192.168.1.101', '2024-12-19 18:45:00', '2024-12-19 18:45:00', '2024-12-20 14:30:00'),
(2, 'session_token_alice_002_secure_random_string_here', 'refresh_token_alice_002_secure_random_string_here', '2024-12-21 08:15:00', '2024-12-21 08:15:00', TRUE, 'Mozilla/5.0 (iPhone; CPU iPhone OS 17_0 like Mac OS X) AppleWebKit/605.1.15', '192.168.1.102', '2024-12-20 08:15:00', '2024-12-20 08:15:00', '2024-12-20 12:20:00'),

-- Benjamin Reader active session
(3, 'session_token_benjamin_001_secure_random_string_here', 'refresh_token_benjamin_001_secure_random_string_here', '2025-01-18 20:15:00', '2025-01-18 20:15:00', TRUE, 'Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36', '192.168.1.103', '2024-12-18 20:15:00', '2024-12-18 20:15:00', '2024-12-20 11:45:00'),

-- Charlotte Novelist active session
(4, 'session_token_charlotte_001_secure_random_string_here', 'refresh_token_charlotte_001_secure_random_string_here', '2025-01-17 15:30:00', '2025-01-17 15:30:00', TRUE, 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36', '192.168.1.104', '2024-12-17 15:30:00', '2024-12-17 15:30:00', '2024-12-19 22:15:00'),

-- David Historian active session
(5, 'session_token_david_001_secure_random_string_here', 'refresh_token_david_001_secure_random_string_here', '2025-01-16 12:00:00', '2025-01-16 12:00:00', TRUE, 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36', '192.168.1.105', '2024-12-16 12:00:00', '2024-12-16 12:00:00', '2024-12-20 09:30:00'),

-- Emily Philosopher active session
(6, 'session_token_emily_001_secure_random_string_here', 'refresh_token_emily_001_secure_random_string_here', '2025-01-15 19:20:00', '2025-01-15 19:20:00', TRUE, 'Mozilla/5.0 (iPad; CPU OS 17_0 like Mac OS X) AppleWebKit/605.1.15', '192.168.1.106', '2024-12-15 19:20:00', '2024-12-15 19:20:00', '2024-12-20 16:45:00');

-- ======================================================================
-- EMAIL VERIFICATION TOKENS
-- ======================================================================
-- Current pending verification for new user

INSERT INTO email_verifications (user_id, verification_token, verification_type, verified, expiry_date, created_at) VALUES
-- Grace Newuser's pending verification
(8, 'verify_token_grace_001_secure_random_string_here', 'REGISTRATION', FALSE, '2024-12-21 08:00:00', '2024-12-20 08:00:00');

-- ======================================================================
-- DATA VERIFICATION NOTES
-- ======================================================================
-- This sample data provides:
-- 
-- 1. Diverse user base (8 users) with different roles and interests
-- 2. Curated book collection (19 books) spanning all categories with dark academia focus
-- 3. Realistic personal library usage patterns showing different reading behaviors
-- 4. Detailed book ratings and reviews from different user perspectives
-- 5. Search history reflecting each user's interests and discovery patterns
-- 6. Active user sessions simulating real authentication states
-- 7. Pending email verification for testing verification workflows
--
-- Password for all users: 'password123' (hashed with BCrypt)
-- All data is suitable for development and testing environments
-- Book ISBNs and cover URLs are examples - replace with real data in production