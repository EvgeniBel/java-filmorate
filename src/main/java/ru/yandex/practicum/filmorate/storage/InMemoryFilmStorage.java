package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;
import java.util.stream.Collectors;

public class InMemoryFilmStorage implements FilmStorage{
    private final Map<Long, Film> films = new HashMap<>();
    private final Map<Long, Set<Long>> likes = new HashMap<>();
    private long nextId = 1;

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
        if (!films.containsKey(film.getId())) {
            throw new NoSuchElementException("Фильм с id=" + film.getId() + " не найден");
        }
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
        if (!films.containsKey(filmId)) {
            throw new NoSuchElementException("Фильм с id=" + filmId + " не найден");
        }
        likes.get(filmId).add(userId);
    }

    @Override
    public void removeLike(Long filmId, Long userId) {
        if (!films.containsKey(filmId)) {
            throw new NoSuchElementException("Фильм с id=" + filmId + " не найден");
        }
        likes.get(filmId).remove(userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        return films.values().stream()
                .sorted((f1, f2) -> {
                    int likes1 = likes.get(f1.getId()).size();
                    int likes2 = likes.get(f2.getId()).size();
                    return Integer.compare(likes2, likes1);
                })
                .limit(count)
                .collect(Collectors.toList());
    }
}