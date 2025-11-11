package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@EqualsAndHashCode(exclude = {"id", "name"})
public class User {
    private Long id;

    @NotBlank(message = "Имейл должен быть указан")
    @Email(message = "Имейл должен содержать символ - @")
    private String email;

    @NotBlank(message = "Логин не может быть пустым")
    private String login;
    private String name;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
