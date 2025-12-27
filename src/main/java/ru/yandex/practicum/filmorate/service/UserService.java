package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.modelUser.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

@Service
public class UserService {
    private final UserStorage userStorage;

    @Autowired
    public UserService(@Qualifier("userDbStorage") UserStorage userStorage) {
        this.userStorage = userStorage; // Будет использоваться только UserDbStorage
    }

    public List<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long id) {
        return findUserOrThrow(id);
    }

    public User create(User user) {
        validateUser(user);
        checkNameExist(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        findUserOrThrow(user.getId());
        validateUser(user);
        checkNameExist(user);
        return userStorage.update(user);
    }

    public void delete(Long userId) {
        findUserOrThrow(userId);
        userStorage.delete(userId);
    }

    public void addFriend(Long userId, Long friendId) {
        validateFriendRequest(userId, friendId);
        userStorage.addFriend(userId, friendId);
    }

    public void removeFriend(Long userId, Long friendId) {
        findUserOrThrow(userId);
        findUserOrThrow(friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        findUserOrThrow(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(Long userId, Long otherId) {
        findUserOrThrow(userId);
        findUserOrThrow(otherId);
        return userStorage.getCommonFriends(userId, otherId);
    }

    public void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ @");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

    private void validateFriendRequest(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить самого себя в друзья");
        }
        findUserOrThrow(userId);
        findUserOrThrow(friendId);

        // Проверяем, не отправил ли уже пользователь заявку
        String checkSql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        Integer count = ((org.springframework.jdbc.core.JdbcTemplate) userStorage).queryForObject(
                checkSql, Integer.class, userId, friendId);

        if (count != null && count > 0) {
            throw new ValidationException("Заявка в друзья уже отправлена");
        }
    }

    private void validateFriendConfirmation(Long userId, Long friendId) {
        // Проверяем, что friendId действительно отправил заявку userId
        String checkSql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ? AND status = 'PENDING'";
        Integer count = ((org.springframework.jdbc.core.JdbcTemplate) userStorage).queryForObject(
                checkSql, Integer.class, friendId, userId);

        if (count == null || count == 0) {
            throw new ValidationException("Заявка в друзья не найдена или уже обработана");
        }
    }

    private static void checkNameExist(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    private User findUserOrThrow(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id = %d не найден", userId)));
    }

    public void confirmFriend(Long userId, Long friendId) {
        findUserOrThrow(userId);
        findUserOrThrow(friendId);
        userStorage.confirmFriend(userId, friendId);
    }

    public List<User> getFriendRequests(Long userId) {
        findUserOrThrow(userId);
        return userStorage.getFriendRequests(userId);
    }
}