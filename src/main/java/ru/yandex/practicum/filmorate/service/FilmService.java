package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.modelFilm.Film;
import ru.yandex.practicum.filmorate.model.modelUser.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.List;

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
        return findFilmOrThrow(filmId);
    }

    public Film create(Film film) {
        validatedFilm(film);
        return filmStorage.create(film);
    }

    public Film update(Film film) {
        findFilmOrThrow(film.getId());
        validatedFilm(film);
        return filmStorage.update(film);
    }

    public void delete(Long filmId) {
        findFilmOrThrow(filmId);
        filmStorage.delete(filmId);
    }

    public void addLike(Long filmId, Long userId) {
        findFilmOrThrow(filmId);
        findUserOrThrow(userId);
        filmStorage.addLike(filmId, userId);
    }

    public void removeLike(Long filmId, Long userId) {
        findFilmOrThrow(filmId);
        findUserOrThrow(userId);
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
        if (film.getReleaseDate().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата релиза не может быть в будущем");
        }
        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом");
        }
    }

    private Film findFilmOrThrow(Long filmId) {
        return filmStorage.findById(filmId)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id = %d не найден", filmId)));
    }

    private User findUserOrThrow(Long userId) {
        return userStorage.findById(userId)
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id = %d не найден", userId)));
    }
}
