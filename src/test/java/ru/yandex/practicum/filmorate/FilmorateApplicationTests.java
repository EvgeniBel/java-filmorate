package ru.yandex.practicum.filmorate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.controller.UserController;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class FilmorateApplicationTests {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired(required = false)
    private FilmController filmController;

    @Autowired(required = false)
    private UserController userController;

    @Test
    void contextLoads() {
        // Базовый тест - проверяет что Spring контекст поднимается без ошибок
        assertNotNull(applicationContext, "Spring контекст должен быть загружен");
    }

    @Test
    void testMainControllersAreLoaded() {
        assertNotNull(filmController, "FilmController должен загрузиться в контекст");
        assertNotNull(userController, "UserController должен загрузиться в контекст");
    }

    @Test
    void testMainMethodStartsApplication() {
        assertDoesNotThrow(() -> FilmorateApplication.main(new String[]{}),
                "Метод Main должен запускать приложение без исключений");
    }
}
