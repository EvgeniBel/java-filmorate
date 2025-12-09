package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.modelUser.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserServiceTest {
    @Autowired
    private UserService userService;

    @Test
    void createWithValidUserTest() {
        User user = createValidUser();

        assertDoesNotThrow(() -> userService.create(user));
    }

    @Test
    void createWithInvalidEmailTest() {
        User user = createValidUser();
        user.setEmail("invalid-email");

        assertThrows(ValidationException.class, () -> userService.create(user));
    }

    @Test
    void createWithEmptyLoginTest() {
        User user = createValidUser();
        user.setLogin("");

        assertThrows(ValidationException.class, () -> userService.create(user));
    }

    @Test
    void createLoginWithSpacesTest() {
        User user = createValidUser();
        user.setLogin("l o g i n");

        assertThrows(ValidationException.class, () -> userService.create(user));
    }

    @Test
    void createWithFutureBirthdayTest() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> userService.create(user));
    }

    @Test
    void createNameIsEmptyTest() {
        User user = createValidUser();
        user.setName("");

        User result = userService.create(user);

        assertEquals(user.getLogin(), result.getName());
    }

    @Test
    void createNameIsNullTest() {
        User user = createValidUser();
        user.setName(null);

        User result = userService.create(user);

        assertEquals(user.getLogin(), result.getName());
    }

    private User createValidUser() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testlogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(2025, 1, 1));
        return user;
    }
}