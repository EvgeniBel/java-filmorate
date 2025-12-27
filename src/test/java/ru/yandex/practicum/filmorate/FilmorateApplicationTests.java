package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.modelFilm.Film;
import ru.yandex.practicum.filmorate.model.modelUser.User;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


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
        jdbcTemplate.execute("DELETE FROM likes");
        jdbcTemplate.execute("DELETE FROM friends");
        jdbcTemplate.execute("DELETE FROM film_genres");
        jdbcTemplate.execute("DELETE FROM films");
        jdbcTemplate.execute("DELETE FROM users");

        jdbcTemplate.execute("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");
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
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);

        // Используем MpaDto.fromRatingMPA
        film.setMpa(createMpaDto());

        // Добавляем жанры
        List<GenreDto> genres = Arrays.asList(
                new GenreDto(1L, "Комедия"),
                new GenreDto(2L, "Драма")
        );
        film.setGenres(genres);

        Film createdFilm = filmStorage.create(film);

        // Проверяем создание
        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isNotNull();

        // Дополнительные проверки
        assertThat(createdFilm.getMpa()).isNotNull();
        assertThat(createdFilm.getMpa().getId()).isEqualTo(1L);
        assertThat(createdFilm.getMpa().getName()).isEqualTo("G");

        // Проверяем, что фильм можно найти
        Optional<Film> foundFilm = filmStorage.findById(createdFilm.getId());
        assertThat(foundFilm).isPresent();
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

        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();

        // Создаем фильм
        Film film = new Film();
        film.setName("Film");
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        film.setMpa(createMpaDto());

        Film createdFilm = filmStorage.create(film);

        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isNotNull();

        // Добавляем лайк
        filmStorage.addLike(createdFilm.getId(), createdUser.getId());

        // Проверяем, что лайк добавлен - загружаем фильм и проверяем likes
        Optional<Film> filmAfterLike = filmStorage.findById(createdFilm.getId());
        assertThat(filmAfterLike).isPresent();

        Film filmWithLike = filmAfterLike.get();
        // Предполагая, что у Film есть метод getLikes()
        assertThat(filmWithLike.getLikes()).isNotEmpty();
        assertThat(filmWithLike.getLikes()).contains(createdUser.getId());

        // Удаляем лайк
        filmStorage.removeLike(createdFilm.getId(), createdUser.getId());

        // Проверяем, что лайк удален
        Optional<Film> filmAfterRemove = filmStorage.findById(createdFilm.getId());
        assertThat(filmAfterRemove).isPresent();

        Film filmWithoutLike = filmAfterRemove.get();
        // Проверяем, что likes пустой или не содержит пользователя
        assertThat(filmWithoutLike.getLikes()).doesNotContain(createdUser.getId());
    }

    @Test
    void testGetPopularFilms() {
        // Создаем фильмы
        Long filmId1 = createFilm("Film 1").getId();
        Long filmId2 = createFilm("Film 2").getId();

        // Создаем пользователя
        Long userId = createUser("test@mail.ru").getId();

        // Добавляем лайк только Film 2
        filmStorage.addLike(filmId2, userId);

        List<Film> popular = filmStorage.getPopularFilms(2);
        assertThat(popular).hasSize(2);
        assertThat(popular.get(0).getName()).isEqualTo("Film 2"); // С лайком
        assertThat(popular.get(1).getName()).isEqualTo("Film 1"); // Без лайка
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Description");
        film.setReleaseDate(LocalDate.now().minusYears(1));
        film.setDuration(100);
        film.setMpa(createMpaDto());

        return filmStorage.create(film);
    }

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setLogin("login_" + email);
        user.setName("User");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        return userStorage.create(user);
    }

    private MpaDto createMpaDto() {
        MpaDto mpa = new MpaDto();
        mpa.setId(1L); // G рейтинг
        mpa.setName("G");
        mpa.setDescription("Description");
        return mpa;
    }
}
