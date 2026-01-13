package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.RatingMPA;
import ru.yandex.practicum.filmorate.storage.FilmStorage;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Film> filmRowMapper = (rs, rowNum) -> {
        Film film = new Film();
        film.setId(rs.getLong("id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));

        // ВАЖНО: Загружаем MPA как DTO
        String mpaCode = rs.getString("mpa_rating");
        if (mpaCode != null && !mpaCode.isEmpty()) {
            try {
                // Получаем RatingMPA по коду
                RatingMPA rating = RatingMPA.fromCode(mpaCode);
                // Создаем MpaDto
                film.setMpa(new MpaDto(rating.getId(), rating.getCode(), rating.getDescription()));
            } catch (Exception e) {
                System.err.println("Error loading MPA rating for film " + film.getId() + ": " + e.getMessage());
            }
        } else {
            System.err.println("Warning: mpa_rating is null or empty for film " + film.getId());
        }

        return film;
    };

    @Override
    public List<Film> findAll() {
        String sql = "SELECT * FROM films";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper);
        films.forEach(this::loadFilmData);
        return films;
    }

    @Override
    public Optional<Film> findById(Long id) {
        String sql = "SELECT * FROM films WHERE id = ?";
        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, id);
        films.forEach(this::loadFilmData);
        return films.stream().findFirst();
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_rating) " +
                "VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());

            // Получаем код MPA из DTO
            String mpaCode = null;
            if (film.getMpa() != null) {
                // Проверяем, что MPA ID валидный
                if (film.getMpa().getId() != null) {
                    RatingMPA rating = RatingMPA.fromId(film.getMpa().getId());
                    mpaCode = rating.getCode();
                    // Обновляем DTO с правильными данными
                    film.setMpa(new MpaDto(rating.getId(), rating.getCode(), rating.getDescription()));
                }
            }
            ps.setString(5, mpaCode);
            return ps;
        }, keyHolder);

        film.setId(keyHolder.getKey().longValue());
        saveGenres(film);

        // Загружаем полные данные (включая жанры)
        loadFilmData(film);

        return film;
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, " +
                "duration = ?, mpa_rating = ? WHERE id = ?";

        // Получаем код MPA
        String mpaCode = null;
        if (film.getMpa() != null && film.getMpa().getId() != null) {
            try {
                RatingMPA rating = RatingMPA.fromId(film.getMpa().getId());
                mpaCode = rating.getCode();
                // Обновляем DTO
                film.setMpa(new MpaDto(rating.getId(), rating.getCode(), rating.getDescription()));
            } catch (Exception e) {
                System.err.println("Error updating MPA for film " + film.getId() + ": " + e.getMessage());
            }
        }

        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                mpaCode,
                film.getId());

        // Обновляем жанры
        String deleteGenresSql = "DELETE FROM film_genres WHERE film_id = ?";
        jdbcTemplate.update(deleteGenresSql, film.getId());
        saveGenres(film);

        // Загружаем полные данные
        loadFilmData(film);

        return film;
    }

    @Override
    public void delete(Long id) {
        String sql = "DELETE FROM films WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        String sql = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        String sql = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, COUNT(l.user_id) as like_count " +
                "FROM films f " +
                "LEFT JOIN likes l ON f.id = l.film_id " +
                "GROUP BY f.id " +
                "ORDER BY like_count DESC " +
                "LIMIT ?";

        List<Film> films = jdbcTemplate.query(sql, filmRowMapper, count);
        films.forEach(this::loadFilmData);
        return films;
    }

    private void saveGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            String sql = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";

            // Убираем дубликаты, сохраняя порядок первого вхождения
            List<Long> uniqueGenreIds = film.getGenres().stream()
                    .map(GenreDto::getId)
                    .distinct()
                    .collect(Collectors.toList());

            for (Long genreId : uniqueGenreIds) {
                jdbcTemplate.update(sql, film.getId(), genreId);
            }
        }
    }

    private void loadFilmData(Film film) {
        // Загружаем жанры
        String genresSql = "SELECT genre_id FROM film_genres WHERE film_id = ? ORDER BY genre_id";
        List<Long> genreIds = jdbcTemplate.queryForList(genresSql, Long.class, film.getId());

        List<GenreDto> genres = genreIds.stream()
                .map(genreId -> {
                    try {
                        Genre genre = Genre.fromId(genreId);
                        return new GenreDto(genre.getId(), genre.getName());
                    } catch (Exception e) {
                        System.err.println("Error loading genre " + genreId + " for film " + film.getId());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        film.setGenres(genres);

        // Загружаем MPA если она не была загружена
        if (film.getMpa() == null) {
            String mpaSql = "SELECT mpa_rating FROM films WHERE id = ?";
            try {
                String mpaCode = jdbcTemplate.queryForObject(mpaSql, String.class, film.getId());
                if (mpaCode != null && !mpaCode.isEmpty()) {
                    RatingMPA rating = RatingMPA.fromCode(mpaCode);
                    film.setMpa(new MpaDto(rating.getId(), rating.getCode(), rating.getDescription()));
                }
            } catch (Exception e) {
                System.err.println("Error loading MPA for film " + film.getId() + ": " + e.getMessage());
            }
        }

        // Загружаем лайки
        String likesSql = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likes = jdbcTemplate.queryForList(likesSql, Long.class, film.getId());
        film.setLikes(new HashSet<>(likes));
    }
}