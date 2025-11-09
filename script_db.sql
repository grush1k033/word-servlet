CREATE DATABASE dictionary_db;
USE dictionary_db;

CREATE TABLE dictionary (
    id INT AUTO_INCREMENT PRIMARY KEY,
    russian_word VARCHAR(100) NOT NULL,
    english_word VARCHAR(100) NOT NULL
);

INSERT INTO dictionary (russian_word, english_word) VALUES
('привет', 'hello'),
('мир', 'world'),
('программа', 'program'),
('компьютер', 'computer'),
('язык', 'language');