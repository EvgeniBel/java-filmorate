package ru.yandex.practicum.filmorate.model.modelFilm;


import lombok.Getter;

@Getter
public enum RatingMPA {
    G("G", "Нет возрастных ограничений"),
    PG("PG", "Детям рекомендуется смотреть с родителями"),
    PG_13("PG-13", "Детям до 13 лет просмотр не желателен"),
    R("R", "Лицам до 17 лет просмотр только в присутствии взрослого"),
    NC_17("NC-17", "Лицам до 18 лет просмотр запрещён");

    private final String code;
    private final String description;

    RatingMPA(String code, String description) {
        this.code = code;
        this.description = description;
    }


    public static RatingMPA fromCode(String code) {
        for (RatingMPA rating : values()) {
            if (rating.getCode().equals(code)) {
                return rating;
            }
        }
        throw new IllegalArgumentException("Неизвестный рейтинг MPA: " + code);
    }

}
