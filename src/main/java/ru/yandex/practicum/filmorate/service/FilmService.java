package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.GenreDto;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.modelFilm.Film;
import ru.yandex.practicum.filmorate.model.modelFilm.Genre;
import ru.yandex.practicum.filmorate.model.modelFilm.RatingMPA;
import ru.yandex.practicum.filmorate.model.modelUser.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public List<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film findById(Long filmId) {
        return findFilm(filmId);
    }

    public Film create(Film film) {
        validateAndProcessFilm(film);
        return filmStorage.create(film);
    }


    public Film update(Film film) {
        findFilm(film.getId());
        validateAndProcessFilm(film);
        return filmStorage.update(film);
    }

    public void delete(Long filmId) {
        findFilm(filmId);
        filmStorage.delete(filmId);
    }

    public void addLike(Long filmId, Long userId) {
        findFilm(filmId);
        findUser(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        findFilm(filmId);
        findUser(userId);
        filmStorage.removeLike(filmId, userId);
    }

    public List<Film> getPopularFilms(Integer count) {
        return filmStorage.getPopularFilms(count);
    }

    public void validatedFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым");
        }
        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов");
        }
        if (film.getReleaseDate() == null) {
            throw new ValidationException("Дата релиза должна быть указана");
        }
        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("MPA рейтинг должен быть указан");
        }
    }

    private Film findFilm(Long filmId) {
        return filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id = %d не найден", filmId)));
    }

    private User findUser(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id = %d не найден", userId)));
    }

    private void validateAndProcessFilm(Film film) {
        validatedFilm(film);
        validateAndProcessMpa(film);
        validateAndProcessGenres(film);
    }

    private void validateAndProcessMpa(Film film) {
        if (film.getMpa() == null || film.getMpa().getId() == null) {
            throw new ValidationException("MPA рейтинг должен быть указан");
        }

        try {
            RatingMPA rating = RatingMPA.fromId(film.getMpa().getId());
            film.setMpa(new MpaDto(rating.getId(), rating.getCode(), rating.getDescription()));
        } catch (NotFoundException e) {
            throw new NotFoundException("Неверный ID рейтинга MPA: " + film.getMpa().getId());
        }
    }

    private void validateAndProcessGenres(Film film) {
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<GenreDto> validatedGenres = new ArrayList<>();
            Set<Long> seenIds = new HashSet<>();

            for (GenreDto genreDto : film.getGenres()) {
                try {
                    Genre genre = Genre.fromId(genreDto.getId());
                    if (!seenIds.contains(genre.getId())) {
                        seenIds.add(genre.getId());
                        validatedGenres.add(new GenreDto(genre.getId(), genre.getName()));
                    }
                } catch (NotFoundException e) {
                    throw new NotFoundException("Неверный ID жанра: " + genreDto.getId());
                }
            }
            film.setGenres(validatedGenres);
        }
    }
}
