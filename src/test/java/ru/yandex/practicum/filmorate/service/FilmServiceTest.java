package ru.yandex.practicum.filmorate.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.modelFilm.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilmServiceTest {

    @Mock
    private FilmStorage filmStorage;

    @Mock
    private UserStorage userStorage;

    @InjectMocks
    private FilmService filmService;

    private Film validFilm;

    @BeforeEach
    void setUp() {
        validFilm = new Film();
        validFilm.setName("Valid Film");
        validFilm.setDescription("Valid Description");
        validFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        validFilm.setDuration(120);
        MpaDto mpa = new MpaDto();
        mpa.setId(1L); // Например, G рейтинг
        validFilm.setMpa(mpa);
    }


    @Test
    void createWithValidFilmTest() {
        // Arrange
        when(filmStorage.create(any(Film.class))).thenReturn(validFilm);

        // Act & Assert
        assertDoesNotThrow(() -> filmService.create(validFilm));
        verify(filmStorage, times(1)).create(any(Film.class));
    }

    @Test
    void createWithEmptyNameTest() {
        // Arrange
        validFilm.setName("");

        // Act & Assert
        assertThrows(ValidationException.class, () -> filmService.create(validFilm));
        verify(filmStorage, never()).create(any(Film.class));
    }

    @Test
    void createWithTooLongDescriptionTest() {
        // Arrange
        validFilm.setDescription("A".repeat(201)); // Больше 200 символов

        // Act & Assert
        assertThrows(ValidationException.class, () -> filmService.create(validFilm));
        verify(filmStorage, never()).create(any(Film.class));
    }

    @Test
    void createWithTooEarlyReleaseDateTest() {
        // Arrange
        validFilm.setReleaseDate(LocalDate.of(1890, 1, 1));

        // Act & Assert
        assertThrows(ValidationException.class, () -> filmService.create(validFilm));
        verify(filmStorage, never()).create(any(Film.class));
    }

    @Test
    void createWithFutureReleaseDateTest() {
        // Arrange
        validFilm.setReleaseDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThrows(ValidationException.class, () -> filmService.create(validFilm));
        verify(filmStorage, never()).create(any(Film.class));
    }

    @Test
    void createWithNegativeDurationTest() {
        // Arrange
        validFilm.setDuration(-10);

        // Act & Assert
        assertThrows(ValidationException.class, () -> filmService.create(validFilm));
        verify(filmStorage, never()).create(any(Film.class));
    }
}