package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.filmorate.model.film.RatingMPA;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpaDto {
    @NotNull(message = "ID рейтинга MPA не может быть пустым")
    private Long id;

    private String name;
    private String description;

    public static MpaDto fromRatingMPA(RatingMPA rating) {
        return new MpaDto(rating.getId(), rating.getCode(), rating.getDescription());
    }
}
