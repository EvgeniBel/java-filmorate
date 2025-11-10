package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.bind.MethodArgumentNotValidException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        filmController = new FilmController();
    }

    // Граничные условия для name
    @Test
    void testCreateFilmWithNullName() {
        Film film = new Film();
        film.setName(null); // граничное условие
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(120);

        assertThrows(MethodArgumentNotValidException.class, () -> filmController.create(film));
    }

    @Test
    void testCreateFilmWithEmptyName() {
        Film film = new Film();
        film.setName(""); // граничное условие
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void testCestCreateFilmWithOnlySpacesName() {
        Film film = new Film();
        film.setName("   "); // граничное условие
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    // Граничные условия для description
    @Test
    void testCreateFilmWithNullDescription() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription(null); // граничное условие
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(120);

        assertDoesNotThrow(() -> filmController.create(film));
    }

    @Test
    void testCreateFilmWithEmptyDescription() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription(""); // граничное условие
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(120);

        assertDoesNotThrow(() -> filmController.create(film));
    }

    @Test
    void testCreateFilmWithExactly200CharsDescription() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("A".repeat(200)); // граничное условие
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(120);

        assertDoesNotThrow(() -> filmController.create(film));
    }

    @Test
    void testCreateFilmWith201CharsDescription() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("A".repeat(201)); // граничное условие
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void testCreateFilmWithVeryLongDescription() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("A".repeat(1000)); // граничное условие
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    // Граничные условия для releaseDate
    @Test
    void testCreateFilmWithNullReleaseDate() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(null); // граничное условие
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void testCreateFilmWithExactMinReleaseDate() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28)); // граничное условие
        film.setDuration(120);

        assertDoesNotThrow(() -> filmController.create(film));
    }

    @Test
    void testCreateFilmWithDayBeforeMinReleaseDate() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(1895, 12, 27)); // граничное условие
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void testCreateFilmWithFutureReleaseDate() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.now().plusDays(1)); // граничное условие
        film.setDuration(120);

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    // Граничные условия для duration
    @Test
    void testCreateFilmWithZeroDuration() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(0); // граничное условие

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void testCreateFilmWithNegativeDuration() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(-1); // граничное условие

        assertThrows(ValidationException.class, () -> filmController.create(film));
    }

    @Test
    void testCreateFilmWithVeryLargeDuration() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(Integer.MAX_VALUE); // граничное условие

        assertDoesNotThrow(() -> filmController.create(film));
    }

    @Test
    void testCreateFilmWithMinimalDuration() {
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2025, 1, 1));
        film.setDuration(1); // граничное условие

        assertDoesNotThrow(() -> filmController.create(film));
    }
}
