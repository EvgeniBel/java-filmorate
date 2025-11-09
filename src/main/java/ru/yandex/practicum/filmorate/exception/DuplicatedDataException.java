package ru.yandex.practicum.filmorate.exception;
//Дубликат
public class DuplicatedDataException extends RuntimeException{
    public DuplicatedDataException(String message) {
        super(message);
    }
}
