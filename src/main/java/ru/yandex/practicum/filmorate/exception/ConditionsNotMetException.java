package ru.yandex.practicum.filmorate.exception;
//Не соответсвующие условия
public class ConditionsNotMetException extends RuntimeException{
    public ConditionsNotMetException(String message) {
        super(message);
    }
}