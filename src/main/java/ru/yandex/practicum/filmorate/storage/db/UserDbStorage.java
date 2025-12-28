package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.modelUser.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Transactional
@Repository("userDbStorage")
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<User> userRowMapper = new RowMapper<User>() {
        @Override
        public User mapRow(ResultSet rs, int rowNum) throws SQLException {
            User user = new User();
            user.setId(rs.getLong("id"));
            user.setEmail(rs.getString("email"));
            user.setLogin(rs.getString("login"));
            user.setName(rs.getString("name"));
            user.setBirthday(rs.getDate("birthday").toLocalDate());
            return user;
        }
    };

    @Override
    public List<User> findAll() {
        String sql = "SELECT * FROM users ORDER BY id";
        return jdbcTemplate.query(sql, userRowMapper);
    }

    @Override
    public Optional<User> findById(Long id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        List<User> users = jdbcTemplate.query(sql, userRowMapper, id);
        return users.stream().findFirst();
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(keyHolder.getKey().longValue());
        return user;
    }

    @Override
    public User update(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                Date.valueOf(user.getBirthday()),
                user.getId());
        return user;
    }

    @Override
    public void delete(Long id) {
        // Сначала удаляем связи
        jdbcTemplate.update("DELETE FROM friends WHERE user_id = ? OR friend_id = ?", id, id);
        jdbcTemplate.update("DELETE FROM likes WHERE user_id = ?", id);
        // Затем удаляем пользователя
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    @Override
    public void addFriend(Long userId, Long friendId) {
        // Проверяем, что не добавляем самого себя
        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить самого себя в друзья");
        }

        // Проверяем существование пользователей
        String checkUsersSql = "SELECT COUNT(*) FROM users WHERE id IN (?, ?)";
        Integer userCount = jdbcTemplate.queryForObject(checkUsersSql, Integer.class, userId, friendId);

        if (userCount == null || userCount != 2) {
            throw new ValidationException("Один или оба пользователя не существуют");
        }

        // Проверяем, не добавлен ли уже друг
        String checkFriendshipSql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        Integer existingCount = jdbcTemplate.queryForObject(checkFriendshipSql, Integer.class, userId, friendId);

        if (existingCount != null && existingCount > 0) {
            throw new ValidationException("Пользователь уже добавлен в друзья");
        }

        // Односторонняя дружба: userId -> friendId
        String sql = "INSERT INTO friends (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);

        System.out.println("Друг добавлен: " + userId + " -> " + friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        int rows = jdbcTemplate.update(sql, userId, friendId);

        if (rows == 0) {
            System.out.println("Друг не найден для удаления: " + userId + " -> " + friendId);
        } else {
            System.out.println("Друг удален: " + userId + " -> " + friendId);
        }
    }

    @Override
    public List<User> getFriends(Long userId) {
        // Получаем всех, кого пользователь добавил в друзья
        String sql = """
                SELECT u.*
                FROM users u
                INNER JOIN friends f ON u.id = f.friend_id
                WHERE f.user_id = ?
                ORDER BY u.id
                """;

        List<User> friends = jdbcTemplate.query(sql, userRowMapper, userId);
        System.out.println("Найдено друзей для пользователя " + userId + ": " + friends.size());
        return friends;
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        // Общие друзья - те, кого оба пользователя добавили в друзья
        String sql = """
                SELECT u.* FROM users u
                WHERE u.id IN (
                    SELECT friend_id FROM friends WHERE user_id = ?
                    INTERSECT
                    SELECT friend_id FROM friends WHERE user_id = ?
                )
                ORDER BY u.id
                """;
        return jdbcTemplate.query(sql, userRowMapper, userId, otherId);
    }

    @Override
    public void confirmFriend(Long userId, Long friendId) {
        // Для односторонней системы подтверждение не требуется
        System.out.println("confirmFriend не используется в односторонней системе друзей");
    }

    @Override
    public List<User> getFriendRequests(Long userId) {
        // Для односторонней системы запросы не используются
        System.out.println("getFriendRequests не используется в односторонней системе друзей");
        return List.of();
    }
}