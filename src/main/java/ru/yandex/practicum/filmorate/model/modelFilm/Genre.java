package ru.yandex.practicum.filmorate.model.modelFilm;

import lombok.Getter;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@Getter
public enum Genre {
    COMEDY(1L, "Комедия"),
    DRAMA(2L, "Драма"),
    CARTOON(3L, "Мультфильм"),
    THRILLER(4L, "Триллер"),
    DOCUMENTARY(5L, "Документальный"),
    ACTION(6L, "Боевик");

    private final Long id;
    private final String name;

    Genre(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Genre fromId(Long id) {
        for (Genre genre : values()) {
            if (genre.getId().equals(id)) {
                return genre;
            }
        }
        throw new NotFoundException("Неизвестный ID жанра: " + id);
    }

    public static Genre fromName(String name) {
        for (Genre genre : values()) {
            if (genre.getName().equalsIgnoreCase(name)) {
                return genre;
            }
        }
        throw new NotFoundException("Неизвестный жанр: " + name);
    }
}
