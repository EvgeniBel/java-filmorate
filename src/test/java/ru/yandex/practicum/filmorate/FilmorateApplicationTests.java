package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.model.modelFilm.*;
import ru.yandex.practicum.filmorate.model.modelUser.User;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.yandex.practicum.filmorate.model.modelFilm.RatingMPA.*;


@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({FilmDbStorage.class, UserDbStorage.class}) // ЯВНО импортируем классы для тестов
class FilmorateApplicationTests {
    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // Очищаем таблицы перед каждым тестом
        jdbcTemplate.execute("DELETE FROM friends");
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM users");

        // Вставляем жанры, если их нет
        jdbcTemplate.execute("""
                MERGE INTO genres (id, name) KEY(id) VALUES (1, 'Комедия')
                """);
        jdbcTemplate.execute("""
                MERGE INTO genres (id, name) KEY(id) VALUES (2, 'Драма')
                """);
    }

    @Test
    void testCreateAndFindUser() {
        // Создаем пользователя
        User user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("testLogin");
        user.setName("Test User");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        // Проверяем создание
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();

        // Ищем пользователя по ID
        Optional<User> foundUser = userStorage.findById(createdUser.getId());

        assertThat(foundUser)
                .isPresent()
                .hasValueSatisfying(u -> {
                    assertThat(u.getId()).isEqualTo(createdUser.getId());
                    assertThat(u.getEmail()).isEqualTo("test@mail.ru");
                });
    }

    @Test
    void testCreateAndFindFilm() {
        // Создаем фильм
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(G);

        // Добавляем жанры
        film.setGenres(new java.util.HashSet<>());
        film.getGenres().add(Genre.fromId(1L)); // Комедия
        film.getGenres().add(Genre.fromId(2L)); // Драма

        Film createdFilm = filmStorage.create(film);

        // Проверяем создание
        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isNotNull();

        // Ищем фильм по ID
        Optional<Film> foundFilm = filmStorage.findById(createdFilm.getId());

        assertThat(foundFilm)
                .isPresent()
                .hasValueSatisfying(f -> {
                    assertThat(f.getId()).isEqualTo(createdFilm.getId());
                    assertThat(f.getName()).isEqualTo("Test Film");
                    assertThat(f.getGenres()).hasSize(2);
                });
    }

    @Test
    void testAddAndRemoveLike() {
        // Создаем пользователя
        User user = new User();
        user.setEmail("user@mail.ru");
        user.setLogin("userLogin");
        user.setName("User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        // Создаем фильм
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(G);
        Film createdFilm = filmStorage.create(film);

        // Добавляем лайк
        filmStorage.addLike(createdFilm.getId(), createdUser.getId());

        // Проверяем, что фильм найден
        Optional<Film> foundFilm = filmStorage.findById(createdFilm.getId());
        assertThat(foundFilm).isPresent();

        // Удаляем лайк
        filmStorage.removeLike(createdFilm.getId(), createdUser.getId());
    }

    @Test
    void testGetPopularFilms() {
        // Создаем несколько фильмов
        for (int i = 1; i <= 3; i++) {
            Film film = new Film();
            film.setName("Film " + i);
            film.setDescription("Description " + i);
            film.setReleaseDate(LocalDate.of(2020, i, i));
            film.setDuration(100 + i);
            film.setMpa(G);
            filmStorage.create(film);
        }

        // Получаем популярные фильмы
        List<Film> popularFilms = filmStorage.getPopularFilms(2);

        assertThat(popularFilms).hasSize(2);
    }
}
