package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenreDto {
    private Long id;
    private String name;

    public static GenreDto fromGenre(ru.yandex.practicum.filmorate.model.modelFilm.Genre genre) {
        return new GenreDto(genre.getId(), genre.getName());
    }
}