package ru.yandex.practicum.filmorate.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.dto.MpaDto;
import ru.yandex.practicum.filmorate.model.film.RatingMPA;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/mpa")
public class MpaController {

    @GetMapping
    public List<MpaDto> getAllMpaRatings() {
        return Arrays.stream(RatingMPA.values())
                .map(MpaDto::fromRatingMPA)
                .sorted(Comparator.comparing(MpaDto::getId))
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public MpaDto getRatingById(@PathVariable Long id) {
        try {
            RatingMPA rating = RatingMPA.fromId(id);
            return MpaDto.fromRatingMPA(rating);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Рейтинг MPA не найден", e);
        }
    }
}
