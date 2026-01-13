package ru.yandex.practicum.filmorate.model.film;

import lombok.Getter;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

@Getter
public enum RatingMPA {
    G(1L, "G", "Нет возрастных ограничений"),
    PG(2L, "PG", "Детям рекомендуется смотреть с родителями"),
    PG_13(3L, "PG-13", "Детям до 13 лет просмотр не желателен"),
    R(4L, "R", "Лицам до 17 лет просмотр только в присутствии взрослого"),
    NC_17(5L, "NC-17", "Лицам до 18 лет просмотр запрещён");

    private final Long id;
    private final String code;
    private final String description;

    RatingMPA(Long id, String code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }

    public static RatingMPA fromId(Long id) {
        for (RatingMPA rating : values()) {
            if (rating.getId().equals(id)) {
                return rating;
            }
        }
        throw new NotFoundException("Неизвестный ID рейтинга MPA: " + id);
    }

    public static RatingMPA fromCode(String code) {
        for (RatingMPA rating : values()) {
            if (rating.getCode().equals(code)) {
                return rating;
            }
        }
        throw new NotFoundException("Неизвестный рейтинг MPA: " + code);
    }
}
