package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Validated
public class FilmController {
    private final FilmService filmService;


    private final Map<Long, Film> films = new HashMap<>();
    private final LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> findAll() {
        log.info("GET /films - получение всех фильмов. Количество фильмов - {}", films.size());
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
      return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
    }



    }

    // вспомогательный метод для генерации идентификатора нового поста

}
