package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@RequestBody User user) {
        // проверяем выполнение необходимых условий
        validateEmail(user.getEmail());
        checkEmailDublicate(user.getEmail());
        // формируем дополнительные данные
        user.setId(getNextId());

        users.put(user.getId(), user);
        return user;
    }

    @PutMapping
    public User update(@RequestBody User newUser) {
        // проверяем необходимые условия
        validateId(newUser);

        if (!users.containsKey(newUser.getId())) {
            throw new NotFoundException("User с id = " + newUser.getId() + " не найден");
        }

        User oldUser = users.get(newUser.getId());
        if (newUser.getEmail() != null && !newUser.getEmail().equals(oldUser.getEmail())) {
            checkEmailDublicate(newUser.getEmail());
            oldUser.setEmail(newUser.getEmail()); // обновляем только если прошел проверку
        }
        if (newUser.getName() != null) {
            oldUser.setName(newUser.getName());
        }
        if (newUser.getLogin() != null) {
            oldUser.setLogin(newUser.getLogin());
        }
        return oldUser;
    }

    private void validateId(User newUser) {
        if (newUser.getId() == null) {
            throw new ConditionsNotMetException("Id должен быть указан");
        }
    }

    private void checkEmailDublicate(String email) {
        boolean emailExist = users.values()
                .stream()
                .anyMatch(existUser -> email.equals(existUser.getEmail()));
        if (emailExist) {
            throw new DuplicatedDataException("Этот имейл уже используется");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new ConditionsNotMetException("Имейл должен быть указан");
        }
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
