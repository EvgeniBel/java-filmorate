package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.film.Genre;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreDto {
    private Long id;
    private String name;

    public static GenreDto fromGenre(Genre genre) {
        Objects.requireNonNull(genre, "Genre не должен быть null");
        return new GenreDto(genre.getId(), genre.getName());
    }
}
