package ru.yandex.practicum.filmorate.model.modelFilm;

import lombok.Data;
import lombok.EqualsAndHashCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = "id")
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Продолжительность должна быть положительной")
    private int duration;

    private String mpaRating;
    private Set<Genre> genres = new HashSet<>();
    private Set<Long> likes = new HashSet<>();
}

