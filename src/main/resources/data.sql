DELETE FROM likes;
DELETE FROM film_genres;
DELETE FROM genres;
DELETE FROM films;
DELETE FROM users;
DELETE FROM friends;
DELETE FROM mpa_ratings;

INSERT INTO mpa_ratings (id, code, description) VALUES
(1, 'G', 'Нет возрастных ограничений'),
(2, 'PG', 'Детям рекомендуется смотреть с родителями'),
(3, 'PG-13', 'Детям до 13 лет просмотр не желателен'),
(4, 'R', 'Лицам до 17 лет просмотр только в присутствии взрослого'),
(5, 'NC-17', 'Лицам до 18 лет просмотр запрещён');

INSERT INTO genres (id, name) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');