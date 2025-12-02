# java-filmorate
Template repository for Filmorate project.


![Схема базы данных](https://github.com/user-attachments/assets/e51d51df-58f8-41f5-815e-c13b25cb3751)
)

##Ключевые особенности:
#users и films# - основные сущности

#mpa_ratings# и #genres# - справочники с предопределенными значениями

#film_genres# - связь многие-ко-многим (фильм может иметь несколько жанров)

#film_likes# - связь многие-ко-многим (пользователь может лайкнуть много фильмов)

#User_friends# - самосвязь users (пользователь может дружить с пользователем)

###Получение друзей пользователя
SELECT u.*
FROM users u
JOIN user_friends uf ON u.user_id = uf.friend_id
WHERE uf.user_id = :userId
AND uf.is_confirmed = true;

###Добавление друга
INSERT INTO user_friends (user_id, friend_id, is_confirmed)
VALUES (:userId, :friendId, false)
ON CONFLICT (user_id, friend_id) DO UPDATE
SET is_confirmed = false;
