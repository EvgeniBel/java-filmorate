package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
class FilmServiceTest {

    @Autowired
    private FilmService filmService;

    @Test
    void createWithValidFilmTest() {
        Film film = createValidFilm();

        assertDoesNotThrow(() -> filmService.create(film));
    }

    @Test
    void createWithEmptyNameTest() {
        Film film = createValidFilm();
        film.setName("");

        assertThrows(ValidationException.class, () -> filmService.create(film));
    }

    @Test
    void createWithTooEarlyReleaseDateTest() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.of(1890, 1, 1));

        assertThrows(ValidationException.class, () -> filmService.create(film));
    }

    @Test
    void createWithFutureReleaseDate() {
        Film film = createValidFilm();
        film.setReleaseDate(LocalDate.now().plusDays(1));

        assertThrows(ValidationException.class, () -> filmService.create(film));
    }

    private Film createValidFilm() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        return film;
    }
}
