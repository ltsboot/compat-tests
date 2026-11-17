CREATE TABLE book (
    id             BIGSERIAL PRIMARY KEY,
    title          VARCHAR(200) NOT NULL,
    author         VARCHAR(120) NOT NULL,
    published_year INTEGER
);

CREATE INDEX idx_book_author ON book (author);
