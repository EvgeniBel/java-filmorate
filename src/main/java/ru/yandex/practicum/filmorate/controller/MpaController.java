package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.modelFilm.RatingMPA;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mpa")
public class MpaController {

    @GetMapping
    public List<MpaDto> getAllMpaRatings() {
        return Arrays.stream(RatingMPA.values())
                .map(MpaDto::fromRatingMPA)
                .sorted((m1, m2) -> m1.getId().compareTo(m2.getId())) // Сортировка по ID
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    private MpaDto getRatingById(@PathVariable Long id) {
        try {
            RatingMPA rating = switch (id.intValue()) {
                case 1 -> RatingMPA.G;
                case 2 -> RatingMPA.PG;
                case 3 -> RatingMPA.PG_13;
                case 4 -> RatingMPA.R;
                case 5 -> RatingMPA.NC_17;
                default -> throw new IllegalArgumentException("Рейтинг MPA с id=" + id + " не найден");
            };
            return MpaDto.fromRatingMPA(rating);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Рейтинг MPA не найден", e);
        }
    }
}
