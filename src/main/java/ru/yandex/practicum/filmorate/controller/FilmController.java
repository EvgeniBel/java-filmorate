package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Validated
public class FilmController {

    private final static Logger log = LoggerFactory.getLogger(FilmController.class);
    private final Map<Long, Film> films = new HashMap<>();
    private final LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> findAll() {
        log.info("GET /films - получение всех фильмов. Количество фильмов - {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("POST /films - попытка создания нового фильма: {}", film);
        // проверяем выполнение необходимых условий
        checkNameIsExist(film.getName());
        checkRealeseDate(film.getReleaseDate());
        checkDurations(film.getDuration());
        checkDescriptionLength(film.getDescription());
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
        checkNameIsExist(newFilm.getName());
        validateId(newFilm.getId());
        checkDurations(newFilm.getDuration());
        checkDescriptionLength(newFilm.getDescription());
        checkRealeseDate(newFilm.getReleaseDate());
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
        throw new ValidationException(String.format("Фильм с названием %s" + newFilm.getId() + " не найден", newFilm.getName()));
    }

    private static void validateId(Long id) {
        if (id == null) {
            log.error("Попытка операции с null ID");
            throw new ValidationException("Id должен быть указан");
        }
        log.debug("Валидация ID: {} - OK", id);
    }

    private void checkRealeseDate(LocalDate releaseDate) {
        if (releaseDate == null) {
            log.warn("Попытка создания фильма с null датой релиза");
            throw new ValidationException("Дата релиза должна быть указана");
        }
        // ПРОВЕРКА: дата не раньше 28 декабря 1895 года
        if (releaseDate.isBefore(minReleaseDate)) {
            log.warn("Попытка создания фильма с некорректной датой релиза - {}. Минимальная дата: {}",
                    releaseDate, minReleaseDate);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года");
        }
        if (releaseDate.isAfter(LocalDate.now())) {
            log.warn("Попытка создания фильма с будущей датой релиза - {}", releaseDate);
            throw new ValidationException("Дата релиза не может быть в будущем");
        }
        log.debug("Валидация даты релиза {} - OK", releaseDate);
    }

    private static void checkNameIsExist(String name) {
        if (name == null || name.isBlank()) {
            log.warn("Попытка создания фильма с пустым названием");
            throw new ValidationException("Название не может быть пустым");
        }
    }
    private static void checkDurations(int duration) {
        if (duration <= 0) {
            log.warn("Попытка создания фильма c отрицательным значением продолжительности");
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
    }
    private static void checkDescriptionLength(String description) {
        if (description != null && description.length() > 200) {
            log.warn("Попытка создания фильма с превышением длины описания. Текущая длина равна {}",
                    description.length());
            throw new ValidationException("Максимальная длина описания — 200 символов");
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

