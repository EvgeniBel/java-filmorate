package ru.yandex.practicum.filmorate.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ConditionsNotMetException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
public class FilmController {

    private final static Logger log = LoggerFactory.getLogger(FilmController.class);
    private final Map<Long, Film> films = new HashMap<>();
    private final LocalDate minReleaseDate = LocalDate.of(1895, 12, 12);

    @GetMapping
    public Collection<Film> findAll() {
        log.info("GET /films - получение всех фильмов. Количество фильмов - {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        log.info("POST /films - попытка создания нового фильма: {}", film);
        // проверяем выполнение необходимых условий
        validateId(film);
        checkNameIsExist(film);
        checkDescriptionLength(film);
        checkRealeseDate(film);
        checkDurations(film);

        // формируем дополнительные данные
        film.setId(getNextId());
        // сохраняем новую публикацию в памяти приложения
        films.put(film.getId(), film);
        log.info("Фильм с названием  - {}, успешно создан.", film.getName());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        log.info("PUT /films - попытка обновления фильма: {}", newFilm);
        // проверяем необходимые условия
        validateId(newFilm);
        checkNameIsExist(newFilm);
        checkDescriptionLength(newFilm);
        checkRealeseDate(newFilm);
        checkDurations(newFilm);
        // если фильм найден и все условия соблюдены, обновляем содержимое
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            log.debug("Найден фильм для обновления. Старые данные: {}", oldFilm);
            boolean isUpdated = false;

            if (!newFilm.getName().equals(oldFilm.getName())) {
                log.debug("Обновление названия: '{}' -> '{}'", oldFilm.getName(), newFilm.getName());
                oldFilm.setName(newFilm.getName());
                isUpdated = true;
            }

            if (!newFilm.getDescription().equals(oldFilm.getDescription())) {
                log.debug("Обновление описания");
                oldFilm.setDescription(newFilm.getDescription());
                isUpdated = true;
            }

            if (!newFilm.getReleaseDate().equals(oldFilm.getReleaseDate())) {
                log.debug("Обновление даты релиза: {} -> {}", oldFilm.getReleaseDate(), newFilm.getReleaseDate());
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
                isUpdated = true;
            }

            if (newFilm.getDuration() != oldFilm.getDuration()) {
                log.debug("Обновление продолжительности: {} -> {}", oldFilm.getDuration(), newFilm.getDuration());
                oldFilm.setDuration(newFilm.getDuration());
                isUpdated = true;
            }

            if (isUpdated) {
                log.info("Фильм с ID {} успешно обновлен. Новые данные: {}", newFilm.getId(), oldFilm);
            } else {
                log.info("Фильм с ID {} не требует обновления - данные идентичны", newFilm.getId());
            }
            return oldFilm;
        }
        log.warn("Фильм с ID {} не найден для обновления", newFilm.getId());
        throw new NotFoundException(String.format("Фильм с названием %s" + newFilm.getId() + " не найден", newFilm.getName()));
    }

    private static void validateId(Film film) {
        if (film.getId() == null) {
            log.warn("Попытка создания фильма с указанным ID: {}", film.getId());
            throw new ConditionsNotMetException("Id должен быть указан");
        }
    }

    private static void checkNameIsExist(Film film) {
        if (film.getName() == null || film.getDescription().isBlank()) {
            log.warn("Попытка создания фильма с пустым названием");
            throw new ConditionsNotMetException("Название не может быть пустым");
        }
    }

    private static void checkDescriptionLength(Film film) {
        if (film.getDescription().length() >= 200) {
            log.warn("Попытка создания фильма с превышением длины описания. Текущая длина равна {}",film.getDescription().length());
            throw new ConditionsNotMetException("Максимальная длина описания — 200 символов");
        }
    }

    private void checkRealeseDate(Film film) {
        if (film.getReleaseDate().isBefore(minReleaseDate)) {
            log.warn("Попытка создания фильма с некорректной датой релиза - {}",film.getReleaseDate());
            throw new ConditionsNotMetException("Дата релиза — не раньше 28 декабря 1895 года");
        }
    }

    private static void checkDurations(Film film) {
        if (film.getDuration() <= 0) {
            log.warn("Попытка создания фильма c отрицательным значением продолжительности");
            throw new ConditionsNotMetException("Продолжительность фильма должна быть положительным числом.");
        }
    }

    // вспомогательный метод для генерации идентификатора нового поста
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}

