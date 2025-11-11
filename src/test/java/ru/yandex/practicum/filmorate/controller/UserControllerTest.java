package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class UserControllerTest {

    @Autowired
    private UserController userController;

    @BeforeEach
    void setUp() {
        userController = new UserController();
    }

    // Граничные условия для email
    @Test
    void testCreateUserWithNullEmail() {
        User user = new User();
        user.setEmail(null); // граничное условие
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1993, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void testCreateUserWithEmptyEmail() {
        User user = new User();
        user.setEmail(""); // граничное условие
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1993, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void testCreateUserWithOnlySpacesEmail() {
        User user = new User();
        user.setEmail("   "); // граничное условие
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1993, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void testCreateUserWithEmailMissingAtSymbol() {
        User user = new User();
        user.setEmail("user.com"); // граничное условие
        user.setLogin("login");
        user.setBirthday(LocalDate.of(1993, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    // Граничные условия для login
    @Test
    void testCreateUserWithNullLogin() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin(null); // граничное условие
        user.setBirthday(LocalDate.of(1993, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void testCreateUserWithEmptyLogin() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin(""); // граничное условие
        user.setBirthday(LocalDate.of(1993, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void testCreateUserWithOnlySpacesLogin() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("   "); // граничное условие
        user.setBirthday(LocalDate.of(1993, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void testCreateUserWithLoginStartingWithSpace() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin(" login"); // граничное условие
        user.setBirthday(LocalDate.of(1993, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void testCreateUserWithLoginEndingWithSpace() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("login "); // граничное условие
        user.setBirthday(LocalDate.of(1993, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void testCreateUserWithLoginContainingMultipleSpaces() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("l o g i n"); // граничное условие
        user.setBirthday(LocalDate.of(1993, 1, 1));

        assertThrows(ValidationException.class, () -> userController.create(user));
    }


    // Граничные условия для birthday
    @Test
    void testCreateUserWithNullBirthday() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("login");
        user.setBirthday(null); // граничное условие

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void testCreateUserWithTodayBirthday() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.now()); // граничное условие

        assertDoesNotThrow(() -> userController.create(user));
    }

    @Test
    void testCreateUserWithTomorrowBirthday() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.now().plusDays(1)); // граничное условие

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void createUser_WithFarFutureBirthday_ShouldThrowException() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.now().plusYears(100)); // граничное условие

        assertThrows(ValidationException.class, () -> userController.create(user));
    }

    @Test
    void testCreateUserWithVeryOldBirthday() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("testlogin");
        user.setBirthday(LocalDate.of(1900, 1, 1)); // граничное условие

        assertDoesNotThrow(() -> userController.create(user));
    }

    // Граничные условия для name
    @Test
    void testCreateUserWithEmptyName() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("login");
        user.setName(""); // граничное условие
        user.setBirthday(LocalDate.of(1993, 1, 1));

        User createdUser = userController.create(user);

        assertEquals("login", createdUser.getName());
    }

    @Test
    void testCreateUserWithOnlySpacesName() {
        User user = new User();
        user.setEmail("test@mail.com");
        user.setLogin("login");
        user.setName("   "); // граничное условие
        user.setBirthday(LocalDate.of(1993, 1, 1));

        User createdUser = userController.create(user);

        assertEquals("login", createdUser.getName());
    }
}
