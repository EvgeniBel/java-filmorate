package ru.yandex.practicum.filmorate.model.modelFilm;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = "id")
public class Film {
    private Long id;

    @NotBlank(message = "Название не может быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @NotNull(message = "Дата релиза должна быть указана")
    private LocalDate releaseDate;

    @Positive(message = "Продолжительность фильма должна быть положительным числом")
    private int duration;
    //Поле жанр
    @NotNull(message = "Жанр должен быть указан")
    private Set<Genre> genres = new LinkedHashSet<>();
   // Поле возрастного рейтинга
    @NotNull(message = "Рейтинг MPA должен быть указан")
    private RatingMPA mpa;
}

