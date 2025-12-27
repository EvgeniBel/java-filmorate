package ru.yandex.practicum.filmorate.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MpaDto {
    private Long id;
    private String name;

    public static MpaDto fromRatingMPA(ru.yandex.practicum.filmorate.model.modelFilm.RatingMPA rating) {
        // Сопоставляем enum с ID (1-5)
        Long id = switch (rating) {
            case G -> 1L;
            case PG -> 2L;
            case PG_13 -> 3L;
            case R -> 4L;
            case NC_17 -> 5L;
        };
        return new MpaDto(id, rating.getCode());
    }
}
