package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(FilmDbStorage.class)
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;

    @Test
    void testFindFilmById() {
        Film testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2025, 1, 1));
        testFilm.setDuration(120);

        Film savedFilm = filmStorage.create(testFilm);
        Long filmId = savedFilm.getId();

        Optional<Film> filmOptional = filmStorage.findById(filmId);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(film ->
                        assertThat(film).hasFieldOrPropertyWithValue("id", filmId)
                );
    }

    @Test
    void testFindAllFilms() {
        // Создаем несколько фильмов
        Film film1 = new Film();
        film1.setName("Film 1");
        film1.setDescription("TDescription 1");
        film1.setReleaseDate(LocalDate.of(2025, 1, 1));
        film1.setDuration(120);

        Film film2 = new Film();
        film2.setName("Film 2");
        film2.setDescription("Description 2");
        film2.setReleaseDate(LocalDate.of(2025, 1, 1));
        film2.setDuration(120);

        filmStorage.create(film1);
        filmStorage.create(film2);

        // Получаем все фильмы
        List<Film> films = filmStorage.findAll();

        assertThat(films).hasSize(2);
    }

    @Test
    void testCreateFilm() {
        Film testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2025, 1, 1));
        testFilm.setDuration(150);

        Film savedFilm = filmStorage.create(testFilm);

        assertThat(savedFilm.getId()).isNotNull();
        assertThat(savedFilm.getName()).isEqualTo("Test Film");
        assertThat(savedFilm.getDuration()).isEqualTo(150);
    }

    @Test
    void testUpdateFilm() {
        // Создаем фильм
        Film testFilm = new Film();
        testFilm.setName("Test Film");
        testFilm.setDescription("Test Description");
        testFilm.setReleaseDate(LocalDate.of(2025, 1, 1));
        testFilm.setDuration(120);

        Film savedFilm = filmStorage.create(testFilm);

        // Обновляем фильм
        savedFilm.setName("Updated Name");
        savedFilm.setDuration(180);

        Film updatedFilm = filmStorage.update(savedFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Name");
        assertThat(updatedFilm.getDuration()).isEqualTo(180);
    }
}