-- Заполнение таблицы жанров (соответствует enum Genre)
MERGE INTO genres (id, name) KEY(id) VALUES (1, 'Комедия');
MERGE INTO genres (id, name) KEY(id) VALUES (2, 'Драма');
MERGE INTO genres (id, name) KEY(id) VALUES (3, 'Мультфильм');
MERGE INTO genres (id, name) KEY(id) VALUES (4, 'Триллер');
MERGE INTO genres (id, name) KEY(id) VALUES (5, 'Документальный');
MERGE INTO genres (id, name) KEY(id) VALUES (6, 'Боевик');

-- Примеры пользователей (для тестирования)
MERGE INTO users (id, email, login, name, birthday) KEY(id) VALUES
(1, 'user1@example.com', 'user1', 'John Doe', '1990-01-15'),
(2, 'user2@example.com', 'user2', 'Jane Smith', '1992-05-20'),
(3, 'user3@example.com', 'user3', 'Bob Johnson', '1985-11-30'),
(4, 'user4@example.com', 'user4', 'Alice Brown', '1995-03-10');

-- Примеры фильмов (для тестирования)
MERGE INTO films (id, name, description, release_date, duration, mpa_rating) KEY(id) VALUES
(1, 'Matrix', 'Science fiction film', '1999-03-31', 136, 'R'),
(2, 'Inception', 'Science fiction action film', '2010-07-16', 148, 'PG-13'),
(3, 'Titanic', 'Romantic disaster film', '1997-12-19', 195, 'PG-13'),
(4, 'Avatar', 'Epic science fiction film', '2009-12-18', 162, 'PG-13');

-- Связи фильмов с жанрами
MERGE INTO film_genres (film_id, genre_id) KEY(film_id, genre_id) VALUES
(1, 6), -- Matrix -> Боевик
(1, 4), -- Matrix -> Триллер
(2, 6), -- Inception -> Боевик
(2, 2), -- Inception -> Драма
(3, 2), -- Titanic -> Драма
(4, 6), -- Avatar -> Боевик
(4, 2); -- Avatar -> Драма

-- Примеры лайков
MERGE INTO likes (film_id, user_id) KEY(film_id, user_id) VALUES
(1, 1), -- User1 likes Matrix
(1, 2), -- User2 likes Matrix
(2, 1), -- User1 likes Inception
(2, 3), -- User3 likes Inception
(3, 2), -- User2 likes Titanic
(4, 1), -- User1 likes Avatar
(4, 2), -- User2 likes Avatar
(4, 3), -- User3 likes Avatar
(4, 4); -- User4 likes Avatar

-- Примеры дружеских отношений
MERGE INTO friends (user_id, friend_id, status) KEY(user_id, friend_id) VALUES
-- User1 отправил заявку User2 (ожидает подтверждения)
(1, 2, 'PENDING'),
-- User1 и User3 - подтвержденные друзья
(1, 3, 'CONFIRMED'),
(3, 1, 'CONFIRMED'), -- Обратная связь для подтвержденной дружбы
-- User2 отправил заявку User4
(2, 4, 'PENDING'),
-- User3 отправил заявку User4
(3, 4, 'PENDING');