package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.modelFilm.Film;
import ru.yandex.practicum.filmorate.model.modelFilm.Genre;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, Set<Long>> likes = new HashMap<>();
    private long nextId = 1;
    private Set<Genre> genres = new HashSet<>();

    @Override
    public List<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public Film create(Film film) {
        film.setId(nextId++);
        films.put(film.getId(), film);
        likes.put(film.getId(), new HashSet<>());
        return film;
    }

    @Override
    public Film update(Film film) {
        films.put(film.getId(), film);
        return film;
    }

    @Override
    public void delete(Long id) {
        films.remove(id);
        likes.remove(id);
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        likes.get(filmId).add(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        likes.get(filmId).remove(userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> {
                    int likes1 = likes.getOrDefault(f1.getId(), Collections.emptySet()).size();
                    int likes2 = likes.getOrDefault(f2.getId(), Collections.emptySet()).size();
                    return Integer.compare(likes2, likes1);  // Сортировка по убыванию лайков
                })
                .limit(count)
                .collect(Collectors.toList());
    }

}
