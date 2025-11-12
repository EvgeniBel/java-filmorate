package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Validated
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("GET /users - получение всех пользователей. Количество - {}", users.size());
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("POST /users - создание нового пользователя: {}", user);
        // проверяем выполнение необходимых условий
        checkNameExist(user);
        validateEmail(user.getEmail());
        checkEmailDuplicate(user.getEmail());
        validateLogin(user.getLogin());
        validateBirthday(user.getBirthday());
        // формируем дополнительные данные
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Пользователь успешно создан: ID={}, Email={}, Login={}", user.getId(), user.getEmail(), user.getLogin());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        log.info("PUT /users - обновление пользователя: {}", newUser);
        // проверяем необходимые условия
        validateEmail(newUser.getEmail());
        validateId(newUser.getId());
        validateEmail(newUser.getEmail());
        validateBirthday(newUser.getBirthday());

        if (!users.containsKey(newUser.getId())) {
            log.warn("Пользователь с ID={} не найден при попытке обновления", newUser.getId());
            throw new ValidationException(String.format("User с id = %d не найден",newUser.getId()));
        }

        User oldUser = users.get(newUser.getId());
        log.debug("Найден пользователь для обновления: {}", oldUser);

        if (newUser.getEmail() != null && !newUser.getEmail().equals(oldUser.getEmail())) {
            checkEmailDuplicate(newUser.getEmail());
        }
        boolean updated = false;
        if (newUser.getEmail() != null && !newUser.getEmail().equals(oldUser.getEmail())) {
            log.debug("Обновление email: {} -> {}", oldUser.getEmail(), newUser.getEmail());
            oldUser.setEmail(newUser.getEmail());
            updated = true;
        }
        if (newUser.getName() != null) {
            log.debug("Обновление name: {} -> {}", oldUser.getName(), newUser.getName());
            oldUser.setName(newUser.getName());
            updated = true;
        }
        if (newUser.getLogin() != null && !newUser.getLogin().equals(oldUser.getLogin())) {
            log.debug("Обновление login: {} -> {}", oldUser.getLogin(), newUser.getLogin());
            oldUser.setLogin(newUser.getLogin());
            updated = true;
        }
        if (newUser.getBirthday() != null && !newUser.getBirthday().equals(oldUser.getBirthday())) {
            log.debug("Обновление birthday: {} -> {}", oldUser.getBirthday(), newUser.getBirthday());
            oldUser.setBirthday(newUser.getBirthday());
            updated = true;
        }
        if (updated) {
            log.info("Пользователь с ID={} успешно обновлен: {}", newUser.getId(), oldUser);
        } else {
            log.info("Пользователь с ID={} не требует обновления - данные идентичны", newUser.getId());
        }
        return oldUser;
    }

    private void validateId(Long id) {
        log.error("Попытка операции с null ID");
        if (id == null) {
            throw new ValidationException("Id должен быть указан");
        }
        log.debug("Валидация ID: {} - OK", id);
    }

    private void checkEmailDuplicate(String email) {
        boolean emailExist = users.values()
                .stream()
                .anyMatch(existUser -> email.equals(existUser.getEmail()));
        if (emailExist) {
            log.warn("Попытка использования существующего email: {}", email);
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
        log.debug("Проверка уникальности email: {} - OK", email);
    }

    private static void checkNameExist(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("Имя пользователя пустое, устанавливается login: {}", user.getLogin());
            user.setName(user.getLogin());
        }
        log.debug("Имя пользователя установлено: {}", user.getName());
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ValidationException("Имейл должен быть указан");
        }
        if (!email.contains("@")) {
            log.error("Валидация email: {} не содержит символ @", email);
            throw new ValidationException("Имейл должен содержать символ - @");
        }
        log.debug("Валидация email: {} - OK", email);
    }

    private void validateLogin(String login) {
        if (login == null || login.isBlank() || login.contains(" ")) {
            log.error("Валидация login: login пустой (null) или содержит пробелы ");
            throw new ValidationException("Логин не может быть пустым и содержать пробелы");
        }
        log.debug("Валидация login: {} - OK", login);
    }

    private void validateBirthday(LocalDate birthday) {
        if (birthday == null) {
            log.error("Валидация birthday: дата рождения null");
            throw new ValidationException("Дата рождения должна быть указана");
        }
        if (birthday.isAfter(LocalDate.now())) {
            log.error("Валидация birthday: {} находится в будущем", birthday);
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
        log.debug("Валидация birthday: {} - OK", birthday);
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}