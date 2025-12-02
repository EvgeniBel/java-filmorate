package ru.yandex.practicum.filmorate.controller;

import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/films")

public class FilmController {
    private final FilmService filmService;

    private static final String FILM_BY_ID = "/{id}";
    private static final String LIKE_PATH = "/{id}/like/{userId}";
    public static final String FILM_POPULAR = "/popular";

    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping(FILM_BY_ID)
    public Film findById(@PathVariable Long id) {
        return filmService.findById(id);
    }

    @PostMapping
    public Film create(@RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film newFilm) {
        return filmService.update(newFilm);
    }

    @PutMapping(LIKE_PATH)
    public void addLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping(LIKE_PATH)
    public void removeLike(@PathVariable Long id, @PathVariable Long userId) {
        filmService.removeLike(id, userId);
    }

    @GetMapping(FILM_POPULAR)
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        return filmService.getPopularFilms(count);
    }
}
