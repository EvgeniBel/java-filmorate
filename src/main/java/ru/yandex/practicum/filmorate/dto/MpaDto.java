package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.modelFilm.RatingMPA;

import java.util.Objects;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpaDto {
    private Long id;
    private String name;
    private String description;

    public static MpaDto fromRatingMPA(RatingMPA rating) {
        Objects.requireNonNull(rating, "RatingMPA не должен быть null");
        return new MpaDto(rating.getId(), rating.getCode(), rating.getDescription());
    }
}
