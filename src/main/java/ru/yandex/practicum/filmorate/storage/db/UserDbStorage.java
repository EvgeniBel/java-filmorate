package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.modelUser.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
@Qualifier("userDbStorage")
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
        return users.isEmpty() ? Optional.empty() : Optional.of(users.get(0));
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

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
        // Сначала удаляем связи друзей
        jdbcTemplate.update("DELETE FROM friends WHERE user_id = ? OR friend_id = ?", id, id);
        // Затем удаляем пользователя
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }

    // ========== МЕТОДЫ ДЛЯ ОДНОСТОРОННЕЙ ДРУЖБЫ ==========

    @Override
    public void addFriend(Long userId, Long friendId) {
        // Односторонняя дружба: userId отправляет заявку friendId
        String sql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, 'PENDING') " +
                "ON CONFLICT (user_id, friend_id) DO NOTHING";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(Long userId, Long friendId) {
        // Удаление заявки/дружбы (userId удаляет friendId из друзей)
        String sql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void confirmFriend(Long userId, Long friendId) {
        // Пользователь userId подтверждает заявку от friendId
        String checkSql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, friendId, userId);

        if (count == null || count == 0) {
            // Вместо исключения просто ничего не делаем или логируем
            System.out.println("Заявка в друзья не найдена или уже обработана: userId=" + userId + ", friendId=" + friendId);
            return; // Или можно выбросить ValidationException
        }

        // Меняем статус на 'CONFIRMED' (подтверждено)
        String sql = "UPDATE friends SET status = 'CONFIRMED' WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, friendId, userId);
    }

    @Override
    public List<User> getFriendRequests(Long userId) {
        // Заявки, которые другие отправили данному пользователю
        String sql = """
        SELECT u.* FROM users u 
        JOIN friends f ON u.id = f.user_id 
        WHERE f.friend_id = ? AND f.status = 'PENDING'
        """;
        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> getFriends(Long userId) {
        // Получаем только тех, кого пользователь добавил И подтвердил
        String sql = """
        SELECT u.* FROM users u 
        JOIN friends f ON u.id = f.friend_id 
        WHERE f.user_id = ? AND f.status = 'CONFIRMED'
        """;
        return jdbcTemplate.query(sql, userRowMapper, userId);
    }

    @Override
    public List<User> getCommonFriends(Long userId, Long otherId) {
        // Общие друзья - это пользователи, которые являются подтвержденными друзьями обоих
        String sql = """
                SELECT u.* FROM users u 
                JOIN friends f1 ON u.id = f1.friend_id 
                JOIN friends f2 ON u.id = f2.friend_id 
                WHERE f1.user_id = ? AND f2.user_id = ? 
                AND f1.status = 'CONFIRMED' AND f2.status = 'CONFIRMED'
                AND u.id NOT IN (?, ?)
                UNION
                SELECT u.* FROM users u 
                JOIN friends f1 ON u.id = f1.user_id 
                JOIN friends f2 ON u.id = f2.user_id 
                WHERE f1.friend_id = ? AND f2.friend_id = ? 
                AND f1.status = 'CONFIRMED' AND f2.status = 'CONFIRMED'
                AND u.id NOT IN (?, ?)
                ORDER BY id
                """;
        return jdbcTemplate.query(sql, userRowMapper,
                userId, otherId, userId, otherId,
                userId, otherId, userId, otherId);
    }
}