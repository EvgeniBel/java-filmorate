package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private UserService userService;

    private User validUser;

    @BeforeEach
    void setUp() {
        validUser = new User();
        validUser.setEmail("test@mail.com");
        validUser.setLogin("testlogin");
        validUser.setName("Test User");
        validUser.setBirthday(LocalDate.of(2000, 1, 1));
    }

    @Test
    void createWithValidUserTest() {
        // Arrange
        when(userStorage.create(any(User.class))).thenReturn(validUser);

        // Act
        User result = userService.create(validUser);

        // Assert
        assertNotNull(result);
        verify(userStorage, times(1)).create(any(User.class));
    }

    @Test
    void createWithInvalidEmailTest() {
        // Arrange
        validUser.setEmail("invalid-email");

        // Act & Assert
        assertThrows(ValidationException.class, () -> userService.create(validUser));
        verify(userStorage, never()).create(any(User.class));
    }

    @Test
    void createWithEmptyEmailTest() {
        // Arrange
        validUser.setEmail("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> userService.create(validUser));
        verify(userStorage, never()).create(any(User.class));
    }

    @Test
    void createWithEmptyLoginTest() {
        // Arrange
        validUser.setLogin("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> userService.create(validUser));
        verify(userStorage, never()).create(any(User.class));
    }

    @Test
    void createLoginWithSpacesTest() {
        // Arrange
        validUser.setLogin("l o g i n");

        // Act & Assert
        assertThrows(ValidationException.class, () -> userService.create(validUser));
        verify(userStorage, never()).create(any(User.class));
    }

    @Test
    void createWithFutureBirthdayTest() {
        // Arrange
        validUser.setBirthday(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(ValidationException.class, () -> userService.create(validUser));
        verify(userStorage, never()).create(any(User.class));
    }

    @Test
    void createNameIsEmptyTest() {
        // Arrange
        validUser.setName("");
        when(userStorage.create(any(User.class))).thenReturn(validUser);

        // Act
        User result = userService.create(validUser);

        // Assert
        assertEquals(validUser.getLogin(), result.getName());
        verify(userStorage, times(1)).create(any(User.class));
    }

    @Test
    void createNameIsNullTest() {
        // Arrange
        validUser.setName(null);
        when(userStorage.create(any(User.class))).thenReturn(validUser);

        // Act
        User result = userService.create(validUser);

        // Assert
        assertEquals(validUser.getLogin(), result.getName());
        verify(userStorage, times(1)).create(any(User.class));
    }
}
